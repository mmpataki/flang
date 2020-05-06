package flang.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import flang.api.AST;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public class SimpleParser {

	@Retention(RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ParserRule {
		public String value();
	}

	int INIT_STATE = 0;
	int TRAP_STATE = 1;
	HashMap<Integer, HashMap<String, Integer>> rules = new HashMap<>();
	
	public void run(String className) throws Exception {
		
		/* init/trap transition */
		rules.put(INIT_STATE, new HashMap<>());
		rules.put(TRAP_STATE, null);
		
		Class<?> clazz = Class.forName(className);
		for (Method m : clazz.getMethods()) {
			ParserRule pRule = m.getAnnotation(ParserRule.class);
			if(pRule == null)
				continue;
			addRule(pRule.value(), m);
		}
		
		for (Entry<Integer, HashMap<String, Integer>> v : rules.entrySet()) {
			System.out.println(v.getKey() + " => " + v.getValue());
		}
	}

	private void addRule(String rule, Method m) {
		int curState = INIT_STATE;
		for (String tok : rule.split("[ \t\n]+")) {
			HashMap<String, Integer> transition = rules.get(curState);
			Integer nextState = transition.get(tok);
			if(nextState == null) {
				nextState = rules.size();
				transition.put(tok, nextState);
				rules.put(nextState, new HashMap<>());
			}
			curState = nextState;
		}
	}



	public static void main(String args[]) throws Exception {
		new SimpleParser().run(SimpleParser.class.getCanonicalName());
	}
	
	@ParserRule("/ = IDENT OP ARGLIST CP")
	public AST functionDefinition() {
		return null;
	}
	
	@ParserRule("ARGLIST = IDENT NEXTIDENTS")
	public AST argList() {
		return null;
	}
	
	@ParserRule("NEXTIDENTS = COMMA IDENT NEXTIDENTS | ")
	public AST nextIdents() {
		return null;
	}
	
}
