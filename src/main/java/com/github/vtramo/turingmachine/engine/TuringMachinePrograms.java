package com.github.vtramo.turingmachine.engine;

public class TuringMachinePrograms {
    public static TuringMachine isPalindromeTwoTapes() {
        final int numberOfTapes = 2;
        final DeltaProgram deltaProgram = new DeltaProgram(numberOfTapes);
        deltaProgram.addInstruction(Instruction.of("s, >, >", "s  , >, ->, >, ->"));
        deltaProgram.addInstruction(Instruction.of("s, 1, _", "s  , 1, ->, 1, ->"));
        deltaProgram.addInstruction(Instruction.of("s, 0, _", "s  , 0, ->, 0, ->"));
        deltaProgram.addInstruction(Instruction.of("s, _, _", "q  , _, <-, _, - "));
        deltaProgram.addInstruction(Instruction.of("q, 0, _", "q  , 0, <-, _, - "));
        deltaProgram.addInstruction(Instruction.of("q, 1, _", "q  , 1, <-, _, - "));
        deltaProgram.addInstruction(Instruction.of("q, >, _", "p  , >, ->, _, <-"));
        deltaProgram.addInstruction(Instruction.of("q, _, >", "yes, >, ->, _, <-"));
        deltaProgram.addInstruction(Instruction.of("p, 0, 0", "p  , 0, ->, 0, <-"));
        deltaProgram.addInstruction(Instruction.of("p, 1, 1", "p  , 1, ->, 1, <-"));
        deltaProgram.addInstruction(Instruction.of("p, 0, 1", "no , 0, - , 1, - "));
        deltaProgram.addInstruction(Instruction.of("p, 1, 0", "no , 1, - , 0, - "));
        deltaProgram.addInstruction(Instruction.of("p, _, >", "yes, _, - , >, ->"));
        return new TuringMachine("s", deltaProgram);
    }
}
