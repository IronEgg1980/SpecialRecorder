package th.yzw.specialrecorder.DAO;

import android.text.TextUtils;

import org.litepal.LitePal;

import java.util.List;
import java.util.UUID;

import th.yzw.specialrecorder.model.AppSetup;
import th.yzw.specialrecorder.model.UserPassWord;
import th.yzw.specialrecorder.tools.EncryptAndDecrypt;

public final class AppSetupOperator {
    private static String PHONE_ID;
    // 获取唯一ID，恢复出厂设置会改变
    private static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    public static UserPassWord getPassWord(){
        UserPassWord userPassWord ;
        int size;
        List<UserPassWord> list = LitePal.findAll(UserPassWord.class);
        if(list != null &&(size = list.size())> 0){
            userPassWord = list.get(0);
            for(int i = 1;i<size;i++){
                UserPassWord tmp = list.get(i);
                LitePal.delete(UserPassWord.class,tmp.getId());
            }
        }else{
            userPassWord = new UserPassWord();
            userPassWord.setKey(1);
            userPassWord.setValue("F0b1laslH4Ec2Cppathw+g==");
            userPassWord.save();
        }
        return userPassWord;
    }

    public static void savePassWord(String pwd){
        UserPassWord userPassWord = getPassWord();
        String password = "(/"+pwd+"/)";
        userPassWord.setValue(EncryptAndDecrypt.encryptPassword(password));
        userPassWord.save();
    }

    public static int getCount(){
        return LitePal.count(AppSetup.class);
    }

    public static List<AppSetup> findAll(){
        return LitePal.findAll(AppSetup.class);
    }

    public static void saveAll(List<AppSetup> list){
        LitePal.saveAll(list);
    }

    public static <T> T getSetupValue(String key, T defaultValue) {
        String _key = key.toLowerCase();
        AppSetup appSetup = LitePal.where("key = ?", _key).findFirst(AppSetup.class);
        if (appSetup == null) {
            appSetup = new AppSetup(_key, String.valueOf(defaultValue));
            appSetup.save();
            return defaultValue;
        }
        String value =appSetup.getValue();
        if (defaultValue instanceof Integer)
            return (T)Integer.valueOf(value);
        if (defaultValue instanceof Long)
            return (T)Long.valueOf(value);
        if (defaultValue instanceof Boolean)
            return (T)Boolean.valueOf(value);
        if (defaultValue instanceof Float)
            return (T)Float.valueOf(value);
        return (T)value;
    }

    public static void saveSetupValue(String key, Object value) {
        String _key = key.toLowerCase();
        AppSetup appSetup = LitePal.where("key = ?", _key).findFirst(AppSetup.class);
        if (appSetup == null) {
            appSetup = new AppSetup(_key, String.valueOf(value));
        } else {
            appSetup.setValue(String.valueOf(value));
        }
        appSetup.save();
    }

    public static String getPhoneId() {
        if (TextUtils.isEmpty(PHONE_ID)) {
            PHONE_ID = getSetupValue("phoneid",getUUID());
        }
        return PHONE_ID;
    }

    public static void setPhoneId(String id){
        saveSetupValue("phoneid",id);
    }

    public static long getLastAppVersion(){
        return getSetupValue("lastappversion",0L);
    }

    public static void setLastAppVersion(long version){
        saveSetupValue("lastappversion",version);
    }

    public static boolean isHideMode(){
        return getSetupValue("ishidemode",false);
    }

    public static void setHideMode(boolean b){
        saveSetupValue("ishidemode",b);
    }

    public static int getInputMethod(){
        return getSetupValue("inputmethod",1);
    }

    public static void setInputMethod(int method){
        saveSetupValue("inputmethod",method);
    }

    public static int getShowInformationMode(){
        return getSetupValue("showinfomode",1);
    }

    public static void setShowInformationMode(int mode){
        saveSetupValue("showinfomode",mode);
    }

    public static int getVibrateLevel(){
        return getSetupValue("vibratelevel",30);
    }

    public static void setVibrateLevel(int level){
        saveSetupValue("vibratelevel",level);
    }

    public static boolean isUseVibrate(){
        return getSetupValue("vibratemode",true);
    }

    public static void setUseVibrate(boolean b){
        saveSetupValue("vibratemode",b);
    }

    public static int getSpanCount(){
        return getSetupValue("spancount",2);
    }

    public static void setSpanCount(int count){
        saveSetupValue("spancount",count);
    }

    public static boolean isUseAlarmMode(){
        return getSetupValue("alarm",false);
    }

    public static void setUseAlarmMode(boolean b){
        saveSetupValue("alarm",b);
    }

    public static int getItemVersion(){
        return getSetupValue("itemversion",0);
    }

    public static void setItemVersion(int version){
        saveSetupValue("itemversion",version);
    }

    public static long getDownloadAppVersion(){ return getSetupValue("downloadappversion",1L);}

    public static void setDownloadAppVersion(long version){ saveSetupValue("downloadappversion",version);}

    public static boolean isForceUpdate(){ return getSetupValue("forceupdate",false);}

    public static void setForceUpdate(boolean b){ saveSetupValue("forceupdate",b);}

    public static String getTotalFileName(){
        return getSetupValue("totalfilename","none");
    }

    public static void setTotalFileName(String fileName){
        saveSetupValue("totalfilename",fileName);
    }

    public static boolean getShowGroupButtonStatus(){
        return getSetupValue("showgroupbuttonstatus",false);
    }

    public static void setShowGroupButtonStatus(boolean b){
        saveSetupValue("showgroupbuttonstatus",b);
    }

    public static int getTipsTimes(){
        return getSetupValue("tipstimes",5);
    }

    public static void setTipsTimes(int value){
        saveSetupValue("tipstimes",value);
    }

    //    private static SharedPreferences sharedPreferences;


//    private static SharedPreferences getPreference(Context context) {
//        if (sharedPreferences == null)
//            sharedPreferences = context.getSharedPreferences("AppSetup", MODE_PRIVATE);
//        return sharedPreferences;
//    }
//
//    private static SharedPreferences.Editor getPreferenceEditor(Context context) {
//        return getPreference(context).edit();
//    }

//    static Object getPreferenceValue(Context context,String key,Object defaultValue){
//        if(defaultValue instanceof Integer)
//            return getPreference(context).getInt(key,(int)defaultValue);
//        else if(defaultValue instanceof Float)
//            return getPreference(context).getFloat(key,(float)defaultValue);
//        else if(defaultValue instanceof Long)
//            return getPreference(context).getLong(key,(long)defaultValue);
//        else if(defaultValue instanceof Boolean)
//            return getPreference(context).getBoolean(key,(boolean)defaultValue);
//        else
//            return getPreference(context).getString(key,(String) defaultValue);
//    }
//
//    static void savePreferenceValue(Context context,String key,Object value){
//        SharedPreferences.Editor editor = getPreferenceEditor(context);
//        if(value instanceof Integer)
//            editor.putInt(key,(int)value);
//        else if (value instanceof Float)
//            editor.putFloat(key,(float)value);
//        else if(value instanceof Long)
//            editor.putLong(key,(long)value);
//        else if (value instanceof Boolean)
//            editor.putBoolean(key,(boolean)value);
//        else
//            editor.putString(key,(String)value);
//        editor.apply();
//    }
}
