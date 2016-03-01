#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <ctype.h>
#include <jni.h>

#include "../include/CodemonGUI.h"
#include "mainParser.tab.h"
#include "test.h"
#include "../include/common.h"
#include "../include/parser.h"
#include "../include/arch.h"

struct Codemon_pkg *parseBinary(const char *filename);

/**
 * Fetches the report requested and saves it to a given file.
 * reportNumJ   : a number representing the requested report
 * outFilenameJ : a file path to the output file
 * RETURNS      : a status message if something went wrong, or NULL otherwise.
 */
JNIEXPORT jstring JNICALL Java_CodemonGUI_getReport (JNIEnv *env, jobject obj, jstring reportNumJ, jstring outFilenameJ)
{

	// JNI conversion
	const char *outFilename = (*env)->GetStringUTFChars(env, outFilenameJ, NULL);
	const char *reportNum = (*env)->GetStringUTFChars(env, reportNumJ, NULL);
	const char *returnString;
	char tempReturnString[STRBUFF];
	FILE *destFile;
	int reportStatus;

	// Fetch the report and save it to file.
	if (!(destFile = fopen(outFilename, "wb")))
	{
		return (*env)->NewStringUTF(env, strerror(errno));
	}

	reportStatus = getReport(atoi(reportNum), destFile);
	fclose(destFile);

	// Did the report come out correctly?
	if (reportStatus == 1)
		return NULL;
	else {
		tempReturnString[0] = ' ';
		strcat(tempReturnString, "Could not fetch report #");
		strcat(tempReturnString, reportNum);
		returnString = tempReturnString;
		return (*env)->NewStringUTF(env, returnString);	
	}
}




/**
 * Attempts to run a test on the server.
 * filenameJ : a Java string with a filename
 * numTurns  : an integer for the number of turns we'll run the test.
 * RETURNS   : an error string, or the report ID if A-OK.
 */

JNIEXPORT jstring JNICALL Java_CodemonGUI_sendTest (JNIEnv *env, jobject obj, jstring filenameJ, jint numTurns)
{
	// JNI conversion
	const char *filename = (*env)->GetStringUTFChars(env, filenameJ, NULL);
	const char *returnString;

	// Does the file have the appropriate extension?
	if (!(strlen(filename) > strlen(CODEMONEXT) && strcmp(&filename[strlen(filename) - strlen(CODEMONEXT)], CODEMONEXT) == 0))
	{
		returnString = "File must have a \".codemon\" file extension!";
		return (*env)->NewStringUTF(env, returnString);
	}

	// Try to pass in the codemon.
	struct Codemon_pkg *p1;
	if ((p1 = parseBinary(filename)) == NULL)
	{
		returnString =  "Could not open that file!";
		free(p1);
		return (*env)->NewStringUTF(env, returnString);
	}

	int status = runTest(p1, NULL, numTurns);
	free(p1);
	
	// If there was an issue connecting -- assuming the
	// appropriate checks went through with parseBinary()
	if (!status)
	{
		returnString = "Could not connect to server with that codemon!";
		return (*env)->NewStringUTF(env, returnString);
	}

	char temp[STRBUFF];
	sprintf(temp, "%d", status);
	return (*env)->NewStringUTF(env, temp);
}


/**
 * Attempts to run a test battle on the server.
 * filenameJ  : a Java string with a filename
 * filename2J : a Java  string with a second filename
 * numTurns   : an integer for the number of turns we'll run the test
 * RETURNS   : an error string, or the report ID if A-OK.
 */
JNIEXPORT jstring JNICALL Java_CodemonGUI_sendTestBattle (JNIEnv *env, jobject obj, jstring filenameJ, jstring filename2J, jint turnNum)
{

	// JNI conversion
	const char *filename = (*env)->GetStringUTFChars(env, filenameJ, NULL);
	const char *filename2 = (*env)->GetStringUTFChars(env, filename2J, NULL);
	const char *returnString;

	// Do the files have the appropriate extension?
	if (!(strlen(filename) > strlen(CODEMONEXT) && strcmp(&filename[strlen(filename) - strlen(CODEMONEXT)], CODEMONEXT) == 0 && strlen(filename2) > strlen(CODEMONEXT) && strcmp(&filename2[strlen(filename2) - strlen(CODEMONEXT)], CODEMONEXT) == 0))
	{
		returnString = "All files must have a \".codemon\" file extension!";
		return (*env)->NewStringUTF(env, returnString);
	}
	
	// Run the blob on the server.
	else 
	{
		
		// Try to pass in the codemon.
		struct Codemon_pkg *p1;
		if ((p1 = parseBinary(filename)) == NULL)
		{
			returnString = "Could not open file 1!";
			free(p1);
			return (*env)->NewStringUTF(env, returnString);
		}
		struct Codemon_pkg *p2;
		if ((p2 = parseBinary(filename2)) == NULL)
		{
			returnString = "Could not open file 2!";
			free(p2);
			return (*env)->NewStringUTF(env, returnString);	
		}
		int status = runTest(p1, p2, turnNum);

		free(p1);
		free(p2);
		
		// If there was an issue connecting -- assuming the
		// appropriate checks went through with parseBinary()
		if (!status)
		{
			returnString = "Could not connect to server with that codemon!";
			return (*env)->NewStringUTF(env, returnString);
		}
		
		char temp[STRBUFF];
		sprintf(temp, "%d", status);
		return (*env)->NewStringUTF(env, temp);
	}
}


