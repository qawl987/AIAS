## HW6-1 - Fibonacci Series
### Assembly Code
> 請放上你的程式碼並加上註解，讓 TA明白你是如何完成的。
```mipsasm=
## assembly code & comment
## fibonacci.S
## put input n in register x10 (a0)
## put output fibonacci(n) in register x11 (a1)
## use Venus to test correctness

.text
main:
    ## write assembly code here.
    ## call fibonacci function and get return value.
    addi   a0, x0, 16         # set a0 to 16
    jal    fibonacci          # fibonacci
    j      exit

fibonacci:
    ## fibonacci function
    addi   sp, sp, -8         # move stack pointer
    sw     ra, 0(sp)          # store return address

    beq    a0, x0, ret_zero   # when n = 0, go to ret_zero

    addi   t0, x0, 1          # store 1 in t0 for compare
    beq    a0, t0, ret_one    # when n = 1, go to ret_one

    addi   a0, a0, -1         # otherwise, n > 1, n = n - 1
    sw     a0, 4(sp)          # store a0
    jal    fibonacci

    lw     a0, 4(sp)          # load a0
    sw     a1, 4(sp)          # store return value
    addi   a0, a0, -1         # n = n - 2
    jal    fibonacci
    lw     t0, 4(sp)          # read stored return value to t0
    add    a1, t0, a1         # add 2 return values and store in a1
    j      done

ret_one:
    addi   a1, x0, 1          # return 1 in a1
    j done

ret_zero:
    addi   a1, x0, 0          # return 0 in a1

done:
    lw     ra, 0(sp)          # load return address
    addi   sp, sp, 8          # move stack pointer back
    jr     ra

exit:
    addi   a0, x0, 1
    ecall                     # Terminate
```

### Simulation Result
> 請放上你在 Venus上的模擬結果，驗證程式碼的正確性。(螢幕截圖即可)

