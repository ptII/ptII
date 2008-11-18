
/*** preinitBinomialSelectorBlock ***/
long $actorSymbol(sourceTotal) = 0;

int $actorSymbol(trialsRemaining) = 0;
int $actorSymbol(selected) = 0;
long $actorSymbol(sourcePool) = 0;
double $actorSymbol(p) = 0;
/**/

/*** preinitBinomialSelectorArraysBlock($population) ***/
long $actorSymbol(sourceValues_$population) = 0;
/**/

/*** initBinomialSelectorBlock ***/
$actorSymbol(sourceTotal) = 0;
$actorSymbol(selected) = 0;
$actorSymbol(sourcePool) = 0;
$actorSymbol(p) = 0;
/**/

/*** initArraysBinomialSelectorBlock($population) ***/
$actorSymbol(sourceValues_$population) = $ref(populations#$population);
if ($actorSymbol(sourceValues_$population) >= 0) {
    $actorSymbol(sourceTotal) += $actorSymbol(sourceValues_$population);
}
/**/

/*** updateStateVariables ***/
$actorSymbol(sourcePool) = $actorSymbol(sourceTotal);
$actorSymbol(trialsRemaining) = $ref(trials);
/**/

/*** binomialSelectorBlock($num) ***/
$actorSymbol(selected) = 0;
if (($actorSymbol(trialsRemaining) > 0) && ($actorSymbol(sourceValues_$num) > 0)) {
    $actorSymbol(p) = (double) $actorSymbol(sourceValues_$num) / (double) $actorSymbol(sourcePool);
    if ($actorSymbol(p) < 1.0) {
        $actorSymbol(selected) = ColtRandomSource_BinomialDistribution($actorSymbol(trialsRemaining), $actorSymbol(p), &$actorSymbol(current));
    } else {
        $actorSymbol(selected) = $actorSymbol(trialsRemaining);
    }
}

$actorSymbol(trialsRemaining) -= $actorSymbol(selected);
$actorSymbol(sourcePool) -= $actorSymbol(sourceValues_$num);
/**/

/***fireBlock($channel)***/
/* ColtBinomialSelector fireBlock $channel */
$ref(output#$channel) = $actorSymbol(selected);
/**/
