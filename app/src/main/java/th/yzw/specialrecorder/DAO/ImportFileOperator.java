package th.yzw.specialrecorder.DAO;

import org.litepal.LitePal;

import th.yzw.specialrecorder.model.ImportedFile;

public final class ImportFileOperator {
    public static ImportedFile findSingleByPhoneId(String phoneId){
        return LitePal.where("phoneid = ?", phoneId).findFirst(ImportedFile.class);
    }
    public static ImportedFile findSingleByFileName(String fileName){
        return LitePal.where("filename = ?", fileName).findFirst(ImportedFile.class);
    }
    public static void deleAll(){
        LitePal.deleteAll(ImportedFile.class);
    }

    public static void deleAll(String phoneId){
        LitePal.deleteAll(ImportedFile.class, "phoneid = ? ", phoneId);
    }
}
