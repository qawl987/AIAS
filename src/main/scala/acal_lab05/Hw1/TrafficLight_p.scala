package acal_lab05.Hw1

import chisel3._
import chisel3.util._

class TrafficLight_p(Ytime:Int, Gtime:Int, Ptime:Int) extends Module{
  val io = IO(new Bundle{
    val P_button = Input(Bool())
    val H_traffic = Output(UInt(2.W))
    val V_traffic = Output(UInt(2.W))
    val P_traffic = Output(UInt(2.W))
    val timer     = Output(UInt(5.W))
  })

  //please implement your code below...
  io.H_traffic := 0.U
  io.V_traffic := 0.U
  io.P_traffic := 0.U
  io.timer := 0.U
  
  // Parameter
  val Off = 0.U
  val Red = 1.U
  val Yellow = 2.U
  val Green = 3.U

  val sIdle :: sHGVR :: sHYVR :: sHRVG :: sHRVY :: sPG :: Nil = Enum(6)
  val state = RegInit(sIdle)
  val interruptState = RegInit(sIdle)

  // Counter
  val cntMode = WireDefault(0.U(2.W))
  val cntReg = RegInit(0.U(4.W))
  val cntDone = Wire(Bool())
  cntMode := 0.U
  cntDone := cntReg === 0.U


  // State
  when(cntDone){
    when(cntMode === 0.U){
      cntReg := (Gtime-1).U
    }.elsewhen(cntMode === 1.U){
      cntReg := (Ytime-1).U
    }.elsewhen(cntMode === 2.U){
      cntReg := (Ptime-1).U
    }
  }.otherwise{
    cntReg := cntReg - 1.U
  }
  
  when(io.P_button){
    when(state =/= sPG){
      interruptState := state
      state := sPG
      cntReg := (Ptime-1).U
    }
  }
  
  switch(state){
    is(sIdle){
      state := sHGVR
    }
    is(sHGVR){
      when(cntDone) {state := sHYVR}
    }
    is(sHYVR){
      when(cntDone) {state := sHRVG}
    }
    is(sHRVG){
      when(cntDone) {state := sHRVY}
    }
    is(sHRVY){
      when(cntDone) {state := sPG}
    }
    is(sPG){
      when(cntDone) {
        when(interruptState =/= sIdle) {
          state := interruptState
          interruptState := sIdle
        }.otherwise{
          state := sHGVR
        }
      }
    }

  }

  
  

  // Output Decoder

  switch(state){
    is(sHGVR){
      cntMode := 1.U
      io.H_traffic := Green
      io.V_traffic := Red
      io.P_traffic := Red
    }
    is(sHYVR){
      cntMode := 0.U
      io.H_traffic := Yellow
      io.V_traffic := Red
      io.P_traffic := Red
    }
    is(sHRVG){
      cntMode := 1.U
      io.H_traffic := Red
      io.V_traffic := Green
      io.P_traffic := Red
    }
    is(sHRVY){
      cntMode := 2.U
      io.H_traffic := Red
      io.V_traffic := Yellow
      io.P_traffic := Red
    }
    is(sPG){
      cntMode := 0.U
      io.H_traffic := Red
      io.V_traffic := Red
      io.P_traffic := Green
    }
  }
  io.timer := cntReg
  // val ss = Module(new SevenSeg())
  // ss.io.num := cntReg
  // io.display := ss.io.display
}