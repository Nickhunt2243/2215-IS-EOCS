package VM_Translator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CodeWriter {

  /**
   * This is the output writer.
   */
  private PrintWriter outputWriter;
  /**
   * This is the name of the file we would like to output to.
   */
  private String fileName;
  /**
   * The Stack Pointer in code. It should and will match up with the stack pointer in the generated
   * assembly code as each instruction is run.
   */
  private int SP = 256;
  /**
   * This is the total current line count. It is used at the end for the infinite loop.
   */
  private int lineCount = 0;
  /**
   * This is the operations Hash Map. It is used to clean up the arithmetic portion of writing.
   */
  private final HashMap<String, String> operationHashMap = new HashMap<>();
  /**
   * This is the label count. Used for knowing how many labels we have for future use.
   */
  private int labelCount = 0;
  /**
   * The number of jump instructions you have written.
   */
  private int jumpCount = 0;
  /**
   * The REGEX pattern for labels.
   */
  private static final Pattern labelReg = Pattern.compile("^[^0-9][0-9A-Za-z\\_\\:\\.\\$]+");
  /**
   * This method is used to set the name of the file to print to.
   * @param fileName
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  /**
   * This method is handles the arithmetic operations that can be performed on the stack.
   *
   * @param command: The command in question to perform.
   */
  public void writeArithmetic(String command) {

    outputWriter.printf("// %s\n", command);
    // do actual operation
    switch (command) {
      case "and":
      case "add":
      case "sub":
      case "or":
        arithmeticTemplate();
        outputWriter.println("M=M" + operationHashMap.get(command) + "D");
        lineCount++;
        break;
      case "neg":
      case "not":
        outputWriter.println("@SP\n"
            + "A=M-1\n"
            + "M="+ operationHashMap.get(command) +"M");
        lineCount+=3;
        break;
      case "eq":
        writeCompareLogic("JNE");
        break;
      case "gt":
        writeCompareLogic("JLE");
        break;
      case "lt":
        writeCompareLogic("JGE");
        break;
    }
  }
  /**
   * This method is used to perform the push/pop instructions. Depending on what command type the
   * method receives, it will either push/pop.
   *
   * @param commandType: The command to either push/pop.
   * @param segment: The segment to push from/pop onto.
   * @param index: The value to push/pop.
   */
  public void writePushPop(CommandType commandType, String segment, int index) {
    switch (commandType) {
      case C_PUSH -> {
        outputWriter.printf("// push %s %d\n", segment, index);
        switch (segment) {
          case "constant" -> {
            // store value in D
            outputWriter.println("@" + index + "\n"
                + "D=A\n"
                + "@SP\n"
                + "A=M\n"
                + "M=D\n"
                + "@SP\n"
                + "M=M+1");
            lineCount += 7;
          }
          case "local" -> {
            pushSegment("LCL", index, false);
          }
          case "argument" -> {
            pushSegment("ARG", index, false);
          }
          case "this" -> {
            pushSegment("THIS", index, false);
          }
          case "that" -> {
            pushSegment("THAT", index, false);
          }
          case "pointer" -> {
            if ( index == 0 ) {
              pushSegment("THIS", index,true);
            } else if ( index == 1 ) {
              pushSegment("THAT", index,true);
            }
          }
          case "temp" -> {
            pushSegment("R5", index + 5, false);
          }
          case "static" -> {
            outputWriter.println("@" + fileName + "." + index + "\n"
                + "D=M\n"
                + "@SP\n"
                + "A=M\n"
                + "M=D\n"
                + "@SP\n"
                + "M=M+1");
            lineCount+=7;
          }
        }
      }
      case C_POP -> {
        outputWriter.printf("// pop %s %d\n", segment, index);
        switch (segment) {
          case "constant" -> {
            outputWriter.println("@" + index);
            lineCount++;
          }
          case "local" -> {
            popSegment("LCL", index, false);
          }
          case "argument" -> {
            popSegment("ARG", index, false);
          }
          case "this" -> {
            popSegment("THIS", index, false);
          }
          case "that" -> {
            popSegment("THAT", index, false);
          }
          case "pointer" -> {
            if ( index == 0 ){
              popSegment("THIS", index,true);
            } else if ( index == 1 ) {
              popSegment("THAT", index,true);
            }
          }
          case "temp" -> {
            popSegment("R5", index + 5, false);
          }
          case "static" -> {
            outputWriter.println("@SP\n"
                + "AM=M-1\n"
                + "D=M\n"
                + "@" + fileName + "." + index + "\n"
                + "M=D");
            lineCount+=5;
          }
        }
      }
    }
  }
  /**
   * This method is here to translate the label VM instruction.
   *
   * @param label: The string to be turned into a label.
   */
  public void writeLabel(String label) {
    // given "label labelName" because of how we parse the file
    //        0123456  thus we take substring starting at index 6 (labelName start)
    outputWriter.println("// label " + label);
    outputWriter.println("(" + label + ")");
  }
  /**
   * This method is used to translate the function VM instruction. It creates a label for the
   * function name and then initializes the functions local stack space to 0.
   *
   * @param functionName: The name of the function.
   * @param nVar:         The number of local variables you will need in the stack frame.
   */
  public void writeFunction(String functionName, int nVar) {
    outputWriter.println("// function " + functionName + " " + nVar);
    outputWriter.println("(" + functionName + ")");
    for ( int i=0; i<nVar; i++ ) {
      outputWriter.println("@0\n"
          + "D=A\n"
          + "@SP\n"
          + "A=M\n"
          + "M=D");
      lineCount+=5;
      incrementStackPointer();
    }
  }
  /**
   * This method is here to translate the return VM instruction. It has internal comments, so it
   * is easier to follow along with the book.
   */
  public void writeReturn() {
    outputWriter.println("// return ");
    // frame = LCL
    outputWriter.println("@LCL\n"
        + "D=M");
    lineCount+=2;
    outputWriter.println("@frame\n"
        + "M=D");
    lineCount+=2;
    // retAddr = *(frame-5)
    outputWriter.println("@5\n"
        + "A=D-A\n"
        + "D=M\n"
        + "@retAddress\n"
        + "M=D");
    lineCount+=5;
    // *ARG = pop()
    popSegment("ARG", 0, false);
    // SP = ARG+1
    outputWriter.println("@ARG\n"
        + "D=M\n"
        + "@SP\n"
        + "M=D+1");
    lineCount+=4;
    // THAT = *(frame - 1)
    preFrame("THAT");
    // THIS = *(frame - 2)
    preFrame("THIS");
    // ARG = *(frame - 3)
    preFrame("ARG");
    // LCL = *(frame - 4)
    preFrame("LCL");
    outputWriter.println("@retAddress\n"
        + "A=M\n"
        + "0;JMP");
    lineCount+=3;
  }
  /**
   * This method is here to clean up the return code. Its main purpose is to write code that will
   * re-establish the LCL, ARG, THIS, and THAT sections when you return from a function.
   *
   * @param segment: The segment to re-establish.
   */
  public void preFrame(String segment){
    outputWriter.println("@frame\n" +
        "D=M-1\n" +
        "AM=D\n" +
        "D=M\n" +
        "@" + segment + "\n" +
        "M=D");
    lineCount+=6;
  }
  /**
   * This method is here to translate the call VM instruction. It has internal comments, so it
   * is easier to follow along with how the book describes a call instruction.
   *
   * @param functionName: The name of the function you are calling.
   * @param nArg:         The number of arguments you are to pass.
   */
  public void writeCall(String functionName, int nArg) {
    outputWriter.println("// call " + functionName + " " + nArg);
    String label = "RETURN_" + ( labelCount++ );
    // incrementing the stack pointer to save a space for the return address
    outputWriter.println("@" + label + "\n"
        + "D=A\n"
        + "@SP\n"
        + "A=M\n"
        + "M=D\n"
        + "@SP\n"
        + "M=M+1");
    lineCount+=7;
    // push LCL
    pushSegment("LCL", 0, true);
    // push ARG
    pushSegment("ARG", 0, true);
    // push THIS
    pushSegment("THIS", 0, true);
    // push THAT
    pushSegment("THAT", 0, true);
    // ARG = SP - 5 - nArgs
    outputWriter.println("@SP\n"
        + "D=M\n"
        + "@5\n"
        + "D=D-A\n"
        + "@" + nArg + "\n"
        + "D=D-A\n"
        + "@ARG\n"
        + "M=D");
    lineCount+=8;
    // LCL = SP
    outputWriter.println("@SP\n" +
        "D=M\n" +
        "@LCL\n" +
        "M=D");
    lineCount+=4;
    // goto f
    outputWriter.println("@" + functionName + "\n"
        + "0;JMP");
    lineCount+=2;
    // Pushing line count (return address) to the stack
    outputWriter.println("(" + label + ")");
  }
  /**
   * This method is here to right our conditional jump ASM instruction. It uses REGEX to ensure
   * that the label stays within the specified format.
   *
   * Labels start with a non-digit char, and can contain A-Z, 0-9, _, ., $, or :
   *
   * @param arg1: Where we will be jumping.
   */
  public void writeIf(String arg1) {
    Matcher m = labelReg.matcher(arg1);
    if (m.find()){
      arithmeticTemplate();
      outputWriter.println("@" + arg1 + "\n"
          + "D;JNE");
      lineCount+=2;
    } else {
      throw new IllegalArgumentException("Wrong label format!");

    }
  }
  /**
   * This method is here to right our unconditional jump ASM instruction. It uses REGEX to ensure
   * that the label stays within the specified format.
   *
   * Labels start with a non-digit char, and can contain A-Z, 0-9, _, ., $, or :
   *
   * @param arg1: Where we will be jumping.
   */
  public void writeGOTO(String arg1){
    Matcher m = labelReg.matcher(arg1);
    if (m.find()){
      outputWriter.println("@" + arg1 +"\n"
          + "0;JMP");
      lineCount+=2;
    } else {
      throw new IllegalArgumentException("Wrong label format!");
    }
  }
  /**
   * This method is used to initialize the stack pointer to 256.
   */
  public void initializeStackPointer(int SP) {
    outputWriter.println(
              "@"+ SP +"\n"
            + "D=A\n"
            + "@SP\n"
            + "M=D");
    lineCount+=4;
  }
  /**
   * This method is called at the end of the translation process to provide the program with
   * an infinite loop.
   */
  public void endOutputString() {
    outputWriter.println(
        "@" + lineCount + "\n"
            + "0;JMP");
  }
  /**
   * This method is used to close the output file at the end of translation.
   */
  public void close() {
    outputWriter.close();
  }
  /**
   * This method is used to increment the stack pointer.
   */
  private void incrementStackPointer() {
    outputWriter.println("@SP");
    outputWriter.println("M=M+1");
    SP++;
    lineCount+=2;
  }
  /**
   * THis method is used to load the stack pointer into the A register.
   */
  private void loadStackPointerToA() {
    outputWriter.println("@SP");
    outputWriter.println("A=M");
    lineCount+=2;
  }
  /**
   * This handles the branching instructions in our assembly code.
   *
   * @param jumpCommand: The command to use JNE, JLE, JGE.
   */
  private void writeCompareLogic(String jumpCommand) {
    outputWriter.println("@SP\n"
        + "AM=M-1\n"
        + "D=M\n"
        + "A=A-1\n"
        + "D=M-D\n"
        + "@FALSE_" + jumpCount + "\n"
        + "D;" + jumpCommand + "\n"
        + "@SP\n"
        + "A=M-1\n"
        + "M=-1\n"
        + "@CONTINUE_" + jumpCount + "\n"
        + "0;JMP\n"
        + "(FALSE_" + jumpCount + ")\n"
        + "@SP\n"
        + "A=M-1\n"
        + "M=0\n"
        + "(CONTINUE_" + jumpCount + ")");
    jumpCount++;
    lineCount+=15;
  }
  /**
   * This method is here to clean up the writeArithmetic() function.
   */
  private void arithmeticTemplate(){
    outputWriter.println("@SP\n"
        + "AM=M-1\n"
        + "D=M\n"
        + "A=A-1");
    lineCount+=4;
  }
  /**
   * This method is here to clean up the popping translation of the Stack VM.
   *
   * @param segment:        The segment to load.
   * @param index:          The index of the segment to push to.
   * @param isDirectAccess: Boolean of whether you are directly accessing.
   */
  private void popSegment(String segment, int index, boolean isDirectAccess) {
    //When it is a pointer R13 will store the address of THIS or THAT
    String noPointerCode = "D=A\n";
    if ( !isDirectAccess ) {
      noPointerCode = "D=M\n"
          + "@" + index + "\n"
          + "D=D+A\n";
      lineCount+=2;
    }
    if ( segment.equals("R5") ) {
      outputWriter.println("@SP\n"
          + "AM=M-1\n"
          + "D=M\n"
          + "@" + index + "\n"
          + "M=D\n");
    } else {
      outputWriter.println("@" + segment + "\n" +
          noPointerCode +
          "@R13\n" +
          "M=D\n" +
          "@SP\n" +
          "AM=M-1\n" +
          "D=M\n" +
          "@R13\n" +
          "A=M\n" +
          "M=D");
    }
    lineCount+=10;
  }
  /**
   * This method is here to clean up the pushing translation of the Stack VM.
   *
   * @param segment:        The segment to push to.
   * @param index:          The index of the segment to push to.
   * @param isDirectAccess: Boolean of whether you are directly accessing.
   */
  private void pushSegment(String segment, int index, boolean isDirectAccess) {
    //When it is a pointer, just read the data stored in THIS or THAT
    String noPointerCode = "";
    if ( !isDirectAccess ) {
      noPointerCode = "@" + index + "\n" + "A=D+A\nD=M\n";
      lineCount+=3;
    }
    outputWriter.println("@" + segment + "\n" +
        "D=M\n"+
        noPointerCode +
        "@SP\n" +
        "A=M\n" +
        "M=D\n" +
        "@SP\n" +
        "M=M+1");
    lineCount+=7;
  }
  /**
   * This method is used to initialize the operationHashMap for cleaner code.
   */
  public void InitializeOperHash() {
    operationHashMap.put("add", "+");
    operationHashMap.put("sub", "-");
    operationHashMap.put("neg", "-");
    operationHashMap.put("or", "|");
    operationHashMap.put("and", "&");
    operationHashMap.put("not", "!");
  }
  /**
   * This function is here to help write folders that contain a Sys.vm file
   */
  public void writeSys() {
    writeCall("Sys.init", 0);
  }
  /**
   * This method is used to write the name in .asm out file as a comment to keep track of where you
   * are for debugging.
   *
   * @param file: The file to print out.
   */
  public void writeCurrentFile(File file) {
    outputWriter.println("// *** Writing to: " + file.getName() + " ***");
  }
  /**
   * This is the constructor for the codeWriter class.
   *
   * @param file: The file name of the file to append to.
   */
  public CodeWriter(File file) {
    outputWriter = null;
    File outputFile = new File(file.getName());
    try {
      outputWriter = new PrintWriter(new FileWriter(outputFile));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}