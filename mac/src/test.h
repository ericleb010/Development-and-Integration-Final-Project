#ifndef PARSER_H
#define PARSER_H

// CONSTANTS
#define STRBUFF         259
#define NUMBUFF         15

// ERRNO EXTENDED
#define ENOBEGI         35
#define ENOBEGN         36
#define ETOOMANY        37
#define EINVBEGN        38
#define EFTOOBIG        39
#define EITOOBIG        40
#define ELABDUP         41
#define EINVOPC         42
#define ESPCOPC         43
#define EINVINS         44
#define ENLABDEF        45
#define ENOCOMMA        46
#define EFLDMISS        47
#define EINVMODE        48

#include "../include/arch.h"

// PROTOTYPES
int checkModes(int inst, int amode, int bmode);
int parseOpcode(char *inst);
int parseMode(char t);
const char *parseTextErrors(int status);
void printHelp(void);
uint64 convertNegative(int num);
int checkFields(int inst, int fields);
int checkForDigits(char *str);

#endif
