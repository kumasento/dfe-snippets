import java.util.ArrayList;
import com.maxeler.maxcompiler.v2.kernelcompiler.Kernel;
import com.maxeler.maxcompiler.v2.kernelcompiler.KernelParameters;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.memory.*;
import com.maxeler.maxcompiler.v2.kernelcompiler.types.base.DFEVar;
import com.maxeler.maxcompiler.v2.kernelcompiler.types.composite.*;

class ReadBcsrvControl extends Kernel {

    protected ReadBcsrvControl(KernelParameters parameters,
                               SpmvEngineParams engineParams,
                               int numPipes) {
        super(parameters);

        // -- Read indptr values
        DFEVectorType<DFEVar> value_t =
            new DFEVectorType<DFEVar>(dfeFloat(11, 53), numPipes);

        DFEVector<DFEVar> bcsrv_values  = io.input("bcsrv_values", value_t);

        for (int i = 0; i < numPipes; i++) {
            DFEVar value = bcsrv_values[i];
            io.output("rc_bcsrv_value_" + i, value, dfeFloat(11, 53));
        }
    }
}
