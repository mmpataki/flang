package flang.impl.nodes;

import flang.api.AST;
import flang.api.EvalContext;
import flang.api.FunctionCall;
import flang.api.Variable;

public class FunctionCallImpl extends ASTImpl implements FunctionCall {
	String name;

	public FunctionCallImpl(String name) {
		this.name = name;
	}

	@Override
	public Variable eval(EvalContext ctxt) throws Exception {
		int i=0;
		Variable args[] = new Variable[getChildren().size()];
		for (AST chld : getChildren()) {
			args[i++] = chld.eval(ctxt);
		}
		return ctxt.f(getName()).call(ctxt, args);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("F[%s]", getName());
	}

}
