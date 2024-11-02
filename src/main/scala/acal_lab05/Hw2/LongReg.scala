package acal_lab05.Hw2

import chisel3._
import chisel3.util._

class LongReg extends Module {
    val io = IO(new Bundle{
        val key_in = Input(UInt(4.W))
        val push = Input(Bool())
        val init = Input(Bool())
        val readPtr = Input(UInt(8.W))
        val output = Output(UInt(4.W))
    })

    val savePtr = RegInit(0.U(8.W))
    val regVec = Reg(Vec(256, UInt(4.W)))
    val initialized = RegInit(false.B) // Flag for initialization

    // Initialize regVec to 0 once
    when(!initialized || io.init) {
        for (i <- 0 until 256) {
            regVec(i) := 0.U
        }
        initialized := true.B
        savePtr := 0.U
    }
    when(io.push){
        regVec(savePtr) := io.key_in
        savePtr := savePtr + 1.U
    }
    io.output := regVec(io.readPtr)
}