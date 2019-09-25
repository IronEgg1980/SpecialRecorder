package th.yzw.specialrecorder.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import th.yzw.specialrecorder.DAO.MyDBHelper;
import th.yzw.specialrecorder.model.AppSetup;

public class AppSetupJSONHelper extends JSONHelper<AppSetup> {
    public final static String KEY = "key";
    public final static String VALUE = "value";
    public final static String CLASSNAME = "appsetup";


    @Override
    public JSONObject toJSONObject(AppSetup entity) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(KEY, entity.getKey());
        object.put(VALUE, entity.getValue());
        return object;
    }

    @Override
    public JSONObject listToJSONObject(List<AppSetup> list) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(CLASS, CLASSNAME);
        object.put(JSONARRAY, toJSONArray(list));
        return object;
    }

    @Override
    public AppSetup parseSingle(JSONObject object) throws JSONException {
        AppSetup appSetup = new AppSetup();
        String key = object.getString(KEY);
        String value = object.getString(VALUE);
        appSetup.setKey(key);
        appSetup.setValue(value);
        appSetup.setDataMode(MyDBHelper.DATA_MODE_NEWDATA);
        return appSetup;
    }

    @Override
    public List<AppSetup> parseList(JSONArray array) throws JSONException {
        List<AppSetup> list = new ArrayList<>();
        for(int i = 0;i<array.length();i++){
            JSONObject object = array.getJSONObject(i);
            AppSetup appSetup = parseSingle(object);
            list.add(appSetup);
        }
        return list;
    }

}
