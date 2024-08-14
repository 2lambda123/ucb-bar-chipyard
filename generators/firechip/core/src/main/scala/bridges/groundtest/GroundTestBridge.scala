//See LICENSE for license details
package firesim.bridges

import chisel3._

import firesim.lib._
import firesim.compat._

class GroundTestBridge extends BlackBox
    with Bridge[HostPortIO[GroundTestBridgeTargetIO]] {
  val moduleName = "firesim.bridges.GroundTestBridgeModule"
  val io = IO(new GroundTestBridgeTargetIO)
  val bridgeIO = HostPort(io)
  val constructorArg = None
  generateAnnotations()
}

object GroundTestBridge {
  def apply(clock: Clock, success: Bool): GroundTestBridge = {
    val bridge = Module(new GroundTestBridge)
    bridge.io.success := success
    bridge.io.clock := clock
    bridge
  }
}
