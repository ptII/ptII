/***declareBlock***/
typedef int IntToken;
/**/

/***funcDeclareBlock***/
Token Int_new(int i);
/**/


/***Int_new***/
// make a new integer token from the given value.
Token Int_new(int i) {
    Token result;
    result.type = TYPE_Int;
    result.payload.Int = i;
    return result;
}
/**/

/***Int_equals***/
Token Int_equals(Token thisToken, ...) {
    va_list argp;
    Token otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new(thisToken.payload.Int == otherToken.payload.Int);
}
/**/

/***Int_isCloseTo***/
Token Int_isCloseTo(Token thisToken, ...) {
    va_list argp;
    Token otherToken;
    Token tolerance;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);
    tolerance = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new(fabs(thisToken.payload.Int - otherToken.payload.Int) < tolerance.payload.Double);
}
/**/

/***Int_delete***/
/* Instead of Int_delete(), we call scalarDelete(). */
/**/

/***Int_print***/
Token Int_print(Token thisToken, ...) {
    printf("%d", thisToken.payload.Int);
}
/**/

/***Int_toString***/
Token Int_toString(Token thisToken, ...) {
    return String_new(InttoString(thisToken.payload.Int));
}
/**/

/***Int_add***/
Token Int_add(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return Int_new(thisToken.payload.Int + otherToken.payload.Int);
}
/**/

/***Int_subtract***/
Token Int_subtract(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return Int_new(thisToken.payload.Int - otherToken.payload.Int);
}
/**/

/***Int_multiply***/
Token Int_multiply(Token thisToken, ...) {
    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    switch (otherToken.type) {
    case TYPE_Int:
        result = Int_new(thisToken.payload.Int * otherToken.payload.Int);
        break;

#ifdef TYPE_Double
    case TYPE_Double:
        result = Double_new(thisToken.payload.Int * otherToken.payload.Double);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Int_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/***Int_divide***/
Token Int_divide(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return Int_new(thisToken.payload.Int / otherToken.payload.Int);
}
/**/

/***Int_negate***/
Token Int_negate(Token thisToken, ...) {
    thisToken.payload.Int = -thisToken.payload.Int;
    return thisToken;
}
/**/

/***Int_zero***/
Token Int_zero(Token token, ...) {
    return Int_new(0);
}
/**/

/***Int_one***/
Token Int_one(Token token, ...) {
    return Int_new(1);
}
/**/

/***Int_clone***/
Token Int_clone(Token thisToken, ...) {
    return thisToken;
}
/**/

---------------- static functions -----------------------

/***Int_convert***/
Token Int_convert(Token token, ...) {
    switch (token.type) {

#ifdef TYPE_Double
    case TYPE_Double:
        token.payload.Int = DoubletoInt(token.payload.Double);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Int_convert(): Conversion from an unsupported type. (%d)\n", token.type);
        break;
    }
    token.type = TYPE_Int;
    return token;
}
/**/

