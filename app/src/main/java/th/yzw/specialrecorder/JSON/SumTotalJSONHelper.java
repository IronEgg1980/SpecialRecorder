package th.yzw.specialrecorder.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.ItemStatisticalInformationOperator;
import th.yzw.specialrecorder.DAO.MyDBHelper;
import th.yzw.specialrecorder.DAO.SumTotalOperator;
import th.yzw.specialrecorder.model.ItemStatisticalInformation;
import th.yzw.specialrecorder.model.SumTotalRecord;

public final class SumTotalJSONHelper extends JSONHelper<SumTotalRecord> {
    public final static String CLASSNAME = "SumTotalRecord";
    public final static String NAME = "name";
    public final static String COUNT = "count";
    public final static String MONTH = "month";
    public final static String SENDTIME = "sendtime";


    @Override
    public JSONObject listToJSONObject(List<SumTotalRecord> list) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(CLASS,CLASSNAME);
        object.put(JSONARRAY,toJSONArray(list));
        return object;
    }
    @Override
    public JSONObject toJSONObject(SumTotalRecord entity) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(NAME,entity.getName());
        object.put(COUNT,entity.getCount());
        object.put(PHONEID,entity.getPhoneId());
        object.put(MONTH,entity.getMonth());
        return object;
    }
    @Override
    public SumTotalRecord parseSingle(JSONObject object) throws JSONException {
        SumTotalRecord record = new SumTotalRecord();
        record.setName(object.getString(NAME));
        record.setCount(object.getInt(COUNT));
        record.setPhoneId(object.getString(PHONEID));
        record.setMonth(object.getLong(MONTH));
        record.setDataMode(MyDBHelper.DATA_MODE_NEWDATA);
        return record;
    }

    public List<SumTotalRecord> parseList(String jsonString) throws JSONException{
        JSONArray array = new JSONArray(jsonString);
        List<SumTotalRecord> tmp = parseList(array);
        return tmp;
    }

    public List<SumTotalRecord> parseListWithOldMode(String arrayString,long mergeMonth) throws JSONException {
        List<SumTotalRecord> list = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(arrayString );
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            String name = object.getString(NAME);
            int count = object.getInt(COUNT);
            SumTotalRecord record = new SumTotalRecord(name, count);
            record.setMonth(mergeMonth);
            record.setPhoneId("sendbyoldversion");
            list.add(record);
        }
        return list;
    }

    public String getSharedJSON(List<SumTotalRecord> list) throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(PHONEID, AppSetupOperator.getPhoneId());
        jsonObject.put(SENDTIME,System.currentTimeMillis());
        JSONArray array = toJSONArray(list);
        jsonObject.put(JSONARRAY,array);
        JSONArray extraData = new ItemStatisticJSONHelper().getShareFileJSONArray();
        jsonObject.put(EXTRADATA,extraData);
        return jsonObject.toString();
    }

    public List<SumTotalRecord> parseSharedJSON(String jsonString, Map<String,Object> outMap) throws JSONException,NullPointerException{
        JSONObject object = new JSONObject(jsonString );
        String phoneId = object.getString(PHONEID);
        long sendTime = object.getLong(SENDTIME);
        outMap.put(PHONEID,phoneId);
        outMap.put(SENDTIME,sendTime);
        JSONArray array = object.getJSONArray(JSONARRAY);
        List<SumTotalRecord> tmp = parseList(array);
        return tmp;
    }
}
