// See LICENSE for license details
package firesim.bridges

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters

import firesim.lib._
import firesim.compat._

class TSIBridge(memoryRegionNameOpt: Option[String]) extends BlackBox with Bridge[HostPortIO[TSIBridgeTargetIO]] {
  val moduleName = "firesim.bridges.TSIBridgeModule"
  val io = IO(new TSIBridgeTargetIO)
  val bridgeIO = HostPort(io)
  val constructorArg = Some(TSIBridgeParams(memoryRegionNameOpt))
  generateAnnotations()
}

object TSIBridge {
  def apply(clock: Clock, port: testchipip.tsi.TSIIO, memoryRegionNameOpt: Option[String], reset: Bool)(implicit p: Parameters): TSIBridge = {
    val ep = Module(new TSIBridge(memoryRegionNameOpt))
    require(ep.io.tsi.w == port.w)
    ep.io.tsi <> port
    ep.io.clock := clock
    ep.io.reset := reset
    ep
  }
}
