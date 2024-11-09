package acal_lab05.Hw2

import chisel3.iotesters.{Driver,PeekPokeTester}
import scala.language.implicitConversions

class LongRegTest(dut:LongReg) extends PeekPokeTester(dut){

    val dict = Map(
      '0' -> 0,
      '1' -> 1,
      '2' -> 2,
      '3' -> 3,
      '4' -> 4,
      '5' -> 5,
      '6' -> 6,
      '7' -> 7,
      '8' -> 8,
      '9' -> 9,
      '+' -> 10,
      '-' -> 11,
      '*' -> 12,
      '(' -> 13,
      ')' -> 14,
      '=' -> 15
    )

    val golden = Seq(
                    //  ("50=",50),
                     ("(((((-12)+8)*((5-1)*((-3)*9)-3)+1)-(-3))*4*(5-3)-3)=",3581),
                     
                     )
                     
                     //you can add your own formular for testing

    golden.zipWithIndex.foreach{ case((input,output),index)=>
        input.foreach{ ch =>
            poke(dut.io.key_in,dict(ch))
            poke(dut.io.push, 1)
            step(1)
        }
        
        for(i <- 0 to  80){
            poke(dut.io.readPtr, i)
            step(1)
            println("LongReg output: " + peek(dut.io.output))
        }
        step(1)
    }
}

object LongRegTest extends App{
    Driver.execute(args,()=>new LongReg){
        c:LongReg => new LongRegTest(c)
    }
}