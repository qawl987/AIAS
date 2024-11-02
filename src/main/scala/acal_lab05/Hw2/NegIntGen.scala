package acal_lab05.Hw2

import chisel3._
import chisel3.util._

class NegIntGen extends Module{
    val io = IO(new Bundle{
        val key_in = Input(UInt(4.W))
        val value = Output(Valid(UInt(32.W)))
    })

    //please implement your code below
    io.value.valid := false.B
    io.value.bits := 0.U

    
    val negative = RegInit(false.B)
    when (!negative && io.key_in === 11.U) {
        negative := true.B
    }
    val leftBrace = WireDefault(false.B)
    leftBrace := io.key_in === 13.U
    val rightBrace = RegInit(false.B)
    rightBrace := io.key_in === 14.U
    val equal = WireDefault(false.B)
    equal := io.key_in === 15.U
    
    val isDigit = RegInit(false.B)
    isDigit := io.key_in =/= 10.U && io.key_in =/= 11.U && io.key_in =/= 12.U && io.key_in =/= 13.U && io.key_in =/= 14.U && io.key_in =/= 15.U

    val sIdle :: sAccept :: sEqual :: Nil = Enum(3)
    val state = RegInit(sIdle)

    switch(state){
        is(sIdle){
            state := sAccept
        }
        is(sAccept){
            when(equal) { state := sEqual }
        }
        is(sEqual){
            state := sAccept
        }
    }
    
    val in_buffer = RegNext(io.key_in)
    
    val number = RegInit(0.U(32.W))
    when(state === sAccept){
        when(isDigit){
            number := (number<<3.U) + (number<<1.U) + in_buffer
        }
        when(rightBrace){
            when(negative){
                number := (-number.asSInt).asUInt
                negative := false.B
                rightBrace := false.B
            }
        }
    }.elsewhen(state === sEqual){
        in_buffer := 0.U
        number := 0.U
    }

    io.value.valid := Mux(state === sEqual, true.B, false.B)
    io.value.bits := number
}