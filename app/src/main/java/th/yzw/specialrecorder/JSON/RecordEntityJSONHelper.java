package th.yzw.specialrecorder.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import th.yzw.specialrecorder.DAO.MyDBHelper;
import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.model.BaseModel;
import th.yzw.specialrecorder.model.RecordEntity;

public final class RecordEntityJSONHelper extends JSONHelper<RecordEntity> {
    public final static String DATE = "date";
    public final static String ITEMNAME = "name";
    public final static String COUNT = "count";
    public final static String CLASSNAME = "RecordEntity";

    @Override
    public JSONObject listToJSONObject(List<RecordEntity> list) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(CLASS, CLASSNAME);
        object.put(JSONARRAY, toJSONArray(list));
        return object;
    }

    @Override
    public JSONObject toJSONObject(RecordEntity entity) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(ITEMNAME, entity.getName());
        object.put(DATE, entity.getDate());
        object.put(COUNT, entity.getCount());
        return object;
    }

    @Override
    public RecordEntity parseSingle(JSONObject object) throws JSONException {
        long date = object.getLong(DATE);
        String name = object.getString(ITEMNAME);
        int count = object.getInt(COUNT);
        RecordEntity entity = new RecordEntity();
        entity.setName(name);
        entity.setDate(date);
        entity.setCount(count);
        entity.setDataMode(MyDBHelper.DATA_MODE_NEWDATA);
        return entity;
    }

    @Override
    public List<RecordEntity> parseList(JSONArray array) throws JSONException {
        List<RecordEntity> itemNames = new ArrayList<>();
        int size = array.length();
        for (int j = 0; j < size; j++) {
            JSONObject object = array.getJSONObject(j);
            RecordEntity recordEntity = parseSingle(object);
            itemNames.add(recordEntity);
        }
        return itemNames;
    }

}
