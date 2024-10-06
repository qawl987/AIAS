package acal_lab04.Lab

import chisel3._
import chisel3.util.log2Ceil

class Queue(val depth: Int) extends Module {
  val io = IO(new Bundle {
    val push    = Input(Bool())
    val pop     = Input(Bool())
    val en      = Input(Bool())
    val dataIn  = Input(UInt(32.W))
    val dataOut = Output(UInt(32.W))
    val empty   = Output(Bool())
    val full    = Output(Bool())
  })
  val queue_mem = Mem(depth, UInt(32.W))
    
  // Head and tail pointers
  val head = RegInit(0.U(log2Ceil(depth).W)) // Points to the element to be popped
  val tail = RegInit(0.U(log2Ceil(depth).W)) // Points to the location for pushing data
  val count = RegInit(0.U(log2Ceil(depth+1).W)) // Keeps track of number of elements in the queue
  val out   = RegInit(0.U(32.W))

  io.empty   := count === 0.U
  io.full    := count === depth.U
  when(io.en) {
    when(io.push && !io.full) {
      // Write data to the queue at the tail pointer
      queue_mem(tail) := io.dataIn
      tail := Mux(tail === (depth-1).U, 0.U, tail + 1.U) // Wrap the tail pointer
      count := count + 1.U
    } .elsewhen(io.pop && !io.empty) {
      // Read data from the queue at the head pointer
      // out := queue_mem(head)
      head := Mux(head === (depth-1).U, 0.U, head + 1.U) // Wrap the head pointer
      count := count - 1.U
    }
    out := queue_mem(head)
  }
  io.dataOut := out
  // check twice
  io.empty   := count === 0.U
  io.full    := count === depth.U  
}
