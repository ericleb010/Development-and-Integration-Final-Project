%{
	#include <string.h>
	#include <stdlib.h>
	#include "mainParser.tab.h"
	#include "../include/common.h"
	#include "test.h"
	
	int instIndex, i;
	int labelsInTable;
	char labelNames[MAXINST][STRBUFF];
	int labelNum[MAXINST];

%}



%option caseless 
%option prefix="main_"
%option noyywrap
%x comment second
lab	[0-9]*[a-z0-9]+

%%


<INITIAL>[ \t\n]+		{ }
<INITIAL>^[ \t\n]*{lab}:	{
					i = 0;
					labelNum[labelsInTable] = instIndex; 
					while (isspace(yytext[i])) {
						i++;
					}
					strncpy(labelNames[labelsInTable], &yytext[i], strlen(yytext) - i - 1); 
					labelsInTable++; 
				}
<INITIAL>;			{ instIndex++; }
<INITIAL>.			{ }
<INITIAL><<EOF>>		{ BEGIN second; instIndex = 0; fseek(yyin, 0L, SEEK_SET); }


<second>!				{ BEGIN comment; }
<comment>\n				{ BEGIN second; }
<comment>.+				{ }


<second>(dat)|(mov)|(add)|(sub)|(mul)|(div)|(mod)|(jmp)|(jmz)|(jmn)|(djn)|(seq)|(sne)|(slt)|(set)|(clr)|(frk)|(nop)|(rnd)			{ main_lval.temp = strdup(yytext); return OPCODE; }

<second>[#\$\[\]\@\{\}\<\>]		{ main_lval.cr = yytext[0]; return MODE; }
<second>begin\@				{ return BEGINDEC; }
<second>(\-)?[0-9]+			{ main_lval.temp = strdup(yytext); return NUM; }
<second>{lab}				{ main_lval.temp = strdup(yytext); return LABEL; }
<second>;				{ instIndex++; return SEMICOLON; }

<second>\+				{ return PLUS; }
<second>\-				{ return MINUS; }
<second>\*				{ return STAR; }
<second>\/				{ return DIVIDE; }
<second>\%				{ return MOD; }
<second>\(				{ return LPAR; }
<second>\)				{ return RPAR; }
<second>,				{ return COMMA; }


<second>[\t ]+				{ }
<second>{lab}:				{ }





