package acal_lab05.Hw3

import chisel3._
import chisel3.util._

class PRNG(seed:Int) extends Module{
    val io = IO(new Bundle{
        val gen = Input(Bool())
        val puzzle = Output(Vec(4,UInt(4.W)))
        val ready = Output(Bool())
    })

    io.puzzle := VecInit(Seq.fill(4)(0.U(4.W)))
    io.ready := false.B

    val seedDigits = Seq(
        (seed / 1000) % 10,  // Thousands place
        (seed / 100) % 10,   // Hundreds place
        (seed / 10) % 10,    // Tens place
        seed % 10            // Ones place
    )

    // registers
    val regs = RegInit(
        VecInit(seedDigits.map(n =>
        n.U(4.W))))

    io.puzzle := regs

    // left shift and non linear change
    val nextNumber = (regs.asUInt() << 1 | (regs(2)(2) ^ regs(3)(0) ^ regs(3)(1) ^ regs(3)(3)))(4 * 4 - 1, 0)

    val nextRegVec = VecInit(Seq.tabulate(16 / 4)(i => 
            nextNumber(4*(i+1)-1, 4*i)
        ))

    val isInRange = nextRegVec
        .map { case n => n <= 9.U }
        .reduce((a, b) => a & b)

    // combination make all reputation
    val noRepetation = nextRegVec
        .combinations(2)
        .map(e => e(0) =/= e(1))
        .reduce((a, b) => a & b)

    val isValid = isInRange && noRepetation

    val sIdle :: sGen :: Nil = Enum(2)
    val state = RegInit(sIdle)

    io.ready  := true.B
    io.puzzle := regs

    switch(state) {
        is(sIdle) {
            when(io.gen) {
                state    := sGen
                io.ready := false.B
            }
        }
        is(sGen) {
            io.ready := false.B
            regs      := nextRegVec
            when(isValid) {
                state := sIdle
            }
        }
    }
}