import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class main {

  public static void main(String[] args) {

    for (String arg: args) {
      File inputFile = new File(arg);
      ArrayList<File> fileList = new ArrayList<>();

      if (inputFile.isDirectory()) {
        iterateFiles( inputFile.listFiles() );
      } else {
        if (inputFile.getName().endsWith(".jack")) {
          fileList.add(inputFile);
          compile( fileList );
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
        if (file.getName().endsWith(".jack")) {
          fileList.add(file);
        }
      }
    }
    if (fileList.size() != 0)
      compile( fileList );

  }

  private static void closeFile(String fileToString, File outputFile, CompilationEngine analyzer){
    System.out.println("Successfully translated file: " + fileToString + " into file:"
        + "\n\t" + outputFile.getAbsolutePath() + "\n");
    analyzer.close();
  }

  /**
   * The translator is used as the logic behind this main function deciding if what method to call
   * based on what commandType the line is.
   *
   * @param fileList: The files to parse and translate.
   */
  private static void compile( ArrayList<File> fileList ) {
    File outputFile;
    CompilationEngine analyzer;

    for (File file: fileList) {
      outputFile = new File(file.getPath().split(".jack")[0] + ".xml");
      System.out.println(Arrays.toString(file.getAbsolutePath().split(".jack")) + ".xml");

      analyzer = new CompilationEngine(file, outputFile);

      analyzer.compileClass();
      closeFile(outputFile.toString(), outputFile, analyzer);
      }
    }


}
