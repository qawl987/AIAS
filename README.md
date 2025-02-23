## Hw5-1 TrafficLight with Pedestrian button
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `TrafficLight_pTest.scala` test
package acal_lab05.Hw1

import chisel3._
import chisel3.util._

class TrafficLight_p(Ytime:Int, Gtime:Int, Ptime:Int) extends Module{
  val io = IO(new Bundle{
    val P_button = Input(Bool())
    val H_traffic = Output(UInt(2.W))
    val V_traffic = Output(UInt(2.W))
    val P_traffic = Output(UInt(2.W))
    val timer     = Output(UInt(5.W))
  })

  //please implement your code below...

  //parameter declaration
  val Off = 0.U
  val Red = 1.U
  val Yellow = 2.U
  val Green = 3.U

  val sIdle :: sHGVR :: sHYVR :: sHRVG :: sHRVY :: sPG :: Nil = Enum(6)
  val state = RegInit(sIdle)
  // record previous state when P_button trigger
  val prevState = RegInit(sIdle)
  // record whether sPG state triggered by P_button
  val buttonTrigger = RegInit(false.B)

  //Counter============================
  val cntMode = WireDefault(0.U(2.W))
  val cntReg = RegInit(0.U(4.W))
  val cntDone = Wire(Bool())
  cntDone := cntReg === 0.U

  when(cntDone){
    when(cntMode === 0.U){
      // 綠燈
      cntReg := (Gtime-1).U
    }.elsewhen(cntMode === 1.U){
      // 黃燈
      cntReg := (Ytime-1).U
    }.otherwise{
      // 行人
      cntReg := (Ptime-1).U
    }
  }.otherwise{
    cntReg := cntReg - 1.U
  }
  //Counter end========================

  //Next State Decoder
  switch(state){
    is(sIdle){
      state := sHGVR
    }
    is(sHGVR){
      when(io.P_button){
        // record current state, buttonTrigger, change state to sPG
        buttonTrigger := true.B
        prevState := sHGVR
        state := sPG
      }.elsewhen(cntDone){
        state := sHYVR
      }
    }
    is(sHYVR){
      when(io.P_button){
        // record current state, buttonTrigger, change state to sPG
        buttonTrigger := true.B
        prevState := sHYVR
        state := sPG
      }.elsewhen(cntDone){
        state := sHRVG
      }
    }
    is(sHRVG){
      when(io.P_button){
        // record current state, buttonTrigger, change state to sPG
        buttonTrigger := true.B
        prevState := sHRVG
        state := sPG
      }.elsewhen(cntDone){
        state := sHRVY
      }
    }
    is(sHRVY){
      when(io.P_button){
        // record current state, buttonTrigger, change state to sPG
        buttonTrigger := true.B
        prevState := sHRVY
        state := sPG
      }.elsewhen(cntDone){
        state := sPG
      }
    }
    is(sPG){
      when(cntDone){
        // if triggered by P_button
        when(buttonTrigger){
          // switch back to previous state
          state := prevState
          // set buttonTrigger to false
          buttonTrigger := false.B
        }.otherwise{
          state := sHGVR
        }
      }
    }
  }

  //Output Decoder
  //Default statement
  cntMode := 0.U
  io.H_traffic := Off
  io.V_traffic := Off
  io.P_traffic := Off

  switch(state){
    is(sHGVR){
      when(io.P_button){
        // end current time counter, switch cntMode to 2
        cntDone := true.B
        cntMode := 2.U
      }.otherwise{
        cntMode := 1.U
      }
      io.H_traffic := Green
      io.V_traffic := Red
      io.P_traffic := Red
    }
    is(sHYVR){
      when(io.P_button){
        // end current time counter, switch cntMode to 2
        cntDone := true.B
        cntMode := 2.U
      }.otherwise{
        cntMode := 0.U
      }
      io.H_traffic := Yellow
      io.V_traffic := Red
      io.P_traffic := Red
    }
    is(sHRVG){
      when(io.P_button){
        // end current time counter, switch cntMode to 2
        cntDone := true.B
        cntMode := 2.U
      }.otherwise{
        cntMode := 1.U
      }
      io.H_traffic := Red
      io.V_traffic := Green
      io.P_traffic := Red
    }
    is(sHRVY){
      when(io.P_button){
        // end current time counter
        cntDone := true.B
      }
      cntMode := 2.U
      io.H_traffic := Red
      io.V_traffic := Yellow
      io.P_traffic := Red
    }
    is(sPG){
      when(buttonTrigger){
        // hold previous state's cntMode
        switch(prevState){
          is(sHGVR){
            cntMode := 0.U
          }
          is(sHYVR){
            cntMode := 1.U
          }
          is(sHRVG){
            cntMode := 0.U
          }
          is(sHRVY){
            cntMode := 1.U
          }
        }
      }.otherwise{
        cntMode := 0.U
      }
      io.H_traffic := Red
      io.V_traffic := Red
      io.P_traffic := Green
    }
  }
  
