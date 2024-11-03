// See LICENSE.txt for license details.
package acal_lab05.Hw2

import chisel3._
import chisel3.util.log2Ceil

class CalStack(val depth: Int) extends Module {
  val io = IO(new Bundle {
    val en      = Input(Bool())
    val push    = Input(Bool())
    val cal     = Input(Bool())
    val dataIn  = Input(UInt(32.W))
    val init = Input(Bool())
    val dataOutLast = Output(UInt(32.W))
    val dataOutSecond = Output(UInt(32.W))
    val empty   = Output(Bool())
  })

  val stack_mem = Mem(depth, UInt(32.W))
  val sp        = RegInit(0.U(log2Ceil(depth+1).W))
  val outLast       = RegInit(0.U(32.W))
  val outSecondLast = RegInit(0.U(32.W))

  when(io.init){
    sp := 0.U
    outLast := 0.U
    outSecondLast := 0.U
  }

  when (io.en) {
    when(io.push && (sp < depth.asUInt)) {
      stack_mem(sp) := io.dataIn
      sp := sp + 1.U
      outLast := io.dataIn
      outSecondLast := Mux(sp > 0.U, stack_mem(sp - 1.U), 0.U)
    }.elsewhen(io.cal && (sp > 0.U)) {
        stack_mem(sp - 2.U) := io.dataIn
        sp := sp - 1.U
        outLast := io.dataIn
        outSecondLast := Mux(sp > 2.U, stack_mem(sp - 3.U), 0.U)
    }.otherwise{
      when (sp > 0.U) {
        outLast := stack_mem(sp - 1.U)
      }
      when(sp > 1.U) {
        outSecondLast := stack_mem(sp - 2.U)
      }
    }  
  }

  io.dataOutLast := outLast
  io.dataOutSecond := outSecondLast
  io.empty   := sp === 0.U
}