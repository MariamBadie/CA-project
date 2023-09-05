public class Register {
    private String name;
    private String value;
    public Register(String name) {
        value = "00000000";

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (this.name.equalsIgnoreCase("R0")){
            System.out.println("VALUE OF ZERO REGISTER CANNOT CHANGE");
            return;
        }
        if (value.length() > 8) {
            System.out.println("CANT SET THE WHOLE VALUE SO WE WOULD ONLY TAKE THE LEAST SIGNIFICANT 8 BITS");
            String val = "";
            for (int i = value.length() - 1 ; i >= getValue().length() - 8 ; i--){
                val = value.charAt(i) + val;
            }
            this.value = val;
            return;
        }
        if (value.length() < 8 ){
            while (value.length() < 8){
                value = extendTo8Bits(value);
            }
            this.value = value;
            return;
        }
            this.value = value;
    }
    public static String extendTo8Bits(String binary) {
        if (binary.charAt(0) == '1') {
            // Negative number, extend with 1's
            StringBuilder extended = new StringBuilder();

            for (int i = 0; i < 8 - binary.length(); i++) {
                extended.append('1');
            }

            extended.append(binary.substring(0)); // Preserve the original bits

            return extended.toString();
        } else {
            // Positive number, extend with leading zeros
            StringBuilder extended = new StringBuilder(binary);

            while (extended.length() < 8) {
                extended.insert(0, '0');
            }

            return extended.toString();
        }
    }

}