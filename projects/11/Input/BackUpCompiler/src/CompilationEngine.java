import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;

public class CompilationEngine {

  /**
   * The instance of the symbol table that we will use throughout compilation.
   */
  private final SymbolTable symbolTable;
  /**
   * THe PrintWriter for the output file
   */
  private PrintWriter outputWriter;
  /**
   * The string that will add tabs to the output file to add "parent/child" xml formatting
   */
  private String tabString = "";
  /**
   * The instance of the tokenizer that will be used throughout compilation.
   */
  private Tokenizer tokenizer;
  /**
   *
   */
  private SymbolTable localSymbolTable;

  /**
   * Class variable of the Jack Grammar
   *
   * This will be called first as it is used to check if the first token is class. If it is not the
   * language will be rejected by this PDA.
   */
  public void compileClass() {
    // Getting "class"
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.KEYWORD && tokenizer.keyword() != KeywordType.CLASS) {
      COMPILATIONERROR("class");
    }
    outputWriter.println(tabString + "<class>");
    tabString += "\t";
    outputWriter.println( tabString + "<keyword> class </keyword>" );
    // Getting className
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
      COMPILATIONERROR("className");
    }
    outputWriter.println( tabString + "<identifier> " + tokenizer.identifier() + " </identifier>" );
    // Getting {
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '{' ) {
      COMPILATIONERROR("{");
    }
    outputWriter.println(tabString + "<symbol> " + tokenizer.symbol() + " </symbol>");
    // Compile field/static variables then other subroutines within class
    compileClassVarDec();
    compileSubroutine();
    // Getting }
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '}' ) {
      COMPILATIONERROR("}");
    }
    outputWriter.println(tabString + "<symbol> } </symbol>");
    outputWriter.println(tabString + "</class>");
  }

  /**
   * Class Variable Declaration variable of the Jack Grammar.
   */
  public void compileClassVarDec() {
    tokenizer.advance();
    String name = "";
    String type = "";
    String kind = "";
    while (tokenizer.tokenType() == TokenType.KEYWORD && ( tokenizer.keyword() == KeywordType.FIELD
        || tokenizer.keyword() == KeywordType.STATIC ) ) {
      // Getting static or field
      outputWriter.println( tabString + "<classVarDec>");
      tabString += "\t";
      outputWriter.println(tabString + "<keyword> " + tokenizer.getCurrentToken() + " </keyword>");
      kind = tokenizer.getCurrentToken();
      // Getting int, char, boolean, className
      tokenizer.advance();
      if ( tokenizer.tokenType() == TokenType.IDENTIFIER ) {
        outputWriter.println( tabString + "<identifier> " + tokenizer.identifier()
            + " </identifier>" );
      }
      if ( tokenizer.tokenType() == TokenType.KEYWORD && ( tokenizer.keyword() == KeywordType.BOOLEAN
          || tokenizer.keyword() == KeywordType.INT|| tokenizer.keyword() == KeywordType.CHAR )) {
        outputWriter.println( tabString + "<keyword> " + tokenizer.getCurrentToken()
            + " </keyword>" );
      }
      type = tokenizer.getCurrentToken();
      // Getting varName
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
        COMPILATIONERROR("varName");
      }
      outputWriter.println( tabString + "<identifier> " + tokenizer.identifier() + " </identifier>");
      name = tokenizer.identifier();
      symbolTable.define(name, type, kind);
      outputWriter.println(tabString + "<Symbol_Table_Entry> name: " + name + " type: "
          + symbolTable.typeOf(name) + " kind: " + symbolTable.kindOf(name) + " #: "
          + symbolTable.indexOf(name) + " </Symbol_Table_Entry>" );
      // Checking for , varName

      tokenizer.advance();
      while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ',') {
        outputWriter.println( tabString + "<symbol> , </symbol>" );
        // Get varName
        tokenizer.advance();
        if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
          COMPILATIONERROR("varName");
        }

        outputWriter.println( tabString + "<identifier> " + tokenizer.identifier() + " </identifier>");
        name = tokenizer.getCurrentToken();
        symbolTable.define(name, type, kind);
        outputWriter.println(tabString + "<Symbol_Table_Entry> name: " + name + " type: "
            + symbolTable.typeOf(name) + " kind: " + symbolTable.kindOf(name) + " #: "
            + symbolTable.indexOf(name) + " </Symbol_Table_Entry>" );
        tokenizer.advance();
      }
      tokenizer.decrementIndex();
      // Getting ;
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ';' ) {
        COMPILATIONERROR(";");
      }
      outputWriter.println( tabString + "<symbol> " + tokenizer.symbol() + " </symbol>");
      tokenizer.advance();
      tabString = tabString.substring(0, tabString.length() - 1);
      outputWriter.println(tabString + "</classVarDec>");
    }
    tokenizer.decrementIndex();

  }
  /**
   *  Subroutine variable of the jack Grammar
   */
  public void compileSubroutine() {
    tokenizer.advance();
    localSymbolTable = new SymbolTable();
    while (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '}') {
      outputWriter.println(tabString + "<subroutineDec>");
      tabString += "\t";
      // Getting method, function, constructor
      if ( tokenizer.tokenType() != TokenType.KEYWORD
          && ( tokenizer.keyword() != KeywordType.CONSTRUCTOR
          || tokenizer.keyword() != KeywordType.FUNCTION
          || tokenizer.keyword() != KeywordType.METHOD ) ) {
        COMPILATIONERROR("method, function, or constructor");
      }
      outputWriter.println( tabString + "<keyword> " + tokenizer.getCurrentToken() + " </keyword>" );
      // Getting int, char, boolean, void, className
      tokenizer.advance();
      if ( tokenizer.tokenType() == TokenType.IDENTIFIER ) {
        outputWriter.println(tabString + "<identifier> " + tokenizer.identifier() + " </identifier>");
      }
      if ( tokenizer.tokenType() == TokenType.KEYWORD
          && ( tokenizer.keyword() == KeywordType.BOOLEAN
          || tokenizer.keyword() == KeywordType.INT
          || tokenizer.keyword() == KeywordType.CHAR
          || tokenizer.keyword() == KeywordType.VOID ) ) {
        outputWriter.println(tabString + "<keyword> " + tokenizer.getCurrentToken() + " </keyword>");
      }
      // Getting subroutineName
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
        COMPILATIONERROR( "subroutineName" );
      }
      outputWriter.println(tabString + "<identifier> " + tokenizer.identifier() + " </identifier>");
      //  Getting (
      tokenizer.advance();
      if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '(') {
        COMPILATIONERROR("(");
      }
      outputWriter.println(tabString + "<symbol> ( </symbol>");
      compileParameterList();
      // Getting )
      tokenizer.advance();
      if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')') {
        COMPILATIONERROR(")");
      }
      outputWriter.println(tabString + "<symbol> ) </symbol>");

      compileSubroutineBody();
      localSymbolTable.reset();
      tabString = tabString.substring(0, tabString.length() - 1);
      outputWriter.println(tabString + "</subroutineDec>");
      tokenizer.advance();
    }
    tokenizer.decrementIndex();
  }
  /**
   * Parameter list variable of the Jack Grammar
   */
  public void compileParameterList() {
    tokenizer.advance();
    outputWriter.println(tabString + "<parameterList>");
    tabString += "\t";
    if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')') {
//      tokenizer.decrementIndex();
      tabString = tabString.substring(0, tabString.length() - 1);
      tokenizer.decrementIndex();
      outputWriter.println(tabString + "</parameterList>");
      return;
    }
    // Getting type
    if ( tokenizer.tokenType() == TokenType.IDENTIFIER ) {
      outputWriter.println( tabString + "<identifier> " + tokenizer.identifier() + " </identifier>" );
    }
    if ( tokenizer.tokenType() == TokenType.KEYWORD
        && ( tokenizer.keyword() == KeywordType.BOOLEAN
        || tokenizer.keyword() == KeywordType.INT
        || tokenizer.keyword() == KeywordType.CHAR ) ) {
      outputWriter.println( tabString + "<keyword> " + tokenizer.getCurrentToken() + " </keyword>" );
    }
    String type = tokenizer.identifier();
    // Getting varName
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
      COMPILATIONERROR("varName");
    }
    outputWriter.println( tabString + "<identifier> " + tokenizer.identifier() + " </identifier>");
    String name = tokenizer.identifier();
    localSymbolTable.define(name, type, "arg");
    outputWriter.println(tabString + "<Symbol_Table_Entry> name: " + name + " type: "
        + localSymbolTable.typeOf(name) + " kind: " + localSymbolTable.kindOf(name) + " #: "
        + localSymbolTable.indexOf(name) + " </Symbol_Table_Entry>" );
    // Getting , type varName
    tokenizer.advance();
    while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ',') {
      // Getting ,
      outputWriter.println( tabString + "<symbol> " + tokenizer.symbol() + " </symbol>" );
      // Getting type
      tokenizer.advance();
      if ( tokenizer.tokenType() == TokenType.IDENTIFIER ) {
        outputWriter.println( tabString + "<identifier> " + tokenizer.identifier()
            + " </identifier>" );
      }
      if ( tokenizer.tokenType() == TokenType.KEYWORD && ( tokenizer.keyword() == KeywordType.BOOLEAN
          || tokenizer.keyword() == KeywordType.INT || tokenizer.keyword() == KeywordType.CHAR ) ) {
        outputWriter.println( tabString + "<keyword> " + tokenizer.getCurrentToken()
            + " </keyword>" );
      }
      type = tokenizer.identifier();
      // Getting varName
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
        COMPILATIONERROR("varName");
      }
      outputWriter.println( tabString + "<identifier> " + tokenizer.identifier() + " </identifier>");
      name = tokenizer.identifier();
      localSymbolTable.define(name, type, "arg");
      outputWriter.println(tabString + "<Symbol_Table_Entry> name: " + name + " type: "
          + localSymbolTable.typeOf(name) + " kind: " + localSymbolTable.kindOf(name) + " #: "
          + localSymbolTable.indexOf(name) + " </Symbol_Table_Entry>" );
      tokenizer.advance();
    }
    tokenizer.decrementIndex();

    tabString = tabString.substring(0, tabString.length() - 1);
    outputWriter.println(tabString + "</parameterList>");
  }

  /**
   * Subroutine Body variable for the Jack Grammar
   */
  public void compileSubroutineBody() {
    tokenizer.advance();
    outputWriter.println(tabString + "<subroutineBody>");
    tabString += "\t";
    if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '{' ){
      COMPILATIONERROR("{");
    }
    outputWriter.println(tabString + "<symbol> { </symbol>");
    compileVarDec();
    compileStatements();

    // Getting }
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '}') {
      COMPILATIONERROR("}");
    }
    outputWriter.println(tabString + "<symbol> } </symbol>");

    tabString = tabString.substring(0, tabString.length() - 1);
    outputWriter.println(tabString + "</subroutineBody>");
  }
  /**
   * Variable Declaration variable of the Jack Grammar
   */
  public void compileVarDec() {
    tokenizer.advance();
    // Checking if there are any var to declare
    if ( tokenizer.tokenType() == TokenType.KEYWORD && ( tokenizer.keyword() == KeywordType.LET
        ||  tokenizer.keyword() == KeywordType.DO ||  tokenizer.keyword() == KeywordType.RETURN
        ||  tokenizer.keyword() == KeywordType.WHILE ||  tokenizer.keyword() == KeywordType.IF ) ) {
      tokenizer.decrementIndex();
      return;
    }
    String name = "";
    String type = "";
    String kind = "";

    // Getting var type varName
    while ( tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyword() == KeywordType.VAR ) {
      outputWriter.println(tabString + "<varDec>");
      tabString += "\t";
      outputWriter.println(tabString + "<keyword>  var </keyword>");
      kind = "var";
      // Getting int, char, boolean, className
      tokenizer.advance();
      if ( tokenizer.tokenType() == TokenType.IDENTIFIER ) {
        outputWriter.println(tabString + "<identifier> " + tokenizer.identifier() + "</identifier>");
      }
      if ( tokenizer.tokenType() == TokenType.KEYWORD
          && ( tokenizer.keyword() == KeywordType.BOOLEAN
          || tokenizer.keyword() == KeywordType.INT
          || tokenizer.keyword() == KeywordType.CHAR ) ) {
        outputWriter.println( tabString + "<keyword> " + tokenizer.getCurrentToken() + " </keyword>" );
      }
      type = tokenizer.getCurrentToken();
      // Getting varName
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
        COMPILATIONERROR("varName");
      }
      outputWriter.println( tabString + "<identifier> " + tokenizer.identifier() + " </identifier>");
      name = tokenizer.identifier();
      localSymbolTable.define(name, type, kind);
      outputWriter.println(tabString + "<Symbol_Table_Entry> name: " + name + " type: "
          + localSymbolTable.typeOf(name) + " kind: " + localSymbolTable.kindOf(name) + " #: "
          + localSymbolTable.indexOf(name) + " </Symbol_Table_Entry>" );
      // Getting , varName
      tokenizer.advance();
      while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ',') {
        outputWriter.println( tabString + "<symbol> , </symbol>" );
        tokenizer.advance();
        if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
          COMPILATIONERROR("varName");
        }
        outputWriter.println( tabString + "<identifier> " + tokenizer.identifier() + " </identifier>");
        name = tokenizer.identifier();
        localSymbolTable.define(name, type, kind);
        outputWriter.println(tabString + "<Symbol_Table_Entry> name: " + name + " type: "
            + localSymbolTable.typeOf(name) + " kind: " + localSymbolTable.kindOf(name) + " #: "
            + localSymbolTable.indexOf(name) + " </Symbol_Table_Entry>" );
        tokenizer.advance();
      }
      tokenizer.decrementIndex();
      // Getting ;
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ';' ) {
        COMPILATIONERROR(";");
      }
      outputWriter.println( tabString + "<symbol> " + tokenizer.symbol() + " </symbol>");
      tokenizer.advance();
      tabString = tabString.substring(0, tabString.length() - 1);
      outputWriter.println(tabString + "</varDec>");
    }
    tokenizer.decrementIndex();
  }
  /**
   * Statements variable of the Jack Grammar
   */
  public void compileStatements() {
    tokenizer.advance();
    outputWriter.println(tabString + "<statements>");
    tabString += "\t";
    while (tokenizer.tokenType() == TokenType.KEYWORD) {
      if ( tokenizer.keyword() == KeywordType.LET) {
        compileLet();
      } else if ( tokenizer.keyword() == KeywordType.DO ) {
        compileDo();
      } else if ( tokenizer.keyword() == KeywordType.RETURN ) {
        compileReturn();
      } else if ( tokenizer.keyword() == KeywordType.WHILE ) {
        compileWhile();
      } else if ( tokenizer.keyword() == KeywordType.IF ) {
        compileIf();
      }
      tokenizer.advance();
    }
    tokenizer.decrementIndex();

    tabString = tabString.substring(0, tabString.length() - 1);
    outputWriter.println(tabString + "</statements>");
  }
  /**
   * Let variable of the Jack Grammar
   */
  public void compileLet() {
    outputWriter.println(tabString + "<letStatement>");
    tabString += "\t";
    outputWriter.println(tabString + "<keyword> let </keyword>");
    // Getting varName
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
      COMPILATIONERROR("varName");
    }
    String name = tokenizer.identifier();
    if ( !localSymbolTable.contains(name) ) {
      if ( !symbolTable.contains(name) ) {
        UNIDENTIFIEDSYMBOLERROR( name );
      }
    }
    outputWriter.println( tabString + "<identifier> " + tokenizer.identifier() + " </identifier>");
    // Checking [
    tokenizer.advance();
    if ( tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '[' ) {
      outputWriter.println(tabString + "<symbol> [ </symbol>");
      compileExpression();
      // Getting ]
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ']' ) {
        COMPILATIONERROR("]");
      }
      outputWriter.println(tabString + "<symbol> ] </symbol>");
      tokenizer.advance();
    }
    // Getting =
    if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '=' ) {
      COMPILATIONERROR("=");
    }
    outputWriter.println(tabString + "<symbol> = </symbol>");
    // Getting expression
    compileExpression();
    // Getting ;
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ';' ) {
      COMPILATIONERROR(";");
    }
    outputWriter.println(tabString + "<symbol> ; </symbol>");

    tabString = tabString.substring(0, tabString.length() - 1);
    outputWriter.println(tabString + "</letStatement>");
  }
  /**
   * If variable of the Jack Grammar
   */
  public void compileIf() {

    outputWriter.println(tabString + "<ifStatement>");
    tabString += "\t";
    outputWriter.println(tabString + "<keyword> if </keyword>");
    // Getting (
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '(') {
      COMPILATIONERROR("(");
    }
    outputWriter.println(tabString + "<symbol> ( </symbol>");

    compileExpression();

    // Getting )
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')') {
      COMPILATIONERROR(")");
    }
    outputWriter.println(tabString + "<symbol> ) </symbol>");
    // Getting {
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '{') {
      COMPILATIONERROR("{");
    }
    outputWriter.println(tabString + "<symbol> { </symbol>");
    // Getting statements
    compileStatements();
    // Getting }
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '}') {
      COMPILATIONERROR("}");
    }
    outputWriter.println(tabString + "<symbol> } </symbol>");
    // Checking for else
    tokenizer.advance();
    boolean hasElse = false;
    if ( tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyword() == KeywordType.ELSE ) {
      outputWriter.println(tabString + "<keyword> else </keyword>");
      hasElse = true;
      // Getting {
      tokenizer.advance();
      if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '{') {
        COMPILATIONERROR("{");
      }
      outputWriter.println(tabString + "<symbol> { </symbol>");
      // Getting statements
      compileStatements();
      // Getting }
      tokenizer.advance();
      if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '}') {
        COMPILATIONERROR("}");
      }
      outputWriter.println(tabString + "<symbol> } </symbol>");
    }
    if ( !hasElse ) tokenizer.decrementIndex();

    tabString = tabString.substring(0, tabString.length() - 1);
    outputWriter.println(tabString + "</ifStatement>");
  }

  /**
   * While variable of the Jack Grammar
   */
  public void compileWhile() {
    outputWriter.println(tabString + "<whileStatement>");
    tabString += "\t";
    outputWriter.println(tabString + "<keyword> while </keyword>");
    // Getting (
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '(') {
      COMPILATIONERROR("(");
    }
    outputWriter.println(tabString + "<symbol> ( </symbol>");
    // Getting Expression
    compileExpression();
    // Getting )
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')') {
      COMPILATIONERROR(")");
    }
    outputWriter.println(tabString + "<symbol> ) </symbol>");
    // Getting {
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '{') {
      COMPILATIONERROR("{");
    }
    outputWriter.println(tabString + "<symbol> { </symbol>");
    // Getting statements
    compileStatements();
    // Getting }
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '}') {
      COMPILATIONERROR("}");
    }
    outputWriter.println(tabString + "<symbol> } </symbol>");

    tabString = tabString.substring(0, tabString.length() - 1);
    outputWriter.println(tabString + "</whileStatement>");
  }
  /**
   * Do variable of the Jack Grammar
   */
  public void compileDo() {
    outputWriter.println(tabString + "<doStatement>");
    tabString += "\t";
    outputWriter.println(tabString + "<keyword> do </keyword>");
    // Getting subroutineName | className | varName
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
      COMPILATIONERROR("subroutineName, className, varName");
    }
    outputWriter.println(tabString + "<identifier> " + tokenizer.identifier() + " </identifier>");
    // Checking for .
    tokenizer.advance();
    boolean hasDot = false;
    if ( tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '.' ) {
      outputWriter.println(tabString + "<symbol> . </symbol>");
      hasDot = true;
      // Getting subroutineName
      tokenizer.advance();
      if (tokenizer.tokenType() != TokenType.IDENTIFIER ) {
        COMPILATIONERROR("subroutineName");
      }
      outputWriter.println(tabString + "<identifier> " + tokenizer.identifier() + " </identifier>");
    }
    if ( !hasDot ) tokenizer.decrementIndex();
    // Getting (
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '(') {
      COMPILATIONERROR("(");
    }
    outputWriter.println(tabString + "<symbol> ( </symbol>");
    // Getting ExpressionList
    compileExpressionList();
    // Getting )
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')') {
      COMPILATIONERROR(")");
    }
    outputWriter.println(tabString + "<symbol> ) </symbol>");
    // Getting ;
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ';') {
      COMPILATIONERROR(";");
    }
    outputWriter.println(tabString + "<symbol> ; </symbol>");
    tabString = tabString.substring(0, tabString.length() - 1);
    outputWriter.println(tabString + "</doStatement>");
  }
  /**
   * Return variable of the Jack Grammar
   */
  public void compileReturn() {
    outputWriter.println(tabString + "<returnStatement>");
    tabString += "\t";
    outputWriter.println(tabString + "<keyword> return </keyword>");
    //Checking for expression to return
    tokenizer.advance();
    boolean hasExpression = false;
    if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ';' ) {
      hasExpression = true;
      tokenizer.decrementIndex();
      compileExpression();
    }
    if (!hasExpression) {
      tokenizer.decrementIndex();
    }
    // Getting ;
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ';' ) {
      COMPILATIONERROR(";");
    }
    outputWriter.println(tabString + "<symbol> ; </symbol>");
    tabString = tabString.substring(0, tabString.length() - 1);
    outputWriter.println(tabString + "</returnStatement>");
  }
  /**
   * Expression variable of the Jack Grammar
   */
  public void compileExpression() {
    outputWriter.println(tabString + "<expression>");
    tabString += "\t";
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')' ) {
      tokenizer.decrementIndex();
      tabString = tabString.substring(0, tabString.length() - 1);
      outputWriter.println(tabString + "</expression>");
      return;
    }
    tokenizer.decrementIndex();
    // Getting term
    compileTerm();
    // Checking for (op term)*
    tokenizer.advance();
    while ( tokenizer.tokenType() == TokenType.SYMBOL && ( tokenizer.symbol() == '+'
        ||  tokenizer.symbol() == '-' ||  tokenizer.symbol() == '*'
        ||  tokenizer.symbol() == '/' ||  tokenizer.symbol() == '&'
        ||  tokenizer.symbol() == '|' ||  tokenizer.symbol() == '<'
        ||  tokenizer.symbol() == '>' ||  tokenizer.symbol() == '=' ) ) {
      outputWriter.println(tabString + "<symbol> " + tokenizer.getCurrentToken() + " </symbol>");
      // Getting term
      compileTerm();
      tokenizer.advance();
    }
    tokenizer.decrementIndex();
    tabString = tabString.substring(0, tabString.length() - 1);
    outputWriter.println(tabString + "</expression>");
  }
  /**
   * Term variable of the Jack Grammar
   */
  public void compileTerm() {
    outputWriter.println(tabString + "<term>");
    tabString += "\t";
    tokenizer.advance();
    if ( tokenizer.tokenType() == TokenType.KEYWORD ) {
      if ( tokenizer.keyword() == KeywordType.TRUE || tokenizer.keyword() == KeywordType.FALSE
          || tokenizer.keyword() == KeywordType.NULL || tokenizer.keyword() == KeywordType.THIS ) {
        outputWriter.println(tabString + "<keyword> "
            + tokenizer.getCurrentToken() + " </keyword>");
      }
    } else if ( tokenizer.tokenType() == TokenType.STRING_CONST ) {
      outputWriter.println(tabString + "<stringConstant> "
          + tokenizer.stringVal() + " </stringConstant>");
    } else if ( tokenizer.tokenType() == TokenType.INT_CONST ) {
      outputWriter.println(tabString + "<integerConstant> "
          + tokenizer.intVal() + " </integerConstant>");
    } else if ( tokenizer.tokenType() == TokenType.SYMBOL ) {
      if (tokenizer.symbol() == '~' || tokenizer.symbol() == '-') {
        outputWriter.println(tabString + "<symbol> " + tokenizer.symbol() + " </symbol>");
        // Getting term
        compileTerm();
      } else if (tokenizer.symbol() == '(' ) {
        outputWriter.println(tabString + "<symbol> ( </symbol>");
        // Getting expression
        compileExpression();
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')' ) {
          COMPILATIONERROR(")");
        }
        outputWriter.println(tabString + "<symbol> ) </symbol>");
      }
    } else if ( tokenizer.tokenType() == TokenType.IDENTIFIER ) {
      String varName = tokenizer.identifier();
      tokenizer.advance();
      if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '['){
        outputWriter.print(tabString + "<identifier> " + varName + " </identifier>\n");
        String name = varName;
        if ( !localSymbolTable.contains(name) ) {
          if ( !symbolTable.contains(name) ) {
            UNIDENTIFIEDSYMBOLERROR( name );
          }
        }
        //this is an array entry
        outputWriter.print(tabString + "<symbol> [ </symbol>\n");
        //expression
        compileExpression();
        //']'
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ']') {
           COMPILATIONERROR("]");
        }
        outputWriter.println(tabString + "<symbol> ] </symbol>");
      } else if (tokenizer.tokenType() == TokenType.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')){
        //this is a subroutineCall
        tokenizer.decrementIndex();
        outputWriter.print(tabString + "<identifier> " + varName + " </identifier>\n");
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '('){
          // Getting (
          outputWriter.print(tabString + "<symbol> ( </symbol>\n");
          //Getting expressionList
          compileExpressionList();
          // Getting )
          tokenizer.advance();
          if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')') {
            COMPILATIONERROR(")");
          }
          outputWriter.println(tabString + "<symbol> ) </symbol>");
        }else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '.'){
          // (className|varName) '.' subroutineName '(' expressionList ')'
          outputWriter.print(tabString + "<symbol> . </symbol>\n");
          //subroutineName
          tokenizer.advance();
          if (tokenizer.tokenType() != TokenType.IDENTIFIER){
            COMPILATIONERROR("identifier");
          }
          outputWriter.print(tabString + "<identifier> " + tokenizer.identifier() + " </identifier>\n");
          //'('
          tokenizer.advance();
          if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '(') {
            COMPILATIONERROR("(");
          }
          outputWriter.println(tabString + "<symbol> ( </symbol>");
          //expressionList
          compileExpressionList();
          //')'
          tokenizer.advance();
          if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')') {
            COMPILATIONERROR(")");
          }
          outputWriter.println(tabString + "<symbol> ) </symbol>");
        }else {
          COMPILATIONERROR("'('|'.'");
        }
      }else {
        outputWriter.print(tabString + "<identifier> " + varName + " </identifier>\n");
        //this is varName
        tokenizer.decrementIndex();
      }
    }
    tabString = tabString.substring(0, tabString.length() - 1);
    outputWriter.println(tabString + "</term>");
  }
  /**
   * Expression variable of the Jack Grammar
   *
   * @return The number of expressions compiled
   */
  public int compileExpressionList() {
    int numExpressions = 0;
    tokenizer.advance();
    outputWriter.println(tabString + "<expressionList>");
    tabString += "\t";
    if ( tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')' ) {
      tokenizer.decrementIndex();
      tabString = tabString.substring(0, tabString.length() - 1);
      outputWriter.println(tabString + "</expressionList>");
      return numExpressions;
    } else {
      numExpressions++;
      tokenizer.decrementIndex();
      compileExpression();
      // Checking for , expression
      tokenizer.advance();
      while ( tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ',' ) {
        outputWriter.println(tabString + "<symbol> , </symbol>");
        numExpressions++;
        // Getting next expression
        compileExpression();
        tokenizer.advance();
      }
      tokenizer.decrementIndex();
      tabString = tabString.substring(0, tabString.length() - 1);
      outputWriter.println(tabString + "</expressionList>");
    }
    return numExpressions;
  }
  /**
   * This method is used to close the outputWriter, so we can write to the output file prior to
   * ending
   */
  public void close() {
    outputWriter.close();
  }
  /**
   * This method is used to output compilation (CFG is not accepting the language provided) errors.
   *
   * @param missingComponent: The token that was supposed to be provided.
   */
  private void COMPILATIONERROR(String missingComponent){
    throw new IllegalStateException("Expected token missing: " + missingComponent
        + ", Current token: " + tokenizer.getCurrentToken());
  }

  /**
   * THis method is used to output unidentified symbol errors. If a identifier is not in our symbol
   * table
   *
   * @param unidentifiedSymbol: Symbol in question
   */
  private void UNIDENTIFIEDSYMBOLERROR(String unidentifiedSymbol){
    throw new IllegalStateException("Unidentified symbol: " + unidentifiedSymbol + "\n");
  }

  /**
   * This is the constructor of the CompilationEngine class.
   *
   * @param inputFile: The file to read in as a .jack file.
   * @param OutputFile: The file to output to as a .vm file.
   */
  public CompilationEngine(File inputFile, File OutputFile) {
    tokenizer = new Tokenizer(inputFile);
    symbolTable = new SymbolTable();
    outputWriter = null;
    File outputFile = new File(OutputFile.getPath());
    try {
      outputWriter = new PrintWriter( new FileWriter(outputFile) );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
