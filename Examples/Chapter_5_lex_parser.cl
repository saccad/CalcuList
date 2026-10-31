TOKEN: /* keyword tokens: declarations */
double_t, long_t, int_t, char_t, bool_t, string_t, list_t, 
json_t, null_t,  type_t, true_t, false_t, exc_t, halt_t;
:TOKEN;
/* keyword tokens: definitions */
double_t="double"; long_t="long"; int_t="int"; char_t="char"; 
bool_t="bool"; string_t="string"; list_t="list"; json_t="json";
null_t="null"; type_t="type"; true_t="true"; false_t="false";
exc_t="exc"; halt_t="halt";

TOKEN: /* builtin function tokens: declarations */
exp, log,  pow, len, rand, ind, tuple, isKey, pDate, gDate;
/* builtin function tokens: definitions */
exp= "_exp"; log= "_log"; pow= "_pow"; len= "_len"; 
rand= "_rand"; ind= "_ind"; tuple= "_tuple"; isKey= "_isKey"; 
pDate= "_pDate"; gDate= "_gDate";

TOKEN: /* operator and separator tokens: declarations */
plus, minus, plusplus, star, div, divtr, remainder, assign, 
print, semicolon,  colon, dot, comma, qmark, emark, eq, neq, 
gt, lt, gte, lte, and,  or, cast, continue, pipe, map, lambda, 
opar, cpar, ospar, cspar, obrace, cbrace, obraceset, cbraceset,
outs, ins,  hash, underscore;
/* operator and separator tokens: definitions */
plus="+"; minus="-"; plusplus="++"; star="*"; div="/"; divtr="//";
remainder="%"; assign="="; print="^"; semicolon=";";
colon=":"; dot="."; comma=","; qmark= "?"; emark="!"; 
eq="=="; neq="!="; gt= ">"; lt= "<"; gte= ">="; lte= "<=";
and= "&&"; or="||"; cast="@"; continue= "&"; pipe="|"; 
map="->"; lambda= "\\";opar= "("; cpar= ")"; 
ospar= "["; cspar= "]"; obrace= "{"; cbrace= "}";
obraceset= "{!"; cbraceset= "!}"; 
outs= ">>"; ins= "<<"; hash="#"; underscore= "_";

TOKEN: /* category tokens: declarations */
INT, LONG, DOUBLE,  BOOL, CHAR, STRING, TYPE, 
ID, IDL, PRINTOPT, OP_ASS;
/* category tokens: definitions */
INT="INT"; LONG="LONG"; DOUBLE="DOUBLE"; BOOL="BOOL"; 
CHAR="CHAR"; STRING= "STRING"; TYPE= "TYPE"; 
ID="ID"; IDL="IDL"; PRINTOPT= "PRINTOPT"; OP_ASS= "OP_ASS";

LEX: /* lexical analyzer groups: declarations */
Types, /* all keyword tokens of TYPE category except null */ 
Bools, /* keyword tokens of BOOL category */
Others, /*  all other keyword tokens: halt, exc and null */
Builtins; /* all builtin function tokens */
/* lexical analyzer groups: definitions */
:LEX;
Types = [TOKEN.double_t, TOKEN.long_t, TOKEN.int_t, TOKEN.char_t, TOKEN.bool_t, TOKEN.string_t, TOKEN.list_t, TOKEN.json_t, TOKEN.type_t];
Bools = [ TOKEN.false_t, TOKEN.true_t ];
Others = [ TOKEN.halt_t, TOKEN.exc_t, TOKEN.null_t ];
Builtins = [ TOKEN.exp, TOKEN.log,  TOKEN.pow, TOKEN.len, TOKEN.rand, TOKEN.ind, TOKEN.tuple, TOKEN.isKey, TOKEN.pDate, TOKEN.gDate];

LEX:  tokenize*/1, /* tokenizing function */
 tokenize1*/3, /* auxilary tokenizing function defined next*/
 err; /* prefix string for reporting a lexical error */
:LEX;
err= "LEX ERROR: ";
/* tokenizes the current line and returns the list of all tokens */
tokenize*(line)->tokenize1(line,0,_len(line)); 

LEX: /* basic functions for character manipulation */
isLetter/1, isDigit/1, isLetterOrDigitOrUnd/1, isWhite/1;

