# sudoku_2x2_asm.S
    .text                           # code section
    .global sudoku_2x2_asm          # declare the asm function as a global function
    .type sudoku_2x2_asm, @function # define sum_asm as a function 
sudoku_2x2_asm:
    addi    sp, sp, -4
    sw      ra, 0(sp)
    li      a1, 0
    jal     solve
    lw      ra, 0(sp)
    addi    sp, sp, 4
    jr      ra

check:
check_prologue:
    addi    sp, sp, -20
    sw      ra, 0(sp)
    sw      s0, 4(sp)
    sw      s1, 8(sp)
    sw      s2, 12(sp)
    sw      s3, 16(sp)
##=========Debug use Please comment below=========
    # li      a2, 0
##================================================


check_body:
    add     t1, a0, a1      # t1 = *test_c_data[index]
    lb      t0, 0(t1)       # t0 = ans = test_c_data[index]
    li      t2, 4           # t2 = devider
    divu    t3, a1, t2      # t3 = row = index / 4;
    remu    t4, a1, t2      # t4 = col = index % 4;
    li      t5, 0           # int i=0
    li      t6, 4           # int i<4

loop_rule1:
    bge     t5, t6, check_rule2
    slli    s0, t3, 2       # t7 = row*4 + i
    add     s0, s0, t5
    bne     s0, a1, check_row_data_same # t7 != index
    addi    t5, t5, 1
    j       loop_rule1
check_row_data_same:
    add     s0, s0, a0      # real address
    lb      s1, 0(s0)       # s1 = test_c_data[row*4 + i]
    beq     s1, t0, return_false
    addi    t5, t5, 1
    j       loop_rule1

check_rule2:
    li      t5, 0           # int i=0
    li      t6, 4           # int i<4
loop_rule2:
    bge     t5, t6, check_block
    slli    s0, t5, 2       # s0 = i*4 + col
    add     s0, s0, t4      # s0 = i*4 + col
    bne     s0, a1, check_col_data_same # t7 != index
    addi    t5, t5, 1
    j       loop_rule2
check_col_data_same:
    add     s0, s0, a0      # real address
    lb      s1, 0(s0)          # s1 = test_c_data[i*4 + col]
    beq     s1, t0, return_false
    addi    t5, t5, 1
    j       loop_rule2

check_block:
    li      t5, 0           # int i=0
    li      t6, 4           # int i<4
    srli    s0, a1, 3       # (index/8)
    slli    s0, s0, 3       # (index/8)*8
    srli    s2, t4, 1       # (col/2)
    slli    s2, s2, 1       # (col/2)*2
    add     s0, s0, s2      # s0 = blk = (index/8)*8 + (col/2)*2
loop_rule3:
    bge     t5, t6, return_true
    srli    s1, t5, 1       # s1 = i/2
    slli    s1, s1, 2       # s1 = i/2 * 4
    add     s2, s0, s1      # blk + i/2 * 4
    li      s1, 2
    remu    s3, t5, s1      # s2 = i%2
    add     s2, s2, s3      # blk + i/2 * 4 + i%2

    bne     s2, a1, check_block_data_same # t7 != index
    addi    t5, t5, 1
    j       loop_rule3
check_block_data_same:
    add     s2, a0, s2      # s0 = address: blk + (i/2)*4 + (i%2)
    lb      s1, 0(s2)       # s1 = test_c_data[i*4 + col]
    beq     s1, t0, return_false
    addi    t5, t5, 1
    j       loop_rule3

return_true:
    li      a2, 1           # Return true in a2
    j       check_epilogue  # jump to check_epilogue
    
return_false:
    li      a2, 0           # Return false in a2

check_epilogue:
    lw      ra, 0(sp)
    lw      s0, 4(sp)
    lw      s1, 8(sp)
    lw      s2, 12(sp)
    lw      s3, 16(sp)
    addi    sp, sp, 20
    jr      ra

## =========================================================
solve:
solve_prologue:
    addi    sp, sp, -32
    sw      ra, 0(sp)
    sw      a1, 4(sp)       # int index
    sw      a2, 8(sp)
    sw      s0, 12(sp)
    sw      s1, 16(sp)
    sw      s2, 20(sp)
    sw      s3, 24(sp)
    sw      s4, 28(sp)
solve_body:
    li      t0, 16
    bge     a1, t0, ret_true    # index >= 16 return true;

    add     s0, a0, a1      # s0= *test_c_data + index
    lb      s1, 0(s0)       # s1 = test_c_data[index]
    li      t2, 0          # move '0' in t2
    li      s2, 1           # int guess=1
    li      s3, 4
    beq     s1, t2, loop_start   # if test_c_data[index] == '0' start loop

    addi    a1, a1, 1       # index+1
    jal     solve           # solve(test_c_data, index+1);
                            # suppose don't need to restore a1
                            # a2 return by solve will then return after epilogue
    j       solve_epilogue        # return solve

loop_start:
    bgt     s2, s3, loop_end # guess<=4
    sb      s2, 0(s0)       # test_c_data[index] = tmp;

    lw      a1, 4(sp)       # check(test_c_data, index)
    jal     check
    li      t0, 1
    # mv      s4, a2          # returned bool value from check
    beq     a2, t0, check_done
    addi    s2, s2, 1
    j       loop_start
check_done:
    lw      a1, 4(sp)       # solve(test_c_data, index+1), maybe no need restore
    addi    a1, a1, 1
    jal     solve
    lw      a1, 4(sp)       # reload a1, maybe no need
    li      t0, 1
    beq     a2, t0, ret_true    # check && solve
    addi    s2, s2, 1       # guess++
    j       loop_start
loop_end:
    li      t2, 0
    sb      t2, 0(s0)       # test_c_data[index] = '0';
    j       ret_false

ret_false:
    li      a2, 0
    j       solve_epilogue

ret_true:
    li      a2, 1

solve_epilogue:
    lw      ra, 0(sp)
    lw      s0, 12(sp)
    lw      s1, 16(sp)
    lw      s2, 20(sp)
    lw      s3, 24(sp)
    lw      s4, 28(sp)
    addi    sp, sp, 32
    jr      ra
    ## add your assembly code here
    .size sudoku_2x2_asm, .-sudoku_2x2_asm