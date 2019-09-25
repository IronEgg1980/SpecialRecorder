package th.yzw.specialrecorder.JSON;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import th.yzw.specialrecorder.DAO.ItemStatisticalInformationOperator;
import th.yzw.specialrecorder.DAO.MyDBHelper;
import th.yzw.specialrecorder.model.ItemStatisticalInformation;

public class ItemStatisticJSONHelper extends JSONHelper<ItemStatisticalInformation> {
    public final static String ITEMNAME = "name";
    public final static String ITEMFORMALATION = "formalation";
    public final static String ITEMTYPE = "itemType";
    public final static String SELECTEDTIMES = "selectedTimes";
    public final static String QUANTITY = "totalQuantity";
    public final static String CLASSNAME = "ItemStatisticalInformation";

    @Override
    public JSONObject listToJSONObject(List<ItemStatisticalInformation> list) throws JSONException {
        JSONObject totalObject = new JSONObject();
        totalObject.put(CLASS, CLASSNAME);
        totalObject.put(JSONARRAY, toJSONArray(list));
        return totalObject;
    }

    @Override
    public JSONObject toJSONObject(ItemStatisticalInformation entity) throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(PHONEID,entity.getPhoneId());
        jsonObject.put(ITEMNAME,entity.getName());
        jsonObject.put(ITEMFORMALATION,entity.getFormalation());
        jsonObject.put(ITEMTYPE,entity.getItemType());
        jsonObject.put(SELECTEDTIMES,entity.getSelectedTimes());
        jsonObject.put(QUANTITY,entity.getTotalQuantity());
        return jsonObject;
    }

    @Override
    public ItemStatisticalInformation parseSingle(JSONObject object) throws JSONException {
        ItemStatisticalInformation itemStatisticalInformation = new ItemStatisticalInformation();
        if(!object.isNull(PHONEID))
            itemStatisticalInformation.setPhoneId(object.getString(PHONEID));
        else
            itemStatisticalInformation.setPhoneId("null");
        itemStatisticalInformation.setName(object.getString(ITEMNAME));
        itemStatisticalInformation.setFormalation(object.getInt(ITEMFORMALATION));
        itemStatisticalInformation.setItemType(object.getInt(ITEMTYPE));
        itemStatisticalInformation.setSelectedTimes(object.getInt(SELECTEDTIMES));
        itemStatisticalInformation.setTotalQuantity(object.getInt(QUANTITY));
        itemStatisticalInformation.setDataMode(MyDBHelper.DATA_MODE_NEWDATA);
        return itemStatisticalInformation;
    }

//    @Override
//    public List<ItemStatisticalInformation> parseList(JSONArray jsonArray) throws JSONException {
//        List<ItemStatisticalInformation> list = new ArrayList<>();
//        for(int i = 0;i<jsonArray.length();i++){
//            JSONObject object = jsonArray.getJSONObject(i);
//            ItemStatisticalInformation tmp = parseSingle(object);
//            list.add(tmp);
//        }
//        return list;
//    }

    public JSONArray getShareFileJSONArray() throws JSONException{
        List<ItemStatisticalInformation> tmpList = ItemStatisticalInformationOperator.findAll();
        JSONArray array = toJSONArray(tmpList);
        return array;
    }

    public List<ItemStatisticalInformation> parseSharedFile(String jsonString,Map<String,String> outMap) throws JSONException{
        JSONObject object = new JSONObject(jsonString );
        String phoneId = object.getString(PHONEID);
        outMap.put(PHONEID,phoneId);
        JSONArray array = object.getJSONArray(EXTRADATA);
        List<ItemStatisticalInformation> tmp = parseList(array);
        return tmp;
    }
}
