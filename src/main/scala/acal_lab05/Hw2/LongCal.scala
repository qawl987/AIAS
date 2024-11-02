package acal_lab05.Hw2

import chisel3._
import chisel3.util._

class LongCal extends Module{
    val io = IO(new Bundle{
        val key_in = Input(UInt(4.W))
        val value = Output(Valid(UInt(32.W)))
    })

    //please implement your code below
    io.value.valid := false.B
    io.value.bits := 0.U

    //Reg Declaration====================================
    val in_buffer = RegNext(io.key_in)
    val src1 = RegInit(0.U(32.W))
    val op = RegInit(0.U(2.W))
    val src2 = RegInit(0.U(32.W))

    val ans = Wire(UInt(32.W))
    ans := 0.U
    val negative = RegInit(false.B)
    when (!negative && io.key_in === 11.U) {
        negative := true.B
    }


    
    val num = WireDefault(false.B)
    num := io.key_in < 10.U
    val equal = WireDefault(false.B)
    equal := io.key_in === 15.U
    val isDigit = RegInit(false.B)
    isDigit := io.key_in < 10.U
    val leftBrace = RegInit(false.B)
    leftBrace := io.key_in === 13.U
    val rightBrace = RegInit(false.B)
    rightBrace := io.key_in === 14.U
    val operator = WireDefault(false.B)
    operator := io.key_in >= 10.U && io.key_in <= 12.U
    when(leftBrace){
        when(io.key_in === 11.U){
            operator := false.B
        }
    }
    //State and Constant Declaration=====================
    val sIdle :: sSrc1 :: sOp :: sSrc2 :: sEqual :: Nil = Enum(5)
    val add = 0.U
    val sub = 1.U
    val mul = 2.U

    val state = RegInit(sIdle)
    switch(state){
        is(sIdle){
            state := sSrc1
        }
        is(sSrc1){
            when(operator) {state := sOp}
            when(equal) {state := sEqual}
        }
        is(sOp){
            when(io.key_in === 13.U) { state := sSrc2}
            when(io.key_in < 10.U) {state := sSrc2}
        }
        is(sSrc2){
            when(operator) {
                state := sOp
            }
            when(equal) {state := sEqual}
        }
        is(sEqual){
            state := sSrc1
        }
    }

    when(state === sSrc1){
        when(isDigit){
            src1 := (src1<<3.U) + (src1<<1.U) + in_buffer
        }
        when(rightBrace){
            when(negative){
                src1 := (-src1.asSInt).asUInt
                negative := false.B
            }
            rightBrace := false.B
            leftBrace := false.B
        }
    }

    when(state === sOp){
        src1 := MuxLookup(op,0.U,Seq(
            add -> (src1 + src2),
            sub -> (src1 - src2),
            mul -> (src1 * src2)
        ))
        op := in_buffer - 10.U
        src2 := 0.U
    }

    when(state === sSrc2){
        when(isDigit){
            src2 := (src2<<3.U) + (src2<<1.U) + in_buffer
        }
        when(rightBrace){
            when(negative){
                src2 := (-src2.asSInt).asUInt
                negative := false.B
            }
            rightBrace := false.B
            leftBrace := false.B
        }
    }

    
    when(state === sEqual){
        when(equal){
            ans := MuxLookup(op,0.U,Seq(
                add -> (src1 + src2),
                sub -> (src1 - src2),
                mul -> (src1 * src2)
            ))
        }
        src1 := 0.U
        src2 := 0.U
        op := 0.U
        in_buffer := 0.U
        rightBrace := false.B
        negative := false.B
        isDigit := false.B
    }


    io.value.valid := Mux(state === sEqual,true.B,false.B)
    io.value.bits := ans
}