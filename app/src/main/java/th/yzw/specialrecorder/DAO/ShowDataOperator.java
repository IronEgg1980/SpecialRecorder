package th.yzw.specialrecorder.DAO;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import th.yzw.specialrecorder.model.ShowDataEntity;
import th.yzw.specialrecorder.model.SumTotalRecord;

public class ShowDataOperator {
    public static void saveAll(List<ShowDataEntity> list){
        LitePal.saveAll(list);
    }
    public static int deleAll(){
        return LitePal.deleteAll(ShowDataEntity.class);
    }
    public static int deleAll(String fileName){
        return LitePal.deleteAll(ShowDataEntity.class, "filename=?", fileName);
    }
    public static List<ShowDataEntity> findAll(){
        return LitePal.order("name").find(ShowDataEntity.class);
    }
    public static List<ShowDataEntity> findAll(String fileName){
        return LitePal.order("name").where("filename = ?", fileName).find(ShowDataEntity.class);
    }
    public static List<ShowDataEntity> getShareList(List<SumTotalRecord> list,String fileName){
        List<ShowDataEntity> result = new ArrayList<>();
        if(list != null && !list.isEmpty()){
            for(SumTotalRecord record:list){
                ShowDataEntity entity = new ShowDataEntity();
                entity.setName(record.getName());
                entity.setCount(record.getCount());
                entity.setFileName(fileName);
                entity.setDone(false);
                entity.setDataMode(MyDBHelper.DATA_MODE_NEWDATA);
                result.add(entity);
            }
        }
        return result;
    }
}
