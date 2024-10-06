package acal_lab04.Hw2

import chisel3._
import chisel3.util._
import acal_lab04.Lab._

class Add_Suber extends Module{
  val io = IO(new Bundle{
  val in_1 = Input(UInt(4.W))
	val in_2 = Input(UInt(4.W))
	val op = Input(Bool()) // 0:ADD 1:SUB
	val out = Output(UInt(4.W))
	val o_f = Output(Bool())
  })

  val n = 4
  //please implement your code below
  val FA_Array = Array.fill(n)(Module(new FullAdder()).io)
  val carry = Wire(Vec(n+1, UInt(1.W)))
  val sum   = Wire(Vec(n, Bool()))
  carry(0) := io.op
  val nb = Wire(UInt(n.W))
  nb := io.in_2 ^ Fill(n, io.op)
  for (i <- 0 until n) {
    FA_Array(i).A := io.in_1(i)
    FA_Array(i).B := nb(i)
    FA_Array(i).Cin := carry(i)
    carry(i+1) := FA_Array(i).Cout
    sum(i) := FA_Array(i).Sum
  }
  io.out := sum.asUInt
  io.o_f := carry(n) ^ carry(n-1)
  // io.o_f := carry(n) ^ sum(n-1)
}
