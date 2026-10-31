/* Examples of Section 8 */

LIST: member/2, equal/2, compare/2, reverse/1, reverse_1/2;
:LIST;
member(X,L) -> /* it returns true if X is an element of the list L */
L==[]? false: L[.]==X? true: member(X,L[>]);

equal(L1,L2) -> /* it returns true if two lists have the same elements */
L1==[]||L2==[]? L1==[]&&L2==[]: L1[.]==L2[.] && equal(L1[>],L2[>]); 

reverse(L) -> /* reverse of a list computed by tail recursion */
reverse_1(L,[]);
reverse_1(L, M) -> /* called by reverse(L), not be used stand-alone */
L==[]? M: reverse_1(L[>],[L[.]|M]); 

compare(L1,L2) -> /* comparison of two lists; it returns 0 if equal(L1,L2) is true, or -1 (resp. 1) if L1 lexicographically precedes (resp., follows) L2 */
| L1==[] && L2==[]: 0
| L1==[]: -1
| L2==[]: 1
| L1[.]<L2[.]: -1
| L1[.]>L2[.]: 1
|: compare(L1[>],L2[>]);

LIST: isPalindrome/1, rep/2, range/2;

isPalindrome(L) -> /* returns true if L reads the same backward or forward */
L==[] || L[>]==[]? true: equal(L,reverse(L));

rep(L,i) -> /* returns the list obtained by concatenating the list L for i times */
i<=0? []: L[:] ++ rep(L,i-1);

range(x1, x2) -> /* it returns the list of numbers between X1 and X2 */
x1>x2? []: [x1|range(x1+1, x2)];

^isPalindrome([1,0,2,0,1]);
^rep([0,1],5);
L=range(-10000,-1);
^L;

LIST: append/2;

append(L1,L2) -> /* deep append of two lists L1 and L2 */
L1[:] ++ L2;

append1(L1,L2) -> L1==[]? L2: [L1[.]|append1(L1[>],L2)];

L1=append(L,[0]);
!clops; /* 410310 */
L1=append1(L,[0]);
!clops; /* 3930354 */

LIST: rotate/2;

rotate(L,k) -> /* list rotation by k positions */
<n,k1> {! n=_len(L) !}  
| k==0: []
| {! k1=k%n !} k>0: L[k1:] ++ L[:k1]
|: L[n+k1:] ++ L[:n+k1];

^rotate([ 1, 2, 3, 4, 5, 6 ],3);
^rotate([ 1, 2, 3, 4, 5, 6 ],-4);

LIST: compress/1, countH/1, countH_1/3, compress_1/2;

compress(L) -> /* compress a list */
L==[]?[]: compress_1(L,[]);
countH(L) -> L==[]? []: countH_1(L[.],L[>],1);
countH_1(x,L,n) -> L==[]||L[.]!=x?[L,n]: countH_1(x,L[>],n+1);
compress_1(L,M) -> <T> 
| L==[]: M
|: {! T=countH(L) !} compress_1(T[0],M++[[L[.],T[1]]]);

L = [1, 1, 2, 2, 3, 3, 3, 3, 4, 1, 1, 2, 2, 2, 2, 4, 4, 4, 4];
^compress(L);

LIST: replace/3, checkSL/2;

replace(L,SL1,SL2) -> /* replace sublist in a list */
<cRes> 
| L==[]: []
| {! cRes = checkSL(L,SL1) !} cRes[0]: SL2[:]++replace(cRes[1],SL1,SL2)
| : [L[.]|replace(L[>],SL1,SL2)];
checkSL(L,SL) -> 
| SL==[]: [true,L]
| L==[] || SL[.]!=L[.]: [false,L]
| : checkSL(L[>],SL[>]);
L=[ 1, 1, 2, 2, 3, 1, 2, 3, 3, 1, 1, 2, 2 ];
^replace(L,[1,2],[-1,-2,-4]);

LIST: subSet/1, add1E/2;

subSet(S) -> /* construct all subsets of a set */
<R> 
| S==[]: [[]]
|: {! R = subSet(S[>]) !} R ++ add1E(R, S[.]);
add1E(LS,E) -> /* given a list LS of sublists, add E to each sublist of LS */
LS==[]? []:  [ [E|LS[.]] |add1E(LS[>],E)];
Set=[1,2,3];
^subSet(Set)%*;

LIST: PermL, permute*/1, heapP*/2, heapP_iter*/3, swap*/3;
:LIST;
permute*(L) -> /* construct all permutations of a list */
{! PermL=[] !} L==[]? []: heapP(L,_len(L));
heapP*(L,n)-> /* Heap's permutation algorithm */
| n==0: PermL
| n==1: {! PermL= [L[:]|PermL] !} PermL
|: heapP_iter(L,0,n);
heapP_iter*(L,i,n) -> /* heapP iteration */ 
| i>=n: PermL
|: {! [heapP(L,n-1)] !} heapP_iter(swap(L,n%2==0?i:0,n-1),i+1,n);
swap*(L,i,j) -> /* swap elements i and j of a list L */
<temp>
| i==j: L 
|: {! temp=L[i] & L[i]=L[j] & L[j]= temp !} L;
^permute(Set)%*;

STRING: hasProp(_,_/1), hasProp1(_,_/1,_,_), allProp(_,_/1), 
allProp1(_,_/1,_,_), isPalindrome/1, isPalindrome1/3;
:STRING;
hasProp(S,f/1) -> /* checking whether a string contains a character 
with the property defined by the generic function f */
hasProp1(S,f,0,_len(S));
hasProp1(S,f/1,i,l) -> 
| i >= l: false
| f(S[i]): true
|: hasProp1(S,f,i+1,l);
allProp(S,f/1) -> /* checking whether all elements of a string or of a list 
have the property defined by the generic function f */
allProp1(S,f,0,_len(S));
allProp1(S,f/1,i,l) -> i >= l? true: f(S[i]) && allProp1(S,f,i+1,l);
isPalindrome(S) -> isPalindrome1(S,0,_len(S)-1);
isPalindrome1(S,i,j)-> i>j?true: S[i]==S[j] && isPalindrome1(S,i+1,j-1);

LIST: hasProp(_,_/1), allProp(_,_/1);
LIST.hasProp(L,f/1) -> /* property check function specialized for a list */
L==[]? false: f(L[.])? true: hasProp(L[>],f);
LIST.allProp(L,f/1) -> /* all property check function specialized for a list  */
| L==[]: true 
| !f(L[.]): false
| : allProp(L[>],f);

L=LIST.rep(['0','1','0'],500); /* create a list by repeating a given char sublist 500 times */
S=L@string; /* convert the list L into the string S */
^hasProp(S, \(x) -> x=='2'); /* checks whether the character '2' is in the string S */
!clops; /* 835978 */
^hasProp(L,\(x) -> x=='2'); /* the same check for the list L */
!clops; /* the function hasPropS is not efficient for a list */ /* 27915493 */
^LIST.hasProp(L,\(x) -> x=='2'); /* same check with specific function for lists*/ 
!clops; /* 801336 */


CHAR: isDigit/1; CHAR.isDigit(X) -> '0' <= X && X <= '9';
^allProp(S,CHAR.isDigit); /* checks whether all characters of the string S are digits */
!clops; /* 979978 */
^allProp(L,CHAR.isDigit); /* the same check for the list L */
!clops; /* the function allPropS is not efficient for a list */ /* 28044493 */
^LIST.allProp(L,CHAR.isDigit); /* same check with specific function for lists*/ 
!clops; /* 940836 */


