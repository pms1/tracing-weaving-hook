package tracing;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

/**
 * A {@link WeavingHook} that instruments classes for tracing.
 * 
 * @author pms1
 */
public class TracingWeavingHook implements WeavingHook {

	private long total;
	private long totalnum;

	private final static String tracerPackage = Tracer.class.getPackage().getName();

	@Override
	public void weave(WovenClass wc) {
		// don't try to weave or own classes or their dependencies
		if (wc.getClassName().startsWith("org.objectweb.asm."))
			return;
		if (wc.getClassName().startsWith("tracing."))
			return;
		if (wc.getClassName().startsWith("tracing."))
			return;

		System.out.println("Weaving " + wc + " " + wc.getBundleWiring().getClassLoader());

		if (!wc.getDynamicImports().contains(tracerPackage))
			wc.getDynamicImports().add(tracerPackage);

		long start = System.nanoTime();

		ClassReader cr = new ClassReader(wc.getBytes());
		ClassWriter cw = new NonLoadingClassWriter(cr,
				ClassReader.EXPAND_FRAMES | ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
				wc.getBundleWiring().getClassLoader());
		ClassVisitor returnAdapter = new TracingClassVisitor(cw);
		cr.accept(returnAdapter, ClassReader.EXPAND_FRAMES);
		byte[] newBytecode = cw.toByteArray();

		long end = System.nanoTime();
		total += (end - start);
		totalnum++;

		// Modify instrumented class
		wc.setBytes(newBytecode);

		System.out.println("Woven " + totalnum + " classes in " + total);
	}

}
