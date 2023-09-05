import java.util.List;

public class InstructionMemory {
    Instruction[] instructions;
    static InstructionMemory instance;
    private int instructionCount;

    public int getInstructionCount() {
        return instructionCount;
    }

    private InstructionMemory() {
        instructions = new Instruction[1024];
        instructionCount = 0;
    }
    public static InstructionMemory getInstance(){
        if (instance == null)
            instance = new InstructionMemory();
        return instance;
    }
    public void putInstruction(Instruction ins){
        instructions[instructionCount] = ins;
        instructionCount++;
    }
}
