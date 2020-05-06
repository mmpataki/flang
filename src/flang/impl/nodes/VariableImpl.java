package flang.impl.nodes;

import flang.api.EvalContext;
import flang.api.Variable;

public class VariableImpl extends ASTImpl implements Variable {

	Object val;
	String name;

	@Override
	public void setValue(Object val) {
		this.val = val;
	}

	@Override
	public String getName() {
		return name;
	}

	public VariableImpl(String name) {
		this(name, null);
	}
	
	public VariableImpl(String name, Object val) {
		this.name = name;
		this.val = val;
	}
	
	@Override
	public Variable eval(EvalContext ctxt) throws Exception {
		if(ctxt.v(getName()) == null)
			ctxt.setVar(getName(), getValue());
		return ctxt.v(getName());
	}
	
	@Override
	public String toString() {
		try {
			return String.format("V[%s]", getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.format("V[%s]", getName());
	}

	@Override
	public Object getValue() {
		return val;
	}

}
