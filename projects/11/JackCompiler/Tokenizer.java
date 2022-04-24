import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

  /**
   * The scanner holding the input file.
   */
  private Scanner inputReader;
  /**
   * The list of tokens we pulled from the input file.
   */
  private final LinkedList<String> tokenList;
  /**
   * The current token we are working on.
   */
  private String currentToken;
  /**
   * The keyword Hashmap that holds its string value as it appears in jack code and KeywordType
   * enum.
   */
  private HashMap<String, KeywordType> keywordMap;
  /**
   * The list of symbols we have read in.
   */
  private LinkedList<String> symbolList;
  /**
   * The regex for accepted tokens. Includes identifierRegex, strRegex, intRegex,
   * symbolRegex
   */
  private static Pattern tokenPatterns;
  /**
   * The regex for accepting labels.
   */
  private Pattern IdentifierPattern;
  /**
   * The index of the token we are currently on.
   */
  private int indexOfToken = 0;


  /**
   * A checker to see when we reach the end of the tokens.
   *
   * @return A boolean value of if we have more tokens to read.
   */
  public boolean hasMoreTokens() {
    return indexOfToken < tokenList.size();
  }
  /**
   * This method is used to advance to the next token.
   */
  public void advance(){
    if ( hasMoreTokens() ) {
      currentToken = tokenList.get( indexOfToken );
      indexOfToken++;
    } else {
      throw new IllegalStateException("There are no more tokens to read!");
    }
  }
  /**
   * This method is used ot decrement the current token.
   */
  public void decrementIndex() {
    indexOfToken--;
    currentToken = tokenList.get( indexOfToken );
  }
  /**
   * This method returns the current token as a string
   *
   * @return The current token.
   */
  public String getCurrentToken() {
    return currentToken;
  }
  /**
   * This method is used to figure out which TokenType the current token falls under. It will
   * check if the token is in the keywordMap, then symbolList, then check its syntax to see if it
   * is an identifier, then it will check if it's surrounded by " (String constant), and then if all
   * else fails we declare the token as an integer constant.
   *
   * @return The tokenType of the current token.
   */
  public TokenType tokenType() {
    char firstCharacter = currentToken.charAt(0);
    if ( keywordMap.get( currentToken ) != null ) {
      return TokenType.KEYWORD;
    } else if ( symbolList.contains( currentToken ) ) {
      return TokenType.SYMBOL;
    } else if ( currentToken.charAt(0) >= 'A' && currentToken.charAt(0) <= 'Z' ||
        currentToken.charAt(0) >= 'a' && currentToken.charAt(0) <= 'z') { //IdentifierPattern.matcher(currentToken).matches() ) {
//      System.out.println(currentToken);
      return TokenType.IDENTIFIER;
    } else if ( firstCharacter == '"' ) {
      return TokenType.STRING_CONST;
    } else {
      return TokenType.INT_CONST;
    }
  }
  /**
   * If the tokenType is a keyword then we can use this to get what type of keyword it is.
   *
   * @return The keywordType of the currentToken.
   */
  public KeywordType keyword() {
    return keywordMap.get(currentToken);
  }
  /**
   * If the tokenType is a symbol then this will return the symbol as a character.
   *
   * @return The symbol as a character.
   */
  public char symbol() {
    return currentToken.charAt(0);
  }
  /**
   * If the tokenType is an identifier then this method will return the current token as is.
   *
   * @return The currentToken with no modifications.
   */
  public String identifier() {
    return currentToken;
  }
  /**
   * If the tokenType is an integer constant then this method will return the currentToken as an
   * integer.
   *
   * @return The current token as an integer.
   */
  public int intVal() {
    return Integer.parseInt( currentToken );
  }
  /**
   * If the tokenType is a string constant then this method will return the currentToken with the
   * surrounding ".
   *
   * @return The currentToken without the "
   */
  public String stringVal() {
    return currentToken.substring(1, currentToken.length() - 1);
  }
  /**
   * This method is used to initialize out KeywordMap and symbolList.
   */
  public void initializeLists() {
    keywordMap = new HashMap<>();
    keywordMap.put("class", KeywordType.CLASS);
    keywordMap.put("method", KeywordType.METHOD);
    keywordMap.put("function", KeywordType.FUNCTION);
    keywordMap.put("constructor", KeywordType.CONSTRUCTOR);
    keywordMap.put("int", KeywordType.INT);
    keywordMap.put("boolean", KeywordType.BOOLEAN);
    keywordMap.put("char", KeywordType.CHAR);
    keywordMap.put("void", KeywordType.VOID);
    keywordMap.put("var", KeywordType.VAR);
    keywordMap.put("static", KeywordType.STATIC);
    keywordMap.put("field", KeywordType.FIELD);
    keywordMap.put("let", KeywordType.LET);
    keywordMap.put("do", KeywordType.DO);
    keywordMap.put("if", KeywordType.IF);
    keywordMap.put("else", KeywordType.ELSE);
    keywordMap.put("while", KeywordType.WHILE);
    keywordMap.put("return", KeywordType.RETURN);
    keywordMap.put("true", KeywordType.TRUE);
    keywordMap.put("false", KeywordType.FALSE);
    keywordMap.put("null", KeywordType.NULL);
    keywordMap.put("this", KeywordType.THIS);

    String[] symbolArr = {"{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/",
        "&", "|", "<", ">", "=", "~"};
    symbolList = new LinkedList<>(Arrays.stream(symbolArr).toList());

  }
  /**
   * This method is used to initialize out Regex patterns, so we can extract tokens correctly.
   */
  private void initializeRegex() {

    String keyWordRegex = "";

    for (String seg: keywordMap.keySet()){

      keyWordRegex += seg + "|";

    }
    String symbolRegex = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
    String intRegex = "[0-9]+";       // one or more concatenations of an 0-9
    String strRegex = "\"[^\"\n]*\""; // Anything in " " except " and \n (user Output.println for \n)
    String identifierRegex = "[\\w_]+"; // one or more concatenations of A-Z | a-z | _
    IdentifierPattern = Pattern.compile(identifierRegex);
    tokenPatterns = Pattern.compile(keyWordRegex
        + symbolRegex
        + "|" + intRegex
        + "|" + strRegex
        + "|" + identifierRegex);

  }
  /**
   * This method is used to delete block comments from the jack file.
   *
   * @param line: The line to clean.
   * @return The clean line
   */
  public static String noBlockComments( String line ) {
    int startIndex = line.indexOf( "/*" );
    if ( startIndex == -1 ) return line;
    String result = line;
    int endIndex = line.indexOf( "*/" );
    while( startIndex != -1 ) {
      if ( endIndex == -1 ) {
        return line.substring( 0,startIndex - 1 );
      }
      result = result.substring( 0,startIndex ) + result.substring( endIndex + 2 );
      startIndex = result.indexOf( "/*" );
      endIndex = result.indexOf( "*/" );
    }
    return result;
  }
  /**
   * This method is used to delete // comments from the jack file.
   *
   * @param line: The line to be cleaned.
   * @return The clean line.
   */
  public static String noComments(String line){
    int position = line.indexOf("//");
    if (position != -1){
      line = line.substring(0, position);
    }
    return line;
  }

  /**
   * This is the constructor of the Tokenizer class. It will read through the input file and
   * compose a list of tokens that we will iterate through in CompilationEngine.
   *
   * @param inputFile: The file to read tokens in from.
   */
  public Tokenizer(File inputFile) {
    initializeLists();
    try {
      inputReader = new Scanner(inputFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
    String preprocessed = "";
    String line = "";
    while(inputReader.hasNext()){
      line = noComments(inputReader.nextLine()).trim();
      if (line.length() > 0) {
        preprocessed += line + "\n";
      }
    }
    preprocessed = noBlockComments(preprocessed).trim();
    preprocessed = preprocessed.trim();
    //init all regex
    initializeRegex();
    Matcher m = tokenPatterns.matcher(preprocessed);
    tokenList = new LinkedList<>();
    while ( m.find() ){
      tokenList.add(m.group());
    }
  }
}
