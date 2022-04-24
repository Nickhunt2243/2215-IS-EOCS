import java.util.HashMap;

public class SymbolTable {
  /**
   * A hashmap for the symbol table where the key is the name of the symbol and the value is a
   * length 3 Object array.
   * 0 -> type   (String)
   * 1 -> kind   (enum)
   * 2 -> number (int)
   */
  private HashMap<String, String[]> classSymbols;
  private HashMap<String, String[]> subroutineSymbols;
  /**
   * This is the kind index hashmap. It holds the current index of each kind.
   */
  private HashMap<kindEnum, Integer> kindIndex;
  /**
   * This is the kind Hashmap it makes it easier for us to diagnose what kind of type a variable is.
   */
  private HashMap<String, kindEnum> kindMap;
  /**
   * This is the Enum for the Kind of variable we take in.
   */
  public enum kindEnum {
    STATIC,
    FIELD,
    ARG,
    VAR,
    NONE
  }
  /**
   * This is the method we call after every subroutine to reset the subroutine related symbol table.
   */
  public void reset() {
    kindIndex.put( kindEnum.ARG, 0 );
    kindIndex.put( kindEnum.VAR, 0 );
    subroutineSymbols.clear();
  }
  /**
   * This method is used to add variable to the symbol table.
   *
   * @param name: The name of the variable.
   * @param type: The type of the variable (int, boolean, char, className).
   * @param kind: The kind of the variable (static, field, var, arg).
   */
  public void define( String name, String type, String kind ) {
    if ( name.isEmpty() || type.isEmpty() || kind.isEmpty() ) {
      throw new IllegalStateException("Error compiling Symbol Table!\n\tName: "
          + name + "\n\tType: " + type + "\n\tKind: " + kind);
    }
    kindEnum kindenum = kindMap.get( kind );
    if (kindenum == kindEnum.ARG || kindenum == kindEnum.VAR){

      Integer index = kindIndex.get( kindenum );
      subroutineSymbols.put(name, new String[] { type, kind, index.toString() });
      kindIndex.put( kindMap.get( kind ), ++index );

    }else if(kindenum == kindEnum.STATIC || kindenum == kindEnum.FIELD){
      Integer index = kindIndex.get( kindenum );
      classSymbols.put(name, new String[] { type, kind, index.toString() });
      kindIndex.put( kindMap.get( kind ), ++index );

    }

  }
  /**
   * This method checks if a variable name is in the symbol table.
   *
   * @param name: The name of the variable in question.
   * @return A Boolean of whether the symbol was found.
   */
  public boolean contains( String name ) {
    if ( !subroutineSymbols.containsKey( name ) ) {
      return classSymbols.containsKey( name );
    }
    return subroutineSymbols.containsKey( name );
  }
  /**
   * This method is used to retrieve the current variable index of a given kind. Used for popping
   * a new variable onto a segment.
   *
   * @param kind: The kind of variable you would like to receive the index of.
   * @return The integer value of the index.
   */
  public int varCount( String kind ) {
    return kindIndex.get( kindMap.get( kind ) );
  }
  /**
   * This method is used to retrieve the current variable kind.
   *
   * @param name: The name of the variable in question.
   * @return The kindEnum of the variable in question.
   */
  public kindEnum kindOf( String name ) {
    if ( subroutineSymbols.containsKey( name ) ) {
      return kindMap.get( subroutineSymbols.get( name )[1] );
    } else if ( classSymbols.containsKey( name ) ) {
      return kindMap.get( classSymbols.get( name )[1] );
    } else return kindEnum.NONE;
  }
  /**
   * This method is used to retrieve the type of a given variable.
   *
   * @param name: The variable in question.
   * @return The type of the variable in question
   */
  public String typeOf( String name ) {
    if ( subroutineSymbols.containsKey( name ) ) {
      return subroutineSymbols.get( name )[0];
    } else if ( classSymbols.containsKey( name ) ) {
      return classSymbols.get( name )[0];
    } else return "";
  }
  /**
   * This method is used to retrieve the index of the current variable. Used for popping the
   * variable off of its given segment.
   *
   * @param name: The name of the variable in question.
   * @return The index of the variable in question.
   */
  public int indexOf( String name ) {
    if ( subroutineSymbols.containsKey( name ) ) {
      return Integer.parseInt( subroutineSymbols.get( name )[2] );
    } else if ( classSymbols.containsKey( name ) ) {
      return Integer.parseInt( classSymbols.get( name )[2] );
    } else return -1;
  }
  /**
   * This method is used to initialize the Hashmaps needed.
   */
  public void initializeKindHashMaps() {
    kindIndex = new HashMap<>();
    kindIndex.put( kindEnum.STATIC, 0 );
    kindIndex.put( kindEnum.FIELD, 0 );
    kindIndex.put( kindEnum.ARG, 0 );
    kindIndex.put( kindEnum.VAR, 0 );

    kindMap = new HashMap<>();
    kindMap.put( "static", kindEnum.STATIC );
    kindMap.put( "field", kindEnum.FIELD );
    kindMap.put( "arg", kindEnum.ARG );
    kindMap.put( "var", kindEnum.VAR );
  }
  /**
   * This is the constructor of the Symbol Table.
   */
  public SymbolTable() {
    subroutineSymbols = new HashMap<>();
    classSymbols = new HashMap<>();
    initializeKindHashMaps();
  }


}
