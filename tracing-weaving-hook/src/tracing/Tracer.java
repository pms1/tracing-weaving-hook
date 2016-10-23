package tracing;

import java.util.Arrays;

public class Tracer {
	public static boolean debug = false;

	public static void enter(Object[] args, Object instance, String clazz, String method, String signature) {
		if (debug)
			System.err.println("ENTER " + instance + " " + clazz + " " + method + " " + signature + " " + Arrays.toString(args));
	}

	public static void exitReturn(Object o, Object instance, String clazz, String method, String signature) {
		if (debug)
			System.err.println("EXIT " + instance + " " + clazz + " " + method + " " + signature + " " + o);
	}

	public static void exitReturn(Object instance, String clazz, String method, String signature) {
		if (debug)
			System.err.println("EXIT " + instance + " " + clazz + " " + method + " " + signature);
	}

	public static void exitException(Throwable e, Object instance, String clazz, String method, String signature) {
		if (debug)
			System.err.println("EXIT EXCEPTION " + instance + " " + clazz + " " + method + " " + signature + " " + e);
	}

	public static void exitThrow(Throwable e, Object instance, String clazz, String method, String signature) {
		if (debug)
			System.err.println("EXIT THROW " + instance + " " + clazz + " " + method + " " + signature + " " + e);
	}
}
