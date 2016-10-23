package tracing.sample;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import tracing.Tracer;
import tracing.TracingClassVisitor;

public class SampleMain {

	public static void main(String[] args) throws Exception {

		String className = Sample.class.getName();

		// adapting the class.
		ClassReader cr = new ClassReader(
				TracingClassVisitor.class.getClassLoader().getResourceAsStream(className.replace('.', '/') + ".class"));
		ClassWriter cw = new ClassWriter(
				ClassReader.EXPAND_FRAMES | ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		TracingClassVisitor returnAdapter = new TracingClassVisitor(cw);
		cr.accept(returnAdapter, ClassReader.EXPAND_FRAMES);
		byte[] instrumented = cw.toByteArray();

		// Files.write(Paths.get("c:/temp/foo.class"), instrumented);

		Class<?> class1 = new ClassLoader() {
			{
				defineClass(className, cw.toByteArray(), 0, instrumented.length);
			}
		}.loadClass(className);

		Tracer.debug = true;

		Object o = class1.newInstance();

		for (Method m : o.getClass().getMethods()) {
			if (m.getDeclaringClass() == Object.class)
				return;
			try {
				System.err.println("CALLING " + m);
				Object invoke = m.invoke(o);
				System.err.println("RETURN " + invoke);
			} catch (InvocationTargetException e) {
				System.err.println("EXCEPTION " + e.getCause());
			}
		}
	}

}
