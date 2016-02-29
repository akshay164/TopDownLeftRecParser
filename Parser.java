/* 		OBJECT-ORIENTED RECOGNIZER FOR SIMPLE EXPRESSIONS
expr    -> term   (+ | -) expr | term
term    -> factor (* | /) term | factor
factor  -> int_lit | '(' expr ')'     
*/

public class Parser {
	public static char[] vars = new char[20];
	public static int i=1;
	public static void main(String[] args) {
		System.out.println("Enter an expression, end with semi-colon!\n");
		Lexer.lex();
		new Program();
		
		Code.output();
		//System.exit(0);
	}
}
class Program {
	public static int stop =0;
	Decls d;
	Stmts s;
	Program p;
	public Program(){
		
		if (Lexer.nextToken == Token.KEY_INT || Lexer.nextToken == Token.KEY_BOOL){
			Lexer.lex();
			d = new Decls();
		}
		if (Lexer.nextToken != Token.KEY_END){
			
			s = new Stmts();
		}
		
		if (Lexer.nextToken == Token.KEY_END){
			if (stop == 0){
				Code.gen(Code.terminate());
				stop = 1;
			}	
		}
	}
}

class Decls{
	
	Program p;
	Idlist il;
	
	public Decls(){
		if (Lexer.nextToken == Token.ID){
			il = new Idlist();
		}
		if(Lexer.nextToken == Token.SEMICOLON){
			Lexer.lex();
			
		}
	}
}


class Idlist{
	
	Idlist il;
	Program p;
	Decls de;
	
	public Idlist(){
		
			if(Lexer.nextToken == Token.ID){
				ids();
						
			}
			if(Lexer.nextToken == Token.COMMA){
			Lexer.lex();
			
			il = new Idlist();
			}
		}
		
	public void ids(){
		Parser.vars[Parser.i] = Lexer.ident;
		Parser.i++;
		Lexer.lex();
	}
}

class Stmts{
	Stmts ss;
	Stmt st;
	Program p;
	public Stmts() {
		
		if (Lexer.nextToken!=Token.KEY_ELSE){
			st = new Stmt();
			if (Lexer.nextToken!=Token.KEY_END)
				ss = new Stmts();
		}
	}
}
class Stmt{
	Assign as;
	Cmpd cm;
	Cond cd;
	Stmt s;
	Loop l;
	public Stmt(){
		switch (Lexer.nextToken){
		case Token.ID: {
			as = new Assign();
			Lexer.lex();
			break;
		}
		case Token.LEFT_BRACE: {
			cm = new Cmpd();
			break;
		}
		case Token.RIGHT_BRACE: {
			Lexer.lex();
			
			break;
		}
		case Token.KEY_IF: {
			Lexer.lex();
			cd = new Cond();
			break;
		}
		case Token.KEY_ELSE: {
			
			return;
		}
		
		case Token.KEY_FOR: {
			Lexer.lex();
			l = new Loop();
			break;
		}
		}
		
	}
}
class Assign {
	Expr e;
	public Assign() {
		for (int i=1;i<Parser.vars.length;i++) {
			if(Lexer.ident == Parser.vars[i]){
				Lexer.lex();
				Lexer.lex();
				e = new Expr();
				Code.gen(Code.varcode(i));
				break;
			}
		}
	}
}
class Cmpd {
	Stmts ss;
	public Cmpd() {
		if(Lexer.nextToken==Token.LEFT_BRACE){
			Lexer.lex();
			ss = new Stmts();
		}
		else if(Lexer.nextToken==Token.RIGHT_BRACE){
			Lexer.lex();
			
		}
	}
}
class Loop {
	Assign a1, a2;
	Rexp r;
	Stmt s;
	int ptr2;
	int j, l, g, i, k;
	
	public static int[] PTR = new int[10];
	public static int ptr = 0;
	String[] block; 
	public Loop()
	{
		block = new String[10];
		if(Lexer.nextToken==Token.LEFT_PAREN) {
			Lexer.lex();
			if(Lexer.nextToken==Token.ID) {
				a1 = new Assign();
			}
			Lexer.lex();
			g = Code.codeptr;
			if(Lexer.nextToken==Token.ID) {
			r = new Rexp();
			}
			l = Code.codeptr;
			Code.gen(Code.relation());
			Lexer.lex();
			PTR[ptr] = Code.codeptr;
			if(Lexer.nextToken==Token.ID){
				a2 = new Assign();		
			}
			
		}
		if(Lexer.nextToken==Token.RIGHT_PAREN) {
			Lexer.lex();
			ptr2 = Code.codeptr;
			j=0;
			for (i=PTR[ptr]; i<ptr2; i++){
				block[j] = Code.code[i];
				j++;
			}
			for (k=j; k<block.length;k++){
				block[k] = "ignore";
			}
			Code.codeptr = PTR[ptr];
			s = new Stmt();
			
			for (k = 0; k<this.block.length; k++){
				if (block[k]!="ignore"){
					Code.code[Code.codeptr] = block[k];
					Code.codeptr++;
				}
				
			}
			Code.gen(Code.elsegoto(g));
			Code.code[l] = Code.code[l] +" "+ Code.codeptr;
			ptr++;
			
		}
	}
}
class Cond {
	Rexp rx;
	Stmt st1, st2;
	public static int rel = 0;
	public static String[] RELN = new String[5];
	public static int[] INSI = new int[4];
	public static int[] JMPI = new int[4];
	public static int[] INSE = new int[4];
	public static int[] JMPE = new int[4];
	public static int jmpi = 0, jmpe = 0;
	public static int insi = 0, inse = 0; // location to insert
	
