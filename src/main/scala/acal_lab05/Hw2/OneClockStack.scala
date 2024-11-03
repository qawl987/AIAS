package acal_lab05.Hw2
import chisel3._
import chisel3.util._

class OneClockStack[T <: Data](val dataType: T, val depth: Int) extends Module {
  val io = IO(new Bundle {
    val push    = Input(Bool())
    val pop     = Input(Bool())
    val en      = Input(Bool())
    val dataIn  = Input(dataType)
    val init    = Input(Bool())
    val dataOut = Output(dataType)
    val empty   = Output(Bool())
  })
  
  // Internal storage using the parameterized data type
  val stack_mem = Reg(Vec(depth, dataType))
  val count_reg = RegInit(0.U(log2Ceil(depth + 1).W))
  
  // Register for data output to avoid combinational loops
  val dataOut_reg = RegInit(0.U.asTypeOf(dataType))
  
  // Calculate top pointers
  val curr_top_ptr = count_reg - 1.U
  val next_top_ptr = count_reg - 2.U
  
  when(io.init){
    count_reg := 0.U
    dataOut_reg := 0.U
  }
  // Output logic - update dataOut_reg on clock edge
  when (io.en && io.pop && (count_reg > 0.U)) {
    // If popping, show the next item in stack (or default if empty)
    dataOut_reg := Mux(count_reg > 1.U, stack_mem(next_top_ptr), 0.U.asTypeOf(dataType))
  } .elsewhen (count_reg > 0.U) {
    // If not popping, show the current top
    dataOut_reg := stack_mem(curr_top_ptr)
  } .otherwise {
    dataOut_reg := 0.U.asTypeOf(dataType)
  }

  // Connect dataOut to registered dataOut_reg
  io.dataOut := dataOut_reg
  
  // Empty signal
  io.empty := count_reg === 0.U
  
  // State update logic
  when (io.en) {
    when (io.push && (count_reg < depth.U)) {
      // Push data onto stack and increment count
      stack_mem(count_reg) := io.dataIn
      count_reg := count_reg + 1.U
    } .elsewhen (io.pop && (count_reg > 0.U)) {
      // Pop from stack and decrement count
      count_reg := count_reg - 1.U
    }
  }
}