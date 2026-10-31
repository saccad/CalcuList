/* Examples of Chapter 4 */

DIV: zero; /* label DIV for the Boolean variable zero */
div*(x, y) -> <DIV*>  
| y==0: 0 {! zero=true !} 
|: x/y {! DIV.zero=false !};

^ div(3, 0);
^ DIV.zero;
^ div(2, 3);
^ DIV.zero;
^ div(3, 0) & DIV.zero? " error: zero divide!": "";
^ div(2, 3) & DIV.zero? " error: zero divide!": "";
div*(x, y) -> <DIV*>  {! DIV.zero=false !} 
| y==0: 0 {! zero=true !} 
| : x/y;

div_exc(x,y) -> {! exc(y==0,"zeroDivide") !} x/y;
^div_exc(3,0);
^div_exc(2,3);

count*(x,C)-> {! exc(x@type!=int,"integer expected") !}
              {! C[0]=x==0?C[0]+1: C[0] & C[1]=x>0?C[1]+1: C[1] !} 
              {! C[2]=x<0?C[2]+1: C[2] !} null;
count*(x,C)-> 
| x@type!=int: {! exc(true,"integer expected") !} null
| x==0: {! C[0]+=1 !} null
| x>0:  {! C[1] +=1 !} null
|: {! C[2] +=1 !} null;
countL*(L,C)-> 
| L==[]: C
|: {! [count(L[.],C)] !} countL(L[>],C);
^countL([1,0,2,-3,4,0,-4,-5,-7],[0,0,0]);
^countL([1,true],[0,0,0]);

dept=<<("dept_unical.dat"); /* reading a json from a file */
emps=dept["emps"]; /* extract the list of employees */
addProj1Emp*(E,p) -> /* add a project to an employee E */
| E["projects"]==null: {! E["projects"]=[p] !} true
| : {! E["projects"]++=[p] !} true;
addProjEmps*(EL,p) -> /* update all employees in the list EL */
| EL==[]: true
| : {! [addProj1Emp(EL[.],p)] !} addProjEmps(EL[>],p);
^addProjEmps(emps,"p0");
^emps %>;

DIV: indeterm, div*/2;
DIV.div*(x, y) -> {! zero=false & indeterm=false!}
| y!=0: x/y
| {! zero=true !} x!=0: 0
|: {! indeterm= true !} 0;
^DIV.div(0,0);
^DIV.zero;
^DIV.indeterm;

:DIV;
^div(0, 0);
^zero;
^indeterm;
:#;
^div(2, 3);
^zero; /* error */
^DIV.zero;
^DIV.indeterm;

LEX_INT: /* label for variables/functions for recognizing integers */
		val, b10exp, digitVal/1, isDigit/1, rec*/1, rec1*/2, natRec*/3; 
:LEX_INT;
isDigit(x) -> '0'<=x && x<='9';
digitVal(x) -> x-'0';
rec*(S) -> rec1(S,_len(S));
rec1*(S,l) -> 
| l==0: false /* empty string is wrong */
| S[0]=='-': natRec(S,l,1) {!val=-val!} /*sign is changed */
| S[0]=='+': natRec(S,l,1) /* val unchanged */
| : natRec(S,l,0); /* recognition of unsigned integer */
natRec*(S,l,i) -> 
| i>=l || !isDigit(S[i]): false /* empty string or wrong digit*/
| i==l-1: true {! val=digitVal(S[i]) & b10exp=1!} /* single digit integer */
| : natRec(S,l,i+1) {!b10exp *= 10!} {!val += b10exp*digitVal(S[i])!};
^ rec("+");
^ rec("+2384");
^ val;
^ rec("-2384");
^ val;

LEX_INT2: err, natRec*/4, rec*/1, rec1*/2, printErr/1;
:LEX_INT2;
rec*(S) -> rec1(S,_len(S)); 
rec1*(S,l) -> {! err=false !}  
| l==0: 0 {!err=true!} 
| S[0]=='-': -natRec(S, 0,l,1)
| S[0]=='+': natRec(S, 0,l,1)
| : natRec(S, 0,l,0);
natRec*(S, v,l,i) -> 
| i>=l || !LEX_INT.isDigit(S[i]): 0 {!err=true!}  
| i==l-1: v*10+LEX_INT.digitVal(S[i]) 
| : natRec(S, v*10+LEX_INT.digitVal(S[i]),l,i+1);
printErr(e)-> e? " wrong integer string!": "";
^ rec("-") & printErr(err) ;
^ rec("-2384") & printErr(err);
^ rec("2384") & printErr(err);

LEX_INT_EXC: rec/1, natRec/4, rec1/2;
:LEX_INT_EXC;
natRec(S, v,l,i) ->  
{! exc(i>=l || !LEX_INT.isDigit(S[i]),"Wrong Integer String") !}
| i==l-1: v*10+LEX_INT.digitVal(S[i]) 
| : natRec(S, v*10+LEX_INT.digitVal(S[i]),l,i+1);
rec1(S,l) ->  
| {! exc(l==0,"Empty String") !}
  S[0]=='-': -natRec(S, 0,l,1)
