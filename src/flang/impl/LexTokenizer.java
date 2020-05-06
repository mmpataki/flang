package flang.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import flang.api.Tokenizer;

public class LexTokenizer implements Tokenizer<String> {

	private static final String UNKNOWN_TOK = "UNKNOWN_TOK";

	private static Logger LOG = Logger.getLogger("LexTokenizer");
	String nextTokType;
	StringBuffer nextTok = new StringBuffer();
	DataInputStream din;

	List<String> toks = new LinkedList<String>();
	List<String> vals = new LinkedList<String>();

	@Override
	public String token() {
		return nextTok.toString();
	}

	@Override
	public boolean hasNext() throws Exception {
		if (nextTokType == null) {
			if (!toks.isEmpty()) {
				nextTokType = toks.remove(0);
				nextTok = new StringBuffer(vals.remove(0));
			} else {
				nextTokType = getNext();
				if ("STRING".equals(nextTokType)) {
					nextTok.deleteCharAt(0);
					nextTok.deleteCharAt(nextTok.length() - 1);
				}
			}
		}
		return nextTokType != null;
	}

	public String peek() throws Exception {
		if (!hasNext()) {
			throw new Exception("No tokens left");
		}
		String tok = next();
		String val = token();
		toks.add(tok);
		vals.add(val);
		return tok;
	}

	/**
	 * Undoing the read is implemented using this list.
	 */
	ArrayList<Character> pushBackBuf = new ArrayList<Character>(2);

	@Override
	public String next() throws Exception {
		if (nextTokType == null) {
			throw new Exception("either you haven't called hasNext or there are no tokens");
		}
		String ret = nextTokType;
		nextTokType = null;
		return ret;
	}

	private final static Map<Character, Character> escapeMap = new HashMap<>();
	static {
		escapeMap.put('n', '\n');
		escapeMap.put('t', '\t');
		escapeMap.put('\\', '\\');
		escapeMap.put('b', '\b');
		escapeMap.put('\"', '\"');
		escapeMap.put('\'', '\'');
		escapeMap.put('f', '\f');
	}
	
	public String getNext() throws Exception {
		nextTok.setLength(0);
		FinalState lastState = null;
		try {
			int curState = INIT_STATE;
			Integer lastMatch = null;
			while (true) {
				char b = (char) (pushBackBuf.size() > 0 ? pushBackBuf.remove(0) : din.readByte());

				/* escape them early */
				if(b == '\n' || b == '\r')
					continue;
				
				if (b == '\\') {
					/* consume the next byte as well */
					b = (char) (pushBackBuf.size() > 0 ? pushBackBuf.remove(0) : din.readByte());
					if(escapeMap.containsKey(b))
						b = escapeMap.get(b);
					else
						LOG.warning("Invalid eacape sequence \\" + b);
				}
				
				int c = (int) ((byte) b);
				int nextState = T[curState][c];

				if (nextState == TRAP_STATE) {

					/* for a single character we don't need to pushback, we just reject it */
					if (nextTok.length() > 0)
						pushBackBuf.add(b);

					if (lastMatch != null) {
						String unmatched = nextTok.substring(nextTok.length() - lastMatch);
						for (int i = 0; i < unmatched.length(); i++) {
							pushBackBuf.add(unmatched.charAt(i));
						}
						lastMatch = null;
						if (lastState.tokType.equals("IGNORE"))
							return getNext();
						return lastState.tokType;
					}
					return UNKNOWN_TOK;
				} else {
					if (lastMatch != null)
						lastMatch++;
				}

				nextTok.append(b);

				/*
				 * we will try to match to the longer string, so just consume the string for
				 * now, we will see whether next character also belongs to the same token if not
				 * we will accept it.
				 */
				if (finalStates.containsKey(nextState)) {
					lastState = finalStates.get(nextState);
					lastMatch = 0;
				}

				curState = nextState;
			}
		} catch (EOFException eofe) {
			/* we want it to happen */
		}
		return lastState == null ? null : lastState.tokType;
	}

