/***constantsBlock***/

// Constants.
#define MISSING 0
#define boolean unsigned char

/* Infinity is a valid Ptolemy identifier. */
#define Infinity HUGE_VAL

#ifdef linux
/* Linux tends to have NAN. */
#define NaN (__builtin_nanf (""))
#else /*linux*/
#define NaN nanf(0)
#endif /*linux*/

#define false 0
#define true 1

/**/

/***funcHeaderBlock ($function)***/

Token $function (Token thisToken, ...);
/**/

/***tokenDeclareBlock ($types)***/

// Token structure containing the specified types.
struct token {         // Base type for tokens.
    char type;         // TYPE field has to be the first field.
    union typeMembers {
        // type member declarations [i.e. Type1Token Type1;]
        $types
    } payload;
};
/**/


/***convertPrimitivesBlock***/
#define StringtoInt atoi
#define StringtoDouble atof
#define StringtoLong atol
#define DoubletoInt floor
#define InttoDouble (double)
#define InttoLong (long)

char* InttoString (int i) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%d", i);
    return string;
}

char* LongtoString (long long l) {
    char* string = (char*) malloc(sizeof(char) * 22);
    sprintf(string, "%lld", l);
    return string;
}

char* DoubletoString (double d) {
    int index;
    char* string = (char*) malloc(sizeof(char) * 20);
    sprintf(string, "%.14g", d);

        // Make sure that there is a decimal point.
    if (strrchr(string, '.') == NULL) {
        index = strlen(string);
        if (index == 20) {
            string = (char*) realloc(string, sizeof(char) * 22);
        }
        string[index] = '.';
        string[index + 1] = '0';
        string[index + 2] = '\0';
    }
    return string;
}

char* BooleantoString (boolean b) {
    char *results;
    if (b) {
        // AVR does not have strdup
        results = (char*) malloc(sizeof(char) * 5);
        strcpy(results, "true");
    } else {
        results = (char*) malloc(sizeof(char) * 6);
        strcpy(results, "false");
    }
    return results;
}

char* UnsignedBytetoString (unsigned char b) {
    char* string = (char*) malloc(sizeof(char) * 3);
    sprintf(string, "%d", (int) b);
    return string;
}

/**/

/*** unsupportedTypeFunction ***/
/* We share one method between all types so as to reduce code size. */
Token unsupportedTypeFunction(Token token, ...) {
    fprintf(stderr, "Attempted to call unsupported method on a type.\n");
    exit(1);
    return emptyToken;
}
/**/

/*** scalarDeleteFunction ***/
/* We share one method between all scalar types so as to reduce code size. */
Token scalarDelete(Token token, ...) {
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return emptyToken;
}
/**/
