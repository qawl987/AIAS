# sudoku_2x2_asm.S
    .section .text, "ax"
    # .text                           # code section
    .align 2
    .global sudoku_2x2_asm          # declare the asm function as a global function
    .type sudoku_2x2_asm, @function # define sum_asm as a function 
sudoku_2x2_asm:
    li      a1, 0
    jal     solve
    ret

check:
    li      a2, 0
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
    li      t2, 48          # move '0' in t2
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
    addi    t2, s2, 48      # tmp = '0'+ guess
    sb      t2, 0(s0)       # test_c_data[index] = tmp;

    lw      a1, 4(sp)       # check(test_c_data, index)
    jal     check
    mv      s4, a2          # returned bool value from check
    
    lw      a1, 4(sp)       # solve(test_c_data, index+1), maybe no need restore
    addi    a1, a1, 1
    jal     solve
    lw      a1, 4(sp)       # reload a1, maybe no need
    beq     s4, a2, ret_true    # check && solve
    addi    s2, s2, 1       # guess++
    j       loop_start
loop_end:
    li      t2, 48
    sb      t2, 0(s0)       # test_c_data[index] = '0';
    jal     ret_false

ret_false:
    li      a2, 0
    j       solve_epilogue

ret_true:
    li      a2, 1

solve_epilogue:
    lw      ra, 0(sp)
    lw      a1, 4(sp)       # int index
    lw      a2, 8(sp)
    lw      s0, 12(sp)
    lw      s1, 16(sp)
    lw      s2, 20(sp)
    lw      s3, 24(sp)
    lw      s4, 28(sp)
    addi    sp, sp, 32
    jr      ra
    ## add your assembly code here

    .size sudoku_2x2_asm, .-sudoku_2x2_asm