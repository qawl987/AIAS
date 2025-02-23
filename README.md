## Homework 9

### Homework 9-1 5-stage pipelined CPU Implementation
- Please Finish the all others rv32i instrunctions，and pass the **rv32ui_SingleTest/TestALL.S** 

Paste the **simulate RegFile result**  Here
![](https://course.playlab.tw/md/uploads/f053a5c8-517d-4006-91e3-dc6e94fc439a.png)

- You can use any single test，and compare to the  **rv32ui_SingleTest/golden.txt** 

### Homework 9-2 Data and Controll Hazards  
- List possible data hazard scenarios and describe how to resolve the hazard in your design.
    
- Rd of instruction A in EXE or MEM or WB stage is the same as Rs of instruction B in ID stage** 
    stall IF, ID stage and flush EXE stage
    
- Please Finish the all others RV32I instrunctions，and pass the **rv32ui_SingleTest/TestDataHazard.S** 

Paste the **simulate RegFile result**  Here
![](https://course.playlab.tw/md/uploads/538b5e8d-0980-4725-b079-12d8cc901ef0.png)

- In the result of register file，if sp(x2) is **zero**，it means you passed the test，otherwise，the value of sp(x2) is the test case that you did not passed.

### Homework 9-3 Performance Counters and Performance Analysis
- Run the **fib.asm** and post the **Performance count and analysis** result.

Paste the result Here (Screenshot).
![](https://course.playlab.tw/md/uploads/9f12ea75-e65b-4d51-8801-ebb99c153ede.png)

- Explain How a 5-stage pipelined  CPU improves performance compared to a single-cycle CPU

Explain the result Here.
Single-cycle CPU必須在一個較長的cycle內完成每條instruction的所有步驟(stage)，並且CPU的某些component因為要等待其他component的結果而閒置，而5-stage pipelined CPU透過將instruction分成五個步驟(stage)，讓這五個步驟(stage)同時運行，減少CPU內component的idle時間，並降低cycle時長，進而提升效能。

## Bonus 

- If you have done any bonus, you may paste your results in this section
 
- Bonus: Kadane's algorithm
![](https://course.playlab.tw/md/uploads/39abe00a-630e-4b2b-95af-aa59a356d067.png)

- Bonus: Merge sort
![](https://course.playlab.tw/md/uploads/21597291-8572-480b-950a-afe738e0c0f9.png)
