/* Examples of Chapter 3 */
isLowerCase(c) -> 'a'<=c && c<='z'; /* the function returns a boolean */
toUpperCase(c) -> isLowerCase(c)? ('A'+c-'a') @char: c; /* the function returns a character */
^ toUpperCase('b');

fact(x) ->  /* the factorial recursive function */
			x<=1? 1: x*fact(x-1);
^ fact(5);
fib(x) ->  /* the Fibonacci recursive function */
			x<=1? x: fib(x-1)+fib(x-2);
^ fib(10);
!clops; /* 46300 */
fibe1(x, f2, f1, k) -> x==k? f1: fibe1(x, f1, f1+f2, k+1); /* function that will be called next */
fibe(x) -> /* the Fibonacci tail-0  recursive function */
			x<=1? x: fibe1(x, 0, 1, 1);
^ fibe(10);
!clops; /* 3746*/
^fib(30);
!clops; /* 704098440 */
^fibe(30);
!clops; /* 10946 */

is_odd(x) -> null; /* dummy definition */
is_even(x) -> /* returns true if x is even */ x<0? is_even(-x): x==0? true: is_odd(x-1);
is_odd(x) -> /* returns true if x is odd */ x<0? is_odd(-x): x==0.0? false: is_even(x-1); /* actual definition */
^is_odd(-3);
^is_even(-3);

d =_gDate(); /* get current date as a long value */
^d; /* print the long value d */
^d@type; /* print the type for d */
^_pDate(d); /* print the date stored in d in the default Java format */
^_pDate(d,"dd/MM/yyyy 'h:' HH.mm.ss"); /* print the date in a given format */ 
dd = _gDate("1950/11/05 22:20:30:150","yyyy/MM/dd HH:mm:ss:SSS"); /* get a date given in the format described by the second argument */ 
^_pDate(dd); /* print the date stored in dd */


^ _exp(1); /* print Euler's number */
^ _log(3); /* print the natural logarithm of 3 */
^ _pow(2, 3); /* print 2^3 */
log(b, x) -> _log(x)/_log(b); /* logarithm of a double x in base b */
^ log(2, 8); /* logarithm of 8 in base 2 */
sqrt(x) -> _pow(x, 1/2); /* squared root of a double x */
^ sqrt(16);
^_len("bye"); /* length of a string  */
^_len([1,2,3]); /* length of a list */
emp={"firstName":"Luisa", "lastName":"Cosentino", "age": 28, "projects": ["p1", "p3"]};
^_len(emp); /* length of a json (i.e., number of fields) */
^_tuple(dd); /* list of values for the date "1950/11/05 22:20:30:150"  */
^_tuple(emp); /* list of values for a json */
isLeap(D) -> _tuple(D)[0]%4==0; 
^isLeap(_gDate()); /*  the current year is 2020 */
^_rand(); /* generation of a random double x, 0 <= x <= 1 */
st = "01010101";
^_ind(st,"10"); /* returns the index within st of the first occurrence of "10" */
^_ind(st,"100"); /* returns -1 as "100" does not occur in s  */
^_ind(st,"10",4); /* returns the index within st of the first occurrence of "10", starting at the index 4 */
^_ind(st,"10",6); /* returns -1 as "10" does not occur in st, starting at the index 6 */

L1 = [1, 3, 5, 7, 9];
L2 = [0, 2, 4, 6, 8, 10, 12];
listReverse(L) -> L==[]? []: listReverse(L[>])++[L[.]];
^ listReverse(L1);
listMerge(O1, O2) -> O1==[]? O2[:]: O2==[]? O1[:]: O1[.]<O2[.]? [O1[.]|listMerge(O1[>], O2)]: [O2[.]|listMerge(O1, O2[>])];
^ listMerge(L1, L2) ;
^ L1; /* no sides effects on L1*/
^ L2; /* no sides effects on L2*/

reverse1(L, R) -> L==[]? R: reverse1(L[>], [L[.]|R]);
reverse(L) -> reverse1(L, []);
range(x1, x2) -> x1>x2? []: [x1|range(x1+1, x2)]; 
L = range(1, 1000);
^ listReverse(L);
!clops; /* 2886827 */
^ reverse(L);
!clops; /* 394458 */

