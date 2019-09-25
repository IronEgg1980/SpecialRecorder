package th.yzw.specialrecorder.JSON;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import th.yzw.specialrecorder.DAO.ItemNameOperator;
import th.yzw.specialrecorder.DAO.MyDBHelper;
import th.yzw.specialrecorder.model.ItemName;

public final class ItemNameJSONHelper extends JSONHelper<ItemName> {
    public final static String ITEMVERSION = "itemversion";
    public final static String NAME = "name";
    public final static String OFTENUSE = "isOftenUse";
    public final static String FORMALATION = "formalation";
    public final static String TYPE = "itemType";
    public final static String CLASSNAME = "itemname";

    @Override
    public JSONObject listToJSONObject(List<ItemName> list) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(CLASS, CLASSNAME);
        object.put(JSONARRAY, toJSONArray(list));
        return object;
    }

    @Override
    public JSONObject toJSONObject(ItemName entity) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(NAME, entity.getName());
        obj.put(OFTENUSE, entity.isOftenUse());
        obj.put(FORMALATION, entity.getFormalation());
        obj.put(TYPE, entity.getItemType());
        return obj;
    }

    @Override
    public ItemName parseSingle(JSONObject object) throws JSONException {
        String name = object.getString(NAME);
        boolean isOftenUse = object.getBoolean(OFTENUSE);
        int type = object.getInt(TYPE);
        int formalation = object.getInt(FORMALATION);
        ItemName itemName = new ItemName();
        itemName.setName(name);
        itemName.setOftenUse(isOftenUse);
        itemName.setItemType(type);
        itemName.setFormalation(formalation);
        itemName.setDataMode(MyDBHelper.DATA_MODE_NEWDATA);
        return itemName;
    }

    @Override
    public List<ItemName> parseList(JSONArray array) throws JSONException {
        List<ItemName> itemNames = new ArrayList<>();
        int size = array.length();
        for (int j = 0; j < size; j++) {
            JSONObject object = array.getJSONObject(j);
            ItemName itemName = parseSingle(object);
            itemNames.add(itemName);
        }
        return itemNames;
    }

    public List<ItemName> parseListAndModify(String arrayString) throws JSONException {
        JSONArray array = new JSONArray(arrayString);
        List<ItemName> itemNames = new ArrayList<>();
        int size = array.length();
        for (int j = 0; j < size; j++) {
            JSONObject object = array.getJSONObject(j);
            ItemName itemName = parseSingleAndModify(object.toString());
            itemNames.add(itemName);
        }
        return itemNames;
    }

    public ItemName parseSingleAndModify(String objectString) throws JSONException {
        JSONObject object = new JSONObject(objectString);
        String name = object.getString(NAME);
        boolean isOftenUse = object.getBoolean(OFTENUSE);
        ItemName dbItemName = ItemNameOperator.findSingle(name);
        if (dbItemName != null)
            isOftenUse = dbItemName.isOftenUse();
        int type = object.getInt(TYPE);
        int formalation = object.getInt(FORMALATION);
        ItemName itemName = new ItemName();
        itemName.setName(name);
        itemName.setOftenUse(isOftenUse);
        itemName.setItemType(type);
        itemName.setFormalation(formalation);
        itemName.setDataMode(MyDBHelper.DATA_MODE_NEWDATA);
        return itemName;
    }

    public String getUpdateFileJSONString(int currentVersion) throws JSONException {
        List<ItemName> list = ItemNameOperator.findAllForShare();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ITEMVERSION, currentVersion);
        JSONArray array = toJSONArray(list);
        jsonObject.put(JSONARRAY, array);
        return jsonObject.toString();
    }
    public int parseUpdateFile(String jsonString, List<ItemName> outList) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        int version = jsonObject.getInt(ITEMVERSION);
        if(!jsonObject.isNull(JSONARRAY)) {
            JSONArray jsonArray = jsonObject.getJSONArray(JSONARRAY);
            List<ItemName> tmp = parseListAndModify(jsonArray.toString());
            outList.addAll(tmp);
        }
        return version;
    }


}
