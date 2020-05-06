package flang.impl.exec;

import java.util.HashMap;
import java.util.Map;

import flang.api.EvalContext;
import flang.api.Variable;
import flang.impl.nodes.VariableImpl;

public class BaseEvalContext implements EvalContext {

	Map<String, Variable> variables;
	Map<String, Module> modules;
	Map<String, Class<?>> modClasses;

	BaseEvalContext() {
		variables = new HashMap<>();
		modules = new HashMap<>();
	}

	@Override
	public Variable v(String vName) {
		return variables.get(vName);
	}

	@Override
	public void registerModule(Class<?> modClass) {
		if (!modClass.isAnnotationPresent(ModuleProvider.class))
			throw new RuntimeException(modClass.getCanonicalName() + " is not a module provider");
		Module mod = new Module(modClass.getAnnotation(ModuleProvider.class).name(), modClass);
		modules.put(mod.getName(), mod);
	}

	@Override
	public Function f(String name) {
		String chunks[] = name.split("\\.");
		Module mod;
		String modname = "", funcname = name;
		if (chunks.length == 1) {
			mod = modules.get(modname = "");
		} else {
			mod = modules.get(modname = chunks[0]);
			funcname = chunks[1];
		}
		if (mod == null)
			throw new RuntimeException("Mmodule: [" + modname + "] is not included. Use INCLUDE(" + modname + ") to include it");
		Function m = mod.getMethod(funcname);
		if (m == null)
			throw new RuntimeException("No such method: [" + funcname + "]");
		return m;
	}

	@Override
	public void setVar(String name, Object value) {
		Variable v = variables.get(name);
		if(v == null)
			variables.put(name, new VariableImpl(name));
		variables.get(name).setValue(value);
	}

}
