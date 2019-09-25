package th.yzw.specialrecorder.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

import th.yzw.specialrecorder.model.BaseModel;

public abstract class JSONHelper<T extends BaseModel> {
    public final static String JSONARRAY = "array";
    public final static String CLASS = "classname";
    public final static String EXTRADATA = "ItemStatisticalInformation";
    public final static String PHONEID = "phoneid";

//    public static String getBackupFileString(String...objectString) throws JSONException{
//        JSONArray jsonArray = new JSONArray();
//        for(String s : objectString){
//            jsonArray.put(new JSONObject(s));
//        }
//        return jsonArray.toString();
//    }

    public abstract JSONObject toJSONObject(T entity) throws JSONException;

    public abstract JSONObject listToJSONObject(List<T> list) throws JSONException;

    public abstract T parseSingle(JSONObject object) throws JSONException;

    public List<T> parseList(JSONArray array) throws JSONException {
        if (array.length() == 0)
            return null;
        List<T> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            list.add(parseSingle(object));
        }
        return list;
    }

    public JSONArray toJSONArray(List<T> list) throws JSONException {
        JSONArray array = new JSONArray();
        if (list != null && !list.isEmpty()) {
            for (T entity : list) {
                array.put(toJSONObject(entity));
            }
        }
        return array;
    }

    public JSONObject backup(List<T> list) throws JSONException {
        if (list == null || list.isEmpty())
            return new JSONObject();
        return listToJSONObject(list);
    }

    public List<T> restore(JSONObject object) throws JSONException {
        if (object.isNull(JSONARRAY))
            return new ArrayList<>();
        JSONArray array = object.getJSONArray(JSONARRAY);
        List<T> tmp = parseList(array);
        return tmp;
    }

    public static String getBackupString(JSONObject... objects) {
        JSONArray array = new JSONArray();
        for (JSONObject object : objects) {
            array.put(object);
        }
        return array.toString();
    }
}
