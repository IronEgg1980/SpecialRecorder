package th.yzw.specialrecorder.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class AppUpdateJSONHelper{
    private JSONArray mJsonArray;
    public AppUpdateJSONHelper(String jsonString) throws JSONException {
        this.mJsonArray = new JSONArray(jsonString.replace("\\",""));
    }
    public long getAPKVersion() throws JSONException{
        JSONObject object = mJsonArray.getJSONObject(0);
        JSONObject apkInfoObject = object.getJSONObject("apkData");//旧版本apkInfo，新版本apkData
        return apkInfoObject.getLong("versionCode");

    }


}
