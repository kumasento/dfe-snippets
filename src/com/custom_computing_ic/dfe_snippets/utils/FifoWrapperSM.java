package com.custom_computing_ic.dfe_snippets.utils;

import com.maxeler.maxcompiler.v2.kernelcompiler.KernelLib;
import com.maxeler.maxcompiler.v2.statemachine.DFEsmInput;
import com.maxeler.maxcompiler.v2.statemachine.DFEsmOutput;
import com.maxeler.maxcompiler.v2.statemachine.stdlib.buffer.*;
import com.maxeler.maxcompiler.v2.statemachine.stdlib.Buffer.DFEsmFifoConfig;
import com.maxeler.maxcompiler.v2.statemachine.stdlib.Buffer.BufferSpec;
import com.maxeler.maxcompiler.v2.statemachine.kernel.KernelStateMachine;
import com.maxeler.maxcompiler.v2.statemachine.types.DFEsmValueType;

/***
    This is a thin wrapper around DFEsmFifo class provided
    by Maxeler.

    For some info about DFEsmFifo, follow the MDX post
    https://groups.google.com/a/maxeler.com/forum/#!searchin/mdx/buffer/mdx/_sQ6PZwkk5Q/gLWcxD0qW8QJ

    Few caveats:
    - in some cases hardware compile fails if FIFO depth is too small (<16)
    - it still occupies some BRAM even when declared with UseLUTRam
    - readEnable signal enables for output at the NEXT cycle
    - beware of data being out twice if FIFO is empty first time
      (though 'valid' output signal is 0 second time)
    - internal data type must be signed in order to allow storing
      64-bit floating point values.
*/
public class FifoWrapperSM extends KernelStateMachine
{
    private DFEsmInput  m_dataIn;
    private DFEsmInput  m_writeEnable;
    private DFEsmInput  m_readEnable;
    private DFEsmOutput m_dataOut;
    private DFEsmOutput m_outValid;
    private DFEsmOutput m_empty;
    private DFEsmOutput m_full;
    private DFEsmOutput m_dataCount;
    private DFEsmFifo   m_buffer;

    private DFEsmValueType m_dType;

    public FifoWrapperSM(KernelLib owner, int dataBitWidth, int fifoDepth)
    {
        super(owner);

        m_dType = dfeInt(dataBitWidth);

        DFEsmFifoConfig config = new DFEsmFifoConfig();
        config.setBufferSpec(BufferSpec.UseLUTRam);
        config.setHasDataCount();
        config.setHasValidFlag();
        m_buffer        = buffer.fifo(m_dType, fifoDepth, config);

        m_dataIn        = io.input("dataIn",     m_dType);
        m_writeEnable   = io.input("writeEnable",dfeBool());
        m_readEnable    = io.input("readEnable", dfeBool());
        m_dataOut       = io.output("dataOut",   m_dType);
        m_dataCount     = io.output("dataCount", m_dType);
        m_outValid      = io.output("outValid",  dfeBool());
        m_empty         = io.output("empty",     dfeBool());
        m_full          = io.output("full",      dfeBool());
    }

    @Override
    public void nextState()
    {
        m_buffer.input.writeEnable <== m_writeEnable;
        m_buffer.output.readEnable <== m_readEnable;
        m_buffer.input.dataIn <== m_dataIn;
    }

    @Override
    public void outputFunction()
    {
        m_full      <== m_buffer.input.full;
        m_empty     <== m_buffer.output.empty;
        m_dataOut   <== m_buffer.output.dataOut;
        m_outValid  <== m_buffer.output.valid;
        m_dataCount <== m_buffer.data_count.cast(m_dType);
    }
}