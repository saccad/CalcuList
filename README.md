# CalcuList

**Current release: 5.0.0** (April 24, 2026)

**CalcuList** (Calculator with List manipulation) is a compact functional programming language that serves as a complete case study in programming language design: how to extend a functional language with controlled, inspectable side effects, enabled only under explicit request by the programmer, by grounding them in the same attribute-grammar semantic actions that already define its static semantics. The full design, from formal syntax and semantics to implementation, is described in the book [1].

CalcuList was originally conceived as an educational language for teaching functional programming [2], and it retains this role. In addition to strings and lists, the language natively supports JSON objects.

The language adopts a Python-like syntax and enables interactive computation sessions through a REPL (Read-Evaluate-Print-Loop) shell. The object code produced by a compilation is a program that is eventually executed by the CalcuList Virtual Machine (CLVM).

The language supports higher-order functions, which may be effectively used to implement generic MapReduce recursive procedures to manipulate JSON lists. As MapReduce is a popular model in distributed computing that underpins many NoSQL systems, and a JSON list can be thought of as a dataset of a document NoSQL datastore, CalcuList can be used for teaching advanced query algorithms for document datastores such as MongoDB and CouchDB [3, 4].

### Running CalcuList

Download `CalcuList_R_5_0_0.jar` from the [Releases](https://github.com/saccad/CalcuList/releases) page and run it with Java 8 or later:

```
java -jar CalcuList_R_5_0_0.jar
```

Optional arguments set the sizes (in words) of the memory arrays MEM, OUTPUT and CODE, for example `java -jar CalcuList_R_5_0_0.jar EXECCMS 512000 EXECOS 128000`; see Appendix D of the book [1].

The CLVM assembler is distributed as `CLVM_ASM_R_5_0_0.jar`. The files `Utilities.cl` and `Utilities_chapter_7.cl` contain utility functions; the `.dat` files are sample datasets used in the examples.

### License

Software: GPL-3.0 (see LICENSE). The book is released under CC BY-NC-ND 4.0.

### References

[1] Saccà, D.: *CalcuList: A Functional Programming Language with Controlled Side Effects*. First edition (2026). Free PDF: [DOI 10.5281/zenodo.22129890](https://doi.org/10.5281/zenodo.22129890). Paperback: ISBN 9791224347903, [Amazon.it](https://www.amazon.it/dp/B0HJXPY2HD).

[2] Saccà, D., Furfaro, A.: CalcuList: a Functional Language Extended with Imperative Features. arXiv preprint [arXiv:1802.06651](https://arxiv.org/abs/1802.06651) (2018).

[3] Saccà, D., Furfaro, A.: Using CalcuList To MapReduce Jsons. In: Proceedings of the 22nd International Database Engineering and Applications Symposium (IDEAS 2018), Villa San Giovanni, Italy, June 18–20, 2018, pp. 74–83. ACM (2018).

[4] Saccà, D., Furfaro, A.: Using CalcuList To MapReduce Json Documents. In: Proceedings of the 27th Italian Symposium on Advanced Database Systems (SEBD 2019), Castiglione della Pescaia, Italy, June 16–19, 2019. [CEUR Workshop Proceedings, vol. 2400](https://ceur-ws.org/Vol-2400/) (2019).

