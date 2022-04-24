package VM_Translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;




public class Main {

  private static CodeWriter codeWriter;

  /**
   * This is the main method of the package. It takes in .vm files to be translated into .asm files.
   *
   * @param args: The files to be translated into .asm files.
   */
  public static void main(String[] args) {
    for (String arg: args) {
      File inputFile = new File(arg);

      if (inputFile.isDirectory()) {
        iterateFiles(inputFile.listFiles());
      } else {
        codeWriter = new CodeWriter(inputFile);
        codeWriter.InitializeOperHash();
        translate( inputFile );
        closeFile( inputFile );
      }
    }
  }

  private static void iterateFiles(File[] files) {
    for (File file : files) {
      if (file.isDirectory()) {
        iterateFiles(file.listFiles()); // Calls same method again.
      } else {
        if (file.getName().endsWith(".vm")) {
          codeWriter = new CodeWriter(file);
          codeWriter.InitializeOperHash();
          translate( file );
          closeFile( file );
        }
      }
    }
  }

  private static void closeFile(File inputFile){
    codeWriter.endOutputString();
    String outputFile = inputFile.getName().split(".vm")[0] + ".asm";
    System.out.println("Successfully translated file: " + inputFile.getName() + " into file:"
        + "\n\t" + outputFile + "\n");
    codeWriter.close();
  }

  /**
   * The translator is used as the logic behind this main function deciding if what method to call
   * based on what commandType the line is.
   *
   * @param file: The file to parse and translate.
   */
  private static void translate( File file ) {
    File outputFile = new File(file.getName().split(".vm")[0] + ".asm" );
    System.out.println("Translating file: " + file.getName() + "\n");
    Scanner inputScanner = null;
    try {
      inputScanner = new Scanner(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    Parser parser = new Parser(inputScanner);
    codeWriter.setFileName(outputFile.getName());
    codeWriter.initializeOutputString();
    while (parser.hasMoreCommands()) {
      parser.advance();
      switch (parser.commandType()) {
        case C_PUSH, C_POP -> codeWriter.writePushPop(parser.commandType(), parser.arg1(),
            parser.arg2());
        case C_ARITHMETIC -> codeWriter.writeArithmetic(parser.arg1());
      }
    }
  }
}
