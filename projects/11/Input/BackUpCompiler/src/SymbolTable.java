import java.util.HashMap;

public class SymbolTable {

  /**
   * A hashmap for the symbol table where the key is the name of the symbol and the value is a
   * length 3 Object array.
   * 0 -> type   (String)
   * 1 -> kind   (enum)
   * 2 -> number (int)
   */
  private HashMap<String, String[]> symbolTable;
  /**
   *
   */
  private HashMap<kindEnum, Integer> kindIndex;
  private HashMap<String, kindEnum> kindMap;

  private enum kindEnum {
    STATIC,
    FIELD,
    ARG,
    VAR
  }

  public void reset() {
    kindIndex.put( kindEnum.STATIC, 0 );
    kindIndex.put( kindEnum.FIELD, 0);
    kindIndex.put( kindEnum.ARG, 0 );
    kindIndex.put( kindEnum.VAR, 0 );
    symbolTable = new HashMap<>();
  }

  public void define( String name, String type, String kind) {
    if ( name.isEmpty() || type.isEmpty() || kind.isEmpty() ) {
      throw new IllegalStateException("Error compiling Symbol Table!\n\tName: "
          + name + "\n\tType: " + type + "\n\tKind: " + kind);
    }
    Integer index = kindIndex.get( kindMap.get( kind ) );
    symbolTable.put(name, new String[] { type, kind, index.toString() });
    kindIndex.put( kindMap.get( kind ), ++index );
  }

  public boolean contains( String name ) {
    return symbolTable.containsKey( name );
  }

  public HashMap getMap() {
    return symbolTable;
  }

  public int varCount( String kind ) {
    return kindIndex.get( kindMap.get( kind ) );
  }

  public kindEnum kindOf( String name ) {
    return kindMap.get( symbolTable.get( name )[1] );
  }

  public String typeOf( String name ) {
    return symbolTable.get( name )[0];
  }

  public int indexOf( String name ) {
    return Integer.parseInt( symbolTable.get( name )[2] );
  }

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

  public SymbolTable() {
    symbolTable = new HashMap<>();
    initializeKindHashMaps();
  }


}
