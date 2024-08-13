//See LICENSE for license details
package firesim.bridges

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.util._

//import testchipip.cosim.{SerializableTileTraceIO, TileTraceIO, TraceBundleWidths}

import midas.targetutils.TriggerSource
import firesim.lib._
import firesim.compat._

/** Target-side module for the TracerV Bridge.
  *
  * @param insnWidths
  *   A case class containing the widths of configurable-length fields in the trace interface.
  *
  * @param numInsns
  *   The number of instructions captured in a single a cycle (generally, the commit width of the pipeline)
  *
  * Warning: If you're not going to use the companion object to instantiate this bridge you must call
  * [[TracerVBridge.generateTriggerAnnotations] _in the parent module_.
  */
class TracerVBridge(widths: TraceBundleWidths)
    extends BlackBox
    with Bridge[HostPortIO[TracerVTargetIO]] {
  require(widths.retireWidth > 0, "TracerVBridge: number of instructions must be larger than 0")
  val moduleName = "firesim.bridges.TracerVBridgeModule"
  val io                                 = IO(new TracerVTargetIO(widths))
  val bridgeIO = HostPort(io)
  val constructorArg                     = Some(widths)
  generateAnnotations()
  // Use in parent module: annotates the bridge instance's ports to indicate its trigger sources
  // def generateTriggerAnnotations(): Unit = TriggerSource(io.triggerCredit, io.triggerDebit)
  def generateTriggerAnnotations(): Unit =
    TriggerSource.evenUnderReset(WireDefault(io.triggerCredit), WireDefault(io.triggerDebit))

  // To placate CheckHighForm, uniquify blackbox module names by using the
  // bridge's instruction count as a string suffix. This ensures that TracerV
  // blackboxes with different instruction counts will have different defnames,
  // preventing FIRRTL CheckHighForm failure when using a chipyard "Hetero"
  // config. While a black box parameter relaxes the check on leaf field
  // widths, CheckHighForm does not permit parameterizations of the length of a
  // Vec enclosing those fields (as is the case here), since the Vec is lost in
  // a lowered verilog module.
  //
  // See https://github.com/firesim/firesim/issues/729.
  def defnameSuffix = s"_${widths.retireWidth}Wide_" + widths.toString.replaceAll("[(),]", "_")

  override def desiredName = super.desiredName + defnameSuffix
}

object FireSimTraceBundleWidths {
  def apply(widths: testchipip.cosim.TraceBundleWidths): TraceBundleWidths = {
    TraceBundleWidths(
      retireWidth = widths.retireWidth,
      iaddrWidth = widths.iaddr,
      insnWidth = widths.insn,
      wdataWidth = widths.wdata,
      causeWidth = widths.cause,
      tvalWidth = widths.tval,
    )
  }
}

object FireSimTileTraceIO {
  def apply(tiletrace: testchipip.cosim.TileTraceIO): TileTraceIO = {
    val ttw = Wire(new TileTraceIO(FireSimTraceBundleWidths(tiletrace.traceBundleWidths)))
    ttw.clock := tiletrace.clock
    ttw.reset := tiletrace.reset
    ttw.trace.retiredinsns.zip(tiletrace.trace.insns).map{ case (l, r) =>
      l.valid := r.valid
      l.iaddr := r.iaddr
      l.insn := r.insn
      l.wdata.zip(r.wdata).map { case (l, r) => l := r }
      l.priv := r.priv
      l.exception := r.exception
      l.interrupt := r.interrupt
      l.cause := r.cause
      l.tval := r.tval
    }
    ttw.trace.time := tiletrace.trace.time
    ttw
  }
}

object TracerVBridge {
  def apply(widths: TraceBundleWidths)(implicit p: Parameters): TracerVBridge = {
    val tracerv = Module(new TracerVBridge(widths))
    tracerv.generateTriggerAnnotations()
    tracerv.io.tiletrace.clock := Module.clock
    tracerv.io.tiletrace.reset := Module.reset
    tracerv
  }

  def apply(tracedInsns: testchipip.cosim.TileTraceIO)(implicit p: Parameters): TracerVBridge = {
    val tracerv = withClockAndReset(tracedInsns.clock, tracedInsns.reset) {
      TracerVBridge(FireSimTraceBundleWidths(tracedInsns.traceBundleWidths))
    }
    tracerv.io.tiletrace <> FireSimTileTraceIO(tracedInsns)
    tracerv
  }
}
