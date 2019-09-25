package th.yzw.specialrecorder.model;

public class SumTotalRecord extends BaseModel {

    private String phoneId;
    private String name;
    private int count;
    private long month;

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

    public long getMonth() {
        return month;
    }

    public void setMonth(long month) {
        this.month = month;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public SumTotalRecord(){}

    public SumTotalRecord(String _name, int _count) {
        this.name = _name;
        this.count = _count;
    }

    public SumTotalRecord(String _name, int _count,String phoneId) {
        this.name = _name;
        this.count = _count;
        this.phoneId = phoneId;
    }

    public SumTotalRecord(String _name, int _count,String phoneId,long month) {
        this.name = _name;
        this.count = _count;
        this.phoneId = phoneId;
        this.month = month;
    }
}

