package flang.api;

import java.util.List;

public interface AST {

	public List<AST> getChildren();
	
	public void addChild(AST chld);
	
	public Variable eval(EvalContext ctxt) throws Exception;
	
	public void print();
}