^isPalindrome(S); /* checks whether the string S is a palindrome */
!clops; /* 391987 */
^isPalindrome(L); /* checks whether the list L is a palindrome */
!clops; /* the function isPalindromeS is not efficient for a list */ /* 27456502 */
^LIST.isPalindrome(L); /* specific palindrome check for a list */
!clops; /* the function isPalindromeS is not efficient for a list */ /* 1516388 */
^isPalindrome("INGIRUMIMUSNOCTEETCONSUMIMURIGNI"); 

CHAR: toUpperCase/1;
CHAR.toUpperCase(X) -> 'a' <= X && X <= 'z'? ('A'+X-'a')@char: X;
STRING: toUpperCase/1, toUpperCase1/3;
toUpperCase(S) -> toUpperCase1(S,0,_len(S))@string;
toUpperCase1(S,i,l) -> i>=l? []: [CHAR.toUpperCase(S[i]) | toUpperCase1(S,i+1,l)];

STRING: reverse/1, reverse1/4;
reverse(S) -> reverse1(S,[],0,_len(S))@string;
reverse1(S,L,i,l) -> 
i>=l? L: reverse1(S,[S[i]|L],i+1,l);

^toUpperCase("CalcuList");
^toUpperCase("InGirumImusNocteEtConsumimurIgni"); 
^reverse(S); /* S previously defined */
!clops; /* 692029 */
^LIST.reverse(S@list)@string;
!clops; /* 665037 */

STRING: replace/3, replaceM/3, replaceM1/3;
replace(S,S1,S2) -> /* replace all occurrences of a substring in a string */
LIST.replace(S@list, S1@list, S2@list)@string;
replaceM(S,LS1,LS2) -> /* replace multiple substrings in a string */
replaceM1(S@list,LS1,LS2)@string;
replaceM1(L,LS1,LS2) -> LS1==[] || LS2==[]? 
L: replaceM1(LIST.replace(L, LS1[.]@list, LS2[.]@list),LS1[>],LS2[>]);

^replace("In Girum Imus Nocte Et Consumimur Igni"," ", "");
^replaceM("Dr. <name>\n<city>\n\ndear <name> how are you?", ["<name>", "<city>"], ["Mark", "Rome"]); 

CHAR: indL/1, isLetter/1, codeDecL/3, mod26/1;
:CHAR;
indL(X) -> X-'A'; /* offset w.r.t. 'A': values from 0 to 25 */ 
isLetter(X) -> 'a' <= X && X <= 'z' || 'A' <= X && X <= 'Z';
codeDecL(X,k,cd) -> cd=='C'? mod26(indL(X)+k)@char: mod26(indL(X)-k)@char;
mod26(k) -> 
<tmp> {! tmp=k%26 !} {! tmp=tmp<0? tmp+26: tmp !} 'A'+tmp;
CAESAR: code/2, decode/2, codeDec/5;
:CAESAR;
code(S,K) -> <SU> {! SU = STRING.toUpperCase(S) !} 
| CHAR.isLetter(K): codeDec(SU,CHAR.indL(K),'C',0,_len(SU))@string
| : {! exc(true,"wrong key '"++ K ++"'") !} null;
decode(S,K) -> <SU> {! SU = STRING.toUpperCase(S) !} 
| CHAR.isLetter(K): codeDec(SU,CHAR.indL(K),'D',0,_len(SU))@string
| : {! exc(true,"wrong key '"++ K ++"'") !} null;
codeDec(S,k,cd,i,n) ->
| i>=n: []
| CHAR.isLetter(S[i]): [CHAR.codeDecL(S[i],k,cd) | codeDec(S,k,cd,i+1,n)]
|: codeDec(S,k,cd, i+1,n);

text="attack the Gauls at dawn";
CODE=code(text,'H'); /* coding */ 
^CODE;
^decode(CODE,'H'); /* decoding */ 

VIGENERE: code/2, decode/2, codeDec/6, convertKey/3;
:VIGENERE;
code(S,K) -> <SU,LK> 
{! SU=STRING.toUpperCase(S) & LK=convertKey(STRING.toUpperCase(K),0,_len(K)) !} 
codeDec(SU,LK, LK, 'C',0,_len(S))@string;
decode(S,K) -> <SU,LK> 
{! SU=STRING.toUpperCase(S) & LK=convertKey(STRING.toUpperCase(K),0,_len(K)) !} 
codeDec(SU,LK, LK, 'D',0,_len(S)) @string;
convertKey(K,i,n) -> /* convert key chars to indices stored into a list */
| i >=n: []
| CHAR.isLetter(K[i]): [ CHAR.indL(K[i]) | convertKey(K,i+1,n) ]
|: {! exc(true,"wrong key '"++ K ++"'") !} null;
codeDec(S,K1,K,cd,i,n) -> <K2> 
| i >=n: []
| CHAR.isLetter(S[i]): {! K2= K1[>]==[]? K: K1[>] !}
                       [CHAR.codeDecL(S[i],K1[.],cd) | codeDec(S,K2,K,cd,i+1,n)]
|: codeDec(S,K1, K,cd,i+1,n);

CODE=code(text,"huge"); /* coding */
^CODE;
^decode(CODE,"huge"); /* decoding */


dept=
{ 
     "deptName": "unical",
     "emps": [
          { 
               "firstName": "Mimmo",
               "lastName": "Sacca",
               "age": 30
          },
          { 
               "firstName": "Anna",
               "lastName": "Rossi",
               "age": 32,
               "projects": [
                    "p1",
                    "p2"
               ]
          },
          { 
               "firstName": "Luisa",
               "lastName": "Cosentino",
               "age": 28,
               "projects": [
                    "p1",
                    "p3"
               ]
          }
     ]
};

deptSchema = 
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "title": "employee json schema",
  "description": "Schema of a dataset of university employees",
  "properties": {
    "deptName": {
      "type": "string",
      "description": "name of the university"
    },
    "emps": {
      "type": "array",
      "description": "list of employees",
      "items": {
        "type": "object",
        "description": "an employee",
        "properties": {
          "firstName": {
            "type": "string"
          },
          "lastName": {
            "type": "string"
          },
          "age": {
            "type": "integer"
          },
          "projects": {
            "type": "array",
            "description": "list of projects",
            "items": {
              "type": "string",
              "description": "project name"
            }
          }
        },
        "required": [
          "firstName",
          "lastName",
          "age"
        ]
      }
    }
  },
  "required": [
    "deptName",
    "emps"
  ]
};

/* validate a json against its schema */
JSON: checkJS/2, checkJSall/2, checkJSobj/2, checkJSprop/2, checkJSarray/2, 
checkJSreq/2, checkJSlist/2, checkJSfield/3;
: JSON;
checkJS(S,J) -> /* Validate a JSON instance against its schema */
S@type==json && (!_isKey(S,"type") || checkJSall(S,J));
checkJSall(S,J) -> <t> /* Validate a type instance against its schema */
{! t=S["type"] !} 
| t=="object": checkJSobj(S,J) 
| t=="array": checkJSarray(S,J)
| t=="string": J@type==string
| t=="null": J==null
| t=="boolean": J@type==bool
| t=="integer": J@type==int
| t=="number": J@type==int || J@type==double
|: false;
checkJSobj(S,J) -> /* validate a json value against its object schema */
J@type==json && 
( !_isKey(S,"properties") || checkJSprop(S["properties"]@list,J) ) && 
( !_isKey(S,"required") || checkJSreq(S["required"],J) );
checkJSprop(SP,J) -> /* validate the instance w.r.t. all fields in "properties" */
| SP==[]: true
|: checkJSfield(SP[.][0],SP[.][1],J) && checkJSprop(SP[>],J);
checkJSarray(S,J) -> <o> /* validate list elements against their schema */ 
| J@type!=list: false
| !_isKey(S,"items"): true
|: {! o=S["items"] !}
   o@type==json && ( !_isKey(o,"type") || checkJSlist(o,J)); 
