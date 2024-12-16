package acal_lab05.Hw2

import chisel3._
import chisel3.util._

class Token(val valueWidth: Int) extends Bundle {
  val isOperator = Bool()  // Flag to indicate if it's an operator
  val value = UInt(valueWidth.W)   // Can hold either the number or the operator encoding
}

class CpxCal(val bitsWidth: Int) extends Module{
    val io = IO(new Bundle{
        val key_in = Input(UInt(bitsWidth.W))
        val value = Output(Valid(UInt(bitsWidth.W)))
    })

    io.value.bits := 0.U
    io.value.valid := false.B
    
    // longReg
    val regVec = Module(new LongReg(bitsWidth))
    regVec.io.key_in := io.key_in
    regVec.io.push := false.B
    regVec.io.init := false.B
    val ptr_reg = RegInit(0.U(10.W))
    val token = WireDefault(0.U(bitsWidth.W))
    val lastToken_reg = RegNext(token)
    regVec.io.readPtr := ptr_reg
    token := regVec.io.output
    // StackInit
    val dataInInit = Wire(new Token(bitsWidth))
    dataInInit.isOperator := false.B
    dataInInit.value := 0.U
    val infixStack = Module(new OneClockStack(UInt(bitsWidth.W), 128))
    infixStack.io.en := true.B
    infixStack.io.push := false.B
    infixStack.io.pop := false.B
    infixStack.io.dataIn := 0.U
    infixStack.io.init := false.B
    val calStack = Module(new CalStack(bitsWidth, 128))
    calStack.io.en := true.B
    calStack.io.push := false.B
    calStack.io.cal := false.B
    calStack.io.dataIn := 0.U
    calStack.io.init := false.B
    // queue
    val queue = Module(new Queue(new Token(bitsWidth), bitsWidth))  // 8-bit wide, 4 entries deep
    val pushQueue = WireDefault(false.B)
    val deqQueue = WireDefault(false.B)
    queue.io.enq.bits := dataInInit
    queue.io.enq.valid := pushQueue
    queue.io.deq.ready := deqQueue
    // sStore
    val srcInit = Wire(new Token(bitsWidth))
    srcInit.isOperator := false.B
    srcInit.value := 0.U
    val src_reg = RegInit(srcInit)
    val pending_reg = RegInit(false.B)
    val pendingOpInit = Wire(new Token(bitsWidth))
    pendingOpInit.isOperator := true.B
    pendingOpInit.value := 0.U
    val pendingOp_reg = RegInit(pendingOpInit)
    val numberReady_reg = RegInit(false.B)
    val opToken = Wire(new Token(bitsWidth))
    opToken.isOperator := true.B
    opToken.value := 0.U
    val leftBrace = Wire(new Token(bitsWidth))
    leftBrace.isOperator := true.B
    leftBrace.value := 13.U
    val rightBrace = Wire(new Token(bitsWidth))
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
    val inputEnd = WireDefault(false.B)
    val sWait :: sSrc :: sOp :: sEqual :: sWaitCal :: Nil = Enum(5)
    val state = RegInit(sWait)
    
    // Infix2Postfix
    val calInput = Wire(new Token(bitsWidth))
    calInput.isOperator := false.B
    calInput.value := 0.U
    val sNoOperation :: sPendingPush :: sTestStackTop :: sRightBrace :: sPopAllStack :: Nil = Enum(5)
    val stackOp_reg = RegInit(sNoOperation)
    val queueData = queue.io.deq.bits
    val stackData = infixStack.io.dataOut
    val infixOperator_reg = RegInit(0.U(bitsWidth.W))

    // Calculate
    val dataOutLast = WireDefault(0.U(bitsWidth.W))
    val dataOutSecond = WireDefault(0.U(bitsWidth.W))
    dataOutLast := calStack.io.dataOutLast
    dataOutSecond := calStack.io.dataOutSecond
    val doneCal = WireDefault(false.B)

    def resetAllRegs(): Unit = {
        regVec.io.init := true.B
        infixStack.io.init := true.B
        calStack.io.init := true.B
        state := sWait
        ptr_reg := 0.U
        src_reg.value := 0.U
        src_reg.isOperator := 0.U
        pending_reg := false.B
        pendingOp_reg.value := 0.U
        numberReady_reg := false.B
        negative_reg := false.B
        stackOp_reg := sNoOperation
        infixOperator_reg := 0.U
    }
    
    // State
    switch(state){
        is(sWait){
            regVec.io.push := true.B
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
            when(inputEnd){ state := sWaitCal }
        }
        is(sWaitCal){
            when(doneCal){
                state := sWait
                resetAllRegs()
                // doneCal := false.B
            }
        }
    }

    io.value.valid := Mux((state === sWaitCal) && doneCal,true.B,false.B)
    io.value.bits := 0.U
    
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
            // Num determine and so ')' Ex: (-14)
            when(!numberReady_reg && !pending_reg){
                when(negative_reg){
                    src_reg.value := (-src_reg.value.asSInt).asUInt
                    negative_reg := false.B
                }
                numberReady_reg := true.B
                pending_reg := true.B
                pendingOp_reg.value := 14.U
            }
            // Num have pushed, Next push op
            .elsewhen(numberReady_reg && pending_reg){
                numberReady_reg := false.B
            }
            // Op have pushed
            .elsewhen(!numberReady_reg && pending_reg){
                pending_reg := false.B
            }
        }
    }

    when(state === sOp){
        // Src Reday Ex: 50-
        when(src_reg.value =/= 0.U){
            numberReady_reg := true.B
        }
        // Operator also determine, pending push
        when(operator){
            pending_reg := true.B
            pendingOp_reg.value := token
        }
    }
    
    when(state === sEqual){
        when(!numberReady_reg && !pending_reg){
            numberReady_reg := true.B
            pending_reg := true.B
            pendingOp_reg.value := 15.U
        }.elsewhen(numberReady_reg && pending_reg){
            numberReady_reg := false.B
        }.elsewhen(!numberReady_reg && pending_reg){
            pending_reg := false.B
        }
    }

    // Stack push logic for src and op 
    when(state === sSrc || state === sOp){
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

    // sEqual push queue logic
    when(state === sEqual){
        when(numberReady_reg && pending_reg){
            queue.io.enq.bits := src_reg
            pushQueue := true.B
            numberReady_reg := false.B
            src_reg.value := 0.U
        }
        // If simply op then forward ptr Ex: 5-8
        .elsewhen(numberReady_reg && !pending_reg) {
            // Push the number
            queue.io.enq.bits := src_reg
            pushQueue := true.B
            numberReady_reg := false.B
            src_reg.value := 0.U
        }.elsewhen(!numberReady_reg && pending_reg){
            queue.io.enq.bits := pendingOp_reg
            pushQueue := true.B
            pending_reg := false.B
            pendingOp_reg.value := 0.U
            inputEnd := true.B
        }.otherwise {
            pushQueue := false.B
        }
    }

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

    // Calculate
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