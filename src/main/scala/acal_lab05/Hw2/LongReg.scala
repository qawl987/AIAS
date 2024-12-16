package acal_lab05.Hw2

import chisel3._
import chisel3.util._

class LongReg(val bitsWidth: Int) extends Module {
    val io = IO(new Bundle{
        val key_in = Input(UInt(bitsWidth.W))
        val push = Input(Bool())
        val init = Input(Bool())
        val readPtr = Input(UInt(10.W))
        val output = Output(UInt(bitsWidth.W))
    })

    val savePtr = RegInit(0.U(10.W))
    val regVec = Reg(Vec(1024, UInt(bitsWidth.W)))
    val initialized = RegInit(false.B) // Flag for initialization

    // Initialize regVec to 0 once
    when(!initialized || io.init) {
        for (i <- 0 until 1024) {
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