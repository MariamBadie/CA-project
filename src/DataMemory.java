public class DataMemory {
    String[] data;
    static DataMemory instance;

    private DataMemory() {
        data = new String[2048];
        for (int i = 0 ; i < data.length ; i++)
            data[i] = "00000000";
    }

    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }
    public static DataMemory getInstance(){
        if (instance == null)
            instance = new DataMemory();
        return instance;
    }
}
