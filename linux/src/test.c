#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <ctype.h>

#include "../include/common.h"
#include "test.h"
#include "../include/arch.h"


/* and determines if it is a valid combination. If it is, it returns 
 * which fields (A or B) the value corresponds to.
 *
 * inst    :	an integer describing the opcode
 * fields  : 	an integer, 0 - 2, describing the fields parsed in
 * RETURNS :	an integer, 1 - 3, which tells us if field A or B was 
 * 		used, or neither (3). Returns -1 if it is invalid.
 */
int checkFields(int inst, int fields)
{
	switch (inst)
	{
		case 0x01:
		case 0x02:
		case 0x03:
		case 0x04:
		case 0x05:
		case 0x06:
		case 0x08:
		case 0x09:
		case 0x0A:
		case 0x0B:
		case 0x0C:
		case 0x0D:
		case 0x12:
			if (fields != 2)
				return -1;
			return 0;
		case 0x00:
			if (fields < 1)
				return -1;
			if (fields == 2)
				return 0;
			return 2;
		case 0x07:
		case 0x10:
			if (fields < 1)
				return -1;
			if (fields == 2)
				return 0;
			return 1;
		default:
			if (fields == 2)
				return 0;
			if (fields == 1)
				return 1;
			return 3;
	}
}


/**
 * Takes in the instruction and modes parsed and determines if
 * the combination is valid according to spec.
 *
 * inst    : 	an integer, describing the instruction
 * amode   : 	an integer, describing the mode for the A field
 * bmode   :	an integer, describing the mode for the B field
 * RETURNS :	an exit status; 0 if OK, -1 if invalid.
 */
int checkModes(int inst, int amode, int bmode)
{
	switch (inst) 
	{
		case 0x00:
			if ((amode >= 0 && amode != 0x01) || bmode != 0x01)
				return -1;
			break;
		case 0x07:
		case 0x08:
		case 0x09:
		case 0x0A:
			if (amode == 0x01)
				return -1;
			break;
	}
	return 0;
}

/**
 * A helper function to convert an opcode from text to integer.
 *
 * inst    : 	a string, the opcode
 * RETURNS : 	a long long, the appropriate value for the opcode
 */
int parseOpcode(char *inst)
{
	int i;

	for (i = 0; i < strlen(inst); i++)
	{
		inst[i] = toupper(inst[i]);
	}

	if (strcmp(inst, "DAT") == 0)
		return 0;
	else if (strcmp(inst, "MOV") == 0)
		return 1;
	else if (strcmp(inst, "ADD") == 0)
		return 2;
	else if (strcmp(inst, "SUB") == 0)
		return 3;
	else if (strcmp(inst, "MUL") == 0)
		return 4;
	else if (strcmp(inst, "DIV") == 0)
		return 5;
	else if (strcmp(inst, "MOD") == 0)
		return 6;
	else if (strcmp(inst, "JMP") == 0)
		return 7; 
	else if (strcmp(inst, "JMZ") == 0)
		return 8;
	else if (strcmp(inst, "JMN") == 0)
		return 9;
	else if (strcmp(inst, "DJN") == 0)
		return 10;
	else if (strcmp(inst, "SEQ") == 0)
		return 11;
	else if (strcmp(inst, "SNE") == 0)
		return 12;
	else if (strcmp(inst, "SLT") == 0)
		return 13;
	else if (strcmp(inst, "SET") == 0)
		return 14;
	else if (strcmp(inst, "CLR") == 0)
		return 15;
	else if (strcmp(inst, "FRK") == 0)
		return 16;
	else if (strcmp(inst, "NOP") == 0)
		return 17;
	else if (strcmp(inst, "RND") == 0)
		return 18;
	else 
		return -1;
}


/**
 * A helper function to convert a mode from its character to its value.
 *
 * t       : 	a character, the textual representation of the mode
 * RETURNS :	a long long, the appropriate value for the mode.
 */
