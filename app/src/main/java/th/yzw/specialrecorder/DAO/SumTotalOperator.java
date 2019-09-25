package th.yzw.specialrecorder.DAO;

import android.database.Cursor;
import android.util.ArrayMap;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import th.yzw.specialrecorder.model.BaseModel;
import th.yzw.specialrecorder.model.RecordEntity;
import th.yzw.specialrecorder.model.SumTotalRecord;
import th.yzw.specialrecorder.tools.MyDateUtils;

public final class SumTotalOperator {
    public static List<SumTotalRecord> getSumData(Calendar c) {
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();
        long end = start + c.getActualMaximum(Calendar.DATE) * MyDateUtils.ONE_DAY_MILLIS - 1;
        return getSumData(start, end);
    }

    public static void deleAll(){
        LitePal.deleteAll(SumTotalRecord.class);
    }

    public static void deleAll(String phoneId){
        LitePal.deleteAll(SumTotalRecord.class, "phoneid =  ?", phoneId);
    }

    public static List<SumTotalRecord> getSumData(Date start, Date end) {
        return getSumData(start.getTime(), end.getTime());
    }

    public static List<SumTotalRecord> getSumData(long start, long end) {
        List<SumTotalRecord> list = new ArrayList<>();
        List<RecordEntity> recordEntities = RecordEntityOperator.findAllBetweenDate(start,end);
        Collections.sort(recordEntities, new Comparator<RecordEntity>() {
            @Override
            public int compare(RecordEntity o1, RecordEntity o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        Map<String,Integer> temp = new LinkedHashMap<>();
        for(RecordEntity entity:recordEntities){
            String key = entity.getName();
            temp.put(key,temp.getOrDefault(key,0)+entity.getCount());
        }
        for(ArrayMap.Entry<String,Integer> entry:temp.entrySet()){
            SumTotalRecord record = new SumTotalRecord();
            record.setName(entry.getKey());
            record.setCount(entry.getValue());
            list.add(record);
        }
        return list;

//        Cursor cursor = LitePal.findBySQL("SELECT DISTINCT name FROM recordentity WHERE date >= ? AND date <= ? GROUP BY name ORDER BY name", String.valueOf(start), String.valueOf(end));
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                String name = cursor.getString(cursor.getColumnIndex("name"));
//                int count = LitePal.where("name = ? AND date >= ? AND date <= ? ", name, String.valueOf(start), String.valueOf(end)).sum(RecordEntity.class, "count", int.class);
//                SumTotalRecord totalRecord = new SumTotalRecord(name, count);
//                list.add(totalRecord);
//            } while (cursor.moveToNext());
//        }
//        return list;
    }

    public static List<SumTotalRecord> getMergeDataAll() {
        List<SumTotalRecord> list =  new ArrayList<>();
        Cursor cursor = LitePal.findBySQL("SELECT DISTINCT name,month FROM sumtotalrecord GROUP BY name ORDER BY name");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                long month = cursor.getLong(cursor.getColumnIndex("month"));
                int count = LitePal.where("name = ? ", name).sum(SumTotalRecord.class, "count", int.class);
                SumTotalRecord totalRecord = new SumTotalRecord(name, count);
                totalRecord.setMonth(month);
                list.add(totalRecord);
            } while (cursor.moveToNext());
        }
        return list;
    }

    public static void saveAll(List<SumTotalRecord> list){
        LitePal.saveAll(list);
    }
}
