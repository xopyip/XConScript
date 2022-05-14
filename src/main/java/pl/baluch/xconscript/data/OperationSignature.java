package pl.baluch.xconscript.data;

import java.util.ArrayList;
import java.util.List;

public class OperationSignature {
    private final List<InsOuts> overrides = new ArrayList<>();

    public List<InsOuts> getOverrides() {
        return overrides;
    }

    public OperationSignature addOverride(DataType in1, DataType out, int opcode) {
        overrides.add(new InsOuts(in1, out, opcode));
        return this;
    }
    public OperationSignature addOverride(DataType in1, DataType in2, DataType out, int opcode) {
        overrides.add(new InsOuts(in1, in2, out, opcode));
        return this;
    }

    public static class InsOuts {
        public DataType in1;
        public DataType in2;
        public DataType out;
        public int opcode;

        public InsOuts(DataType in1, DataType out, int opcode) {
            this.in1 = in1;
            this.out = out;
            this.opcode = opcode;
        }

        public InsOuts(DataType in1, DataType in2, DataType out, int opcode) {
            this.in1 = in1;
            this.in2 = in2;
            this.out = out;
            this.opcode = opcode;
        }
    }
}
