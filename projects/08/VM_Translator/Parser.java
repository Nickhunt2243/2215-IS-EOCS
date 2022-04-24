package VM_Translator;

import java.util.Scanner;

public class Parser {

  /**
   *  Scanner that is used to parse through file.
   */
  private final Scanner mScanner;
  /**
   * The current line parser is reading.
   */
  private String line;

  /*
  Checks for next line
   */
  public boolean hasMoreCommands() {
    return mScanner.hasNextLine();
  }

  /**
   * This method is used to move to the next line. It will also check for comments and white
   * space. There is a regular expression alternative provided for as an alternative, but the
   * current method is fine for simple parsing.
   */
  public void advance() {
    if (hasMoreCommands()) {
      line = mScanner.nextLine();

      /* Checking for comments
         Could have also used:
         String regexComments = "//.*";
         Pattern ptComments = Pattern.compile( regexComments );
       */
      int commentIndex = line.indexOf("/");
      if (commentIndex >= 0) {
        line = line.substring(0, commentIndex);
      }

      /* Checking for white space
         Could have also used:
         String regexWhiteSpace = "\\s*";
         Pattern ptWhiteSpace = Pattern.compile( regexWhiteSpace ); */
      line = line.trim();

      if (line.isEmpty()) {
        advance();
      }
    }
  }

  /**
   * This method will determine the command type of the line. More implementation in the
   * future chapters.
   *
   * @return The type of command the line is.
   */
  public CommandType commandType() {
    String command = line.split(" ")[0];
    return switch (command) {
      case "push" -> CommandType.C_PUSH;
      case "pop" -> CommandType.C_POP;
      case "label" -> CommandType.C_LABEL;
      case "function" -> CommandType.C_FUNCTION;
      case "return" -> CommandType.C_RETURN;
      case "call" -> CommandType.C_CALL;
      case "goto" -> CommandType.C_GOTO;
      case "if-goto" -> CommandType.C_IF;
      default -> CommandType.C_ARITHMETIC;
    };
  }

  /**
   * This method returns the first argument of the line or the segment portion of the line. More
   * implementation in the future chapters.
   *
   * @return The first argument of the current line.
   */
  public String arg1() {
    if (commandType() == CommandType.C_RETURN) {
      return null;
    }

    if (commandType() == CommandType.C_ARITHMETIC) {
      return line;
    }

    return line.split(" ")[1];
  }

  /**
   * This method returns the second argument of the line or the value portion of the line. More
   * implementation in the future chapters.
   *
   * @return The second argument of the current line.
   */
  public int arg2() {
    if (commandType() == CommandType.C_PUSH
        || commandType() == CommandType.C_POP
        || commandType() == CommandType.C_FUNCTION
        || commandType() == CommandType.C_CALL) {
      return Integer.parseInt(line.split(" ")[2]);
    }

    return 0;
  }

  /**
   * The Parser constructor that takes in a scanner from the main method.
   *
   * @param scanner: The scanner from the main method.
   */
  public Parser(Scanner scanner) {
    this.mScanner = scanner;
  }
}
