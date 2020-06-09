package th.yzw.specialrecorder.DAO;

import org.litepal.LitePal;

import th.yzw.specialrecorder.model.DownLoadMergeFile;

public class DownloadFileOperator {
    public static DownLoadMergeFile findSingleByFileName(String fileName) {
        return LitePal.where("filename = ?", fileName).findFirst(DownLoadMergeFile.class);
    }

//    public static void deleAll() {
//        LitePal.deleteAll(DownLoadMergeFile.class);
//    }

    public static void deleAllMergeFile() {
        LitePal.deleteAll(DownLoadMergeFile.class, "filename like 'SendBy%'");
    }

    public static void deleAllTotalFile() {
        LitePal.deleteAll(DownLoadMergeFile.class, "filename like '%.total'");
    }

    public static boolean isDownload(String fileName) {
        return LitePal.isExist(DownLoadMergeFile.class, "filename = ?", fileName);
    }
}
