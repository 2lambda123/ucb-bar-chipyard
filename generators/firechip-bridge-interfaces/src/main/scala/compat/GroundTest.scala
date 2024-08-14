package firesim.compat

import chisel3._

class GroundTestBridgeTargetIO extends Bundle {
  val success = Input(Bool())
  val clock = Input(Clock())
}
