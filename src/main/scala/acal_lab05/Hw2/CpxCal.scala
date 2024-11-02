package acal_lab05.Hw2

import chisel3._
import chisel3.util._

class Token(val valueWidth: Int) extends Bundle {
  val isOperator = Bool()  // Flag to indicate if it's an operator
  val value = UInt(valueWidth.W)   // Can hold either the number or the operator encoding
}

class CpxCal extends Module{
    val io = IO(new Bundle{
        val key_in = Input(UInt(4.W))
        val value = Output(Valid(UInt(32.W)))
        val stackDebug = Output(UInt(32.W))
        val calInputDebug = Output(UInt(32.W))
    })

    io.value.bits := 0.U
    io.value.valid := false.B
    // longReg
    val regVec = Module(new LongReg())
    regVec.io.key_in := io.key_in
    regVec.io.push := false.B
    regVec.io.init := false.B
    val len_reg = RegInit(0.U(8.W))
    val ptr_reg = RegInit(0.U(8.W))
    val lastPtr_reg = RegNext(ptr_reg)
    val token = WireDefault(0.U(4.W))
    val lastToken_reg = RegNext(token)
    regVec.io.readPtr := ptr_reg
    token := regVec.io.output
    // StackInit
    val dataInInit = Wire(new Token(32))
    dataInInit.isOperator := false.B
    dataInInit.value := 0.U
    val infixStack = Module(new OneClockStack(UInt(32.W), 32))
    infixStack.io.en := true.B
    infixStack.io.push := false.B
    infixStack.io.pop := false.B
    infixStack.io.dataIn := 0.U
    val calStack = Module(new CalStack(32))
    calStack.io.en := true.B
    calStack.io.push := false.B
    calStack.io.cal := false.B
    calStack.io.dataIn := 0.U
    // queue
    val queue = Module(new Queue(new Token(32), 32))  // 8-bit wide, 4 entries deep
    val pushQueue = WireDefault(false.B)
    val deqQueue = WireDefault(false.B)
    queue.io.enq.bits := dataInInit
    queue.io.enq.valid := pushQueue
    queue.io.deq.ready := deqQueue
    // sStore
    val srcInit = Wire(new Token(32))
    srcInit.isOperator := false.B
    srcInit.value := 0.U
    val src_reg = RegInit(srcInit)
    val pending_reg = RegInit(false.B)
    val pendingOpInit = Wire(new Token(32))
    pendingOpInit.isOperator := true.B
    pendingOpInit.value := 0.U
    val pendingOp_reg = RegInit(pendingOpInit)
    val numberReady_reg = RegInit(false.B)
    val opToken = Wire(new Token(32))
    opToken.isOperator := true.B
    opToken.value := 0.U
    val leftBrace = Wire(new Token(32))
    leftBrace.isOperator := true.B
    leftBrace.value := 13.U
    val rightBrace = Wire(new Token(32))
    rightBrace.isOperator := true.B
    rightBrace.value := 14.U
    val operator = WireDefault(false.B)
    operator := token >= 10.U && token <= 12.U
    when(token === 11.U){
        when(lastToken_reg === 13.U){
            operator := false.B
        }
    }
    val negative_reg = RegInit(false.B)
    when (!negative_reg && lastToken_reg === 13.U && token === 11.U) {
        negative_reg := true.B
    }
    val sWait :: sSrc :: sOp :: sEqual :: sWaitCal :: Nil = Enum(5)
    val state = RegInit(sWait)
    val doneStackPop = RegInit(false.B)
    val doneCal = WireDefault(false.B)
    // State

    switch(state){
        is(sWait){
            regVec.io.push := true.B
            len_reg := len_reg + 1.U
            when(io.key_in === 15.U) {state := sSrc}
        }
        is(sSrc){
            when(operator) {state := sOp}
            when(token === 15.U) {state := sEqual}
        }
        is(sOp){
            when(token < 10.U || token === 13.U) {state := sSrc}
            when(token === 15.U) {state := sEqual}
        }
        is(sEqual){
            state := sWaitCal
        }
        is(sWaitCal){
            when(doneStackPop){
                state := sWait
                // doneStackPop := false.B
            }
        }
    }

    when(state === sSrc){
        when(token < 10.U){
            src_reg.value := (src_reg.value<<3.U) + (src_reg.value<<1.U) + token
            ptr_reg := ptr_reg + 1.U
        }
        // (- move forward)
        when(token === 11.U && lastToken_reg === 13.U){
            ptr_reg := ptr_reg + 1.U
        }
        when(token === 14.U){
            when(!numberReady_reg && !pending_reg){
                when(negative_reg){
                    src_reg.value := (-src_reg.value.asSInt).asUInt
                    negative_reg := false.B
                }
                numberReady_reg := true.B
                pending_reg := true.B
                pendingOp_reg.value := 14.U
            }.elsewhen(numberReady_reg && pending_reg){
                numberReady_reg := false.B
            }.elsewhen(!numberReady_reg && pending_reg){
                pending_reg := false.B
            }
        }
    }

