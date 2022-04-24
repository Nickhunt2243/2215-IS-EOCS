package VM_Translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
      ArrayList<File> fileList = new ArrayList<>();

      if (inputFile.isDirectory()) {
        iterateFiles( inputFile.listFiles() );
      } else {
        if (inputFile.getName().endsWith(".vm")) {
          fileList.add(inputFile);
          translate( inputFile, fileList );
          closeFile(inputFile.getName(), inputFile);
        }
      }
    }
  }

  private static void iterateFiles(File[] files) {
    ArrayList<File> fileList = new ArrayList<>();
    for (File file : files) {
      if (file.isDirectory()) {
        iterateFiles( file.listFiles() ); // Calls same method again.
      } else {
        if (file.getName().endsWith(".vm")) {
          fileList.add(file);
        }
      }
    }
    if (fileList.size() != 0)
      translate( files[0].getParentFile(), fileList );

  }

  private static void closeFile(String fileToString, File outputFile){
    codeWriter.endOutputString();
    System.out.println("Successfully translated file/s: " + fileToString + " into file:"
        + "\n\t" + outputFile.getName() + "\n");
    codeWriter.close();
  }

  /**
   * The translator is used as the logic behind this main function deciding if what method to call
   * based on what commandType the line is.
   *
   * @param folderName: The name that we will use for the generated .asm file.
   * @param fileList: The files to parse and translate.
   */
  private static void translate( File folderName, ArrayList<File> fileList ) {
    File outputFile;
    if (folderName.getName().endsWith(".vm")){
      outputFile = new File(folderName.getName().split(".vm")[0]
          + ".asm");
    } else {
      outputFile = new File(folderName.getName() + "/" + folderName.getName()
          + ".asm");
    }

    //Checking if there is a Sys.vm file present to initialize starter code.
    codeWriter = new CodeWriter(outputFile);
    codeWriter.initializeStackPointer(256);
    codeWriter.InitializeOperHash();

    for ( File file: fileList ) {
      if (file.getName().equals("Sys.vm")) {
        codeWriter.writeSys();
      }
    }
    String fileToString = "";
    int i=0;
    for (File file: fileList) {
      fileToString += file.getName();
      if ( i+1 < fileList.size() ) {
        fileToString += ", ";
        i++;
      }
      System.out.println("Translating file: " + file.getName() + "\n");
      Scanner inputScanner = null;
      try {
        inputScanner = new Scanner(file);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      Parser parser = new Parser(inputScanner);
      codeWriter.setFileName(file.getName());
      codeWriter.writeCurrentFile(file);
      while (parser.hasMoreCommands()) {
        parser.advance();
        switch (parser.commandType()) {
          case C_PUSH, C_POP -> codeWriter.writePushPop(parser.commandType(), parser.arg1(),
              parser.arg2());
          case C_ARITHMETIC -> codeWriter.writeArithmetic( parser.arg1() );
          case C_CALL -> codeWriter.writeCall( parser.arg1(), parser.arg2() );
          case C_FUNCTION -> codeWriter.writeFunction( parser.arg1(), parser.arg2() );
          case C_RETURN -> codeWriter.writeReturn();
          case C_LABEL -> codeWriter.writeLabel( parser.arg1() );
          case C_GOTO -> codeWriter.writeGOTO( parser.arg1() );
          case C_IF -> codeWriter.writeIf( parser.arg1() );
        }
      }
    }
    closeFile(fileToString, outputFile);
  }
}
