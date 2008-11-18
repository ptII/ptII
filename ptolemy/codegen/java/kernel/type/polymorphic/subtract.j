/*** subtract_Array_Array() ***/
inline Token subtract_Array_Array(Token a1, Token a2) {
    return $Array_subtract(a1, a2);
}
/**/

/*** subtract_Array_Double() ***/
inline Token subtract_Array_Double(Token a1, double a2) {
    int i;
    Token result = $new(Array(a1.payload.Array->size, 0));

    for (i = 0; i < a1.payload.Array->size; i++) {
        Array_set(result, i, $subtract_Token_Double(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** subtract_Array_Integer() ***/
inline Token subtract_Integer_Array(Token a1, int a2) {
    int i;
    Token result = $new(Array(a1.payload.Array->size, 0));

    for (i = 0; i < a1.payload.Array->size; i++) {
        Array_set(result, i, $subtract_Token_Integer(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** subtract_Array_Long() ***/
inline Token subtract_Long_Array(Token a1, long long a2) {
    int i;
    Token result = $new(Array(a1.payload.Array->size, 0));

    for (i = 0; i < a1.payload.Array->size; i++) {
        Array_set(result, i, $subtract_Token_Long(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** subtract_Boolean_Boolean() ***/
inline boolean subtract_Boolean_Boolean(boolean a1, boolean a2) {
    return a1 | a2;
}
/**/

/*** subtract_Boolean_Integer() ***/
inline int subtract_Boolean_Integer(boolean a1, int a2) {
    //return $subtract_Integer_Boolean(a2, a1);
}
/**/

/*** subtract_Double_Array() ***/
Token subtract_Double_Array(double a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $subtract_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** subtract_Double_Double() ***/
inline double subtract_Double_Double(double a1, double a2) {
    return a1 - a2;
}
/**/

/*** subtract_Double_Integer() ***/
inline double subtract_Double_Integer(double a1, int a2) {
    return a1 - a2;
}
/**/

/*** subtract_Double_Token() ***/
Token subtract_Double_Token(double a1, Token a2) {
    Token token = $new(Double(a1));
    return $subtract_Token_Token(token, a2);
}
/**/

/*** subtract_Integer_Array() ***/
Token subtract_Integer_Array(int a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $subtract_Integer_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** subtract_Integer_Boolean() ***/
int subtract_Integer_Boolean(int a1, boolean a2) {
    return a1 - (a2 ? 1 : 0);
}
/**/

/*** subtract_Integer_Integer() ***/
int subtract_Integer_Integer(int a1, int a2) {
    return a1 - a2;
}
/**/

/*** subtract_Integer_Token() ***/
int subtract_Integer_Token(int a1, Token a2) {
    Token token = $new(Integer, a1);
    return $typeFunc(TYPE_Integer::subtract(token, a2));
}
/**/

/*** subtract_Long_Array() ***/
Token subtract_Long_Array(long long a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $subtract_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** subtract_Long_Long() ***/
inline long long subtract_Long_Long(long long a1, long long a2) {
    return a1 - a2;
}
/**/

/*** subtract_Long_Token() ***/
Token subtract_Long_Token(long long a1, Token a2) {
    Token token = $new(Long(a1));
    return $subtract_Token_Token(token, a2);
}
/**/

/*** subtract_Token_Double() ***/
inline Token subtract_Token_Double(Token a1, double a2) {
    Token token = $new(Double(a2));
    return $subtract_Token_Token(a1, token);
}
/**/

/*** subtract_Token_Integer() ***/
inline int subtract_Token_Integer(Token a1, int a2) {
    Token token = $new(Integer, a2);
    return $typeFunc(TYPE_Integer::subtract(a1, token));
}
/**/

/*** subtract_Token_Token() ***/
inline Token subtract_Token_Token(Token a1, Token a2) {
    return $tokenFunc(a1::subtract(a2));
}
/**/