member(X,L) -> /* returns true if X is an element X of the list L */
L==[]? false: L[.]==X? true: member(X,L[>]); 
addEl(L,X) -> /* adds the element X to the list L if it is not in it */
member(X,L)? L: [X|L];
listUnion(L1,L2) -> /* union of the elements of the lists L1 and L2  */
L2==[]? L1: listUnion(addEl(L1,L2[.]),L2[>]);
listAllProj(E) -> E==[]? []: E[.]["projects"]==null? listAllProj(E[>]): listUnion(listAllProj(E[>]),E[.]["projects"]);
dept=<<("dept_unical.dat"); /* reading a json from a file */
emps=dept["emps"]; /* extract the list of employees */
^listAllProj(emps); /* list of all projects */

MATH: min/2, max/2, log(base,x), sqrt/1, abs/1, round(_), 
      floor/1, floor_1/2, ceil/1, ceil_1/2, E, PI; /* labelled functions and variables */
:MATH;
E = 2.718281828459045; /* approximated Euler's number */
PI = 3.141592653589793; /* constant pi with relative error of about 8*10^(−8) */
^min; 
min(x,y) -> /* minimum of two comparable (number, bool, string) elements */ 
x < y ? x: y; 
max(x,y) -> /* maximum of two comparable (number, bool, string) elements */
x > y ? x: y; 
log(b,x) -> /* logarithm of x with base b */
_log(x)/_log(b); 
sqrt(x) -> /* square root of a number */
_pow(x,1/2); 
abs(x) -> /* absolute value of a number x */
x < 0? -x: x; 
floor(x) -> /* integer floor of a double */
floor_1(x,x@int)@int ; 
floor_1(x,n) ->  /* called by floor(x), not to be used stand-alone */ 
x==n || x>0 ? n:  n-1;
ceil_1(x,n) ->  /* called by ceil(x), not to be used stand-alone */ 
x==n || x<0 ? n:  n+1;
round(x) -> /* round of x to the closest integer */
abs(x-x@int) < 0.5? (x>0? floor(x): ceil(x)): (x>0? ceil(x): floor(x));
:#; 
MATH.ceil(x) -> MATH.ceil_1(x,x@int)@int; /* integer ceil of a double */
^MATH.round(-1.51);
^MATH;


merge(O1, O2, ord/2) -> O1==[]? O2[:]: O2==[]? O1[:]: ord(O1[.], O2[.])? [O1[.]|merge(O1[>], O2, ord)]: [O2[.]|merge(O1, O2[>], ord)];
lte(x, y) -> x<=y;
gte(x, y) -> x>=y;
L1 = [1, 3, 5, 7, 9, 11, 13, 15, 17, 19];
L2 = [2, 4, 6, 8, 10, 16];
^ merge(L1, L2, lte);
^ merge(reverse(L1), reverse(L2), gte);
checkOrder(L, o/2) -> L==[] || L[>]==[]? true: o(L[.], L[1]) && checkOrder(L[>], o);
^ checkOrder(L1, lte);
^ checkOrder(reverse(L1), lte);
^ checkOrder(reverse(L1), gte);

filter(L, f/1) -> L==[]? []: f(L[.])? [L[.]|filter(L[>], f)]: filter(L[>], f);
d2or3(x) -> x%2==0 || x%3==0;
d2and3(x) -> x%2==0 && x%3==0;
^ filter(range(1, 20), d2or3);
^ filter(range(1, 20), d2and3);

map(L, f/1) -> L==[]? []: [f(L[.])|map(L[>], f)];
square(x) -> x*x;
cube(x) -> x*x*x;
^ map(range(1, 10), square);
^ map(range(1, 10), cube);
reduce(L, f/2, init) -> L==[]? init: f(L[.], reduce(L[>], f, init));
sum(x, y) -> x+y;
prod(x, y) -> x*y;
^ reduce(range(1, 10), sum, 0);
^ reduce(range(1, 10), prod, 1);
^ reduce(map(filter(range(1, 10), d2and3), cube), sum, 0);

fmap(L, f/1, g/1) -> L==[]? []: f(L[.])? [g(L[.])|fmap(L[>], f, g)]: fmap(L[>], f, g);
^fmap(range(1, 20), d2and3, cube);