	public Cond() {
		if (Lexer.nextToken==Token.LEFT_PAREN) {
			
			Lexer.lex();
			rx = new Rexp();
			RELN[rel] = Rexp.reln;
			rel++;
			
		}
		if (Lexer.nextToken==Token.RIGHT_PAREN) {
			
			Lexer.lex();
			INSI[insi] = Code.codeptr;
					
			Code.code[INSI[insi]] = Rexp.reln;//Code.jump(100);	//print if statement(can print anything to reserve location)
			Code.codeptr = Code.codeptr + 3;
			insi++;
			
			st1 = new Stmt();
			if (Lexer.nextToken == Token.KEY_ELSE){
				
				INSE[inse] = Code.codeptr;
				inse++;
				Code.gen(Code.elsegoto(Code.codeptr));
				reprintif();
				Lexer.lex();
				st2 = new Stmt();
				
				jmpe = Code.codeptr;
				inse--;
				Code.code[INSE[inse]] = Code.elsegoto(jmpe);		
			}
			else reprintif();
		}	if(Lexer.nextToken == Token.RIGHT_BRACE)
				Lexer.lex();	
	}
	public void reprintif() {
		
		
		jmpi = Code.codeptr; //location of cursor
			
		
		insi --;
		Code.code[INSI[insi]] = Code.code[INSI[insi]] + " "+ jmpi; //reprint if statement with cursor in reserved location
		
	}
}
class Rexp {
	Expr ex1, ex2;
	Cond c;
	public static String reln;
	public Rexp(){
		ex1 = new Expr();
		
		if (Lexer.nextToken==Token.GREATER_OP) {
			reln = "if_icmple";
			Lexer.lex();
		}
		else if (Lexer.nextToken==Token.LESSER_OP) {
			reln = "if_icmge";
			Lexer.lex();
		}
		else if (Lexer.nextToken==Token.EQ_OP) {
			reln = "if_icmpne";
			Lexer.lex();
		}
		else if (Lexer.nextToken==Token.NOT_EQ){
			reln = "if_icmpeq";
			Lexer.lex();
		}
		else if (Lexer.nextToken==Token.RIGHT_PAREN) {
			
		}
		ex2 = new Expr();
		
		
	}
}
class Expr   { 
	Term t;
	Expr e;
	char op;

	public Expr() {
		t = new Term();
		if (Lexer.nextToken == Token.ADD_OP || Lexer.nextToken == Token.SUB_OP) {
			op = Lexer.nextChar;
			Lexer.lex();
			e = new Expr();
			Code.gen(Code.opcode(op));	 
		}
	}
}
class Term    { // term -> factor (* | /) term | factor
	Factor f;
	Term t;
	char op;

	public Term() {
		f = new Factor();
		if (Lexer.nextToken == Token.MULT_OP || Lexer.nextToken == Token.DIV_OP) {
			op = Lexer.nextChar;
			Lexer.lex();
			t = new Term();
			Code.gen(Code.opcode(op));
			}
	}
}

class Factor { // factor -> number | '(' expr ')'
	Expr e;
	int i;

	public Factor() {
		switch (Lexer.nextToken) {
		case Token.INT_LIT: // number
			i = Lexer.intValue;
			Code.gen(Code.intcode(i));
			Lexer.lex();
			break;
		case Token.LEFT_PAREN: // '('
			Lexer.lex();
			e = new Expr();
			Lexer.lex(); // skip over ')'
			break;
		case Token.ID: {
			for (int i=1;i<Parser.vars.length;i++) {
				if(Lexer.ident == Parser.vars[i]){
					
					Code.gen(Code.loadcode(i));
					Lexer.lex();
					
					break;
				}
			}
			break;
		}
		case Token.BOOL_TRUE: {
			Code.gen(Code.intcode(1));
			Lexer.lex();
			break;
		}
		case Token.BOOL_FALSE:{
			Code.gen(Code.intcode(0));
			Lexer.lex();
			break;
		}
			
		default:
			break;
		}
	}
}


class Code {
	static String[] code = new String[200];
	static int codeptr = 0;
	static int increment = 1;
	
	public static void gen(String s) {
		code[codeptr] = s;
		codeptr = codeptr + increment;
	}
	
	public static String intcode(int i) {
		if (i > 127) {
			increment = 3;
			return "sipush " + i;
		}
		else if (i > 5) {
			increment = 2;
			return "bipush " + i;
		}
		else {
			increment = 1;
			return "iconst_" + i;
		}
	}
	public static String varcode(int i) {
		
		if (i > 3) {
			increment = 2;
			return "istore " + i;
		}
		else {
			increment = 1;
			return "istore_" + i;
		}
		
	}
	public static String loadcode(int i){
		increment = 1;
		if (i>3) {
			increment = 2;
			return "iload "+i;
		}
		else {
			increment = 1;
			return "iload_"+i;
		}
	}
	public static  String terminate () {
		
		return "return";
	}
	public static String jump(int i) {
		
		String s = Rexp.reln + " " + i;
		return s;
	}
	public static String elsegoto(int i) {
		increment = 3;
		String s = "goto " + i;
		return s;
	}
	public static String relation() {
		increment = 3;
		String s = Rexp.reln;
		return s;
	}
	
	
	public static String opcode(char op) {
		increment = 1;
		switch(op) {
		case '+' : return "iadd";
		case '-':  return "isub";
		case '*':  return "imul";
		case '/':  return "idiv";
		default: return "";
		}
	}
	
	public static void output() {
		
		for (int i=0; i<codeptr; i++)
			if(code[i]!=null){
				System.out.println(i+": "+code[i]);
			}
		
	}
}


