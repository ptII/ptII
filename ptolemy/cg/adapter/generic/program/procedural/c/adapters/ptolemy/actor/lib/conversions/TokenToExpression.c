/***preinitBlock***/
static Token $actorSymbol(state);
/**/

/*** TokenFireBlock ***/
// FIXME: is this the proper way to free the allocated space?
//free(put(output));

$put(output, ($Array_toString($get(input)).getPayload()));

/**/

/*** TokenArrayFireBlock($elementType) ***/
$actorSymbol(state) = $typeFunc(TYPE_Array::convert($get(input), $elementType));
$put(output, ($tokenFunc($actorSymbol(state)::toString()).payload));
/**/

/*** FireBlock($type) ***/
$put(output, $typetoString($get(input)));
/**/
