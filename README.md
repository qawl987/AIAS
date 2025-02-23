## Hw 7-1 - RISC-V M-Standard Extension
### C code - MUL (範例)
> 請參考 Lab6-1和下方範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// C code you add & comment
// Please DON'T copy all your code, just copy the part you add

// Instructionuction : MUL
// Line 48
typedef enum {
    UNIMPL = 0,
    MUL,  
} instr_type;

//line 98
instr_type parse_instr(char* tok) {
    if ( streq(tok , "mul")) return MUL;
}

//line 528
switch( op ) {
    case UNIMPL: return 1;
    case MUL:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 774
switch (i.op) {
    case MUL:
        rf[i.a1.reg] = rf[i.a2.reg] * rf[i.a3.reg];
        break;
}
```
#### Simulation Result & Assembly Code
> 1. 請放上你用來驗證 instruction的 assembly code, 並加上預期結果的註解。
> 2. 使用 RV32 Emulator模擬, 驗證程式碼的正確性。
> 3. 用以驗證的 assembly code可以有不只一組, 也可以只有一組, 確保 function正確就好。
- Assembly code to test MUL function
```mipsasm=
main:
addi x28,x0 ,2     ## x28 = 2
addi x29,x0 ,3     ## x29 = 3
mul  x30,x28,x29   ## x30 = 2*3 = 6
hcf                ## Terminate
```
- Simulation result

![](https://course.playlab.tw/md/uploads/caa38c6b-01bf-49ae-93e0-6dee5f1d0c69.png)



### C code - MULHU (TODO)
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : MULHU
// Line 48
typedef enum {
    UNIMPL = 0,
    MULHU,  
} instr_type;

//line 98
instr_type parse_instr(char* tok) {
    if ( streq(tok , "mulhu")) return MULHU;
}

//line 528
switch( op ) {
    case UNIMPL: return 1;
    case MULHU:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 774
switch (i.op) {
    case MULHU:
        // convert to 64 bits unsigned integer and mutiply, then store the first 32 bits
        rf[i.a1.reg] = ((uint64_t)rf[i.a2.reg] * (uint64_t)rf[i.a3.reg]) >> 32;
        break;
}
```

#### Simulation Result & Assembly Code

- Assembly code to test ULHUMULHU function
```mipsasm=
main:
addi   x28, x0, 255  ## x28 = 0x000000ff
slli   x28, x28, 23  ## x28 = 0x7f800000
addi   x29, x0, 15   ## x29 = 0x0000000f
mulhu  x30, x28, x29 ## x30 = first 32 bits of (0x7f800000 * 0x0000000f)
mul    x31, x28, x29 ## x31 = last 32 bits of (0x7f800000 * 0x0000000f)

## check MULHU instruction treat number as unsigned int
slli   x26, x28, 1   ## x26 = 0xff000000
mulhu  x27, x26, x29 ## x27 = first 32 bits of (0xff000000 * 0x0000000f)
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/2722d0fb-7d83-4e98-913f-a2724b4bbfa5.png)

### C code - REM (TODO)
```cpp=
// Instructionuction : REM
// Line 48
typedef enum {
    UNIMPL = 0,
    REM,  
} instr_type;

//line 98
instr_type parse_instr(char* tok) {
    if ( streq(tok , "rem")) return REM;
}

//line 528
switch( op ) {
    case UNIMPL: return 1;
    case REM:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 774
switch (i.op) {
    case REM:
        // check division by zero
        if (rf[i.a3.reg] == 0)
        {
            rf[i.a1.reg] = rf[i.a2.reg];
        }
        // check signed division overflow
        else if (rf[i.a2.reg] == 0x80000000 && rf[i.a3.reg] == 0xffffffff)
        {
            rf[i.a1.reg] = 0;
        }
        // get remainder
        else
        {
            rf[i.a1.reg] = (int32_t)rf[i.a2.reg] % (int32_t)rf[i.a3.reg];
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test MULH function
```mipsasm=
main:
addi   x27, x0, -23  ## x27 = -23
addi   x28, x0, 5    ## x28 = 5
rem    x29, x27, x28 ## x29 = (-23) % 5

## check signed division overflow
addi   x27, x0, 1    ## x27 = 1
slli   x27, x27, 31  ## x27 = 0x80000000
addi   x28, x0, -1   ## x28 = -1
rem    x30, x27, x28 ## x30 = 0x80000000 % (-1)

## check division by zero
rem    x31, x27, x0  ## x31 = 0x80000000 % 0
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/670028ff-e36f-4379-83f8-70a5612318fe.png)

### C code - REMU (TODO)
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : REMU
// Line 48
typedef enum {
    UNIMPL = 0,
    REMU,  
} instr_type;

//line 98
instr_type parse_instr(char* tok) {
    if ( streq(tok , "remu")) return REMU;
}

//line 528
switch( op ) {
    case UNIMPL: return 1;
    case REMU:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 774
switch (i.op) {
    case REMU:
        // check division by zero
        if (rf[i.a3.reg] == 0)
        {
            rf[i.a1.reg] = rf[i.a2.reg];
        }
        // convert to unsigned integer and get remainder
        else
        {
            rf[i.a1.reg] = (uint32_t)rf[i.a2.reg] % (uint32_t)rf[i.a3.reg];
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test REMU function
```mipsasm=
main:
addi   x27, x0, 23   ## x27 = 23
addi   x28, x0, 5    ## x28 = 5
remu   x29, x27, x28 ## x29 = 23 % 5 = 3

## check REMU instruction treat number as unsigned int
addi   x27, x0, -23  ## x27 = -23
remu   x30, x27, x28 ## x30 = (-23) % 5

## check division by zero
remu   x31, x27, x0  ## x31 = (-23) % 0
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/809fccfa-8f7c-4b4e-b179-5594e9b10061.png)
