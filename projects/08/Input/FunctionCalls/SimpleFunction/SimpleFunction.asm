@256
D=A
@SP
M=D
// *** Writing to: SimpleFunction.vm ***
// function SimpleFunction.test 2
(SimpleFunction.test)
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
// push local 0
@LCL
D=M
@0
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1
// push local 1
@LCL
D=M
@1
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1
// add
@SP
AM=M-1
D=M
A=A-1
M=M+D
// not
@SP
A=M-1
M=!M
// push argument 0
@ARG
D=M
@0
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1
// add
@SP
AM=M-1
D=M
A=A-1
M=M+D
// push argument 1
@ARG
D=M
@1
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1
// sub
@SP
AM=M-1
D=M
A=A-1
M=M-D
// return 
@LCL
D=M
@frame
M=D
@5
A=D-A
D=M
@retAddress
M=D
@ARG
D=M
@0
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
@ARG
D=M
@SP
M=D+1
@frame
D=M-1
AM=D
D=M
@THAT
M=D
@frame
D=M-1
AM=D
D=M
@THIS
M=D
@frame
D=M-1
AM=D
D=M
@ARG
M=D
@frame
D=M-1
AM=D
D=M
@LCL
M=D
@retAddress
A=M
0;JMP
@128
0;JMP
