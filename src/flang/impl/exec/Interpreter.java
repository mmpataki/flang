package flang.impl.exec;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import flang.api.AST;
import flang.api.EvalContext;
import flang.api.Parser;
import flang.impl.FlangParserImpl;
import flang.impl.exec.modules.BaseModule;

public class Interpreter {

	List<Class<?>> baseModules = Arrays.asList(BaseModule.class);
	EvalContext ctxt;

	public Interpreter() {
		ctxt = new BaseEvalContext();
	}

	void eval(AST ast) throws Exception {

		/* step 1 :: load the provider classes */
		loadModules();

		/* start executing */
		ast.eval(ctxt);

	}

	private void loadModules() {
		for (Class<?> klass : baseModules) {
			ctxt.registerModule(klass);
		}
	}

	public static void main(String[] args) throws Exception {
		boolean debug = true;
		Interpreter machine = new Interpreter();
		Parser P = new FlangParserImpl();
		if (debug) {
			machine.eval(P.parse(new FileInputStream("inputFile.txt")));
		} else {
			Scanner sc = new Scanner(System.in);
			while(true) {
				System.out.print("> ");
				System.out.flush();
				String line = sc.nextLine();
				ByteArrayInputStream bis = new ByteArrayInputStream(line.getBytes());
				try {
					AST a = P.parse(bis);
					a.print();
					machine.eval(a);
				} catch(Throwable e) {
					e.printStackTrace();
				}
				System.err.flush();
			}
		}
	}
}
