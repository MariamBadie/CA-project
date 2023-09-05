public class test {
        public static void main(String[] args) {
            int decimalNumber = -13;
            int bits = 6;

            // Determine the sign (positive or negative)
            boolean isNegative = decimalNumber < 0;
            int absValue = Math.abs(decimalNumber);

            // Convert the absolute value to binary
            String binary = Integer.toBinaryString(absValue);

            // Pad with leading zeros for positive numbers or find the two's complement and pad with leading ones for negative numbers
            String signedBinary = isNegative ? padTwosComplement(binary, bits) : padLeadingZeros(binary, bits);

//            System.out.println("Decimal: " + decimalNumber);
//            System.out.println("Signed Binary: " + signedBinary);
//            System.out.println(convertToDecimal(signedBinary));
//            System.out.println(addBinaryStrings("01111111" , "01111111"));
            String s = "1234";

            String fir = convertTo2sComp(-15);
            System.out.println(fir);
            String sec = convertTo2sComp(15);
            String res = performBitwiseAND(fir , sec);
            System.out.println(convertToDecimal(res));

        }

    public static String multiplyBinaryStrings(String binary1, String binary2) {
        int fir = convertToDecimal(binary1);
        int sec = convertToDecimal(binary2);
        int res = fir * sec;
        String bin = convertTo2sComp(res);
        if (bin.length() > 8){
            return null;
        }
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
    private static String padWithLeadingZeros(String binary, int length) {
        StringBuilder paddedBinary = new StringBuilder(binary);
        while (paddedBinary.length() < length) {
            paddedBinary.insert(0, '0');
        }
        return paddedBinary.toString();
    }

    private static String zeros(int count) {
        StringBuilder zeros = new StringBuilder();
        for (int i = 0; i < count; i++) {
            zeros.append('0');
        }
        return zeros.toString();
    }

    private static String addBinaryStrings(String binary1, String binary2) {
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


    public static String convertTo2sComp(int decimalNumber){
        int bits = 6;

        // Determine the sign (positive or negative)
        boolean isNegative = decimalNumber < 0;
        int absValue = Math.abs(decimalNumber);

        // Convert the absolute value to binary
        String binary = Integer.toBinaryString(absValue);

        // Pad with leading zeros for positive numbers or find the two's complement and pad with leading ones for negative numbers
        String signedBinary = isNegative ? padTwosComplement(binary, bits) : padLeadingZeros(binary, bits);

        System.out.println("Decimal: " + decimalNumber);
        System.out.println("Signed Binary: " + signedBinary);
        return signedBinary;
    }
    public static String subtractBinaryStrings(String binary1, String binary2) {
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

        // Pad the binary representation with leading zeros
        private static String padLeadingZeros(String binary, int bits) {
            StringBuilder sb = new StringBuilder();
            for (int i = binary.length(); i < bits; i++) {
                sb.append('0');
            }
            sb.append(binary);
            return sb.toString();
        }

        // Find the two's complement and pad with leading ones
        private static String padTwosComplement(String binary, int bits) {
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
        public static int convertToDecimal(String binary) {
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

        private static String invertBits(String binary) {
            StringBuilder inverted = new StringBuilder();
            for (char bit : binary.toCharArray()) {
                inverted.append(bit == '0' ? '1' : '0');
            }
            return inverted.toString();
        }

        private static int binaryToDecimal(String binary) {
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

}
