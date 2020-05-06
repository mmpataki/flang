package flang.impl.exec.modules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import flang.api.EvalContext;
import flang.api.Variable;
import flang.impl.exec.ExportFunc;
import flang.impl.exec.ModuleProvider;
import flang.impl.nodes.VariableImpl;

@ModuleProvider(name = "")
public class BaseModule {

	static Logger LOG = Logger.getLogger("BaseModule");
	static Map<String, Class<?>> modClasses = new HashMap<>();

	static {
		String classes = System.getProperty("modClasses");
		if (classes == null) {
			LOG.warning("-DmodClasses is null. No modules to import");
		} else {
			String modClassNames[] = classes.split(",");
			for (String modClass : modClassNames) {
				Class<?> klass = null;
				try {
					klass = Class.forName(modClass);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if (!klass.isAnnotationPresent(ModuleProvider.class)) {
					System.err.println(klass + " is not a ModuleProvider");
					continue;
				}
				modClasses.put(klass.getAnnotation(ModuleProvider.class).name(), klass);
			}
		}
	}

	@ExportFunc(name = "FOREACH")
	public static Object forEach(EvalContext ctxt, Variable... args) throws Exception {
		String fToCall = args[1].getName();
		int i = 0;
		Object o = args[0].getValue();
		if (o instanceof Object[]) {
			for (Object v : (Object[]) o)
				ctxt.f(fToCall).call(ctxt, new VariableImpl("#" + i++, v));
		} else if (o instanceof Iterable<?>) {
			Iterable<?> it = (Iterable<?>) o;
			for (Object v : it)
				ctxt.f(fToCall).call(ctxt, new VariableImpl("#" + i++, v));
		} else if (o instanceof Iterator<?>) {
			Iterator<?> it = (Iterator<?>) o;
			while (it.hasNext())
				ctxt.f(fToCall).call(ctxt, new VariableImpl("#" + i++, it.next()));
		}
		return null;
	}

	@ExportFunc(name = "ARRAY")
	public static Object[] array(EvalContext ctxt, Variable... args) throws Exception {
		return Arrays.stream(args).map(v -> v.getValue()).toArray();
	}

	@ExportFunc(name = "LIST")
	public static List<?> list(EvalContext ctxt, Variable... args) {
		return Arrays.stream(args).map(v -> v.getName()).collect(Collectors.toList());
	}

	@ExportFunc(name = "INCLUDE")
	public static Object include(EvalContext ctxt, Variable... args) {
		Variable modName = args[0];
		if (!modClasses.containsKey(modName.getName()))
			throw new RuntimeException("No Module provider found for module: [" + modName + "]");
		ctxt.registerModule(modClasses.get(modName.getName()));
		return null;
	}

	@ExportFunc(name = "ASSIGN")
	public static Object assign(EvalContext ctxt, Variable... args) {
		args[0].setValue(args[1].getValue());
		return null;
	}

	@ExportFunc(name = "MAP")
	public static Map<Object, Object> map(EvalContext ctxt, Variable... args) {
		HashMap<Object, Object> m = new HashMap<>();
		try {
			for (int i = 0; i < args.length; i += 2) {
				m.put(args[i].getValue(), args[i + 1].getValue());
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Even num of arguments required in k1, v1, k2, v2 order");
		}
		return m;
	}

	@SuppressWarnings("unchecked")
	@ExportFunc(name = "PUT")
	public static Object put(EvalContext ctxt, Variable... args) {
		Map<Object, Object> m = (Map<Object, Object>) args[0].getValue();
		m.put(args[1].getValue(), args[2].getValue());
		return null;
	}

	@SuppressWarnings("unchecked")
	@ExportFunc(name = "GET")
	public static Object get(EvalContext ctxt, Variable... args) {
		Map<Object, Object> m = (Map<Object, Object>) args[0].getValue();
		return m.get(args[1].getValue());
	}

}
