import java.io.File;
import java.util.HashMap;

public class CompilationEngine {

  /**
   * The instance of the symbol table that we will use throughout compilation.
   */
  private final SymbolTable symbolTable;
  /**
   * The VMWriter used to write output VM code.
   */
  private final VMWriter writer;
  /**
   * The instance of the tokenizer that will be used throughout compilation.
   */
  private final Tokenizer tokenizer;
  /**
   * The current Class we are working in. Used for function names as different classes may have
   * the same function names.
   */
  private String currentClass;
  /**
   * The current subroutine we are working on. Used for function names for ease.
   */
  private String currentSubroutine;
  /**
   * The hashmap containing symbol to VM code conversions of the operations possible.
   * + -> add
   * - -> sub
   * etc.
   */
  private HashMap<String, String> operationHashMap;
  /**
   * This is the label index. Used for creating new labels
   */
  private int labelIndex = 0;
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
    // Getting className
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
      COMPILATIONERROR("className");
    }
    currentClass = tokenizer.identifier();
    // Getting {
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '{' ) {
      COMPILATIONERROR("{");
    }
    // Compile field/static variables then other subroutines within class
    compileClassVarDec();
    compileSubroutine();
    // Getting }
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '}' ) {
      COMPILATIONERROR("}");
    }
    writer.close();
  }

  /**
   * Class Variable Declaration variable of the Jack Grammar.
   */
  public void compileClassVarDec() {
    tokenizer.advance();
    String name;
    String type;
    String kind;
    while (tokenizer.tokenType() == TokenType.KEYWORD && ( tokenizer.keyword() == KeywordType.FIELD
        || tokenizer.keyword() == KeywordType.STATIC ) ) {
      // Getting static or field

      kind = tokenizer.getCurrentToken();
      // Getting int, char, boolean, className
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.IDENTIFIER
          && tokenizer.tokenType() != TokenType.KEYWORD
          && ( tokenizer.keyword() == KeywordType.BOOLEAN
          || tokenizer.keyword() == KeywordType.INT
          || tokenizer.keyword() == KeywordType.CHAR )) {
        COMPILATIONERROR("int|char|boolean|className");
      }
      type = tokenizer.getCurrentToken();
      // Getting varName
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
        COMPILATIONERROR("varName");
      }
      name = tokenizer.identifier();
      symbolTable.define(name, type, kind);

      // Checking for , varName
      tokenizer.advance();
      while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ',') {
        // Get varName
        tokenizer.advance();
        if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
          COMPILATIONERROR("varName");
        }
        name = tokenizer.getCurrentToken();
        symbolTable.define(name, type, kind);
        tokenizer.advance();
      }
      tokenizer.decrementIndex();
      // Getting ;
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ';' ) {
        COMPILATIONERROR(";");
      }
      tokenizer.advance();
    }
    tokenizer.decrementIndex();
  }
  /**
   *  Subroutine variable of the jack Grammar
   */
  public void compileSubroutine() {
    tokenizer.advance();
    while (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '}') {
      boolean isMethod = false;
      boolean isConstructor = false;
      // Getting method, function, constructor
      if ( tokenizer.tokenType() == TokenType.KEYWORD ) {
        if ( tokenizer.keyword() == KeywordType.CONSTRUCTOR ) {
          isConstructor = true;
        } else if ( tokenizer.keyword() == KeywordType.METHOD ) {
          symbolTable.define("this", currentClass, "arg");
          isMethod = true;
        } else if ( tokenizer.keyword() == KeywordType.FUNCTION ) {

        } else {
          COMPILATIONERROR("method, function, or constructor");
        }
        // Getting int, char, boolean, void, className
        tokenizer.advance();
        if ( tokenizer.tokenType() != TokenType.IDENTIFIER
            && tokenizer.tokenType() != TokenType.KEYWORD
            && ( tokenizer.keyword() != KeywordType.BOOLEAN
            || tokenizer.keyword() != KeywordType.INT
            || tokenizer.keyword() != KeywordType.CHAR
            || tokenizer.keyword() != KeywordType.VOID ) ) {
          COMPILATIONERROR("int|char|boolean|void|className");
        }
        // Getting subroutineName
        tokenizer.advance();

        if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
          COMPILATIONERROR( "subroutineName" );
        }
        currentSubroutine = tokenizer.identifier();
        //  Getting (
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '(') {
          COMPILATIONERROR("(");
        }
        compileParameterList();
        // Getting )
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')') {
          COMPILATIONERROR(")");
        }
        compileSubroutineBody(isMethod, isConstructor);
        symbolTable.reset();
        tokenizer.advance();
      }
    }
    tokenizer.decrementIndex();
  }
  /**
   * Parameter list variable of the Jack Grammar
   */
  public void compileParameterList() {
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')') {
      tokenizer.decrementIndex();
      return;
    }
    // Getting type
    if ( tokenizer.tokenType() != TokenType.IDENTIFIER
        && tokenizer.tokenType() != TokenType.KEYWORD
        && ( tokenizer.keyword() != KeywordType.BOOLEAN
        || tokenizer.keyword() != KeywordType.INT
        || tokenizer.keyword() != KeywordType.CHAR ) ) {
      COMPILATIONERROR("int|char|boolean|className");
    }
    String type = tokenizer.identifier();
    // Checking varName
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
      COMPILATIONERROR("varName");
    }
    String name = tokenizer.identifier();
    symbolTable.define(name, type, "arg");
    // Checking , type varName
    tokenizer.advance();
    while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ',') {
      // Checking type
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.IDENTIFIER
          && tokenizer.tokenType() != TokenType.KEYWORD
          && ( tokenizer.keyword() != KeywordType.BOOLEAN
          || tokenizer.keyword() != KeywordType.INT
          || tokenizer.keyword() != KeywordType.CHAR ) ) {
        COMPILATIONERROR("int|char|boolean|className");
      }
      type = tokenizer.identifier();
      // Checking varName
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
        COMPILATIONERROR("varName");
      }
      name = tokenizer.identifier();
      symbolTable.define(name, type, "arg");
      tokenizer.advance();
    }
    tokenizer.decrementIndex();
  }
  /**
   * Subroutine Body variable for the Jack Grammar
   *
   * @param isMethod:      Used to know if we need to initialize VM code for method creation.
   * @param isConstructor: Used to know if we need to initialize VM code for constructor creation.
   */
  public void compileSubroutineBody(boolean isMethod, boolean isConstructor) {
    tokenizer.advance();
    // Checking {
    if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '{' ){
      COMPILATIONERROR("{");
    }
    compileVarDec();
    // Writing function
    writer.writeFunction(currentClass + "." + currentSubroutine, symbolTable.varCount("var"));

    if ( isMethod ) {
      writer.writePush(VMWriter.SegmentEnum.ARGUMENT, 0);
      writer.writePop(VMWriter.SegmentEnum.POINTER,0);
    } else if ( isConstructor ) {
      writer.writePush(VMWriter.SegmentEnum.CONSTANT, symbolTable.varCount("field"));
      writer.writeCall("Memory.alloc", 1);
      writer.writePop(VMWriter.SegmentEnum.POINTER,0);
    }
    compileStatements();
    // Getting }
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '}') {
      COMPILATIONERROR("}");
    }
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
    String name;
    String type;
    String kind;
    // Getting var type varName
    while ( tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyword() == KeywordType.VAR ) {
      kind = "var";
      // Checking int, char, boolean, className
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.IDENTIFIER
          && tokenizer.tokenType() != TokenType.KEYWORD
          && ( tokenizer.keyword() == KeywordType.BOOLEAN
          || tokenizer.keyword() == KeywordType.INT
          || tokenizer.keyword() == KeywordType.CHAR ) ) {
        COMPILATIONERROR("int|char|boolean|className");
      }
      type = tokenizer.getCurrentToken();
      // Checking varName
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
        COMPILATIONERROR("varName");
      }
      name = tokenizer.identifier();
      symbolTable.define(name, type, kind);
      // Checking for , varName
      tokenizer.advance();
      while (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ',') {
        tokenizer.advance();
        // Checking varName
        if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
          COMPILATIONERROR("varName");
        }
        name = tokenizer.identifier();
        symbolTable.define(name, type, kind);
        tokenizer.advance();
      }
      tokenizer.decrementIndex();
      // Checking ;
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ';' ) {
        COMPILATIONERROR(";");
      }
      tokenizer.advance();
    }
    tokenizer.decrementIndex();
  }
  /**
   * Statements variable of the Jack Grammar
   */
  public void compileStatements() {
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.KEYWORD ) {
      COMPILATIONERROR("keyword");
    }
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
      } else {
        COMPILATIONERROR("let | do | return | while | if");
      }
      tokenizer.advance();
    }
    tokenizer.decrementIndex();
  }
  /**
   * Let variable of the Jack Grammar
   */
  public void compileLet() {
    // Getting varName
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
      COMPILATIONERROR("varName");
    }
    String name = tokenizer.identifier();
    if ( !symbolTable.contains(name) ) {
      if ( !symbolTable.contains(name) ) {
        UNIDENTIFIEDSYMBOLERROR( name );
      }
    }
    // Checking [
    tokenizer.advance();
    boolean hasExpression = false;
    if ( tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '[' ) {
      hasExpression = true;
      writer.writePush(getSeg(symbolTable.kindOf( name )), symbolTable.indexOf( name ));
      // Getting index
      compileExpression();
      // Checking ]
      tokenizer.advance();
      if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ']' ) {
        COMPILATIONERROR("]");
      }
      writer.writeArithmetic(operationHashMap.get("+"));
      tokenizer.advance();
    }
    // Checking =
    if ( tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '=' ) {
      COMPILATIONERROR("=");
    }
    // Getting expression
    compileExpression();
    // Getting ;
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ';' ) {
      COMPILATIONERROR(";");
    }
    if (hasExpression){
      //*(base+offset) = expression
      //pop expression value to temp
      writer.writePop(VMWriter.SegmentEnum.TEMP,0);
      //pop base+index into 'that'
      writer.writePop(VMWriter.SegmentEnum.POINTER,1);
      //pop expression value into *(base+index)
      writer.writePush(VMWriter.SegmentEnum.TEMP,0);
      writer.writePop(VMWriter.SegmentEnum.THAT,0);
    }else {
      //pop expression value directly
      writer.writePop(getSeg(symbolTable.kindOf( name )), symbolTable.indexOf( name ));
    }
  }
  /**
   * If variable of the Jack Grammar
   */
  public void compileIf() {
    String elseLabel = newLabel();
    String endLabel = newLabel();
    // Checking (
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '(') {
      COMPILATIONERROR("(");
    }
    compileExpression();
    // Checking )
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')') {
      COMPILATIONERROR(")");
    }
    //if ~(condition) go to else label
    writer.writeArithmetic("not");
    writer.writeIf(elseLabel);
    // Checking {
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '{') {
      COMPILATIONERROR("{");
    }
    // Checking statements
    compileStatements();
    // Checking }
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '}') {
      COMPILATIONERROR("}");
    }
    writer.writeGoto(endLabel);
    // Checking for else
    writer.writeLabel(elseLabel);
    tokenizer.advance();
    boolean hasElse = false;
    if ( tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyword() == KeywordType.ELSE ) {
      hasElse = true;
      // Getting {
      tokenizer.advance();
      if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '{') {
        COMPILATIONERROR("{");
      }
      // Checking statements
      compileStatements();
      // Checking }
      tokenizer.advance();
      if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '}') {
        COMPILATIONERROR("}");
      }
    }
    if ( !hasElse ) tokenizer.decrementIndex();
    writer.writeLabel(endLabel);
  }

  /**
   * This method is used for generating new label strings for if/while statements.
   *
   * @return The new label.
   */
  private String newLabel(){
    return "LABEL_" + (labelIndex++);
  }
  /**
   * While variable of the Jack Grammar
   */
  public void compileWhile() {
    String continueLabel = newLabel();
    String topLabel = newLabel();
    //top label
    writer.writeLabel(topLabel);
    // Getting (
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '(') {
      COMPILATIONERROR("(");
    }
    // Getting Expression
    compileExpression();
    // Getting )
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')') {
      COMPILATIONERROR(")");
    }
    //if ~(condition) go to continue label
    writer.writeArithmetic("not");
    writer.writeIf(continueLabel);
    // Getting {
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '{') {
      COMPILATIONERROR("{");
    }
    // Getting statements
    compileStatements();
    // Getting }
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '}') {
      COMPILATIONERROR("}");
    }
    //if (condition) to the top
    writer.writeGoto(topLabel);
    //else end loop
    writer.writeLabel(continueLabel);
  }
  /**
   * Do variable of the Jack Grammar
   */
  public void compileDo() {
    // Checking subroutineName | className | varName
    tokenizer.advance();
    if ( tokenizer.tokenType() != TokenType.IDENTIFIER ) {
      COMPILATIONERROR("subroutineName, className, varName");
    }
    String name = tokenizer.identifier();
    int nArgs = 0;
    // Checking for .
    tokenizer.advance();
    boolean hasDot = false;
    if ( tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '.' ) {
      // Method or function call
      String objectName = name;
      hasDot = true;
      String kindType = symbolTable.typeOf( objectName );
      // Getting subroutineName
      tokenizer.advance();
      if (tokenizer.tokenType() != TokenType.IDENTIFIER ) {
        COMPILATIONERROR("subroutineName");
      }
      name = tokenizer.identifier();

      if ( kindType.equals("") ) {
        name = objectName + "." + name;
      } else {
        nArgs = 1;
        writer.writePush(getSeg(symbolTable.kindOf( objectName ))
            , symbolTable.indexOf( objectName ));
        name = symbolTable.typeOf( objectName ) + "." + name;
      }
    }
    if ( !hasDot ) tokenizer.decrementIndex();
    // Getting (
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '(') {
      COMPILATIONERROR("(");
    }
    // Getting ExpressionList
    nArgs += compileExpressionList();
    // Getting )
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')') {
      COMPILATIONERROR(")");
    }
    // Getting ;
    tokenizer.advance();
    if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ';') {
      COMPILATIONERROR(";");
    }
    writer.writeCall(name, nArgs);
  }
  private VMWriter.SegmentEnum getSeg(SymbolTable.kindEnum kind){
    return switch (kind) {
      case FIELD -> VMWriter.SegmentEnum.THIS;
      case STATIC -> VMWriter.SegmentEnum.STATIC;
      case VAR -> VMWriter.SegmentEnum.LOCAL;
      case ARG -> VMWriter.SegmentEnum.ARGUMENT;
      default -> VMWriter.SegmentEnum.NONE;
    };
  }
  /**
   * Return variable of the Jack Grammar
   */
  public void compileReturn() {
    //Checking for expression to return
    tokenizer.advance();
    if ( tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ';' ) {
      writer.writePush(VMWriter.SegmentEnum.CONSTANT, 0);
    } else {
      tokenizer.decrementIndex();
      compileExpression();
      // Getting ;
      tokenizer.advance();
    }
    writer.writeReturn();
  }
  /**
   * Expression variable of the Jack Grammar
   */
  public void compileExpression() {
    tokenizer.advance();
    if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')' ) {
      tokenizer.decrementIndex();
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
      String command = operationHashMap.get( Character.toString( tokenizer.symbol() ) );
      // Getting term
      compileTerm();
      writer.writeArithmetic( command );
      tokenizer.advance();
    }
    tokenizer.decrementIndex();
  }
  /**
   * Term variable of the Jack Grammar
   */
  public void compileTerm() {
    tokenizer.advance();
    if ( tokenizer.tokenType() == TokenType.KEYWORD ) {
      if ( tokenizer.keyword() == KeywordType.TRUE ) {
        writer.writePush(VMWriter.SegmentEnum.CONSTANT,0);
        writer.writeArithmetic("not");
      } else if ( tokenizer.keyword() == KeywordType.FALSE
          || tokenizer.keyword() == KeywordType.NULL) {
        writer.writePush(VMWriter.SegmentEnum.CONSTANT,0);
      } else if ( tokenizer.keyword() == KeywordType.THIS ) {
        writer.writePush(VMWriter.SegmentEnum.POINTER, 0);
      }
    } else if ( tokenizer.tokenType() == TokenType.STRING_CONST ) {
      String str = tokenizer.stringVal();
      writer.writePush(VMWriter.SegmentEnum.CONSTANT, str.length());
      writer.writeCall("String.new",1);
      for (int i = 0; i < str.length(); i++){
        writer.writePush(VMWriter.SegmentEnum.CONSTANT, str.charAt(i));
        writer.writeCall("String.appendChar",2);
      }
    } else if ( tokenizer.tokenType() == TokenType.INT_CONST ) {
      writer.writePush(VMWriter.SegmentEnum.CONSTANT, tokenizer.intVal());
    } else if ( tokenizer.tokenType() == TokenType.SYMBOL ) {
      if (tokenizer.symbol() == '~' || tokenizer.symbol() == '-') {
        String command = Character.toString( tokenizer.symbol() );
        // Getting term
        compileTerm();
        if (command.equals("~")) {
          writer.writeArithmetic("not");
        } else {
          writer.writeArithmetic("neg");
        }
      } else if (tokenizer.symbol() == '(' ) {
        // Checking expression
        compileExpression();
        // Checking )
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')' ) {
          COMPILATIONERROR(")");
        }
      }
    } else if ( tokenizer.tokenType() == TokenType.IDENTIFIER ) {
      String varName = tokenizer.identifier();
      tokenizer.advance();
      if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '[') {
        // Checking varName is in SymbolTable
        if (!symbolTable.contains(varName)) {
          if (!symbolTable.contains(varName)) {
            UNIDENTIFIEDSYMBOLERROR(varName);
          }
        }
        //this is an array entry
        writer.writePush(getSeg(symbolTable.kindOf(varName)),symbolTable.indexOf(varName));
        //expression
        compileExpression();
        // Checking ]
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ']') {
          COMPILATIONERROR("]");
        }
        writer.writeArithmetic( operationHashMap.get("+") );
        writer.writePop(VMWriter.SegmentEnum.POINTER, 1);
        writer.writePush(VMWriter.SegmentEnum.THAT, 0);
      } else if (tokenizer.tokenType() == TokenType.SYMBOL && (tokenizer.symbol() == '('
          || tokenizer.symbol() == '.')) {
        //this is a subroutineCall
        tokenizer.decrementIndex();
        tokenizer.decrementIndex();
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
          COMPILATIONERROR("subroutineName|className|varName");
        }
        String name = tokenizer.identifier();
        int nArgs = 0;
        // Checking for .
        tokenizer.advance();
        boolean hasDot = false;
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '.') {
          // Method or function call
          String objectName = name;
          hasDot = true;
          String kindType = symbolTable.typeOf(objectName);
          // Getting subroutineName
          tokenizer.advance();
          if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            COMPILATIONERROR("subroutineName");
          }
          name = tokenizer.identifier();
          if (kindType.equals("")) {
            name = objectName + "." + name;
          } else {
            nArgs = 1;
            writer.writePush(getSeg(symbolTable.kindOf(objectName))
                , symbolTable.indexOf(objectName));
            name = symbolTable.typeOf(objectName) + "." + name;
          }
        }
        if (!hasDot)
          tokenizer.decrementIndex();
        // Getting (
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != '(') {
          COMPILATIONERROR("(");
        }
        // Getting ExpressionList
        nArgs += compileExpressionList();
        // Getting )
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() != ')') {
          COMPILATIONERROR(")");
        }
        writer.writeCall(name, nArgs);
      } else {
        //this is varName
        tokenizer.decrementIndex();
        writer.writePush(getSeg( symbolTable.kindOf( varName ) ), symbolTable.indexOf( varName ));
      }
    }
  }
  /**
   * Expression variable of the Jack Grammar
   *
   * @return The number of expressions compiled
   */
  public int compileExpressionList() {
    int numExpressions = 0;
    tokenizer.advance();
    if ( tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')' ) {
      tokenizer.decrementIndex();
      return numExpressions;
    } else {
      numExpressions++;
      tokenizer.decrementIndex();
      compileExpression();
      // Checking for , expression
      tokenizer.advance();
      while ( tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ',' ) {
        numExpressions++;
        // Getting next expression
        compileExpression();
        tokenizer.advance();
      }
      tokenizer.decrementIndex();
    }
    return numExpressions;
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
   * This method is used to output unidentified symbol errors. If an identifier is not in our symbol
   * table
   *
   * @param unidentifiedSymbol: Symbol in question
   */
  private void UNIDENTIFIEDSYMBOLERROR(String unidentifiedSymbol){
    throw new IllegalStateException("Unidentified symbol: " + unidentifiedSymbol + "\n");
  }
  /**
   * This method is used to initialize the HashMaps necessary.
   */
  private void initHashMap() {
    operationHashMap = new HashMap<>();
    operationHashMap.put("+", "add");
    operationHashMap.put("-", "sub");
    operationHashMap.put("=", "eq");
    operationHashMap.put(">", "gt");
    operationHashMap.put("<", "lt");
    operationHashMap.put("&", "and");
    operationHashMap.put("|", "or");
    operationHashMap.put("*", "call Math.multiply 2");
    operationHashMap.put("/", "call Math.divide 2");

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
    writer = new VMWriter(OutputFile);
    initHashMap();
  }
}