| S[0]=='+': natRec(S, 0,l,1)
| : natRec(S, 0,l,0);
rec(S) -> rec1(S,_len(S)); 
^rec("-");
^rec("-2384");
^ans@type; /* Not cheating: it’s really an integer not a string*/

LEX_INT_LIST: natRec/4, rec/1, rec1/2, chs/1;
:LEX_INT_LIST;
chs(L) -> [-L[0],L[1]];
natRec(S, v,l,i) ->  
| i>=l || !LEX_INT.isDigit(S[i]): [0, false] 
| i==l-1: [v*10+LEX_INT.digitVal(S[i]),true] 
| : natRec(S, v*10+LEX_INT.digitVal(S[i]),l,i+1);
rec1(S,l) -> 
| l==0: [0, false] 
| S[0]=='-': chs(natRec(S, 0,l,1))
| S[0]=='+': natRec(S, 0,l,1)
| : natRec(S, 0,l,0);
rec(S) -> rec1(S,_len(S)); 
^ rec("-");
^ rec("-2384");

LEX_INT: conv*/1;
:LEX_INT;
rec*(S) -> <l> {! l=_len(S) !} 
| l==0: false /* empty string is wrong */
| S[0]=='-': natRec(S,l,1) {!val=-val!} /*sign is changed */
| S[0]=='+': natRec(S,l,1) /* val unchanged */
| : natRec(S,l,0); /* recognition of unsigned integer */
conv*(S)-> {! exc(!rec(S),"wrong integer string") !} val;
^ conv("-");
^ conv("-2384");
:#;