checkJSreq(LR,J) -> /* all “required” fields must occur in the instance */
LR==[]? true: _isKey(J,LR[.]) && checkJSreq(LR[>],J); 
checkJSlist(S,LJ) -> /* validate a list element against the item schema */
!_isKey(S,"type") || LJ==[]? true: checkJSall(S,LJ[.]) &&  checkJSlist(S,LJ[>]);
checkJSfield(K,V,J) -> /* check whether a schema key has a type and the instance has that key  */
!_isKey(V,"type") || !_isKey(J,K) || checkJSall(V,J[K]);

^checkJS(deptSchema,dept);
dept1= dept[:]; /* clone dept */
dept1["emps"][1]["age"]= #null; /* delete a field */
^checkJS(deptSchema,dept1);

prizeSchema=
{ 
     "type": "object",
     "title": "nobel_prizes json schema",
     "description": " ",
     "properties": { 
          "prizes": { 
               "type": "array",
               "description": "list of all nobel prizes, ordered by year",
               "items": { 
                    "type": "object",
                    "description": "a nobel prize given a year for a category",
                    "properties": { 
                         "year": { 
                              "type": "string",
                              "description": "From 1901 to 2023"
                         },
                         "category": { 
                              "type": "string",
                              "description": "6 categories: Physics, Chemistry, Medicine, Literature, Peace, Economy"
                         },
                         "overallMotivation": { 
                              "type": "string",
                              "description": "only when the prize was not awarded"
                         },
                         "laureates": { 
                              "type": "array",
                              "description": "list of the recipients for that category and that year - from 1 to 3 laureates sharing the same prize",
                              "items": { 
                                   "type": "object",
                                   "description": "recipient or co-recipient",
                                   "properties": { 
                                        "id": { 
                                             "type": "string"
                                        },
                                        "firstName": { 
                                             "type": "string"
                                        },
                                        "surname": { 
                                             "type": "string"
                                        },
                                        "motivation": { 
                                             "type": "string"
                                        },
                                        "share": { 
                                             "type": "string",
                                             "description": "from 1 to 3"
                                        }
                                   }
                              }
                         }
                    },
                    "required": [
                    	"year",
                    	"category"
                    ]
               }
          }
     }
};


nobel_prizes=<<(".../nobel_prizes.dat"); /* read nobel prize dataset from a text file */
prizes=nobel_prizes["prizes"]; /* extract the list of all prizes */
^_len(prizes); /* total number of prizes */
^prizes[_len(prizes)-1] %>; /* last prize in the list */
^checkJS(prizeSchema,nobel_prizes);

NOBEL: filter/4, fmapPr(P,year,cat,lau,Myear,Mcat,Mlau,mLau/1), mapPr(p,MYear,MCat, MLau,mLau/1), mLauNo/1, mLauL/1, mLauNull/1;
:NOBEL;
filter(p,yearL,catL,lauNoL) -> <laureates>
{! laureates= p["laureates"]==null? []: p["laureates"] !}
( yearL==null || LIST.member(p["year"],yearL) ) &&
( catL==null || LIST.member(p["category"],catL) ) &&
( lauNoL==null || LIST.member(_len(laureates),lauNoL) );
fmapPr(P,yearL,catL,lauNoL,MYear,MCat,MLau,mLau/1) ->
| P==[]: []
| filter(P[.],yearL,catL,lauNoL): [ mapPr(P[.],MYear,MCat,MLau,mLau) | 
 fmapPr(P[>],yearL,catL, lauNoL, MYear,MCat,MLau,mLau)]
|: fmapPr(P[>],yearL,catL,lauNoL,MYear,MCat,MLau,mLau);
mapPr(p,MYear,MCat,MLau,mLau/1) -> <laureates>
{! laureates= p["laureates"]==null? []: p["laureates"] !}
| MYear && MCat && MLau: [ p["year"], p["category"], mLau(laureates) ]
| MYear && MCat: [ p["year"], p["category"] ]
| MYear && MLau: [ p["year"], mLau(laureates) ]
| MYear: p["year"]
| MCat && MLau: [ p["category"], mLau(laureates) ]
| MCat: p["category"]
|: mLau(laureates);
mLauNo(laureates) -> _len(laureates); /* number of laureates for a prize */
mLauL(laureates) -> laureates; /* all info on laureates for a prize */
mLauNull(_) -> null; /* no info is displayed on laureates for a prize */

/* info on all laureates in "peace" and "literature" for 2023 and 1901 */
^fmapPr(prizes,["2023","1901"],["peace","literature"],null,true,true,true,mLauL) %>;
/* numbers of laureates in all categories for 2023 and 1901 */
^fmapPr(prizes,["2023","1901"],null,null,true,true,true,mLauNo) %*;
/* categories for which no prize was awarded across all years */
^fmapPr(prizes,null,null,[0],true,true,false,mLauNo) %*;
/* the first nobel prize in economics was awarded in 1969 */
^fmapPr(prizes,["1969","1968"],["economics"],null,true,false,true,mLauNo);



NOBEL: reducePrC(P,r/2,init), reducePrC1(_,_/2,_), 
reducePrA*(MP,r/2,LresInit), reducePrA1*(_,_/2,_), modResPrA*/2,
redCount/2, redCount1/3, redSum/2, redSum1/3, redList/2, redList1/3;
reducePrC(P,r/2,init) -> /* reduce by counting/summing occurrences */
reducePrC1(P,r,init);
reducePrC1(P,r/2,res) -> 
| P==[]: res
|: reducePrC1(P[>],r,r(res,P[.]));
reducePrA*(MP,r/2,Lres) -> /* reduce by aggregating occurrences */
| MP==[]: Lres
|: {! Lres = modResPrA(Lres,r(Lres,MP[.]))!}
   reducePrA(MP[>],r,Lres);
modResPrA*(Lres,K) -> 
| K[0]==0: [ [K[1],K[2] ] | Lres ]
|: {! Lres[K[1]][1] += K[2] !} Lres;
redCount(L,mP)-> /* aggregation by counting */
redCount1(L,mP,0);
redCount1(L,l,i)-> 
| L==[]: [0,l,1]
| l==L[0][0]: [1,i,1]
|: redCount1(L[>],l,i+1);
redSum(L,mP)-> /* aggregation by summing */
redSum1(L,mP,0);
redSum1(L,l,i)-> 
| L==[]: [0,l[0],l[1]]
| l[0]==L[0][0]: [1,i,l[1]]
|: redSum1(L[>],l,i+1);

/* cumulative count of laureates over all years */
^reducePrC(fmapPr(prizes,null,null,null,false,false,true,mLauNo),\(x,y)->x+y,0);
countL=ans;
/* total number of prizes available (including non-awarded) over all years */
^reducePrC(fmapPr(prizes,null,null,null,false,false,true,mLauNo),\(x,y)->x+1,0);
/* mean count of laureates for prize over the years */
^countL/ans;
/* total number of awarded prizes over all years */
^reducePrC(fmapPr(prizes,null,null,[1,2,3],false,false,true,mLauNo),\(x,y)->x+1,0);
/* mean count of laureates for awarded prize over the years */
^countL/ans;
/* count of different types of prize shares throughout the years */
^reducePrA(fmapPr(prizes,null,null,null,false,false,true,mLauNo),redCount,[]) %*;
/* number of awarded prizes by category over all years */
^reducePrA(fmapPr(prizes,null,null,[1,2,3],false,true,false,mLauNull),redCount,[]) %*;
nAP= ans;
/* number of awarded laureates by category over all years */
^reducePrA(fmapPr(prizes,null,null,[1,2,3],false,true,true,mLauNo),redSum,[]) %*;
nAL=ans;
/* average number of awarded laureates by category over all years */
avLC(nAP,nAL)-> nAP==[]? []: [ [ nAP[.][0], nAL[.][1]/nAP[.][1] ] | avLC(nAP[>],nAL[>]) ];
^avLC(nAP,nAL) %*;

