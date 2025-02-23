## HW7-2 - RISC-V Bit Manipulation Extension

### C code - ANDN
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : ANDN
// Line 50
typedef enum {
    UNIMPL = 0,
    ANDN,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "andn")) return ANDN;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case ANDN:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 896
switch (i.op) {
    case ANDN:
        // X(rd) = X(rs1) & ~X(rs2)
        rf[i.a1.reg] = rf[i.a2.reg] & ~rf[i.a3.reg];
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test ANDN function
```mipsasm=
main:
addi x29, x0, 549  ## x29 = 549
addi x30, x0, 0    ## x30 = 0
andn x31, x29, x30 ## x31 = x29 & ~x30 = 549
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/3480c4fa-6915-4376-9dca-bfb02e1a3bd0.png)

### C code - CLMUL
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : CLMUL
// Line 50
typedef enum {
    UNIMPL = 0,
    CLMUL,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "clmul")) return CLMUL;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case CLMUL:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 896
switch (i.op) {
    case CLMUL:
        // output : xlenbits = 0
        rf[i.a1.reg] = 0;
        // foreach (i from 0 to (xlen - 1) by 1)
        for (int j = 0; j < 32; j++)
        {
            // if ((rs2_val >> i) & 1) then output = output ^ (rs1_val << i)
            if ((rf[i.a3.reg] >> j) & 1)
            {
                rf[i.a1.reg] = rf[i.a1.reg] ^ (rf[i.a2.reg] << j);
            }
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test CLMUL function
```mipsasm=
main:
addi  x26, x0, 2    ## x26 = 2
addi  x27, x0, 3    ## x27 = 3
clmul x28, x26, x27 ## x28 = 2 * 3 carry-less

addi  x29, x0, 3    ## x29 = 3
addi  x30, x0, 3    ## x30 = 3
clmul x31, x29, x30 ## x31 = 3 * 3 carry-less
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/3fd37eae-9cf8-4e51-ae63-2f0bd924df1d.png)

### C code - CLMULH
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : CLMULH
// Line 50
typedef enum {
    UNIMPL = 0,
    CLMULH,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "clmulh")) return CLMULH;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case CLMULH:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 896
switch (i.op) {
    case CLMULH:
        // output : xlenbits = 0
        rf[i.a1.reg] = 0;
        // foreach (i from 1 to xlen by 1)
        for (int j = 1; j <= 32; j++)
        {
            // if ((rs2_val >> i) & 1) then output = output ^ (rs1_val >> (xlen - i))
            if ((rf[i.a3.reg] >> j) & 1)
            {
                rf[i.a1.reg] = rf[i.a1.reg] ^ (rf[i.a2.reg] >> (32 - j));
            }
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test CLMULH function
```mipsasm=
main:
addi   x29, x0, -549 ## x29 = -549
addi   x30, x0, -648 ## x30 = -648
clmulh x31, x29, x30 ## x31 = clmulh, -549, -648, should equal to (clmulr, -549, -648) >> 1
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/5ef846f3-601c-4553-ad53-f3b7deb96cd3.png)

### C code - CLMULR
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : CLMULR
// Line 50
typedef enum {
    UNIMPL = 0,
    CLMULR,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "clmulr")) return CLMULR;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case CLMULR:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 896
switch (i.op) {
    case CLMULR:
        // output : xlenbits = 0
        rf[i.a1.reg] = 0;
        // foreach (i from 0 to (xlen - 1) by 1)
        for (int j = 0; j < 32; j++)
        {
            // if ((rs2_val >> i) & 1) then output = output ^ (rs1_val >> (xlen - i - 1))
            if ((rf[i.a3.reg] >> j) & 1)
            {
                rf[i.a1.reg] = rf[i.a1.reg] ^ (rf[i.a2.reg] >> (32 - j - 1));
            }
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test CLMULR function
```mipsasm=
main:
addi   x29, x0, -549 ## x29 = -549
addi   x30, x0, -648 ## x30 = -648
clmulr x31, x29, x30 ## x31 = clmulr, -549, -648, should equal to (clmulh, -549, -648) << 1
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/4dbafae8-8a3c-4a61-8fa6-a9d4ebaca770.png)

### C code - CLZ
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : CLZ
// Line 50
typedef enum {
    UNIMPL = 0,
    CLZ,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "clz")) return CLZ;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case CLZ:
        if ( !o1 || !o2 || o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        return 1;
}

