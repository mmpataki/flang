package flang.impl.exec;

import java.lang.reflect.Method;

import flang.api.EvalContext;
import flang.api.Variable;
import flang.impl.nodes.VariableImpl;

public class Function {

	String name;
	Method m;
	
	public Function(String name, Method m) {
		this.name = name;
		this.m = m;
	}
	
	public String getName() {
		return name;
	}
	
	public Variable call(EvalContext ctxt, Variable ...args) throws Exception {
		Object rval = m.invoke(null, ctxt, args);
		Variable ret = new VariableImpl("ret", rval);
		return ret;
	}
	
}
