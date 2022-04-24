import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;

public class VMWriter {

  /**
   * The PrintWriter for output.
   */
  private PrintWriter outputWriter;
  /**
   * The Segment Hashmap
   */
  private HashMap<SegmentEnum, String> segmentMap;
  /**
   * The Command Hashmap
   */
  private HashMap<Operation, String> commandMap;
  /**
   * The Enum for Segments
   */
  enum SegmentEnum {
    CONSTANT,
    ARGUMENT,
    LOCAL,
    STATIC,
    THIS,
    THAT,
    POINTER,
    TEMP,
    NONE
  }
  /**
   * The Enum for Operations
   */
  enum Operation {
    ADD,
    SUB,
    NEG,
    EQ,
    GT,
    LT,
    AND,
    OR,
    NOT
  }
  /**
   * This method is used for writing push VM code.
   *
   * @param segment: The segment to push onto.
   * @param index: The index of the segment.
   */
  public void writePush( SegmentEnum segment, int index ) {
    outputWriter.println("push " + segmentMap.get(segment) + " " + index );
  }
  /**
   * This method is used for writing pop VM code
   *
   * @param segment: The Segment to pop from.
   * @param index: The index of the segment.
   */
  public void writePop( SegmentEnum segment, int index ) {
    outputWriter.println("pop " + segmentMap.get(segment) + " " + index );
  }
  /**
   * This method is used for writing arithmetic VM code.
   *
   * @param command: The operation to write.
   */
  public void writeArithmetic( String command ) {
    outputWriter.println( command );
  }
  /**
   * This method is used for writing label VM code.
   * @param label: The label to write.
   */
  public void writeLabel( String label ) {
    outputWriter.println("label " + label);
  }
  /**
   * This method is used for writing goto VM code.
   *
   * @param label: The goto label.
   */
  public void writeGoto( String label ) {
    outputWriter.println("goto " + label);
  }

  /**
   * This method is used for writing if VM Code.
   *
   * @param label: The if label.
   */
  public void writeIf( String label ) {
    outputWriter.println("if-goto " + label);
  }
  /**
   * This method is used for writing function call VM code.
   *
   * @param name: The name of the functions.
   * @param nArgs: The number of arguments to pass.
   */
  public void writeCall( String name, int nArgs ) {
    outputWriter.println("call " + name + " " + nArgs);
  }
  /**
   * This method is used for writing function VM code.
   *
   * @param name: The name of the function.
   * @param nVars: The number of parameters the function takes.
   */
  public void writeFunction(String name, int nVars) {
    outputWriter.println("function " + name + " " + nVars);
  }

  /**
   * This method is used for writing return VM code.
   */
  public void writeReturn() {
    outputWriter.println("return");
  }
  /**
   * This method is used to close the output file writer.
   */
  public void close() {
    outputWriter.close();
  }

  /**
   * This method is used to initialize the hashmaps used throughout.
   */
  public void initHashMap() {
    segmentMap = new HashMap<>();
    segmentMap.put(SegmentEnum.CONSTANT, "constant");
    segmentMap.put(SegmentEnum.ARGUMENT, "argument");
    segmentMap.put(SegmentEnum.LOCAL, "local");
    segmentMap.put(SegmentEnum.STATIC, "static");
    segmentMap.put(SegmentEnum.THIS, "this");
    segmentMap.put(SegmentEnum.THAT, "that");
    segmentMap.put(SegmentEnum.POINTER, "pointer");
    segmentMap.put(SegmentEnum.TEMP, "temp");

    commandMap = new HashMap<>();
    commandMap.put(Operation.ADD, "add");
    commandMap.put(Operation.SUB, "sub");
    commandMap.put(Operation.NEG, "neg");
    commandMap.put(Operation.EQ, "eq");
    commandMap.put(Operation.GT, "gt");
    commandMap.put(Operation.LT, "lt");
    commandMap.put(Operation.AND, "and");
    commandMap.put(Operation.OR, "or");
    commandMap.put(Operation.NOT, "not");
  }

  /**
   * This is the constructor of the VMWriter class.
   *
   * @param OutputFile: The file to output the translation to.
   */
  public VMWriter(File OutputFile) {
    outputWriter = null;
    initHashMap();
    try {
      outputWriter = new PrintWriter( new FileWriter(OutputFile) );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
