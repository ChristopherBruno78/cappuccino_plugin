/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 by Bart Kiers (original author) and Alexandre Vitorelli (contributor -> ported to CSharp)
 * Copyright (c) 2017 by Ivan Kochurkin (Positive Technologies):
    added ECMAScript 6 support, cleared and transformed to the universal grammar.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
parser grammar ObjJSjParser;

options {
    tokenVocab=ObjJSjLexer;
    superClass=ObjJSjBaseParser;
}

script
    : scriptHeader file+ EEnd EOF
    ;

scriptHeader
	: AtStatic
	;

file
	: fileHeader fileElements*
	;

fileHeader
	: fileNameProp TProp AtStatic imports? TProp
	;

fileNameProp
	: FileNameProp
	;

imports
	:	importStatement+
	;

importStatement
	: fileImport=FileImport
	| frameworkImport=FrameworkImport
	;

importExecuteFile
	: 'objj_executeFile' '('StringLiteral',' BooleanLiteral ')' ';'
	;

fileElements
	: importExecuteFile
	| classDefinition
	| categoryDefinition
	| protocolDefinition
	| typedefDefinition
	| statement
	;

classDefinition
	: '{'
			'var' Identifier '=' 'objj_allocateClassPair' '(' superClass=(Identifier|NilLiteral) ',' className=StringLiteral')' ','
			Identifier '=' Identifier'.'Identifier eos
			classStatements*
	  '}'
	;

classStatements
	: inheritsProtoStatement
	| iVars
    | 'objj_registerClassPair' '(' Identifier ')' eos
    | classMethodDecs
    | statement
	;

categoryDefinition
	: '{'
			'var' Identifier '=' 'objj_getClass' '(' baseClass=(StringLiteral|NilLiteral)')' eos
			ifStatement
			'var' Identifier '=' Identifier'.'Identifier eos
			inheritsProtoStatement*
			iVars?
			classMethodDecs*
	  '}'
	;

classMethodDecs
	:
		'class_addMethods' '(' target=Identifier ',' '[' (classMethodDefinition (',' classMethodDefinition)*)?  ']' ')' eos
	;

classMethodDefinition
	: 'new' 'objj_method''(''sel_getUid''('selector=StringLiteral')' ',' 'function' Identifier'('self=Identifier',' cmd=Identifier (',' varName)*')'
      '{'
       	functionBody
      '}'

      ',' '[' returnType=StringLiteral (',' paramType)* ']' ')'
	;
varName
	: Identifier
	;
iVars
	: 'class_addIvars' '(' Identifier ',' '[' (iVar (',' iVar)* )? ']' ')' eos
	;

iVar
	: 'new' 'objj_ivar' '(' name=StringLiteral ',' type=StringLiteral ')'
	;


protocolDefinition
	: '{'
		'var' Identifier '=' 'objj_allocateProtocol' '(' protocolName=StringLiteral ')' eos
		inheritsProtoStatement*
		'objj_registerProtocol' '(' Identifier ')' eos
		(methodDecs=protoMethodsDec eos)*
	  '}'
	;

inheritsProtoStatement
	: 'var' Identifier '=' 'objj_getProtocol' '(' protocolName=StringLiteral ')' eos
	  ifStatement
     ('class_addProtocol'|'protocol_addProtocol') '('Identifier ',' Identifier ')' eos
	;
protoMethodsDec
	: 'protocol_addMethodDescriptions' '(' Identifier ',' '[' protoMethodsList? ']' ',' BooleanLiteral ',' isInstanceMethods=BooleanLiteral ')' eos
	;

protoMethodsList
	: protocolMethodDefinition (',' protocolMethodDefinition)*
	;

protocolMethodDefinition
	: 'new' 'objj_method' '(' 'sel_getUid' '(' selector=StringLiteral ')' ',' NilLiteral
             ',' '['returnType=StringLiteral (',' paramTypes=paramType)* ']'  ')'
	;

paramType
	: StringLiteral
	;

typedefDefinition
	: '{'
		'var' Identifier '=' 'objj_allocateTypeDef''('type=StringLiteral ')' eos
		'objj_registerTypeDef''('Identifier')'eos
	  '}'
	;


