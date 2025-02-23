### Homework 9-4 Bitmanip Extension (Group Assignment)
#### Group Members
- List your group members (including you!) -
    - 簡子茸
    - 丁方凱

#### 硬體架構圖：
- 小組選擇的base CPU架構圖，是誰的呢?
    - 簡子茸的 (同lab9)

    ![](https://course.playlab.tw/md/uploads/d82c2f12-9f25-4108-a097-6e4d5b4a69ba.png)

- Option 1 - 照指令分工的組別
    - 請描述你負責哪些指令，你如何設計Datapath和Controll signal呢?
    - 新增了哪些Blocks，或者因為有相似利用了原本的Datapath呢?
    - 根據你的描述，將你的設計畫在小組選擇的base CPU上。

    
    ### Emulator functionality - <clz>
    > 描述該指令的格式以及功能
    - clz rd, rs
    - count leading zeros in rs, store the result in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add CLZ
    // function instr_type parse_instr
    if ( streq(tok , "clz")) return CLZ;
    // function int parse_instr
    case CLZ:
        if ( !o1 || !o2 || o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        return 1;
    // function execute
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
    ```

    ### Assembler translation - <clz>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case CLZ:
        binary = 0x13;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x1 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += 0x0 << 20;
        binary += 0x30 << 25;
        break;
    ```
    
    ### Emulator functionality - <ctz>
    > 描述該指令的格式以及功能
    - ctz rd, rs
    - count tailing zeros in rs, store the result in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add CTZ
    // function instr_type parse_instr
    if ( streq(tok , "ctz")) return CTZ;
    // function int parse_instr
    case CTZ:
        if ( !o1 || !o2 || o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        return 1;
    // function execute
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
    ```

    ### Assembler translation - <ctz>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case CTZ:
        binary = 0x13;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x1 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += 0x1 << 20;
        binary += 0x30 << 25;
        break;
    ```
    
    ### Emulator functionality - <cpop>
    > 描述該指令的格式以及功能
    - cpop rd, rs
    - count ones in rs, store the result in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add CPOP
    // function instr_type parse_instr
    if ( streq(tok , "cpop")) return CPOP;
    // function int parse_instr
    case CPOP:
        if ( !o1 || !o2 || o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        return 1;
    // function execute
    case CPOP:
        // output : xlenbits = 0
        rf[i.a1.reg] = 0;
        // loop through all bit
        for (int j = 0; j < 32; j++)
        {
            // check each bit
            if ((rf[i.a2.reg] >> j) & 1)
            {
                rf[i.a1.reg]++;
            }
        }
        break;
    ```

    ### Assembler translation - <cpop>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case CPOP:
        binary = 0x13;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x1 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += 0x2 << 20;
        binary += 0x30 << 25;
        break;
    ```
    
    ### Emulator functionality - <andn>
    > 描述該指令的格式以及功能
    - andn rd, rs1, rs2
    - calculate rs1 & ~rs2, store the result in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add ANDN
    // function instr_type parse_instr
    if ( streq(tok , "andn")) return ANDN;
    // function int parse_instr
    case ANDN:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line, "Invalid format" );
        i->a1.reg = parse_reg(o1, line);
        i->a2.reg = parse_reg(o2, line);
        i->a3.reg = parse_reg(o3, line);
        return 1;
    // function execute
    case ANDN:
        // X(rd) = X(rs1) & ~X(rs2)
        rf[i.a1.reg] = rf[i.a2.reg] & ~rf[i.a3.reg];
        break;
    ```

    ### Assembler translation - <andn>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case ANDN:
        binary = 0x33;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x7 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += i.a3.reg << 20;    //rs2
        binary += 0x20 << 25;
        break;
    ```
    
    ### Emulator functionality - <orn>
    > 描述該指令的格式以及功能
    - orn rd, rs1, rs2
    - calculate rs1 | ~rs2, store the result in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add ORN
    // function instr_type parse_instr
    if ( streq(tok , "orn")) return ORN;
    // function int parse_instr
    case ORN:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line, "Invalid format" );
        i->a1.reg = parse_reg(o1, line);
        i->a2.reg = parse_reg(o2, line);
        i->a3.reg = parse_reg(o3, line);
        return 1;
    // function execute
    case ORN:
        // X(rd) = X(rs1) | ~X(rs2)
        rf[i.a1.reg] = rf[i.a2.reg] | ~rf[i.a3.reg];
        break;
    ```

    ### Assembler translation - <orn>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case ORN:
        binary = 0x33;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x6 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += i.a3.reg << 20;    //rs2
        binary += 0x20 << 25;
        break;
    ```
    
    ### Emulator functionality - <xnor>
    > 描述該指令的格式以及功能
    - xnor rd, rs1, rs2
    - calculate ~(rs1 ^ rs2), store the result in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add XNOR
    // function instr_type parse_instr
    if ( streq(tok , "xnor")) return XNOR;
    // function int parse_instr
    case XNOR:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line, "Invalid format" );
        i->a1.reg = parse_reg(o1, line);
        i->a2.reg = parse_reg(o2, line);
        i->a3.reg = parse_reg(o3, line);
        return 1;
    // function execute
    case XNOR:
        // X(rd) = ~(X(rs1) ^ ~X(rs2))
        rf[i.a1.reg] = ~(rf[i.a2.reg] ^ rf[i.a3.reg]);
        break;
    ```

    ### Assembler translation - <xnor>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case XNOR:
        binary = 0x33;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x4 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += i.a3.reg << 20;    //rs2
        binary += 0x20 << 25;
        break;
    ```
    
    ### Emulator functionality - <min>
    > 描述該指令的格式以及功能
    - min rd, rs1, rs2
    - compare rs1, rs2 as signed integer, store the smaller one in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add MIN
    // function instr_type parse_instr
    if ( streq(tok , "min")) return MIN;
    // function int parse_instr
    case MIN:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line, "Invalid format" );
        i->a1.reg = parse_reg(o1, line);
        i->a2.reg = parse_reg(o2, line);
        i->a3.reg = parse_reg(o3, line);
        return 1;
    // function execute
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
    ```

    ### Assembler translation - <min>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case MIN:
        binary = 0x33;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x4 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += i.a3.reg << 20;    //rs2
        binary += 0x5 << 25;
        break;
    ```
    
    ### Emulator functionality - <max>
    > 描述該指令的格式以及功能
    - max rd, rs1, rs2
    - compare rs1, rs2 as signed integer, store the larger one in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add MAX
    // function instr_type parse_instr
    if ( streq(tok , "max")) return MAX;
    // function int parse_instr
    case MAX:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line, "Invalid format" );
        i->a1.reg = parse_reg(o1, line);
        i->a2.reg = parse_reg(o2, line);
        i->a3.reg = parse_reg(o3, line);
        return 1;
    // function execute
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
    ```

    ### Assembler translation - <max>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case MAX:
        binary = 0x33;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x6 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += i.a3.reg << 20;    //rs2
        binary += 0x5 << 25;
        break;
    ```
    
    ### Emulator functionality - <minu>
    > 描述該指令的格式以及功能
    - minu rd, rs1, rs2
    - compare rs1, rs2 as unsigned integer, store the smaller one in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add MINU
    // function instr_type parse_instr
    if ( streq(tok , "minu")) return MINU;
    // function int parse_instr
    case MINU:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line, "Invalid format" );
        i->a1.reg = parse_reg(o1, line);
        i->a2.reg = parse_reg(o2, line);
        i->a3.reg = parse_reg(o3, line);
        return 1;
    // function execute
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
    ```

    ### Assembler translation - <minu>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case MINU:
        binary = 0x33;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x5 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += i.a3.reg << 20;    //rs2
        binary += 0x5 << 25;
        break;
    ```
    
    ### Emulator functionality - <maxu>
    > 描述該指令的格式以及功能
    - maxu rd, rs1, rs2
    - compare rs1, rs2 as unsigned integer, store the larger one in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add MAXU
    // function instr_type parse_instr
    if ( streq(tok , "maxu")) return MAXU;
    // function int parse_instr
    case MAXU:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line, "Invalid format" );
        i->a1.reg = parse_reg(o1, line);
        i->a2.reg = parse_reg(o2, line);
        i->a3.reg = parse_reg(o3, line);
        return 1;
    // function execute
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
    ```

    ### Assembler translation - <maxu>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case MAXU:
        binary = 0x33;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x7 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += i.a3.reg << 20;    //rs2
        binary += 0x5 << 25;
        break;
    ```
    
    ### Emulator functionality - <sext.b>
    > 描述該指令的格式以及功能
    - sext.b rd, rs
    - sign-extends the least-significant byte in rs, store the result in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add SEXT_B
    // function instr_type parse_instr
    if ( streq(tok , "sext.b")) return SEXT_B;
    // function int parse_instr
    case SEXT_B:
        if ( !o1 || !o2 || o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        return 1;
    // function execute
    case SEXT_B:
        // check the sign bit of the target byte
        if ((rf[i.a2.reg] >> 7) & 1)
        {
            // extend with 1
            rf[i.a1.reg] = (rf[i.a2.reg] & 0xff) | 0xffffff00;
        }
        else{
            // extend with 0
            rf[i.a1.reg] = rf[i.a2.reg] & 0xff;
        }
        break;
    ```

    ### Assembler translation - <sext.b>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case SEXT_B:
        binary = 0x13;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x1 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += 0x4 << 20;
        binary += 0x30 << 25;
        break;
    ```
    
    ### Emulator functionality - <sext.h>
    > 描述該指令的格式以及功能
    - sext.h rd, rs
    - sign-extends the least-significant halfword in rs, store the result in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add SEXT_H
    // function instr_type parse_instr
    if ( streq(tok , "sext.h")) return SEXT_H;
    // function int parse_instr
    case SEXT_H:
        if ( !o1 || !o2 || o3 || o4 ) print_syntax_error( line,  "Invalid format" );
        i->a1.reg = parse_reg(o1 , line);
        i->a2.reg = parse_reg(o2 , line);
        return 1;
    // function execute
    case SEXT_H:
        // check the sign bit of the target halfword
        if ((rf[i.a2.reg] >> 15) & 1)
        {
            // extend with 1
            rf[i.a1.reg] = (rf[i.a2.reg] & 0xffff) | 0xffff0000;
        }
        else{
            // extend with 0
            rf[i.a1.reg] = rf[i.a2.reg] & 0xffff;
        }
        break;
    ```

    ### Assembler translation - <sext.h>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case SEXT_H:
        binary = 0x13;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x1 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += 0x5 << 20;
        binary += 0x30 << 25;
        break;
    ```
    
    ### Emulator functionality - <bset>
    > 描述該指令的格式以及功能
    - bset rd, rs1, rs2
    - set the ith bit (determined by rs2's last 5 bits) in rs1, store the result in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add BSET
    // function instr_type parse_instr
    if ( streq(tok , "bset")) return BSET;
    // function int parse_instr
    case BSET:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line, "Invalid format" );
        i->a1.reg = parse_reg(o1, line);
        i->a2.reg = parse_reg(o2, line);
        i->a3.reg = parse_reg(o3, line);
        return 1;
    // function execute
    case BSET:
        // X(rd) = X(rs1) | (1 << (X(rs2) & 0x1f))
        rf[i.a1.reg] = rf[i.a2.reg] | (1 << (rf[i.a3.reg] & 0x1f));
        break;
    ```

    ### Assembler translation - <bset>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case BSET:
        binary = 0x33;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x1 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += i.a3.reg << 20;    //rs2
        binary += 0x14 << 25;
        break;
    ```
    
    ### Emulator functionality - <bclr>
    > 描述該指令的格式以及功能
    - bclr rd, rs1, rs2
    - clear the ith bit (determined by rs2's last 5 bits) in rs1, store the result in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add BCLR
    // function instr_type parse_instr
    if ( streq(tok , "bclr")) return BCLR;
    // function int parse_instr
    case BCLR:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line, "Invalid format" );
        i->a1.reg = parse_reg(o1, line);
        i->a2.reg = parse_reg(o2, line);
        i->a3.reg = parse_reg(o3, line);
        return 1;
    // function execute
    case BCLR:
        // X(rd) = X(rs1) & ~(1 << (X(rs2) & 0x1f))
        rf[i.a1.reg] = rf[i.a2.reg] & ~(1 << (rf[i.a3.reg] & 0x1f));
        break;
    ```

    ### Assembler translation - <bclr>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case BCLR:
        binary = 0x33;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x1 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += i.a3.reg << 20;    //rs2
        binary += 0x24 << 25;
        break;
    ```
    
    ### Emulator functionality - <binv>
    > 描述該指令的格式以及功能
    - binv rd, rs1, rs2
    - invert the ith bit (determined by rs2's last 5 bits) in rs1, store the result in rd

    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    // emulator.h instr_type add BINV
    // function instr_type parse_instr
    if ( streq(tok , "binv")) return BINV;
    // function int parse_instr
    case BINV:
        if ( !o1 || !o2 || !o3 || o4 ) print_syntax_error( line, "Invalid format" );
        i->a1.reg = parse_reg(o1, line);
        i->a2.reg = parse_reg(o2, line);
        i->a3.reg = parse_reg(o3, line);
        return 1;
    // function execute
    case BINV:
        // X(rd) = X(rs1) ^ (1 << (X(rs2) & 0x1f))
        rf[i.a1.reg] = rf[i.a2.reg] ^ (1 << (rf[i.a3.reg] & 0x1f));
        break;
    ```

    ### Assembler translation - <binv>
    > **將新增的 code**(case部份就好)放在下方並加上註解, 讓TA明白你是如何完成的。
    ```cpp=
    case BINV:
        binary = 0x33;               //opcode
        binary += i.a1.reg << 7;     //rd
        binary += 0x1 << 12;
        binary += i.a2.reg << 15;    //rs1
        binary += i.a3.reg << 20;    //rs2
        binary += 0x34 << 25;
        break;
    ```
    

#### 測試結果
- 測試檔案
    - ``lab08/emulator/example_code/hw8_3.asm``
- 測試結果
    - ![](https://course.playlab.tw/md/uploads/229bed91-5ae5-4bc9-8bb6-4bc3f06ef177.jpg)


#### 小組最後完成CPU架構圖
- 同 lab9 (沒有新增 hardware module)
    ![](https://course.playlab.tw/md/uploads/d82c2f12-9f25-4108-a097-6e4d5b4a69ba.png)