//line 896
switch (i.op) {
    case CLZ:
        // output : xlenbits = 0
        rf[i.a1.reg] = 0;
        // count from MSB
        for (int j = 31; j >= 0; j--)
        {
            // check if bit set 1
            if ((rf[i.a2.reg] >> j) & 1)
            {
                break;
            }
            else
            {
                rf[i.a1.reg]++;
            }
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test CLZ function
```mipsasm=
main:
addi x30, x0, 1   ## x30 = 1
slli x30, x30, 17 ## x30 = 0x00020000
clz  x31, x30     ## get 0s' count start from MSB till the first 1
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/1dda2379-c2bd-440b-aad3-ee387b41a288.png)

### C code - CPOP
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : CPOP
// Line 50
typedef enum {
    UNIMPL = 0,
    CPOP,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "cpop")) return CPOP;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case CPOP:
        if ( !o1 || !o2 || o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        return 1;
}

//line 896
switch (i.op) {
    case CPOP:
        // output : xlenbits = 0
        rf[i.a1.reg] = 0;
        // loop through all bits
        for (int j = 0; j < 32; j++)
        {
            // check each bit
            if ((rf[i.a2.reg] >> j) & 1)
            {
                rf[i.a1.reg]++;
            }
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test CPOP function
```mipsasm=
main:
addi x30, x0, 1   ## x30 = 1
slli x30, x30, 17 ## x30 = 0x00020000
addi x30, x30, 15 ## x30 = 0x0002000f
cpop x31, x30     ## get all 1s' count
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/14bf5913-6616-4878-9731-bbc8f9f6db09.png)

### C code - CTZ
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : CTZ
// Line 50
typedef enum {
    UNIMPL = 0,
    CTZ,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "ctz")) return CTZ;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case CTZ:
        if ( !o1 || !o2 || o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        return 1;
}

//line 896
switch (i.op) {
    case CTZ:
        // output : xlenbits = 0
        rf[i.a1.reg] = 0;
        // count from LSB
        for (int j = 0; j < 32; j++)
        {
            // check if bit set 1
            if ((rf[i.a2.reg] >> j) & 1)
            {
                break;
            }
            else
            {
                rf[i.a1.reg]++;
            }
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test CTZ function
```mipsasm=
main:
addi x30, x0, 1   ## x30 = 1
slli x30, x30, 17 ## x30 = 0x00020000
ctz  x31, x30     ## get 0s' count start from LSB till the first 1
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/96031733-8696-43e3-a8b7-f7f8913450ef.png)

### C code - MAX
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : MAX
// Line 50
typedef enum {
    UNIMPL = 0,
    MAX,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "max")) return MAX;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case MAX:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 896
switch (i.op) {
    case MAX:
        // compare 2 signed integers, return bigger one
        if ((int32_t)rf[i.a2.reg] > (int32_t)rf[i.a3.reg])
        {
            rf[i.a1.reg] = rf[i.a2.reg];
        }
        else
        {
            rf[i.a1.reg] = rf[i.a3.reg];
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test MAX function
```mipsasm=
main:
addi x26, x0, 5    ## x26 = 5
addi x27, x0, -1   ## x27 = -1
max  x28, x26, x27 ## x28 should be 5, check number < 0 comparison

addi x29, x0, 35   ## x29 = 35
addi x30, x0, 35   ## x30 = 35
max  x31, x29, x30 ## x31 should be 35, check number equal
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/3131a55e-f983-4206-a405-311d4049bba1.png)

### C code - MAXU
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : MAXU
// Line 50
typedef enum {
    UNIMPL = 0,
    MAXU,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "maxu")) return MAXU;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case MAXU:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 896