jsfmap(LJ,filterf/3,K,V,mapf/2,M) -> LJ==[]? []: filterf(LJ[.],K,V)? [mapf(LJ[.],M)|jsfmap(LJ[>],filterf,K,V,mapf,M)]: jsfmap(LJ[>],filterf,K,V, mapf,M); /* filter function for a list of jsons */
filter1KV(J,K,V) -> J[K]!=null && J[K]==V; /* the value of the field K must be equal to V */
map1K(J,K) -> J[K]; /* the value of the field K is mapped */
^jsfmap(emps,filter1KV,"age",28,map1K,"lastName");
filter1KinV(J,K,V) -> J[K]!=null && member(V,J[K]); /* the value of K must be a list containing the element V */
selLK(LJ,LK) -> LJ==[]? []: member(LJ[.][0],LK)? [LJ[.] | selK(LJ[>],LK)]: selK(LJ[>],LK);
map1LK(J,LK) -> selLK(J@list,LK);
^jsfmap(emps,filter1KinV,"projects","p1",map1LK, ["firstName","lastName", "age"]) %*; /* option %* to print with first level indentation */

^ checkOrder(L1, \(x,y)-> x < y ); /* function lambda(x,y)= x < y */
/* query with three lambda functions */
^ reduce(fmap(range(1, 20), \(x)-> x%5==0, \(x)->2*x ), \(x,y) -> x+y, 0);
/* a function containing lambda functions in its body */
mapRed5(L)-> reduce(fmap(L, \(x)-> x%5==0, \(x)->2*x ), \(x,y)-> x+y, 0);
^ mapRed5(range(1,20));
^jsfmap(emps,filter1KinV,"projects","p1",\(J,d)->J, null) %>;
f(g/2,x) -> x+2+g(x,2*x);
a(x,y)-> x+y;
^f(\(x,y)-> x+f(a,y),3);
^f(\(x,y)-> x+f(\(z,w)-> z+w,y),3); /* error: lambda def inside lambda */

/* function returning a function */
add(a)/1->\(b)->a+b;
^add(4)(3);
^add(4)(add(3)(5));
twice(f/1)/1 -> \(x)-> f(f(x)); 
^twice(add(3))(7); /* add 6 (i.e., twice 3) to 7 */
^twice(\(x)->x*x)(7); /* compute 7^4 */

/* map function for list with currying */
map1(f/1,L)-> L==[]? []: [f(L[.]) | map1(f,L[>])];
map0(f/1)/1->\(L)-> map1(f,L); /* map0 stands for map */
/* N.B.: map has been previously defined with different signature */ 
^map0(add(10))([1,2,3]);
len(x)->_len(x);
^map0(len)(["two", "three", "sixteen"]);

/* grouping parameters for polynomial computation */
pol2val(a,b,c)/1 -> \(x) -> a*x*x+b*x+c;
^pol2val(4,-3,5)(10);

/*  definition of fmap/reduce with currying features */
reduce(L, f/2, init) -> L==[]? init: f(L[.], reduce(L[>], f, init));
fReduce(f/2,init)/1 -> \(L) -> reduce(L,f,init);
^fReduce(sum,0)(range(1,10));
^reduce(fmap(range(1,10),d2and3,cube),sum,0);
^fReduce(sum,0)(fmap(range(1,10),d2and3,cube));

/* querying functions */
^fReduce; /* the function is displayed */
^fReduce(sum,0); /* the returned function is displayed */
^twice;
^twice(add(3));
^twice(\(x)->x*x);
^\(x,y)->x<y;

/* guards */
exampleGuardFunction(x)->
| x<0: "Negative"
| x==0: "Zero"
| x<10: "Single Digit"
| : "Multiple Digits";
^exampleGuardFunction(8)
exampleNoGuardFunction(x)->  
 x<0? "Negative": 
 x==0? "Zero": 
 x<10? "Single Digit": 
 "Multiple Digits";

/* high order labeled functions */
MAP_RED: fmap(L, f/1, m/1), reduce(L, r/2, init), fReduce(r/2,init)/1;
:MAP_RED;
^fmap;
^fReduce;
fmap(L, f/1, m/1) -> 
| L==[]: []
| f(L[.]): [m(L[.])|fmap(L[>], f, m)]
| : fmap(L[>], f, m);
reduce(L, r/2, res) -> 
| L==[]: res
|: reduce(L[>], r, r(res,L[.]));
fReduce(r/2,init)/1-> \(L)-> reduce(L,r,init);
^fReduce(sum,0)(fmap(range(1,10),d2and3,cube));
^fmap;
^fReduce;
^fReduce(sum,0);
^MAP_RED;

/* End of Examples of Chapter 3 */