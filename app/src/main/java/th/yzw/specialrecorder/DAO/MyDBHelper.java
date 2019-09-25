package th.yzw.specialrecorder.DAO;

import android.content.ContentValues;

import org.litepal.LitePal;

public class MyDBHelper {
    final static int ITEM_TYPE_KSS = 1;// 抗生素
    final static int ITEM_TYPE_KBD = 2;// 抗病毒
    final static int ITEM_TYPE_ZKHTPC = 3;// 止咳化痰平喘
    final static int ITEM_TYPE_JRZTXY = 4;// 解热镇痛消炎
    final static int ITEM_TYPE_KGM = 5;// 抗过敏
    final static int ITEM_TYPE_ZJKJJ = 6;// 镇静抗惊厥
    final static int ITEM_TYPE_YSJ = 7;// 益生菌
    final static int ITEM_TYPE_THY = 8;// 退黄药
    final static int ITEM_TYPE_ZXY = 9;// 止泻药
    final static int ITEM_TYPE_TLCD = 10;// 调理肠道
    final static int ITEM_TYPE_JS = 11;// 激素
    final static int ITEM_TYPE_QT = 12;// 其他


    final static int ITEM_FORMALATION_KF = 1;// 口服
    final static int ITEM_FORMALATION_ZS = 2;// 注射
    final static int ITEM_FORMALATION_WY = 3;// 外用

    public final static int DATA_MODE_NEWDATA = 1;
    public final static int DATA_MODE_OLDDATA = 2;

    public static boolean modifyDataMode(Class c, int mode) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("datamode", mode);
        return LitePal.updateAll(c, contentValues) > 0;
    }

    public static boolean deleteAllWithDataMode(Class c, int mode) {
        return LitePal.deleteAll(c, "dataMode = ?", String.valueOf(mode)) > 0;
    }

}
