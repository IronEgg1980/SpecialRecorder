package th.yzw.specialrecorder.model;
public class UserPassWord extends BaseModel {
    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private int key;
    private String value;
}
