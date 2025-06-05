.globl FacComputeFac
FacComputeFac:
  //TODO: Allocate a stack frame in function prologue
  =============
  sw fp, -8(sp)
  mv fp, sp
  li t6, 32
  sub sp, sp, t6
  sw ra, -4(fp)
  =============
  li t0, 1
  sw t0, -12(fp)
  lw t1, 4(fp)
  lw t2, -12(fp)
  slt t0, t1, t2
  sw t0, -16(fp)
  lw t1, -16(fp)
  bnez t1, FacComputeFacif1_else_no_long_jump1
  jal FacComputeFacif1_else
FacComputeFacif1_else_no_long_jump1:
  li t0, 1
  sw t0, -20(fp)
  jal FacComputeFacif1_end
FacComputeFacif1_else:
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
  //TODO: Complete a function call
  =============
  li t6, 8
  sub sp, sp, t6
  lw t6, 0(fp)
  sw t6, 0(sp)
  lw t6, -28(fp)
  sw t6, 4(sp)
  jalr t1
  addi sp, sp, 8
  mv t0, a0
  =============
  sw t0, -32(fp)
  lw t1, 4(fp)
  lw t2, -32(fp)
  mul t0, t1, t2
  sw t0, -20(fp)
FacComputeFacif1_end:
  //TODO: Function epilogue
  =============
  lw a0, -20(fp)
  lw ra, -4(fp)
  lw fp, -8(fp)
  addi sp, sp, 32
  jr ra
  =============

