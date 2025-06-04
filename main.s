.equiv @sbrk, 9
.equiv @print_string, 4
.equiv @print_char, 11
.equiv @print_int, 1
.equiv @exit, 10
.equiv @exit2, 17

.text

.globl m
 jal main
  li a0, @exit
  ecall

.globl main
main:
sw fp, -8(sp)
mv fp, sp
li t6, 1000
sub sp, sp, t6
sw ra, -4(fp)
li a2, 108
mv t1, a2
mv a2, t1
mv a0, a2
jal alloc
mv a3, a0
mv t1, a3
la a2, A_run
mv t2, a2
mv a2, t1
mv a3, t2
sw a3, 0(a2)
la a2, A_helper
mv t2, a2
mv a2, t1
mv a3, t2
sw a3, 4(a2)
li a2, 100
mv t2, a2
mv a2, t2
mv a0, a2
jal alloc
mv a3, a0
mv t2, a3
li a2, 504
mv t2, a2
mv a2, t2
mv a0, a2
jal alloc
mv a3, a0
mv t2, a3
mv a2, t2
mv a3, t1
sw a3, 0(a2)
li a2, 504
mv t3, a2
mv a2, t3
mv a0, a2
jal alloc
mv a3, a0
mv t3, a3
mv a2, t3
mv a3, t1
sw a3, 0(a2)
mv a2, t2
lw a3, 0(a2)
mv t1, a3
mv a2, t1
lw a3, 0(a2)
mv t3, a3
mv a2, t2
sw t2, -12(fp)
mv t0, t3
li t6, 0
sub sp, sp, t6
jalr t0
mv a2, a0
lw t2, -12(fp)
mv t4, a2
mv a2, t4
mv a0, a2
jal print
li a2, 0
mv t4, a2
sw t4, -20(fp)
lw a0, -20(fp)
lw ra, -4(fp)
lw fp, -8(fp)
addi sp, sp, 1000
jr ra
.globl A_run
A_run:
sw fp, -8(sp)
mv fp, sp
li t6, 1000
sub sp, sp, t6
sw ra, -4(fp)
mv t1, a2
li a2, 108
mv t2, a2
mv a2, t2
mv a0, a2
jal alloc
mv a3, a0
mv t2, a3
la a2, A_run
mv t3, a2
mv a2, t2
mv a3, t3
sw a3, 0(a2)
la a2, A_helper
mv t3, a2
mv a2, t2
mv a3, t3
sw a3, 4(a2)
li a2, 100
mv t2, a2
mv a2, t2
mv a0, a2
jal alloc
mv a3, a0
mv t2, a3
li a2, 0
mv t2, a2
li a2, 0
mv t3, a2
li a2, 0
mv t2, a2
li a2, 0
mv t3, a2
li a2, 1
mv t4, a2
mv a3, t4
mv t2, a3
li a2, 2
mv t4, a2
mv a3, t4
mv t3, a3
li a2, 12
mv t4, a2
mv a2, t1
lw a3, 0(a2)
mv t4, a3
mv a2, t4
lw a3, 4(a2)
mv t5, a3
li a2, 12
mv s1, a2
mv a2, t1
mv a3, s1
sw t1, -12(fp)
sw t2, -16(fp)
sw t3, -20(fp)
sw t5, -24(fp)
mv t0, t5
li t6, 0
sub sp, sp, t6
jalr t0
mv a2, a0
lw t5, -24(fp)
lw t1, -12(fp)
lw t2, -16(fp)
lw t3, -20(fp)
mv s2, a2
mv a3, s2
mv t2, a3
li a2, 15
mv t4, a2
mv a2, t1
lw a3, 0(a2)
mv t4, a3
mv a2, t4
lw a3, 4(a2)
mv t5, a3
li a2, 15
mv s1, a2
mv a2, t1
mv a3, s1
sw t1, -12(fp)
sw t2, -16(fp)
sw t3, -20(fp)
sw t5, -24(fp)
mv t0, t5
li t6, 0
sub sp, sp, t6
jalr t0
mv a2, a0
lw t5, -24(fp)
lw t1, -12(fp)
lw t2, -16(fp)
lw t3, -20(fp)
mv s2, a2
mv a3, s2
mv t3, a3
add t4, t2, t3
sw t4, -32(fp)
lw a0, -32(fp)
lw ra, -4(fp)
lw fp, -8(fp)
addi sp, sp, 1000
jr ra
.globl A_helper
A_helper:
sw fp, -8(sp)
mv fp, sp
li t6, 1000
sub sp, sp, t6
sw ra, -4(fp)
mv t1, a2
mv t2, a3
li a2, 108
mv t1, a2
mv a2, t1
mv a0, a2
jal alloc
mv a3, a0
mv t1, a3
la a2, A_run
mv t3, a2
mv a2, t1
mv a3, t3
sw a3, 0(a2)
la a2, A_helper
mv t3, a2
mv a2, t1
mv a3, t3
sw a3, 4(a2)
li a2, 100
mv t1, a2
mv a2, t1
mv a0, a2
jal alloc
mv a3, a0
mv t1, a3
li a2, 0
mv t1, a2
li a2, 0
mv t1, a2
mv a3, t2
mv t1, a3
li a2, 1
mv t3, a2
add t4, t2, t3
mv a3, t4
mv t2, a3
mv a2, t1
mv a0, a2
jal print
sw t1, -12(fp)
lw a0, -12(fp)
lw ra, -4(fp)
lw fp, -8(fp)
addi sp, sp, 1000
jr ra
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