	/**
	 * Constructor with lex file name arg
	 * 
	 * @param fileName: The file containing token-i-zation rules. The rules must be
	 *                  written in the below format. Note that pattern is not a
	 *                  regular expression pattern ==> TOKEN_NAME
	 * @param in:       Input stream to parse.
	 * @throws Exception
	 */
	public LexTokenizer(String fileName, DataInputStream in) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;

		DFASTART();
		while ((line = br.readLine()) != null) {
			String rule[] = line.split("==>");
			try {
				String pattern = rule[0];
				String tokenName = rule[1];
				ingest(pattern, tokenName);
			} catch (ArrayIndexOutOfBoundsException ae) {
				throw new Exception("Rule must be defined in \"pattern==>TOKEN_NAME\" format");
			}
		}
		DFADONE();

		br.close();
		this.din = in;
	}

	private void DFADONE() {
		T = new int[tmpT.size()][MAX_CHARS];
		int i = 0;
		for (int[] txs : tmpT) {
			for (int j = 0; j < txs.length; j++) {
				T[i][j] = txs[j];
			}
			i++;
		}
		if(LOG.isLoggable(Level.FINE))
			printStateDiagram();
	}

	private void DFASTART() {
		/* no op */
	}

	private void ingest(String pattern, String tokenName) throws Exception {
		addPath(pattern, tokenName);
	}

	/********* Deterministic Finite Automata *********/
	final int MAX_CHARS = 128;
	final int INIT_STATE = 1;
	final int TRAP_STATE = 0;
	int T[][] = null;
	ArrayList<int[]> tmpT = new ArrayList<>();
	HashMap<Integer, FinalState> finalStates = new HashMap<>();
	boolean init = false;

	private void addPath(String s, String tokType) throws Exception {
		if (!init) {
			init = true;
			addTrapStateTransitions();
			addInitStateTransistions();
		}
		int curState = INIT_STATE;

		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < s.length(); i++) {
			int c = (byte) s.charAt(i);

			int[] curStateTransitions = tmpT.get(curState);
			assert curStateTransitions != null;

			/* regex stuff : see lang.txt/regex */
			char wildChar = 0;
			switch ((char) c) {
			case '[':
				/* a char group just started, read fully */
				boolean escape = false;
				for (++i; i < s.length(); i++) {
					c = s.charAt(i);
					if (!escape && c == ']')
						break;
					buf.append(((char) c));
					if (escape) {
						escape = false;
					} else if (c == '\\') {
						escape = true;
					}
				}
				if (i + 1 < s.length()) {
					wildChar = s.charAt(i + 1);
					if (wildChar == '*' || wildChar == '?' || wildChar == '+')
						i++;
				}
				LOG.fine("chargroup: " + buf.toString());
				String grp = buf.toString();
				int newState = addNewState(true, tokType);
				for (int j = 0; j < grp.length(); j++) {
					char x = grp.charAt(j);
					if (x == '-') {
						if (curStateTransitions[x] == TRAP_STATE) {
							curStateTransitions[x] = addNewState((i + 1 == s.length()), tokType);
						}
					} else {
						char from, to;
						if (j + 2 < grp.length() && grp.charAt(j + 1) == '-') {
							from = x;
							to = grp.charAt(j + 2);
							for (int k = from; k <= to; k++) {
								if (curStateTransitions[k] == TRAP_STATE) {
									curStateTransitions[k] = newState;
								}
							}
						} else {
							from = x;
							to = x;
						}

						/* new state must consume all chars if '* OR +' are given */
						if (wildChar == '*' || wildChar == '*') {
							int newStateTransitions[] = tmpT.get(newState);
							for (int k = from; k <= to; k++) {
								if (newStateTransitions[k] == TRAP_STATE) {
									newStateTransitions[k] = newState;
								}
							}
						}

						/* we have to skip a '-' and a character */
						j += 2;
					}
				}
				continue;
			case ']':
				throw new Exception("Unmatched ]");
			case '*':
			case '.':
			case '?':
				for (int j = 0; j < curStateTransitions.length; j++) {
					if (curStateTransitions[j] == TRAP_STATE)
						curStateTransitions[j] = curState;
				}
				continue;
			case '+':
				throw new Exception("+ is currently not supported");
			case '\\':
				if (++i < s.length())
					c = s.charAt(i);
				else
					throw new Exception("Invalid escape");
			default:
			}
			if (curStateTransitions[c] == TRAP_STATE) {
				curStateTransitions[c] = addNewState((i + 1 == s.length()), tokType);
			} else if (curStateTransitions[c] == curState) {
				LOG.fine("WARN: Overriding a transistion");
				curStateTransitions[c] = addNewState((i + 1 == s.length()), tokType);
			}
			curState = curStateTransitions[c];
		}
	}

	class FinalState {
		String tokType;

		public FinalState(String tokType) {
			this.tokType = tokType;
		}

		public String getTokType() {
			return tokType;
		}
	}

	private void printStateDiagram() {

		/* a-z A-Z 0-9 !@#$%^&*()_-+={[]}:;"'<,>.?/|\~` */
		/* 26 26 10 01234567890123456789012345678901 */
		/* 26 + 26 + 10 + 32 = 94 */
		char chs[] = new char[94];
		String s = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()_-+={[]}:;\"'<,>.?/|\\~`";

		for (int i = 0; i < s.length(); i++) {
			chs[i] = s.charAt(i);
		}

		System.out.print("   ");
		for (int i = 0; i < chs.length; i++) {
			System.out.printf(" %2c", ((char) chs[i]));
		}
		System.out.println();
		for (int i = 0; i < T.length; i++) {
			System.out.printf("%-3d", i);

			for (int j = 0; j < chs.length; j++) {
				System.out.printf(" %2d", T[i][chs[j]]);
			}
			if (finalStates.containsKey(i))
				System.out.printf("   [%s]", finalStates.get(i).tokType);
			System.out.println();
		}
	}

	private int addInitStateTransistions() {
		tmpT.add(INIT_STATE, getNewStateTransitions());
		return INIT_STATE;
	}

	/* no need to implement again */
	private int addTrapStateTransitions() {
		tmpT.add(TRAP_STATE, getNewStateTransitions());
		return TRAP_STATE;
	}

	private int addNewState(boolean finalState, String tokType) {
		tmpT.add(getNewStateTransitions());
		int sNo = tmpT.size() - 1;
		if (finalState) {
			finalStates.put(sNo, new FinalState(tokType));
		}
		return sNo;
	}

	private int[] getNewStateTransitions() {
		int[] ret = new int[MAX_CHARS];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = TRAP_STATE;
		}
		return ret;
	}

	/**************************************************/

	public static void main(String arg[]) throws Exception {

		/* test the tokenizer */
		String tokenDefnFile = "tokendefns.txt";
		String inputFile = "inputFile.txt";
		// createData(tokenDefnFile, inputFile);
		Tokenizer<String> toknzr = new LexTokenizer(tokenDefnFile, new DataInputStream(new FileInputStream(inputFile)));

		while (toknzr.hasNext()) {
			String type = toknzr.next();
			LOG.fine(toknzr.token());
		}
	}

	private static void createData(String tokenDefnFile, String inputFile) throws Exception {
		FileWriter fw = new FileWriter(tokenDefnFile);
		fw.write("[A-Za-z]*==>IDENT\n");
		fw.write("(==>OP\n");
		fw.write(")==>CP\n");
		fw.write(",==>COMMA\n");
		fw.write("[0-9]*==>NUM");
		fw.close();

		/**
		 * fw = new FileWriter(inputFile); fw.write("FOREEACH(MRSGET(x), X, (ASSGN(X,
		 * SUM(X, 1))))"); fw.close(); /
		 **/
	}

}
