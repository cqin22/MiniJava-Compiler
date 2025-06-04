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
sw fp, -8(sp)
mv fp, sp
li t6, 1000
sub sp, sp, t6
sw ra, -4(fp)
la t0, f
sw t0, -12(fp)
lw t1, -12(fp)
li t6, 0
sub sp, sp, t6
jalr t1
mv t0, a0
sw t0, -20(fp)
lw t1, -20(fp)
mv a0, t1
jal print
lw a0, -20(fp)
lw ra, -4(fp)
lw fp, -8(fp)
addi sp, sp, 1000
jr ra
.globl f
f:
sw fp, -8(sp)
mv fp, sp
li t6, 1000
sub sp, sp, t6
sw ra, -4(fp)
li t0, 10
sw t0, -24(fp)
li t0, 20
sw t0, -28(fp)
lw t1, -24(fp)
lw t2, -28(fp)
add t0, t1, t2
sw t0, -32(fp)
li t0, 30
sw t0, -36(fp)
lw t1, -24(fp)
lw t2, -36(fp)
slt t0, t1, t2
sw t0, -40(fp)
lw t1, -40(fp)
bnez t1, No_Jump_fL8
jal f_L8
No_Jump_fL8:
lw t1, -24(fp)
lw t2, -32(fp)
add t0, t1, t2
sw t0, -44(fp)
lw t1, -24(fp)
lw t2, -28(fp)
add t0, t1, t2
sw t0, -48(fp)
jal f_L10
f_L8:
lw t1, -28(fp)
lw t2, -32(fp)
add t0, t1, t2
sw t0, -52(fp)
jal f_L14
f_L10:
li t0, 50
sw t0, -56(fp)
lw t1, -44(fp)
lw t2, -56(fp)
add t0, t1, t2
sw t0, -28(fp)
lw t1, -32(fp)
lw t2, -44(fp)
add t0, t1, t2
sw t0, -60(fp)
lw t1, -60(fp)
lw t2, -48(fp)
add t0, t1, t2
sw t0, -64(fp)
lw t1, -64(fp)
mv a0, t1
jal print
lw t1, -32(fp)
lw t2, -48(fp)
add t0, t1, t2
sw t0, -52(fp)
li t0, 40
sw t0, -68(fp)
lw t1, -28(fp)
lw t2, -68(fp)
add t0, t1, t2
sw t0, -28(fp)
jal f_L8
f_L14:
li t0, 10
sw t0, -72(fp)
lw t1, -24(fp)
lw t2, -72(fp)
add t0, t1, t2
sw t0, -48(fp)
f_L22:
li t0, 55
sw t0, -28(fp)
li t0, 20
sw t0, -76(fp)
lw t1, -24(fp)
lw t2, -76(fp)
sub t0, t1, t2
sw t0, -32(fp)
lw t1, -28(fp)
lw t2, -32(fp)
slt t0, t1, t2
sw t0, -80(fp)
lw t1, -80(fp)
bnez t1, No_Jump_fL20
jal f_L20
No_Jump_fL20:
lw t1, -32(fp)
lw t2, -28(fp)
add t0, t1, t2
sw t0, -44(fp)
lw t1, -44(fp)
lw t2, -72(fp)
sub t0, t1, t2
sw t0, -52(fp)
lw t1, -44(fp)
lw t2, -52(fp)
add t0, t1, t2
sw t0, -84(fp)
lw t1, -84(fp)
mv a0, t1
jal print
lw t1, -32(fp)
lw t2, -52(fp)
add t0, t1, t2
sw t0, -28(fp)
li t0, 2
sw t0, -88(fp)
lw t1, -88(fp)
lw t2, -28(fp)
sub t0, t1, t2
sw t0, -48(fp)
jal f_L22
f_L20:
li t0, 7
sw t0, -92(fp)
lw t1, -48(fp)
lw t2, -92(fp)
add t0, t1, t2
sw t0, -28(fp)
lw t1, -32(fp)
lw t2, -28(fp)
add t0, t1, t2
sw t0, -32(fp)
li t0, 2
sw t0, -88(fp)
lw t1, -88(fp)
lw t2, -32(fp)
mul t0, t1, t2
sw t0, -52(fp)
lw t1, -48(fp)
lw t2, -72(fp)
add t0, t1, t2
sw t0, -96(fp)
li t0, 55
sw t0, -100(fp)
lw t1, -100(fp)
sw t1, -104(fp)
la t0, g
sw t0, -108(fp)
lw t1, -96(fp)
lw t2, -104(fp)
slt t0, t1, t2
sw t0, -36(fp)
lw t1, -36(fp)
bnez t1, No_Jump_fL27
jal f_L27
No_Jump_fL27:
lw t1, -96(fp)
lw t2, -76(fp)
sub t0, t1, t2
sw t0, -112(fp)
lw t1, -108(fp)
li t6, 32
sub sp, sp, t6
lw t6, -28(fp)
sw t6, 0(sp)
lw t6, -32(fp)
sw t6, 4(sp)
lw t6, -44(fp)
sw t6, 8(sp)
lw t6, -52(fp)
sw t6, 12(sp)
lw t6, -48(fp)
sw t6, 16(sp)
lw t6, -96(fp)
sw t6, 20(sp)
lw t6, -104(fp)
sw t6, 24(sp)
lw t6, -112(fp)
sw t6, 28(sp)
jalr t1
mv t0, a0
sw t0, -104(fp)
lw t1, -112(fp)
lw t2, -104(fp)
add t0, t1, t2
sw t0, -116(fp)
jal f_L29
f_L27:
li t0, 1
sw t0, -120(fp)
lw t1, -96(fp)
lw t2, -120(fp)
add t0, t1, t2
sw t0, -124(fp)
lw t1, -88(fp)
lw t2, -124(fp)
mul t0, t1, t2
sw t0, -116(fp)
f_L29:
li t0, 5
sw t0, -128(fp)
lw t1, -116(fp)
lw t2, -128(fp)
sub t0, t1, t2
sw t0, -132(fp)
lw t1, -96(fp)
lw t2, -132(fp)
add t0, t1, t2
sw t0, -124(fp)
lw t1, -116(fp)
lw t2, -124(fp)
sub t0, t1, t2
sw t0, -136(fp)
lw t1, -136(fp)
lw t2, -132(fp)
add t0, t1, t2
sw t0, -140(fp)
lw t1, -140(fp)
mv a0, t1
jal print
lw t1, -116(fp)
lw t2, -124(fp)
add t0, t1, t2
sw t0, -112(fp)
lw t1, -32(fp)
lw t2, -44(fp)
add t0, t1, t2
sw t0, -32(fp)
lw t1, -44(fp)
lw t2, -76(fp)
add t0, t1, t2
sw t0, -144(fp)
lw t1, -112(fp)
lw t2, -144(fp)
add t0, t1, t2
sw t0, -148(fp)
lw t1, -44(fp)
lw t2, -148(fp)
slt t0, t1, t2
sw t0, -60(fp)
f_L3:
lw t1, -60(fp)
bnez t1, No_Jump_fL12
jal f_L12
No_Jump_fL12:
lw t1, -44(fp)
lw t2, -144(fp)
add t0, t1, t2
sw t0, -152(fp)
li t0, 30
sw t0, -156(fp)
lw t1, -152(fp)
lw t2, -156(fp)
add t0, t1, t2
sw t0, -160(fp)
lw t1, -112(fp)
lw t2, -160(fp)
sub t0, t1, t2
sw t0, -160(fp)
lw t1, -152(fp)
lw t2, -160(fp)
sub t0, t1, t2
sw t0, -148(fp)
li t0, 0
sw t0, -164(fp)
lw t1, -148(fp)
lw t2, -164(fp)
mul t0, t1, t2
sw t0, -60(fp)
lw t1, -32(fp)
lw t2, -96(fp)
add t0, t1, t2
sw t0, -144(fp)
lw t1, -152(fp)
lw t2, -148(fp)
add t0, t1, t2
sw t0, -148(fp)
lw t1, -144(fp)
lw t2, -148(fp)
sub t0, t1, t2
sw t0, -168(fp)
lw t1, -168(fp)
mv a0, t1
jal print
jal f_L3
f_L12:
lw t1, -124(fp)
lw t2, -148(fp)
sub t0, t1, t2
sw t0, -96(fp)
lw t1, -152(fp)
lw t2, -96(fp)
add t0, t1, t2
sw t0, -96(fp)
lw a0, -96(fp)
lw ra, -4(fp)
lw fp, -8(fp)
addi sp, sp, 1000
jr ra
.globl g
g:
sw fp, -8(sp)
mv fp, sp
li t6, 1000
sub sp, sp, t6
sw ra, -4(fp)
lw t1, 0(fp)
lw t2, 4(fp)
add t0, t1, t2
sw t0, -24(fp)
lw t1, 8(fp)
lw t2, 12(fp)
add t0, t1, t2
sw t0, -28(fp)
lw t1, 16(fp)
lw t2, 20(fp)
add t0, t1, t2
sw t0, -32(fp)
lw t1, 24(fp)
lw t2, 28(fp)
add t0, t1, t2
sw t0, -44(fp)
li t0, 10
sw t0, -72(fp)
lw t1, -72(fp)
sw t1, -52(fp)
lw t1, 28(fp)
lw t2, 0(fp)
slt t0, t1, t2
sw t0, -172(fp)
lw t1, -172(fp)
bnez t1, No_Jump_gL42
jal g_L42
No_Jump_gL42:
la t0, g
sw t0, -176(fp)
lw t1, -176(fp)
li t6, 32
sub sp, sp, t6
lw t6, -172(fp)
sw t6, 0(sp)
lw t6, 24(fp)
sw t6, 4(sp)
lw t6, -172(fp)
sw t6, 8(sp)
lw t6, 16(fp)
sw t6, 12(sp)
lw t6, -72(fp)
sw t6, 16(sp)
lw t6, -172(fp)
sw t6, 20(sp)
lw t6, 4(fp)
sw t6, 24(sp)
lw t6, -72(fp)
sw t6, 28(sp)
jalr t1
mv t0, a0
sw t0, -24(fp)
g_L42:
lw t1, -44(fp)
lw t2, -24(fp)
sub t0, t1, t2
sw t0, -52(fp)
lw t1, -28(fp)
lw t2, -52(fp)
mul t0, t1, t2
sw t0, -48(fp)
lw t1, -32(fp)
lw t2, -48(fp)
sub t0, t1, t2
sw t0, -96(fp)
lw t1, -44(fp)
lw t2, -52(fp)
add t0, t1, t2
sw t0, -104(fp)
lw t1, -52(fp)
lw t2, -48(fp)
add t0, t1, t2
sw t0, -112(fp)
lw t1, -96(fp)
lw t2, -104(fp)
add t0, t1, t2
sw t0, -116(fp)
lw a0, -116(fp)
lw ra, -4(fp)
lw fp, -8(fp)
addi sp, sp, 1000
jr ra

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

