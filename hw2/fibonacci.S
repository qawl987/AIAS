## fibonacci.S
    .text                          # code section 
    .global fibonacci_asm          # declar the sum_asm function as a  global function
    .type fibonacci_asm, @function # define sum_asm as a function 
fibonacci_asm:
prologue:
    # Write your assembly code funtion here.
    # Please notice the rules of calling convention.
    ## Prologue: Save ra, caller-saved register(ra, a0), callee-saved registers(sp, s1, s2)
    addi    sp, sp, -16    # Allocate 16 bytes on stack
    sw      ra, 12(sp)     # Save return address
    sw      a0, 8(sp)      # Save a0 (input)
    sw      s1, 4(sp)      # Save s1 f(n-1) output
    sw      s2, 0(sp)      # Save s2 f(n-2) output

    ## Base cases
    li      t0, 1          # Load constant 1 into t0
    beq     a0, t0, ret_one # If n == 1, return 1
    blt     a0, t0, ret_zero # If n < 1, return 0

    ## Recursive case
    ## Calculate Fibonacci(n-1)
    addi    a0, a0, -1     # a0 = n - 1
    jal     fibonacci_asm      # Call fibonacci(n-1)
    mv      s1, a1         # Store result of fibonacci(n-1) in s1

    ## Calculate Fibonacci(n-2)
    lw      a0, 8(sp)      # Restore original a0 (n)
    addi    a0, a0, -2     # a0 = n - 2
    jal     fibonacci_asm      # Call fibonacci(n-2)
    mv      s2, a1         # Store result of fibonacci(n-2) in s2

    ## Add results of Fibonacci(n-1) and Fibonacci(n-2)
    add     a1, s1, s2     # a1 = s1 + s2 (result)
    mv      a0, a1         # move the answer to a0
    j       epilogue           # Skip to epilogue

## Return 1 for Fibonacci(1)
ret_one:
    li      a1, 1          # a1 = 1
    j       epilogue           # Skip to epilogue

## Return 0 for Fibonacci(0)
ret_zero:
    li      a1, 0          # a1 = 0

## Epilogue: Restore saved registers and return
epilogue:
    lw      ra, 12(sp)     # Restore return address
    lw      s1, 4(sp)      # Restore s1
    lw      s2, 0(sp)      # Restore s2
    addi    sp, sp, 16     # Deallocate stack space
    jr      ra             # Return to caller
.size fibonacci_asm, .-fibonacci_asm