NOBEL: fmapLau(P,year,cat,lau), mapLau(p), mLau/3;
fmapLau(P,yearL,catL,lauNoL) ->
| P==[]: []
| filter(P[.],yearL,catL,lauNoL): mapLau(P[.]) ++ fmapLau(P[>],yearL,catL, lauNoL)
|: fmapLau(P[>],yearL,catL,lauNoL);
mapLau(p) -> <laureates>
{! laureates= p["laureates"]==null? []: p["laureates"] !}
mLau(laureates,p["year"],p["category"]);
mLau(laureates,year,cat) -> /* add year and category to each laureate */
| laureates==[]: []
|: [ [ laureates[.]["id"], laureates[.]["firstname"],laureates[.]["surname"],year,cat] | mLau(laureates[>],year,cat)];

/* list of all laureates with details on the awarded prizes - a pluri-laureate is listed multiple times */
laureatesL=fmapLau(prizes,null,null,[1,2,3]);
/* first 26 laureates in the list - the ones for year 2023 and 2022 */
^laureatesL[0:25] %*;
/* count of laureates across all categories over the years */
^_len(laureatesL);

NOBEL: reduceLau*(LaureatesL), reduceLaus1*/2, modResLau*/3;
/* reduce by aggregating prize of the same laureate */
reduceLau*(LaureatesL) -> reduceLaus1(LaureatesL,[]);
reduceLaus1*(LausL,Lres) -> 
| LausL==[]: Lres
|: reduceLaus1(LausL[>],modResLau(Lres,LausL[.],Lres));
modResLau*(Lres,l,LresIn) -> 
| Lres==[]: [ [ l[0], l[1], l[2], [ [l[3],l[4]] ] ] | LresIn ]
| Lres[0][0] == l[0]: {! Lres[0][3] ++= [[l[3],l[4]]] !} LresIn
|: modResLau(Lres[>],l,LresIn);
/* list of distinct laureates, grouping possible multiple prizes for the same individual */
laureatesP=reduceLau(laureatesL);

/* number of distinct laureates */
^_len(laureatesP);
kLaureates(L,k) -> 
| L==[]: []
| _len(L[.][3])>=k: [ L[.] | kLaureates(L[>],k) ]
|: kLaureates(L[>],k);
^kLaureates(laureatesP,2);
^kLaureates(laureatesP,3);


/* sorting algorithms */
LIST: new_r/3, isOrdered(L,o/2);
:LIST;
isOrdered(L,o/2)->
| L==[] || L[>]==[]: true
|: o(L[.],L[>][.]) && isOrdered(L[>],o);
new_r(m,minv, maxv)-> /* generate a list of m random int in the range <minv, maxv> */
m<=0? []: [(_rand()*(maxv-minv)+minv) | new_r(m-1,minv, maxv)];
/* lists with 500 and 1000 random double numbers from [10..100000] */
RL500=new_r(500,10,100000); 
RL1000=new_r(1000,10,100000); 

SORT:  bubble*(L,o/2), bub_iter*(L,o/2,last), bub_pass*(L,o/2,i,last,lastSwap);
:SORT;
bubble*(L,o/2) -> L==[] || L[>]==[]? L: bub_iter(L,o,_len(L)-2);
/* repeat scanning of the list to swap any two non-ordered consecutive elements */
bub_iter*(L, o/2, last)-> <lastSwap> 
| last<0: L
|: {! lastSwap=bub_pass(L,o,0,last,0) !} bub_iter(L, o, lastSwap-1); 
/* Swap adjacent out-of-order elements and return the last swap's index */
bub_pass*(L,o/2,i,last,lastSwap)-> 
| i>last: lastSwap
| !o(L[0],L[1]): {! L=LIST.swap(L,0,1) !} bub_pass(L[>],o,i+1,last,i)
|: bub_pass(L[>], o, i+1, last,lastSwap); 

L=bubble(RL500[:],\(x,y)->x<=y); /* run bubble sort */
!clops; /* 147382897 */
L=#null;
L=bubble(RL1000[:],\(x,y)->x<=y); /* run bubble sort */
!clops; /* 582829613 */
^LIST.isOrdered(L,\(x,y)->x<=y); /* check correctness of bubble sort */
L=#null;


SORT: selection*(L,o/2), sel_iter*(L,o/2,L1), sel_min*(o/2,L1,vmin,j,jmin);
selection*(L,o/2)-> L==[] || L[>]==[]? []: sel_iter(L,o,L);
sel_iter*(L,o/2,L1)-> <jmin> /* the scan starts from the begin of the sublist to reduce offset sizes  */
| L1[>]==[]: L
|: {! jmin= sel_min(o,L1[>],L1[.],1,0) & L1=LIST.swap(L1,0,jmin) !}
   sel_iter(L,o,L1[>]); 
sel_min*(o/2,L1,vmin,j,jmin)-> 
| L1==[]: jmin
| o(vmin, L1[.]): sel_min(o,L1[>], vmin,j+1,jmin)
|: sel_min(o,L1[>],L1[.],j+1,j); 

L=selection(RL500[:],\(x,y)->x<=y); /* run selection sort */
!clops; /* 88533923 */
L=#null;
L=selection(RL1000[:],\(x,y)->x<=y); /* run selection sort */
!clops; /* 351834371 */
^LIST.isOrdered(L,\(x,y)->x<=y);
L=#null;


SORT: insertion(L,o/2), ins(L,o/2,O), add(O1,o/2,O,E);
insertion(L,o/2)-> L==[] || L[>]==[]? [] : ins(L[>],o,[L[.]]);
ins(L,o/2,O) -> /* each element E is taken from the input list L and inserted in the output list O */
L==[]? O: ins(L[>],o,add([],o,O,L[.]));
add(O1,o/2,O,E)-> /* the element E is inserted into the correct position of the output ordered list O */
| O==[]: O1++[E]
| o(E,O[.]): O1++[E|O]
|: add(O1++[O[.]],o, O[>],E);

L=insertion(RL500,\(x,y)->x<=y); /* run insertion sort */
!clops; /* 82300630 */
L=#null;
L=insertion(RL1000,\(x,y)->x<=y); /* run insertion sort */
!clops; /* 491541970 */
^LIST.isOrdered(L,\(x,y)->x<=y);
L=#null;

