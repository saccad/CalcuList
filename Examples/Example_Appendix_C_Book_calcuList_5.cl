/ Examples of assembler text file

/ ** First Program
$BEGIN
INIT	1 	/ one global variable (GV0)
LOADGV	0 	/ address of GV0
SLIST		/ start a list
PUSHI -5	
HLIST		/ -5 is the first list element (head) 
PUSHI	100	
CLIST		/ 100 is the second list element
PUSHD  	3.14		
CLIST		/ 3.14 is the third list element
ELIST		/ end of the list
DUPL		/ duplicate the list address
PRINT	1	/ print the list with print option "%*"
MODV		/ assign the list address to GV0
HALT
$END

/ ** Second Program
$BEGIN
INIT	1	/ one global variable (GV0) with value [-5,100,3.14]
PUSHF	100	/ function sum
STOREF		/ store it
LOADGV	0 	/ address of GV0  
DEREF		/ value of GV0
GETF		/ get function sum
CALL		/ launch sum
PRINT	0	/ print the result with standard print option
HALT
100	START	0	/ first instruction of sum
LOADPAR	1		/ load first sum parameter (L)
DEREF			/ address of L
SLIST			/ empty list []
ELIST
EQ				/ compare L and []
JUMPZ	200		/ if different goto label 21
PUSHI	0		/ L is empty list
RETURN 1		/ return the value 0
200	LOADPAR	1	/ load parameter L
DEREF			/ address of L
HEAD			/ value of L head (L[.])
PUSHF	100		/ function sum
STOREF			/ store it
LOADPAR	1		/ load parameter L
DEREF			/ address of L
TAIL			/ tail of L (L[>])
GETF			/ get function sum
CALL			/ launch sum
ADD				/ L[.] + sum(L[>])
RETURN	1		/ return the result of the sum
$END