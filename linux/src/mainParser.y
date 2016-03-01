%{
	#include <stdio.h>
	#include <stdlib.h>
	#include <string.h>
	#include "test.h"
	#include "../include/common.h"
	#include "../include/arch.h"
	
	extern int labelsInTable;
	extern char labelNames[MAXINST][259];
	extern int labelNum[MAXINST];
	extern FILE *main_in;
	extern FILE *main_out;

	int i, inst_number, found, fields_used;
	uint32 begin;
	uint64 instruction, imode1, imode2, opcode, dummy;
	char mode1 = '&';
	char mode2 = '&'; 
	char GLOBMESSAGE[STRBUFF];

	int yylex(void);
	void yyerror(const char *input) {
		mode1 = '&';
		mode2 = '&';	
		fflush(NULL);
		strcpy(GLOBMESSAGE, input);
	}

%}

%token <cr> MODE
%token <temp> LABEL NUM SEMICOLON COMMA STAR PLUS MINUS MOD DIVIDE LPAR RPAR OPCODE BEGINDEC

%union { int n; char cr; char *temp; }
%type<n> expression mterm brackets field base
%start program
%define api.prefix {main_}
	

%%

program:	start_line lines		
		;

lines:		lines line | line			
		;

start_line:	BEGINDEC expression SEMICOLON 		
		{ 
			// For the begin instruction.
			begin = (uint64) $2;
			fflush(main_out);
			fwrite(&begin, sizeof(uint32), 1, main_out);
		}
		;

line:		OPCODE SEMICOLON			
		{ 
			// For when no fields are provided.
			opcode = parseOpcode($1);
			free($1);
			if (opcode < 1) {
				yyerror("Invalid opcode.");
			}
			instruction = 0;
			imode1 = -1;
			imode2 = -1;

			if (checkModes(opcode, imode1, imode2) < 0) {
				
				yyerror("Invalid mode combination.");
				YYERROR;
			}
			fields_used = checkFields(opcode, 0);
			if (fields_used < 0) {
				yyerror("Missing fields.");
				YYERROR;
			}

			instruction |= (opcode << 58);
			dummy = 1;
			instruction |= (dummy << 54);
			dummy = 0;
			instruction |= (dummy << 29);
			dummy = 1;
			instruction |= (dummy << 25);
			instruction |= 0;

			fflush(main_out);
			fwrite(&instruction, sizeof(uint64), 1, main_out);
			mode1 = '&';
			mode2 = '&';
		}
		|
		OPCODE field SEMICOLON	
		{ 
			// For when one field is provided.
			opcode = parseOpcode($1);
			free($1);
			if (opcode < 1) {
				yyerror("Invalid opcode.");
				YYERROR;
			}	
			instruction = 0;
			imode1 = -1;
			imode2 = -1;
			if (mode1 != '&') 
			{
				imode1 = parseMode(mode1);
				if (imode1 < 0) {
					yyerror("Invalid mode 1 for A-field.");
					YYERROR;
				}
			}
			else
				imode1 = -1;
			if (mode2 != '&')
			{
				imode2 = parseMode(mode2);
				if (imode2 < 0) {
					yyerror("Invalid mode 2 for B-field.");
					YYERROR;
				}
			}
			else 
				imode2 = -1;

			if (checkModes(opcode, imode1, imode2) < 0) {
				yyerror("Invalid mode combination.");
				YYERROR;
			}
			fields_used = checkFields(opcode, 1);
			if (fields_used < 0) {
				yyerror("Missing fields.");
				YYERROR;
			}


			instruction |= (opcode << 58);
			if (fields_used != 2) 
			{
				instruction |= (imode1 << 54);
				instruction |= ($2 << 29);
			}
			else 
			{
				dummy = 1;
				instruction |= (dummy << 54);
				dummy = 0;
				instruction |= (dummy << 29);
			}
			if (fields_used != 1) 
			{
				instruction |= (imode2 << 25);
				instruction |= $2;
			}
			else 
			{
				dummy = 1;
				instruction |= (dummy << 25);
				instruction |= 0;
			}
			
	
		
			fflush(main_out);
			fwrite(&instruction, sizeof(uint64), 1, main_out);
			mode1 = '&';
			mode2 = '&';
		}
		|
		OPCODE field COMMA field SEMICOLON	
		{ 
			// For when two fields are provided.
			opcode = parseOpcode($1);
			free($1);
			if (opcode < 0) {

				yyerror("Invalid opcode.");
				YYERROR;
			}
			instruction = 0;
			imode1 = -1;
			imode2 = -1;
			if (mode1 != '&') 
			{
				imode1 = parseMode(mode1);
				if (imode1 < 0) {
					yyerror("Invalid mode for A-field.");
					YYERROR;
				}
			}
			if (mode2 != '&')
			{
				imode2 = parseMode(mode2);
				if (imode2 < 0) {
					yyerror("Invalid mode for B-field.");
					YYERROR;
				}
			}

			if (checkModes(opcode, imode1, imode2) < 0) {
				yyerror("Invalid mode combination.");
				YYERROR;
			}
			fields_used = checkFields(opcode, 2);
			if (fields_used < 0) {
				yyerror("Missing fields.");
				YYERROR;
			}

			
			instruction |= (opcode << 58);
			if (fields_used != 2) 
			{
				uint64 temp1 = imode1;
				instruction |= (temp1 << 54);
				temp1 = $2;
				instruction |= (temp1 << 29);
			}
			else 
			{
				dummy = 1;
				instruction |= (dummy << 54);
				dummy = 0;
				instruction |= (dummy << 29);
			}
			if (fields_used != 1) 
			{
				uint64 temp2 = imode2;
				instruction |= (temp2 << 25);
				temp2 = $4;
				instruction |= temp2;
			}
			else 
			{
				dummy = 1;
				instruction |= (dummy << 25);
				instruction |= 0;
			}

			fflush(main_out);
			fwrite(&instruction, sizeof(uint64), 1, main_out);
			mode1 = '&';
			mode2 = '&';
		}
		;