statement
    : block
    | methodCallTempVarList
    | variableStatement
    | emptyStatement
    | expressionStatement
    | ifStatement
    | iterationStatement
    | continueStatement
    | breakStatement
    | returnStatement
    | withStatement
    | labelledStatement
    | switchStatement
    | throwStatement
    | tryStatement
    | debuggerStatement
    | functionDeclaration
    | jsClassDeclaration
    ;

block
    : '{' statementList? '}'
    ;

statementList
    : statement+
    ;
methodCallTempVarList
    : varModifier MethodCallTempVar (',' MethodCallTempVar)* eos
    ;

variableStatement
    : varModifier variableDeclarationList eos
    ;

variableDeclarationList
    : variableDeclaration (',' variableDeclaration)*
    ;

variableDeclaration
    : (Identifier | arrayLiteral | objectLiteral) ('=' singleExpression)? // ECMAScript 6: Array & Object Matching
    ;

emptyStatement
    : SemiColon
    ;

expressionStatement
    : {notOpenBraceAndNotFunction()}? expressionSequence eos
    ;

ifStatement
    : If '(' expressionSequence ')' statement (Else statement)?
    ;


iterationStatement
    : Do statement While '(' expressionSequence ')' eos                                                 # DoStatement
    | While '(' expressionSequence ')' statement                                                        # WhileStatement
    | For '(' expressionSequence? ';' expressionSequence? ';' expressionSequence? ')' statement         # ForStatement
    | For '(' varModifier variableDeclarationList ';' expressionSequence? ';' expressionSequence? ')'
          statement                                                                                     # ForVarStatement
    | For '(' singleExpression (In | Identifier{p("of")}?) expressionSequence ')' statement             # ForInStatement
    | For '(' varModifier variableDeclaration (In | Identifier{p("of")}?) expressionSequence ')' statement      # ForVarInStatement
    ;

varModifier  // let, const - ECMAScript 6
    : Var
    | Let
    | Const
    ;

continueStatement
    : Continue ({notLineTerminator()}? Identifier)? eos
    ;

breakStatement
    : Break ({notLineTerminator()}? Identifier)? eos
    ;

returnStatement
    : Return ({notLineTerminator()}? expressionSequence)? eos
    ;

withStatement
    : With '(' expressionSequence ')' statement
    ;

switchStatement
    : Switch '(' expressionSequence ')' caseBlock
    ;

caseBlock
    : '{' caseClauses? (defaultClause caseClauses?)? '}'
    ;

caseClauses
    : caseClause+
    ;

caseClause
    : Case expressionSequence ':' statementList?
    ;

defaultClause
    : Default ':' statementList?
    ;

labelledStatement
    : Identifier ':' statement
    ;

throwStatement
    : Throw {notLineTerminator()}? expressionSequence eos
    ;

tryStatement
    : Try block (catchProduction finallyProduction? | finallyProduction)
    ;

catchProduction
    : Catch '(' Identifier ')' block
    ;

finallyProduction
    : Finally block
    ;

debuggerStatement
    : Debugger eos
    ;

functionDeclaration
    : Function functionName=Identifier '(' formalParameterList? ')' '{' functionBody '}'
    ;

jsClassDeclaration
    : Class Identifier jsClassTail
    ;

jsClassTail
    : (Extends singleExpression)? '{' jsClassElement* '}'
    ;

jsClassElement
    : Static? jsMethodDefinition
    ;

jsMethodDefinition
    : propertyName '(' formalParameterList? ')' '{' functionBody '}'
    | getter '(' ')' '{' functionBody '}'
    | setter '(' formalParameterList? ')' '{' functionBody '}'
    | generatorMethod
    ;

generatorMethod
    : '*'? Identifier '(' formalParameterList? ')' '{' functionBody '}'
    ;

formalParameterList
    : formalParameterArg (',' formalParameterArg)* (',' lastFormalParameterArg)?
    | lastFormalParameterArg
    | arrayLiteral                            // ECMAScript 6: Parameter Context Matching
    | objectLiteral                           // ECMAScript 6: Parameter Context Matching
    ;

