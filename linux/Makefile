CC=gcc
CFLAGS=-Wall -shared -Wl,-soname,libCodemonGUI.so
EXE=Codemon-GUI
LIB=libCodemonGUI.so
OBJ=$(SRC)/parser.o $(SRC)/client.o $(SRC)/mainParser.tab.o $(SRC)/lex.main_.o $(SRC)/test.o
SRC=src
INCLUDE=include


all: $(LIB) java
	echo java -cp ./src Run > $(EXE)
	chmod a+x ./$(EXE)

$(LIB): $(OBJ) $(SRC)/CodemonGUI.java
	gcc -shared -fPIC -Wl,-soname,libCodemonGUI.so -I/usr/lib/jvm/oracle-jdk-bin-1.8/include -I/usr/lib/jvm/oracle-jdk-bin-1.8/include/linux -o $(SRC)/libCodemonGUI.so $(OBJ)
	
java: $(SRC)/CodemonGUI.java $(SRC)/Run.java $(SRC)/CodemonVisualization.java	
	javac $(SRC)/CodemonGUI.java $(SRC)/CodemonVisualization.java $(SRC)/Run.java 

$(SRC)/parser.o: $(SRC)/parser.c $(INCLUDE)/CodemonGUI.h $(SRC)/mainParser.tab.h $(SRC)/test.h $(INCLUDE)/common.h $(INCLUDE)/parser.h $(INCLUDE)/arch.h
	gcc -c $(SRC)/parser.c

$(SRC)/client.o: $(SRC)/client.c $(INCLUDE)/common.h $(INCLUDE)/arch.h
	gcc -c $(SRC)/common.c

$(SRC)/mainParser.tab.o: $(SRC)/mainParser.tab.c $(SRC)/test.h $(INCLUDE)/common.h $(INCLUDE)/arch.h
	gcc -c $(SRC)/mainParser.tab.c

$(SRC)/lex.main_.o: $(SRC)/lex.main_.c $(SRC)/mainParser.tab.h $(SRC)/test.h $(INCLUDE)/common.h
	gcc -c $(SRC)/lex.main_.c

$(SRC)/test.o: $(SRC)/test.c $(SRC)/test.h $(INCLUDE)/common.h $(INCLUDE)/arch.h
	gcc -c $(SRC)/test.c


clean: $(EXE)
	rm -f $(EXE)
	rm -f $(SRC)/lib*.so
	rm -f $(SRC)/*$$*.class

