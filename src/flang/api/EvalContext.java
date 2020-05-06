package flang.api;

import flang.impl.exec.Function;

public interface EvalContext {

	/**
	 * Gets the value of the variable from context.
	 * @param name : name of the variable
	 * @return : value of the variable
	 */
	public Variable v(String name);
	
	/**
	 * Sets the value of a variable in context.
	 * @param name : name of the variable
	 * @param value : value of the variable
	 */
	public void setVar(String name, Object value);
	
	/**
	 * Register a module from a class. 
	 * - The klass should be annotated with ModuleProvider
	 * - The methods which should be exported should be marked with ExportFunc
	 * @param klass
	 */
	public void registerModule(Class<?> klass);

	/**
	 * Gets a function from the module
	 * @param fToCall : function name
	 * @return : Function
	 */
	public Function f(String fToCall);

}
