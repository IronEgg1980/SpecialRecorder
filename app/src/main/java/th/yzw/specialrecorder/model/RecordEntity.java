package th.yzw.specialrecorder.model;
public class RecordEntity extends BaseModel {

    private long date;
    private String name;
    private int count;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
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

    public RecordEntity() {

    }

    public RecordEntity(long _date, String _name, int _count) {
        this.date = _date;
        this.name = _name;
        this.count = _count;
    }
}

