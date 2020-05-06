package flang.api;

import java.io.InputStream;

public interface Parser {

	AST parse(InputStream in) throws Exception;
	
}
