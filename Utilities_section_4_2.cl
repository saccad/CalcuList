/***** UTILITIES *****/

swap*(L, i, j) : /* swap elements i and j of the list L */ 
<tmp> {! tmp=L[i] !} {! L[i]=L[j]!} {! L[j]=tmp !} true;

numeric(x) : x@type==int || x@type==double || x@type==char;

MATH: /* label MATH for the Boolean variable err*/
err;

div*(x, y) : /* it computes the division of x by y and set MATH.err to true if y is zero or x and/or y are not numeric */
<MATH*> {! err=false !} !numeric(x) || !numeric(y) || y==0? 0 {! err=true !} : x/y;

/***** END OF UTILITIES *****/

