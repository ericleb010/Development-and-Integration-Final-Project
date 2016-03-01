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

// PROTOTYPES
JNIEXPORT jstring JNICALL Java_CodemonGUI_sendTest (JNIEnv *env, jobject obj, jstring filenameJ, jint numTurns);
JNIEXPORT jstring JNICALL Java_CodemonGUI_sendTestBattle (JNIEnv *env, jobject obj, jstring filenameJ, jstring filename2J, jint turnNum);
JNIEXPORT jstring JNICALL Java_CodemonGUI_sendBattle (JNIEnv *env, jobject obj, jstring filenameJ, jint numPlayers);
JNIEXPORT jstring JNICALL Java_CodemonGUI_parseTextFile (JNIEnv *env, jobject obj, jstring filenameJ, jstring destFileJ);
JNIEXPORT jstring JNICALL Java_CodemonGUI_getReport (JNIEnv *env, jobject obj, jstring reportNumJ, jstring outFilenameJ);

int32 parseTextFile(char *filename);
struct Codemon_pkg *parseBinary(const char *filename);

#endif