switch (i.op) {
    case MAXU:
        // compare 2 unsigned integers, return bigger one
        if ((uint32_t)rf[i.a2.reg] > (uint32_t)rf[i.a3.reg])
        {
            rf[i.a1.reg] = rf[i.a2.reg];
        }
        else
        {
            rf[i.a1.reg] = rf[i.a3.reg];
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test MAXU function
```mipsasm=
main:
addi x26, x0, 5    ## x26 = 5
addi x27, x0, -1   ## x27 = -1 (unsigned max)
maxu x28, x26, x27 ## x28 should be -1, check MAXU treat number as unsigned int

addi x29, x0, 38   ## x29 = 38
addi x30, x0, 354  ## x30 = 354
maxu x31, x29, x30 ## x31 should be 354
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/a91a716f-abaf-4330-baf8-2eedbe0c7939.png)

### C code - MIN
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : MIN
// Line 50
typedef enum {
    UNIMPL = 0,
    MIN,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "min")) return MIN;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case MIN:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 896
switch (i.op) {
    case MIN:
        // compare 2 signed integers, return smaller one
        if ((int32_t)rf[i.a2.reg] < (int32_t)rf[i.a3.reg])
        {
            rf[i.a1.reg] = rf[i.a2.reg];
        }
        else
        {
            rf[i.a1.reg] = rf[i.a3.reg];
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test MIN function
```mipsasm=
main:
addi x26, x0, 5    ## x26 = 5
addi x27, x0, -1   ## x27 = -1
min  x28, x26, x27 ## x28 should be -1, check number < 0 comparison

addi x29, x0, -38  ## x29 = -38
addi x30, x0, -8   ## x30 = -8
min  x31, x29, x30 ## x31 should be -38
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/d6452f4e-6fb1-4e95-be04-ddbb9679240e.png)

### C code - MINU
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : MINU
// Line 50
typedef enum {
    UNIMPL = 0,
    MINU,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "minu")) return MINU;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case MINU:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 896
switch (i.op) {
    case MINU:
        // compare 2 unsigned integers, return smaller one
        if ((uint32_t)rf[i.a2.reg] < (uint32_t)rf[i.a3.reg])
        {
            rf[i.a1.reg] = rf[i.a2.reg];
        }
        else
        {
            rf[i.a1.reg] = rf[i.a3.reg];
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test MINU function
```mipsasm=
main:
addi x26, x0, 5    ## x26 = 5
addi x27, x0, -1   ## x27 = -1
minu x28, x26, x27 ## x28 should be 5, check MINU treat number as unsigned int

addi x29, x0, 38   ## x29 = 38
addi x30, x0, 8    ## x30 = 8
minu x31, x29, x30 ## x31 should be 8
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/3bc15fb3-1785-4dc2-8614-98adb11ed11a.png)

### C code - ORC_B
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : ORC_B
// Line 50
typedef enum {
    UNIMPL = 0,
    ORC_B,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "orc.b")) return ORC_B;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case ORC_B:
        if ( !o1 || !o2 || o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        return 1;
}

//line 896
switch (i.op) {
    case ORC_B:
        // output : xlenbits = 0
        rf[i.a1.reg] = 0;
        // loop each byte
        for (int j = 24; j >= 0; j -= 8)
        {
            rf[i.a1.reg] = rf[i.a1.reg] << 8;
            // check each byte
            if ((rf[i.a2.reg] >> j) & 0xff)
            {
                rf[i.a1.reg] = rf[i.a1.reg] | 0xff;
            }
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test ORC_B function
```mipsasm=
main:
addi x30, x0, 15 ## x30 = 15
slli x30, x30, 6 ## x30 = 0x000003c0
orc.b x31, x30   ## x31 should be 0x0000ffff
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/64f6733f-5715-469e-b054-d6521832189a.png)

### C code - ORN
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : ORN
// Line 50
typedef enum {
    UNIMPL = 0,
    ORN,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "orn")) return ORN;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case ORN:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 896
