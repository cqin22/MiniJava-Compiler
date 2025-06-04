.equiv @sbrk, 9
.equiv @print_string, 4
.equiv @print_char, 11
.equiv @print_int, 1
.equiv @exit, 10
.equiv @exit2, 17

.text

.globl m
  jal Main
  li a0, @exit
  ecall


.globl Main
Main:
  ### Activation record start
  sw fp, -8(sp)
  mv fp, sp
  li t6, 28
  sub sp, sp, t6
  sw ra, -4(fp)
  ### Activation record end
  li t0, 4
  sw t0, -12(fp)
  lw t1, -12(fp)
  mv a0, t1
  jal alloc
  mv t0, a0
  sw t0, -16(fp)
  lw t1, -12(fp)
  mv a0, t1
  jal alloc
  mv t0, a0
  sw t0, -20(fp)
  la t0, FacComputeFac
  sw t0, -12(fp)
  lw t0, -20(fp)
  lw t1, -12(fp)
  sw t1, 0(t0)
  lw t0, -20(fp)
  sw t0, -12(fp)
  lw t0, -16(fp)
  lw t1, -12(fp)
  sw t1, 0(t0)
  lw t0, -16(fp)
  bnez t0, Main_null1_no_long_jump0
  jal Main_null1
Main_null1_no_long_jump0:
  lw t1, -16(fp)
  lw t0, 0(t1)
  sw t0, -24(fp)
  lw t1, -24(fp)
  lw t0, 0(t1)
  sw t0, -24(fp)
  li t0, 6
  sw t0, -12(fp)
  lw t1, -24(fp)
  # Call function
  li t6, 8
  sub sp, sp, t6
  lw t6, -16(fp)
  sw t6, 0(sp)
  lw t6, -12(fp)
  sw t6, 4(sp)
  jalr t1
  mv t0, a0
  # End call function
  sw t0, -28(fp)
  lw t0, -28(fp)
  mv a0, t0
  jal print
  jal Main_main_end
Main_null1:
  la a0, msg_nullptr
  jal error
Main_main_end:
  # Start function epilogue
  lw a0, -12(fp)
  lw ra, -4(fp)
  lw fp, -8(fp)
  addi sp, sp, 28
  jr ra
  # End function epilogue

.globl FacComputeFac
FacComputeFac:
  ### Activation record start
  sw fp, -8(sp)
  mv fp, sp
  li t6, 32
  sub sp, sp, t6
  sw ra, -4(fp)
  ### Activation record end
  li t0, 1
  sw t0, -12(fp)
  lw t1, 4(fp)
  lw t2, -12(fp)
  slt t0, t1, t2
  sw t0, -16(fp)
  lw t0, -16(fp)
  bnez t0, FacComputeFac_if1_else_no_long_jump1
  jal FacComputeFac_if1_else
FacComputeFac_if1_else_no_long_jump1:
  li t0, 1
  sw t0, -20(fp)
  jal FacComputeFac_if1_end
FacComputeFac_if1_else:
  lw t1, 0(fp)
  lw t0, 0(t1)
  sw t0, -24(fp)
  lw t1, -24(fp)
  lw t0, 0(t1)
  sw t0, -24(fp)
  li t0, 1
  sw t0, -12(fp)
  lw t1, 4(fp)
  lw t2, -12(fp)
  sub t0, t1, t2
  sw t0, -28(fp)
  lw t1, -24(fp)
  # Call function
  li t6, 8
  sub sp, sp, t6
  lw t6, 0(fp)
  sw t6, 0(sp)
  lw t6, -28(fp)
  sw t6, 4(sp)
  jalr t1
  mv t0, a0
  # End call function
  sw t0, -32(fp)
  lw t1, 4(fp)
  lw t2, -32(fp)
  mul t0, t1, t2
  sw t0, -20(fp)
FacComputeFac_if1_end:
  # Start function epilogue
  lw a0, -20(fp)
  lw ra, -4(fp)
  lw fp, -8(fp)
  addi sp, sp, 32
  jr ra
  # End function epilogue
  li a1, 10
  li a0, 11
  ecall


.globl print
print:
  mv a1, a0
  li a0, @print_int
  ecall
  li a1, 10
  li a0, @print_char
  ecall
  jr ra

.globl error
error:
  mv a1, a0
  li a0, @print_string
  ecall
  li a1, 10
  li a0, @print_char
  ecall
  li a0, @exit
  ecall
abort_17:
  j abort_17

.globl alloc
alloc:
  mv a1, a0
  li a0, @sbrk
  ecall
  jr ra

.data

.globl msg_nullptr
msg_nullptr:
  .asciiz "null pointer"
  .align 2

.globl msg_array_oob
msg_array_oob:
  .asciiz "array index out of bounds"
  .align 2

