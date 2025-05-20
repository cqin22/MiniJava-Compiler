package sparrowUtils;

import sparrowv.*;
import java.util.BitSet;

public class InstrsData {
    int index = -1;
    Instruction instruction;
    java.util.BitSet def = new java.util.BitSet();
    java.util.BitSet use = new java.util.BitSet();
    java.util.BitSet in = new java.util.BitSet();
    java.util.BitSet out = new java.util.BitSet();
}