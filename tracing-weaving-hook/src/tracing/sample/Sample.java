package tracing.sample;

public class Sample {

	public Sample() {
		System.err.println("CONSTRUCTOR");
	}

	public void returnVoid() {
		System.err.println("RETURN VOID");
	}

	public double returnDouble() {
		System.err.println("RETURN Double");
		return 42.0;
	}

	public int returnInt() {
		System.err.println("RETURN Int");
		return 42;
	}

	public String returnString() {
		System.err.println("RETURN String");
		return "42";
	}

	public void returnThrow() {
		System.err.println("RETURN Throw");
		throw new IllegalArgumentException();
	}

	public void returnThrow2() {
		returnThrow22();
	}

	private void returnThrow22() {
		throw new IllegalArgumentException();
	}

	public void withArgs() {
		withArg(42, "foo", 4L, 53);
	}

	private void withArg(int i, String string, long l, int j) {

	}
}
