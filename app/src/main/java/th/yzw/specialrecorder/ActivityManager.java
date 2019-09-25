package th.yzw.specialrecorder;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityManager {
    private static List<Activity> activityList = new ArrayList<>() ;
    public static void add(Activity activity){
        activityList.add(activity);
    }
    public static void remove(Activity activity){
        activityList.remove(activity);
    }
    public static void closeAll(){
        for(Activity a:activityList){
            if(!a.isFinishing()){
                a.finish();
            }
        }
        if(activityList != null)
            activityList.clear();
        System.exit(0);
    }
}
