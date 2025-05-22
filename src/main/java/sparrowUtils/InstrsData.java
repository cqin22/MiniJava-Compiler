package sparrowUtils;

import sparrow.*;
import java.util.BitSet;

public class InstrsData {
    public int index = -1;
    public int goesTo = -1;
    public String labelName;
    public Instruction instruction;
    public java.util.BitSet def = new java.util.BitSet();
    public java.util.BitSet use = new java.util.BitSet();
    public java.util.BitSet in = new java.util.BitSet();
    public java.util.BitSet out = new java.util.BitSet();
}