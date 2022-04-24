package VM_Translator;

/**
 * This is the CommandType enum that is used to decide what the current line of vm instruction
 * is meant to do. These categories help us generalize the functionality of the vm instructions.
 */
public enum CommandType {
  C_ARITHMETIC,
  C_PUSH,
  C_POP,
  C_LABEL,
  C_GOTO,
  C_IF,
  C_FUNCTION,
  C_RETURN,
  C_CALL
}
