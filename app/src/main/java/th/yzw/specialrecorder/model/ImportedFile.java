package th.yzw.specialrecorder.model;

public class ImportedFile extends BaseModel {
    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getPhoneid() {
        return phoneid;
    }

    public void setPhoneid(String setId) {
        this.phoneid = phoneid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ImportedFile(long sendTime,String phoneid,String fileName){
        this.sendTime = sendTime;
        this.phoneid = phoneid;
        this.fileName = fileName;
    }
    private String phoneid;
    private long sendTime;
    private String fileName;
}

