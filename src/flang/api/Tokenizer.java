package flang.api;

public interface Tokenizer<T> {

	/**
	 * @return: Is there any token left?
	 * @throws Exception 
	 */
	public boolean hasNext() throws Exception;
	
	/**
	 * @return: Next token in the stream
	 * @throws Exception 
	 */
	public T next() throws Exception;
	
	/**
	 * @return: Return the actual token
	 */
	public String token();

	/**
	 * @return: Peek the next token 
	 */
	public String peek() throws Exception;
	
}
