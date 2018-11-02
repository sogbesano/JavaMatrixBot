import com.google.gson.annotations.SerializedName;

public class MarkReadJson {

    @SerializedName("m.fully_read")
    private String fully_read;
    @SerializedName("m.read")
    private String read;

    public MarkReadJson(String fully_read, String read) {
        this.fully_read = fully_read;
        this.read = read;
    }

}