![](https://course.playlab.tw/md/uploads/41ed0a52-cb09-41cd-bbee-629b270c6f7c.png)

## HW6-2 - Fibonacci Series with C/Assembly Hybrid 
### Assembly Code & C Code
> 請放上你的程式碼並加上註解，讓 TA明白你是如何完成的。
- `fibonacci.S`
```mipsasm=
## assembly code & comment
## fibonacci.S
## fibonacci.S

    .text                          # code section 
    .global fibonacci_asm          # declar the sum_asm function as a  global function
    .type fibonacci_asm, @function # define sum_asm as a function 
fibonacci_asm:

    # Write your assembly code funtion here.
    # Please notice the rules of calling convention.
    
    addi   sp, sp, -8         # move stack pointer
    sw     ra, 0(sp)          # store return address

    beq    a0, x0, ret_zero   # when n = 0, go to ret_zero

    addi   t0, x0, 1          # store 1 in t0 for compare
    beq    a0, t0, ret_one    # when n = 1, go to ret_one

    addi   a0, a0, -1         # otherwise, n > 1, n = n - 1
    sw     a0, 4(sp)          # store a0
    jal    fibonacci_asm

    add    t0, a0, x0         # store return value in t0
    lw     a0, 4(sp)          # load a0 back
    sw     t0, 4(sp)          # store return value

    addi   a0, a0, -1         # n = n - 2
    jal    fibonacci_asm
    lw     t0, 4(sp)          # read stored return value to t0
    add    a0, t0, a0         # add 2 return values and store in a0
    j      done

ret_one:
    addi   a0, x0, 1          # return 1 in a0
    j done

ret_zero:
    addi   a0, x0, 0          # return 0 in a0

done:
    lw     ra, 0(sp)          # load return address
    addi   sp, sp, 8          # move stack pointer back
    jr     ra

    .size fibonacci_asm, .-fibonacci_asm
```

- `fibonacci.c`
```cpp=
// c code & comment
// HW2 main function
#include <stdio.h>
#include <stdlib.h>


int fibonacci_c(int n) { 
    if(n == 0) {
        return 0;
    }
        
    else if(n == 1) {
        return 1;
    }
        
    else {
        return fibonacci_c(n-1)+fibonacci_c(n-2);        
    }
}

int fibonacci_asm(int n);

int main() {
    int n = 6;    // setup input value n
    int out = 0; // setup output value fibonacci(n)

    out = fibonacci_c(n);
    char str[25];
    itoa(out,str,10);
    puts("C code fibonacci_c=");
    puts(str);
    puts("\n");  
    out = fibonacci_asm(n);
    puts("ASM code fibonacci_asm=");
    itoa(out,str,10);
    puts(str);
    puts("\n");
    return 0;
}
```

### Simulation Result
> 請放上你在 docker中的 qemu模擬結果，驗證程式碼的正確性。 (螢幕截圖即可)

![](https://course.playlab.tw/md/uploads/dc195039-f889-492f-a919-81408939f8e7.png)

## HW6-3 - 2x2 Sudoku
### Assembly Code & C Code
> 請放上你的程式碼並加上註解，讓 TA明白你是如何完成的。
- `main.c`
```cpp=
// c code & comment
// main.c
#include <stdio.h>
#include <stdlib.h>
#include "sudoku_2x2_c.h"
#define SIZE 16

char test_c_data[16] = { 0, 0, 2, 0, 
                         0, 0, 0, 4,
                         2, 3, 0, 0, 
                         0, 4, 0, 0 };
                      
char test_asm_data[16] = { 0, 0, 2, 0, 
                           0, 0, 0, 4,
                           2, 3, 0, 0, 
                           0, 4, 0, 0 };

void print_sudoku_result() {
    int i;
    char str[25];
    puts("Output c & assembly function result\n");
    puts("c result :\n");
    for( i=0 ; i<SIZE ; i++) {   
        int j= *(test_c_data+i);
        itoa(j, str, 10);
        puts(str);
    }

    puts("\n\nassembly result :\n");
    for( i=0 ; i<SIZE ; i++) {
        int j= *(test_asm_data+i);
        itoa(j, str, 10);
        puts(str);
    }

    int flag = 0;
    for( i=0 ; i<SIZE ; i++) {
        if (*(test_c_data+i) != *(test_asm_data+i)) {
            flag = 1;
            break;
        }
    }

    if (flag == 1){
        puts("\n\nyour c & assembly got different result ... QQ ...\n");
    }
    else {
        puts("\n\nyour c & assembly got same result!\n");
    }
}


void sudoku_2x2_asm(char *test_asm_data); // TODO, sudoku_2x2_asm.S

void sudoku_2x2_c(char *test_c_data); // TODO, sudoku_2x2_c.S
                        
int main() {
    sudoku_2x2_c(test_c_data);
    sudoku_2x2_asm(test_asm_data);
    print_sudoku_result();
    return 0;
}
```
- `sudoku_2x2_asm.S`
```mipsasm=
## assembly code & comment
# sudoku_2x2_asm.S

    .text                           # code section
    .global sudoku_2x2_asm          # declare the asm function as a global function
    .type sudoku_2x2_asm, @function # define sum_asm as a function
sudoku_2x2_asm:

    ## add your assembly code here

    addi   sp, sp, -4               # move stack pointer
    sw     ra, 0(sp)                # store return address

    add    a1, a0, x0               # set a1 to a0 (*test_c_data)
    addi   a0, x0, 0                # set a0 to 0
    jal    solve

    lw     ra, 0(sp)                # load return address
    addi   sp, sp, 4                # move stack pointer back
    jr     ra

solve:
    addi   sp, sp, -16              # move stack pointer
    sw     ra, 0(sp)                # store return address

    addi   t0, x0, 16               # store 16 in t0 for compare
    bge    a0, t0, solve_ret_one    # return 1 if index >= 16

    add    t0, a0, a1
    lb     t0, 0(t0)                # get test_c_data[index] in t0
    blt    x0, t0, solve_ret_solve  # recursive call if test_c_data[index] > 0

    addi   t0, x0, 1                # init t0 (i) to 1
    addi   t1, x0, 4                # init t1 to 4 for compare
solve_loop:
    add    t2, a0, a1
    sb     t0, 0(t2)                # test_c_data[index] = i
    sw     a0, 4(sp)                # store a0
    sw     t0, 8(sp)                # store t0
    sw     t1, 12(sp)               # store t1
    jal    check
    beq    a0, x0, solve_check_fail # check return false
    lw     a0, 4(sp)                # load a0
    addi   a0, a0, 1                # increase index
    jal    solve
    bne    a0, x0, solve_ret_one    # return 1 if both are true
solve_check_fail:
    lw     a0, 4(sp)                # load a0
    lw     t0, 8(sp)                # load t0
    lw     t1, 12(sp)               # load t1
    addi   t0, t0, 1                # increase i
    bge    t1, t0, solve_loop       # back to loop if i <= 4

    add    t0, a0, a1
    sb     x0, 0(t0)                # set test_c_data[index] to 0
    j      solve_ret_zero

solve_ret_solve:
    addi   a0, a0, 1                # add index by 1
    jal    solve                    # call solve
    j      solve_done

solve_ret_one:
    addi   a0, x0, 1                # return 1 in a0
    j      solve_done

solve_ret_zero:
    addi   a0, x0, 0                # return 0 in a0

solve_done:
    lw     ra, 0(sp)                # load return address
    addi   sp, sp, 16               # move stack pointer back
    jr     ra

check:
    addi   sp, sp, -4               # move stack pointer
    sw     ra, 0(sp)                # store return address

    srl    t0, a0, 2                # row = index / 4
    andi   t1, a0, 3                # column = index % 4
    add    t2, a0, a1
    lb     t2, 0(t2)                # test_c_data[index]

    addi   t3, x0, 1                # init t3(i) to 1
    addi   t4, x0, 4                # init t4 to 4 for compare
check_loop_1:
    sll    t5, t3, 2                # 4 * i
    add    t5, t5, a0               # index + 4 * i
    andi   t5, t5, 15               # (index + 4 * i) % 16
    add    t5, t5, a1
    lb     t5, 0(t5)                # test_c_data[(index + 4 * i) % 16]
    beq    t5, t2, check_ret_zero
    add    t5, a0, t3               # index + i
    andi   t5, t5, 3                # (index + i) % 4
    sll    t6, t0, 2                # 4 * row
    add    t5, t5, t6               # (index + i) % 4 + 4 * row
    add    t5, t5, a1
    lb     t5, 0(t5)                # test_c_data[(index + i) % 4 + 4 * row]
    beq    t5, t2, check_ret_zero
    addi   t3, t3, 1                # increase i
    blt    t3, t4, check_loop_1     # back to loop if i < 4

    andi   t3, t0, 1                # row % 2
    sub    t0, t0, t3               # start_row = row - row % 2
    andi   t3, t1, 1                # column % 2
    sub    t1, t1, t3               # start_column = column - column % 2

    addi   t3, x0, 0                # init t3(i) to 0
    addi   t5, x0, 2                # init t5 to 2 for compare
check_loop_out:
    addi   t4, x0, 0                # init t4(j) to 0
check_loop_in:
    add    t6, t0, t3               # start_row + i
    sll    t6, t6, 2                # (start_row + i) * 4
    add    t6, t6, t1               # (start_row + i) * 4 + start_column
    add    t6, t6, t4               # (start_row + i) * 4 + start_column + j
    beq    a0, t6, check_fail       # index == id
    add    t6, t6, a1
    lb     t6, 0(t6)                # test_c_data[id]
    beq    t6, t2, check_ret_zero
check_fail:
    addi   t4, t4, 1                # increase j
    blt    t4, t5, check_loop_in    # back to loop if j < 2
    addi   t3, t3, 1                # increase i
    blt    t3, t5, check_loop_out   # back to loop if i < 2

    addi   a0, x0, 1                # return 1 in a0
    j      check_done

check_ret_zero:
    addi   a0, x0, 0                # return 0 in a0

check_done:
    lw     ra, 0(sp)                # load return address
    addi   sp, sp, 4                # move stack pointer back
    jr     ra

    .size sudoku_2x2_asm, .-sudoku_2x2_asm
```

- `sudoku_2x2_c.c`
```cpp=
// c code & comment
#include <stdio.h>
#include <stdlib.h>

int check(int index, char *test_c_data)
{
    int row = index / 4;    // calculate row
    int column = index % 4; // calculate col

    for (int i = 1; i < 4; i++)
    {
        // check numbers in same col
        if (test_c_data[(index + 4 * i) % 16] == test_c_data[index])
        {
            return 0;
        }
        // check numbers in same row
        if (test_c_data[(index + i) % 4 + 4 * row] == test_c_data[index])
        {
            return 0;
        }
    }
    
    int start_row = row - row % 2;          // top row in square
    int start_column = column - column % 2; // left col in square

    for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++)
        {
            // check numbers in square
            int id = (start_row + i) * 4 + start_column + j;
            if (test_c_data[id] == test_c_data[index] && index != id)
            {
                return 0;
            }
        }
    }
    
    // pass
    return 1;
}

int solve(int index, char *test_c_data)
{
    if(index >= 16)
    {
        // all number filled, return true
        return 1;
    }
    if(test_c_data[index])
    {
        // number already filled, solve the next number
        return solve(index + 1, test_c_data);
    }
    else
    {
        for (int i = 1; i <= 4; i++)
        {
            // try 1 ~ 4
            test_c_data[index] = i;
            // check whether i is valid in sudoku
            // if check pass, solve the next number
            if (check(index, test_c_data) && solve(index + 1, test_c_data))
            {
                return 1;
            }
        }
    }
    // set value to 0 to mark it as empty
    test_c_data[index] = 0;
    // solve failed
    return 0;
}

void sudoku_2x2_c(char *test_c_data){
    // TODO
    // Finish your sudoku algorithm in c language
    solve(0, test_c_data);
}
```

### Simulation Result
> 請放上你在 docker中的 rv32emu模擬結果，驗證程式碼的正確性。 (螢幕截圖即可)

![](https://course.playlab.tw/md/uploads/b2d4508c-0bd1-4801-b8c4-ffe4ea0133c5.png)
