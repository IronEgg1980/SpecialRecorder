package th.yzw.specialrecorder.model;

public class DownLoadMergeFile extends BaseModel {
    private long downloadTime;
    private String fileName;

    public long getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(long downloadTime) {
        this.downloadTime = downloadTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
