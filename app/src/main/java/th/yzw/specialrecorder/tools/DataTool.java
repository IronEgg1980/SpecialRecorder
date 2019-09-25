package th.yzw.specialrecorder.tools;

public final class DataTool {

    public static String[] getItemTypeAll(){
        return new String[]{"KSS", "KBB", "ZKHTPC", "JRZTXY", "KGM", "ZJKJJ", "YSJ", "THY", "ZXY", "TLCD", "JS", "QT"};
    }

    public static String getItemTypeString(int itemType) {
        switch (itemType) {
            case 1:
                return "KSS";
            case 2:
                return "KBB";
            case 3:
                return "ZKHTPC";
            case 4:
                return "JRZTXY";
            case 5:
                return "KGM";
            case 6:
                return "ZJKJJ";
            case 7:
                return "YSJ";
            case 8:
                return "THY";
            case 9:
                return "ZXY";
            case 10:
                return "TLCD";
            case 11:
                return "JS";
            case 12:
                return "QT";
            default:
                return "Undefine";
        }
    }

    public static String[] getItemFomalationAll(){
        return new String[]{"KF", "ZS", "WY"};
    }

    public static String getItemFomalationString(int fomalation) {
        switch (fomalation) {
            case 1:
                return "KF";
            case 2:
                return "ZS";
            case 3:
                return "WY";
            default:
                return "Undefine";
        }
    }


}
