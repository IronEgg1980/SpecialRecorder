package th.yzw.specialrecorder.model;

public class ShowDataEntity extends BaseModel {
    private String fileName;
    private String name;
    private int count;
    private boolean isDone;


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public ShowDataEntity() {

    }

    public ShowDataEntity(String _fileName,String _name, int _count) {
        this.fileName = _fileName;
        this.name = _name;
        this.count = _count;
        this.isDone = false;
    }
}
