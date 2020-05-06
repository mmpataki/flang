package flang.impl;

public class TokPair {
	String f, s;

	public TokPair(String f, String s) {
		this.f = f;
		this.s = s;
	}

	public String toktyp() {
		return f;
	}

	public String tok() {
		return s;
	}
}
