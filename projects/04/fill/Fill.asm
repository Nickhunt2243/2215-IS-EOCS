// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.
//SCREENISWHITE = 1;

//WHILE ( TRUE ) {
//    NUMREGISTERS = 8192;
//    IF ( KBD == 0 && SCREENISWHITE == 0 ) {
//        COLORTOPRINT = 0;
//        Screen = @screen
//        WHILE ( NUMREGISTERS > 0 ){
//            SCREEN = COLORTOPRINT;
//            NUMREGISTERS--;
//            SCREEN ++;
//        }
//    } ELSE IF ( KBD == 1 && SCREENISWHITE == 1 ) {
//        COLORTOPRINT = -1;
//        WHILE ( NUMREGISTERS > 0 ){
//            SCREEN = COLORTOPRINT;
//            NUMREGISTERS--;
//        }
//    }
//}

// Number of registers in screen

    // BOOLEAN SCREENISWHITE == 1 since the screen starts white 
    @SCREENISWHITE
    M=1
    @COLORTOPRINT
    M=0
    @KBD
    M=0
    // SETTING AND RESETTING THE NUMBER OF REGISTERS
( RESETNUMREG )  
    @SCREEN
    D=A
    @R0
    M=D
    // SETTING THE INC TO 0 AND GOING TO INC BY 16 EACH TIME
    @8192
    D=A
    @NUMREGISTERS
    M=D
// Check kbd inputs
    @CHECKKBD
    0;JMP

( COLORWHITE )
    // setting RAM[COLORTOPRINT] = 0000 0000 0000 0000
    @COLORTOPRINT
    M=0
    @SCREENISWHITE
    M=1
    @COLORSCREEN
    0;JMP

( COLORBLACK )
    // setting RAM[COLORTOPRINT] = 1111 1111 1111 1111
    @COLORTOPRINT
    M=-1
    @SCREENISWHITE
    M=0
    @COLORSCREEN
    0;JMP
    
( COLORSCREEN )
    // D = COLORTOPRINT
    @COLORTOPRINT
    D=M
    // Grabbing Incremented screen address 
    @R0
    // loading COLORTOPRINT into Incremented screen address 
    A=M
    M=D
    // incrementing R0 by 1 since we need to move up 16 addresses in memory
    @1
    D=A
    @R0
    M=M+D
    // decrementing the number of addresses to update
    @NUMREGISTERS
    D=M
    D=D-1
    M=D
    // When NUMREGISTERS reaches 0 the screen is updated 100% therefore we jump to RESETNUMREG to reset the count and continue checking the KBD
    @RESETNUMREG
    D;JEQ
    @COLORSCREEN
    D;JNE

( CHECKSCREENISWHITE )
    // 0 == black screen -> CHECKKBD
    // 1 == white screen -> COLORBLACK
    // IF (SCREENISWHITE == 0) JMP COLORWHITE ELSE CHECKKBD 
    @SCREENISWHITE
    D=M
    @COLORBLACK
    D;JNE  // IF SCREEN IS WHITE ( 1 ) 
    @CHECKKBD
    0;JMP

( CHECKSCREENISBLACK )
    // 0 == black screen -> COLORWHITE
    // 1 == white screen -> CHECKKBD
    // IF (SCREENISWHITE == 0) JMP COLORWHITE ELSE CHECKKBD 
    @SCREENISWHITE
    D=M
    @COLORWHITE
    D;JEQ // IF SCREEN IS BLACK ( 0 ) 
    @CHECKKBD
    0;JMP

( CHECKKBD ) 
    // 0 == not pressing -> White
    // 1 == pressing -> Black
    // IF (KBD == 0) JMP CHECKSCREENISBLACK ELSE CHECKSCREENISWHITE 
    @KBD
    D=M
    @CHECKSCREENISWHITE
    D;JNE // KBD != 0 
    @CHECKSCREENISBLACK
    D;JEQ // KBD == 0