switch (i.op) {
    case ORN:
        // X(rd) = X(rs1) | ~X(rs2)
        rf[i.a1.reg] = rf[i.a2.reg] | ~rf[i.a3.reg];
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test ORN function
```mipsasm=
main:
addi x29, x0, 0    ## x29 = 0
addi x30, x0, 346  ## x30 = 346
orn  x31, x29, x30 ## x31 = x29 | ~x30
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/8c4c1389-cb0e-4011-b971-8f76fc70bb09.png)

### C code - REV8
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : REV8
// Line 50
typedef enum {
    UNIMPL = 0,
    REV8,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "rev8")) return REV8;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case REV8:
        if ( !o1 || !o2 || o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        return 1;
}

//line 896
switch (i.op) {
    case REV8:
        // output : xlenbits = 0
        rf[i.a1.reg] = 0;
        // loop each byte
        for (int j = 0; j < 32; j += 8)
        {
            // loop each bit in byte
            for (int k = 7; k >= 0; k--)
            {
                rf[i.a1.reg] = rf[i.a1.reg] << 1;
                rf[i.a1.reg] = rf[i.a1.reg] | ((rf[i.a2.reg] >> (j+k)) & 1);
            }
        }
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test REV8 function
```mipsasm=
main:
addi x30, x0, 189  ## x30 = 189
slli x30, x30, 8   ## x30 = 0x0000bd00
addi x30, x30, 245 ## x30 = 0x0000bdf5
slli x30, x30, 16  ## x30 = 0xbdf50000
addi x30, x30, 67  ## x30 = 0xbdf50043
rev8 x31, x30      ## reverse each byte
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/d2942cf7-1774-4c18-b908-dbf8ef23b511.png)

### C code - ROL
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : ROL
// Line 50
typedef enum {
    UNIMPL = 0,
    ROL,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "rol")) return ROL;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case ROL:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 896
switch (i.op) {
    case ROL:
        // shamt = rf[i.a3.reg] & 0x1f
        // (X(rs1) << shamt) | (X(rs1) >> (xlen - shamt))
        rf[i.a1.reg] = (rf[i.a2.reg] << (rf[i.a3.reg] & 0x1f)) | (rf[i.a2.reg] >> (32 - (rf[i.a3.reg] & 0x1f)));
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test ROL function
```mipsasm=
main:
addi x29, x0, 15   ## x29 = 15
slli x29, x29, 20  ## x29 = 0x00f00000
addi x29, x29, 255 ## x29 = 0x00f000ff
addi x30, x0, 365  ## x30 = 365
rol  x31, x29, x30 ## rol, 0x00f000ff, 365
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/fa1b8d11-3b7f-4912-9356-8fd6d3d66d33.png)

### C code - ROR
> 請參考 Lab6-1和範例, **將新增的 code**放在下方並加上註解, 讓 TA明白你是如何完成的。
```cpp=
// Instructionuction : ROR
// Line 50
typedef enum {
    UNIMPL = 0,
    ROR,  
} instr_type;

//line 112
instr_type parse_instr(char* tok) {
    if ( streq(tok , "ror")) return ROR;
}

//line 633
switch( op ) {
    case UNIMPL: return 1;
    case ROR:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        i->a3.reg = parse_reg(o3 , line);
        return 1;
}

//line 896
switch (i.op) {
    case ROR:
        // shamt = rf[i.a3.reg] & 0x1f
        // (X(rs1) >> shamt) | (X(rs1) << (xlen - shamt))
        rf[i.a1.reg] = (rf[i.a2.reg] >> (rf[i.a3.reg] & 0x1f)) | (rf[i.a2.reg] << (32 - (rf[i.a3.reg] & 0x1f)));
        break;
}
```
#### Simulation Result & Assembly Code

- Assembly code to test ROR function
```mipsasm=
main:
addi x29, x0, 15   ## x29 = 15
slli x29, x29, 20  ## x29 = 0x00f00000
addi x29, x29, 255 ## x29 = 0x00f000ff
addi x30, x0, 365  ## x30 = 365
ror  x31, x29, x30 ## ror, 0x00f000ff, 365
hcf
```
- Simulation result
> 請放上模擬結果的螢幕截圖

![](https://course.playlab.tw/md/uploads/08a3ca04-c562-47ca-bc97-fa754536fb2a.png)
