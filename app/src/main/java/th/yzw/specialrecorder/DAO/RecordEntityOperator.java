package th.yzw.specialrecorder.DAO;

import org.litepal.LitePal;

import java.util.List;

import th.yzw.specialrecorder.model.BaseModel;
import th.yzw.specialrecorder.model.RecordEntity;
import th.yzw.specialrecorder.tools.MyDateUtils;

public final class RecordEntityOperator {
    private RecordEntityOperator(){}
    public static int getCount(){
        return LitePal.count(RecordEntity.class);
    }

    public static List<RecordEntity> findAll(){
        return LitePal.order("date").find(RecordEntity.class);
    }

    public static List<RecordEntity> findAllDesc(){
        return LitePal.order("date desc").find(RecordEntity.class);
    }

    public static List<RecordEntity> findOrderByDateDescWithOffset(int offset){
        return LitePal.order("date desc")
                .limit(100)
                .offset(offset)
                .find(RecordEntity.class);
    }

    public static void update(RecordEntity recordEntity,int newValue){
        recordEntity.setCount(newValue);
        recordEntity.save();
    }

    public static void saveOrMergeByDateAndCount(long date,String name,int count){
        RecordEntity r = findSingleByDateAndName(date,name);
        if (r == null) {
            r = new RecordEntity(date,name, count);
        } else {
            r.setDate(date);
            r.setCount(r.getCount() + count);
        }
        r.save();
    }

    public static void saveAll(List<RecordEntity> list){
        LitePal.saveAll(list);
    }

    public static void del(RecordEntity recordEntity){
        if(recordEntity.isSaved())
            LitePal.delete(RecordEntity.class,recordEntity.getId());
    }

    public static void deleAll(){
        LitePal.deleteAll(RecordEntity.class);
    }

    public static void deleAllBetweenDate(long start,long end){
        LitePal.deleteAll(RecordEntity.class, "date >=? and date <=?", String.valueOf(start), String.valueOf(end));
    }

    public static List<RecordEntity> findAllBetweenDate(String start,String end){
        return LitePal
                .where("date >= ? and date <= ?",start,end)
                .order("date")
                .find(RecordEntity.class);
    }

    public static List<RecordEntity> findAllBetweenDate(long start,long end){
        return findAllBetweenDate(String.valueOf(start),String.valueOf(end));
    }

    public static RecordEntity findSingleByDateAndName(long date,String name){
        String[] dates = MyDateUtils.getDayStartAndEnd(date);
        return LitePal
                .where("name = ? and date >= ? and date <= ?", name, dates[0], dates[1])
                .findFirst(RecordEntity.class);
    }
}
