package th.yzw.specialrecorder.DAO;

import android.util.Log;

import org.json.JSONException;
import org.litepal.LitePal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import th.yzw.specialrecorder.JSON.ItemNameJSONHelper;
import th.yzw.specialrecorder.model.ItemName;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;

public final class ItemNameOperator {

    private ItemNameOperator(){}

    public static int getCount(){
        return LitePal.count(ItemName.class);
    }

    public static boolean isExist(String name){
        return LitePal.isExist(ItemName.class,"name = ?", name);
    }

    public static List<ItemName> findAll(){
        return LitePal.findAll(ItemName.class);
    }

    public static List<String> getItemNameFistLetterList() {
        Set<String> set = new TreeSet<>();
        for (ItemName itemName : ItemNameOperator.findAll(true)) {
            set.add(itemName.getName().substring(0, 1).toUpperCase());
        }
        return new ArrayList<>(set);
    }

    public static String[] getItemNameFirstLetters(){
       List<String> tmp = getItemNameFistLetterList();
       String[] result = new String[tmp.size()];
       tmp.toArray(result);
       return result;
    }

    public static List<ItemName> findAll(boolean orderByNameAesc){
        String order = orderByNameAesc?"name":"name desc";
        return LitePal.order(order).find(ItemName.class);
    }

    public static List<ItemName> findAllForShare(){
        List<ItemName> list = findAll( true );
        for(ItemName itemName:list){
            itemName.setOftenUse(false);
        }
        return list;
    }

    public static List<ItemName> findAllByOftenUse(boolean isOftenUse){
        String oftenUseString =isOftenUse?"1" : "0";
        List<ItemName> list = LitePal.order("name")
                .where("isOftenUse = ? and dataMode = ?",oftenUseString,String.valueOf(MyDBHelper.DATA_MODE_NEWDATA))
                .find(ItemName.class);
        return list;
    }

    public static ItemName findSingle(String name){
        return LitePal.where("name = ?",name).findFirst(ItemName.class);
    }

    public static void saveAll(List<ItemName> list){
        LitePal.saveAll(list);
    }

    public static void deleAll(){
        LitePal.deleteAll(ItemName.class);
    }

    public static int dele(ItemName itemName){
        if(itemName.isSaved())
            return itemName.delete();
        else{
            return LitePal.deleteAll(ItemName.class,"name = ?",itemName.getName());
        }
    }

    public static String[] getItemNameList(List<ItemName> outList) {
        LitePal.deleteAll(ItemName.class, "dataMode = ?", String.valueOf(MyDBHelper.DATA_MODE_OLDDATA));
        String[] itemList = null;
        outList.addAll(LitePal.order("name")
                .where("dataMode = ?", String.valueOf(MyDBHelper.DATA_MODE_NEWDATA))
                .find(ItemName.class));
        if (outList.size() > 0) {
            int size = outList.size();
            itemList = new String[size];
            for (int i = 0; i < size; i++) {
                itemList[i] = outList.get(i).getName();
            }
        }
        return itemList;
    }

    public static boolean updateItemByAsset(String jsonString){
        boolean result = false;
        try {
            List<ItemName> list = new ArrayList<>();
            int fileItemVersion =new ItemNameJSONHelper().parseUpdateFile(jsonString, list);
            if (fileItemVersion > AppSetupOperator.getItemVersion()) {
                ItemNameOperator.deleAll();
                ItemNameOperator.saveAll(list);
                AppSetupOperator.setItemVersion(fileItemVersion);
                result  = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
