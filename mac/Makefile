CC=gcc
CFLAGS=-mmacosx-version-min=10.10 -w -dynamiclib -framework JavaVM
EXE=Codemon-GUI
LIB=libCodemonGUI.jnilib
SRC=src
INCLUDE=include


all: $(LIB) java
	echo java -cp ./src Run > $(EXE)
	chmod a+x ./$(EXE)

$(LIB): $(SRC)/parser.c $(SRC)/client.c $(SRC)/mainParser.tab.c $(SRC)/lex.main_.c $(SRC)/test.c $(INCLUDE)/parser.h $(INCLUDE)/common.h $(INCLUDE)/arch.h $(INCLUDE)/CodemonGUI.h $(SRC)/mainParser.tab.h $(SRC)/CodemonGUI.java
	gcc $(CFLAGS) -I/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/include -I/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/include/darwin src/test.c src/client.c src/parser.c src/mainParser.tab.c $(SRC)/lex.main_.c -o $(LIB)
	
java: $(SRC)/CodemonGUI.java $(SRC)/Run.java $(SRC)/CodemonVisualization.java
	javac $(SRC)/CodemonGUI.java $(SRC)/CodemonVisualization.java $(SRC)/Run.java

clean: $(EXE)
	rm -f $(EXE)
	rm -f $(SRC)/lib*.jnilib
	rm -f $(SRC)/*$$*.class

