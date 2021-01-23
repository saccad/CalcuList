/***** UTILITIES *****/

/**** some maths ****/
min(x,y) -> /* minimum of two comparable (number, bool, string) elements */ 
x < y ? x: y; 

max(x,y) -> /* maximum of two comparable (number, bool, string) elements */
x > y ? x: y; 

log(b,x) -> /* logarithm of x with base b */
_log(x)/_log(b); 

sqrt(x) -> /* square root of a number */
_pow(x,1/2); 

divisible(n,m) -> /* true if n is divisible by m., i.e. the remainder n % m is zero */
n % m ==0;

GCD(n,m) -> /* greatest common divisor of two integers */
n==m || m == 0? n: n<m? GCD(m,n): GCD(m,n%m); 

LCM(n,m) -> /* least common multiple of two integers */
n*m/ GCD(n,m); 

abs(x) -> /* absolute value of a number x */
x < 0? -x: x; 

/* integer floor of a double */
floor_1(x,n) ->  /* called by floor(x), not to be used stand-alone */ 
x==n || x>0 ? n:  n-1;
floor(x) -> /* integer floor of a double */
floor_1(x,x@int) ; 

/* integer ceil of a double */
ceil_1(x,n) ->  /* called by ceil(x), not to be used stand-alone */ 
x==n || x<0 ? n:  n+1;
ceil(x) -> /* integer ceil of a double */
ceil_1(x,x@int); 

round(x) -> /* round of x to the closest integer */
abs(x-x@int) < 0.5? floor(x): ceil(x); 

/**** operations on characters ****/
isSpace(c) -> /* returns true if c is space */
c == ' ';

isWhiteSpace(x) -> /* returns true if x is a white space */
x==' ' || x=='\t' || x=='\n' || x=='\f' || x=='\r';

digitVal(x) -> /* returns the value (from 0 to 9) of a decimal char */
x-'0';

isDigit(x) -> /* returns true if x is a decimal char */
'0'<=x && x<='9';

isLetter(x) -> /* returns true if x is a letter */
'a' <= x && x <= 'z' || 'A' <= x && x <= 'Z';

isLetterOrDigit(x) -> /* returns true if x is a letter or a decimal digit */
isDigit(x) || isLetter(x);

isUpperCase(x) -> /* returns true if x is an upper case letter */
'A' <= x && x <= 'Z';

isLowerCase(x) -> /* returns true if x is a lower case letter */
'a' <= x && x <= 'z';

toUpperCase(x) -> /* change case to a lower case letter */
isLowerCase(x)? (x-'a'+'A')@char: x;

toLowerCase(x) -> /* change case to an upper case letter */
isUpperCase(x)? (x-'A'+'a')@char: x;

/*** basic list operations ****/

range(x1,x2) -> /* construct a list of all integers in the range <x1, x2> */
x1 > x2? []: [x1|range(x1+1,x2)];

randL(minv, maxv, k) -> /* generate a list of k random doubles in the range <minv, maxv> */
k<=0? []: [_rand()*(maxv-minv)+minv|randL(minv, maxv, k-1)];

randLI(minv, maxv, k) -> /* generate a list of k random integers in the range <minv, maxv> */
k<=0? []: [round(_rand()*(maxv-minv)+minv)|randLI(minv, maxv, k-1)];

/* list membership test */
member(X,L) -> /* returns true if X is an element X of the list L */
L!=[] && ( L[.]==X || member(X,L[>]) ); 

/* deep append of the list L2 to the end of a list L1, that does not modify both lists  */
/* shallow append, coalescing the two lists, can be simply implemented as "L1+L2" */
/* other semi-deep operators are: */
/* \n(1) L1+L2[:], that only modifies L1 and \n(2) L1[:]+L2 that only modifies L2 */
append(L1,L2) -> /* deep append of two lists L1 and L2 - more efficient than L1[:]+L2[:] */
L1==[]? L2[:]: [L1[.]|append(L1[.],L2)];

equalL(L1,L2) -> /* return true if two lists have the same elements */
L1==[]||L2==[]? L1==[]&&L2==[]: L1[.]==L2[.] && equalL(L1[>],L2[>]); 

compareL(L1,L2) -> /* comparison of two lists L1 and L2: it returns \n0 if L1 and L2 have the same elements, \n-1 (resp. 1) if L1 lexicographically precedes (resp., follows) L2 */
L1==[] && L2==[]? 0: L1==[]? -1: L2==[]? 1: L1[.]<L2[.]? -1: L1[.]>L2[.]? 1: compareL(L1[>],L2[>]);


/* maximum of a list */
maxL1(L,m) -> /* called by maxL – not to be used stand-alone */
L[>]==[]? max(m,L[.]): maxL1(L[>],max(m,L[.]));
maxL(L) -> /* it computes the maximum of a list */
L==[]? null: maxL1(L[>],L[.]);

/* minimum of a list */
minL1(L,m) -> /* called by minL – not to be used stand-alone */
L[>]==[]? min(m,L[.]): minL1(L[>],min(m,L[.]));
minL(L) -> /* it computes the minimum of a list */
L==[]? null: minL1(L[>],L[.]);

/* reverse of a list */
reverse_1(L, M) -> /* called by reverse(L), not be used otherwise */
L==[]? M: reverse_1(L[>],[L[.]|M]); 
reverse(L) -> /* reverse of a list computed by tail recursion */
reverse_1(L,[]);