SORT: mergeSort(L,o/2), ms(L,o/2,n), merge(O1,O2,o/2);
mergeSort(L,o/2)-> L==[] || L[>]==[]? L[:]: ms(L,o,_len(L));
ms(L,o/2,n)-> <n2> /* divide the list elements into two sublists of equalę size, recursively order them and then merge the results */ 
{! n2=n//2 !} 
n<=1? L[:]: merge(ms(L[:n2],o,n2), ms(L[n2:], o, n-n2),o);
merge(O1,O2,o/2)-> /* merge of two ordered lists */
| O1==[]: O2[:]
| O2==[]: O1[:]
| o(O1[.],O2[.]): [O1[.] | merge(O1[>],O2,o)] 
|: [O2[.] | merge(O1,O2[>],o)];

L=mergeSort(RL500,\(x,y)->x<=y); /* run mergeSort sort */
!clops; /* 4287155 */
L=#null;
L=mergeSort(RL1000,\(x,y)->x<=y); /* run mergeSort sort */
!clops; /* 9440650 */
^LIST.isOrdered(L,\(x,y)->x<=y);
L=#null;

SORT: quickSort(L,o/2), partition(x,T,T0,T1,o/2);
quickSort(L,o/2)-> <T01> /* partition the tail T of L into two sublists T0 and T1 and recursively perform quicksort on them, leaving the head of L between the two ordered sublists */
| L==[]||L[>]==[]:L[:]
|: {! T01= partition(L[.],L[>],[],[],o) !}
   quickSort(T01[0],o)++[L[.] | quickSort(T01[1],o)];
partition(x,T,T0,T1,o/2)-> /* partition T in the two sublists T0, with elements smaller than x, and T1, with all other elements of T */
| T==[]: [T0,T1] 
| o(T[.],x): partition(x,T[>],[T[.]|T0],T1,o)
|: partition(x,T[>],T0, [T[.]|T1],o);

L=quickSort(RL500,\(x,y)->x<=y); /* run quick sort */
!clops; /* 4539550 */
L=#null;
L=quickSort(RL1000,\(x,y)->x<=y); /* run quick sort */
!clops; /* 9082421 */
^LIST.isOrdered(L,\(x,y)->x<=y);
L=#null;

/* Algorithms for Matrices */

LIST: new_x/2, new_x1/3, mod*/3, prod/2;
:LIST;
new_x(m,x) -> /* create a list with m elements equal to x*/
m <= 0? []: new_x1(m,0,x);
new_x1(m,j,x) -> j >=m? []: [x | new_x1(m,j+1,x)];
mod*(L,i,x) -> {! L[i] = x !} L;
prod(L1,L2) -> /* scalar product of two list L1 and L2  */
| L1==[] && L2==[]: 0
| L1==[] || L2==[]: {! exc(true,"lists non conform for sum") !} null 
|: L1[0]*L2[0]+prod(L1[>],L2[>]);

MATR: new_x/3,new_x1/4, new_r/4, new_r1/5, eye/1, eye1/2, eye2/3;
:MATR;
new_x(n,m,x) -> /* create a matrix M with n x m elements equal to x */
n<=0? []: new_x1(n,0,m,x); 
new_x1(n,i,m,x) -> i>= n? []: [LIST.new_x(m,x) | new_x1(n,i+1,m,x)];
new_r(n,m,minv,maxv) -> /* create a matrix M with n x m random elements */
n<=0? []: new_r1(n,0,m,minv,maxv); 
new_r1(n,i,m,minv,maxv) -> i>= n? []: [LIST.new_r(m,minv,maxv) | new_r1(n,i+1,m,minv,maxv)];
eye(n) -> /* create identity matrix with all 1 in the diagonal and 0 elsewhere*/
n<=0? []: eye1(n,0);
eye1(n,i) -> i>= n? []: [eye2(n,i,0) | eye1(n,i+1)];
eye2(n,i,j) -> j >=n? []: i==j? [1 | eye2(n,i,j+1)]: [0 | eye2(n,i,j+1)];

MATR: swapCol*/3, swapCol1*/4, colM/2, transpose/1, transpose1/3;
swapCol*(M,j1,j2) -> /* exchange  columns j1 and j2 of matrix M */
| M==[] || j1== j2: M
|: swapCol1(M,M,j1,j2);
swapCol1*(M,M1,j1,j2) -> 
| M1==[]: M
|: {! [LIST.swap(M1[0],j1,j2)] !} swapCol1(M,M1[>],j1,j2);
colM(M,j) -> /* deep copy of the column j of matrix  M */
M==[]? []: [M[0][j] | colM(M[>],j)];
transpose(M) -> /* construct the transpose of matrix M */
M==[]? []: transpose1(M,M[0],0);
transpose1(M,C,j) -> C==[]? []: [colM(M,j) | transpose1(M,C[>],j+1)];

M= [ [1, -4], [2,-8], [-11,12] ];
^colM(M,1); /* extract and display column 1 of M */
MT=transpose(M);
^MT %*;
^swapCol(M,0,1)%*; /* swap columns 0 and 1 of M */
M50=new_r(50,50,10,100000); /* matrix with 50 x 50 random elements */
R50= transpose(M50);
!clops; /* 2799646 */
M100=new_r(100,100,10,100000); /* matrix with 100 x 100 random elements */
R100= transpose(M100);
!clops; /* 17128646 */

MATR: prod*/2, prod1*/4, prod2*/3;
prod*(A,B)-> <C> /* product of A (n x k) and B (k x m) */
{! C=new_x(_len(A),_len(B[0]),0) !} 
| _len(A[0])!=_len(B): {! exc(true,"matrices non conformant to product") !} null 
|: prod1(A,transpose(B),C,C);
prod1*(A,BT,C,CC)->  
| C==[]: CC
|: {! [prod2(A,BT,C[.])] !} prod1(A[>],BT,C[>],CC);  
prod2*(A,BT,CR)-> 
| CR==[]: null
|: {! CR[0]=LIST.prod(A[0],BT[0]) !} prod2(A,BT[>],CR[>]);
   
^prod(M,MT)%*;
A50=M50;  
B50=new_r(50,50,10,100000);
C50=prod(A50,B50);
!clops; /* 112643943 */
A100=M100; 
B100=new_r(100,100,10,100000);
C100=prod(A100,B100);
!clops; /* 881840893 */

MATR: isUpperTriang/1, isLowerTriang/1, isTriang1/3, isTriang2/2, isDiagonal/1, makeUpper*/1, makeUpper1*/3, makeUpper2*/3;
isUpperTriang(M) -> /* true if all elements of M below the diagonal are 0 */
isTriang1(M,0,'u');
isLowerTriang(M) -> /* true if all elements of M above the diagonal are 0 */
isTriang1(M,0,'l');
isTriang1(M,i,UL) -> 
M==[] || isTriang2(UL=='u'?M[.]:M[.][>i],i-1) && isTriang1(M[>],i+1,UL);
isTriang2(V,j)-> j<0 || V==[]? true: V[.]==0 && isTriang2(V[>],j-1);
isDiagonal(M) -> /* true if all off-diagonal elements are 0 */
isUpperTriang(M) && isLowerTriang(M);
makeUpper*(M)-> makeUpper1(M,M[>],1);
makeUpper1*(M,M1,i)-> 
| M1==[]: M
|: {! [makeUpper2(M1[.],i,0)] !} makeUpper1(M,M1[>],i+1);
makeUpper2*(R,i,j)-> 
| j>=i: null
|: {! R[0]=0 !} makeUpper2(R[>],i,j+1);

A=[ [ 2, 3, -1 ], [ 0, 1, -1 ], [ 0, 0, 2 ] ];
^A%*;
AT=transpose(A);
^AT%*;
D=[ [ -1, 0, 0 ], [0, 2, 0], [0, 0, 3] ];
^D%*;
^isUpperTriang(A);
^isLowerTriang(AT);
^isDiagonal(D);
XX=[ [1,2,3,4], [5,6,7,8], [9,10,11,12], [13,14,15,16] ];
^makeUpper(XX) %*;

MATR: triangLinearSystem*/2, triangLinearSystem1*/5,triangLinearSystem2/2;
triangLinearSystem*(A,B)-> <n> {! n=_len(A) !} 
| n==0: {! exc(true,"Empty matrix") !} null
| n!=_len(B): {! exc(true,"Vector B non conformant")!} null
|: triangLinearSystem1(A,B,LIST.new_x(n,0),n-1,n);
triangLinearSystem1*(A,B,X,i,n) -> <Ai,Xi> 
| i < 0: X
| n!=_len(A[i]): {! exc(true,"Matrix row non conformant") !} null
| {! Ai =i==0? A[i]: A[i][>i-1] !} {! Xi = i==0? X: X[>i-1] !}
  Ai[0]==0: {! exc(true,"Singular Matrix") !} null 
|: {! Xi[0]=(B[i]-triangLinearSystem2(Ai[>],Xi[>]))/Ai[0] !}
   triangLinearSystem1(A,B,X,i-1,n);
triangLinearSystem2(Ai,Xi)-> Xi==[]? 0: Ai[.]*Xi[.]+triangLinearSystem2(Ai[>],Xi[>]);

A50=new_r(50,50,10,100000);
A50=makeUpper(A50);
B50=LIST.new_r(50,10,100000);
X50=triangLinearSystem(A50,B50);
!clops; /* 900507 */
A100=new_r(100,100,10,100000);
A100=makeUpper(A100);
B100=LIST.new_r(100,10,100000);
X100=triangLinearSystem(A100,B100);
!clops; /* 3394957 */

LIST: jMax/1, jMax1/4, abs/1;
:LIST;
abs(x) -> /* return the absolute value */
x>=0? x: -x;
jMax(V) -> /* index of the V's element with max absolute value */ 
V==[]? -1: jMax1(V[>],1,0,V[0]);
jMax1(V,i,imax,xmax) ->
| V==[]: imax
| abs(V[0])>abs(xmax): jMax1(V[>],i+1,i,V[0])
|: jMax1(V[>],i+1,imax,xmax);

MATR: linearSystem*/2, linearSystem1*/6, triang*/4, triang1*/3;
:MATR;
linearSystem*(A,B)-> <n,AT,BB> /* solve a linear equation system A*X = B 
where A is a matrix n x n and B is an n-dimension vector */
{! n=_len(A) !}
| n==0: {! exc(true,"Empty matrix") !} null
| n!=_len(B): {! exc(true,"Vector B non conformant")!} null
|:{! AT=transpose(A) !} {! BB=B[:] !} linearSystem1(AT,BB,AT,BB,0,n);
linearSystem1*(AT,B,ATC,BC,i,n)-> <imax> 
| ATC[>]==[]: 
   {! exc(ATC[.][i]==0,"singular matrix") !} 
   triangLinearSystem1(transpose(AT),B,LIST.new_x(n,0),n-1,n)
|: {! imax=LIST.jMax1(ATC[.][>i],i+1,i,ATC[.][i]) !}  
   {! [LIST.swap(B,i,imax)] & [swapCol(ATC,i,imax)] !} 
   {! exc(ATC[.][i]==0,"singular matrix") !}
   {! [triang1(BC[.]/ATC[.][i],ATC[.][>i],BC[>])] !} 	
   {! [triang(ATC[.][i],ATC[.][>i],ATC[>],i)] !} 
   linearSystem1(AT,B,ATC[>],BC[>],i+1,n);
triang*(aii,ATCi,ATC,i) -> 
| ATC==[]: null 
|: {! [triang1(ATC[0][i]/aii,ATCi,ATC[.][>i])] !} 
   triang(aii,ATCi,ATC[>],i);
triang1*(vi_aii,ATCi,V) -> 
| V==[]: null 
|: {! V[0]=V[.]-ATCi[.]*vi_aii !} 
   triang1(vi_aii,ATCi[>],V[>]);

A= [ [2, 1,-1], [-3, -1, 2], [-2, 1, 2] ];
B= [8, -11,-3];
X=linearSystem(A,B);
^X;
^prod(A,transpose([X]));


A50=new_r(50,50,10,100000);
B50=LIST.new_r(50,10,100000);
X50=linearSystem(A50,B50);
!clops; /* 39931769 */
A100=new_r(100,100,10,100000);
B100=LIST.new_r(100,10,100000);
X100=linearSystem(A100,B100);
!clops; /* 288993585 */

MATR: det*/1, det1*/5, triangDet/1, triangDet1/2;
det*(A)-> <n,AT> /* compute the determinant of a square matrix */
{! n=_len(A) !} 
| n==0 || n != _len(A[0]): {! exc(true,"matrix non conform") !} null
|: {! AT=transpose(A) !} det1(AT,AT,0,n,1);
det1*(AT,ATC,i,n,signDet) -> <imax> 
| ATC[>]==[]: ATC[.][i]==0? 0: signDet*triangDet(AT)
| {! imax=LIST.jMax1(ATC[.][>i],i+1,i,ATC[.][i]) !} 
  {! [swapCol(ATC,i,imax)] !} 
  {! signDet= imax==i? signDet: -signDet !} 
  ATC[.][i]==0: 0
|: {! [triang(ATC[.][i],ATC[.][>i],ATC[>],i)] !}
	det1(AT,ATC[>],i+1,n,signDet);
triangDet(M)-> /* product of all diagonal elements of a Matrix M */
triangDet1(M,0);
triangDet1(M,i)-> M==[]? 1: M[.][i]*triangDet1(M[>],i+1); 

^A%*;
^det(A);
M= [ [1, 2, -3], [2, 4, -6], [-2,-4,6] ];
^det(M);

D50=det(A50);
!clops; /* 35289599 */
D100=det(A100);
!clops; /* 264693813 */

MATR: rank*/1, rank1*/4;
rank*(A)-> <n> /* return the rank of a square matrix A  */
{! n=_len(A) !} 
| n==0 || n != _len(A[0]): {! exc(true,"non square matrix") !} null
|: rank1(transpose(A),0,n,n);
rank1*(ATC,i,n,rankMat) -> <imax> 
| ATC[>]==[]: ATC[.][i]==0? rankMat-1: rankMat
| {! imax=LIST.jMax1(ATC[.][>i],i+1,i,ATC[.][i]) !} 
  {! [swapCol(ATC,i,imax)] !} 
  ATC[.][i]==0: rank1(ATC[>],i+1,n,rankMat-1)
|: {! [triang(ATC[.][i],ATC[.][>i],ATC[>],i)] !}
	rank1(ATC[>],i+1,n,rankMat);
^rank(A);
^rank(M);
^rank(A50);
!clops; /* 35231227 */
^rank(A100); /* 264527277 */
!clops;

MATR: inv*/1, inv1*/6, inv_triang*/4, inv_triang1*/3;
inv*(A) -> <n,AT,I> /* return the inverse of a square, non-singular matrix A  */
{! n=_len(A) !} 
|	n==0 || n != _len(A[0]): {! exc(true, "non square matrix") !} null 
|:  {! AT=transpose(A) !} {! I = eye(n) !} transpose(inv1(AT,AT,I,I,0,n));
inv1*(AT,ATC,AIT,AITC,i,n) ->  <imax>
| ATC[>]==[]: 
    {! exc(ATC[.][i]==0,"singular matrix") !} 
    inv_triang1(AT,AIT,n-1)
|:	{! imax=LIST.jMax1(ATC[.][>i],i+1,i,ATC[.][i]) !}  
	{! [swapCol(AIT,i,imax)] & [swapCol(ATC,i,imax)] !} 
	{! exc(ATC[.][i]==0,"singular matrix") !}
 	{! [inv_triang(ATC[.][i],ATC[.][>i],AITC,i)] !} 	
	{! [triang(ATC[.][i],ATC[.][>i],ATC[>],i)] !} 
	inv1(AT,ATC[>],AIT,AITC,i+1,n);
inv_triang*(aii,ATCi,AITC,i) -> 
| AITC==[]: null
|: {! [triang1(AITC[0][i]/aii,ATCi,AITC[.][>i])] !} 
   triang(aii,ATCi,AITC[>],i);
inv_triang1*(AT,AIT,n1) -> 
| AIT==[]: []
|: [ triangLinearSystem1(transpose(AT),AIT[.],LIST.new_x(n1+1,0),n1,n1+1) | inv_triang1(AT,AIT[>],n1) ];

AI=inv(A);
^AI%*;
^prod(A,AI);
^inv(M);
AI50=inv(A50);
!clops; /* 276314003 */
AI100=inv(A100);
!clops; /* 2742529427 */

/* Finite State Automata */
AUT1: rec/1, q0/3, q1/3, q2/3, q3/3, q4/3;
:AUT1;
rec(S) -> q0(S,0,_len(S));
q0(S,i,n) -> i < n && S[i]=='a' && q1(S,i+1,n);
q1(S,i,n) -> i < n && S[i] =='b' && q2(S,i+1,n);
q2(S,i,n) ->
| i >= n: false
| S[i]=='a': q1(S,i+1,n)
| S[i]=='b' || S[i]=='d': q3(S,i+1,n)  
| S[i]=='c': q4(S,i+1,n)
|: false;
q3(S,i,n) -> 
| i >= n: false
| S[i]=='b' || S[i]=='d': q3(S,i+1,n)  
| S[i]=='c': q4(S,i+1,n) 
|: false;
q4(S,i,n) -> i>=n || ( S[i]=='c' && q4(S,i+1,n) );

^rec("ababbdbbcc");
^rec("abdc");
^rec("abadc");
^rec("abbdb");

AUT1L: rec/1, q0/1, q1/1, q2/1, q3/1, q4/1;
:AUT1L;
rec(S) -> q0(S@list);
q0(L) -> L!=[] && L[.]== 'a' && q1(L[>]);
q1(L) -> L!=[] && L[.]=='b' && q2(L[>]);
q2(L) ->
| L==[]: false
| L[.]=='a': q1(L[>])
| L[.]=='b' || L[.]=='d': q3(L[>])  
| L[.]=='c': q4(L[>])
|: false;
q3(L) -> 
| L==[]: false
| L[.]=='b' || L[.]=='d': q3(L[>])  
| L[.]=='c': q4(L[>]) 
|: false;
q4(L) -> L==[] || ( L[.]=='c' && q4(L[>]) );

^rec("ababbdbbcc");
^rec("abdc");
^rec("abadc");
^rec("abbdb");

AUTF: rec/1, q0/1, q1/1, q2/1, q3/1, q4/1, q5/1, q6/1, q7/1;
:AUTF;
rec(S) -> q0(S@list);
q0(L) -> 
| L==[]: false
| CHAR.isDigit(L[.]): q4(L[>])
| L[.]== '+'|| L[.]== '-': q2(L[>])
| L[.]== '.': q1(L[>])
|: false;
q1(L) -> L!=[] && CHAR.isDigit(L[.]) && q3(L[>]);
q2(L) -> 
| L==[]: false
| CHAR.isDigit(L[.]): q4(L[>])
| L[.]=='.': q1(L[>])
|: false;
q3(L) -> 
| L==[]: true
| CHAR.isDigit(L[.]): q3(L[>]) 
| L[.]== 'e': q5(L[>])
|: false;
q4(L) -> 
| L==[]: true 
| CHAR.isDigit(L[.]): q4(L[>]) 
| L[.]== 'e': q5(L[>]) 
| L[.]== '.': q3(L[>])
|: false;
q5(L) -> 
| L==[]: false
| CHAR.isDigit(L[.]): q6(L[>]) 
| L[.]== '+'|| L[.]== '-': q7(L[>])
|: false;
q6(L) -> L==[] || ( CHAR.isDigit(L[.]) && q6(L[>]) );
q7(L) -> L!=[] && CHAR.isDigit(L[.]) && q6(L[>]);

^rec("+2.232e-5");
^rec(".024");
^rec("+.024");
^rec("e+2");
^rec(".");

UAUT: rec/3, uq/4, next/3;
:UAUT;
rec(F,G,S) -> uq(F,G,S@list,0);
uq(F,G,L,q) -> 
| q <0: false
| L==[]: LIST.member(q,F)
|:  uq(F,G,L[>],next(G,L[.],q));
next(G,x,q) -> 
| G==[]: -1
| G[.][0]==x && G[.][1]==q: G[.][2]
|: next(G[>],x,q);

F=[4];
G=[ ['a',0,1], ['a',2,1], ['b',1,2], ['b',2,3], ['b',3,3], ['c',2,4], ['c',3,4], ['c',4,4], ['d',2,3], ['d',3,3] ];
^rec(F,G,"ababbdbbcc");
^rec(F,G,"abdc");
^rec(F,G,"abadc"); 
^rec(F,G,"abbdb");

modInpFP(L) -> 
| L==[]: []
| CHAR.isDigit(L[.]): ['d' | modInpFP(L[>]) ]
| L[.]=='+' || L[.]=='-': ['s' | modInpFP(L[>]) ]
|: [L[.] | modInpFP(L[>]) ];
recFP(F,G,S) -> uq(F,G,modInpFP(S@list),0);

F=[3,4,6];
G =[ ['d',0,4], ['d',1,3], ['d',2,4], ['d',3,3], ['d',4,4], ['d',5,6], ['d',6,6], ['d',7,6], ['s',0,2], ['s',5,7], ['.',0,1], ['.',2,1], ['.',4,3], ['e',3,5], ['e',4,5] ];
^recFP(F,G, "+2.232e-5");
^recFP(F,G,".024");
^recFP(F,G,"+.024");
^recFP(F,G,"e+2");
^recFP(F,G,".");

CHAR: isSpace/1; CHAR.isSpace(x) -> x==' ';
DATE: MonthDays, Day, Month, Year, isMonthDay*/0, isYear*/0, rec*/1,
q0*/1, q1*/1, q2*/1, q3*/1, q4*/1, q5*/1, q6*/1;
:DATE;
MonthDays = { "JANUARY": 31, "FEBRUARY": 28, "MARCH": 31, 
    "APRIL": 30, "MAY": 31, "JUNE": 30, "JULY": 31, 
    "AUGUST": 31, "SEPTEMBER": 30, "OCTOBER": 31,
    "NOVEMBER": 30, "DECEMBER": 31};
isMonthDay*() -> 
| !_isKey(MonthDays,Month): {! exc(true,"wrong month") !} false 
| MonthDays[Month]<Day || Day==0: {! exc(true,"wrong day") !} false 
|: true;
isYear*() -> 
| 1900 <= Year && Year <= 2030: true
|: {! exc(true,"wrong year") !} false;
rec*(S) -> q0(S@list);
q0*(L) ->	
| L==[]: false
| CHAR.isSpace(L[.]): q0(L[>])
| CHAR.isDigit(L[.]): {! Day=L[.]-'0' !} q1(L[>])
|: false;
q1*(L) ->
| L==[]: false
| CHAR.isDigit(L[.]): {! Day=Day*10+L[.]-'0' !} q1(L[>]) 
| CHAR.isSpace(L[.]): q2(L[>])
|: false;
q2*(L) -> 	
| L==[]: false
| CHAR.isSpace(L[.]): q2(L[>])
| CHAR.isLetter(L[.]): {! Month = CHAR.toUpperCase(L[.])@string !} q3(L[>])
|: false;
q3*(L) ->
| L==[]: false
| CHAR.isLetter(L[.]):  {! Month++=CHAR.toUpperCase(L[.])@string !} q3(L[>])
| CHAR.isSpace(L[.]): {! [isMonthDay()] !} q4(L[>])
|: false;
q4*(L) ->
| L==[]: false
| CHAR.isSpace(L[.]): q4(L[>]) 
| CHAR.isDigit(L[.]): {! Year=L[.]-'0'!} q5(L[>])
|: false;
q5*(L) -> 	
| L==[]:  {! exc(!isYear(),"wrong year") !} true
| CHAR.isDigit(L[.]): {! Year=Year*10+L[.]-'0'!} q5(L[>]) 
| CHAR.isSpace(L[.]): {! [isYear()] !} q6(L[>])
|: false;
q6*(L) -> L==[] || CHAR.isSpace(L[.]) && q6(L[>]);

^rec(" 31 june 2017");
^rec(" 0 june 2017 ");
^rec(" 30 juny 2017 ");
^rec(" 30 june 1017 ");
^rec(" 30 june 2017 ");

CHAR: isDigit/1,isSpace/1; 
CHAR.isDigit(x) -> x>='0' && x<='9';
CHAR.isSpace(x) -> x==' ';

DOUBLEV: IsToken, Sign, Val, Vi, Vd, Ve, Ltail, val*/1, q0*/2, q1*/1, q2*/1, q3*/1, q4*/1, q5*/1, q6*/1, q7*/1, valToken*/1;
:DOUBLEV;
val*(S) -> <isDouble> {! isDouble=q0(S@list,false) !} 
| isDouble: Val
|: {! exc(true,"wrong double")!} null;
q0*(L,isToken) -> {! IsToken=isToken & Vi=0 & Vd=0 & Ve=0 & Sign=1!}
| L==[]: false
| CHAR.isDigit(L[.]): {! Vi=L[.]-'0' !} q4(L[>]) {! Val=(Vi+Vd)*_pow(10,Ve) !}
| L[.]== '+': q2(L[>]) {! Val=(Vi+Vd)*_pow(10,Ve) !}
| L[.]== '-': q2(L[>]) {! Val=-(Vi+Vd)*_pow(10,Ve) !}
| L[.]== '.': q1(L[>]) {! Val=Vd*_pow(10,Ve) !}
|: false;
q1*(L) -> 
| L==[]: false 
| CHAR.isDigit(L[.]): q3(L[>]) {! Vd=(Vd+(L[.]-'0'))/10 !}
|: false;
q2*(L) -> 
| L==[]: false
| CHAR.isDigit(L[.]): {! Vi=L[.]-'0' !} q4(L[>])
| L[.]=='.': q1(L[>])
|: false;
q3*(L) -> 
| L==[]: {! Ltail=[] !} true
| CHAR.isDigit(L[.]): q3(L[>]) {! Vd=(Vd+(L[.]-'0'))/10 !}
| L[.]== 'e': q5(L[>])
| IsToken: {! Ltail=L !} true
|: false;
q4*(L) -> 
| L==[]: {! Ltail=[] !} true 
| CHAR.isDigit(L[.]): {! Vi=10*Vi+(L[.]-'0') !} q4(L[>]) 
| L[.]== 'e': q5(L[>]) 
| L[.]== '.': q3(L[>])
| IsToken: {! Ltail=L !} true
|: false;
q5*(L) -> 
| L==[]: false
| CHAR.isDigit(L[.]): {! Ve=L[.]-'0' !} q6(L[>]) 
| L[.]== '+': q7(L[>])
| L[.]== '-': q7(L[>]) {! Ve=-Ve !}
|: false;
q6*(L) -> 
| L==[]: {! Ltail=[] !} true
|  CHAR.isDigit(L[.]): {! Ve=10*Ve+(L[.]-'0') !} q6(L[>])
| IsToken: {! Ltail=L !} true
|: false;
q7*(L) -> 
| L==[]: {! Ltail=[] !} true
| CHAR.isDigit(L[.]): {! Ve=L[.]-'0' !} q6(L[>])
| IsToken: {! Ltail=L !} true
|: false;
valToken*(S) -> <isDouble> {! isDouble=q0(S@list,true) !} 
          isDouble? Val: null;

EXPR_EXEC: Val, IsExp, exec*/1, q0*/1, q1*/1, q2*/2;
:EXPR_EXEC;
exec*(S) -> {! IsExp=q0(S@list) !} 
| IsExp: Val
|: {! exc(true,"wrong expression")!} null;
q0*(L) ->  <DOUBLEV*>
| L==[]: false
| CHAR.isSpace(L[.]): q0(L[>]) 
| DOUBLEV.q0(L,true): {! Val=DOUBLEV.Val !} q1(DOUBLEV.Ltail)
|: false;
q1*(L) -> <DOUBLEV*>
| L==[]: true
| CHAR.isSpace(L[.]): q1(L[>])
| L[.]=='+' || L[.]=='-': q2(L[>],L[.])
|: false;
q2*(L,s) -> <DOUBLEV*>
| L==[]: false
| CHAR.isSpace(L[.]): q2(L[>],s)  
| DOUBLEV.q0(L,true): {! Val=s=='+'?Val+DOUBLEV.Val: Val-DOUBLEV.Val !} 
                      q1(DOUBLEV.Ltail)
|: false;      
^exec("22e-2+3+.11e+2+4.-10.1");
^exec("22e-2+3+-.11e+2+4.-10.1");
^exec("-22e-2+3+.11e+2+4.-10.1");
^exec("  -22e-2 +   3 + +.11e+2 + 4. -10.1  ");
^exec("22e-2+3+.e+2+4.-10.1");
^exec("22e-2+3*2");
        
EXPR_TRANSL: Code, IsExp, exec*/1, q0*/1, q1*/1, q2*/2;
:EXPR_TRANSL;
exec*(S) -> {! IsExp=q0(S@list) !} 
| IsExp: Code
|: {! exc(true,"wrong expression")!} null;
q0*(L) ->  <DOUBLEV*>
| L==[]: false
| CHAR.isSpace(L[.]): q0(L[>]) 
| DOUBLEV.q0(L,true): {! Code=[ ["PUSH",DOUBLEV.Val] ] !} q1(DOUBLEV.Ltail)
|: false;
q1*(L) -> <DOUBLEV*>
| L==[]: true {! Code++=[ ["PRINT",0] ] !}
| CHAR.isSpace(L[.]): q1(L[>])
| L[.]=='+' || L[.]=='-': q2(L[>],L[.])
|: false;
q2*(L,s) -> <DOUBLEV*>
| L==[]: false
| CHAR.isSpace(L[.]): q2(L[>],s)  
| DOUBLEV.q0(L,true): {! Code++=[ ["PUSH",DOUBLEV.Val] ] !}
                      {! Code++= s=='+'?[ ["SUM",0] ]: [ ["SUB",0] ] !} 
                      q1(DOUBLEV.Ltail)
|: false;      

code=exec("22e-2+3+.11e+2+4.-10.1");
^Code%*;


SIMPLEM: Out, Stack, exec*/1, exec1*/1, execOne*/1; 
:SIMPLEM;
exec*(C) -> {! Stack=[] & Out=[]!} exec1(C);
exec1*(C) -> 
| C==[]: Out
|: {! [execOne(C[.])] !} exec1(C[>]);
execOne*(I) ->  
| I[0]=="PUSH": {! Stack=[I[1]|Stack] !} true
| I[0]=="SUM": {! exc(Stack==[]||Stack[>]==[],"missing operand(s) for SUM") !}
				{! Stack[1]+=Stack[0] & Stack=Stack[>] !} true
| I[0]=="SUB": {! exc(Stack==[]||Stack[>]==[],"missing operand(s) for SUB") !}
				{! Stack[1]-=Stack[0] & Stack=Stack[>] !} true
| I[0]=="PRINT": {! exc(Stack==[],"missing operand for PRINT") !}
				{! Out++=[Stack[0]] & Stack=Stack[>] !} true
|: {! exc(true,"undefined instruction") !} false;

^exec(code);
^exec(EXPR_TRANSL.exec("22e-2+3+.11e+2+4.-10.1"));




 
