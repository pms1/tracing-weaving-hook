package tracing;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * A {@link MethodVisitor} that instruments code for tracing
 * 
 * @see https://gist.github.com/VijayKrishna/1ca807c952187a7d8c4d
 *
 */
class TracingMethodVisitor extends AdviceAdapter {
	private final Label startFinally = new Label();

	private final String owner;
	private final String name;
	private final int access;
	
	public TracingMethodVisitor(int api, String owner, int access, String name, String desc, MethodVisitor mv) {
		super(Opcodes.ASM5, mv, access, name, desc);
		this.owner = owner;
		this.name = name;
		this.access = access;
	}

	private void invokeTracer(String method, String firstArg) {
		if ((access & Opcodes.ACC_STATIC) != 0) 
			mv.visitInsn(ACONST_NULL);
		else
			loadThis();
		
		mv.visitLdcInsn(owner);
		mv.visitLdcInsn(name);
		mv.visitLdcInsn(methodDesc);
		
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "tracing/Tracer", method,
				"(" + firstArg + "Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
	}

	@Override
	protected void onMethodEnter() {
		loadArgArray();
		invokeTracer("enter", "[Ljava/lang/Object;");
		visitLabel(startFinally);
	}

	public void visitMaxs(int maxStack, int maxLocals) {
		Label endFinally = new Label();
		mv.visitTryCatchBlock(startFinally, endFinally, endFinally, "java/lang/Throwable");
		mv.visitLabel(endFinally);

		dup();
		invokeTracer("exitException", "Ljava/lang/Throwable;");
		mv.visitInsn(ATHROW);
		mv.visitMaxs(maxStack, maxLocals);
	}

	@Override
	protected void onMethodExit(int opcode) {
		String method;
		String firstArg;

		if (opcode == RETURN) {
			method = "exitReturn";
			firstArg = "";
		} else if (opcode == ARETURN) {
			dup();
			method = "exitReturn";
			firstArg = "Ljava/lang/Object;";
		} else if (opcode == ATHROW) {
			dup();
			method = "exitThrow";
			firstArg = "Ljava/lang/Throwable;";
		} else {
			if (opcode == LRETURN || opcode == DRETURN)
				dup2();
			else
				dup();
			box(Type.getReturnType(this.methodDesc));
			method = "exitReturn";
			firstArg = "Ljava/lang/Object;";
		}

		invokeTracer(method, firstArg);
	}
}