## Hw4-1 Mix Adder
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `MixAdderTest.scala` test
package acal_lab04.Hw1

import chisel3._
import acal_lab04.Lab._

class MixAdder (n:Int) extends Module{
  val io = IO(new Bundle{
      val Cin = Input(UInt(1.W))
      val in1 = Input(UInt((4*n).W))
      val in2 = Input(UInt((4*n).W))
      val Sum = Output(UInt((4*n).W))
      val Cout = Output(UInt(1.W))
  })
  //please implement your code below
  val CLA_Array = Array.fill(n)(Module(new CLAdder()).io)
  val carry = Wire(Vec(n+1, UInt(1.W)))
  // 每個 sum 都是 4 bits
  val sum   = Wire(Vec(n, UInt(4.W)))

  carry(0) := io.Cin

  for (i <- 0 until n) {
    // 取 4 bits
    CLA_Array(i).in1 := io.in1(4*i+3, 4*i)
    CLA_Array(i).in2 := io.in2(4*i+3, 4*i)
    CLA_Array(i).Cin := carry(i)
    // 更新 carry
    carry(i+1) := CLA_Array(i).Cout
    sum(i) := CLA_Array(i).Sum
  }

  io.Sum := sum.asUInt
  io.Cout := carry(n)
}
```
### Test Result
![](https://course.playlab.tw/md/uploads/c35d8c08-304e-4b0e-b3e9-dbae57dd9a81.png)

## Hw4-2 Add-Suber
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `Add_SuberTest.scala` test
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

  //please implement your code below
  // 實際 in2
  val in2 = Wire(UInt(4.W))
  // 抓 2's complement 後 overflow
  val o_f = Wire(UInt(1.W))
  o_f := 0.U

  when(io.op){
    //SUB
    in2 := ~io.in_2 + 1.U
    // -8 做 2's complement 後會 overflow
    when(in2(3) & io.in_2(3)){
      o_f := 1.U
    }
  }.otherwise{
    in2 := io.in_2
  }

  val FA_Array = Array.fill(4)(Module(new FullAdder).io)
  val carry = Wire(Vec(5, UInt(1.W)))
  val sum   = Wire(Vec(4, UInt(1.W)))

  carry(0) := 0.U

  for(i <- 0 until 4){
      FA_Array(i).A := io.in_1(i)
      FA_Array(i).B := in2(i)
      FA_Array(i).Cin := carry(i)
      carry(i+1) := FA_Array(i).Cout
      sum(i) := FA_Array(i).Sum
  }

  io.o_f := (carry(3) ^ carry(4)) | o_f
  io.out := sum.asUInt
}
```
### Test Result
![](https://course.playlab.tw/md/uploads/aa9843d3-c3e8-4aa4-a418-605baa7e15f4.png)

## Hw4-3 Booth Multiplier
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `Booth_MulTest.scala` test
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
  // signed padding 後的 in1
  val in1 = Wire(UInt((2 * width).W))
  when (io.in1(width - 1)) {
    in1 := Cat(Fill(width, 1.U), io.in1)
  } .otherwise {
    in1 := io.in1
  }
  // out array，需初始化
  val out = Wire(Vec((width + 1) / 2, UInt((2*width).W)))
  for (i <- 0 until ((width + 1) / 2)) {
    out(i) := 0.U
  }

  for (i <- 0 until ((width + 1) / 2)) {
    // 取 3 bits
    val y = Wire(UInt(3.W))
    if (i == 0) {
      // padding 0 到 io.in2(-1)
      y := Cat(io.in2(1, 0), 0.U(1.W))
    } else if ((i == (width + 1) / 2 - 1) && width % 2 == 1) {
      // width 是奇數的話 io.in2(2 * i + 1) 會超過長度限制
      y := Cat(0.U(1.W), io.in2(2 * i, 2 * i - 1))
    } else {
      y := io.in2(2 * i + 1, 2 * i - 1)
    }

    switch (y) {
      is ("b000".U) {
        // 0
        // do nothing
      }
      is ("b001".U) {
        // 1
        out(i) := in1 << (2 * i)
      }
      is ("b010".U) {
        // 1
        out(i) := in1 << (2 * i)
      }
      is ("b011".U) {
        // 2
        out(i) := in1 << (2 * i + 1)
      }
      is ("b100".U) {
        // -2
        out(i) := (~(in1 << 1) + 1.U) << (2 * i)
      }
      is ("b101".U) {
        // -1
        out(i) := (~in1 + 1.U) << (2 * i)
      }
      is ("b110".U) {
        // -1
        out(i) := (~in1 + 1.U) << (2 * i)
      }
      is ("b111".U) {
        // 0
        // do nothing
      }
    }
  }
  // 全部加總
  io.out := out.reduce(_ +& _)
}
```
### Test Result
![](https://course.playlab.tw/md/uploads/36ab1bea-c00d-41bc-a748-cc98ef14e3bf.png)

## Hw4-4 Queue Implementation
#### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `SharedBusTest.scala` test
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

  // memory space for queue
  val queue_mem      = Mem(depth, UInt(32.W))
  // head of circular queue
  val head           = RegInit(0.U(log2Ceil(depth+1).W))
  // tail of circular queue
  val tail           = RegInit(0.U(log2Ceil(depth+1).W))
  // element count in queue
  val count          = RegInit(0.U(log2Ceil(depth+1).W))
  // io.dataOut
  val out            = RegInit(0.U(32.W))

  when (io.en) {
    when(io.pop && (count > 0.U)) {
      // update count, head
      count := count - 1.U
      head := (head + 1.U) % depth.asUInt
      // update out
      when (count === 1.U) {
        // when pop the last one, out = 0
        out := 0.U
      }.otherwise {
        // else out = next one
        out := queue_mem((head + 1.U) % depth.asUInt)
      }
      // push only when io.pop = 0
    } .elsewhen(!io.pop && io.push && (count < depth.asUInt)) {
      // insert to queue tail and update count, tail
      queue_mem(tail) := io.dataIn
      count := count + 1.U
      tail := (tail + 1.U) % depth.asUInt
      // update out when first insert to queue
      when (count === 0.U) {
        out := io.dataIn
      }
    }
  }

  io.dataOut := out
  io.empty   := count === 0.U
  io.full    := count === depth.asUInt
}
```
### Test Result
![](https://course.playlab.tw/md/uploads/eb9628a2-898e-4eed-8202-a1e4e75cce42.png)

## Hw4-5 Bus Implementation
#### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `SharedBusTest.scala` test
package acal_lab04.Lab

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil

class MultiShareBus(val addrWidth: Int,val dataWidth: Int,val numMasters: Int,val numSlaves: Int, val addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val masters = Vec(numMasters, Flipped(Decoupled(new MasterInterface(addrWidth, dataWidth))))
    val slaves = Vec(numSlaves, Decoupled(new SlaveInterface(addrWidth, dataWidth)))
  })

  // round robin arbiter
  val arbiter = Module(new RoundRobinArbiter(new MasterInterface(addrWidth, dataWidth), numMasters))
  for (i <- 0 until numMasters) {
    // connect masters' valid, ready to arbiter
    arbiter.io.in(i).valid <> io.masters(i).valid
    arbiter.io.in(i).ready <> io.masters(i).ready
    // set arbiter.io.in.bits to 0, dont care
    arbiter.io.in(i).bits.addr := 0.U
    arbiter.io.in(i).bits.data := 0.U
    arbiter.io.in(i).bits.size := 0.U
  }
  
  // decoder
  val decoders = Seq.tabulate(numSlaves) { i =>
    Module(new MyDecoder(addrWidth, addrMap.slice(i, i+1)))
  }

  // Output dicision mux
  for (i <- 0 until numSlaves) {
    decoders(i).io.addr := io.masters(arbiter.io.chosen).bits.addr
    io.slaves(i).valid := arbiter.io.out.valid && decoders(i).io.select
    io.slaves(i).bits.addr := io.masters(arbiter.io.chosen).bits.addr
    io.slaves(i).bits.data := io.masters(arbiter.io.chosen).bits.data
    io.slaves(i).bits.size := io.masters(arbiter.io.chosen).bits.size
  }

  // set arbiter.io.out.ready to update io.masters(arbiter.io.chosen).ready
  arbiter.io.out.ready := io.slaves.map(_.ready).reduce(_ || _) // OR all ready signals
}
```
### Decoder
#### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
class MyDecoder(addrWidth: Int, addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(addrWidth.W))
    val select = Output(Bool())
  })
  // init select to false
  io.select := false.B

  // iterate the map
  for ((startAddress, size) <- addrMap) {
    // set select to true if any of the address is in the range
    when(io.addr >= startAddress.U && io.addr < (startAddress + size).U) {
      io.select := true.B
    }
  }
}
```
### Aribiter
#### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
class RoundRobinArbiter[T <: Data](gen: T, numIn: Int) extends Module {
  val io = IO(new ArbiterIO(gen, numIn))

  // record last out index, init to 0
  val last_out = RegInit(0.U(log2Ceil(numIn).W))

  // init chosen, out.bits
  io.chosen := 0.U
  io.out.bits := io.in(0).bits
  // init out.valid to false
  io.out.valid := false.B
  // init io.in.ready to false
  for (i <- 0 until numIn) {
    io.in(i).ready := false.B
  }
  
  // loop and get out index by rr
  for (i <- 0 until numIn) {
    val index = (last_out + numIn.U - i.U) % numIn.U
    // if any io.in is valid, update io.chosen and io.out.valid to true
    when(io.in(index).valid) {
      io.chosen := index
      io.out.valid := true.B
      // record last_out
      last_out := index
    }
  }

  // delay one cycle to update io.in.ready, caused by one cycle delay of io.out.ready update
  io.in(RegNext(io.chosen)).ready := RegNext(io.out.ready)
}
```
### Test Result
![](https://course.playlab.tw/md/uploads/a5f237cf-8411-42f5-9923-676474f10754.png)
