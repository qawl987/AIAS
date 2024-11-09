package acal_lab05.Hw3

import chisel3._
import chisel3.util._

class NumGuess(seed:Int = 1) extends Module{
    val io  = IO(new Bundle{
        val gen = Input(Bool())
        val guess = Input(UInt(16.W))
        val puzzle = Output(Vec(4,UInt(4.W)))
        val ready  = Output(Bool())
        val g_valid  = Output(Bool())
        val A      = Output(UInt(3.W))
        val B      = Output(UInt(3.W))

        //don't care at Hw6-3-2 but should be considered at Bonus
        val s_valid = Input(Bool())
    })

    io.puzzle := VecInit(Seq.fill(4)(0.U(4.W)))
    io.ready  := false.B
    io.g_valid  := false.B
    io.A      := 0.U
    io.B      := 0.U

    val prng = Module(new PRNG(seed))
    prng.io.gen := io.gen
    io.ready := prng.io.ready
    io.puzzle := prng.io.puzzle

    val guessVec = VecInit(Seq.tabulate(16 / 4)(i => 
            io.guess(4*(i+1)-1, 4*i)
        ))

    def countPGSame(ls: Seq[(UInt, UInt)]) = {
        ls.map { case (a, b) => (a === b).asUInt() }
        .foldLeft(0.U(3.W))((acc, x) => acc + x)
    }

    // zip (p, g) with same index
    io.A := countPGSame(io.puzzle.zip(guessVec))

    // Use for comprehension gen seq pair (p, g) without the same index
    // Only work for puzzle and guess doesn't contain same number
    val pairs = for {
        pi <- io.puzzle.indices
        gi <- guessVec.indices if pi != gi
    } yield (io.puzzle(pi), guessVec(gi))
    io.B := countPGSame(pairs)

    val sIdle :: sGuess :: Nil = Enum(2)
    val state = RegInit(sIdle)

    switch(state) {
        is(sIdle) {
            when(io.ready) {
                state := sGuess
            }
        }
        is(sGuess) {
            io.g_valid := true.B
        }
    }
}