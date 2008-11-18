/***sharedBlock***/
/* The audio actors uses SDL from http://www.libsdl.org */
struct $actorClass(soundBuffer) {
    SDL_AudioSpec spec;
    Uint8   *sound;             /* Pointer to wave data */
    Uint32   soundLength;       /* Length of wave data */
    int      soundPosition;     /* Current read position */
};
/**/

/***preinitBlock***/
struct $actorClass(soundBuffer) $actorSymbol(wave);
unsigned int $actorSymbol(bitsPerSample);
/**/

/*** initBlock($fileName) ***/
$actorSymbol(wave).soundPosition = 0;

/* Load the SDL library */
if ( SDL_Init(SDL_INIT_AUDIO) < 0 ) {
    fprintf(stderr, "Couldn't initialize SDL: %s\n",SDL_GetError());
    exit(1);
}

if ( SDL_LoadWAV("$fileName", &$actorSymbol(wave).spec, &$actorSymbol(wave).sound, &$actorSymbol(wave).soundLength) == NULL ) {
    fprintf(stderr, "Couldn't load $fileName: %s\n", SDL_GetError());
    exit(1);
}
if ($actorSymbol(wave).spec.format == AUDIO_U8 || $actorSymbol(wave).spec.format == AUDIO_S8) {
    $actorSymbol(bitsPerSample) = 8;
}
else {
    $actorSymbol(bitsPerSample) = 16;
}
/**/

/*** fireBlock ***/
if ($actorSymbol(wave).soundPosition >= $actorSymbol(wave).soundLength) {
    // if there is no more data from the sound file, return 0
    $ref(output) = 0;
} else if ($actorSymbol(bitsPerSample) == 8) {
    // Convert sample (Digital to Analog)
    // Input range [0, 255] --> output range [-1.0, 1.0)

    $ref(output) = $actorSymbol(wave).sound[$actorSymbol(wave).soundPosition];
    $ref(output) -= 128;  // 2^7
    $ref(output) /= (double) 128;  // 2^7
    $actorSymbol(wave).soundPosition++;
} else {
    // Convert sample (Digital to Analog)
    // Input range [0, 65535] --> output range [-1.0, 1.0)

    $ref(output) = $actorSymbol(wave).sound[$actorSymbol(wave).soundPosition] & ($actorSymbol(wave).sound[$actorSymbol(wave).soundPosition+1] << 8);
    $ref(output) -= 32768;  // 2^15
    $ref(output) /= (double) 32768; // 2^15
    $actorSymbol(wave).soundPosition += 2;
}
/**/

/*** wrapupBlock ***/
SDL_FreeWAV($actorSymbol(wave).sound);
SDL_Quit();
/**/

