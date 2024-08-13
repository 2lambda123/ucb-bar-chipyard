//See LICENSE for license details
package firesim.bridges

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.util.DecoupledHelper

import firesim.lib._
import firesim.compat._

class BlockDevBridge(bdParams: BlockDeviceConfig) extends BlackBox
    with Bridge[HostPortIO[BlockDevBridgeTargetIO]] {
  val moduleName = "firesim.bridges.BlockDevBridgeModule"
  val io = IO(new BlockDevBridgeTargetIO(bdParams))
  val bridgeIO = HostPort(io)
  val constructorArg = Some(bdParams)
  generateAnnotations()
}

object BlockDevBridge  {
  def apply(clock: Clock, blkdevIO: testchipip.iceblk.BlockDeviceIO, reset: Bool): BlockDevBridge = {
    val ep = Module(new BlockDevBridge(BlockDeviceConfig(blkdevIO.bdParams.nTrackers)))
    // TODO: ensure these are the same (not just string matched)
    ep.io.bdev <> blkdevIO
    ep.io.clock := clock
    ep.io.reset := reset
    ep
  }
}
