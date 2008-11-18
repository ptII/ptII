/***FireBlock***/
// primitive is commutative.
$ref(output) = $val(factor) * $ref(input);
/**/

/***TokenFireBlock***/
if ($ref(scaleOnLeft)) {
    $ref(output) = Scale_scaleOnLeft($ref(input), (double) $val(factor));
} else {
    $ref(output) = Scale_scaleOnRight($ref(input), (double) $val(factor));
}
/**/

/***sharedScaleOnLeftBlock***/
Token Scale_scaleOnLeft(Token input, double factor) {
    int i;
    Token result = new Token();

    if (input.type == TYPE_Array) {
            result = $new(Array(((array)(input.payload)).size, 0));

        for (i = 0; i < ((array)(input.payload)).size; i++) {
            ((array)(result.payload)).elements[i] = Scale_scaleOnLeft(Array_get(input, i), factor);
        }

        return result;
    } else {
        System.out.println("Scale_scaleOnLeft problem");
        //return $tokenFunc($new(Double(factor))::multiply(input));
	return result;
    }
}
/**/

/***sharedScaleOnRightBlock***/
Token Scale_scaleOnRight(Token input, double factor) {
    int i;
    Token result = new Token();

    if (input.type == TYPE_Array) {
            result = $new(Array(((array)(input.payload)).size, 0));

        for (i = 0; i < ((array)(input.payload)).size; i++) {
            ((array)(result.payload)).elements[i] = Scale_scaleOnRight(Array_get(input, i), factor);
        }

        return result;
    } else {
        System.out.println("Scale_scaleOnLeft problem");
	return result;
	//return $tokenFunc(input::multiply($new(Double(factor))));
    }
}
/**/
