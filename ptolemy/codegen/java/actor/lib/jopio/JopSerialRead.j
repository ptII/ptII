/***preinitBlock***/
boolean $actorSymbol(firstFire) = true;
static com.jopdesign.io.SerialPort $actorSymbol(ser) = com.jopdesign.io.IOFactory.getFactory().getSerialPort();
/**/

/*** FireBlock($channel) ***/
if ($actorSymbol(firstFire)) {
    // read the value if available
    if (($actorSymbol(ser).status & com.jopdesign.io.SerialPort.MASK_RDRF)!=0) {
                $ref(output) = $actorSymbol(ser).data;
    } else {
                $ref(output) = -1;
    }
           $actorSymbol(firstFire) = false;
}
/**/

/*** postfireBlock ***/
$actorSymbol(firstFire) = true;
/**/
