package de.peeeq.wurstscript.llvm.analysis;

import de.peeeq.wurstscript.llvm.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BasicBlocks {
    public static List<PhiNode> getPhiNodes(BasicBlock block) {
        List<PhiNode> result = new ArrayList<>();
        for (Instruction i : block) {
            if (i instanceof CommentInstr) {
                continue;
            }
            if (i instanceof PhiNode) {
                result.add((PhiNode) i);
            } else {
                return result;
            }
        }
        return result;
    }

    public static Optional<TerminatingInstruction> getTerminatingInstruction(BasicBlock block) {
        for (int i = block.size() - 1; i >= 0; i--) {
            Instruction instr = block.get(i);
            if (instr instanceof CommentInstr) {
                continue;
            }
            if (instr instanceof TerminatingInstruction) {
                return Optional.of((TerminatingInstruction) instr);
            }
            break;
        }
        return Optional.empty();
    }
}