formalParameterArg
    : paramName=Identifier ('=' singleExpression)?      // ECMAScript 6: Initialization
    ;

lastFormalParameterArg                        // ECMAScript 6: Rest Parameter
    : Ellipsis paramName=Identifier
    ;

functionBody
    : statement*
    ;

files
    : file+
    ;

arrayLiteral
    : '[' ','* elementList? ','* ']'
    ;

elementList
    : singleExpression (','+ singleExpression)* (','+ lastElement)?
    | lastElement
    ;

lastElement                      // ECMAScript 6: Spread Operator
    : Ellipsis Identifier
    ;

objectLiteral
    : '{' (propertyAssignment (',' propertyAssignment)*)? ','? '}'
    ;

propertyAssignment
    : propertyName (':' |'=') singleExpression       # PropertyExpressionAssignment
    | '[' singleExpression ']' ':' singleExpression  # ComputedPropertyExpressionAssignment
    | getter '(' ')' '{' functionBody '}'            # PropertyGetter
    | setter '(' Identifier ')' '{' functionBody '}' # PropertySetter
    | generatorMethod                                # MethodProperty
    | Identifier                                     # PropertyShorthand
    ;

propertyName
    : identifierName
    | StringLiteral
    | numericLiteral
    ;

arguments
    : '('(
          singleExpression (',' singleExpression)* (',' lastArgument)? |
          lastArgument
       )?')'
    ;

lastArgument                                  // ECMAScript 6: Spread Operator
    : Ellipsis Identifier
    ;

expressionSequence
    : singleExpression (',' singleExpression)*
    ;

singleExpression
    : compoundMethodCall													 # CompoundMethodCallExpression
    | methodCall			   												 # MethodCallExpression
    | Function functionName=Identifier? '(' formalParameterList? ')' '{' functionBody '}' # FunctionExpression
    | Class Identifier? jsClassTail                                            # ClassExpression
    | singleExpression '[' expressionSequence ']'                            # MemberIndexExpression
    | singleExpression '.' identifierName                                    # MemberDotExpression
    | singleExpression arguments                                             # ArgumentsExpression
    | New singleExpression arguments?                                        # NewExpression
    | singleExpression {notLineTerminator()}? '++'                           # PostIncrementExpression
    | singleExpression {notLineTerminator()}? '--'                           # PostDecreaseExpression
    | Delete singleExpression                                                # DeleteExpression
    | Void singleExpression                                                  # VoidExpression
    | Typeof singleExpression                                                # TypeofExpression
    | '++' singleExpression                                                  # PreIncrementExpression
    | '--' singleExpression                                                  # PreDecreaseExpression
    | '+' singleExpression                                                   # UnaryPlusExpression
    | '-' singleExpression                                                   # UnaryMinusExpression
    | '~' singleExpression                                                   # BitNotExpression
    | '!' singleExpression                                                   # NotExpression
    | singleExpression ('*' | '/' | '%') singleExpression                    # MultiplicativeExpression
    | singleExpression ('+' | '-') singleExpression                          # AdditiveExpression
    | singleExpression ('<<' | '>>' | '>>>') singleExpression                # BitShiftExpression
    | singleExpression ('<' | '>' | '<=' | '>=') singleExpression            # RelationalExpression
    | singleExpression Instanceof singleExpression                           # InstanceofExpression
    | singleExpression In singleExpression                                   # InExpression
    | singleExpression ('==' | '!=' | '===' | '!==') singleExpression        # EqualityExpression
    | singleExpression '&' singleExpression                                  # BitAndExpression
    | singleExpression '^' singleExpression                                  # BitXOrExpression
    | singleExpression '|' singleExpression                                  # BitOrExpression
    | singleExpression '&&' singleExpression                                 # LogicalAndExpression
    | singleExpression '||' singleExpression                                 # LogicalOrExpression
    | singleExpression '?' singleExpression ':' singleExpression             # TernaryExpression
    | singleExpression '=' singleExpression                                  # AssignmentExpression
    | singleExpression assignmentOperator singleExpression                   # AssignmentOperatorExpression
    | singleExpression TemplateStringLiteral                                 # TemplateStringExpression  // ECMAScript 6
    | This                                                                   # ThisExpression
    | MethodCallTempVar														 # TempVarExpression
    | Identifier                                                             # IdentifierExpression
    | objjFunctionName														 # ObjJFunctionNameExpression
    | Super                                                                  # SuperExpression
    | literal                                                                # LiteralExpression
    | arrayLiteral                                                           # ArrayLiteralExpression
    | objectLiteral                                                          # ObjectLiteralExpression
    | '(' expressionSequence ')'                                             # ParenthesizedExpression
    | arrowFunctionParameters '=>' arrowFunctionBody                         # ArrowFunctionExpression   // ECMAScript 6
    | EEnd																	 # EEndExpression
    ;

