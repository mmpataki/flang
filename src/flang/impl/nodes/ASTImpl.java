package flang.impl.nodes;

import java.util.LinkedList;
import java.util.List;
import flang.api.AST;

public abstract class ASTImpl implements AST {

	List<AST> chldrn = new LinkedList<>();
	
	@Override
	public List<AST> getChildren() {
		return chldrn;
	}

	@Override
	public void addChild(AST chld) {
		chldrn.add(chld);
	}
	
	public void print() {
		_print(this, 0);
	}

	@Override
	public String toString() {
		return "/";
	}
	
	private void _print(AST node, int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("\t");
		}
		System.out.println(node);
		for (AST ast : node.getChildren()) {
			_print(ast, depth+1);
		}
	}
}
