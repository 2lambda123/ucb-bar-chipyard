//See LICENSE for license details
package firesim.bridges

import chisel3._

import midas.widgets._
import firesim.lib._
import firesim.compat._

class GroundTestBridgeModule extends BridgeModule[HostPortIO[GroundTestBridgeTargetIO]] {
  lazy val module = new BridgeModuleImp(this) {
    val io = IO(new WidgetIO)
    val hPort = IO(HostPort(new GroundTestBridgeTargetIO))

    hPort.toHost.hReady := true.B
    hPort.fromHost.hValid := true.B

    val success = RegInit(false.B)

    when (hPort.hBits.success && !success) { success := true.B }

    genROReg(success, "success")
    genCRFile()

    override def genHeader(base: BigInt, memoryRegions: Map[String, BigInt], sb: StringBuilder): Unit = {}
  }
}
