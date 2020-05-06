package flang.impl.nodes;

import flang.api.AST;
import flang.api.EvalContext;
import flang.api.Variable;

public class RootNode extends ASTImpl {
	@Override
	public Variable eval(EvalContext ctxt) throws Exception {
		for (AST ast : getChildren()) {
			ast.eval(ctxt);
		}
		return new VariableImpl("/", 0);
	}
}