  io.timer := cntReg
}
```
### Waveform
> 請依照Lab文件中的說明截圖，並說明之。(文件中搜尋:截圖)

| state | sIdle | sHGVR | sHYVR | sHRVG | sHRVY | sPG |
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:---:|
|  map  |  000  |  001  |  010  |  011  |  100  | 101 |

| traffic | Off | Red | Yellow | Green |
|:-------:|:---:|:---:|:------:|:-----:|
|   map   | 00  | 01  |   10   |  11   |

![](https://course.playlab.tw/md/uploads/c8f5abb0-0c99-4a61-9c57-0e96c809e913.png)
![](https://course.playlab.tw/md/uploads/8c20d670-75e6-4a0b-8e68-d84277bf5744.png)

## Hw5-2-1 Negative Integer Generator
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `NegIntGenTest.scala` test
package acal_lab05.Hw2

import chisel3._
import chisel3.util._

class NegIntGen extends Module{
    val io = IO(new Bundle{
        val key_in = Input(UInt(4.W))
        val value = Output(Valid(UInt(32.W)))
    })

    //please implement your code below
    val equal = WireDefault(false.B)
    equal := io.key_in === 15.U

    val sIdle :: sAccept :: sEqual :: Nil = Enum(3)
    val state = RegInit(sIdle)

    //Next State Decoder
    switch(state){
        is(sIdle){
            state := sAccept
        }
        is(sAccept){
            when(equal){
                state := sEqual
            }
        }
        is(sEqual){
            state := sAccept
        }
    }

    val in_buffer = RegNext(io.key_in)

    val number = RegInit(0.U(32.W))
    when(state === sAccept){
        when(in_buffer <= 9.U){
            // for number input
            number := (number<<3.U) + (number<<1.U) + in_buffer
        }.elsewhen(in_buffer === 14.U){
            // negative number start with "(-", end with ")"
            // set number to its 2's complement when get input ")"
            number := ~number + 1.U
        }
    }.elsewhen(state === sEqual){
        number := 0.U
    }.otherwise{
        number := number
    }

    io.value.valid := Mux(state === sEqual,true.B,false.B)
    io.value.bits := number
}
```
### Test Result
> 請放上你通過test的結果，驗證程式碼的正確性。大致說明值得關注的波形圖位置。

