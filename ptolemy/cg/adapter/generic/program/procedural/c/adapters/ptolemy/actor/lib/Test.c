/*** preinitBlock ***/
// Initialize to -1 because we ALWAYS increment first.
// This is more convenient for multiport, where we check if \$channel
// number is equal zero (the first channel). If so, then we increment.
int $actorSymbol(numberOfTokensSeen) = -1;
$targetType(input) $actorSymbol(inputToken);
/**/

/*** TokenPreinitBlock($channel)***/
Token $actorSymbol(correctValuesThisFiring_$channel);
/**/

/*** toleranceTokenPreinitBlock***/
static Token $actorSymbol(toleranceToken);
/**/

/*** toleranceTokenInitBlock***/
$actorSymbol(toleranceToken) = $new(Double($param(tolerance)));
/**/

/***ComplexBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;

/* Complex $actorSymbol(), ComplexBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
   && !$isCloseTo_Token_Token($actorSymbol(inputToken), Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen)), $actorSymbol(toleranceToken))) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %s. Should have been within %g of %s.\n",
            $actorSymbol(numberOfTokensSeen),
            $ComplextoString($actorSymbol(inputToken)),
            $param(tolerance),
            $ComplextoString(Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen))));
   exit(-1);
}
/**/

/***ComplexBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* Complex $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - (($cgType(input))(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)).$lcCgType(input)Value())
        > $param(tolerance)) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been within %10.30g of: %d\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(tolerance),
            (int)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload));
   exit(-1);
}
/**/

/***IntBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;

/* IB $actorSymbol(), intBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - $param(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $param(tolerance)) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $param(tolerance),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) +
            $param(tolerance));
   exit(-1);
}
/**/


/***IntBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* IBMC $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - IntArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel))
        > $param(tolerance)) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been within %10.30g of: %d\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(tolerance),
            IntArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel));
    exit(-1);
}
/**/


/***DoubleBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;

/* $actorSymbol(), DoubleBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - $param(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $param(tolerance)) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $param(tolerance),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) +
            $param(tolerance));
    exit(-1);
}
/**/

/***DoubleBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* DBMC $channel of $actorSymbol() */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)) {
   $actorSymbol(correctValuesThisFiring_$channel) =
       $param(correctValues, $actorSymbol(numberOfTokensSeen));
   if (abs($actorSymbol(inputToken)
                - DoubleArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel))
           > $param(tolerance)) {
       printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been within %10.30g of: %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(tolerance),
               (Number)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload));
            DoubleArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel));
       exit(-1);
   }
}
/**/

/***BooleanBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
if (($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (!$param(correctValues, $actorSymbol(numberOfTokensSeen))
                && $actorSymbol(inputToken)))
        || ($param(correctValues, $actorSymbol(numberOfTokensSeen))
                && !$actorSymbol(inputToken)) ) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($actorSymbol(inputToken)),
            BooleantoString($param(correctValues, $actorSymbol(numberOfTokensSeen))));
    exit(-1);
}
/**/

/***BooleanBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
    && (!Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).getPayload().equals(
       $actorSymbol(inputToken)))) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a token of value: %s. Should have been a token of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).getPayload());
   exit(-1);
}
/**/

/***StringBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && !$param(correctValues, $actorSymbol(numberOfTokensSeen))
                    .equals($actorSymbol(inputToken))) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)));
   exit(-1);
}
/**/

/***StringBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && !$actorSymbol(inputToken).equals(
                    (String)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload))) {
    hrow new RuntimeException("Test $actorSymbol($channel) fails in iteration "
            + $actorSymbol(numberOfTokensSeen)
            + ".\n Value was a String: \""
            + $actorSymbol(inputToken)
            + "\". Should have been a String: \""
            + (String)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)
            + "\"");
   exit(-1);
}
/**/

/***TokenBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
/* If the type of the input is an array, then cast the input to
 * the type of the elements of the elements of correctValues. */
if (($type(input) != TYPE_Array
#ifdef PTCG_TYPE_Matrix
     && $type(input) != TYPE_Matrix
#endif
     && !equals_Token_Token($actorSymbol(inputToken), Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen))))
    || ($type(input) == TYPE_Array
        && !$isCloseTo_Token_Token($actorSymbol(inputToken), Array_get(Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen)), 0), $actorSymbol(toleranceToken)))
#ifdef PTCG_TYPE_Matrix
    || ($type(input) == TYPE_Matrix
        && !$isCloseTo_Token_Token(Matrix_get($actorSymbol(inputToken), 0, 0), Matrix_get(Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen)), 0, 0), $actorSymbol(toleranceToken)))
#endif
    ) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d\n Value was: %s. Should have been within %f of %s\n",
            $actorSymbol(numberOfTokensSeen),
            $Array_toString($actorSymbol(inputToken)).payload.String,
            $param(tolerance),
            $Array_toString(Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen))).payload.String);
   exit(-1);
}
/**/

/***TokenBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* TBMC $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)) {
    //if ($type(input) != TYPE_Array) {
      if (!$tokenFunc($actorSymbol(inputToken)::equals(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)))) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $tokenFunc($actorSymbol(inputToken)::toString()).payload.String,
            $tokenFunc(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)::toString()).payload.String);
//     throw new RuntimeException("Test $actorSymbol($channel) fails in iteration "
//             + $actorSymbol(numberOfTokensSeen)
//             + ".\n Value was: \""
//             + $actorSymbol(inputToken)
//             + "\". Should have been: \""
//             + Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)
//             + "\"");
//       }
//    } else {
//    throw new RuntimeException("Test $actorSymbol($channel) fails in iteration "
//            + $actorSymbol(numberOfTokensSeen)
//            + ".\n Value was: '"
//            + $tokenFunc($actorSymbol(inputToken)::toString()).payload
//            + "'. Should have been: \""
//            + Array_toString(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)).payload
//            + "\"");
      }
   exit(-1);
}
/**/

/***UnsignedByteBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;

/* UB $actorSymbol(), UnsignedByteBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - $param(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $param(tolerance)) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $param(tolerance),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $param(tolerance));
   exit(-1);
}
/**/

/***UnsignedByteBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* UBMC $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - (($cgType(input))(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)).$lcCgType(input)Value())
        > $param(tolerance)) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been within %10.30g of: %d\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(tolerance),
            (int)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload));
   exit(-1);
}
/**/

/*** wrapupBlock ***/
if (($actorSymbol(numberOfTokensSeen) + 1) < $size(correctValues)) {
    printf("\nTest produced only %d tokens, yet the correctValues parameter was expecting %d tokens.\n", $actorSymbol(numberOfTokensSeen), $size(correctValues));
}
/**/