field:		expression				
		{	
			// No mode provided.
			if (mode1 == '&')
				mode1 = '$';
			else if (mode2 == '&')
				mode2 = '$';
			else {
				yyerror("Programming error! Shouldn't see this.");
				YYERROR;
			}
			$$ = $1;
		}
		| 
		STAR expression	
		{ 
			// Special case for the star.
			if (mode1 == '&')
				mode1 = '*';
			else if (mode2 == '&')
				mode2 = '*';
			else {
				yyerror("Programming error! Shouldn't see this.");
				YYERROR;
			}
			$$ = $2;
			
		}
		|
		MODE expression
		{
			// When a mode is provided.
			if (mode1 == '&')
				mode1 = $1;
			else if (mode2 == '&')
				mode2 = $1;
			else {
				yyerror("Programming error! Shouldn't see this.");
				YYERROR;
			}
			$$ = $2;
				
		}
		;

expression:	mterm
		{
			$$ = $1;
		}
		|
		expression PLUS mterm	
		{
			$$ = $1 + $3;
		}
		|
		expression MINUS mterm
		{ 
			$$ = $1 - $3;
		}
		;
		

mterm: 		brackets
		{
			$$ = $1;
		}
		|
		mterm STAR brackets
		{ 
			$$ = $1 * $3;
		}
		|
		mterm DIVIDE brackets
		{ 
			if ($3 == 0) {
				yyerror("Division by zero!");
				YYERROR;
			}
			$$ = $1 / $3;								
		}
		|
		mterm MOD brackets
		{
			$$ = $1 % $3;
		}
		;

brackets:	base
		{
			$$ = $1;
		}
		|
		LPAR expression RPAR		
		{ 
			$$ = $2; 

		}
		|
		MINUS LPAR expression RPAR
		{
			$$ = -$3;
		}
		;
		


base:		LABEL			
		{ 
			found = 0;
			for (i = 0; i < labelsInTable; i++)
			{
				if (strcmp(labelNames[i], main_lval.temp) == 0)
				{
					found++;
					$$ = convertNegative(labelNum[i] - 1);
				}
				
			}	
			free(main_lval.temp);
			if (!found) {
				yyerror("Label definition not found.");
				YYERROR;
			}
		}
		|
		NUM		
		{ 
			$$ = convertNegative(atoi(main_lval.temp));	
			free(main_lval.temp);
		}
		;

%%
