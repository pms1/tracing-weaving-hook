package tracing;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A {@link ClassVisitor} that instruments all methods with
 * {@link TracingMethodVisitor}.
 * 
 * @author pms1
 */
public class TracingClassVisitor extends ClassVisitor {
	private String className;

	public TracingClassVisitor(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		this.className = name;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv;
		mv = cv.visitMethod(access, name, desc, signature, exceptions);
		mv = new TracingMethodVisitor(Opcodes.ASM5, className, access, name, desc, mv);
		return mv;
	}
}
