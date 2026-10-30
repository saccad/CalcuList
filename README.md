# CalcuList

**Current release: 5.0.0** (April 24, 2026)

CalcuList (Calculator with List manipulation) [1] [2] is an educational language for teaching functional programming extended with some imperative and side-effect features, which are enabled under explicit request by the programmer. In addition to strings and  lists, the language natively supports json objects.

The language adopts a Python-like syntax and enables interactive computation sessions with the user through a REPL (Read-Evaluate-Print-Loop) shell. The object code produced by a compilation is a program that will be eventually executed by the CalcuList Virtual Machine (CLVM).

The language supports higher order functions, which  may be effectively used to implement generic MapReduce  recursive procedures to manipulate json lists. As MapReduce is a popular model in distributed computing that underpins many NoSQL systems and a json list can be thought of as a dataset of a document NoSQL datastore, CalcuList can be used for teaching advanced query algorithms for document datastores such as MongoDB and CouchDB.

###### Running CalcuList
Download `CalcuList_R_5_0_0.jar` from the [Releases](https://github.com/saccad/CalcuList/releases) page and run it with Java 8 or later:

    java -jar CalcuList_R_5_0_0.jar

The CLVM assembler is distributed as `CLVM_ASM_R_5_0_0.jar`. The files `Utilities.cl` and `Utilities_chapter_7.cl` contain utility functions; the `.dat` files are sample datasets used in the examples.

###### References
[1] [Domenico Saccà, Angelo Furfaro: CalcuList: a Functional Language Extended with Imperative Features. CoRR abs/1802.06651 (2018)](https://arxiv.org/abs/1802.06651)

[2] [Domenico Saccà, Angelo Furfaro: Using CalcuList To MapReduce Jsons. IDEAS 2018: 74-83](https://dl.acm.org/citation.cfm?doid=3216122.3216164)