    when(state === sOp){
        when(src_reg.value =/= 0.U){
            numberReady_reg := true.B
        }
        when(operator){
            pending_reg := true.B
            pendingOp_reg.value := token
        }
    }
    
    when(state === sEqual){
        opToken.value := 15.U
        queue.io.enq.bits := opToken
        pushQueue := true.B
        ptr_reg := 0.U
        pending_reg := false.B
        src_reg.value := 0.U
        numberReady_reg := false.B
        negative_reg := false.B
        regVec.io.init := true.B
    }

    when(state === sSrc || state === sOp){
        // Stack push logic
        // If pushing number then don't forward ptr Ex:-12)
        when(numberReady_reg && pending_reg){
            queue.io.enq.bits := src_reg
            pushQueue := true.B
            numberReady_reg := false.B
            src_reg.value := 0.U
        }
        // If simply op then forward ptr Ex: 5-8
        .elsewhen(numberReady_reg && !pending_reg) {
            // Push the number
            ptr_reg := ptr_reg + 1.U
            queue.io.enq.bits := src_reg
            pushQueue := true.B
            numberReady_reg := false.B
            src_reg.value := 0.U
        }.elsewhen(!numberReady_reg && pending_reg){
            ptr_reg := ptr_reg + 1.U
            queue.io.enq.bits := pendingOp_reg
            pushQueue := true.B
            pending_reg := false.B
            pendingOp_reg.value := 0.U
        }.elsewhen(token === 13.U) {
            // Push left brace immediately
            ptr_reg := ptr_reg + 1.U
            queue.io.enq.bits := leftBrace
            pushQueue := true.B
        }.otherwise {
            pushQueue := false.B
        }
    }

    io.value.valid := Mux((state === sWaitCal) && doneCal,true.B,false.B)
    io.value.bits := 0.U
    
    io.stackDebug := queue.io.enq.bits.value

    // Infix2Postfix
    val calInput = Wire(new Token(32))
    calInput.isOperator := false.B
    calInput.value := 0.U
    val sNoOperation :: sPendingPush :: sTestStackTop :: sRightBrace :: sPopAllStack :: Nil = Enum(5)
    val stackOp_reg = RegInit(sNoOperation)
    val queueData = queue.io.deq.bits
    val stackData = infixStack.io.dataOut
    val infixOperator_reg = RegInit(0.U(32.W))
    // val calDigit = Wire(new Token(32))
    // calDigit.isOperator := false.B
    // calDigit.value := 0.U
    // val calOperator = Wire(new Token(32))
    // calOperator.isOperator := true.B
    // calOperator.value := 0.U
    when(queue.io.deq.valid || stackOp_reg =/= sNoOperation){
        // If current don't have stack operation
        // If incoming is '+', and stack top is '*', then save '+'
        when(stackOp_reg === sNoOperation){
            // Read queue data
            when(!queueData.isOperator){
                calInput := queueData
            }.elsewhen(queueData.value === 10.U || queueData.value === 11.U){
                // higher precedence, pop it and push, and then test with new top
                when(stackData === 12.U){
                    infixStack.io.pop := true.B
                    calInput.value := stackData
                    calInput.isOperator := true.B
                    infixOperator_reg := queueData.value
                    stackOp_reg := sTestStackTop
                }
                // same precedence, pop stack and push current operator
                .elsewhen(stackData === 10.U || stackData === 11.U){
                    infixStack.io.pop := true.B
                    calInput.value := stackData
                    calInput.isOperator := true.B
                    infixOperator_reg := queueData.value
                    stackOp_reg := sPendingPush
                }.otherwise{ // '('
                    infixStack.io.push := true.B
                    infixStack.io.dataIn := queueData.value
                }
            }.elsewhen(queueData.value === 12.U){
                // If stack top is '*'
                when(stackData === 12.U){
                    infixStack.io.pop := true.B
                    calInput.value := stackData
                    calInput.isOperator := true.B
                    infixOperator_reg := queueData.value
                    stackOp_reg := sPendingPush
                }
                // '+-('
                .otherwise{
                    infixStack.io.push := true.B
                    infixStack.io.dataIn := queueData.value
                }
            }.elsewhen(queueData.value === 13.U){
                infixStack.io.push := true.B
                infixStack.io.dataIn := queueData.value
            }.elsewhen(queueData.value === 14.U){
                stackOp_reg := sRightBrace
            }.elsewhen(queueData.value === 15.U){
                stackOp_reg := sPopAllStack
            }
            deqQueue := true.B
        }
        switch(stackOp_reg){
            is(sPendingPush){
                infixStack.io.push := true.B
                infixStack.io.dataIn := infixOperator_reg
                infixOperator_reg := 0.U
                stackOp_reg := sNoOperation
            }
            is(sTestStackTop){
                when(stackData === 12.U){
                    infixStack.io.pop := true.B
                    calInput.value := stackData
                    calInput.isOperator := true.B
                }
                // same precedence, pop stack and push current operator
                .elsewhen(stackData === 10.U || stackData === 11.U){
                    infixStack.io.pop := true.B
                    calInput.value := stackData
                    calInput.isOperator := true.B
                    stackOp_reg := sPendingPush
                }.elsewhen(stackData === 13.U){ // '(' Just push incoming
                    stackOp_reg := sPendingPush
                }
                // when Test to empty stack
                when(infixStack.io.empty){
                    stackOp_reg := sPendingPush
                }

            }
            is(sRightBrace){
                when(stackData === 13.U){
                    stackOp_reg := sNoOperation
                    infixStack.io.pop := true.B
                }.otherwise{
                    infixStack.io.pop := true.B
                    calInput.value := stackData
                    calInput.isOperator := true.B
                }
            }
            is(sPopAllStack){
                when(infixStack.io.empty){
                    stackOp_reg := sNoOperation
                    doneStackPop := true.B
                    calInput.value := 15.U
                    calInput.isOperator := true.B
                }.otherwise{
                    infixStack.io.pop := true.B
                    calInput.value := stackData
                    calInput.isOperator := true.B
                }
            }
        }
    }