isLetter(c)-> (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');

isDigit(c)-> c >= '0' && c <= '9';

isLetterOrDigitOrUnd(c) -> isLetter(c) || isDigit(c) || c=='_';

isWhite(c) -> c==' ' || c=='\t'; /* simplified check */

LEX:  isKeyword/4, /* keyword recognition */ isKey/5; /* auxilary function */

/* checks whether the string starting at line index i is included in the list keywords */
/* returns the checked key if the test is positive or null otherwise */
isKeyword(keywords,line,i,len) -> <endKey,key> 
| keywords==[]: null
| {! key= keywords[.] & endKey=i+_len(key) !} 
  isKey(line,i,len,endKey,key): key
| : isKeyword(keywords[>],line,i,len);
isKey(line,i,len, endKey,key)-> _ind(line,key,i)==i && ( endKey == len || !isLetterOrDigitOrUnd(line[endKey]));

LEX: IDval, /* value (string) of the last recognized token */
	checkIDorIDL*/3, checkIDLsuffix*/3; /* ID/IDL recognition */ 

/* checks whether the substring at line index i is an ID or IDL */
/* returns the token ID or IDL and the line index after the recognized token */
checkIDorIDL*(line,i,len)-> <TOKEN*> 
| i==len: [ID,i]
| isLetterOrDigitOrUnd(line[i]): checkIDorIDL(line,i+1,len)
| line[i]=='.' && (i+1==len || !isLetter(line[i+1])): 
               [IDL,-1] {! >(err ++ "wrong string for denoting " ++ IDL ++ "\n") !} 
| line[i]=='.': checkIDLsuffix(line,i+1,len)
| : [ID,i];

/* checks the IDL suffix, i.e. the string after the dot */
checkIDLsuffix*(line,i,len)-> <TOKEN*> 
| i==len: [IDL,i]
| isLetterOrDigitOrUnd(line[i]): checkIDLsuffix(line,i+1,len)
| : [IDL,i];

LEX: checkNum*/4, checkDecimalPart*/3, checkExpPart*/4, checkExpDigits*/3; /* recognition of DOUBLE, LONG, INT */

/* checks whether the string starting at line index i is a number: DOUBLE, LONG, INT  */
/* if initPoint is true, the number is a DOUBLE starting with an initial dot */
/* returns a list with two elements: the recognized number token and the line index after the token */
/* - line index equal to -1 denotes error */
checkNum*(line,i,len,initPoint)-> <TOKEN*> 
| i==len: initPoint? [DOUBLE,i]: [INT,i]
| (line[i]=='l' || line[i]=='L') && initPoint:  	 
	{! >(err ++ "wrong character '") & >(line[i]) & >("' for denoting a double\n") !}
	[DOUBLE,-1]
| line[i]=='l' || line[i]=='L':  [LONG,i+1]	 
| isDigit(line[i]): checkNum(line,i+1,len,initPoint)
| line[i]=='.' && initPoint: 
     {! >(err ++ "duplicated character '.' for denoting a double\n") !} 
     [DOUBLE,-1]
| line[i]=='.': checkDecimalPart(line,i+1,len)
| line[i]=='e' || line[i]=='E': checkExpPart(line,i+1,len,false)
| : initPoint? [DOUBLE,i]: [INT,i];

/* check the decimal part of a DOUBLE */
checkDecimalPart*(line,i,len)-> <TOKEN*>
| i==len: [DOUBLE,i]
| line[i]=='l' || line[i]=='L': 
    {! >(err ++ "wrong character '") & >(line[i]) & >("' for denoting a double\n") !} 
    [DOUBLE,-1]
| line[i]=='.': 
    {! >(err ++ "duplicated char '.' for denoting a double\n") !} 
    [DOUBLE,-1]
| isDigit(line[i]): checkDecimalPart(line,i+1,len)
| line[i]=='e' || line[i]=='E': checkExpPart(line,i+1,len,false)
| : [DOUBLE,i];

/* check the exponent part of a DOUBLE */
checkExpPart*(line,i,len,sign)-> <TOKEN*>
| i==len: {! >(err ++ "incomplete exponent for a double\n") !} 
          [DOUBLE,-1]
| (line[i]=='+' || line[i]=='-') && sign:
    {! >(err ++ "double sign for the exponent\n") !} 
    [DOUBLE,-1]
| line[i]=='+' || line[i]=='-': checkExpPart(line,i+1,len,true)
| isDigit(line[i]): checkExpDigits(line,i+1,len)
| : {! >(err ++ "incomplete exponent for denoting a double\n") !} 
    [DOUBLE,-1];

/* check the exponent digits */
checkExpDigits*(line,i,len)-> <TOKEN*>
| i==len: [DOUBLE,i]
| isDigit(line[i]): checkExpDigits(line,i+1,len)
| : [DOUBLE,i];

LEX: skipToSingleQuote*/3, skipToDoubleQuote*/3, skipToEndComment*/3; /* skip characters */

/* skips a single char along with the next single quote */
skipToSingleQuote*(line,i,len)-> 
| i>=len-1: -1 {! >(err ++ "incomplete char\n") !}
| line[i]=='\'': -1 {! >(err ++ "void char\n") !}
| line[i+1]=='\'': i+2
| : -1 {! >("LEX ERROR: missing final quote for char\n") !};

/* skips all chars up to and including the final double quote */
skipToDoubleQuote*(line,i,len)-> 
| i==len: -1 {! >("incomplete string\n") !}
| line[i]=='"': i+1
| : skipToDoubleQuote(line,i+1,len);

/* skips all chars up to the end of the comment */
skipToEndComment*(line,i,len)-> 
| i>=len-1: -1 {! >(err ++ "incomplete comment\n") !}
| line[i]=='*' && line[i+1]=='/': i+2
| : skipToEndComment(line,i+1,len);

/* tokenizes the sub-line starting at index i */
tokenize1*(line,i,len)-> <TOKEN*, IDorIDL, numT, iLine, key> 
| i==len: [] 
| isWhite(line[i]): tokenize1(line,i+1,len)
| line[i]==';': [ semicolon | tokenize1(line,i+1,len) ]
| line[i]==':': [ colon | tokenize1(line,i+1,len) ] 
| line[i]==',': [ comma | tokenize1(line,i+1,len) ] 
| line[i]=='^': [ print | tokenize1(line,i+1,len) ]
| line[i]=='#': [ hash | tokenize1(line,i+1,len) ] 
| line[i]=='!': i+1<len && line[i+1]=='='? 
				[neq | tokenize1(line,i+2,len) ] : 
				i+1<len && line[i+1]=='}'? 
				[cbraceset | tokenize1(line,i+2,len) ] : 
				[emark | tokenize1(line,i+1,len) ]
| line[i]=='(': [ opar | tokenize1(line,i+1,len) ] 
| line[i]==')': [ cpar | tokenize1(line,i+1,len) ] 
| line[i]=='[': [ ospar | tokenize1(line,i+1,len) ] 
| line[i]==']': [ cspar | tokenize1(line,i+1,len) ] 
| line[i]=='{': i+1<len && line[i+1]=='!'? 
				[obraceset | tokenize1(line,i+2,len) ] : 
				[obrace | tokenize1(line,i+1,len) ]
| line[i]=='}': [ cbrace | tokenize1(line,i+1,len) ] 
| line[i]=='?': [ qmark | tokenize1(line,i+1,len) ] 
| line[i]=='\'': {! iLine=skipToSingleQuote(line,i+1,len) !} 
                 iLine==-1? []: [ CHAR | tokenize1(line,iLine,len) ]  
| line[i]=='"': {! iLine=skipToDoubleQuote(line,i+1,len) !} 
                 iLine==-1? []: [ STRING | tokenize1(line,iLine,len) ]
| line[i]=='%': i+1<len && (line[i+1]=='*' || line[i+1]=='>')? 
				[PRINTOPT | tokenize1(line,i+2,len) ] : 
				[remainder | tokenize1(line,i+1,len) ]
| line[i]=='>': i+1<len && line[i+1]=='>'? 
				[outs | tokenize1(line,i+2,len) ] : 
				i+1<len && line[i+1]=='='? 
				[gte | tokenize1(line,i+2,len) ] : 
				[gt | tokenize1(line,i+1,len) ]
| line[i]=='<': i+1<len && line[i+1]=='<'? 
				[ins | tokenize1(line,i+2,len) ] : 
				i+1<len && line[i+1]=='='?
				[lte | tokenize1(line,i+2,len) ] : 
				[lt | tokenize1(line,i+1,len) ]
| line[i]=='&': i+1<len && line[i+1]=='&'? 
				[and | tokenize1(line,i+2,len) ] : 
				[continue | tokenize1(line,i+1,len) ]
| line[i]=='|': i+1<len && line[i+1]=='|'? 
				[or | tokenize1(line,i+2,len) ] : 
				[pipe | tokenize1(line,i+1,len) ]
| line[i]=='=': i+1<len && line[i+1]=='='? 
				[eq | tokenize1(line,i+2,len) ] : 
				[assign | tokenize1(line,i+1,len) ]
| line[i]=='+': i+1<len && line[i+1]=='+'? 
				i+2<len && line[i+2]=='='?
				[OP_ASS | tokenize1(line,i+3,len) ] : 
				[plusplus | tokenize1(line,i+2,len) ] : 
				i+1<len && line[i+1]=='='?
				[OP_ASS | tokenize1(line,i+2,len) ] : 
				[plus | tokenize1(line,i+1,len) ]
| line[i]=='-': i+1<len && line[i+1]=='>'? 
				[map | tokenize1(line,i+2,len) ] : 
				i+1<len && line[i+1]=='='?
				[OP_ASS | tokenize1(line,i+2,len) ] : 
				[minus | tokenize1(line,i+1,len) ]
| line[i]=='/' && i+1<len && line[i+1]=='*': 
                {! iLine=skipToEndComment(line,i+2,len) !} iLine==-1? []: 
                tokenize1(line,iLine,len)
| line[i]=='/': i+1<len && line[i+1]=='/'? 
				i+2<len && line[i+2]=='='?
				[OP_ASS | tokenize1(line,i+3,len) ] : 
				[divtr | tokenize1(line,i+2,len) ] : 
				i+1<len && line[i+1]=='='?
				[OP_ASS | tokenize1(line,i+2,len) ] : 
				[div | tokenize1(line,i+1,len) ]
| line[i]=='*': i+1<len && line[i+1]=='='? 
				[OP_ASS | tokenize1(line,i+2,len) ] : 
				[star | tokenize1(line,i+1,len) ]
| line[i]=='@': [ cast | tokenize1(line,i+1,len) ] 
| line[i]=='.': [ dot | tokenize1(line,i+1,len) ] 
| line[i]=='_' && i<len && isLetter(line[i+1]): 
                {! key=isKeyword(Builtins,line,i,len) !}
                {! >(key==null? err++ "wrong built-in function\n":"") !}
                key!=null? [ key | tokenize1(line,i+_len(key),len) ]: [] 
| line[i]=='_': [ underscore | tokenize1(line,i+1,len) ] 
| line[i]=='\\': [ lambda | tokenize1(line,i+1,len) ] 
| {! key=isKeyword(Types,line,i,len) !} key!=null: 
  [ TYPE | tokenize1(line,i+_len(key),len)] 
| {! key=isKeyword(Bools,line,i,len) !} key!=null: 
  [ BOOL | tokenize1(line,i+_len(key),len)] 
| {! key=isKeyword(Others,line,i,len) !} key!=null: 
  [ key | tokenize1(line,i+_len(key),len)] 
| isLetter(line[i]): {! IDorIDL=checkIDorIDL(line,i+1,len) !}
                IDorIDL[1]==-1? []: [ IDorIDL[0] | tokenize1(line,IDorIDL[1],len) ]
                {! IDval= IDorIDL[1]!=-1 && IDorIDL[0]==ID? line[i:IDorIDL[1]]: null!}
| isDigit(line[i]): {! numT= checkNum(line,i+1,len,false) !} 
                numT[1]==-1? []: [ numT[0] | tokenize1(line,numT[1],len) ]
| : {! >(err ++ "unexpected character \'") & >(line[i]) & >("\'\n")  !} [];

^tokenize("x=2*L.y+3.14@int+'H';");
^tokenize("a=1.3e+2@int+3+_len(b.c)+d('a',\"hi\");");
^tokenize("a=1.3.5e+2@int+3+_len(b.c)+d('a',\"hi\");");
^tokenize("a=1.3e+2@int+3+_len(b.)+d('a',\"hi\");");

PARSER: err, errGen*/1, errToken1*/2, errTokenL*/3, errIncomplete*/1;
:PARSER;
err= "PARSER ERROR: "; /* prefix of parser error notification */

/* notify a generic error described by st */
errGen*(st) -> null {! >(err ++ st ++ "\n") !};

/* notify the error: token wc is not equal to token ct */
errToken1*(wt,ct) -> null
  {! >(err ++ " wrong token '" ++ wt ++ "' - expected token '" ++ ct ++ "'\n") !};

/* notify the error: token wt not in in the token list tl named tln */
errTokenL*(wt,tln,tl) -> null
  {! >(err ++ " wrong token '" ++ wt ++ "' - expected token in " ++ tln ++ ": \n") & >(tl) & >("\n") !};
  
/* notify the error: incomplete definition for the command comm */
errIncomplete*(comm) -> null
  {! >(err ++ "incomplete " ++ comm ++ " definition\n") !};

PARSER: ltokens, parser*/0, CalcuListSession*/0, statement*/1, readLine*/1, errCheck*/1;

/* 
RULE
⟨CalcuListSession⟩ ::= { [ ⟨statement⟩ ]  ‘;’ } halt

REWRITTEN RULE
⟨CalcuListSession⟩ ::= ‘;’ ⟨CalcuListSession⟩ | halt | ⟨statement⟩ ‘;’ ⟨CalcuListSession⟩
*/

parser*()-> {! >("** CalcuList Parser **\n") !} CalcuListSession();

CalcuListSession*()-> <TOKEN*,line> 
 {! >(">>>") !} /* print the parser cursor */
 {! ltokens=LEX.tokenize(readLine("")) !} /* read a command line */
 {! >("ltokens:") & >(ltokens) & >("\n")!} 
| ltokens==[]: CalcuListSession() /* skip a blank line */
| ltokens[.]==semicolon: /* empty command */
		{! [ ltokens[>]!=[]? errGen("found tokens after '" ++ semicolon ++"'"): null ] !} 
        CalcuListSession()
| ltokens[.]==halt_t: {! >("bye") !} null /* end of the session */
| : /* parse a statement */
    {! ltokens=statement(ltokens) !} 
    /* if ltokens is null, an error has been already detected
       otherwise a possible final error is checked */
    {! [ errCheck(ltokens) ] !}
    CalcuListSession();

/* read a line of characters for lexical tokenization */
readLine*(line) -> <line1, l1> 
    {! <(line1) !} {! l1=_len(line1) !} /* read a line of character */
|	l1 > 0 && line1[l1-1] =='_': /* the command continues in the next line */
	readLine(line ++ line1[:l1-1])
|:	line ++ line1; /* last line of the command */

/* check for any errors at the end of the command */
errCheck*(ltokens)-> <TOKEN*>
| ltokens== null: null /* an error already detected */
| ltokens==[]: errGen( "missing '" ++ semicolon)
| ltokens[.] != semicolon: errToken1(ltokens[.],semicolon)
| ltokens[>]!=[]: errGen("found tokens after '" ++ semicolon ++"'")
|: ltokens;
	

PARSER: First_statement, varFuncLabDef*/1, varFuncDef*/1, query*/1, serviceCommand*/1, isCurrToken/2, member/2, isTokenIn/2, acceptToken*/2, acceptTokenIn*/2;

/*
RULE
⟨statement⟩ ::= ID ⟨varFuncLabDef⟩ IDL ⟨varFuncDef⟩ ‘ˆ’ ⟨query⟩ ‘:’ | ID ‘#’ | ‘!’ ⟨serviceCommand⟩

First(⟨statement⟩) = { ‘ID’, ‘IDL’, ‘ˆ’, ‘:’, ‘!’ }
*/

:TOKEN;
PARSER.First_statement=[ID, IDL, print, colon, emark];
:PARSER;

statement*(ltokens) -> <TOKEN*> 
| ltokens[.]==ID: varFuncLabDef(ltokens[>])
| ltokens[.]==IDL: varFuncDef(ltokens[>])
| ltokens[.]==print: query(ltokens[>])
| ltokens[.]==colon: acceptTokenIn(ltokens[>], [ID,hash])
| ltokens[.]==emark: serviceCommand(ltokens[>])
| : errTokenL(ltokens[.],"First(Statement)", First_statement);
 
/* checks if the current token (ltokens head) is equal to t */
isCurrToken(ltokens,t)->ltokens!=null && ltokens!=[] && ltokens[.]==t;


/* checks if the token t is in the list tokenL */
isTokenIn(t,tokenL)->tokenL!=null && member(t,tokenL);
/* list membership */
member(x,L)-> L!=[] && ( x==L[.] || member(x,L[>]) );

/* the current token is consumed if equal to t, otherwise an error (null) is returned */
acceptToken*(ltokens,t)-> 
| ltokens==null: null
| ltokens==[]: errGen("expected token '"++ t ++ "' - none found")
| ltokens[.] == t: ltokens[>]
| : errToken1(ltokens[.],t);

/* the current token is consumed if is in tokenL, otherwise an error is returned */
acceptTokenIn*(ltokens,tokenL)-> 
| ltokens==null: null
| ltokens==[]: errTokenL("none", "", tokenL)
| isTokenIn(ltokens[.],tokenL): ltokens[>]
| : errTokenL(ltokens[.],"", tokenL);


PARSER: Tokens_service;
/*
RULE
⟨serviceCommand⟩ ::= ‘labels’ | ‘save’ | ‘clops’ | ... 

First(⟨serviceCommand⟩) = { ‘labels’, ‘save’, ‘clops’, . . . }
*/
Tokens_service=[ "labels", "save", "clops" ]; /* to be extended */

serviceCommand*(ltokens) -> <TOKEN*,LEX*> 
| ltokens==[]: errIncomplete("serviceCommand")
| isCurrToken(ltokens,ID) && isTokenIn(IDval,Tokens_service): ltokens[>]
|: errTokenL(ltokens[.]==ID?IDval:ltokens[.],"",Tokens_service);

PARSER: First_query1, First_ifExp, query1*/1, query2*/1, out*/1, ifExp*/2;

/*
RULE
⟨query⟩ ::= [ ⟨out⟩ ] ⟨ifExp⟩ [ PRINTOPT ] { ‘&’ ⟨ifExp⟩ [ PRINTOPT ] }
⟨out⟩ ::= ‘»’ ‘(’ STRING ‘)’
REWRITTEN RULES
⟨query⟩ ::= ⟨out⟩ ⟨query1⟩ ⟨query2⟩
⟨query1⟩ ::= ⟨ifExp1⟩ [ PRINTOPT ]
⟨query2⟩ ::= ‘&’ ⟨query1⟩ ⟨query2⟩ | epsilon
⟨out⟩ ::= ‘»’ ‘(’ STRING ‘)’ | epsilon

First(⟨query⟩) = {">>"} \cup First(⟨query1⟩) 
First(⟨query1⟩) = First(⟨ifExp⟩) 
First(⟨query2⟩) = { "&", epsilon }
First(⟨out⟩) = {">>", epsilon}
Follows(query2)={ ';' }
Follows(out)= First(⟨ifExp⟩)
*/
query*(ltokens) -> <TOKEN*>
| ltokens==null: null /* an error already detected */
| ltokens==[]: errIncomplete("query")
| : query2(query1(out(ltokens)));

query1*(ltokens) -> <TOKEN*>
| ltokens==null: null /* an error already detected */
| ltokens==[]: errIncomplete("query1")
| isTokenIn(ltokens[.],First_ifExp): 
    {! ltokens = ifExp(ltokens,true) !}
    isCurrToken(ltokens,PRINTOPT)? ltokens[>]: ltokens
| : errTokenL(ltokens[.],"First(query1)",First_query1);

query2*(ltokens) -> <TOKEN*>
| ltokens==null: null 
| ltokens==[]: errIncomplete("query2")
| isCurrToken(ltokens, continue): query2(query1(ltokens[>]))
| isCurrToken(ltokens, semicolon): ltokens /* check of follows */
| : errTokenL(ltokens[.],"First\\eps(query2) U Follows(query2)",[continue,semicolon]);

out*(ltokens)-> <TOKEN*>
| ltokens==null: null 
| ltokens==[]: errIncomplete("out")
| isCurrToken(ltokens,outs):
  acceptToken(acceptToken(acceptToken(ltokens[>],opar),STRING),cpar)
|: ltokens;

PARSER: Follows_ifExp1, ifExp1*/1, orExp*/1;

/*
⟨ifExp⟩::=⟨orExp⟩ [‘?’⟨ifExp⟩‘:’⟨ifExp⟩]
⟨ifExp⟩::=⟨orExp⟩ ⟨ifExp1⟩
⟨ifExp1⟩::= ‘?’⟨ifExp⟩‘:’⟨ifExp⟩ | epsilon
first(⟨ifExp⟩) ::= first(⟨orExp⟩)
first(⟨ifExp1⟩) ::= { "?", epsilon }
follows(⟨ifExp1⟩) ::= { "%*", "%>", "&", ";", ":", ",", ")", "]", "}", "|",  "{!",  "!}", "{!", "!}" }
*/
:TOKEN;
PARSER.Follows_ifExp1 = [ PRINTOPT, continue, semicolon, colon, comma, cpar, cspar, cbrace, pipe,  obraceset, cbraceset ];
:PARSER;

ifExp*(ltokens, firstChecked)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("ifExp")
| !firstChecked && !isTokenIn(ltokens[.],First_ifExp): 
  errTokenL(ltokens[.],"First(ifExp)",First_ifExp)
| : ifExp1(orExp(ltokens));
ifExp1*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("ifExp1")
| isCurrToken(ltokens,qmark):  ifExp(acceptToken(ifExp(ltokens[>],false),colon),false)
| isTokenIn(ltokens[.],Follows_ifExp1): ltokens
| : errTokenL(ltokens[.],"First\\eps(ifExp1) U Follows(ifExp1)",[qmark] ++ Follows_ifExp1);


PARSER: Follows_orExp1, orExp1*/1, andExp*/2;
/*
⟨orExp⟩ ::= ⟨andExp⟩ {‘||’⟨andExp⟩}
⟨orExp⟩ ::= ⟨andExp⟩ ⟨orExp1⟩
⟨orExp1⟩ ::= ‘||’⟨andExp⟩ ⟨orExp1⟩ | epsilon

First(⟨orExp⟩) = First(⟨andExp⟩)
First(⟨orExp1⟩) = { ‘||’, epsilon }
Follows(⟨orExp1⟩) = { "?" } \cup Follows(⟨ifExp1⟩) 

*/

Follows_orExp1 = [ TOKEN.qmark ] ++ Follows_ifExp1;

orExp*(ltokens )-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("orExp")
| : orExp1(andExp(ltokens,true)); 
  /* First_ifExp = First_orExp = First_andExp previously checked */ 

orExp1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("orExp1")
| isCurrToken(ltokens, or): orExp1(andExp(ltokens[>],false)) 
  /* First_andExp to be checked */
| isTokenIn(ltokens[.],Follows_orExp1): ltokens
| : errTokenL(ltokens[.],"First\\eps(orExp1) U Follows(orExp1)",[or] ++ Follows_orExp1);

PARSER: First_andExp, Follows_andExp1, andExp1*/1, eqExp*/2;

/*
RULE
⟨andExp⟩ ::= ⟨eqExp⟩ {‘&&’⟨eqExp⟩}

REWRITTEN RULE
⟨andExp⟩ ::= ⟨eqExp⟩ ⟨andExp1⟩
⟨andExp1⟩ ::= ‘&&’⟨eqExp⟩ ⟨andExp1⟩ | epsilon

First(⟨andExp⟩) = First(⟨eqExp⟩)
First(⟨andExp1⟩) = { ‘&&’, epsilon }
Follows(⟨andExp1⟩) = { "||" } \cup Follows(⟨orExp1⟩) 

*/

Follows_andExp1 = [ TOKEN.or ] ++ Follows_orExp1;

andExp*(ltokens, firstChecked)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("andExp")
| !firstChecked && !isTokenIn(ltokens[.],First_andExp): 
   errTokenL(ltokens[.],"First(andExp)",First_andExp)
| : /* First_eqExp previously checked */
   andExp1(eqExp(ltokens,true));  

andExp1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("andExp1")
| isCurrToken(ltokens, and): andExp1(eqExp(ltokens[>],false)) 
  /* First_eqExp to be checked */
| isTokenIn(ltokens[.],Follows_andExp1): ltokens
| : errTokenL(ltokens[.],"First\\eps(andExp1) U Follows(andExp1)",[and] ++ Follows_andExp1);

PARSER: First_eqExp, Follows_eqExp1, eqExp1*/1, relExp*/2;

/*
RULE
⟨eqExp⟩ ::= ⟨relExp⟩ { ('==' | '!=' ) ⟨relExp⟩}

REWRITTEN RULE
⟨eqExp⟩ ::= ⟨relExp⟩ ⟨eqExp1⟩
⟨eqExp1⟩ ::= ‘&&’⟨relExp⟩ ⟨eqExp1⟩ | epsilon

First(⟨eqExp⟩) = First(⟨relExp⟩)
First(⟨eqExp1⟩) = { '==', '!=', epsilon }
Follows(⟨eqExp1⟩) = { '&&' } \cup Follows(⟨andExp1⟩) 

*/

Follows_eqExp1 = [ TOKEN.and ] ++ Follows_andExp1;

eqExp*(ltokens, firstChecked)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("eqExp")
| !firstChecked && !isTokenIn(ltokens[.],First_eqExp):
   errTokenL(ltokens[.],"First(eqExp)",First_eqExp)
| : eqExp1(relExp(ltokens,true)); /* First_relExp previously checked */

eqExp1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("eqExp1")
| isCurrToken(ltokens, eq) || isCurrToken(ltokens, neq): 
     eqExp1(relExp(ltokens[>],false))  /* First_relExp to be checked */
| isTokenIn(ltokens[.],Follows_eqExp1): ltokens
| : errTokenL(ltokens[.],"First\\eps(eqExp1) U Follows(eqExp1)",[eq,neq] ++ Follows_eqExp1);

PARSER: First_relExp, Follows_relExp1, relExp1*/1, expr*/2;

/*
RULE
⟨relExp⟩ ::= ⟨expr⟩ { ('>' | '>=','<' | '<=' ) ⟨expr⟩}

REWRITTEN RULE
⟨relExp⟩ ::= ⟨expr⟩ ⟨relExp1⟩
⟨relExp1⟩ ::= ('>' | '>=','<' | '<=' ) ⟨expr⟩ ⟨relExp1⟩ | epsilon

First(⟨relExp⟩) = First(⟨expr⟩)
First(⟨relExp1⟩) = { '>' | '>=','<' | '<=', epsilon }
Follows(⟨relExp1⟩) = { '==', '!=' } \cup Follows(⟨eqExp1⟩) 

*/
Follows_relExp1 = [ TOKEN.eq, TOKEN.neq ] ++ Follows_eqExp1;

relExp*(ltokens, firstChecked)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("relExp")
| !firstChecked && !isTokenIn(ltokens[.],First_relExp):
   errTokenL(ltokens[.],"First(relExp)",First_relExp)
|: /* First_expr previously checked */
   relExp1(expr(ltokens,true)); 

relExp1*(ltokens) -> <TOKEN*,relOp> {! relOp=[gt,gte,lt,lte] !}
| ltokens==null: null
| ltokens==[]: errIncomplete("relExp1")
| isTokenIn(ltokens[.],relOp):
  /* First_expr to be checked */
  relExp1(expr(ltokens[>],false)) 
| isTokenIn(ltokens[.],Follows_relExp1): ltokens
| : errTokenL(ltokens[.],"First\\eps(relExp1) U Follows(relExp1)",relOp ++ Follows_relExp1);


PARSER: First_expr, Follows_expr1,  expr1*/1, expr2*/1, expr3*/1, term*/2;

/*
RULE
⟨expr⟩ ::= ⟨term⟩ ( {  ('+' | '-' ) ⟨term⟩ } | {  '++' ⟨term⟩ } )

REWRITTEN RULE
⟨expr⟩ ::= ⟨term⟩ ⟨expr1⟩
⟨expr1⟩ ::= ('+' | '-' ) ⟨term⟩ ⟨expr2⟩ | '++' ⟨term⟩ ⟨expr3⟩  | epsilon
⟨expr2⟩ ::= ('+' | '-' ) ⟨term⟩ ⟨expr2⟩ | epsilon
⟨expr3⟩ ::= '++' ⟨term⟩ ⟨expr3⟩ | epsilon

First(⟨expr⟩) = First(⟨term⟩)
First(⟨expr1⟩) = { '+', '-', '++', epsilon }
Follows(⟨expr1⟩) = { '>' | '>=','<' | '<=' } \cup Follows(⟨relExp1⟩) 
First(⟨expr2⟩) = { '+', '-', epsilon }
Follows(⟨expr2⟩) = Follows(⟨expr1⟩) 
First(⟨expr3⟩) = { '++', epsilon }
Follows(⟨expr3⟩) = Follows(⟨expr1⟩) 

*/

Follows_expr1 = [ TOKEN.gt, TOKEN.gte, TOKEN.lt, TOKEN.lte ] ++ Follows_relExp1;


expr*(ltokens, firstChecked)-> 
| ltokens==null: null
| ltokens==[]: errIncomplete("expr")
| !firstChecked && !isTokenIn(ltokens[.],First_expr):
   errTokenL(ltokens[.],"First(expr)",First_expr)
|: /* First_term previously checked */
   expr1(term(ltokens,true)); 

expr1*(ltokens) -> <TOKEN*>
| ltokens==null: null
| ltokens==[]: errIncomplete("expr1")
| isTokenIn(ltokens[.], [plus,minus]): expr2(term(ltokens[>],false))  
| isCurrToken(ltokens, plusplus): expr3(term(ltokens[>],false))  
| isTokenIn(ltokens[.],Follows_expr1): ltokens
|: errTokenL(ltokens[.],"First\\eps(expr1) U Follows(expr1)",[plus,minus,plusplus] ++ Follows_expr1);

expr2*(ltokens) -> <TOKEN*>
| ltokens==null: null
| ltokens==[]: errIncomplete("expr2")
| isTokenIn(ltokens[.], [plus,minus]): expr2(term(ltokens[>],false))  
| isTokenIn(ltokens[.],Follows_expr1): ltokens
|: errTokenL(ltokens[.],"First\\eps(expr2) U Follows(expr2)",[plus,minus] ++ Follows_expr1);

expr3*(ltokens) -> <TOKEN*>
| ltokens==null: null
| ltokens==[]: errIncomplete("expr3")
| isCurrToken(ltokens, plusplus): expr3(term(ltokens[>],false)) 
| isTokenIn(ltokens[.],Follows_expr1): ltokens
|: errTokenL(ltokens[.],"First\\eps(expr3) U Follows(expr3)",[plusplus] ++ Follows_expr1);

PARSER: First_term, Follows_term1, term1*/1, uniTerm*/1;

/*
RULE
⟨term⟩ ::= ⟨uniTerm⟩ ( {  ('*' | '/' | '//' | '%' ) ⟨uniTerm⟩ } 

REWRITTEN RULE
⟨term⟩ ::= ⟨uniTerm⟩ ⟨term1⟩
⟨term1⟩ ::= ('*' | '/' | '//' | '%' )  ⟨term⟩ ⟨expr2⟩ | epsilon

First(⟨term⟩) = First(⟨uniTerm⟩)
First(⟨term1⟩) = { '*', '/', '//', '%', epsilon }
Follows(⟨term1⟩) = { '+', '-', '++' } \cup Follows(⟨expr1⟩) 

*/

Follows_term1 = [ TOKEN.plus, TOKEN.minus, TOKEN.plusplus ] ++ Follows_expr1;

term*(ltokens, firstChecked)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("term")
| !firstChecked && !isTokenIn(ltokens[.],First_term):
   errTokenL(ltokens[.],"First(term)",First_term)
| : term1(uniTerm(ltokens));

term1*(ltokens) -> <TOKEN*>
| ltokens==null: null
| ltokens==[]: errIncomplete("term1")
| isTokenIn(ltokens[.],[div,star,divtr,remainder]): 
    term1(uniTerm(ltokens[>]))  
| isTokenIn(ltokens[.],Follows_term1): ltokens
|: errTokenL(ltokens[.],"First\\eps(term1) U Follows(term1)",[div,star,divtr,remainder] ++ Follows_term1);

PARSER: uniTerm1*/1, uniTerm2*/1, simpTerm*/1; 

/*
RULE
⟨uniTerm⟩ ::= ( { '+' | '-' } | { '!' } ) ⟨simpTerm⟩

REWRITTEN RULE
⟨uniTerm⟩ ::= ( '+' | '-' ) ⟨uniTerm1⟩ |  '!' ⟨uniTerm2⟩ | ⟨simpTerm⟩
⟨uniTerm1⟩ ::= ( '+' | '-' ) ⟨uniTerm1⟩ | ⟨simpTerm⟩
⟨uniTerm2⟩ ::=  '!' ⟨uniTerm2⟩ | ⟨simpTerm⟩

First(⟨uniTerm⟩) = { '+', '-', '!' } \cup First(⟨simpTerm⟩)
First(⟨uniTerm1⟩) = { '+', '-' } \cup First(⟨simpTerm⟩)

*/
uniTerm*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("uniTerm")
| isTokenIn(ltokens[.], [plus,minus]): uniTerm1(ltokens[>])
| isCurrToken(ltokens, emark): uniTerm2(ltokens[>])
| :  simpTerm(ltokens);

uniTerm1*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("uniTerm1")
| isTokenIn(ltokens[.], [plus,minus]): uniTerm1(ltokens[>])
| :  simpTerm(ltokens);

uniTerm2*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("uniTerm2")
| isCurrToken(ltokens, emark): uniTerm2(ltokens[>])
| :  simpTerm(ltokens);

PARSER: Follows_simpTerm1, Follows_simpTerm2, Follows_simpTerm3, simpTerm1*/1, simpTerm2*/1, simpTerm3*/1, fact*/1, actPars*/1, sliceElems*/1;

/*
RULE
⟨simpTerm⟩ ::= ⟨fact⟩ { '(' ⟨actPars⟩ ')' }_2 { '@' TYPE | '[' ⟨sliceElems⟩ ']' } 

REWRITTEN RULE
⟨simpTerm⟩ ::= ⟨fact⟩ ⟨simpTerm1⟩ ⟨simpTerm2⟩
⟨simpTerm1⟩ ::= '(' ⟨actPars⟩ ')' ⟨simpTerm3⟩ | epsilon
⟨simpTerm3⟩ ::= '(' ⟨actPars⟩ ')' | epsilon
⟨simpTerm2⟩ ::=  ('@' TYPE | '[' ⟨sliceElems⟩ ']') ⟨simpTerm2⟩ | epsilon

First(⟨simpTerm⟩) = First(⟨fact⟩)
First(⟨simpTerm1⟩) = First(⟨simpTerm3⟩) = { '(', epsilon } 
First(⟨simpTerm2⟩) = { '@', '[', epsilon }
Follows(⟨simpTerm1⟩) = Follows(⟨simpTerm3⟩) = (First(⟨simpTerm2⟩) \ {epsilon} ) \cup  Follows(⟨term1⟩)
Follows(⟨simpTerm2⟩) = Follows(⟨term1⟩)

*/

Follows_simpTerm1 = [ TOKEN.cast, TOKEN.ospar ] ++ Follows_term1;
Follows_simpTerm3 = Follows_simpTerm1;
Follows_simpTerm2 = Follows_term1;


simpTerm*(ltokens)-> 
| ltokens==null: null
| ltokens==[]: errIncomplete("simpTerm")
| :  simpTerm2(simpTerm1(fact(ltokens)));

simpTerm1*(ltokens) -> <TOKEN*>
| ltokens==null: null
| ltokens==[]: errIncomplete("simpTerm1")
| isCurrToken(ltokens, opar): 
   simpTerm3(acceptToken(actPars(ltokens[>]),cpar))  
| isTokenIn(ltokens[.],Follows_simpTerm1): ltokens
|: errTokenL(ltokens[.],"First\\eps(simpTerm1) U Follows(simpTerm1)",[opar] ++ Follows_simpTerm1);
   
simpTerm3*(ltokens) -> <TOKEN*>
| ltokens==null: null
| ltokens==[]: errIncomplete("simpTerm3")
| isCurrToken(ltokens, opar): 
   acceptToken(actPars(ltokens[>]),cpar)  
| isTokenIn(ltokens[.],Follows_simpTerm3): ltokens
|: errTokenL(ltokens[.],"First\\eps(simpTerm3) U Follows(simpTerm3)",[opar] ++ Follows_simpTerm3);

simpTerm2*(ltokens) -> <TOKEN*>
| ltokens==null: null
| ltokens==[]: errIncomplete("simpTerm2")
| isCurrToken(ltokens, cast): 
  simpTerm2(acceptToken(acceptToken(ltokens,cast),TYPE))  
| isCurrToken(ltokens, ospar): 
    simpTerm2(acceptToken(sliceElems(ltokens[>]),cspar))  
| isTokenIn(ltokens[.],Follows_simpTerm2): ltokens
|: errTokenL(ltokens[.],"First\\eps(simpTerm2) U Follows(simpTerm2)", [cast,ospar] ++ Follows_simpTerm2);

PARSER: actPars1*/1;

/*
RULE
⟨actPars⟩ ::= [ ⟨ifExp⟩ { ‘,’ ⟨ifExp⟩ } ]

REWRITTEN RULE
⟨actPars⟩ ::= ⟨ifExp⟩ ⟨actPars1⟩ | epsilon
⟨actPars1⟩ ::= ',' ⟨ifExp⟩ ⟨actPars1⟩ | epsilon

First(⟨actPars⟩) = First(⟨ifExp⟩) \cup { epsilon }
Follows(⟨actPars⟩) = { ')' }
First(⟨actPars1⟩) = { ',', epsilon } 
Follows(⟨actPars1⟩) = { ')' }

*/

actPars*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("actPars")
| isCurrToken(ltokens, cpar): ltokens
| isTokenIn(ltokens[.],First_ifExp): 
	/* First_ifExp previously checked */ 
	actPars1(ifExp(ltokens,true)) 
| : errTokenL(ltokens[.],"First\\eps(actPars) U Follows(actPars)",[cpar] ++ First_ifExp);

actPars1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("actPars1")
| isCurrToken(ltokens, cpar): ltokens
| isCurrToken(ltokens,comma): 
	/* First_ifExp to be checked */ 
	actPars1(ifExp(ltokens[>],false)) 
| : errTokenL(ltokens[.],"First\\eps(actPars1) U Follows(actPars1)",[cpar,comma]);

PARSER: First_sliceElems, sliceElems1*/1, sliceElems2*/1;

/*
RULE
⟨sliceElems⟩ ::= ‘.’ | ‘>’ [ ⟨ifExp⟩ ] | ⟨ifExp⟩ [ ‘:’ [ ⟨ifExp⟩ ] ] | ‘:’ [ ⟨ifExp⟩ ]

REWRITTEN RULE
⟨sliceElems⟩ ::= ‘.’ | ‘>’ ⟨sliceElems1⟩ | ⟨ifExp⟩ ⟨sliceElems2⟩ | ‘:’ ⟨sliceElems1⟩
⟨sliceElems1⟩ ::= ⟨ifExp⟩ | epsilon
⟨sliceElems2⟩ ::= ‘:’ ⟨sliceElems1⟩ | epsilon

First(⟨sliceElems⟩) = { ‘.’, ‘>’, ‘:’ } \cup First(⟨ifExp⟩)
First(⟨sliceElems1⟩) = { epsilon } \cup First(⟨ifExp⟩)
First(⟨sliceElems2⟩) = { ‘:’, epsilon }
Follows(⟨sliceElems1⟩) = Follows(⟨sliceElems2⟩) = { ']' }

*/

sliceElems*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("sliceElems")
| isCurrToken(ltokens, dot): ltokens[>]
| isCurrToken(ltokens, gt): sliceElems1(ltokens[>])
| isTokenIn(ltokens[.],First_ifExp): sliceElems2(ifExp(ltokens,true))
| isCurrToken(ltokens, colon): sliceElems1(ltokens[>])
| :  errTokenL(ltokens[.],"First(sliceElems)",First_sliceElems);

sliceElems1*(ltokens) -> <TOKEN*> 
| ltokens==[]:  errIncomplete("sliceElems1")
| isTokenIn(ltokens[.],First_ifExp): ifExp(ltokens,true) 
| isCurrToken(ltokens, cspar): ltokens 
| :  errTokenL(ltokens[.],"First\\eps(sliceElems1) U Follows(sliceElems1)", [cspar]++First_ifExp);

sliceElems2*(ltokens) -> <TOKEN*> 
| ltokens==[]:  errIncomplete("sliceElems2")
| isCurrToken(ltokens, colon): sliceElems1(ltokens[>])
| isCurrToken(ltokens, cspar): ltokens 
| :  errTokenL(ltokens[.],"First\\eps(sliceElems2) U Follows(sliceElems2)",[colon,cspar]);


PARSER: First_fact, First_fact_A, First_fact_B, First_callBuiltIn, First_constListElems, First_jsonFields, fact1*/1, fact2*/1, constListElems*/1, jsonFields*/1, callBuiltIn*/1, lambda*/1;

/*
RULE
⟨fact⟩ ::= 	DOUBLE | LONG | INT | CHAR | BOOL | STRING | TYPE | null | ( ID | IDL ) |
			‘(’ ⟨ifExp⟩ ‘)’ | ‘[’ [ ⟨constListElems⟩ ] ‘]’ | ‘{’ [ ⟨jsonFields⟩ ] ‘}’ |
			⟨callBuiltIn⟩ | ⟨lambda⟩

REWRITTEN RULE:
⟨fact⟩ ::= 	DOUBLE | LONG | INT | CHAR | BOOL | STRING | TYPE | null | ( ID | IDL ) |
			‘(’ ⟨ifExp⟩ ‘)’ | ‘[’ ⟨fact1⟩ ‘]’ | ‘{’ ⟨fact2⟩ ‘}’ |
			⟨callBuiltIn⟩ | ⟨lambda⟩
⟨fact1⟩ ::= ⟨constListElems⟩ | epsilon
⟨fact2⟩ ::= ⟨jsonFields⟩ | epsilon


First_fact_A = { DOUBLE, INT, LONG, STRING, CHAR, BOOL, 'null', TYPE, ID, IDL } 
First_fact_B = { ‘(’, ‘[’, ‘{’, '\' }
First(⟨callBuiltIn⟩) = { "_exp", "_log", "_pow", "_len", "_rand", "_ind", "_tuple", "_isKey", "_pDate", "_gDate" }
First(⟨fact⟩) = First_fact_A \cup First_fact_B \cup First(⟨callBuiltIn⟩)
First(⟨fact1⟩) = First(⟨constListElems⟩) 
First(⟨fact2⟩) = First(⟨jsonFields⟩) 
Follows(⟨fact1⟩) = {']'} 
Follows(⟨fact2⟩) = {'}'} 


*/

:TOKEN;
PARSER.First_fact_A = [ DOUBLE, INT, LONG, STRING, CHAR, BOOL, null_t, TYPE, ID, IDL];
PARSER.First_fact_B = [ opar, ospar, obrace, lambda ];
:PARSER;

First_callBuiltIn = LEX.Builtins;

First_fact= First_fact_A[:] ++ First_fact_B ++ First_callBuiltIn;

fact*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("fact")
| isTokenIn(ltokens[.],First_fact_A): ltokens[>]
| isCurrToken(ltokens, opar): /* First_ifExp to be checked */
         acceptToken(ifExp(ltokens[>],false), cpar)  
| isCurrToken(ltokens, ospar): acceptToken(fact1(ltokens[>]), cspar) 
| isCurrToken(ltokens, obrace): acceptToken(fact2(ltokens[>]), cbrace) 
| isTokenIn(ltokens[.],First_callBuiltIn): callBuiltIn(ltokens)
| isCurrToken(ltokens, TOKEN.lambda): lambda(ltokens)
| : errTokenL(ltokens[.],"First(fact)",First_fact);
fact1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("fact1")
| isTokenIn(ltokens[.],First_constListElems): constListElems(ltokens)
| isCurrToken(ltokens, cspar): ltokens
| : errTokenL(ltokens[.],"First\\eps(fact1) U Follows(fact1)", [cspar]++First_constListElems);
fact2*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("fact2")
| isTokenIn(ltokens[.],First_jsonFields): jsonFields(ltokens)
| isCurrToken(ltokens, cbrace): ltokens
| : errTokenL(ltokens[.],"First\\eps(fact2) U Follows(fact2)", [cbrace]++First_jsonFields);

First_simpTerm= First_fact;
First_uniTerm = [ TOKEN.plus, TOKEN.minus, TOKEN.emark] ++ First_fact;

First_ifExp = First_uniTerm;
First_sliceElems= [ TOKEN.dot, TOKEN.gt, TOKEN.colon ] ++ First_ifExp;
First_query1 = First_ifExp;
First_andExp= First_uniTerm;
First_eqExp= First_uniTerm;
First_relExp= First_uniTerm;
First_expr= First_uniTerm;
First_term= First_uniTerm;

PARSER: constListElems1*/1, constListElems2*/1;

/*
RULE
⟨constListElems⟩ ::= ⟨ifExp⟩ { ‘,’ ⟨ifExp⟩ } [ ‘|’ ⟨ifExp⟩ ]


REWRITTEN RULE:
⟨constListElems⟩ ::= ⟨ifExp⟩ ⟨constListElems1⟩ ⟨constListElems2⟩
⟨constListElems1⟩ ::= ‘,’ ⟨ifExp⟩ ⟨constListElems1⟩ | epsilon
⟨constListElems2⟩ ::= ‘|’ ⟨ifExp⟩ | epsilon

First(⟨constListElems⟩) = First(⟨ifExp⟩)
First(⟨constListElems1⟩) = { ',', epsilon }
First(⟨constListElems2⟩) = { '|', epsilon }
Follows(⟨constListElems1⟩) = { '|', ']' }
Follows(⟨constListElems2⟩) = { ']' }

*/

First_constListElems = First_ifExp;

constListElems*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("constListElems")
| : constListElems2(constListElems1(ifExp(ltokens, true)));  

constListElems1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("constListElems1")
| isCurrToken(ltokens, comma): constListElems1(ifExp(ltokens[>],false))
| isTokenIn(ltokens[.], [pipe, cspar]): ltokens
| : errTokenL(ltokens[.],"First\\eps(constListElems1) U Follows(constListElems1)", [comma,pipe,cspar]);

constListElems2*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("constListElems2")
| isCurrToken(ltokens, pipe): ifExp(ltokens[>],false)
| isCurrToken(ltokens, cspar): ltokens
| : errTokenL(ltokens[.],"First\\eps(constListElems2) U Follows(constListElems2)", [pipe,cspar]);

PARSER: jsonFields1*/1;
/*
RULE
⟨jsonFields⟩ ::= ⟨ifExp⟩ ‘:’ ⟨ifExp⟩ { ‘,’ ⟨ifExp⟩ ‘:’ ⟨ifExp⟩ }


REWRITTEN RULE:
⟨jsonFields⟩ ::= ⟨ifExp⟩ ‘:’ ⟨ifExp⟩ ⟨jsonFields1⟩
⟨jsonFields1⟩ ::= ‘,’ ⟨ifExp⟩ ‘:’ ⟨ifExp⟩ ⟨jsonFields1⟩ | epsilon

First(⟨jsonFields⟩) = First(⟨ifExp⟩)
First(⟨jsonFields1⟩) = { ',', epsilon }
Follows(⟨jsonFields1⟩) = { '}' }

*/
First_jsonFields = First_ifExp;

jsonFields*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("jsonFields")
| : jsonFields1(ifExp(acceptToken(ifExp(ltokens,true),colon),false));

jsonFields1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("jsonFields1")
| isCurrToken(ltokens, comma): 
    jsonFields1(ifExp(acceptToken(ifExp(ltokens[>],false),colon),false))
| isCurrToken(ltokens, cbrace): ltokens
| : errTokenL(ltokens[.],"First\\eps(jsonFields1) U Follows(jsonFields1)", [comma,cbrace]);

PARSER: callBuiltIn1*/1, callBuiltIn2*/1;

/*
RULE
⟨callBuiltIn⟩ ::= ‘_rand’ ‘(’ ‘)’ | ('_exp’|‘_log’|‘_len’|‘_tuple’)‘(’⟨ifExp⟩‘)’
                 | (‘_pow’|‘_isKey’) ‘(’ ⟨ifExp⟩ ‘,’ ⟨ifExp⟩ ‘)’ 
                 | ‘_ind’ ‘(’ ⟨ifExp⟩ ‘,’ ⟨ifExp⟩ [‘,’ ⟨ifExp⟩ ] ‘)’ 
                 | ‘_gDate’ ‘(’ [ ⟨ifExp⟩ ] ‘)’
                 | ‘_pDate’ ‘(’ ⟨ifExp⟩ [ ‘,’ ⟨ifExp⟩ ] ‘)’

REWRITTEN RULE:
⟨callBuiltIn⟩ ::= ‘_rand’ ‘(’ ‘)’ | ('_exp’|‘_log’|‘_len’|‘_tuple’)‘(’⟨ifExp⟩‘)’
                 | (‘_pow’|‘_isKey’) ‘(’ ⟨ifExp⟩ ‘,’ ⟨ifExp⟩ ‘)’ 
                 | ‘_ind’ ‘(’ ⟨ifExp⟩ ‘,’ ⟨ifExp⟩ ⟨callBuiltIn1⟩ ‘)’ 
                 | ‘_gDate’ ‘(’ ⟨callBuiltIn2⟩ ‘)’
                 | ‘_pDate’ ‘(’ ⟨ifExp⟩ ⟨callBuiltIn1⟩ ‘)’
⟨callBuiltIn1⟩ ::= ‘,’ ⟨ifExp⟩ | epsilon
⟨callBuiltIn2⟩ ::= ⟨ifExp⟩ | epsilon

First(⟨callBuiltIn⟩) = { "_exp", "_log", "_pow", "_len", "_rand", "_ind", "_tuple", "_isKey", "_pDate", "_gDate" }
First(⟨callBuiltIn1⟩) = { ',', epsilon }
First(⟨callBuiltIn2⟩) = First(⟨ifExp⟩) \cup { epsilon }
Follows(⟨callBuiltIn1⟩) = { ')' }
Follows(⟨callBuiltIn2⟩) = { ')' }

*/

callBuiltIn*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("callBuiltIn")
| isCurrToken(ltokens, rand):acceptToken(acceptToken(ltokens[>],opar),cpar)
| isTokenIn(ltokens[.], [exp,log,len,tuple]):
    acceptToken(ifExp(acceptToken(ltokens[>],opar),false),cpar) 
| isTokenIn(ltokens[.], [pow,isKey]):
    {! ltokens= ifExp(acceptToken(ltokens[>],opar),false) !}
    acceptToken(ifExp(acceptToken(ltokens,comma),false),cpar) 
| isCurrToken(ltokens, ind) :
    {! ltokens= ifExp(acceptToken(ltokens[>],opar),false) !}
    {! ltokens= ifExp(acceptToken(ltokens,comma),false) !}
    acceptToken(callBuiltIn1(ltokens),cpar) 
| isCurrToken(ltokens, gDate) :
    acceptToken(callBuiltIn2(acceptToken(ltokens[>],opar)),cpar)
| : /* remaining case: _pDate, since membership in First_callBuiltIn was already checked */
    {! ltokens=ifExp(acceptToken(ltokens[>],opar),false) !} 
    acceptToken(callBuiltIn1(ltokens),cpar);

callBuiltIn1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("callBuiltIn1")
| isCurrToken(ltokens,comma): ifExp(ltokens[>],false)
| isCurrToken(ltokens,cpar): ltokens
| : errTokenL(ltokens[.],"First\\eps(callBuiltIn1) U Follows(callBuiltIn1)", [comma,cpar]);

callBuiltIn2*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("callBuiltIn2")
| isTokenIn(ltokens[.],First_ifExp): ifExp(ltokens,true)
| isCurrToken(ltokens,cpar): ltokens
| : errTokenL(ltokens[.],"First\\eps(callBuiltIn2) U Follows(callBuiltIn2)", [cpar]++First_ifExp);

PARSER: lambda1*/1, lambda2*/1;

/*
RULE
⟨lambda⟩ ::= ‘\’ ‘(’ [ ID { ‘,’ ID }  ] ‘)’ ‘->’ ⟨ifExp⟩

REWRITTEN RULE:
⟨lambda⟩ ::= ‘\’ ‘(’ ⟨lambda1⟩ ‘)’ ‘->’ ⟨ifExp⟩
⟨lambda1⟩ ::= ID ⟨lambda2⟩ | epsilon
⟨lambda2⟩ ::= ‘,’ ID ⟨lambda2⟩ | epsilon

First(⟨lambda⟩) = { '\' }
First(⟨lambda1⟩) = { ID, epsilon }
First(⟨lambda2⟩) = { ',', epsilon }
Follows(⟨lambda1⟩) = { ')' }
Follows(⟨lambda2⟩) = { ')' }

*/
lambda*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("lambda")
| : {! ltokens=lambda1(acceptToken(acceptToken(ltokens, TOKEN.lambda),opar)) !}
    ifExp(acceptToken(acceptToken(ltokens, cpar),map),false); 

lambda1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("lambda1")
| isCurrToken(ltokens,ID): lambda2(ltokens[>])
| isCurrToken(ltokens,cpar): ltokens	
| : errTokenL(ltokens[.],"First\\eps(lambda1) U Follows(lambda1)", [ID,cpar]);

lambda2*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("lambda2")
| isCurrToken(ltokens,comma): lambda2(acceptToken(ltokens[>],ID))
| isCurrToken(ltokens,cpar): ltokens
| : errTokenL(ltokens[.],"First\\eps(lambda2) U Follows(lambda2)", [comma,cpar]);

PARSER: First_varFunctLabDef, First_varFunctDef, labDef*/1;

/*
RULE

⟨varFuncLabDef⟩ ::= ⟨varFuncDef⟩ | ‘:’ ⟨labDef⟩

REWRITTEN RULE:
as above

First(⟨varFuncLabDef⟩) = First(⟨varFuncDef⟩) \cup { ':' }

*/

varFuncLabDef*(ltokens) -> <TOKEN*> 
| ltokens==[]: errIncomplete("varFuncLabDef")
| isTokenIn(ltokens[.],First_varFunctDef): varFuncDef(ltokens)
| isCurrToken(ltokens, colon): labDef(ltokens[>])
| : errTokenL(ltokens[.],"First(varFuncLabDef)",First_varFunctLabDef);

PARSER: First_varDef, varDef*/1, funcDef*/1;

/*
RULE

⟨varFuncDef⟩ ::= ⟨varDef⟩ | [ ‘*’ ] ⟨funcDef⟩

REWRITTEN RULE:
⟨varFuncDef⟩ ::= ⟨varDef⟩ |  ‘*’ ⟨funcDef⟩ | ⟨funcDef⟩

First(⟨varFuncDef⟩) = First(⟨varDef⟩) \cup { '*' } \cup First(⟨funcDef⟩) = 
First(⟨varDef⟩) \cup { '*', ')' } 
since First(⟨funcDef⟩) = { '(' }

*/

varFuncDef*(ltokens) -> <TOKEN*> 
| ltokens==[]: errIncomplete("varFuncDef")
| isTokenIn(ltokens[.],First_varDef): varDef(ltokens)
| isCurrToken(ltokens, star): funcDef(ltokens[>])
| isCurrToken(ltokens, opar): funcDef(ltokens)
| : errTokenL(ltokens[.],"First(varFuncDef)",First_varFunctDef);

PARSER: varDef1*/1, in*/1;

/*
RULE

⟨varDef⟩ ::= { ‘[’ ⟨ifExp⟩ ‘]’ } ( ‘=’ ( ⟨ifExp⟩ | ⟨in⟩ | ‘#’ null) | OP_ASS ⟨ifExp⟩ )

REWRITTEN RULE:
⟨varDef⟩ ::= ‘[’ ⟨ifExp⟩ ‘]’ ⟨varDef⟩ | ‘=’ ⟨varDef1⟩ | OP_ASS ⟨ifExp⟩ 
⟨varDef1⟩ ::= ⟨ifExp⟩ | ⟨in⟩ | ‘#’ null

First(⟨varDef⟩) = { '[', '=', OP_ASS }  
First(⟨varDef1⟩) = First(⟨ifExp⟩) \cup First(⟨in⟩) \cup { '#' }  = First(⟨ifExp⟩) \cup { '<<', '#' }
since First(⟨in⟩) = { '<<' }

*/
First_varDef = [ TOKEN.ospar, TOKEN.assign, TOKEN.OP_ASS ];
First_varFunctDef = [ TOKEN.star, TOKEN.opar ] ++ First_varDef;
First_varFunctLabDef = [ TOKEN.colon ] ++ First_varFunctDef;

varDef*(ltokens) -> <TOKEN*> 
| ltokens==[]: errIncomplete("varDef")
| isCurrToken(ltokens, ospar):  varDef(acceptToken(ifExp(ltokens[>],false),cspar)) 
| isCurrToken(ltokens, assign): varDef1(ltokens[>])
| isCurrToken(ltokens, OP_ASS): ifExp(ltokens[>],false) 
| : errTokenL(ltokens[.],"First(varDef)",First_varDef);

varDef1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("varDef1")
| isTokenIn(ltokens[.],First_ifExp): ifExp(ltokens,true) 
| isCurrToken(ltokens, ins): in(ltokens)
| isCurrToken(ltokens, hash): acceptToken(ltokens[>],null_t)
| : errTokenL(ltokens[.],"First(varDef1)",[ins,hash] ++ First_ifExp);

PARSER: in1*/1;

/*
RULE

⟨in⟩ ::= ‘«’ ‘(’ STRING { ‘,’ STRING } ‘)’

REWRITTEN RULE:
⟨in⟩ ::= ‘«’ ‘(’ STRING ⟨in1⟩ ‘)’
⟨in1⟩ ::= ‘,’ STRING | epsilon

First(⟨in⟩) = { '<<' }  
First(⟨in1⟩) = { ',', epsilon }
Follows(⟨in1⟩) = { ')' }  

*/
in*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("in")
| : /* First(in) already checked */
   acceptToken(in1(acceptToken(acceptToken(acceptToken(ltokens,ins),opar),STRING)),cpar);

in1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("in1")
| isCurrToken(ltokens,comma): in1( ifExp(ltokens[>],false) ) 
| isCurrToken(ltokens,cpar): ltokens 
| : errTokenL(ltokens[.],"First\\eps(in1) U Follows(in1)", [comma,cpar]);

PARSER:  labDef1*/1, labFuncDef*/1, labFuncDef1*/1, labFuncDef2*/1, labFuncDef3*/1;

/*
RULE

⟨labDef⟩ ::= ID [ ⟨labFuncDef⟩ ] { ‘,’ ID [ ⟨labFuncDef⟩ ] }
⟨labFuncDef⟩::= [‘*’] ( ‘/’INT |‘(’ [ (‘_’ | ID ) [ ‘/’ INT ] { ‘,’ (‘_’ | ID ) [ ‘/’ INT ] } ] ‘)’ [ ‘/’ INT ] )

REWRITTEN RULE:

⟨labDef⟩ ::= ID ⟨labFuncDef⟩ ⟨labDef1⟩ 
⟨labDef1⟩ ::=  ‘,’ ⟨labDef⟩  | epsilon
⟨labFuncDef⟩::= ‘*’ ⟨labFuncDef1⟩ | ⟨labFuncDef1⟩ | epsilon
⟨labFuncDef1⟩::= ‘/’ INT | ‘(’ ⟨labFuncDef2⟩ ‘)’ [ ‘/’ INT ]
⟨labFuncDef2⟩::= ( ‘_’ | ID ) [ ‘/’ INT ] ⟨labFuncDef3⟩ | epsilon
⟨labFuncDef3⟩::= ‘,’ ⟨labFuncDef2⟩ | epsilon

First(⟨labDef⟩) = { ID }  
First(⟨labDef1⟩) = { ',', epsilon }
First(⟨labFuncDef⟩) = { '*', epsilon } U First(⟨labFuncDef1⟩) = { '*', '/', '(', epsilon }
First(⟨labFuncDef1⟩) = { '/', '(' } 
First(⟨labFuncDef2⟩) = { '_', ID, epsilon } 
First(⟨labFuncDef3⟩) = { ',', epsilon } 

Follows(⟨labDef1⟩) = { ';' }  
Follows(⟨labFuncDef⟩) = { ',', ';' }  
Follows(⟨labFuncDef2⟩) = { ',', ')' }  
Follows(⟨labFuncDef3⟩) = { ')' }  
*/
labDef*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("labDef")
| : labDef1(labFuncDef(acceptToken(ltokens,ID))); 

labDef1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("labDef1")
| isCurrToken(ltokens, comma): labDef(ltokens[>])
| isCurrToken(ltokens, semicolon): ltokens
| : errTokenL(ltokens[.],"First\\eps(labDef1) U Follows(labDef1)", [comma,semicolon]);
   
labFuncDef*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("labFuncDef")
| isCurrToken(ltokens, star): labFuncDef1(ltokens[>])
| isTokenIn(ltokens[.],[div,opar]): labFuncDef1(ltokens)
| isTokenIn(ltokens[.],[comma,semicolon]): ltokens
| : errTokenL(ltokens[.],"First\\eps(labFuncDef) U Follows(labFuncDef)", [star, div,opar,comma,semicolon]);

labFuncDef1*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("labFuncDef1")
| isCurrToken(ltokens, div): acceptToken(ltokens[>],INT)
| : /* current token already checked to be opar */ 
  {! ltokens= acceptToken(labFuncDef2(ltokens[>]),cpar) !}
  isCurrToken(ltokens, div)? acceptToken(ltokens[>],INT): ltokens;

labFuncDef2*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("labfuncDef2")
| isTokenIn(ltokens[.],[underscore,ID] ): {! ltokens=ltokens[>]!}
  labFuncDef3(isCurrToken(ltokens,div)? acceptToken(ltokens[>],INT): ltokens)
| isTokenIn(ltokens[.],[comma,cpar]): ltokens
| : errTokenL(ltokens[.],"First\\eps(labFuncDef2) U Follows(labFuncDef2)", [underscore, ID,comma,cpar]);

labFuncDef3*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("labFuncDef3")
| isCurrToken(ltokens, comma): labFuncDef2(ltokens[>])
| isCurrToken(ltokens, cpar): ltokens
| : errTokenL(ltokens[.],"First\\eps(labFuncDef3) U Follows(labFuncDef3)", [comma,cpar]);


PARSER: First_fBody, funcDef1*/1, funcDef2*/1, formPar*/1, formPars*/1, decl*/1, decls*/1, fBody*/1;

/*
RULE

⟨funcDef⟩ ::= ‘(’ [ ⟨formPar⟩ { ‘, ’ ⟨formPar⟩ } ] ‘)’ [ ‘/’ INT ] ‘->’ [ ‘<’ID [‘*’] { ‘,’ ID [‘*’] } '>’ ] ⟨fBody⟩
⟨formPar⟩ ::= ( ID | ‘_’ ) [ ‘/’ INT ]

REWRITTEN RULE:
⟨funcDef⟩ ::= ‘(’ ⟨funcDef1⟩ ‘)’ [ ‘/’ INT ] ‘->’ ⟨funcDef2⟩ ⟨fBody⟩
⟨funcDef1⟩ ::= ⟨formPar⟩ ⟨formPars⟩ | epsilon
⟨formPars⟩ ::= ‘,’ ⟨formPar⟩ ⟨formPars⟩ | epsilon
⟨formPar⟩ ::= ( ID | ‘_’ ) [ ‘/’ INT ]
⟨funcDef2⟩ ::= ‘<’ ⟨decl⟩ ⟨decls⟩ '>' | epsilon
⟨decl⟩ ::= ID ['*']
⟨decls⟩ ::= ',' ⟨decl⟩ ⟨decls⟩ | epsilon

First(⟨funcDef⟩) = { '(' }  
First(⟨funcDef1⟩) = First(⟨formPar⟩) \cup {epsilon} = { ID, '_', epsilon }
First(⟨formPars⟩) = { ',', epsilon }  
First(⟨formPar⟩) = { ID, '_' }  
First(⟨funcDef2⟩) = { '<', epsilon }  
First(⟨decl⟩) = { ID }
First(⟨decls⟩) = { ',', epsilon }
Follows(⟨funcDef1⟩) = { ')' }  
Follows(⟨formPars⟩) = { ')' }  
Follows(⟨funcDef2⟩) = First(⟨fBody⟩)  
Follows(⟨decls⟩) = { '>' }  

*/

funcDef*(ltokens) -> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("funcDef")
| : {! ltokens= acceptToken(funcDef1(acceptToken(ltokens,opar)),cpar) !}
    {! ltokens= isCurrToken(ltokens, div)? acceptToken(ltokens[>],INT): ltokens !}
    fBody(funcDef2(acceptToken(ltokens,map)));

funcDef1*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("funcDef1")
| isTokenIn(ltokens[.],[ID,underscore]): formPars(formPar(ltokens))
| isCurrToken(ltokens,cpar): ltokens
| : errTokenL(ltokens[.],"First\\eps(funcDef1) U Follows(funcDef1)", [ID,underscore,cpar]);

formPars*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("formPars")
| isCurrToken(ltokens,comma): formPars(formPar(ltokens[>]))
| isCurrToken(ltokens,cpar): ltokens
| : errTokenL(ltokens[.],"First\\eps(formPars) U Follows(formPars)", [comma,cpar]);

formPar*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("formPar")
| isTokenIn(ltokens[.],[ID,underscore]):
		isCurrToken(ltokens[>], div)? acceptToken(ltokens[>1],INT): ltokens[>] 
| : errTokenL(ltokens[.],"First(formPar)", [ID,underscore]);

funcDef2*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("funcDef2")
| isCurrToken(ltokens,lt): 
        acceptToken(decls(decl(ltokens[>])),gt)
| isTokenIn(ltokens[.],First_fBody): ltokens
| : errTokenL(ltokens[.],"First\\eps(funcDef2) U Follows(funcDef2)", [lt]++First_fBody);

decl*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("decl")
| isCurrToken(ltokens,ID): 
		isCurrToken(ltokens[>], star)? ltokens[>1]: ltokens[>] 
| : errTokenL(ltokens[.],"First(decl)", [ID]);

decls*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("decls")
| isCurrToken(ltokens,comma): decls(decl(ltokens[>]))
| isCurrToken(ltokens,gt): ltokens
| : errTokenL(ltokens[.],"First\\eps(decls) U Follows(decls)", [comma,gt]);
 
PARSER: Follows_fBody1, Follows_repSet, First_guard, fBody1*/1, fBody2*/1, repSet*/1, guard*/1, set*/1;
/*
RULES

⟨fBody⟩ ::= { ⟨set⟩ } [ ‘|’ ⟨guard⟩ ‘|’ { ⟨guard⟩ ‘|’ } ‘:’ ] { ⟨set⟩ } ⟨ifExp⟩ { ⟨set⟩ }
⟨guard⟩ ::= { ⟨set⟩ } ⟨ifExp⟩ ‘:’ { ⟨set⟩ } ⟨ifExp⟩ { ⟨set⟩ }

REWRITTEN RULES:
⟨fBody⟩ ::= ⟨repSet⟩ ⟨fBody1⟩ ⟨repSet⟩ ⟨ifExp⟩ ⟨repSet⟩
⟨fBody1⟩ ::= ‘|’ ⟨guard⟩ ‘|’ ⟨fbody2⟩ ‘:’ | epsilon
⟨repSet⟩ ::= ⟨set⟩ ⟨repSet⟩ | epsilon
⟨fBody2⟩ ::= ⟨guard⟩ ‘|’ ⟨fBody2⟩ | epsilon
⟨guard⟩ ::= ⟨repSet⟩ ⟨ifExp⟩ ‘:’ ⟨repSet⟩ ⟨ifExp⟩ ⟨repSet⟩

First(⟨fBody⟩) = (First(⟨repSet⟩) \ {epsilon }) \cup { '|' } \cup First(⟨ifExp⟩) = { '{!', '{!', '|' } \cup First(⟨ifExp⟩)
First(⟨fBody1⟩) = { '|', epsilon }
First(⟨repSet⟩) = First(⟨set⟩) \cup { epsilon } = { '{!',  epsilon }
First(⟨fBody2⟩) = First(⟨guard⟩) \cup { epsilon }
First(⟨guard⟩) = (First(⟨repSet⟩) \ {epsilon }) \cup First(⟨ifExp⟩) = { '{!' } \cup First(⟨ifExp⟩)
Follows(⟨fBody1⟩) = { '{!' } \cup First(⟨ifExp⟩)  
Follows(⟨repSet⟩) = { '|', ";" } \cup First(⟨ifExp⟩)  
Follows(⟨fBody2⟩) = { ':' }  

*/
First_fBody = [ TOKEN.pipe, TOKEN.obraceset ] ++ First_ifExp;
First_guard = [ TOKEN.obraceset ] ++ First_ifExp;
Follows_fBody1 = First_guard;
Follows_repSet = [ TOKEN.pipe, TOKEN.semicolon ] ++ First_ifExp;

fBody*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("fBody")
| isTokenIn(ltokens[.],First_fBody):
     repSet(ifExp(repSet(fBody1(repSet(ltokens))),false))
| : errTokenL(ltokens[.],"First(fBody)", First_fBody);

fBody1*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("fBody1")
| isCurrToken(ltokens,pipe): 
     acceptToken(fBody2(acceptToken(guard(ltokens[>]),pipe)),colon)
| isTokenIn(ltokens[.],Follows_fBody1): ltokens
| : errTokenL(ltokens[.],"First\\eps(fBody1) U Follows(fBody1)", [pipe]++Follows_fBody1);

repSet*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("repSet")
| isCurrToken(ltokens,obraceset): repSet(set(ltokens))
| isTokenIn(ltokens[.],Follows_repSet): ltokens
| : errTokenL(ltokens[.],"First\\eps(repSet) U Follows(repSet)", [obraceset] ++ Follows_repSet);

fBody2*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("fBody2")
| isTokenIn(ltokens[.],First_guard): 
     fBody2(acceptToken(guard(ltokens),pipe))
| isCurrToken(ltokens,colon): ltokens
| : errTokenL(ltokens[.],"First\\eps(fBody2) U Follows(fBody2)", [colon]++First_guard);

guard*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("guard")
| : /* First(guard) already checked */
   repSet(ifExp(repSet(acceptToken(ifExp(repSet(ltokens),false),colon)),false));

PARSER: First_prSet, First_ioSet, set1*/1, set2*/1, prSet*/1, ioSet*/1;
/*
RULE
⟨set⟩ ::= ‘{!’ ( ⟨prSet⟩ | ⟨ioSet⟩ ) { ‘&’ ( ⟨prSet⟩ | ⟨ioSet⟩ ) } ‘!}’  
REWRITTEN RULE:
⟨set⟩ ::= ‘{!’ ⟨set1⟩ ⟨set2⟩ ‘!}’ 
⟨set1⟩ ::=  ⟨prSet⟩ | ⟨ioSet⟩
⟨set2⟩ ::= ‘&’ ( ⟨prSet⟩ | ⟨ioSet⟩ )  ⟨set2⟩ | epsilon

First(⟨set⟩) = { '{!' } 
First(⟨set1⟩) = First(⟨prSet⟩) \cup First(⟨ioSet⟩)
First(⟨set2⟩) = { '&', epsilon }
Follows(⟨set2⟩) = { '!}' }  

*/

set*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("set")
| : /* current token is obraceset since First(set) already checked */
    acceptToken(set2(set1(ltokens[>])),cbraceset);

set1*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("set1")
| isTokenIn(ltokens[.],First_prSet): prSet(ltokens)
| isTokenIn(ltokens[.],First_ioSet): ioSet(ltokens)
| : errTokenL(ltokens[.],"First(set1)", First_prSet[:]++First_ioSet);

set2*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("set2")
| isCurrToken(ltokens,continue): set2(set1(ltokens[>]))
| isCurrToken(ltokens,cbraceset): ltokens
| : errTokenL(ltokens[.],"First\\eps(set2) U Follows(set2)", [continue,cbraceset]);

PARSER:  prSet1*/1, prSet2*/1, prSet3*/1;
/*
RULE
⟨prSet⟩ ::= ( ID | IDL ) { ‘[’ ⟨ifExp⟩ ‘]’ } ( OP_ASS ⟨ifExp⟩ | ‘=’ ( ⟨ifExp⟩ |	‘#’null ) )
| ‘[’ ⟨ifExp⟩ ‘]’ |  exc ‘(’ ⟨ifExp⟩ ‘, ’ ⟨ifExp⟩ ‘)’
REWRITTEN RULE:
⟨prSet⟩ ::= ( ID | IDL ) ⟨prSet1⟩ ⟨prSet2⟩ | ‘[’ ⟨ifExp⟩ ‘]’ |  exc ‘(’ ⟨ifExp⟩ ‘, ’ ⟨ifExp⟩ ‘)’
⟨prSet1⟩ ::= ‘[’ ⟨ifExp⟩ ‘]’ ⟨prSet1⟩ | epsilon
⟨prSet2⟩ ::= OP_ASS ⟨ifExp⟩ | ‘=’ ⟨prSet3⟩
⟨prSet3⟩ ::=  ⟨ifExp⟩ |	‘#’null 

First(⟨prSet⟩) = { ID, IDL, '[', exc } 
First(⟨prSet1⟩) = { '[', epsilon } 
First(⟨prSet2⟩) = { OP_ASS, ‘=’ } 
First(⟨prSet3⟩) = First(⟨ifExp⟩) \cup { ‘#’ } 
Follows(⟨prSet1⟩) = First(⟨prSet2⟩) = { OP_ASS, ‘=’ }  

*/

First_prSet= [TOKEN.ID, TOKEN.IDL, TOKEN.ospar, TOKEN.exc_t];

prSet*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("prSet")
| isTokenIn(ltokens[.],[ID,IDL]): prSet2(prSet1(ltokens[>]))
| isCurrToken(ltokens,ospar): 
          acceptToken(ifExp(ltokens[>],false),cspar)
| isCurrToken(ltokens,exc_t): 
          acceptToken(ifExp(acceptToken(ifExp(acceptToken(ltokens[>],opar),false), 
          comma),false), cpar)
| : errTokenL(ltokens[.],"First(prSet)", First_prSet);

prSet1*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("prSet1")
| isCurrToken(ltokens,ospar): 
          prSet1(acceptToken(ifExp(ltokens[>],false),cspar))
| isTokenIn(ltokens[.],[OP_ASS,assign]): ltokens
| : errTokenL(ltokens[.],"First\\eps(prSet1) U Follows(prSet1)", [ospar,OP_ASS,assign]);

prSet2*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("prSet2")
| isCurrToken(ltokens,OP_ASS): ifExp(ltokens[>],false)
| isCurrToken(ltokens,assign): prSet3(ltokens[>])
| : errTokenL(ltokens[.],"First(prSet2)", [OP_ASS,assign]);

prSet3*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("prSet3")
| isTokenIn(ltokens[.],First_ifExp): ifExp(ltokens,false)
| isCurrToken(ltokens,hash): acceptToken(ltokens[>],null_t)
| : errTokenL(ltokens[.],"First(prSet3)", [hash]++First_ifExp);


PARSER: ioSet1*/1;
/*
RULE
⟨ioSet⟩ ::= ‘^’ ‘(’ ⟨ifExp⟩ [ PRINTOPT ] ‘)’ | ‘>’ ‘(’ ⟨ifExp⟩ ‘)’ | ‘<’ ‘(’ (ID | IDL) { ‘[’⟨ifExp⟩‘]’ } ‘)’

REWRITTEN RULE:
⟨ioSet⟩ ::= ‘^’ ‘(’ ⟨ifExp⟩ [ PRINTOPT ] ‘)’ | ‘>’ ‘(’ ⟨ifExp⟩ ‘)’ | ‘<’ ‘(’ (ID | IDL) ⟨ioSet1⟩ ‘)’
⟨ioSet1⟩ ::= ‘[’ ⟨ifExp⟩ ‘]’ ⟨ioSet1⟩ | epsilon

First(⟨ioSet⟩) = { ‘^’, ‘>’, ‘<’ } 
First(⟨ioSet1⟩) = { ‘[’, epsilon } 
Follows(⟨ioSet1⟩) = { ‘)’ }  

*/
First_ioSet= [TOKEN.print, TOKEN.gt, TOKEN.lt];

ioSet*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("ioSet")
| isCurrToken(ltokens,print): 
    {! ltokens=ifExp(acceptToken(ltokens[>],opar),true) !} 
    acceptToken(isCurrToken(ltokens,PRINTOPT)? ltokens[>]: ltokens,cpar)
| isCurrToken(ltokens,gt): 
          acceptToken(ifExp(acceptToken(ltokens[>],opar),false),cpar)
| isCurrToken(ltokens,lt): 
          acceptToken(ioSet1(acceptTokenIn(acceptToken(ltokens[>],opar),[ID,IDL])),cpar)
| : errTokenL(ltokens[.],"First(ioSet)", First_ioSet);

ioSet1*(ltokens)-> <TOKEN*> 
| ltokens==null: null
| ltokens==[]: errIncomplete("ioSet1")
| isCurrToken(ltokens,ospar): 
          ioSet1(acceptToken(ifExp(ltokens[>],false),cspar))
| isCurrToken(ltokens,cpar): ltokens
| : errTokenL(ltokens[.],"First\\eps(ioSet1) U Follows(ioSet1)", [ospar,cpar]);

^parser();