int parseMode(char t)
{
	switch (t)
	{
		case '$':
			return 0;
		case '#':
			return 1;
		case '[':
			return 2;
		case ']':
			return 3;
		case '*':
			return 4;
		case '@':
			return 5;
		case '{':
			return 6;
		case '}':
			return 7;
		case '<':
			return 8;
		case '>':
			return 9;
		default:
			// Not a mode character.
			return -1;
	}
}

/**
 * Prints an error message corresponding to the errno provided.
 *
 * status  : an integer corresponding to an error status.
 * RETURNS : a string corresponding to an error, or NULL if A-OK.
 */
const char *parseTextErrors(int status)
{
	const char *returnString;

	switch(status) {
		case 0:
			return NULL;
			break;
		case ENOENT:
		case ENOTDIR:
			returnString = "Could not find the file requested!";
			break;
		case ENOBEGI:
			returnString = "Input should have a begin instruction on the first line!";
			break;
		case ENOBEGN:
			returnString = "Input is missing a value after the \"begin@\" declaration!";
			break;
		case ETOOMANY:
			returnString = "Input has an invalid number of instructions! Must be from 1 to 50!";
			break;
		case EINVBEGN:
			returnString = "The begin declaration value is invalid! Did you put in a number?";
			break;
		case EFTOOBIG:
			returnString = "The file is too big! Max 50 instructions!";
			break;
		case EITOOBIG:
			returnString = "One of your instructions is way too long!";
			break;
		case ELABDUP:
			returnString = "You've declared a label name twice!";
			break;
		case EINVOPC:
			returnString = "You've entered an invalid opcode!";
			break;
		case ESPCOPC:
			returnString = "Expected a three-character opcode, followed by whitespace!";
			break;
		case EINVINS:
			returnString = "You've entered an incomplete instruction somewhere. Did you forget something?";
			break;
		case ENLABDEF:
			returnString = "One of the labels used was never defined! (If you have no labels, did you type characters when you weren't supposed to?)";
			break;
		case ENOCOMMA:
			returnString = "Expected a comma after the A field.";
			break;
		case EFLDMISS:
			returnString = "There's a field missing for one of the opcodes you specified!";
			break;
		case EINVMODE:
			returnString = "One of the addressing modes selected is invalid for the instruction provided!";
			break;
		default:
			returnString = "Could not open that file! Did you type it correctly? Do you have permission to read the file?";
			break;
	}

	return returnString;
}

/**
 * Prints out the help documentation for this program.
 */ 

void printHelp() 
{	
printf("\nUSAGE:\ncodemon <option>\n\nOPTIONS:\n\t-c <filename>\n\t\tAllows the user to import and convert a .codemon program \n\t\tinto a binary blob for submission to the server.\n\t-t <filename> <limit>\n\t\tAllows the user to send a binary blob to the server for test execution, \n\t\tgiven a number of turns.\n\t-s <filename1> <filename2> <limit>\n\t\tAllows the user to submit two binary blobs to the server for testing \n\t\tagainst each other.\n\t-p <N> <filename>\n\t\tAllows the user to submit a binary blob to the server in a PvP mode \n\t\tof their choice (2, 3, or 4).\n\t-r <reportid>\n\t\tAllows the user to retrieve a given report from the server. Each \n\t\treport can only be accessed once.\n\n");
}

/**
 * Converts a provided negative integer as per specification.
 * 
 * RETURNS : the positive conversion of the integer. Returns itself if non-negative.
 */
uint64 convertNegative(int num)
{
	// Check if it is negative.
	if (num < 0)
	{
		// Need to convert based on available memory.
		return num += MEMSIZE;
	}
	return num;
}

/**
 * Checks a given string to see if it is composed purely of digits.
 *
 * str     : the string to be checked
 * RETURNS : an exit status -- negative if str is not a number, 0 otherwise
 */
int checkForDigits(char *str)
{
	int i;

	for (i = 0; i < strlen(str); i++)
	{
		if (!isdigit(str[i]) && str[i] != '-')
			return -1;
	}
	return 0;
}


