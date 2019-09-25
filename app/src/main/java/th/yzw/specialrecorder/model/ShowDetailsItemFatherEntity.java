package th.yzw.specialrecorder.model;

import java.util.List;

import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.tools.MyDateUtils;

public class ShowDetailsItemFatherEntity {
    private long date;
    private List<RecordEntity> records;
    private boolean isExpand;

    public ShowDetailsItemFatherEntity(long date){
        this.date = date;
        this.isExpand = false;
        String[] times = MyDateUtils.getDayStartAndEnd(date);
        records  = RecordEntityOperator.findAllBetweenDate(times[0],times[1]);
    }

    public long getDate() {
        return date;
    }

    public List<RecordEntity> getRecords() {
        return records;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }
}
