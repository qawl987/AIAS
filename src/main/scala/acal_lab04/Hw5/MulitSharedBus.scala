package acal_lab04.Lab

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil

class RRArbiter(val addrWidth: Int, val dataWidth: Int, val numMasters: Int) extends Module {
  // Use same as ArbiterIO
  val io = IO(new Bundle {
    val in = Vec(numMasters, Flipped(Decoupled(new MasterInterface(addrWidth, dataWidth))))
    val out = Decoupled(new MasterInterface(addrWidth, dataWidth))
    val chosen = Output(UInt(log2Ceil(numMasters).W))
  })

  val lastIdx = RegInit(0.U(log2Ceil(numMasters).W))
  // Initialize
  io.out.valid := false.B
  io.out.bits := io.in(0).bits
  io.chosen := 0.U
  for (i <- 0 until numMasters) {
    io.in(i).ready := false.B
  }

  // Round-robin arbitration logic
  for (i <- 0 until numMasters) {
    // Every time will start from last time index, and the when statement mechanism will only use the first match
    val index = (lastIdx + numMasters.U - i.U) % numMasters.U
    when(io.in(index).valid) {
      io.chosen := index
      io.out.valid := true.B
      lastIdx := index
    }
  }

  // Delay one cycle to meet the test case
  io.in(RegNext(io.chosen)).ready := RegNext(io.out.ready)
}

class MultiShareBus(val addrWidth: Int,val dataWidth: Int,val numMasters: Int,val numSlaves: Int, val addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val masters = Vec(numMasters, Flipped(Decoupled(new MasterInterface(addrWidth, dataWidth))))
    val slaves = Vec(numSlaves, Decoupled(new SlaveInterface(addrWidth, dataWidth)))
  })

  val decoders = Seq.tabulate(numSlaves) { i =>
    Module(new Decoder(addrWidth, addrMap.slice(i, i+1)))
  }

  val arbiter = Module(new RRArbiter(addrWidth, dataWidth, numMasters))
  // Initialize
  for (i <- 0 until numMasters) {
    arbiter.io.in(i).valid <> io.masters(i).valid
    arbiter.io.in(i).ready <> io.masters(i).ready
    arbiter.io.in(i).bits.addr := 0.U
    arbiter.io.in(i).bits.data := 0.U
    arbiter.io.in(i).bits.size := 0.U
  }

  for (i <- 0 until numSlaves) {
    io.slaves(i).valid := arbiter.io.out.valid && decoders(i).io.select
    io.slaves(i).bits.addr := io.masters(arbiter.io.chosen).bits.addr
    io.slaves(i).bits.data := io.masters(arbiter.io.chosen).bits.data
    io.slaves(i).bits.size := io.masters(arbiter.io.chosen).bits.size
    decoders(i).io.addr := io.masters(arbiter.io.chosen).bits.addr
  }
  arbiter.io.out.ready := io.slaves.map(_.ready).reduce(_ || _) // OR all ready signals
  
}