![](https://course.playlab.tw/md/uploads/7be7a9be-d29e-4742-8ac6-62cc7710539c.png)

io.value.bits (number) 會在 in_buffer 等於 ")" (E) 時將 number 轉換為負數

![](https://course.playlab.tw/md/uploads/be5ff61f-ac3a-4b93-ba8b-4542bdf71394.png)

## Hw5-2-2 N operands N-1 operators(+、-)
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `LongCalTest.scala` test
package acal_lab05.Hw2

import chisel3._
import chisel3.util._

class LongCal extends Module{
    val io = IO(new Bundle{
        val key_in = Input(UInt(4.W))
        val value = Output(Valid(UInt(32.W)))
    })

    //please implement your code below
    //Wire Declaration===================================
    val operator = WireDefault(false.B)
    val num = WireDefault(false.B)
    val equal = WireDefault(false.B)
    // check "(", ")" input
    val parentheses = WireDefault(false.B)
    val out = Wire(UInt(32.W))
    operator := io.key_in >= 10.U && io.key_in <= 11.U
    num := io.key_in < 10.U
    equal := io.key_in === 15.U
    parentheses := io.key_in >= 13.U && io.key_in <= 14.U

    //Reg Declaration====================================
    val in_buffer = RegNext(io.key_in)
    val src1 = RegInit(0.U(32.W))
    val op = RegInit(0.U(1.W))
    val src2 = RegInit(0.U(32.W))
    
    //State and Constant Declaration=====================
    val sIdle :: sSrc1 :: sOp :: sSrc2 :: sEqual :: Nil = Enum(5)
    val add = 0.U
    val sub = 1.U
    
    //Next State Decoder
    val state = RegInit(sIdle)
    switch(state){
        is(sIdle){
            state := sSrc1
        }
        is(sSrc1){
            when(operator && in_buffer =/= 13.U){
                // switch to sOP state only if input before operator input is not a "("
                state := sOp
            }.elsewhen(equal){
                state := sEqual
            }
        }
        is(sOp){
            when(num || parentheses){
                state := sSrc2
            }
        }
        is(sSrc2){
            when(equal){
                state := sEqual
            }.elsewhen(operator && in_buffer =/= 13.U){
                // switch to sOP state only if input before operator input is not a "("
                state := sOp
            }
        }
        is(sEqual){
            state := sSrc1
        }
    }
    //==================================================

    when(state === sSrc1){
        when(in_buffer <= 9.U){
            // for number input
            src1 := (src1<<3.U) + (src1<<1.U) + in_buffer
        }.elsewhen(in_buffer === 14.U){
            // for negative number
            src1 := ~src1 + 1.U
        }
    }
    when(state === sSrc2){
        when(in_buffer <= 9.U){
            // for number input
            src2 := (src2<<3.U) + (src2<<1.U) + in_buffer
        }.elsewhen(in_buffer === 14.U){
            // for negative number
            src2 := ~src2 + 1.U
        }
    }
    when(state === sOp){
        // put temp result in src1, set src2 to 0.U for next input number
        src1 := out
        src2 := 0.U
        op := in_buffer - 10.U
    }
    when(state === sEqual){
        src1 := 0.U
        src2 := 0.U
        op := 0.U
    }

    io.value.valid := Mux(state === sEqual,true.B,false.B)
    // compute output
    out := MuxLookup(op,0.U,Seq(
        add -> (src1 + src2),
        sub -> (src1 - src2),
    ))
    io.value.bits := out
}
```

### Test Result
> 請放上你通過test的結果，驗證程式碼的正確性。(螢幕截圖即可)

![](https://course.playlab.tw/md/uploads/31d16b61-3e81-4939-8554-007cdd0150dc.png)

## Hw5-2-3 Order of Operation (+、-、*、(、))
- **如果你有完成Bonus部分，請在此註明。**
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `CpxCalTest.scala` test
package acal_lab05.Hw2

import chisel3._
import chisel3.util._

class CALstack(val depth: Int) extends Module {
    val io = IO(new Bundle {
        val push        = Input(Bool())
        val reset       = Input(Bool())
        val en          = Input(Bool())
        // whether input contains num
        val numIn       = Input(Bool())
        // 0 for num, 1, 2 for op
        val dataIn      = Input(Vec(3, UInt(32.W)))
        // count of valid dataIn
        val dataInCount = Input(UInt(2.W))
        val dataOut     = Output(UInt(32.W))
    })

    val stack_mem = Mem(depth, UInt(32.W))
    val sp        = RegInit(0.U(log2Ceil(depth+1).W))
    val out       = WireDefault(0.U(32.W))

    val add       = 10.U
    val sub       = 11.U
    val mul       = 12.U

    when(sp > 0.U){
        io.dataOut := stack_mem(sp - 1.U)
    }.otherwise{
        io.dataOut := stack_mem(0.U)
    }

    when (io.en) {
        when(io.push) {
            switch(io.dataInCount){
                is(1.U){
                    when(io.numIn){
                        // push 1 num to stack
                        // update stack, out
                        stack_mem(sp) := io.dataIn(0.U)
                        sp := sp + 1.U
                        io.dataOut := io.dataIn(0.U)
                    }.otherwise{
                        // compute stack_mem(sp - 1.U), stack_mem(sp - 2.U) with op io.dataIn(1.U)
                        out := MuxLookup(io.dataIn(1.U),0.U,Seq(
                            add -> (stack_mem(sp - 1.U) + stack_mem(sp - 2.U)),
                            sub -> (stack_mem(sp - 2.U) - stack_mem(sp - 1.U)),
                            mul -> (stack_mem(sp - 1.U) * stack_mem(sp - 2.U))
                        ))
                        // update stack, out
                        stack_mem(sp - 2.U) := out
                        sp := sp - 1.U
                        io.dataOut := out
                    }
                }
                is(2.U){
                    when(io.numIn){
                        // compute stack_mem(sp - 1.U), io.dataIn(0.U) with op io.dataIn(1.U)
                        out := MuxLookup(io.dataIn(1.U),0.U,Seq(
                            add -> (stack_mem(sp - 1.U) + io.dataIn(0.U)),
                            sub -> (stack_mem(sp - 1.U) - io.dataIn(0.U)),
                            mul -> (stack_mem(sp - 1.U) * io.dataIn(0.U))
                        ))
                        // update stack, out
                        stack_mem(sp - 1.U) := out
                        io.dataOut := out
                    }.otherwise{
                        // compute stack_mem(sp - 1.U), stack_mem(sp - 2.U) with op io.dataIn(1.U)
                        // then compute previous result, stack_mem(sp - 3.U) with op io.dataIn(2.U)
                        when(io.dataIn(1.U) === add && io.dataIn(2.U) === sub){
                            out := stack_mem(sp - 3.U) - (stack_mem(sp - 1.U) + stack_mem(sp - 2.U))
                        }.elsewhen(io.dataIn(1.U) === add && io.dataIn(2.U) === mul){
                            out := (stack_mem(sp - 1.U) + stack_mem(sp - 2.U)) * stack_mem(sp - 3.U)
                        }.elsewhen(io.dataIn(1.U) === sub && io.dataIn(2.U) === add){
                            out := stack_mem(sp - 2.U) - stack_mem(sp - 1.U) + stack_mem(sp - 3.U)
                        }.elsewhen(io.dataIn(1.U) === sub && io.dataIn(2.U) === mul){
                            out := (stack_mem(sp - 2.U) - stack_mem(sp - 1.U)) * stack_mem(sp - 3.U)
                        }.elsewhen(io.dataIn(1.U) === mul && io.dataIn(2.U) === add){
                            out := stack_mem(sp - 1.U) * stack_mem(sp - 2.U) + stack_mem(sp - 3.U)
                        }.elsewhen(io.dataIn(1.U) === mul && io.dataIn(2.U) === sub){
                            out := stack_mem(sp - 3.U) - (stack_mem(sp - 1.U) * stack_mem(sp - 2.U))
                        }
                        // update stack, out
                        stack_mem(sp - 3.U) := out
                        sp := sp - 2.U
                        io.dataOut := out
                    }
                }
                is(3.U){
                    // compute stack_mem(sp - 1.U), io.dataIn(0.U) with op io.dataIn(1.U)
                    // then compute previous result, stack_mem(sp - 2.U) with op io.dataIn(2.U)
                    when(io.dataIn(1.U) === add && io.dataIn(2.U) === sub){
                        out := stack_mem(sp - 2.U) - (stack_mem(sp - 1.U) + io.dataIn(0.U))
                    }.elsewhen(io.dataIn(1.U) === add && io.dataIn(2.U) === mul){
                        out := (stack_mem(sp - 1.U) + io.dataIn(0.U)) * stack_mem(sp - 2.U)
                    }.elsewhen(io.dataIn(1.U) === sub && io.dataIn(2.U) === add){
                        out := stack_mem(sp - 1.U) - io.dataIn(0.U) + stack_mem(sp - 2.U)
                    }.elsewhen(io.dataIn(1.U) === sub && io.dataIn(2.U) === mul){
                        out := (stack_mem(sp - 1.U) - io.dataIn(0.U)) * stack_mem(sp - 2.U)
                    }.elsewhen(io.dataIn(1.U) === mul && io.dataIn(2.U) === add){
                        out := stack_mem(sp - 1.U) * io.dataIn(0.U) + stack_mem(sp - 2.U)
                    }.elsewhen(io.dataIn(1.U) === mul && io.dataIn(2.U) === sub){
                        out := stack_mem(sp - 2.U) - (stack_mem(sp - 1.U) * io.dataIn(0.U))
                    }
                    // update stack, out
                    stack_mem(sp - 2.U) := out
                    sp := sp - 1.U
                    io.dataOut := out
                }
            }
        }
    }
    when (io.reset) {
        // reset sp to 0
        sp := 0.U
    }
}

class OPstack(val depth: Int) extends Module {
    val io = IO(new Bundle {
        val push         = Input(Bool())
        val pop          = Input(Bool())
        val en           = Input(Bool())
        val dataIn       = Input(UInt(4.W))
        // store op poped from stack, at most 2
        val dataOut      = Output(Vec(2, UInt(4.W)))
        // count for valid dataOut
        val dataOutCount = Output(UInt(2.W))
    })

    val stack_mem = Mem(depth, UInt(4.W))
    val sp        = RegInit(0.U(log2Ceil(depth+1).W))
    val pos       = WireDefault(0.U(log2Ceil(depth+1).W))

    val add       = 10.U
    val sub       = 11.U
    val mul       = 12.U
    val leftp     = 13.U
    val rightp    = 14.U

    for (i <- 0 until 2){
        io.dataOut(i.U) := 0.U
    }
    io.dataOutCount := 0.U

    when (io.en) {
        when(io.push) {
            when(io.dataIn === add || io.dataIn === sub) {
                // op +-
                when(sp === 0.U || (sp > 0.U && stack_mem(sp - 1.U) === leftp)){
                    // push only
                    stack_mem(sp) := io.dataIn
                    sp := sp + 1.U
                }.elsewhen((sp > 1.U && stack_mem(sp - 2.U) === leftp) || sp === 1.U){
                    // pop stack head, push io.dataIn
                    stack_mem(sp - 1.U) := io.dataIn
                    io.dataOut(0.U) := stack_mem(sp - 1.U)
                    io.dataOutCount := 1.U
                }.otherwise{
                    // pop last 2 op, push io.dataIn
                    stack_mem(sp - 2.U) := io.dataIn
                    sp := sp - 1.U
                    io.dataOut(0.U) := stack_mem(sp - 1.U)
                    io.dataOut(1.U) := stack_mem(sp - 2.U)
                    io.dataOutCount := 2.U
                }
            }.elsewhen(io.dataIn === mul){
                // op *
                when(sp > 0.U && stack_mem(sp - 1.U) === mul){
                    // pop and push op * to stack
                    io.dataOut(0.U)   := mul
                    io.dataOutCount := 1.U
                }.otherwise{
                    // push op *
                    stack_mem(sp) := io.dataIn
                    sp := sp + 1.U
                }
            }.elsewhen(io.dataIn === leftp){
                // symbol (, push only
                stack_mem(sp) := io.dataIn
                sp := sp + 1.U
            }.elsewhen(io.dataIn === rightp){
                // symbol ), find last symbol (
                // pop op after last symbol (
                when(stack_mem(sp - 1.U) === leftp){
                    sp := sp - 1.U
                }.elsewhen(stack_mem(sp - 2.U) === leftp){
                    sp := sp - 2.U
                    io.dataOut(0.U) := stack_mem(sp - 1.U)
                    io.dataOutCount := 1.U
                }.elsewhen(stack_mem(sp - 3.U) === leftp){
                    sp := sp - 3.U
                    io.dataOut(0.U) := stack_mem(sp - 1.U)
                    io.dataOut(1.U) := stack_mem(sp - 2.U)
                    io.dataOutCount := 2.U
                }
            }
        } .elsewhen(io.pop) {
            // reset stack
            sp := 0.U
            // pop all op from stack, triggered when =
            switch(sp){
                is(1.U){
                    io.dataOut(0.U) := stack_mem(0.U)
                }
                is(2.U){
                    io.dataOut(0.U) := stack_mem(1.U)
                    io.dataOut(1.U) := stack_mem(0.U)
                }
            }
            io.dataOutCount := sp
        }
    }
}

class CpxCal extends Module{
    val io = IO(new Bundle{
        val key_in = Input(UInt(4.W))
        val value = Output(Valid(UInt(32.W)))
    })

    //please implement your code below
    //Wire Declaration===================================
    val operator = WireDefault(false.B)
    val num = WireDefault(false.B)
    val equal = WireDefault(false.B)
    // check "(" input
    val leftP = WireDefault(false.B)
    // check ")" input
    val rightP = WireDefault(false.B)
    
    operator := io.key_in >= 10.U && io.key_in <= 12.U
    num := io.key_in < 10.U
    equal := io.key_in === 15.U
    leftP := io.key_in === 13.U
    rightP := io.key_in === 14.U

    //Reg Declaration====================================
    val in_buffer = RegNext(io.key_in)
    val number = RegInit(0.U(32.W))
    // check input number is negative
    val neg_num = RegInit(false.B)
    // whether need to push number to cal_stack
    val push_number = RegInit(false.B)

    // OP stack
    val op_stack = Module(new OPstack(depth = 8))
    op_stack.io.push   := false.B
    op_stack.io.pop    := false.B
    op_stack.io.en     := false.B
    op_stack.io.dataIn := 0.U

    // Cal stack
    val cal_stack = Module(new CALstack(depth = 8))
    cal_stack.io.push   := false.B
    cal_stack.io.reset  := false.B
    cal_stack.io.en     := false.B
    cal_stack.io.numIn  := false.B
    // default set dataIn(0) to number, dataIn(1), dataIn(2), to op_stack dataOut(0), dataOut(1)
    cal_stack.io.dataIn(0.U) := number
    cal_stack.io.dataIn(1.U) := op_stack.io.dataOut(0.U)
    cal_stack.io.dataIn(2.U) := op_stack.io.dataOut(1.U)
    cal_stack.io.dataInCount := 0.U
    
    //State and Constant Declaration=====================
    // sLeft for state (
    // sRight for state )
    val sIdle :: sNum :: sOp :: sLeft :: sRight :: sEqual :: Nil = Enum(6)

    //Next State Decoder
    val state = RegInit(sIdle)
    switch(state){
        is(sIdle){
            when(num){
                state := sNum
            }.elsewhen(leftP){
                state := sLeft
            }
        }
        is(sNum){
            // when switch state from sNum, set push_number to true to push number to cal_stack
            when(operator){
                state := sOp
                push_number := true.B
            }.elsewhen(equal){
                state := sEqual
                push_number := true.B
            }.elsewhen(rightP){
                state := sRight
                push_number := true.B
            }
        }
        is(sOp){
            when(num){
                state := sNum
            }.elsewhen(leftP){
                state := sLeft
            }
        }
        is(sLeft){
            when(!leftP){
                state := sNum
            }
        }
        is(sRight){
            when(equal){
                state := sEqual
            }.elsewhen(operator){
                state := sOp
            }
        }
        is(sEqual){
            state := sIdle
        }
    }

    when(state === sNum){
        when(in_buffer <= 9.U){
            // for number input
            push_number := true.B
            number := (number<<3.U) + (number<<1.U) + in_buffer
        }.elsewhen(in_buffer === 11.U){
            // input number is negative
            neg_num := true.B
        }
    }
    when(state === sLeft){
        // push symbol ( to op_stack
        op_stack.io.push := true.B
        op_stack.io.en := true.B
        op_stack.io.dataIn := in_buffer
    }
    when(state === sOp || state === sRight || state === sEqual){
        op_stack.io.en := true.B
        // push number, output op to cal_stack
        when(push_number || op_stack.io.dataOutCount > 0.U){
            cal_stack.io.push := true.B
            cal_stack.io.en := true.B
        }
        
        when(state === sOp || state === sRight){
            // push op, symbol ) to op_stack
            op_stack.io.push := true.B
            op_stack.io.dataIn := in_buffer
        }.elsewhen(state === sEqual){
            // pop rest op from op_stack
            op_stack.io.pop := true.B
            // reset cal_stack
            cal_stack.io.reset := true.B
        }
        
        when(push_number){
            // push number, op
            when(neg_num){
                cal_stack.io.dataIn(0.U) := ~number + 1.U
                neg_num := false.B
            }
            cal_stack.io.numIn := true.B
            cal_stack.io.dataInCount := op_stack.io.dataOutCount + 1.U

            number := 0.U
            push_number := false.B
        }.otherwise{
            // push op only
            cal_stack.io.dataInCount := op_stack.io.dataOutCount
        }
    }

    io.value.valid := Mux(state === sEqual,true.B,false.B)
    io.value.bits := cal_stack.io.dataOut
}
```
### Test Result
> 請放上你通過test的結果，驗證程式碼的正確性。(螢幕截圖即可)

![](https://course.playlab.tw/md/uploads/f32956eb-84dd-459b-9b8b-d65be1327df2.png)

## Hw5-3-1 Pseudo Random Number Generator
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `PRNGTest.scala` test
package acal_lab05.Hw3

import chisel3._
import chisel3.util._

class PRNG(seed:Int) extends Module{
    val io = IO(new Bundle{
        val gen = Input(Bool())
        val puzzle = Output(Vec(4,UInt(4.W)))
        val ready = Output(Bool())
    })

    io.puzzle := VecInit(Seq.fill(4)(0.U(4.W)))
    io.ready := false.B

    // register to store the output, init to seed
    val shiftReg = RegInit(VecInit(seed.U(16.W).asBools))
    val posReg = RegInit(0.U(2.W))
    val checkDup = WireDefault(false.B)
    // cycle counter, increase in each cycle
    val cycleReg = RegInit(0.U(4.W))
    cycleReg := (cycleReg + 1.U) % 4.U

    val sIdle :: sShift :: sCheck :: sSingleShift :: sOut :: Nil = Enum(5)
    val state = RegInit(sIdle)

    switch(state){
        is(sIdle){
            // switch to sShift when io.gen
            when(io.gen){
                state := sShift
            }
        }
        is(sShift){
            // shift the shiftReg by 1
            (shiftReg.zipWithIndex).map{
                case(sr, i) => sr := shiftReg((i + 1) % 16)
            }
            // xor
            shiftReg(15) := shiftReg(0) ^ shiftReg(2) ^ shiftReg(3) ^ shiftReg(5)

            // switch to sCheck to handle number > 9 and duplicate number
            state := sCheck
        }
        is(sCheck){
            // check if number > 9 or duplicate number
            // get current number
            val cur_number = Cat(
                shiftReg(4.U * posReg + 3.U),
                shiftReg(4.U * posReg + 2.U),
                shiftReg(4.U * posReg + 1.U),
                shiftReg(4.U * posReg)
            )
            // check duplicate number
            for(i <- 0 until 4){
                when(i.U < posReg){
                    val number = Cat(shiftReg.slice(4 * i, 4 * (i + 1)).reverse)
                    when(number === cur_number){
                        checkDup := true.B
                    }
                }
            }
            // switch to sSingleShift to shift single number
            when(checkDup || cur_number > 9.U){
                state := sSingleShift
            }.otherwise{
                // posReg record current number position
                // check next number
                // switch to sOut when all number check finished
                posReg := (posReg + 1.U) % 4.U
                when(posReg === 3.U){
                    state := sOut
                }
            }
        }
        is(sSingleShift){
            // get current number
            val cur_number = Cat(
                shiftReg(4.U * posReg + 3.U),
                shiftReg(4.U * posReg + 2.U),
                shiftReg(4.U * posReg + 1.U),
                shiftReg(4.U * posReg)
            )
            // if current number equal 0, set it to cycleReg
            // otherwise shift the number using LFSR in lab5-3-1
            when(cur_number === 0.U){
                shiftReg(4.U * posReg) := cycleReg(0)
                shiftReg(4.U * posReg + 1.U) := cycleReg(1)
                shiftReg(4.U * posReg + 2.U) := cycleReg(2)
                shiftReg(4.U * posReg + 3.U) := cycleReg(3)
            }.otherwise{
                shiftReg(4.U * posReg) := shiftReg(4.U * posReg + 1.U)
                shiftReg(4.U * posReg + 1.U) := shiftReg(4.U * posReg + 2.U)
                shiftReg(4.U * posReg + 2.U) := shiftReg(4.U * posReg + 3.U)
                shiftReg(4.U * posReg + 3.U) := shiftReg(4.U * posReg) ^ shiftReg(4.U * posReg + 1.U)
            }

            // switch to sCheck to check again
            state := sCheck
        }
        is(sOut){
            // set io.puzzle, io.ready for output
            io.puzzle := VecInit(Seq.tabulate(4)(i => {
                Cat(shiftReg.slice(i*4, (i+1)*4).reverse)
            }))
            io.ready := true.B

            // switch back to sIdle for next gen
            state := sIdle
        }
    }
}
```
### Test Result
> 請放上你通過test的結果，驗證程式碼的正確性。(螢幕截圖即可)

![](https://course.playlab.tw/md/uploads/87d7fd95-f84d-42b7-926a-d5e86dd69d1c.png)

## Hw5-3-2 1A2B game quiz
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `NumGuessTest.scala` test
package acal_lab05.Hw3

import chisel3._
import chisel3.util._

class NumGuess(seed:Int = 1) extends Module{
    require (seed > 0 , "Seed cannot be 0")

    val io  = IO(new Bundle{
        val gen = Input(Bool())
        val guess = Input(UInt(16.W))
        val puzzle = Output(Vec(4,UInt(4.W)))
        val ready  = Output(Bool())
        val g_valid  = Output(Bool())
        val A      = Output(UInt(3.W))
        val B      = Output(UInt(3.W))

        //don't care at Hw6-3-2 but should be considered at Bonus
        val s_valid = Input(Bool())
    })

    // golden data register
    val goldReg = RegInit(VecInit(Seq.fill(4)(0.U(4.W))))
    // input register
    val inReg   = RegInit(VecInit(Seq.fill(4)(0.U(4.W))))
    val AReg = RegInit(0.U(3.W))
    val BReg = RegInit(0.U(3.W))
    val posReg = RegInit(0.U(2.W))

    io.puzzle := VecInit(Seq.fill(4)(0.U(4.W)))
    io.ready  := false.B
    io.g_valid  := false.B
    io.A      := AReg
    io.B      := BReg
    
    val sIdle :: sGen :: sInput :: sJudge :: sOut :: Nil = Enum(5)
    val state = RegInit(sIdle)

    // PRNG from HW5-3-1
    val prng = Module(new PRNG(seed = seed))
    prng.io.gen := false.B

    switch(state){
        is(sIdle){
            // let prng gen random number when io.gen
            // switch to sGen
            when(io.gen){
                prng.io.gen := true.B
                state := sGen
            }
        }
        is(sGen){
            // wait for prng.io.ready
            // set io.puzzle to prng.io.puzzle for testbench debug
            // set io.ready to true to get testbench input
            // record golden data
            // switch to sInput
            when(prng.io.ready){
                io.puzzle := prng.io.puzzle
                io.ready := true.B
                goldReg := prng.io.puzzle
                state := sInput
            }
        }
        is(sInput){
            // get testbench input
            for(i <- 0 until 4){
                val number = Cat(io.guess(4 * i + 3, 4 * i))
                inReg(i) := number
            }
            // reset AReg, BReg to 0
            // switch to sJudge
            AReg := 0.U
            BReg := 0.U
            state := sJudge
        }
        is(sJudge){
            // check 1 number in 1 cycle
            // posReg record current number position
            // switch to sOut when all number compare finished
            when(posReg === 3.U){
                state := sOut
            }
            posReg := (posReg + 1.U) % 4.U

            // compare each number in golden data and testbench input
            // A
            when(inReg(posReg) === goldReg(posReg)){
                AReg := AReg + 1.U
            }.otherwise{
                // B
                for(i <- 0 until 4){
                    when(inReg(posReg) === goldReg(i.U)){
                        BReg := BReg + 1.U
                    }
                }
            }
        }
        is(sOut){
            // set io.g_valid to true to let testbench get output and guess
            io.g_valid := true.B
            
            // switch to sIdle when testbench guess the correct answer
            // otherwise switch to sInput for testbench next input
            when(AReg === 4.U){
                state := sIdle
            }.otherwise{
                state := sInput
            }
        }
    }
}
```
### Test Result
> 請放上你通過test的結果，驗證程式碼的正確性。(螢幕截圖即可)

![](https://course.playlab.tw/md/uploads/520f7dcc-b5dc-4b35-897e-1b8edf935a56.png)

## 文件中的問答題
- Q1:Hw5-2-2(長算式)以及Lab5-2-2(短算式)，需要的暫存器數量是否有差別？如果有，是差在哪裡呢？
    - Ans1: 我的 Hw5-2-2 暫存器數量與 Lab5-2-2 相同，src1 負責接第一個數字、暫存計算結果，src2 負責接第二個~最後一個數字
- Q2:你是如何處理**Hw5-2-3**有提到的關於**編碼衝突**的問題呢?
    - Ans2: 透過不同 state 判斷是 operator 還是 number，並且將 CALstack 的 operator dataIn 與 number dataIn 的 port 區分、以 dataInCount、numIn 判斷 input 是 operator / number
- Q3:你是如何處理**Hw5-3-1**1A2B題目產生時**數字重複**的問題呢?
    - Ans3: 在遇到數字大於 9 或數字重複時，我會將單個數字做 Fibonacci version 的 linear feedback shift，如果要進行 shift 時數字為 0，我會將數字初始化為 cycle counter (一個 4 bits register，在每個 cycle increase 1，增加 randomness)
