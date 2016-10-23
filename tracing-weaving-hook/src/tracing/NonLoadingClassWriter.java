package tracing;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * A {@link ClassWriter} that implements
 * {@link #getCommonSuperClass(String, String)} without loading classes into a
 * {@link ClassLoader}.
 * 
 * This implementation is likely to be sub-optimal regarding performance or correctness.
 *  
 * @author pms1
 */
final class NonLoadingClassWriter extends ClassWriter {
	static class Metaclass {
		final String className;
		final String superClass;
		final String[] interfaces;

		Metaclass(String className, String superClass, String[] interfaces) {
			this.className = className;
			this.superClass = superClass;
			this.interfaces = interfaces;
		}
	}

	static Metaclass load(String className, ClassLoader classLoader) {

		try (InputStream is = classLoader.getResourceAsStream(className + ".class")) {
			if (is != null) {
				ClassReader cr = new ClassReader(is);

				return new Metaclass(cr.getClassName(), cr.getSuperName(), cr.getInterfaces());
			}
		} catch (IOException e) {
			throw new Error(e);
		}

		throw new Error("no class " + className);
	}

	private final Map<String, Metaclass> classes = new HashMap<>();
	private final ClassLoader cl;

	NonLoadingClassWriter(ClassReader classReader, int flags, ClassLoader cl) {
		super(classReader, flags);
		this.cl = cl;
	}

	private Metaclass get(String name) {
		return classes.computeIfAbsent(name, c -> load(c, cl));
	}

	private Set<String> all(Metaclass c) {
		Set<String> r = new HashSet<>();
		LinkedList<Metaclass> todo = new LinkedList<>();
		todo.add(c);

		while (!todo.isEmpty()) {
			c = todo.removeFirst();
			if (r.contains(c.className))
				continue;
			if (c.superClass != null)
				todo.add(get(c.superClass));
			for (String s : c.interfaces)
				todo.add(get(s));
			r.add(c.className);
		}
		return r;
	}

	protected String getCommonSuperClass(String type1, String type2) {
		if (type1.equals(type2))
			return type1;

		if (type1.equals("java/lang/Object")) {
			System.err.println("GCSC4 " + type1 + " " + type2 + " -> " + type1);
			return type1;
		}
		if (type2.equals("java/lang/Object")) {
			System.err.println("GCSC5 " + type1 + " " + type2 + " -> " + type2);
			return type2;
		}

		Metaclass c1 = get(type1);
		Set<String> all1 = all(c1);

		if (all1.contains(type2)) {
			System.err.println("GCSC1 " + type1 + " " + type2 + " -> " + type2);
			return type2;
		}

		Metaclass c2 = get(type2);
		Set<String> all2 = all(c2);

		if (all2.contains(type1)) {
			System.err.println("GCSC2 " + type1 + " " + type2 + " -> " + type1);
			return type1;
		}

		all1.retainAll(all2);

		// At this point, all1 contains all common superclasses and interfaces.
		// Probably the lowest common class should be searched, but it seems
		// that any will suffice as long as it is not Object or Serializable.
		// Most likely this is *NOT* true and this code needs to be better.
		all1.remove("java/lang/Object");
		boolean hasSerializeable = all1.remove("java/io/Serializable");

		if (!all1.isEmpty()) {
			System.err.println("GCSC3 " + type1 + " " + type2 + " -> " + all1);
			return all1.iterator().next();
		} else if (hasSerializeable) {
			System.err.println("GCSC3 " + type1 + " " + type2 + " -> java/io/Serializable [" + all1 + "]");
			return "java/io/Serializable";
		} else {
			System.err.println("GCSC3 " + type1 + " " + type2 + " -> java/lang/Object [" + all1 + "]");
			return "java/lang/Object";
		}

		// if(true)
		// super.getCommonSuperClass(type1, type2);
		//
		// Class<?> c, d;
		// ClassLoader classLoader = wc.getBundleWiring().getClassLoader();
		// try {
		// c = Class.forName(type1.replace('/', '.'), false, classLoader);
		// d = Class.forName(type2.replace('/', '.'), false, classLoader);
		// } catch (Exception e) {
		// throw new RuntimeException(e.toString());
		// }
		// if (c.isAssignableFrom(d)) {
		// return type1;
		// }
		// if (d.isAssignableFrom(c)) {
		// return type2;
		// }
		// if (c.isInterface() || d.isInterface()) {
		// return "java/lang/Object";
		// } else {
		// do {
		// c = c.getSuperclass();
		// } while (!c.isAssignableFrom(d));
		// return c.getName().replace('.', '/');
		// }
	}
}