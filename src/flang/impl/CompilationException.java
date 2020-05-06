package flang.impl;

import flang.api.Tokenizer;

public class CompilationException extends Exception {
	private static final long serialVersionUID = -8145333223013812886L;

	public CompilationException(String msg, String lastTok, Tokenizer<String> tknzr) throws Exception {
		super("compilation failed: [msg=" + msg + ", remaining stream=[" + getRemainingStream(tknzr) + "]");
	}

	private static String getRemainingStream(Tokenizer<String> tknzr) throws Exception {
		StringBuilder sb = new StringBuilder(tknzr.token());
		while (tknzr.hasNext()) {
			tknzr.next();
			sb.append(" ").append(tknzr.token());
		}
		return sb.toString();
	}
}