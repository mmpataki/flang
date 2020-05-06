package flang.impl;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import flang.api.AST;
import flang.api.Parser;
import flang.api.Tokenizer;
import flang.impl.nodes.FunctionCallImpl;
import flang.impl.nodes.RootNode;
import flang.impl.nodes.VariableImpl;

public class FlangParserImpl implements Parser {

	@Override
	public AST parse(InputStream in) throws Exception {

		Tokenizer<String> tknzr = new LexTokenizer("tokendefns.txt", new DataInputStream(in));

		AST root = new RootNode();
		_parse(root, tknzr);

		return root;
	}

	private void _parse(AST root, Tokenizer<String> tknzr) throws Exception {
		while (tknzr.hasNext())
			parse(root, tknzr);
	}

	/* differentiator between function and variable */
	private void parse(AST root, Tokenizer<String> tknzr) throws Exception {
		if (!tknzr.hasNext())
			return;

		TokPair tp = expect(tknzr, "IDENT", "NUM", "STRING");

		if (!tknzr.hasNext()) {
			root.addChild(getVariable(tp));
			return;
		}

		if (tknzr.peek().equals("OP")) {
			/* function call a new stack-frame */
			FunctionCallImpl f = new FunctionCallImpl(tp.tok());
			root.addChild(f);
			parseFuncArgs(f, tknzr);
		} else {
			root.addChild(getVariable(tp));
		}
	}

	private AST getVariable(TokPair tp) {
		VariableImpl v = new VariableImpl(tp.tok());
		if(!tp.toktyp().equals("IDENT")) {
			v.setValue(tp.tok());
		}
		return v;
	}

	private void parseFuncArgs(FunctionCallImpl f, Tokenizer<String> tknzr) throws Exception {
		expect(tknzr, "OP");
		while (!tknzr.peek().equals("CP")) {
			parse(f, tknzr);
			if(tknzr.hasNext()) {
				String typ = tknzr.next();
				if(typ.equals("CP"))
					return;
			}
		}
	}

	TokPair expect(Tokenizer<String> tknzr, String... typs) throws Exception {
		if (!tknzr.hasNext())
			throw new CompilationException("Expecting " + Arrays.toString(typs) + ". found EOF", null, tknzr);
		String ttyp = tknzr.next();
		for (String typ : typs) {
			if (ttyp.equals(typ))
				return new TokPair(ttyp, tknzr.token());
		}
		throw new CompilationException("Expecting " + Arrays.toString(typs) + " found " + ttyp, ttyp, tknzr);
	}

	public static void main(String[] args) throws Exception {
		new FlangParserImpl().parse(new FileInputStream("inputFile.txt")).print();
	}

}
