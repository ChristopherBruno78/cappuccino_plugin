package cappuccino.ide.intellij.plugin.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*;
import java.util.logging.Level;
import java.util.logging.Logger;

%%

%{
	private boolean canRegex;
	StringBuffer pragmaString;
	private static final Logger LOGGER = Logger.getLogger("_ObjJLexer.flex");
	private boolean inPreProc = false;
	private boolean canWhitespace = true;

  	public _ObjJLexer() {
    	this((java.io.Reader)null);
  	}

	protected void canRegex(final boolean canRegex) {
		this.canRegex = canRegex;
	}

	protected boolean canRegex() {
		return canRegex;
	}

	protected boolean canWhiteSpace() {
  	    return canWhitespace;
	}

	protected void canWhiteSpace(boolean canWhitespace) {
  	    this.canWhitespace = canWhitespace;
	}

	private static void log(String message) {
		LOGGER.log(Level.INFO, message);
	}

%}

%public
%class _ObjJLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

PREPROCESSOR_CONTINUE_ON_NEXT_LINE=\\\n
LINE_TERMINATOR=[\r\n\u2028\u2029]
VAR_TYPE_BYTE=((unsigned|signed)[ ]+)?byte
VAR_TYPE_CHAR=((unsigned|signed)[ ]+)?char
VAR_TYPE_SHORT=((unsigned|signed)[ ]+)?short
VAR_TYPE_INT=((unsigned|signed)[ ]+)?int
VAR_TYPE_LONG_LONG=((unsigned|signed)[ ]+)?long[ ]+long
VAR_TYPE_LONG=((unsigned|signed)[ ]+)?long
IMPORT_FRAMEWORK_LITERAL=<[a-zA-Z0-9\-_]+"/"[a-zA-Z0-9\-_]+(\.j)?>
//BAD_SINGLE_QUOTE_STRING_LITERAL=\' SINGLE_QUOTE_STRING
SINGLE_QUOTE_TEXT = ([^\\\'\r\n]|\\ [\\\'brfntvuU0])+
//SINGLE_QUOTE_STRING_LITERAL = {BAD_SINGLE_QUOTE_STRING_LITERAL}\'
//BAD_DOUBLE_QUOTE_STRING_LITERAL=(@\"|\")
DOUBLE_QUOTE_TEXT = ([^\\\"\r\n]|\\ [\\\"brfntvuU0])+
//DOUBLE_QUOTE_STRING_LITERAL = {BAD_DOUBLE_QUOTE_STRING_LITERAL}\"
BOOLEAN_LITERAL=(true|false|YES|NO)
HEX_INTEGER_LITERAL=0 [xX] [0-9a-fA-F]+
OCTAL_INTEGER_LITERAL=0 [0-7]+
OCTAL_INTEGER_LITERAL2=0 [oO] [0-7]+
BINARY_INTEGER_LITERAL=0 [bB] [01]+
DECIMAL_LITERAL1=[0-9]*\.([0-9]*([eE] [+-]? [0-9]+)?|[eE] [+-]? [0-9]+)
DECIMAL_LITERAL2=[0-9]+\.?([eE] [+-]? [0-9]+)
INTEGER_LITERAL=[0-9]+
BAD_BLOCK_COMMENT="/"\* {BLOCK_COMMENT_TEXT}
BLOCK_COMMENT_TEXT = ([^*]|\*[^/]|'\n'|'\r'|";"|\s)+
BLOCK_COMMENT = {BAD_BLOCK_COMMENT}\*"/"
SINGLE_LINE_COMMENT="//"[^\r\n\u2028\u2029]*
REGEXP_LITERAL_SET = \[ ([^\]]|\\[\[\]]|\/)* \]
REGEXP_ESCAPED_CHAR = \\ [^\r\n\u2028\u2029]
REGEXP_CHAR_SIMPLE = [^\r\n\u2028\u2029\\/\[\]]
REGEXP_VALID_CHAR = {REGEXP_LITERAL_SET} | {REGEXP_ESCAPED_CHAR} | {REGEXP_CHAR_SIMPLE}
REGEXP_END = \/ [a-zA-Z]*
REGEXP = \/ {REGEXP_VALID_CHAR}+ {REGEXP_END}
ID=[$_a-zA-Z][_a-zA-Z0-9]*
PP_UNDEF = #\s*undef
PP_IF_DEF = #\s*ifdef
PP_IFNDEF = #\s*ifndef
PP_IF = #\s*if
PP_ELSE = #\s*else
PP_ENDIF = #\s*endif
PP_ELIF = #\s*elif
PP_PRAGMA = #\s*pragma
PP_DEFINE = #\s*define
PP_DEFINED = #\s*defined
PP_ERROR = #\s*error
PP_WARNING = #\s*warning
PP_INCLUDE = #\s*include
PP_FRAGMENT = #\s+{ID}
WHITE_SPACE=\p{Blank}+
%state PREPROCESSOR PRAGMA DOUBLE_QUOTE_STRING SINGLE_QUOTE_STRING BLOCK_COMMENT IN_REGEXP
%%


<PRAGMA> {
	'mark'								{ if (pragmaString == null) pragmaString = new StringBuffer(); return ObjJ_MARK; }
	[^\r\n\u2028\u2029]+				{ if (pragmaString != null) pragmaString.append(yytext());}
	{LINE_TERMINATOR}					{ yybegin(YYINITIAL); return ObjJ_PRAGMA_MARKER; }
}

<PREPROCESSOR> {
	{LINE_TERMINATOR} 				   	 { yybegin(YYINITIAL); inPreProc = false; pragmaString = null; return ObjJ_LINE_TERMINATOR; }
	'#'									 { return WHITE_SPACE; }
 	{WHITE_SPACE}  						 { return WHITE_SPACE; }
	{PREPROCESSOR_CONTINUE_ON_NEXT_LINE} { return WHITE_SPACE; }
}

<SINGLE_QUOTE_STRING> {
	"'" 								 { yybegin(inPreProc ? PREPROCESSOR : pragmaString != null ? PRAGMA : YYINITIAL); return ObjJ_SINGLE_QUO; }
	{LINE_TERMINATOR}					 { return ObjJ_LINE_TERMINATOR; }
	{SINGLE_QUOTE_TEXT}				 	 { return ObjJ_QUO_TEXT; }
}

<DOUBLE_QUOTE_STRING> {
	"\"" 								 { yybegin(inPreProc ? PREPROCESSOR : pragmaString != null ? PRAGMA : YYINITIAL); return ObjJ_DOUBLE_QUO; }
	{LINE_TERMINATOR}					 { return ObjJ_LINE_TERMINATOR; }
	{DOUBLE_QUOTE_TEXT}				 	 { return ObjJ_QUO_TEXT; }
}

<BLOCK_COMMENT> {
	"*/"								 { yybegin(YYINITIAL); canRegex(true); /*log("Ending Comment");*/ return ObjJ_BLOCK_COMMENT_END; }
  	"*"									 { return ObjJ_BLOCK_COMMENT_LEADING_ASTERISK; }
 	'.*'/'\n'							 { return ObjJ_BLOCK_COMMENT_LINE; }
}

<IN_REGEXP> {
  	{REGEXP_VALID_CHAR}					 {}
  	{REGEXP_ESCAPED_CHAR}				 {}
  	{REGEXP_END}						 { yybegin(YYINITIAL); return ObjJ_REGULAR_EXPRESSION_LITERAL_TOKEN; }
	{LINE_TERMINATOR}					 { yybegin(YYINITIAL); return BAD_CHARACTER;}
	<<EOF>>								 { yybegin(YYINITIAL); return BAD_CHARACTER; }
}

<YYINITIAL,PREPROCESSOR> {

	"?*__ERR_SEMICOLON__*?"			 	 { return ObjJ_ERROR_SEQUENCE_TOKEN; }
	"'"									 { canRegex(false);  yybegin(SINGLE_QUOTE_STRING); return ObjJ_SINGLE_QUO; }
	("\""|"@\"")						 { canRegex(false);  yybegin(DOUBLE_QUOTE_STRING); return ObjJ_DOUBLE_QUO; }
	"/*"								 { canRegex(false);  /*log("Starting Comment");*/ yybegin(BLOCK_COMMENT); return ObjJ_BLOCK_COMMENT_START; }
	"@["                                 { canRegex(true); return ObjJ_AT_OPENBRACKET; }
	"["                                  { canRegex(true); return ObjJ_OPEN_BRACKET; }
	"]"                                  { canRegex(false); return ObjJ_CLOSE_BRACKET; }
	"("                                  { canRegex(true); return ObjJ_OPEN_PAREN; }
	")"                                  { canRegex(false); return ObjJ_CLOSE_PAREN; }
	"{"                                  { canRegex(true); return ObjJ_OPEN_BRACE; }
	"}"                                  { canRegex(false); return ObjJ_CLOSE_BRACE; }
	","                                  { canRegex(true); return ObjJ_COMMA; }
	"="                                  { canRegex(true); return ObjJ_ASSIGN; }
	"?"                                  { canRegex(true); return ObjJ_QUESTION_MARK; }
	":"                                  { canRegex(true); return ObjJ_COLON; }
	"..."                                { canRegex(false); return ObjJ_ELLIPSIS; }
	"."                                  { canRegex(false); return ObjJ_DOT; }
	"++"                                 { canRegex(false); return ObjJ_PLUS_PLUS; }
	"--"                                 { canRegex(false); return ObjJ_MINUS_MINUS; }
	"+"                                  { canRegex(true); return ObjJ_PLUS; }
	"-"                                  { canRegex(true); return ObjJ_MINUS; }
	"~"                                  { canRegex(true); return ObjJ_BIT_NOT; }
	"!"                                  { canRegex(true); 	return ObjJ_NOT; }
	"*"                                  { canRegex(true); return ObjJ_MULTIPLY; }
	"/"                                  { canRegex(true); return ObjJ_DIVIDE; }
	"%"                                  { canRegex(true); return ObjJ_MODULUS; }
	">>"                                 { canRegex(true); return ObjJ_RIGHT_SHIFT_ARITHMATIC; }
	"<<"                                 { canRegex(true); return ObjJ_LEFT_SHIFT_ARITHMATIC; }
	">>>"                                { canRegex(true); return ObjJ_RIGHT_SHIFT_LOGICAL; }
	"<<<"                                { canRegex(true); return ObjJ_LEFT_SHIFT_LOGICAL; }
	"<"                                  { canRegex(true); return ObjJ_LESS_THAN; }
	">"                                  { canRegex(true); return ObjJ_GREATER_THAN; }
	"<="                                 { canRegex(true); return ObjJ_LESS_THAN_EQUALS; }
	">="                                 { canRegex(true); return ObjJ_GREATER_THAN_EQUALS; }
	"=="                                 { canRegex(true); return ObjJ_EQUALS; }
	"!="                                 { canRegex(true); return ObjJ_NOT_EQUALS; }
	"==="                                { canRegex(true); return ObjJ_IDENTITY_EQUALS; }
	"!=="                                { canRegex(true); return ObjJ_IDENTITY_NOT_EQUALS; }
	"&"                                  { canRegex(true); return ObjJ_BIT_AND; }
	"^"                                  { canRegex(true); return ObjJ_BIT_XOR; }
	"|"                                  { canRegex(true); return ObjJ_BIT_OR; }
	"&&"                                 { canRegex(true); return ObjJ_AND; }
	"||"                                 { canRegex(true); return ObjJ_OR; }
	"*="                                 { canRegex(true); return ObjJ_MULTIPLY_ASSIGN; }
	"/="                                 { canRegex(true); return ObjJ_DIVIDE_ASSIGN; }
	"%="                                 { canRegex(true); return ObjJ_MODULUS_ASSIGN; }
	"+="                                 { canRegex(true); return ObjJ_PLUS_ASSIGN; }
	"-="                                 { canRegex(true); return ObjJ_MINUS_ASSIGN; }
	"<<="                                { canRegex(true); return ObjJ_LEFT_SHIFT_ARITHMATIC_ASSIGN; }
	">>="                                { canRegex(true); return ObjJ_RIGHT_SHIFT_ARITHMATIC_ASSIGN; }
	"<<<="                               { canRegex(true); return ObjJ_LEFT_SHIFT_LOGICAL_ASSIGN; }
	">>>="                               { canRegex(true); return ObjJ_RIGHT_SHIFT_LOGICAL_ASSIGN; }
	"&="                                 { canRegex(true); return ObjJ_BIT_AND_ASSIGN; }
	"^="                                 { canRegex(true); return ObjJ_BIT_XOR_ASSIGN; }
	"|="                                 { canRegex(true); return ObjJ_BIT_OR_ASSIGN; }
	"=>"                                 { canRegex(false); return ObjJ_ARROW; }
	"@import"                            { canRegex(false); return ObjJ_AT_IMPORT; }
	"@accessors"                         { canRegex(false); return ObjJ_AT_ACCESSORS; }
	"@end"                               { canRegex(false); return ObjJ_AT_END; }
	"@action"                            { canRegex(false); return ObjJ_AT_ACTION; }
	"@selector"                          { canRegex(false); return ObjJ_AT_SELECTOR; }
	"@class"                             { canRegex(false); return ObjJ_AT_CLASS; }
	"@global"                            { canRegex(false); return ObjJ_AT_GLOBAL; }
	"@ref"                               { canRegex(false); return ObjJ_AT_REF; }
	"@deref"                             { canRegex(false); return ObjJ_AT_DEREF; }
	"@protocol"                          { canRegex(false); return ObjJ_AT_PROTOCOL; }
	"@optional"                          { canRegex(false); return ObjJ_AT_OPTIONAL; }
	"@required"                          { canRegex(false); return ObjJ_AT_REQUIRED; }
	"@interface"                         { canRegex(false); return ObjJ_AT_INTERFACE; }
	"@typedef"                           { canRegex(false); return ObjJ_AT_TYPE_DEF; }
	"@implementation"                    { canRegex(false); return ObjJ_AT_IMPLEMENTATION; }
	"@outlet"                            { canRegex(false); return ObjJ_AT_OUTLET; }
	"@{"                                 { canRegex(false); return ObjJ_AT_OPEN_BRACE; }
	"null"|"NULL"                        { canRegex(false); return ObjJ_NULL_LITERAL; }
	"nil"|"Nil"                          { canRegex(false); return ObjJ_NIL; }
	"undefined"                          { canRegex(false); return ObjJ_UNDEFINED; }
	{PP_DEFINE}                          { canRegex(false); yybegin(PREPROCESSOR); inPreProc = true; return ObjJ_PP_DEFINE; }
	{PP_UNDEF}                           { canRegex(false); yybegin(PREPROCESSOR); inPreProc = true; return ObjJ_PP_UNDEF; }
	{PP_IF_DEF}                          { canRegex(false); yybegin(PREPROCESSOR); inPreProc = true;return ObjJ_PP_IF_DEF; }
	{PP_IFNDEF}                          { canRegex(false); yybegin(PREPROCESSOR); inPreProc = true;return ObjJ_PP_IF_NDEF; }
	{PP_IF}                              { canRegex(true); yybegin(PREPROCESSOR); inPreProc = true;return ObjJ_PP_IF; }
	{PP_ELSE}                            { canRegex(true); yybegin(PREPROCESSOR); inPreProc = true;return ObjJ_PP_ELSE; }
	{PP_ENDIF}                           { canRegex(true); yybegin(PREPROCESSOR); inPreProc = true;return ObjJ_PP_END_IF; }
	{PP_ELIF}                            { canRegex(false); yybegin(PREPROCESSOR); inPreProc = true; return ObjJ_PP_ELSE_IF; }
	{PP_PRAGMA}                          { canRegex(false); yybegin(PRAGMA); return ObjJ_PP_PRAGMA; }
	{PP_DEFINED}                         { canRegex(false); return ObjJ_PP_DEFINED; }
	{PP_ERROR}                           { canRegex(false); yybegin(PREPROCESSOR); inPreProc = true; return ObjJ_PP_ERROR; }
	{PP_WARNING}                         { canRegex(false); yybegin(PREPROCESSOR); inPreProc = true; return ObjJ_PP_WARNING; }
	{PP_INCLUDE}                         { canRegex(false);	yybegin(PREPROCESSOR); inPreProc = true;return ObjJ_PP_INCLUDE; }
	"signed"                             { canRegex(false);  return ObjJ_VAR_TYPE_SIGNED; }
	"unsigned"                           { canRegex(false);  return ObjJ_VAR_TYPE_UNSIGNED; }
	"IBAction"                           { canRegex(false); return ObjJ_VAR_TYPE_IBACTION; }
	"IBOutlet"                           { canRegex(false); return ObjJ_VAR_TYPE_IBOUTLET; }
	"SEL"                                { canRegex(false); return ObjJ_VAR_TYPE_SEL; }
	"float"                              { canRegex(false);  return ObjJ_VAR_TYPE_FLOAT; }
	"double"                             { canRegex(false);  return ObjJ_VAR_TYPE_DOUBLE; }
	"BOOL"                               { canRegex(false);  return ObjJ_VAR_TYPE_BOOL; }
	"break"                              { canRegex(false);  return ObjJ_BREAK; }
	"do"                                 { canRegex(false);  return ObjJ_DO; }
	"instanceof"                         { canRegex(true); return ObjJ_INSTANCE_OF; }
	"typeof"                             { canRegex(true); return ObjJ_TYPE_OF; }
	"case"                               { canRegex(false); return ObjJ_CASE; }
	"else"                               { canRegex(false); return ObjJ_ELSE; }
	"new"                                { canRegex(false); return ObjJ_NEW; }
	"var"                                { canRegex(false); return ObjJ_VAR; }
	"catch"                              { canRegex(false); return ObjJ_CATCH; }
	"finally"                            { canRegex(false); return ObjJ_FINALLY; }
	"return"                             { canRegex(true); return ObjJ_RETURN; }
	"void"                               { canRegex(false); return ObjJ_VOID; }
	"continue"                           { canRegex(true); return ObjJ_CONTINUE; }
	"for"                                { canRegex(false); return ObjJ_FOR; }
	"switch"                             { canRegex(false); return ObjJ_SWITCH; }
	"while"                              { canRegex(false); return ObjJ_WHILE; }
	"debugger"                           { canRegex(true); return ObjJ_DEBUGGER; }
	"function"                           { canRegex(false); return ObjJ_FUNCTION; }
	"this"                               { canRegex(false); return ObjJ_THIS; }
	"with"                               { canRegex(true); return ObjJ_WITH; }
	"default"                            { canRegex(true); return ObjJ_DEFAULT; }
	"if"                                 { canRegex(false); return ObjJ_IF; }
	"throw"                              { canRegex(false); return ObjJ_THROW; }
	"delete"                             { canRegex(false); return ObjJ_DELETE; }
	"in"                                 { canRegex(true); return ObjJ_IN; }
	"try"                                { canRegex(false); return ObjJ_TRY; }
	"let"                                { canRegex(false); return ObjJ_LET; }
	"const"                              { canRegex(false); return ObjJ_CONST; }
	";"                                  { canRegex(true); return ObjJ_SEMI_COLON; }
	{BLOCK_COMMENT}                      { canRegex(true); return ObjJ_BLOCK_COMMENT; }
	{PREPROCESSOR_CONTINUE_ON_NEXT_LINE} { canRegex(true); return ObjJ_PREPROCESSOR_CONTINUE_ON_NEXT_LINE; }
	{LINE_TERMINATOR}                    { canRegex(true); return WHITE_SPACE; }
	{VAR_TYPE_BYTE}                      { canRegex(false);  return ObjJ_VAR_TYPE_BYTE; }
	{VAR_TYPE_CHAR}                      { canRegex(false);  return ObjJ_VAR_TYPE_CHAR; }
	{VAR_TYPE_SHORT}                     { canRegex(false);  return ObjJ_VAR_TYPE_SHORT; }
	{VAR_TYPE_INT}                       { canRegex(false);  return ObjJ_VAR_TYPE_INT; }
	{VAR_TYPE_LONG_LONG}                 { canRegex(false);  return ObjJ_VAR_TYPE_LONG_LONG; }
	{VAR_TYPE_LONG}                      { canRegex(false);  return ObjJ_VAR_TYPE_LONG; }
	{IMPORT_FRAMEWORK_LITERAL}           { canRegex(false);  return ObjJ_IMPORT_FRAMEWORK_LITERAL; }
	//{SINGLE_QUOTE_STRING_LITERAL}        { canRegex(false);  return ObjJ_SINGLE_QUOTE_STRING_LITERAL; }
	//{DOUBLE_QUOTE_STRING_LITERAL}        { canRegex(false);  return ObjJ_DOUBLE_QUOTE_STRING_LITERAL; }
	{BOOLEAN_LITERAL}                    { canRegex(false);  return ObjJ_BOOLEAN_LITERAL; }
	{HEX_INTEGER_LITERAL}                { canRegex(false);  return ObjJ_HEX_INTEGER_LITERAL; }
	{OCTAL_INTEGER_LITERAL}              { canRegex(false);  return ObjJ_OCTAL_INTEGER_LITERAL; }
	{OCTAL_INTEGER_LITERAL2}             { canRegex(false);  return ObjJ_OCTAL_INTEGER_LITERAL2; }
	{BINARY_INTEGER_LITERAL}             { canRegex(false);  return ObjJ_BINARY_INTEGER_LITERAL; }
	{DECIMAL_LITERAL1} | {DECIMAL_LITERAL2} { canRegex(false);  return ObjJ_DECIMAL_LITERAL; }
	{INTEGER_LITERAL}                    { canRegex(false);  return ObjJ_INTEGER_LITERAL; }
	{ID}                                 { canRegex(false); return ObjJ_ID; }
	"/"{REGEXP_VALID_CHAR}
        								 {
											if (canRegex()) {
												canRegex(false);
												yybegin(IN_REGEXP);
										 	} else if (yytext().toString().substring(1,2).equals("=")){
										 		yypushback(yytext().length()-2);
										 		return ObjJ_DIVIDE_ASSIGN;
											} else {
												yypushback(yytext().length()-1);
										 		return ObjJ_DIVIDE;
										 	}
										 }
	{BAD_BLOCK_COMMENT}			 		 { canRegex(false); return ObjJ_BLOCK_COMMENT; }
	{SINGLE_LINE_COMMENT}                { canRegex(true); return ObjJ_SINGLE_LINE_COMMENT; }
	//{BAD_DOUBLE_QUOTE_STRING_LITERAL}	 { canRegex(false);	return ObjJ_QUO_TEXT; }
	//{BAD_SINGLE_QUOTE_STRING_LITERAL}	 { canRegex(false); return ObjJ_QUO_TEXT; }

}
{PP_FRAGMENT}				             { canRegex(false); return ObjJ_PP_FRAGMENT; }
"@"{ID}						 			 { canRegex(false); return ObjJ_AT_FRAGMENT; }
<YYINITIAL> {
	{WHITE_SPACE}+                       { return WHITE_SPACE; }
  	"\\"								 { return ObjJ_FORWARD_SLASH; }
}
[^] { return BAD_CHARACTER; }
