package acal_lab04.Hw3

import chisel3._
import chisel3.util._
import scala.annotation.switch

//------------------Radix 4---------------------
class Booth_Mul(width:Int) extends Module {
  val io = IO(new Bundle{
    val in1 = Input(UInt(width.W))      //Multiplicand
    val in2 = Input(UInt(width.W))      //Multiplier
    val out = Output(UInt((2*width).W)) //product
  })
  //please implement your code below
  val mul = Wire(Vec(width / 2 , SInt((2*width).W)))
  // val op = Wire(Vec(width / 2, UInt(3.W)))
  val n = width / 2
  val in2_extended = Cat(io.in2, 0.U(1.W))

  for(i <- 0 until n){
    val op = in2_extended(2*i+2, 2*i)
    mul(i) := 0.S
    // op(i) := in2_extended(2*i+2, 2*i)
    switch(op){
      is("b000".U) { mul(i) := 0.S }  // No operation (0)
      is("b001".U) { mul(i) := io.in1.asSInt << (i * 2) }  // +M
      is("b010".U) { mul(i) := io.in1.asSInt << (i * 2) }  // +M
      is("b011".U) { mul(i) := io.in1.asSInt << (i * 2 + 1) }  // +2M
      is("b100".U) { mul(i) := -(io.in1.asSInt << (i * 2 + 1)) } // -2M
      is("b101".U) { mul(i) := -(io.in1.asSInt << (i * 2)) } // -M
      is("b110".U) { mul(i) := -(io.in1.asSInt << (i * 2)) } // -M
      is("b111".U) { mul(i) := 0.S }  // No operation (0)
    }
  }
  val sum = mul.reduce(_ + _)
  io.out := sum.asUInt
}


