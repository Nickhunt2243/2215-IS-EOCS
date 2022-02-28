import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class HackAssembler {

  /**
   * The hashmap for all the symbols.
   */
  private static HashMap<String, Integer> SymbolMap;
  /**
   * The longest symbol that we have in the symbol table.
   */
  private static String longestSymbol = "";
  /**
   * The address of the next variable.
   */
  private static int addressOfVariable = 16;

  /**
   * This method is responsible for adding the symbols to the symbol table. Along with keeping
   * track of the longest symbol.
   *
   * @param symbol: The symbol to add as a key.
   * @param value: The value to add as the value.
   */
  private static void addEntry( String symbol, int value ) {
    SymbolMap.put( symbol, value );
    if ( symbol.length() > longestSymbol.length() ) longestSymbol = symbol;
  }

  /**
   * This method is responsible for writing the symbol table out to a text file. This wasn't
   * necessary, but as I felt like implementing the ability to assemble multiple files at once
   * I thought it was necessary for checking the symbol table.
   * @param filename: The name of the file to print to. Derived from the input file + "S.txt".
   * @throws IOException The IOException for the FileWriter.
   */
  private static void writeSymbolFile( String filename ) throws IOException{
    FileWriter symbolFile = new FileWriter( filename );
    int spaceToAdd = 2 + ( longestSymbol.length() - 6 ) / 2;
    String symbolSpaceToAddString = "";
    for ( int i=0; i<spaceToAdd;i++ ) {
      symbolSpaceToAddString += " ";
    }
    String symbolOut = symbolSpaceToAddString + "Symbol" + symbolSpaceToAddString + "|" + "  value  \n";
    int numChar = symbolOut.length();
    while ( numChar > 1 ) {
      symbolOut += "_";
      numChar--;
    }
    symbolOut += "\n";


    for ( Map.Entry Symbol : SymbolMap.entrySet() ) {
      spaceToAdd = 2 + ( longestSymbol.length() - Symbol.getKey().toString().length() );
      symbolSpaceToAddString = "";
      String valueSpaceToAddString = "";
      for ( int i=0; i<spaceToAdd;i++ ) {
        symbolSpaceToAddString += " ";
      }
      int value = (int) Symbol.getValue();
      if ( value >= 0 && value <= 9 ) valueSpaceToAddString = "    ";
      else if ( value >= 10 && value <= 99 ) valueSpaceToAddString = "   ";
      else if ( value >= 100 && value <= 999 ) valueSpaceToAddString = "  ";
      else if ( value >= 1000 && value <= 9999 ) valueSpaceToAddString = " ";


      symbolOut += "  " + Symbol.getKey() + symbolSpaceToAddString + "|"
          + "  " +  valueSpaceToAddString + value + "  \n";

    }
    symbolFile.write(symbolOut);


    symbolFile.close();
  }

  /**
   * This method will take in assembly code from the retrieveCInstruction method that has been
   * split by '=' or ';'. It will then return the respective 7-bit destination instruction.
   *
   * @param comp: The string passed from the retrieveCInstruction method.
   * @return The 3-bit computation instruction.
   */
  private static String getComp( String comp) {
    StringBuilder compString = new StringBuilder();
    String AorM;
    if ( comp.contains("M") ) {
      compString.append("1");
      AorM = "M";
    } else {
      compString.append("0");
      AorM = "A";
    }

    if ( comp.equals( "0" ) ) compString.append( "101010" );
    else if ( comp.equals( "1" ) ) compString.append("111111" );
    else if ( comp.equals( "-1" ) ) compString.append( "111010" );
    else if ( comp.equals( "D" ) ) compString.append( "001100" );
    else if ( comp.equals( AorM ) ) compString.append( "110000" );
    else if ( comp.equals( "!D" ) ) compString.append( "001101" );
    else if ( comp.equals( "!" + AorM ) ) compString.append( "110001" );
    else if ( comp.equals( "-D" ) ) compString.append( "001111" );
    else if ( comp.equals( "-" + AorM ) ) compString.append( "110011" );
    else if ( comp.equals( "D+1" ) ) compString.append( "011111" );
    else if ( comp.equals( AorM + "+1" ) ) compString.append( "110111" );
    else if ( comp.equals( "D-1" ) ) compString.append( "001110" );
    else if ( comp.equals( AorM + "-1" ) ) compString.append( "110010" );
    else if ( comp.equals( "D+" + AorM ) ) compString.append( "000010" );
    else if ( comp.equals( "D-" + AorM ) ) compString.append( "010011" );
    else if ( comp.equals( AorM + "-D" ) ) compString.append( "000111" );
    else if ( comp.equals( "D&" + AorM ) ) compString.append( "000000" );
    else if ( comp.equals( "D|" + AorM ) ) compString.append( "010101" );

    return compString.toString();
  }

  /**
   * This method will take in assembly code from the retrieveCInstruction method that has been
   * split by '='. It will then return the respective 3-bit destination instruction.
   *
   * @param destination: The string passed from the retrieveCInstruction method.
   * @return The 3-bit destination instruction.
   */
  private static String getDest( String destination ) {

    if ( destination.equals("M") ) return "001";
    else if ( destination.equals("D") ) return "010";
    else if ( destination.equals("A") ) return "100";
    else if ( destination.equals("MD") || destination.equals("DM") ) return "011";
    else if ( destination.equals("AM") || destination.equals("MA") ) return "101";
    else if ( destination.equals("AD") || destination.equals("DA") ) return "110";
    else if ( destination.equals("ADM") || destination.equals("AMD") || destination.equals("DAM")
           || destination.equals("DMA") || destination.equals("MDA")|| destination.equals("MAD") ) return "111";
    else return "000";
  }

  /**
   * This method will take in assembly code from the retrieveCInstruction method that has been
   * split by ';'. It will then return the respective 3-bit jump instruction.
   *
   * @param jump: The string passed from the retrieveCInstruction method.
   * @return The 3-bit jump instruction.
   */
  private static String getJump( String jump ) {
    if ( jump.equals("JGT") ) return "001";
    else if ( jump.equals("JEQ") ) return "010";
    else if ( jump.equals("JLT") ) return "100";
    else if ( jump.equals("JGE") ) return "011";
    else if ( jump.equals("JNE") ) return "101";
    else if ( jump.equals("JLE") ) return "110";
    else if ( jump.equals("JMP") ) return "111";
    else return "000";
  }

  /**
   * Used to initialize the Symbol table whenever we open a new file.
   */
  private static void initializeSymbolTable() {
    SymbolMap = new HashMap<>();
    addEntry("R0",0);
    addEntry("R1",1);
    addEntry("R2",2);
    addEntry("R3",3);
    addEntry("R4",4);
    addEntry("R5",5);
    addEntry("R6",6);
    addEntry("R7",7);
    addEntry("R8",8);
    addEntry("R9",9);
    addEntry("R10",10);
    addEntry("R11",11);
    addEntry("R12",12);
    addEntry("R13",13);
    addEntry("R14",14);
    addEntry("R15",15);
    addEntry("SP",0);
    addEntry("LCL",1);
    addEntry("ARG",2);
    addEntry("THIS",3);
    addEntry("THAT",4);
    addEntry("SCREEN",16384);
    addEntry("KBD",24576);
  }

  /**
   * This method is responsible for removing comments from the code. This covers if the entire line
   * is a comment or if the comment is on the right side of some assembly code. It checks through
   * the line for a '/' and when it finds one it will take the substring from the beginning of the
   * line to where it found the first '/'.
   * @param line: The line to be checked for comments.
   * @return The same line provided with no comments.
   */
  private static String removeComments(String line) {
    String tmpLine = line;
    for (int j = 0; j < line.length(); j++) {
      if (line.charAt(j) == '/') {
        tmpLine = line.substring(0, j);
        break;
      }
    }
    return tmpLine;
  }

  /**
   * This method is responsible for eliminating the white space on either side of the line of
   * assembly code. It first checks the left side then the right and then returns the result.
   *
   * @param line: The line to check for white space.
   * @return The same line provided with no white space.
   */
  private static String eliminateWhiteSpace( String line ) {
    String tmpLine = line;

    if (line.charAt(0) == ' ') {
      for (int j = 1; j < line.length(); j++) {
        if (line.charAt(j) != ' ') {
          tmpLine = line.substring(j);
          break;
        }
      }
      line = tmpLine;
    }
    if (line.charAt(line.length() - 1) == ' ') {
      for (int j = line.length() - 2; j > -1; j--) {
        if (line.charAt(j) != ' ') {
          tmpLine = line.substring(0, j + 1);
          break;
        }
      }
      line = tmpLine;
    }
    return line;
  }

  /**
   * This method is responsible for adding new Labels to the symbol table. It will extract the
   * symbol name from the line and then add it to the symbol table with the line number as its
   * value.
   * @param lineCharacters: The char array of characters we are reading in currently.
   * @param lineNum: The current line number.
   */
  private static void addNewLabel(char[] lineCharacters, int lineNum ) {
    String symbolName = "";
    for (int j = 1; j < lineCharacters.length; j++) {
      if (lineCharacters[j] == ')')
        break;
      else
        symbolName += lineCharacters[j];
    }
    addEntry(symbolName, lineNum);
  }

  /**
   * This method is responsible for constructing the C-Instruction from a line of assembly code
   * if need be. This method will first split up the line by any '=' or ';'. Then it will send
   * its respective splits to the appropriate helper functions to retrieve the respective
   * destination, computation, and jump strings. After which it will concatenate them together and
   * return that as the C-Instruction.
   *
   * @param line: The line that we are currently reading.
   * @return The 16-bit binary C-Instruction we have computed.
   */
  private static String retrieveCInstruction(String line) {
    String[] separatingEquals;
    String[] separatingSemiColon;
    String destination = "";
    String computation = "";
    String jump = "";
    String cInstruction = "111";

    if (line.contains("=")) {
      separatingEquals = line.split("=");
      destination = getDest(separatingEquals[0]);
      computation = getComp(separatingEquals[1]);
      jump = "000";
    } else if (line.contains(";")) {
      separatingSemiColon = line.split(";");
      destination = "000";
      computation = getComp(separatingSemiColon[0]);
      jump = getJump(separatingSemiColon[1]);
    }
    cInstruction += computation + destination + jump + "\n";

    return cInstruction;
  }

  /**
   * This method is responsible for retrieving the A-instruction from a line of assembly code
   * if need be. It will go through the line and decide whether it needs to pull from the
   * symbol table, or write a new symbol to the table. If the assembler is taking its second pass,
   * and it still does not have the symbol in the chart that means it is a variable, and we need to
   * add it with the value of the next available address (16+). Lastly it will make sure that all
   * binary values are 16-bit.
   *
   * @param lineCharacters: The character array of the line we are reading in.
   * @param isSecondScan: The boolean of whether the assembler is on its second pass.
   * @return The 16-bit binary value A-Instruction as a string.
   */
  private static String retrieveAInstruction( char[] lineCharacters, boolean isSecondScan ) {
    String aInstructionString = "";
    // Retrieving the XXX string from @XXX
    for (int j = 1; j < lineCharacters.length; j++) {
      if (lineCharacters[j] == ' ')
        break;
      else
        aInstructionString += lineCharacters[j];
    }
    boolean hasSymbol_or_isNum = false;
    String aInstruction = "";
    // Checking if XXX is in the symbol map already
    if ( SymbolMap.containsKey(aInstructionString) ) {
      aInstruction = Integer.toBinaryString(SymbolMap.get(aInstructionString));
      hasSymbol_or_isNum = true;
    }
    // Checking if XXX is a numeric value
    else if (aInstructionString.charAt(0) >= '0' && aInstructionString.charAt(0) <= '9') {
      aInstruction = Integer.toBinaryString(Integer.parseInt(aInstructionString));
      hasSymbol_or_isNum = true;
    } else if ( !SymbolMap.containsKey(aInstructionString) && isSecondScan ) {
      addEntry(aInstructionString, addressOfVariable);
      aInstruction = Integer.toBinaryString( addressOfVariable );
      addressOfVariable++;
      hasSymbol_or_isNum = true;
    }
    // If the Symbol was in the HashMap or XXX was a numeric value
    if (hasSymbol_or_isNum) {
      // Making the binary value 16 bit
      int lengthToAdd = 16 - aInstruction.length();
      String zeroString = "";
      for (int k = 0; k < lengthToAdd; k++) {
        zeroString += "0";
      }
      return zeroString + aInstruction + "\n";
    }
    return "";
  }


  /**
   * This is the main method for the HackAssembly.java file. This method will be run first and is the
   * heart of the project. It handles all the logic behind whether you are writing an A or C
   * instruction, or adding a symbol to the symbol table.
   *
   * @param args: The input from the command line.
   */
  public static void main(String[] args) {
    String fileToOpen;
    boolean isAInstruction = false;
    String outputString = "";


    for (int i=0;i< args.length;i++){

      initializeSymbolTable();

      fileToOpen = args[i];
      try {
        int lineNum = 0;
        File inFile = new File(fileToOpen);
        String fileToOut = fileToOpen.substring(0, fileToOpen.length() - 4);
        FileWriter outFile = new FileWriter(fileToOut + ".hack");
        System.out.println("Assembling file: " + fileToOut + ".asm");
        boolean isSecondScan = false;

        for ( int scanNum = 1; scanNum < 3; scanNum++ ) {

          Scanner scan = new Scanner( inFile );
          while (scan.hasNext()) {
            String line = scan.nextLine();
            // Checking for comments within the line and removing them all.
            if ( line.contains("//") )
              line = removeComments( line );

            if (line.length() > 0) {
              // Checking to see if there are any spaces on either side of the Assembly instruction
              line = eliminateWhiteSpace( line );

              char[] lineCharacters = line.toCharArray();
              if ( lineCharacters[0] == '@' )
                isAInstruction = true;

              // Checking for (XXX)
              if (lineCharacters[0] == '(' ) {
                addNewLabel( lineCharacters, lineNum );
              }
              // Checking for @XXX
              else if (isAInstruction) {
                outputString += retrieveAInstruction( lineCharacters, isSecondScan );
                lineNum++;
              }
              // Must be a C instruction
              else {
                outputString += retrieveCInstruction(line);
                lineNum++;
              }
            }
            isAInstruction = false;
          }
          // If it is the second we know we are good to write to the files.
          if ( isSecondScan ) {
            writeSymbolFile(fileToOut + "S.txt");
            outFile.write(outputString);
            outFile.close();
            System.out.println("Successfully assembled file: " + outFile + ".asm into files\n\t"
                                + outFile + ".hack \n\t" + outFile + "S.txt\n");
          }

          outputString = "";
          lineNum = 0;
          addressOfVariable = 16;
          isSecondScan = true;
        }
      } catch (FileNotFoundException e) {
        System.err.println("Error file: \"" + args[i] + "\" not found."
            + "\nPlease try again with files that are within this working directory.");
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
  }
}