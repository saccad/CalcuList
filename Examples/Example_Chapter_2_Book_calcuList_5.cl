/* Examples of Chapter 2 of CalcuList Book */
^"Hello World";
^ 2+2; /* sum of two integers */
^ (2+1/3)*(1-1/3); /* result with type double because of the operator "/" */
^((2+1/3)*(1-1/3))@int; /* result is casted to integer */
^ (2+1//3)*(1-1//3); /* terms truncated to integer by the operator "//" */
^ 2+2== 4; /* '==' equality operator returning a boolean value*/
^ (2+2== 4)@int; /* the boolean value is converted into 1 or 0 */
^ 2147483647+2; /* as the maximum integer value is incremented by 2, the result is long */
^2147483649L@int; /* error: the number cannot be casted to int */
^2147483649L & '-' & 2147483649L@type; /* three queries */
^ 'A'; /* character */
^ 'A'%*; /* %* is a printing option to print a quoted character */
^ 'A' @int; /* character casted to int (code of 'A') */
^ '\n'; /* escape sequence \n to insert a newline */
^'\n'%*; /* because of the format option format an escape character is not interpreted */
^ '\n' @int; /* character casted to int (code of '\n') */
^ 'A'+2; /* add 2 to the code of 'A' thus obtaining an integer */
^ ('A'+2) @char; /* add 2 to the code of 'A' and casting the result to char to obtain 'C' */
^ 'A'+'a'; /* the result is the int 162 */
^ ('A'+'a')@char; /* the result is the cent sign char */


x=2+.1;/* x is a double variable initialized to 2.1 */
x*=2;/* compound assignment operator, same as x=x*2 */
^x;
y=(2e-2+1)*2; /* y is a double variable initialized to 2.04 */
^y;
z= x@int; /* the value of x is truncated and, thereafter, assigned to z */
^ z; 
^ z+y; /* result with type double */
^ans+x; /* returns the value z+y+x as ans = z+y */
st = z < y; /* st is a bool variable initialized to true because z < y */
^st; 
y=null; /* y is now a variable with type null and value null */
^y; /* null is printed as an empty string */
^y%*; /* because of the print option %*, null is printed  */

^ "Hello "+"World"; /* concatenation of two strings */
^ "Hello "+"World" %*; /* the string is quoted because of %* */
^'H'+"ello "+"World"; /* a char cannot be concatenated to a string */
^'H'@string+"ello "+"World"; /* the char is casted to string */
^""; /* empty string printed as empty line */
^"" %*; /* because of %*, the empty string is quoted */

st="CalcuList"; /* st is now a string variable */
^st;
^st[5]; /* returns the character at the index 5 */
^st[:5]; /* returns the prefix up to the index 4 included */
^st[2:5]; /* returns returns the substring from 2 to 4 */
^st[5:]; /* returns the suffix from the index 5 on */
st1=st[:]; /* assigns st to st1 (same as st1=st) */
st1 ++=" is not only "++ ans; /* compound assignment */
^st1;
^st; /* the string in st is not changed */

squares=[1, 4, 9, 16, 25]; /* squares is a variable of type list */
^ squares; /* the elements of squares are printed */
squares[1]=squares[0]+squares[1];
squares[2]=squares[1]+squares[2];
squares[3]=squares[2]+squares[3];
squares[4]=squares[3]+squares[4];
^ squares;
^ squares[5]; /* the element with index 5 does not exist */
^ squares[-1]; /* negative indices are not allowed */
sq= squares; /* sq is a shallow copy of squares */
^sq; 
square[.]=0.5; /* "[.]" cannot be used in the left side of an assignment */
squares[0]= 0.5;
^squares[.]; /* "[.]" is correct here */
^ sq; /* the update on squares[0] affects sq as well */

charList=['H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd']; /* list of chars, including space */
^ charList; /* characters are always quoted inside a list */
^ charList %*; /* list elements indented by the pretty print option “%*”  */ 
^ charList@string; /* the list is transformed into a string */
^ charList@string%*; /* the string is now printed with a quoted format */
charList=["Hello", ' ', "World"]; /* charList now contains 2 strings and the space char */
str = charList[0]++charList[1]@string++charList[2]; /* str is a string */
^ str;
^ str %*;
^str @list; /* the string is transformed into a list of characters */
strangeList = [ 'H',2, 3.5, [1,"World"], true, null]; /* a heterogeneous list */
^ strangeList; 
^strangeList[3][1]; /* element of a list that is an element of another list */
^strangeList %>; /* print option %>: all level indentation */
^strangeList %*; /* print option %*: first level indentation */

L1=[0, 1, 2, 3, 4];
L2=[5, 6];
L3 = L1 + L2; /* concatenation of two lists */
^L3;
^L2;
^L1; /* L1 has been modified by the above concatenation */
L2[0] = -5;
^L3; /* the change on L2 affects L3 ... */
^L1; /* and L1 as well */
L=[-4 | L2 ];
^L;
L = [1,2,3,4,5];
L1 = L[>]; /* shallow operations: L1 shares the elements of L's tail */
^L1;
L[3]=-4; /* the change in L is reflected in L1 as well */
^L1;
^L1[>1]; /* tail of the tail of L1 (i.e., sublist from the element 2 on) */
L2=L1[:]; /* deep assign: L2 is a clone of L1 */
L1[0]=-2; /* the change does not reflect on L2 */
^ L1;
^ L2; /* L2[0] is not changed */
L2=L1[2:]; /* deep assign: deep assign: L2 replicates the elements of L1 from the index 2 on */
L1[3]=-5; /* the change does not affect L2 */
^L2;
L2=L1[:2]; /* deep assign: L2 replicates the elements of L1 from the start up to the index 1 */
L1[1]=-3; /* the change does not reflect on L2 */
^L2;
L2=L1[1:2]; /* deep assign: L2 replicates the element L1[1] */
L1[1] +=1; /* the update by the compound assignment does not affect L2   */
^L2;
^L1;
 
emp1={"firstName":"Mimmo", "lastName":"Sacca", "age": 30};
^emp1; /* display of a json */
^emp1 %*; /* display of a json with pretty print option */
^emp1["age"]; /* display of a field value for a given key */
^emp1["projects"]; /* null for a missing key, displayed as empty string */
^emp1["projects"] %*; /* null is now displayed */
^emp1@list; /* cast from json to list */
emp2L = [ ["firstName","Anna"], ["lastName","Rossi"], ["age", 32], [ "projects", ["p1", "p2"] ] ]; /* list of pairs (key,value) */
emp2= emp2L@json; /* cast from list to json */
^emp2 %*; 
^emp2 %>; 
^emp2["projects"]; /* the value of the field "projects" is a list */
^emp2["projects"][1]; /* second element of the list */
dept = { "deptName": "unical", "emps": [ emp1, emp2[:], {"firstName":"Luisa", "lastName":"Cosentino", "age": 28, "projects": ["p1", "p3"]} ] };
^dept %>;
^dept["emps"][2]["lastName"]; /* last name of the thirs employee */
emps = dept["emps"]; /* extract the list of employees */
^emps %>;
^>>("dept_unical.dat") dept %>; /* output to a file */
dept=<<("dept_unical.dat"); /* input from a file */
dept1=<<("dept_unical.dat","deptName", "age","projects"); /* keys excluded */
^dept1 %>;

X : v1, v2, L; /* 3 variables with label X initialized to null */
^X %*;
X.v1 = 'A';
X.v2=true;
X.L=L2; /* L2 has been previously defined as the list [-3] */
^X;
X : v3, v1; /* X.v3 is initialized to null and X.v1 preserves its value */
X.v3 = {"K1": "list", "K2": X.L};
^X %>;

v1=10; /* unlabelled variable */
Y: v1; /* a new label */
:Y; /* start of the namespace Y */
v1= "yet another v1"; /* set Y.v1 */
:X; /* the namespace X replaces Y */
^v3; /* print X.v3 */
^v1 %*; /* print X.v1 */
!labels; /* service command that lists all labels */
:#; /* end of the namespace X */
^v3; /* the unlabelled variable v3 is not defined */ 
^v1; /* print the unlabelled variable v1 */
!labels; 

M=[3,4];
L=[1,2,M];
L[1]=null; /* setting the element to null */
^L;
L[1]=#null; /* deleting the element */
^L;
L[1]=#null; /* deleting the sublist M */
^L;
L=#null; /* apparently no delete effect: L is set to null */
^L %*; /* the printing option to display the value null  */
^M; /* the list M has been deleted */

^emp1; /* previously defined */
emp1["age"]=null; /* setting the field to null */
^emp1;
emp1["age"]=#null; /* deleting the field */
^emp1;
emp1=#null; /* apparently no delete effect: emp1 is set to null */
^emp1 %*;

ts=[int, char, int, string]; /* tuple scheme */
tv = [2,'a',4,"bye"]; /* tuple value */
^tv[0]@type==ts[0] && tv[1]@type==ts[1] && tv[2]@type==ts[2] && tv[3]@type==ts[3]; /* type check */
^ts[0]==ts[2]; /* equality and inequality supported for values of type 'type'*/
^ts[0]==type; /* equality and inequality also supported for the value 'type' */
^ts[0]<ts[1]; /* precedence operators not supported for values of type 'type'*/
^tv[1]!=ts[1]; /* equality and inequality not supported for value with different types */
a=ts[0]@type; /* casting to type */
^a;
^a@type; /* casting of the value type to type returns type */
!save; /* file chapter_2_session */
/* End of Examples of Chapter 2 */