//See LICENSE for license details
package firesim.bridges

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.{Parameters}

import firesim.lib._
import firesim.compat._

class NICBridge(implicit p: Parameters) extends BlackBox with Bridge[HostPortIO[NICTargetIO]] {
  val moduleName = "firesim.bridges.SimpleNICBridgeModule"
  val io = IO(new NICTargetIO)
  val bridgeIO = HostPort(io)
  val constructorArg = None
  generateAnnotations()
}


object NICBridge {
  def apply(clock: Clock, nicIO: icenet.NICIOvonly)(implicit p: Parameters): NICBridge = {
    val ep = Module(new NICBridge)
    ep.io.nic <> nicIO
    ep.io.clock := clock
    ep
  }
}
