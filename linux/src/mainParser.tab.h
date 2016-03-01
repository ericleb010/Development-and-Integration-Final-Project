/* A Bison parser, made by GNU Bison 3.0.4.  */

/* Bison interface for Yacc-like parsers in C

   Copyright (C) 1984, 1989-1990, 2000-2015 Free Software Foundation, Inc.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.

   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

#ifndef YY_MAIN_MAINPARSER_TAB_H_INCLUDED
# define YY_MAIN_MAINPARSER_TAB_H_INCLUDED
/* Debug traces.  */
#ifndef MAIN_DEBUG
# if defined YYDEBUG
#if YYDEBUG
#   define MAIN_DEBUG 1
#  else
#   define MAIN_DEBUG 0
#  endif
# else /* ! defined YYDEBUG */
#  define MAIN_DEBUG 0
# endif /* ! defined YYDEBUG */
#endif  /* ! defined MAIN_DEBUG */
#if MAIN_DEBUG
extern int main_debug;
#endif

/* Token type.  */
#ifndef MAIN_TOKENTYPE
# define MAIN_TOKENTYPE
  enum main_tokentype
  {
    MODE = 258,
    LABEL = 259,
    NUM = 260,
    SEMICOLON = 261,
    COMMA = 262,
    STAR = 263,
    PLUS = 264,
    MINUS = 265,
    MOD = 266,
    DIVIDE = 267,
    LPAR = 268,
    RPAR = 269,
    OPCODE = 270,
    BEGINDEC = 271
  };
#endif

/* Value type.  */
#if ! defined MAIN_STYPE && ! defined MAIN_STYPE_IS_DECLARED

union MAIN_STYPE
{
#line 35 "mainParser.y" /* yacc.c:1909  */
 int n; char cr; char *temp; 

#line 82 "mainParser.tab.h" /* yacc.c:1909  */
};

typedef union MAIN_STYPE MAIN_STYPE;
# define MAIN_STYPE_IS_TRIVIAL 1
# define MAIN_STYPE_IS_DECLARED 1
#endif


extern MAIN_STYPE main_lval;

int main_parse (void);

#endif /* !YY_MAIN_MAINPARSER_TAB_H_INCLUDED  */
