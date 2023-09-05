import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class CA {
    static Hashtable<Integer, Register> GPRs;
    static Hashtable<Integer, String> operations;
    static int clkCycles = 0;
    PC pc;
    Register SREG;
    DataMemory dataMemory;
    InstructionMemory instructionMemory;
    boolean flag;
    PrintingSimulationGUI gui;
    String pc1 = null;
    String pc2 = null;
    String pc3 = null;

    public CA() throws IOException {
        GPRs = new Hashtable<>();
        operations = new Hashtable<>();

        operations.put(0, "add");
        operations.put(1, "sub");
        operations.put(2, "mul");
        operations.put(3, "movi");
        operations.put(4, "beqz");
        operations.put(5, "andi");
        operations.put(6, "eor");
        operations.put(7, "br");
        operations.put(8, "sal");
        operations.put(9, "sar");
        operations.put(10, "ldr");
        operations.put(11, "str");

        for (int i = 0; i < 64; i++) {
            Register register = new Register("R" + i);
            GPRs.put(i, register);
        }
        gui = new PrintingSimulationGUI();


        pc = PC.getInstance();
        SREG = new Register("SREG");
        dataMemory = DataMemory.getInstance(); // convert to singleton
        instructionMemory = InstructionMemory.getInstance(); // convert to singleton
        ArrayList<String> instructions = readInstructions();
        processInstructions(instructions);
        flag = true;
    }

    public void processInstructions(ArrayList<String> textFile) {
        for (int i = 0; i < textFile.size(); i++) {
            String instructionInBinary = "";
//            GPRs.con
            String currentInstruction = textFile.get(i);
            String[] currentInstructionArr = currentInstruction.split(" ");
            String operation = currentInstructionArr[0];


            for (int j = 0; j < 12; j++) {
                String currOperation = operations.get(j);
                if (currOperation.equalsIgnoreCase(operation)) {
                    String operationInBinary = convertDecToBinary(j, 4);
                    instructionInBinary += operationInBinary;
                    break;
                }
            }

            String firstOperand = currentInstructionArr[1];
            //add r1 r2 >> r1
            for (int j = 0; j < 64; j++) {
                String Reg = GPRs.get(j).getName();
                if (firstOperand.equalsIgnoreCase(Reg)) {
                    String firstOperandInBinary = convertDecToBinary(j, 6);
                    instructionInBinary += firstOperandInBinary;
                    break;
                }
            }

            boolean isImmediate = true;
            String secondOperand = currentInstructionArr[2];

            for (int j = 0; j < 64; j++) {
                String Reg = GPRs.get(j).getName();
                if (secondOperand.equalsIgnoreCase(Reg)) {
                    String firstOperandInBinary = convertDecToBinary(j, 6);
                    instructionInBinary += firstOperandInBinary;
                    isImmediate = false;
                    break;
                }
            }
            // add r1 r2
            // addi r1 5
            if (isImmediate) {
                if (operation.equalsIgnoreCase("LDR") || operation.equalsIgnoreCase("STR")) {
                    String second = convertDecToBinary(Integer.parseInt(secondOperand), 6);
                    instructionInBinary += second;
                    Instruction ins = new Instruction(instructionInBinary);
                    InstructionMemory.getInstance().putInstruction(ins);
                } else {
                    instructionInBinary += convertTo2sComp(Integer.parseInt(secondOperand));
                    Instruction im = new Instruction(instructionInBinary);
                    InstructionMemory.getInstance().putInstruction(im);
                }
            } else {
                Instruction ri = new Instruction(instructionInBinary);
                InstructionMemory.getInstance().putInstruction(ri);
            }


        }
    }

    public String convertDecToBinary(int value, int numBits) {
        String binaryNum = Integer.toBinaryString(value);
        String paddedBinaryNum = String.format("%" + numBits + "s", binaryNum).replace(' ', '0');
        return paddedBinaryNum;
    }

    public String convertTo2sComp(int decimalNumber) {
        int bits = 6;

        // Determine the sign (positive or negative)
        boolean isNegative = decimalNumber < 0;
        int absValue = Math.abs(decimalNumber);

        // Convert the absolute value to binary
        String binary = Integer.toBinaryString(absValue);

        // Pad with leading zeros for positive numbers or find the two's complement and pad with leading ones for negative numbers
        String signedBinary = isNegative ? padTwosComplement(binary, bits) : padLeadingZeros(binary, bits);

//        System.out.println("Decimal: " + decimalNumber);
//        System.out.println("Signed Binary: " + signedBinary);
        return signedBinary;
    }

    // Pad the binary representation with leading zeros
    private String padLeadingZeros(String binary, int bits) {
        StringBuilder sb = new StringBuilder();
        for (int i = binary.length(); i < bits; i++) {
            sb.append('0');
        }
        sb.append(binary);
        return sb.toString();
    }

    // Find the two's complement and pad with leading ones
    private String padTwosComplement(String binary, int bits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < binary.length(); i++) {
            sb.append(binary.charAt(i) == '0' ? '1' : '0');
        }
        String onesComplement = sb.toString();

        // Add one to the ones complement to get the two's complement
        int carry = 1;
        for (int i = onesComplement.length() - 1; i >= 0; i--) {
            if (carry == 0) {
                sb.setCharAt(i, onesComplement.charAt(i));
                break;
            }
            int digit = onesComplement.charAt(i) - '0' + carry;
            sb.setCharAt(i, (char) (digit % 2 + '0'));
            carry = digit / 2;
        }

        // Pad with leading ones
        for (int i = binary.length(); i < bits; i++) {
            sb.insert(0, '1');
        }

        return sb.toString();
    }

    public int convertToDecimal(String binary) {
        int decimal;
        if (binary.charAt(0) == '1') {
            // If the most significant bit is 1, it's a negative number
            String inverted = invertBits(binary);
            decimal = -1 * (binaryToDecimal(inverted) + 1);
        } else {
            // Positive number
            decimal = binaryToDecimal(binary);
        }
        return decimal;
    }

    private String invertBits(String binary) {
        StringBuilder inverted = new StringBuilder();
        for (char bit : binary.toCharArray()) {
            inverted.append(bit == '0' ? '1' : '0');
        }
        return inverted.toString();
    }

    private int binaryToDecimal(String binary) {
        int decimal = 0;
        int power = binary.length() - 1;
        for (char bit : binary.toCharArray()) {
            if (bit == '1') {
                decimal += Math.pow(2, power);
            }
            power--;
        }
        return decimal;
    }


    public ArrayList<String> readInstructions() throws IOException {
        BufferedReader br = null;
        String itemsPath = "instructions.txt";
        ArrayList<String> res = new ArrayList<>();
        br = new BufferedReader(new FileReader(itemsPath));
        String line = br.readLine();
        while (line != null) {
            if (line.trim().equals("")) {
                line = br.readLine();
                continue;
            }
            res.add(line);
            line = br.readLine();
        }
        return res;
    }

    public void pipelining() {
        Instruction first = null;
        Instruction second = null;
        Instruction third = null;
        ArrayList<Object> decode = null;
        // ADD SUB MUL MOVI
        while (convertToDecimal(pc.getValue()) < instructionMemory.getInstructionCount()) {
            clkCycles++;
            int index = convertToDecimal(pc.getValue());
            if (first == null) {
                System.out.println("CYCLE NUMBER " + clkCycles);
                first = fetch(index);
                pc1 = pc.getValue();
                System.out.println("END CYCLE " + clkCycles + "-----------------------------------");
                continue;
            }
            if (second == null) {
                System.out.println("CYCLE NUMBER " + clkCycles);
                second = first;
                first = fetch(index);
                decode = decode(second);
                pc2 = pc1;
                pc1 = pc.getValue();
                System.out.println("END CYCLE " + clkCycles + "-----------------------------------");
                continue;
            } //// ediitt <<<<<<<<<<< 2's complement
            if (third == null) {
                System.out.println("CYCLE NUMBER " + clkCycles);
                third = second;
                second = first;
                first = fetch(index);
                pc3 = pc2;
                pc2 = pc1;
                pc1 = pc.getValue();
                ArrayList<Object> dec = decode(second);
                execute(decode);

                System.out.println("END CYCLE " + clkCycles + "-----------------------------------");

                decode = dec;
                third = null;
                if (!flag) {
                    first = null;
                    second = null;
                    flag = true;
                    decode = null;
                }
                continue;
            }
        }
        if (instructionMemory.getInstructionCount() >= 3) {
            if (first == null && second == null)
                System.out.print("");
            else {
                clkCycles++;
                System.out.println("CYCLE NUMBER " + clkCycles);
                pc3 = pc2;
                pc2 = pc1;
                ArrayList<Object> dec = decode(first);
                execute(decode);
                System.out.println("END CYCLE " + clkCycles + "-----------------------------------");
                if (!flag) {
                    flag = true;
                    pipelining();
                    return;
                }
                clkCycles++;
                pc3 = pc2;
                System.out.println("CYCLE NUMBER " + clkCycles);
                decode = dec;
                execute(decode);

                System.out.println("END CYCLE " + clkCycles + "-----------------------------------");
                if (!flag) {
                    flag = true;
                    pipelining();
                    return;
                }
            }

        } else if (instructionMemory.getInstructionCount() == 2) {
            if (first == null && second == null) {
                // do nothing
            } else {
                clkCycles++;
                System.out.println("CYCLE NUMBER " + clkCycles);
                pc3 = pc2;
                pc2 = pc1;
                ArrayList<Object> dec = decode(first);
                execute(decode);
                System.out.println("END CYCLE " + clkCycles + "-----------------------------------");
                if (!flag) {
                    flag = true;
                    pipelining();
                    return;
                }
                clkCycles++;
                System.out.println("CYCLE NUMBER " + clkCycles);
                pc3 = pc2;
                execute(dec);
                System.out.println("END CYCLE " + clkCycles + "-----------------------------------");
                if (!flag) {
                    flag = true;
                    pipelining();
                    return;
                }
            }

        } else if (instructionMemory.getInstructionCount() == 1) {
            if (first == null) {
                // do nothing
            } else {
                clkCycles++;
                System.out.println("CYCLE NUMBER " + clkCycles);
                pc2 = pc1;
                decode = decode(first);
                System.out.println("END CYCLE " + clkCycles + "-----------------------------------");

                clkCycles++;
                System.out.println("CYCLE NUMBER " + clkCycles);
                pc3 = pc2;
                execute(decode);
                System.out.println("END CYCLE " + clkCycles + "-----------------------------------");
                if (!flag) {
                    flag = true;
                    pipelining();
                    return;
                }
            }
        }
        System.out.println("TOTAL NUMBER OF CYCLES NEEDED = " + (clkCycles));
        printAllRegs();
        printAllMem();
    }

    private void printAllMem() {
        System.out.println("START VALUES OF INSTRUCTION MEMORY");
        for (int i = 0; i < instructionMemory.getInstructionCount(); i++) {
            System.out.println("Instruction " + i + " " + instructionMemory.instructions[i].getValue());
        }
        System.out.println("END VALUES OF INSTRUCTION MEMORY (EXISTING ONLY. AFTER THAT ALL REMAINING CELLS ARE ALL NULLS)");
        System.out.println("START VALUES OF DATA MEMORY");
        for (int i = 0; i < dataMemory.data.length; i++) {
            System.out.println("Data in Cell " + i + " Value : " + dataMemory.data[i] + ", Value in decimal : " + convertToDecimal(dataMemory.data[i]));
        }
    }

    private void printAllRegs() {
        System.out.println("VALUES OF ALL REGISTERS");
        for (int i = 0; i < GPRs.size(); i++)
            System.out.println("Name : " + GPRs.get(i).getName() + ", Value : " + GPRs.get(i).getValue() + ", Value in decimal : " + convertToDecimal(GPRs.get(i).getValue()));
        printSREG();
        System.out.println("PC VALUE IN BINARY: ");
        System.out.println(pc.getValue());
        System.out.println("PC VALUE IN DECIMAL");
        System.out.println(convertToDecimal(pc.getValue()));
        System.out.println("END OF REGISTERS");
    }

    public Instruction fetch(int index) {
        if (index >= instructionMemory.getInstructionCount())
            return null;
        String mid = addBinaryStrings(pc.getValue(), "0000000000000001");
        System.out.println("FETCHED INSTRUCTION IN THIS CYCLE: " + instructionMemory.instructions[index].getValue());
        pc.setValue(mid);
        return instructionMemory.instructions[index];
    }

    public ArrayList<Object> decode(Instruction in) {
        if (in == null)
            return null;
        int index = convertToDecimal(pc.getValue());
        ArrayList<Object> res = new ArrayList<>();
        String value = in.getValue();
        String operationInBinary = value.substring(0, 4);
        String firstOperandInBinary = value.substring(4, 10);
        String secondOperandInBinary = value.substring(10, 16);
        String operation = operations.get(Integer.parseInt(operationInBinary, 2));
        Register firstOperand = GPRs.get(Integer.parseInt(firstOperandInBinary, 2));

        res.add(firstOperand);
        res.add(secondOperandInBinary); // immediate
        res.add(operation);
        Register secondOperand = GPRs.get(Integer.parseInt(secondOperandInBinary, 2)); // register
        res.add(secondOperand);

        System.out.println("FIRST OPERAND IN THIS DECODED INSTRUCTION IN BINARY IS " + firstOperandInBinary);
        System.out.println("FIRST OPERAND IN THIS DECODED INSTRUCTION IN DECIMAL IS " + Integer.parseInt(firstOperandInBinary, 2));
        System.out.println("FIRST OPERAND IN THIS DECODED INSTRUCTION IS: " + ((Register) res.get(0)).getName());
        int imm = convertToDecimal(res.get(1) + "");
        System.out.println("IMMEDIATE VALUE IN THIS DECODED INSTRUCTION IN BINARY IS " + res.get(1));

        System.out.println("SECOND OPERAND IN THIS DECODED INSTRUCTION IN BINARY IS " + secondOperandInBinary);
        System.out.println("SECOND OPERAND IN THIS DECODED INSTRUCTION IN DECIMAL IS " + Integer.parseInt(secondOperandInBinary, 2));
        System.out.println("SECOND OPERAND IN THIS DECODED INSTRUCTION IS: " + ((Register) res.get(3)).getName());
        System.out.println("OPERATION IN THIS DECODED INSTRUCTION IS: " + ((String) res.get(2)));
        return res;

    }

    public void execute(ArrayList<Object> decode) {
        if (decode == null)
            return;
        System.out.println("FIRST OPERAND IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(0)).getName());
        System.out.println("FIRST OPERAND VALUE IN BINARY IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(0)).getValue());
        System.out.println("FIRST OPERAND VALUE IN DECIMAL IN THIS EXECUTING INSTRUCTION IS: " + convertToDecimal(((Register) decode.get(0)).getValue()));


        System.out.println("OPERATION IN THIS EXECUTING INSTRUCTION IS: " + ((String) decode.get(2)));

        String operation = decode.get(2) + "";
        if (operation.equalsIgnoreCase("ADD")) {
            System.out.println("SECOND OPERAND IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(3)).getName());
            System.out.println("SECOND OPERAND VALUE IN BINARY IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(3)).getValue());
            System.out.println("SECOND OPERAND VALUE IN DECIMAL IN THIS EXECUTING INSTRUCTION IS: " + convertToDecimal(((Register) decode.get(3)).getValue()));


            Register firstOperand = (Register) decode.get(0);
            Register secondOperand = (Register) decode.get(3);
            String res = addBinaryStrings(firstOperand.getValue(), secondOperand.getValue());
            String nvalue;
            String carry;
            if (res.length() == 9) {
                carry = "1";
                nvalue = res.substring(1);
            } else {
                carry = "0";
                nvalue = res;
            }
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " = " + nvalue);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " IN DECIMAL = " + convertToDecimal(nvalue));

            boolean hasOverflow = hasOverflow(firstOperand, secondOperand, nvalue);
            firstOperand.setValue(nvalue);
            modifyIndex(carry, 3);
            String overflow;
            if (hasOverflow)
                overflow = "1";
            else
                overflow = "0";
            modifyIndex(overflow, 4);

            String n = nvalue.charAt(0) + "";
            modifyIndex(n, 5);


            String s = (Integer.parseInt(n) ^ Integer.parseInt(overflow)) + "";
            modifyIndex(s, 6);
            int dec = convertToDecimal(nvalue);
            String z;
            if (dec == 0)
                z = "1";
            else
                z = "0";
            modifyIndex(z, 7);
            printSREG();
        } else if (operation.equalsIgnoreCase("SUB")) {
            System.out.println("SECOND OPERAND IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(3)).getName());
            System.out.println("SECOND OPERAND VALUE IN BINARY IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(3)).getValue());
            System.out.println("SECOND OPERAND VALUE IN DECIMAL IN THIS EXECUTING INSTRUCTION IS: " + convertToDecimal(((Register) decode.get(3)).getValue()));
            Register firstOperand = (Register) decode.get(0);
            Register secondOperand = (Register) decode.get(3);
            String res = subtractBinaryStrings(firstOperand.getValue(), secondOperand.getValue());
            if (res.length() >= 9) {
                return;
            }
            firstOperand.setValue(res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " = " + res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " IN DECIMAL = " + convertToDecimal(res));
            String v;
            if (hasOverflow(firstOperand, secondOperand, res))
                v = "1";
            else
                v = "0";
            modifyIndex(v, 4);

            String n = res.charAt(0) + "";
            modifyIndex(n, 5);

            String s = (Integer.parseInt(n) ^ Integer.parseInt(v)) + "";
            modifyIndex(s, 6);
            int dec = convertToDecimal(res);
            String z;
            if (dec == 0)
                z = "1";
            else
                z = "0";
            modifyIndex(z, 7);
            printSREG();
        }

        if (operation.equalsIgnoreCase("MUL")) {
            System.out.println("SECOND OPERAND IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(3)).getName());
            System.out.println("SECOND OPERAND VALUE IN BINARY IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(3)).getValue());
            System.out.println("SECOND OPERAND VALUE IN DECIMAL IN THIS EXECUTING INSTRUCTION IS: " + convertToDecimal(((Register) decode.get(3)).getValue())); Register firstOperand = (Register) decode.get(0);
            Register secondOperand = (Register) decode.get(3);
            String res = multiplyBinaryStrings(firstOperand.getValue(), secondOperand.getValue());
            if (res.length() >= 9) {
                System.out.println("CANT SET VALUE IN REGISTER WITH MORE THAN 8 BITS SO OPERATION CANT BE DONE");
                return;
            }
            firstOperand.setValue(res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " = " + res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " IN DECIMAL = " + convertToDecimal(res));

            String n = res.charAt(0) + "";
            modifyIndex(n, 5);

            int dec = convertToDecimal(res);
            String z;
            if (dec == 0)
                z = "1";
            else
                z = "0";
            modifyIndex(z, 7);
            printSREG();
        }
        if (operation.equalsIgnoreCase("MOVI")) {
            Register firstOperand = (Register) decode.get(0);
            String imm = decode.get(1) + "";
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IS: " + imm);
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IN DECIMAL IS: " + convertToDecimal(imm));

            firstOperand.setValue(imm);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " = " + imm);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " IN DECIMAL = " + convertToDecimal(imm));


        }
        if (operation.equalsIgnoreCase("BEQZ")) {
            Register firstOperand = (Register) decode.get(0);
            String imm = (String) decode.get(1);
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IS: " + imm);
            System.out.println("IMMEDIATE VALUE IN DECIMAL IN THIS EXECUTING INSTRUCTION IS " + convertToDecimal(imm));
            int dec = convertToDecimal(firstOperand.getValue());
            if (dec == 0) {
                String nPC = performSignedUnsignedBinaryAddition(imm, pc3);
                System.out.println(convertToDecimal(pc3) + "<<<<");
                this.pc.setValue(nPC);
                System.out.println("NEW VALUE OF PC " + " = " + nPC);
                System.out.println("NEW VALUE OF PC IN DECIMAL " + " = " + Integer.parseInt(nPC , 2));

                flag = false;
            }
        }
        if (operation.equalsIgnoreCase("ANDI")) {
            Register firstOperand = (Register) decode.get(0);
            String imm = (String) decode.get(1);
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IS: " + imm);
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IN DECIMAL IS: " + convertToDecimal(imm));


            String res = performBitwiseAND(firstOperand.getValue(), imm);
            firstOperand.setValue(res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " = " + res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " IN DECIMAL = " + convertToDecimal(res));

            String n = res.charAt(0) + "";
            modifyIndex(n, 5);

            int dec = convertToDecimal(res);
            String z;
            if (dec == 0)
                z = "1";
            else
                z = "0";
            modifyIndex(z, 7);
            printSREG();
        }
        if (operation.equalsIgnoreCase("EOR")) {
            System.out.println("SECOND OPERAND IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(3)).getName());
            System.out.println("SECOND OPERAND VALUE IN BINARY IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(3)).getValue());
            System.out.println("SECOND OPERAND VALUE IN DECIMAL IN THIS EXECUTING INSTRUCTION IS: " + convertToDecimal(((Register) decode.get(3)).getValue())); Register firstOperand = (Register) decode.get(0);
            Register secondOperand = (Register) decode.get(3);

            String res = performBitwiseXOR(firstOperand.getValue(), secondOperand.getValue());
            firstOperand.setValue(res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " = " + res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " IN DECIMAL = " + convertToDecimal(res));

            String n = res.charAt(0) + "";
            modifyIndex(n, 5);

            int dec = convertToDecimal(res);
            String z;
            if (dec == 0)
                z = "1";
            else
                z = "0";
            modifyIndex(z, 7);
            printSREG();
        }

        if (operation.equalsIgnoreCase("BR")) {
            System.out.println("SECOND OPERAND IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(3)).getName());
            System.out.println("SECOND OPERAND VALUE IN BINARY IN THIS EXECUTING INSTRUCTION IS: " + ((Register) decode.get(3)).getValue());                    
            System.out.println("SECOND OPERAND VALUE IN DECIMAL IN THIS EXECUTING INSTRUCTION IS: " + convertToDecimal(((Register) decode.get(3)).getValue())); Register firstOperand = (Register) decode.get(0);
            Register secondOperand = (Register) decode.get(3);
            String npc = firstOperand.getValue() + secondOperand.getValue();
            pc.setValue(npc);
            System.out.println("NEW VALUE OF PC " + " = " + npc);
            System.out.println("NEW VALUE OF PC IN DECIMAL " + " = " + Integer.parseInt(npc , 2));
            flag = false;
        }
        if (operation.equalsIgnoreCase("SAL")) {
            Register firstOperand = (Register) decode.get(0);
            String imm = (String) decode.get(1);
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IS: " + imm);
            String res = performArithmeticLeftShift(firstOperand.getValue(), imm);
            firstOperand.setValue(res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " = " + res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " IN DECIMAL = " + convertToDecimal(res));

            String n = res.charAt(0) + "";
            modifyIndex(n, 5);

            int dec = convertToDecimal(res);
            String z;
            if (dec == 0)
                z = "1";
            else
                z = "0";
            modifyIndex(z, 7);
            printSREG();
        }

        if (operation.equalsIgnoreCase("SAR")) {
            Register firstOperand = (Register) decode.get(0);
            String imm = (String) decode.get(1);
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IS: " + imm);
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IN DECIMAL IS: " + convertToDecimal(imm));


            String res = performArithmeticRightShift(firstOperand.getValue(), imm);
            firstOperand.setValue(res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " = " + res);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " IN DECIMAL = " + convertToDecimal(res));
            String n = res.charAt(0) + "";
            modifyIndex(n, 5);

            int dec = convertToDecimal(res);
            String z;
            if (dec == 0)
                z = "1";
            else
                z = "0";
            modifyIndex(z, 7);
            printSREG();
        }

        if (operation.equalsIgnoreCase("LDR")) {
            Register firstOperand = (Register) decode.get(0);
            String imm = (String) decode.get(1);
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IS: " + imm);
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IN DECIMAL IS: " + Integer.parseInt(imm,2));

            int address = binaryToDecimal(imm);
            firstOperand.setValue(DataMemory.getInstance().getData()[address]);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " = " + DataMemory.getInstance().getData()[address]);
            System.out.println("NEW VALUE OF REGISTER " + firstOperand.getName() + " IN DECIMAL = " + convertToDecimal(DataMemory.getInstance().getData()[address]));

        }
        if (operation.equalsIgnoreCase("STR")) {
            Register firstOperand = (Register) decode.get(0);
            String imm = (String) decode.get(1);
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IS: " + imm);
            System.out.println("IMMEDIATE VALUE IN THIS EXECUTING INSTRUCTION IN DECIMAL IS: " + Integer.parseInt(imm,2));
            int address = binaryToDecimal(imm);
            String[] mem = DataMemory.getInstance().getData();
            mem[address] = firstOperand.getValue();
            System.out.println("NEW VALUE OF MEMORY ADDRESS " + address + " = " + firstOperand.getValue());
            System.out.println("NEW VALUE OF MEMORY ADDRESS " + address + " IN DECIMAL = " + convertToDecimal(firstOperand.getValue()));
            DataMemory.getInstance().setData(mem);
        }


    }

    public void printSREG() {
        System.out.println("SREG INFO");
        System.out.println("7 6 5 4 3 2 1 0");
        System.out.print("0 0 0 ");
        System.out.print(SREG.getValue().charAt(3) + " ");
        System.out.print(SREG.getValue().charAt(4) + " ");
        System.out.print(SREG.getValue().charAt(5) + " ");
        System.out.print(SREG.getValue().charAt(6) + " ");
        System.out.println(SREG.getValue().charAt(7));
        System.out.println("END SREG");
    }

    private boolean hasOverflow(Register firstOperand, Register secondOperand, String nvalue) {
        boolean isFirstPos = firstOperand.getValue().charAt(0) == '0';
        boolean isSecondPos = secondOperand.getValue().charAt(0) == '0';
        boolean isResPos = nvalue.charAt(0) == '0';

        if ((isFirstPos && isSecondPos && !isResPos) || (!isFirstPos && !isSecondPos && isResPos))
            return true;
        return false;

    }

    public String performSignedUnsignedBinaryAddition(String binary1, String binary2) {
        int maxLength = Math.max(binary1.length(), binary2.length());
        StringBuilder result = new StringBuilder();

        // Pad the binary strings with leading zeros if needed
        binary2 = padWithLeadingZeros(binary2, maxLength);

        // Check if binary1 is negative (2's complement representation)
        boolean isNegative = binary1.charAt(0) == '1';

        // Perform binary addition
        System.out.println(isNegative + "AIOUPQ");
        if (isNegative) {
            // Convert binary1 from 2's complement to decimal
            int decimal1 = convertToDecimal(binary1);
            // Calculate the sum of decimal1 and binary2
            int sum = decimal1 + binaryToDecimal(binary2);
            System.out.println("SUMMMM" + sum);
            // Convert the sum back to binary
            result.append(convertDecToBinary(sum, maxLength));
        } else {
            // Perform unsigned binary addition
            binary1 = padLeadingZeros(binary1, maxLength);
            int carry = 0;
            for (int i = maxLength - 1; i >= 0; i--) {
                int digit1 = binary1.charAt(i) - '0';
                int digit2 = binary2.charAt(i) - '0';

                int sum = digit1 + digit2 + carry;

                result.insert(0, sum % 2);  // Insert the least significant bit of the sum
                carry = sum / 2;  // Calculate the carry for the next bit
            }

            if (carry > 0) {
                result.insert(0, carry);  // Insert the final carry bit if necessary
            }
        }

        return result.toString();
    }


    public String addBinaryStrings(String binary1, String binary2) {
        int maxLength = Math.max(binary1.length(), binary2.length());
        StringBuilder sum = new StringBuilder();
        int carry = 0;


        // Pad the binary strings with leading zeros if needed
        binary1 = padWithLeadingZeros(binary1, maxLength);
        binary2 = padWithLeadingZeros(binary2, maxLength);
        // Perform binary addition
        for (int i = maxLength - 1; i >= 0; i--) {
            int digit1 = binary1.charAt(i) - '0';
            int digit2 = binary2.charAt(i) - '0';

            int currentSum = digit1 + digit2 + carry;
            int sumDigit = currentSum % 2;
            carry = currentSum / 2;

            sum.insert(0, sumDigit);
        }

        // Add final carry if necessary
        if (carry > 0) {
            sum.insert(0, carry);
        }

        return sum.toString();
    }

    public void modifyIndex(String value, int index) {
        String sregBits = SREG.getValue();
        String[] arr = sregBits.split("");
        arr[index] = value;
        String nBits = String.join("", arr);
        SREG.setValue(nBits);

    }

    public String subtractBinaryStrings(String binary1, String binary2) {
        int maxLength = Math.max(binary1.length(), binary2.length());
        StringBuilder difference = new StringBuilder();
        int borrow = 0;

        // Pad the binary strings with leading zeros if needed
        // Perform binary subtraction
        for (int i = maxLength - 1; i >= 0; i--) {
            int digit1 = binary1.charAt(i) - '0';
            int digit2 = binary2.charAt(i) - '0';
            // Apply borrow if necessary
            digit1 -= borrow;

            // Determine the difference
            int currentDifference;
            if (digit1 < digit2) {
                currentDifference = digit1 + 2 - digit2;
                borrow = 1;
            } else {
                currentDifference = digit1 - digit2;
                borrow = 0;
            }

            difference.insert(0, currentDifference);
        }

        return difference.toString();
    }

    public String multiplyBinaryStrings(String binary1, String binary2) {
        int fir = convertToDecimal(binary1);
        int sec = convertToDecimal(binary2);
        int res = fir * sec;
        String bin = convertTo2sComp(res);
        return bin;
    }

    public static String performBitwiseAND(String binary1, String binary2) {
        int maxLength = Math.max(binary1.length(), binary2.length());
        StringBuilder result = new StringBuilder();

        // Pad the binary strings with leading zeros if needed
        binary1 = padWithLeadingZeros(binary1, maxLength);
        binary2 = padWithLeadingZeros(binary2, maxLength);

        // Perform bitwise AND operation
        for (int i = 0; i < maxLength; i++) {
            int digit1 = binary1.charAt(i) - '0';
            int digit2 = binary2.charAt(i) - '0';

            int andResult = digit1 & digit2;
            result.append(andResult);
        }

        return result.toString();
    }

    public static String performBitwiseXOR(String binary1, String binary2) {
        int maxLength = Math.max(binary1.length(), binary2.length());
        StringBuilder result = new StringBuilder();

        // Pad the binary strings with leading zeros if needed
        binary1 = padWithLeadingZeros(binary1, maxLength);
        binary2 = padWithLeadingZeros(binary2, maxLength);

        // Perform bitwise XOR operation
        for (int i = 0; i < maxLength; i++) {
            int digit1 = binary1.charAt(i) - '0';
            int digit2 = binary2.charAt(i) - '0';

            int xorResult = digit1 ^ digit2;
            result.append(xorResult);
        }

        return result.toString();
    }

    private static String padWithLeadingZeros(String binary, int length) {
        StringBuilder paddedBinary = new StringBuilder(binary);
        while (paddedBinary.length() < length) {
            paddedBinary.insert(0, '0');
        }
        return paddedBinary.toString();
    }

    public String performArithmeticLeftShift(String binaryValue, String shiftAmount) {
        int shift = binaryToDecimal(shiftAmount);
        StringBuilder shiftedValue = new StringBuilder(binaryValue);

        // Shift the bits to the left
        for (int i = 0; i < shift; i++) {
            shiftedValue.deleteCharAt(0);
            shiftedValue.append('0');
        }

        return shiftedValue.toString();
    }

    public String performArithmeticRightShift(String binaryValue, String shiftAmount) {
        int shift = binaryToDecimal(shiftAmount);
        StringBuilder shiftedValue = new StringBuilder(binaryValue);

        // Shift the bits to the right
        for (int i = 0; i < shift; i++) {
            char signBit = shiftedValue.charAt(shiftedValue.length() - 1);

            shiftedValue.deleteCharAt(shiftedValue.length() - 1);
            shiftedValue.insert(0, signBit);
        }

        return shiftedValue.toString();
    }

    public static void main(String[] args) throws IOException {
        CA ca = new CA();
//        System.out.println();
        ca.pipelining();

//        Instruction[] inss = ca.instructionMemory.instructions;
//        for (int i = 0; i < ca.instructionMemory.getInstructionCount(); i++) {
//            Instruction ins = inss[i];
//        }
    }
}
