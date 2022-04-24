package VM_Translator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;


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
        popStackToD();
        decrementStackPointer();
        loadStackPointerToA();
        outputWriter.println("M=M" + operationHashMap.get(command) + "D");
        lineCount++;
        incrementStackPointer();
        break;
      case "neg":
      case "not":
        decrementStackPointer();
        loadStackPointerToA();
        outputWriter.println("M="+ operationHashMap.get(command) +"M");
        lineCount++;
        incrementStackPointer();
        break;
      case "eq":
        writeCompareLogic("JEQ");
        break;
      case "gt":
        writeCompareLogic("JGT");
        break;
      case "lt":
        writeCompareLogic("JLT");
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
            outputWriter.println("@" + index);
            outputWriter.println("D=A");
            lineCount += 2;
          }
          case "local" -> {
            loadSegment("LCL", index);
            outputWriter.println("D=M");
            lineCount++;
          }
          case "argument" -> {
            loadSegment("ARG", index);
            outputWriter.println("D=M");
            lineCount++;
          }
          case "this" -> {
            loadSegment("THIS", index);
            outputWriter.println("D=M");
            lineCount++;
          }
          case "that" -> {
            loadSegment("THAT", index);
            outputWriter.println("D=M");
            lineCount++;
          }
          case "pointer" -> {
            outputWriter.println("@R" + String.valueOf(3 + index));
            outputWriter.println("D=M");
            lineCount += 2;
          }
          case "temp" -> {
            outputWriter.println("@R" + String.valueOf(5 + index));
            outputWriter.println("D=M");
            lineCount += 2;
          }
          case "static" -> {
            outputWriter.println("@" + fileName.split("\\.")[0] + String.valueOf(index));
            outputWriter.println("D=M");
            lineCount += 2;
          }
        }
        pushDToStack();
      }
      case C_POP -> {
        outputWriter.printf("// pop %s %d\n", segment, index);
        switch (segment) {
          case "constant" -> {
            outputWriter.println("@" + index);
            lineCount++;
          }
          case "local" -> loadSegment("LCL", index);
          case "argument" -> loadSegment("ARG", index);
          case "this" -> loadSegment("THIS", index);
          case "that" -> loadSegment("THAT", index);
          case "pointer" -> {
            outputWriter.println("@R" + String.valueOf(3 + index));
            lineCount++;
          }
          case "temp" -> {
            outputWriter.println("@R" + String.valueOf(5 + index));
            lineCount++;
          }
          case "static" -> {
            outputWriter.println("@" + fileName.split("\\.")[0] + String.valueOf(index));
            lineCount++;
          }
        }
        outputWriter.println("D=A");
        outputWriter.println("@R13");
        outputWriter.println("M=D");
        popStackToD();
        outputWriter.println("@R13");
        outputWriter.println("A=M");
        outputWriter.println("M=D");
        lineCount += 6;
      }
    }
  }

  /**
   * This method is used to initialize the stack pointer to 256.
   */
  public void initializeOutputString() {
    outputWriter.println(
              "@256\n"
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
    lineCount+=2;
  }

  /**
   * This method is used to decrement the stack pointer.
   */
  private void decrementStackPointer() {
    outputWriter.println("@SP");
    outputWriter.println("M=M-1");
    lineCount+=2;
  }

  /**
   * This method is used to pop from the stack and place the value into D.
   */
  private void popStackToD() {
    decrementStackPointer();
    outputWriter.println("A=M");
    outputWriter.println("D=M");
    lineCount+=2;
  }

  /**
   * This method is used to move the value in the D register into the stack.
   */
  private void pushDToStack() {
    loadStackPointerToA();
    outputWriter.println("M=D");
    lineCount++;
    incrementStackPointer();
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
   * This handles the branching instructions in our assembly code. More on this next chapter!
   *
   * @param jumpCommand: The command to use JEQ, JLT, JGT.
   */
  private void writeCompareLogic(String jumpCommand) {
    popStackToD();
    decrementStackPointer();
    loadStackPointerToA();
    outputWriter.println("D=M-D");
    outputWriter.println("@LABEL" + labelCount);
    outputWriter.println("D;"+jumpCommand);
    loadStackPointerToA();
    outputWriter.println("M=0");
    outputWriter.println("@ENDLABEL" + labelCount);
    outputWriter.println("0;JMP");
    outputWriter.println("(LABEL" + labelCount + ")");
    loadStackPointerToA();
    outputWriter.println("M=-1");
    outputWriter.println("(ENDLABEL" + labelCount + ")");
    incrementStackPointer();
    labelCount++;
    lineCount+=7;
  }

  /**
   * This method is used to clean up the code everywhere that we need to load a segment of memory,
   * such as this, that, argument, local
   *
   * @param segment: The segment to load.
   * @param index:
   */
  private void loadSegment(String segment, int index) {
    outputWriter.println("@" + segment );
    outputWriter.println("D=M");
    outputWriter.println("@"+ index );
    outputWriter.println("A=D+A");
    lineCount+=4;
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
   * This is the constructor for the codeWriter class.
   *
   * @param file: The file name of the file to append to.
   */
  public CodeWriter(File file) {
    outputWriter = null;
    File outputFile = new File(file.getAbsolutePath().split(".vm")[0] + ".asm");
    try {
      outputWriter = new PrintWriter(new FileWriter(outputFile));
      fileName = file.getName();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}