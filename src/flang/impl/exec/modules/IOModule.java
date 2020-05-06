package flang.impl.exec.modules;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

import flang.api.EvalContext;
import flang.api.Variable;
import flang.impl.exec.ExportFunc;
import flang.impl.exec.ModuleProvider;

@ModuleProvider(name = "IO")
public class IOModule {

	@ExportFunc(name = "PRINT")
	public static Object print(EvalContext ctxt, Variable ...args) {
		System.out.print(args[0].getValue());
		return null;
	}
	
	@ExportFunc(name = "PRINTF")
	public static Object printf(EvalContext ctxt, Variable ...args) {
		return System.out.printf(
				(String) args[0].getValue(), 
				(Object[])Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).map(v -> v.getValue()).toArray());
	}
	
	@ExportFunc(name = "FOPEN")
	public static Object fopen(EvalContext ctxt, Variable ...args) throws Exception {
		if(((String)args[1].getValue()).equals("w"))
			return new FileOutputStream((String) args[0].getValue());
		return new FileInputStream((String) args[0].getValue());
	}
	
	@ExportFunc(name = "FCLOSE")
	public static Object fclose(EvalContext ctxt, Variable ...args) throws Exception {
		((FileOutputStream)args[0].getValue()).close();
		return null;
	}
	
	@ExportFunc(name = "FWRITE")
	public static Object fwrite(EvalContext ctxt, Variable ...args) throws Exception {
		((FileOutputStream)args[0].getValue()).write((byte[])args[1].getValue());
		return null;
	}
	
	@ExportFunc(name = "FREAD")
	public static Object fread(EvalContext ctxt, Variable ...args) throws Exception {
		return ((FileInputStream)args[0].getValue()).read((byte[])args[1].getValue());
	}
	
	@ExportFunc(name = "FPRINTF")
	public static Object fprintf(EvalContext ctxt, Variable ...args) throws Exception {
		String fmt = (String) args[1].getValue();
		Object[] vargs = Arrays.copyOfRange(args, 2, args.length);
		FileOutputStream fos = (FileOutputStream) args[0].getValue();
		fos.write(String.format(fmt, vargs).getBytes());
		return null;
	}
}