merge(O1, O2, ord/2) -> 
| O1==[]: O2[:]
| O2==[]: O1[:]
| ord(O1[.], O2[.]): [O1[.]|merge(O1[>], O2, ord)]
| : [O2[.]|merge(O1, O2[>], ord)];
mergeSort(L, ord/2) -> <n, n2> {! n= _len(L) & n2= n//2 !}
| n<2: L
| : merge( mergeSort(L[0:n2],ord), mergeSort(L[n2:n],ord), ord);
L = [3, 1, 7, 4, 0, 5, 13, 11, -1];
^mergeSort(L,\(x,y)->x<y); 
^L; /* L remains unchanged */

POLY2_ROOT: rootType, sqrt/1, solve*/3, printType*/0;
:POLY2_ROOT;
sqrt(X) -> _pow(X,0.5); /* square root of X computed as X^(1/2) */
printType*() -> 
| rootType==1: " * no real roots" 
| rootType==2: " * no roots" 
| rootType==3: " * one root"
| rootType==4: " * two coincident roots" 
| : ""; /* rootType=0: two distinct real roots */
solve*(a,b,c)-> <disc,rdisc,b2a> 
| a==0 && b==0: [] {!rootType=2!}
| a==0 && b!=0: [-c/b] {!rootType=3!}
| {! disc=b*b-4*a*c !}  /* discriminant */
  disc<0: [] {!rootType=1!}
| disc==0: {!b2a=-b/(2*a)!} [b2a,b2a] {!rootType=4!}
| : {! rdisc=sqrt(disc) & rootType=0 !} 
    [(-b-rdisc)/(2*a),(-b+rdisc)/(2*a)];
^ solve(0, 0, 0) & printType();
^ solve(0, 0, 1) & printType();
^ solve(2, 1, 1) & printType();
^ solve(0, -2, 1) & printType();
^ solve(1, -2, 1) & printType();
^ solve(1, 0, -4) & printType();
^ solve(2, 4, -4) & printType();
:#; /* no current label is set */

swap*(L, i, j) -> <tmp> {!tmp=L[i]!} {!L[i]=L[j]!} {!L[j]=tmp!} true;
K = [1, 2, 3, 4];
^ swap(K, 1, 3);
^ K;
^ swap(K, 2, 5); /* list index out of the range */

BUBBLE_SORT: pass*/3, iter*/2, sort*/1;
:BUBBLE_SORT;
sort*(L) -> L==[]? L: iter(L, _len(L)-2);
iter*(L, last) -> 
| last<0: L
| : {! [pass(L, 0, last)] !} iter(L, last-1);
pass*(L, i, last)-> 
| i>last: null /* end of a pass*/
| L[i]>L[i+1]: {! [swap(L, i, i+1)] !} pass(L, i+1, last)
| : pass(L, i+1, last);
^ sort(L);
!clops; /* 47222 */

pass*(L, i, last) -> 
| i>last: null
| L[0]>L[1]: {! [swap(L, 0, 1)] !} pass(L[>], i+1, last)
| : pass(L[>], i+1, last);
L = [3, 1, 7, 4, 0, 5, 13, 11, -1]; /* restore the initial list*/
^ sort(L);
!clops; /* 36510 */

isWhite(x) -> x==' ' || x=='\t' || x=='\n' || x=='\f' || x=='\r';
ExtractWords: ew/1, ew1/4, ew2/5, printW/1;
:ExtractWords;
ew(S) -> ew1(S,[],_len(S),0);
ew1(S,W,l,i) -> 
| l==i: W
| isWhite(S[i]): ew1(S,W,l,i+1)
| : ew2(S,W,[S[i]],l,i+1);
ew2(S,W,W1,l,i) -> 
| l==i: W++[W1@string]
| isWhite(S[i]): ew1(S,W++[W1@string],l,i+1)
| : ew2(S,W,W1++[S[i]],l,i+1);
^ew(" All the world likes CalcuList  ");
printW(W) -> 
| W==[]: null
| : {! ^('\n') & ^(W[.]) !} printW(W[>] );
^printW(ew(" All the world likes CalcuList  "));

ExtractWords: printWn/2, printWn1/3;
printWn(W,n) -> printWn1(W,n,n);
printWn1(W,n,i) -> 
| W==[]: null
| i<n: {! ^('\t') & ^(W[.]) !} printWn1(W[>],n,i+1)
| : {! ^('\n') & ^(W[.]) !} printWn1(W[>],n,1);
^printWn(ew(" All the world likes CalcuList  "),3);
:#;

persons = [  [ "john", 25, "NY"], ["mark", 30, "SF"], 
  		     [ "mimmo", 66, "CS"], [ "luisa", 18, "LA"] ];
printPersons(P) -> 
| P == []: null 
| : {! ^('\n') & ^(P[.][0]) & ^("\taged ") & ^(P[.][1]) & ^("\tlives in ") & ^(P[.][2]) !} 
          printPersons(P[>]);
^printPersons(persons);

revPrintPersons(P) -> 
| P == []: null
| : revPrintPersons(P[>]) 
    {! ^('\n') & ^(P[.][0]) & ^("\taged ") & ^(P[.][1]) & ^("\tlives in ") & ^(P[.][2]) !};
^revPrintPersons(persons);


M=[[-1, 2, 3, 0], [2, 0, 0, 1 ], [-1, 0, 1,  -4], [2, 1, 1, 0] ];
printV(V) -> /* formatted print of a vector V */ 
| V==[]: null
| : {! ^("\t") & ^(V[.]) !}  printV(V[>]);
printM(M) -> /* formatted print of a matrix M */ 
| M==[]: null
| : {! ^("\n") & ^(printV(M[.])) !} printM(M[>]);
^printM(M);
^M %*; /* print option for first-level indentation */ 

POLY2_ROOT: readC*/1, solveEqs*/0, solveEq1*/0;
:POLY2_ROOT;
readC*(abc) -> <cst> {! >("int coefficent "++abc++"\n") & <(cst) !} LEX_INT.conv(cst);
solveEqs*()-> <cont> {! >("continue? (Y/N)\n") & <(cont) !} 
| cont=="Y" || cont=="y": solveEq1() 
| : {! >("bye") !} null;
solveEq1*()-> <a,b,c, sol, stype>
{! a=readC("a") & b=readC("b") & c=readC("c") & sol=solve(a,b,c) & stype=printType() !}  {! >(sol) & >(stype) & >('\n') !} solveEqs();
^solveEqs();

TODO: todos, start*/0, prompt*/0, delTodo*/1, printCurrTodos*/2;
:TODO;
printCurrTodos*(t,i) -> 
| t==[]: null
| : {! >(i) & >(": ") & >(t[.]) & >("\n") !} printCurrTodos(t[>],i+1);
start*()-> {! todos=[] !} 
{! ^("\n** List of all non-empty commands **\n")!}
{! >("Commands:\n") !}
{!  >("+ <String> - Add a TODO entry\n") !} 
{!  >("- <Int>    - Delete the numbered entry\n") !} 
{!  >("q          - Quit\n") !}
prompt();
prompt*()-> <comm> {! [printCurrTodos(todos,0)] & >("\nenter command:") & <(comm) !}
| comm=="": {! >("empty command") !} prompt()
| comm[0]=='+': {! todos ++= [comm[1:]] & ^(comm++"\n")!} prompt()
| comm[0]=='-': {! [delTodo(LEX_INT.conv(comm[1:]))] & ^(comm++"\n") !} prompt()
| comm[0]=='q': {! >("bye") !} null
| : {! >("wrong command\n") !} prompt();
delTodo*(n)-> 
| n<0 || n>=_len(todos): {! >("No TODO entry matches the given number\n") !} null
| : {! todos[n]=#null !} null;
^start();
/*
+ Go to bed
+ Buy some milk
+ Shampoo the hamster
-3
-1
q
*/
/* End of Examples of Chapter 3 */