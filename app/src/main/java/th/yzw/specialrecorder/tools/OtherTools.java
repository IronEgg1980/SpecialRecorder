package th.yzw.specialrecorder.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.StringReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import th.yzw.specialrecorder.DAO.AppSetupOperator;

public final class OtherTools {
    public static long lastClickTime = 0;

    public static String getRandomColor(int alpha){
        String c ="0123456789abcdef";
        StringBuilder buffer = new StringBuilder();
        Random random = new Random();
        buffer.append('#');
        if(alpha < 0x0)
            alpha = 0x0;
        if(alpha > 0xff)
            alpha = 0xff;
        buffer.append(String.format("%02X",alpha));

        for (int i = 0; i < 6; i++) {
            buffer.append(c.charAt(random.nextInt(16)));
        }
        return buffer.toString();
    }

    public static String getTotalDataFilePWD(String originalPWD){
        int length = originalPWD.length();
        if(length < 20){
            String seed = "secret";
            int seedLen = seed.length();
            int count = 20 - length;
            int times = count / seedLen;
            int charCount = count % seedLen;
            StringBuilder builder = new StringBuilder();
            builder.append(originalPWD);
            for(int i = 0;i<times;i++){
                builder.append(seed);
            }
            builder.append(seed.substring(0,charCount));
            return builder.toString();
        }
        return originalPWD;
    }

    public static String getPhoneInformation(){
        return "\nMy phone information:"+
                "\nPhone Brand ======> "+ Build.BRAND+
                "\nPhone Model ======> "+Build.MODEL+
                "\nSystem Version ======> "+Build.VERSION.RELEASE+
                "\nPhone ID ======> "+ AppSetupOperator.getPhoneId()+
                "\n\nSendTime ======> "+MyDateUtils.getDateDiff();
    }

    public static boolean isNetworkConnected(Context context){
        if(context!=null){
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();
            if(info !=null) {
                boolean b = info.isConnected();
                if (b) {
                    return info.getState() == NetworkInfo.State.CONNECTED;
                }
            }
        }
        return false;
    }

    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 检测手机是否安装某个应用
     *
     * @param context
     * @param appPackageName 应用包名
     * @return true-安装，false-未安装
     */
    public static boolean isAppInstall(Context context, String appPackageName) {
        PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (appPackageName.equals(pn)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 分享前必须执行本代码，主要用于兼容SDK18以上的系统
     */
   public  static void checkFileUriExposure() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
    }

    public static void installApk(Activity context, String apkPath) {
        if (context == null || TextUtils.isEmpty(apkPath)) {
            return;
        }
        File file = new File(apkPath);
        openAPKFile(context, file);
    }

    public static long getAppVersionCode(Context context) {
        long appVersionCode = 0;
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appVersionCode = packageInfo.getLongVersionCode();
            } else {
                appVersionCode = packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return appVersionCode;
    }

    /**
     * 获取当前app version name
     */
    public static String getAppVersionName(Context context) {
        String appVersionName = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            appVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersionName;
    }

    /**
     * 打开安装包
     *
     * @param mContext
     * @param apkFile
     */
   public static void openAPKFile(Activity mContext, File apkFile) {
        // 核心是下面几句代码
        if (null != apkFile) {
            try {
                Intent intent;
                //兼容7.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    Uri contentUri = FileProvider.getUriForFile(mContext, "th.yzw.specialrecorder.fileprovider", apkFile);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                } else {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (mContext.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                    mContext.startActivity(intent);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 跳转到设置-允许安装未知来源-页面
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void startInstallPermissionSettingActivity(Context context) {
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static String intsToHex(int[] ints, boolean upperCase) {
        StringBuffer md5str = new StringBuffer();
        int digital;
        for (int i = 0; i < ints.length; i++) {
            digital = ints[i];

            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        if (upperCase) {
            return md5str.toString().toUpperCase();
        }
        return md5str.toString().toLowerCase();
    }
}
