Assignment 3
Eric Leblanc (#0948541)



USAGE:

- Set the class path!
	In a command line, run 
		$ export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:./src:.

- Run make!

- Execute the batch file!
	In a command line, run
		$ ./Codemon-GUI

And you're off!


-----------------------------------------------------------------------------------------


INCLUDED FILES:


Makefile
	The target file for compilation initialization. Run "make" to compile
	or your system, and "make clean" to remove the resulting executable.

src/parser.c 
	An assortment of functions which allows the program to parse input
	files, generate meaningful output, and send the result to the server for process.

src/test.c
	A poorly named modularization of parser.c

src/client.c
	A provided collection of networking routines for connecting to the
	server.

src/lex.main_.c
	The lexer with which we tokenize all source text.

src/mainParser.tab.c / .h
	The parser with which we process and analyse all tokens.

src/CodemonGUI.java
	A single source file for the Swing GUI provided to the user.

src/CodemonVisualization.java
	A separate source file for the visualization JFrame.

src/Run.java
	Just a small main() class for the Swing GUI.



There are other source files for the icons used in the build.



include/parser.h
	A header file for the constants and function prototypes used in
	parser.c and main.c

include/common.h
	A header file for the constants and structures used in the source
	code.

include/arch.h
	A header file establishing certain typedefs used for portability
	purposes in the source code.

include/CodemonGUI.h
	A header file for the JNI integration with the native C code.


-----------------------------------------------------------------------------------------
