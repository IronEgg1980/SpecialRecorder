package th.yzw.specialrecorder.DAO;

import org.litepal.LitePal;

import th.yzw.specialrecorder.model.DownLoadFile;

public class DownloadFileOperator {
    public static DownLoadFile findSingleByFileName(String fileName) {
        return LitePal.where("filename = ?", fileName).findFirst(DownLoadFile.class);
    }

//    public static void deleAll() {
//        LitePal.deleteAll(DownLoadMergeFile.class);
//    }

    public static void deleAllMergeFile() {
        LitePal.deleteAll(DownLoadFile.class, "filename like 'SendBy%'");
    }

    public static void deleAllTotalFile() {
        LitePal.deleteAll(DownLoadFile.class, "filename like '%.total'");
    }

    public static void deleOne(String fileName){
        LitePal.deleteAll(DownLoadFile.class,"filename = ?",fileName);
    }

    public static boolean isDownload(String fileName) {
        return LitePal.isExist(DownLoadFile.class, "filename = ?", fileName);
    }
}
