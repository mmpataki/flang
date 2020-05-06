package flang.impl.exec;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Module {

	String name;
	Map<String, Function> methods = new HashMap<>();
	
	public Module(String name, Class<?> modClass) {
		this.name = name;
		for (Method m : modClass.getDeclaredMethods()) {
			if(!m.isAnnotationPresent(ExportFunc.class))
				continue;
			String shortName = m.getAnnotation(ExportFunc.class).name(); 
			methods.put(
					shortName,
					new Function(shortName, m)
				);
		}
	}
	
	Function getMethod(String shortName) {
		return methods.get(shortName);
	}

	String getName() {
		return name;
	}
	
}