    io.calInputDebug := calInput.value
    
    // val dataOutLast = RegNext(RegNext(calStack.io.dataOutLast))
    // val dataOutSecond = RegNext(RegNext(calStack.io.dataOutSecond))
    val dataOutLast = WireDefault(0.U(32.W))
    val dataOutSecond = WireDefault(0.U(32.W))
    dataOutLast := calStack.io.dataOutLast
    dataOutSecond := calStack.io.dataOutSecond
    when(calInput.value =/= 0.U){
        when(!calInput.isOperator){
            calStack.io.push := true.B
            calStack.io.dataIn := calInput.value
        }.otherwise{
            switch(calInput.value){
                is(10.U){
                    calStack.io.cal := true.B
                    calStack.io.dataIn := (dataOutSecond + dataOutLast)
                }
                is(11.U){
                    calStack.io.cal := true.B
                    calStack.io.dataIn :=  (dataOutSecond - dataOutLast)
                }
                is(12.U){
                    calStack.io.cal := true.B
                    calStack.io.dataIn :=  (dataOutSecond * dataOutLast)
                }
                is(15.U){
                    doneCal := true.B
                    io.value.bits := calStack.io.dataOutLast
                }
            }
        }
    }
}
// Queue dequeue logic
    // val dataOut = queue.io.deq.bits
    // val dataOutValid = queue.io.deq.valid
    // queueEmpty := !queue.io.deq.valid
    // dataOutValid := queue.io.deq.valid
// val stack = Module(new Stack(32))
//     stack.io.en := true.B
//     val pushStack = WireDefault(false.B)
//     stack.io.push := pushStack
//     stack.io.pop := false.B
//     stack.io.dataIn := dataInInit
// val stack = Module(new Stack(32))
    // val pushStack = WireDefault(false.B)
    // stack.io.en := true.B
    // stack.io.push := pushStack
    // stack.io.pop := false.B
    // stack.io.dataIn := dataInInit

// val dataOutLast = WireDefault(0.U(32.W))
//     val dataOutSecond = WireDefault(0.U(32.W))
//     dataOutLast := calStack.io.dataOutLast
//     dataOutSecond := calStack.io.dataOutSecond
//     val saveOp = RegInit(0.U(32.W))
//     val cntReg = RegInit(0.U(4.W))
//     val cntDown = RegInit(false.B)
//     when(cntDown){ cntReg := Mux(cntReg===2.U,0.U,cntReg+(1.U)) }
//     when(calInput.value =/= 0.U){
//         when(!calInput.isOperator){
//             calStack.io.push := true.B
//             calStack.io.dataIn := calInput.value
//             cntReg := 0.U
//             cntDown := true.B
//         }.otherwise{
//             saveOp := calInput.value
//         }
//     }
    
//     when(cntReg =/= 1.U && saveOp =/= 0.U){
//         switch(saveOp){
//             is(10.U){
//                 calStack.io.cal := true.B
//                 calStack.io.dataIn := (dataOutSecond + dataOutLast)
//             }
//             is(11.U){
//                 calStack.io.cal := true.B
//                 calStack.io.dataIn :=  (dataOutSecond - dataOutLast)
//             }
//             is(12.U){
//                 calStack.io.cal := true.B
//                 calStack.io.dataIn :=  (dataOutSecond * dataOutLast)
//             }
//             is(15.U){
//                 doneCal := true.B
//                 io.value.bits := calStack.io.dataOutLast
//             }
//         }
//         cntReg := 0.U
//         cntDown := false.B
//         saveOp := 0.U
//     }