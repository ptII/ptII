/*** equals_Array_Array() ***/
boolean equals_Array_Array(Token a1, Token a2) {
    return $Array_equals(a1, a2);
}
/**/

/*** equals_Array_Double() ***/
boolean equals_Array_Double(Token a1, double a2) {
    return $equals_Double_Array(a2, a1);
}
/**/

/*** equals_Array_Int() ***/
boolean equals_Array_Int(Token a1, int a2) {
    return $equals_Int_Array(a2, a1);
}
/**/

/*** equals_Array_Long() ***/
boolean equals_Array_Long(Token a1, long long a2) {
    return $equals_Long_Array(a2, a1);
}
/**/

/*** equals_Boolean_Boolean() ***/
boolean equals_Boolean_Boolean(boolean a1, boolean a2) {
        // logical comparison.
    return (!a1 == !a2);
}
/**/

/*** equals_Boolean_Int() ***/
boolean equals_Boolean_Int(boolean a1, int a2) {
    return $equals_Int_Boolean(a2, a1);
}
/**/

/*** equals_Boolean_String() ***/
boolean equals_Boolean_String(boolean a1, char* a2) {
    return $equals_String_Boolean(a2, a1);
}
/**/

/*** equals_Double_Array() ***/
boolean equals_Double_Array(double a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $equals_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** equals_Double_Double() ***/
boolean equals_Double_Double(double a1, double a2) {
    return a1 == a2;
}
/**/

/*** equals_Double_Int() ***/
boolean equals_Double_Int(double a1, int a2) {
    return a1 == (double) a2;
}
/**/

/*** equals_Double_String() ***/
boolean equals_Double_String(double a1, char* a2) {
    return $equals_String_Double(a2, a1);
}
/**/

/*** equals_Double_Token() ***/
boolean equals_Double_Token(double a1, Token a2) {
    Token token = $new(Double(a1));
    return $equals_Token_Token(token, a2);
}
/**/

/*** equals_Int_Array() ***/
boolean equals_Int_Array(int a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $equals_Int_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** equals_Int_Boolean() ***/
boolean equals_Int_Boolean(int a1, boolean a2) {
    return $equals_String_Int(a2, a1);
}
/**/

/*** equals_Int_Int() ***/
boolean equals_Int_Int(int a1, int a2) {
    return a1 == a2;
}
/**/

/*** equals_Int_String() ***/
boolean equals_Int_String(int a1, char* a2) {
    return a1 == atoi(a2);
}
/**/

/*** equals_Int_Token() ***/
boolean equals_Int_Token(int a1, Token a2) {
    Token token = $new(Int(a1));
    return $typeFunc(TYPE_Int::equals(token, a2));
}
/**/

/*** equals_Long_Array() ***/
boolean equals_Long_Array(long long a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $equals_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** equals_Long_Long() ***/
boolean equals_Long_Long(long long a1, long long a2) {
    return a1 == a2;
}
/**/

/*** equals_Long_Token() ***/
boolean equals_Long_Token(long long a1, Token a2) {
    Token token = $new(Long(a1));
    return $equals_Token_Token(token, a2);
}
/**/

/*** equals_String_Boolean() ***/
boolean equals_String_Boolean(char* a1, boolean a2) {
    char* a2String = a2 ? "true" : "false";
    return strcmp(a1, a2String) == 0 ? true : false;
}
/**/

/*** equals_String_Double() ***/
boolean equals_String_Double(char* a1, double a2) {
        return atof(a1) == a2;
}
/**/

/*** equals_String_Int() ***/
boolean equals_String_Int(char* a1, int a2) {
        return atoi(a1) == a2;
}
/**/

/*** equals_String_String() ***/
boolean equals_String_String(char* a1, char* a2) {
    return strcmp(a1, a2) == 0 ? true : false;
}
/**/

/*** equals_Token_Double() ***/
boolean equals_Token_Double(Token a1, double a2) {
    return $equals_Double_Token(a2, a1);
}
/**/

/*** equals_Token_Int() ***/
boolean equals_Token_Int(Token a1, int a2) {
    return $equals_Int_Token(a2, a1);
}
/**/

/*** equals_Token_Token() ***/
boolean equals_Token_Token(Token a1, Token a2) {
    return $tokenFunc(a1::equals(a2));
}
/**/