compoundMethodCall
	: '(' '(' MethodCallTempVar '=' (prelimMethodCall=methodCall|prelimCompoundMethodCall=compoundMethodCall) ')'',' MethodCallTempVar '==' 'null' '?' 'null' ':' actualCall=methodCall ')'
	;

methodCall
	: '('  (methodCallTarget | staticMethodCall)'[' selector=StringLiteral ']'  '||' '_objj_forward' ')' '(' target=(Identifier|MethodCallTempVar) ',' StringLiteral (',' parameters=singleExpression)*')'
	;

methodCallTarget
	: (Identifier|MethodCallTempVar) '.' Identifier '.' 'method_msgSend'
	;

staticMethodCall
	: ('objj_getClass'|'objj_getMetaClass') '('StringLiteral')' '.' Identifier '.' 'method_dtable'
	;


arrowFunctionParameters
    : Identifier
    | '(' formalParameterList? ')'
    ;

arrowFunctionBody
    : singleExpression
    | '{' functionBody '}'
    ;
objjFunctionName
	: FuncAllocProto
    | FuncRegProto
    | FuncGetProtocol
    | FuncGetClass
    | FuncAllocClassPair
    | FuncRegClassPair
    | FuncIVar
    | FuncMethod
    | FunAllocTypeDef
    | FuncRegTypeDef
    | ObjJForward
    | FunMethodSend
    | FuncSelUid
    | MethodCallTempVar
    | FunMethodDTable
    | FuncAddProto
    | FuncClassAddMethod
    | FuncClassAddIvars
    | FuncProtoAddMethods
    | FuncProtoAddProto
	;
assignmentOperator
    : '*='
    | '/='
    | '%='
    | '+='
    | '-='
    | '<<='
    | '>>='
    | '>>>='
    | '&='
    | '^='
    | '|='
    ;

literal
    : NullLiteral
    | NilLiteral
    | BooleanLiteral
    | StringLiteral
    | TemplateStringLiteral
    | RegularExpressionLiteral
    | numericLiteral
    | selectorLiteral
    ;

selectorLiteral
	: 'sel_getUid''('selector=StringLiteral')'
	;

numericLiteral
    : DecimalLiteral
    | HexIntegerLiteral
    | OctalIntegerLiteral
    | OctalIntegerLiteral2
    | BinaryIntegerLiteral
    ;

identifierName
    : Identifier
    | reservedWord
    ;

reservedWord
    : keyword
    | NullLiteral
    | BooleanLiteral
    ;

keyword
    : Break
    | Do
    | Instanceof
    | Typeof
    | Case
    | Else
    | New
    | Var
    | Catch
    | Finally
    | Return
    | Void
    | Continue
    | For
    | Switch
    | While
    | Debugger
    | Function
    | This
    | With
    | Default
    | If
    | Throw
    | Delete
    | In
    | Try

    | Class
    | Enum
    | Extends
    | Super
    | Const
    | Export
    | Import
    | Implements
    | Let
    | Private
    | Public
    | Interface
    | Package
    | Protected
    | Static
    | Yield
    ;

getter
    : Identifier{p("get")}? propertyName
    ;

setter
    : Identifier{p("set")}? propertyName
    ;

eos
    : SemiColon
    | EOF
    | {lineTerminatorAhead()}?
    | {closeBrace()}?
    ;