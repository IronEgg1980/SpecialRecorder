package th.yzw.specialrecorder.DAO;

import android.database.Cursor;

import org.litepal.LitePal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import th.yzw.specialrecorder.model.ItemName;
import th.yzw.specialrecorder.model.ItemStatisticalInformation;
import th.yzw.specialrecorder.tools.DataTool;

public final class ItemStatisticalInformationOperator {
    private ItemStatisticalInformationOperator() {
    }

    public static int getCount() {
        return LitePal.count(ItemStatisticalInformation.class);
    }

    public static String[] getNameList(){
        List<ItemStatisticalInformation> list = findAllByOrder();
        if(list == null || list.isEmpty())
            return new String[0];
        else{
            int count = list.size();
            String[] names = new String[count];
            for(int i = 0;i<count;i++){
                names[i] = list.get(i).getName();
            }
            return names;
        }
    }

    public static ItemStatisticalInformation findSingle(String name) {
        ItemStatisticalInformation result = null;
        List<ItemStatisticalInformation> list = findAll(name);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (i == 0)
                    result = list.get(0);
                else
                    list.get(i).delete();
            }
        }
        return result;
    }

    public static List<ItemStatisticalInformation> findAll(String name) {
        return LitePal.where("name = ?", name).find(ItemStatisticalInformation.class);
    }

    public static List<ItemStatisticalInformation> findAll() {
        return LitePal.findAll(ItemStatisticalInformation.class);
    }

    public static List<ItemStatisticalInformation> findAllByOrder() {
        return LitePal.order("selectedTimes desc,totalQuantity desc").find(ItemStatisticalInformation.class);
    }

    public static void saveAll(List<ItemStatisticalInformation> list) {
        LitePal.saveAll(list);
    }

    public static boolean deleAll() {
        return LitePal.deleteAll(ItemStatisticalInformation.class) > 0;
    }

    public static void del(ItemStatisticalInformation information){
        LitePal.delete(ItemStatisticalInformation.class,information.getId());
    }

    public static void del(String name){
        LitePal.deleteAll(ItemStatisticalInformation.class,"name = ?",name);
    }

    public static void del(String name, int count){
        ItemStatisticalInformation itemStatisticalInformation = findSingle(name);
        if(itemStatisticalInformation!=null){
            int selectTimes = itemStatisticalInformation.getSelectedTimes() - 1;
            int totalCount = itemStatisticalInformation.getTotalQuantity() - count;
            if(totalCount < 1 || selectTimes < 1)
                itemStatisticalInformation.delete();
            else {
                itemStatisticalInformation.setSelectedTimes(selectTimes);
                itemStatisticalInformation.setTotalQuantity(totalCount);
                itemStatisticalInformation.save();
            }
        }
    }

    public static void update(String name,int changedValue){
        ItemStatisticalInformation itemStatisticalInformation = findSingle(name);
        if(itemStatisticalInformation!=null){
            itemStatisticalInformation.setTotalQuantity(itemStatisticalInformation.getTotalQuantity()+changedValue);
            itemStatisticalInformation.save();
        }
    }

    public static void saveItemStatisticalInformation(ItemName itemName, int count,boolean isRetouch) {
        String name = itemName.getName();
        ItemStatisticalInformation item = findSingle(name);
        if (item != null) {
            if(isRetouch)
                item.setSelectedTimes(item.getSelectedTimes() + 1);
            item.setTotalQuantity(item.getTotalQuantity() + count);
        } else {
            item = new ItemStatisticalInformation();
            item.setName(name);
            item.setItemType(itemName.getItemType());
            item.setFormalation(itemName.getFormalation());
            item.setPhoneId(AppSetupOperator.getPhoneId());
            item.setSelectedTimes(1);
            item.setTotalQuantity(count);
        }
        item.save();
    }

    public static void saveItemStatisticalInformation(ItemName itemName, int count) {
        saveItemStatisticalInformation(itemName,count,true);
    }

    public static Map<String, Integer> getItemTypeSum() {
        Map<String, Integer> map = new LinkedHashMap<>();
        Cursor cursor1 = LitePal.findBySQL("SELECT DISTINCT itemtype FROM ItemStatisticalInformation GROUP BY itemtype");
        if (cursor1 != null && cursor1.moveToFirst()) {
            do {
                int type = cursor1.getInt(cursor1.getColumnIndex("itemtype"));
                int c = getItemTypeCount(type);
                map.put(DataTool.getItemTypeString(type), c);
            } while (cursor1.moveToNext());
        }
        return map;
    }

    public static Map<String, Integer> getFormalationSum() {
        Map<String, Integer> map = new LinkedHashMap<>();
        Cursor cursor2 = LitePal.findBySQL("SELECT DISTINCT formalation FROM ItemStatisticalInformation GROUP BY formalation");
        if (cursor2 != null && cursor2.moveToFirst()) {
            do {
                int f = cursor2.getInt(cursor2.getColumnIndex("formalation"));
                int c = getFormalationCount(f);
                map.put(DataTool.getItemFomalationString(f), c);
            } while (cursor2.moveToNext());
        }
        return map;
    }

    public static int getItemTypeCount(int type) {
        return LitePal.where("itemtype = ?", String.valueOf(type)).count(ItemStatisticalInformation.class);
    }

    public static int getFormalationCount(int formalation) {
        return LitePal.where("formalation = ?", String.valueOf(formalation)).count(ItemStatisticalInformation.class);
    }
}