/**
 * Attempts to send a codemon for a PvP match on the server.
 * filenameJ  : a Java string with a filename
 * numPlayers : an integer for the type of PvP match, 2-4.
 * RETURNS   : an error string, or the report ID if A-OK.
 */
JNIEXPORT jstring JNICALL Java_CodemonGUI_sendBattle (JNIEnv *env, jobject obj, jstring filenameJ, jint numPlayers)
{

	// JNI integration	
	const char *filename = (*env)->GetStringUTFChars(env, filenameJ, NULL);
	const char *returnString;
	
	// Does the file have the appropriate extension?
	if (!(strlen(filename) > strlen(CODEMONEXT) && strcmp(&filename[strlen(filename) - strlen(CODEMONEXT)], CODEMONEXT) == 0))
	{
		returnString = "File must have a \".codemon\" file extension!";
		return (*env)->NewStringUTF(env, returnString);
	}

	// Run the blob on the server.
	else 
	{
		// Try to pass in the codemon.
		struct Codemon_pkg *p1;
		if ((p1 = parseBinary(filename)) == NULL)
		{
			free(p1);
			returnString = "Could not open that file!";
			return (*env)->NewStringUTF(env, returnString);
		}
		int status = runPvP(p1, numPlayers);
		free(p1);
			
		// If there was an issue connecting -- assuming the
		// appropriate checks went through with parseBinary()
		if (!status)
		{
			returnString = "Could not connect to server with that codemon!";
			return (*env)->NewStringUTF(env, returnString);
		}
		
		// Otherwise, give back the report ID to the user.
		char temp[STRBUFF];
		sprintf(temp, "%d", status);
		return (*env)->NewStringUTF(env, temp);
	}
}



//------------------------------------------------------------------------------------


/**
 * Attempts to parse the input file. If errors are found in its structure,
 * it identifies them and passes the message along to the stack, which should
 * pass the message along to the user. 
 *
 * filenameJ :	a Java string describing the location of the file on disk
 * RETURNS   :	a status Java String message describing an error, or NULL if A-OK. 
 */
JNIEXPORT jstring JNICALL Java_CodemonGUI_parseTextFile (JNIEnv *env, jobject obj, jstring filenameJ, jstring destFileJ)
{ 
	FILE *file, *destFile;
	extern FILE *main_in, *main_out;
	int i, j, k, l;
	char 	c, 
		initString[STRBUFF * MAXINST],
		tempInst[MAXINST + 2][STRBUFF + 3]; // +1 for begin@, and another for buffer.
	extern char GLOBMESSAGE[STRBUFF];

	// JNI conversion	
	const char *filename = (*env)->GetStringUTFChars(env, filenameJ, NULL);
	const char *destFilename = (*env)->GetStringUTFChars(env, destFileJ, NULL);

	if (!(file = fopen(filename, "r"))) {
		return (*env)->NewStringUTF(env, strerror(errno));
	}

	if (!(destFile = fopen(destFilename, "wb"))) {
		return (*env)->NewStringUTF(env, strerror(errno));
	}

	// Check that we'll have enough room to store this file.
	fseek(file, 0L, SEEK_END);
	if (ftell(file) > (STRBUFF * MAXINST) - 1) {
		fclose(main_in);
		fclose(main_out);
		return (*env)->NewStringUTF(env, parseTextErrors(EFTOOBIG));
	}
	fseek(file, 0L, SEEK_SET);


	// Go through parsing and assemble.
	main_in = file;
	main_out = destFile;
	fflush(NULL);

	if (main_parse() != 0) {
		
		fclose(main_in);
		fclose(main_out);
		return (*env)->NewStringUTF(env, GLOBMESSAGE);
	}

	
	fclose(main_in);
	fclose(main_out);
	
	return NULL;
}






/**
 * Attempts to read in a .codemon file, and sets up the appropriate structure
 * and its members so that it is in a presentable format for the server.
 *
 * NOTE: return struct must meet specification laid out by runTest()!
 *
 * filename : 	a string describing the location of the file on disk
 * RETURNS  : 	a Codemon_pkg struct reference with the data if 
 * 		successful, and a NULL pointer otherwise
 */
struct Codemon_pkg *parseBinary(const char *filename)
{
	int i, num = 0, slash = 0;
	FILE *file;
	uint32 size;

	// Creating our package.
	struct Codemon_pkg *codemon = malloc(sizeof(struct Codemon_pkg));
	
	if (!(file = fopen(filename, "rb"))) // "rb" for Windows portability.
	{
		return NULL;
	}

	// Preliminary check to see if the file is not corrupt.
	fseek(file, 0L, SEEK_END);
	if ((size = ftell(file)) % 8 != 4)
	{
		fclose(file);
		return NULL;
	}
	fseek(file, 0L, SEEK_SET);

	// Name?
	for (i = 0; i < strlen(filename); i++)
	{
		if (filename[i] == '.')
		{
			num = i;
			break;
		}
	}
	if (num > MAXNAME - 1) 
		num = MAXNAME - 1;
	char name[MAXNAME];
	strncpy(name, filename, num);

	name[num] = '\0';
	printf("%s", name);
	strcpy(codemon->name, name);

	// Read in the instructions.
	codemon->lines = (size - 4) / 8;
	fread(&(codemon->begin), sizeof(uint32), 1, file);
	fread(&(codemon->program), sizeof(uint64), codemon->lines, file);
	fclose(file);

	return codemon;
}


