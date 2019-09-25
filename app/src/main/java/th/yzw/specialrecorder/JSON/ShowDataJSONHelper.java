package th.yzw.specialrecorder.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import th.yzw.specialrecorder.model.ShowDataEntity;

public class ShowDataJSONHelper extends JSONHelper<ShowDataEntity> {
    public static final String NAME = "name";
    public static final String COUNT = "count";
    public static final String FILENAME = "filename";
    public static final String CLASSNAME = "ShowDataEntity";


    @Override
    public JSONObject toJSONObject(ShowDataEntity entity) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(NAME,entity.getName());
        object.put(FILENAME,entity.getFileName());
        object.put(COUNT,entity.getCount());
        return object;
    }

    @Override
    public JSONObject listToJSONObject(List<ShowDataEntity> list) throws JSONException {
        JSONArray array = toJSONArray(list);
        JSONObject object = new JSONObject();
        object.put(CLASS,CLASSNAME);
        object.put(JSONARRAY,array);
        return object;
    }

    @Override
    public ShowDataEntity parseSingle(JSONObject object) throws JSONException {
        String name = object.getString(NAME);
        int count = object.getInt(COUNT);
        String fileName = "no_filename";
        if(!object.isNull(FILENAME))
            fileName = object.getString(FILENAME);
        ShowDataEntity record = new ShowDataEntity();
        record.setName(name);
        record.setCount(count);
        record.setFileName(fileName);
        return record;
    }

    public List<ShowDataEntity> parseList(String jsonString) throws JSONException{
        JSONArray array = new JSONArray(jsonString);
        List<ShowDataEntity> tmp = parseList(array);
        return tmp;
    }


}
