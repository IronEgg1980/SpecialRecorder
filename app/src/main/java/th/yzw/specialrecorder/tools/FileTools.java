package th.yzw.specialrecorder.tools;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.JSON.AppUpdateJSONHelper;

public final class FileTools {
    
    public static final String EXTERNAL_STORAGE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String APP_EXTERNAL_DIR = EXTERNAL_STORAGE_DIR + "/th.yzw.recorder";
    public static final String MICROMSG_DIR = EXTERNAL_STORAGE_DIR + "/tencent/MicroMsg/download";
    public static final String BACKUP_DIR = APP_EXTERNAL_DIR + "/MyBackup";
    public static final String ITEMNAME_EXPORT_DIR = APP_EXTERNAL_DIR + "/Export";
    public static final String TEMP_DIR = APP_EXTERNAL_DIR + "/temp";

    public static String[] getMergeFileList() {
        File path = new File(MICROMSG_DIR);
        return getFileList(path, ".data");
    }

    public static String[] getFileList(File path, String endName) {
        String[] result = new String[0];
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null && files.length > 0) {
                List<String> fileNameList = new ArrayList<>();
                for (File file : files) {
                    if (file.isFile()) {
                        String name = file.getName();
                        if (name.endsWith(endName)) {
                            fileNameList.add(name);
                        }
                    }
                }
                result = fileNameList.toArray(new String[0]);
            }
        }
        return result;
    }

    public static String[] getFileList(String pathname, String endName) {
        File path = new File(pathname);
        return getFileList(path, endName);
    }

    public static boolean createPath(String path) {
        boolean b = true;
        File dir = new File(path);
        if (!dir.exists())
            b = dir.mkdirs();
        return b;
    }

    public static boolean clearFiles(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        return clearFiles(new File(path));
    }

    public static void deleAllFiles(File dir, String endName) {
        if (dir == null || !dir.exists())
            return;
        if (dir.isFile() && dir.getName().endsWith(endName)) {
            dir.delete();
        } else {
            File[] files = dir.listFiles();
            if (files == null)
                return;
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(endName))
                    file.delete(); // 删除所有文件
                else if (file.isDirectory())
                    deleAllFiles(file, endName); // 递规的方式删除文件夹
            }
        }
    }

    public static boolean clearFiles(File dir) {
        boolean b = true;
        if (dir == null || !dir.exists())
            return false;
        if (dir.isFile()) {
            b = dir.delete();
        } else {
            File[] files = dir.listFiles();
            if (files == null)
                b = false;
            else {
                for (File file : files) {
                    if (file.isFile())
                        b = file.delete(); // 删除所有文件
                    else if (file.isDirectory())
                        b = clearFilesAndDir(file); // 递规的方式删除文件夹
                }
            }
        }
        return b;
    }

    public static boolean clearFilesAndDir(File dir) {
        boolean b;
        b = clearFiles(dir);
        if(b)
            b = dir.delete();// 删除目录本身
        return b;
    }

    public static boolean delMergeFiles() {
        boolean b = false;
        File path = new File(MICROMSG_DIR);
        if (isMicroMsgPathExist()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName();
                    if (name.endsWith(".data") || (name.startsWith("shared") && name.endsWith(".xls"))) {
                        b = file.delete();
                    }
                }
            }
        }
        return b;
    }

    public static void clearSameFile(String fileName) {
        File path = new File(MICROMSG_DIR);
        if (isMicroMsgPathExist()) {
            if (path.exists()) {
                File file = new File(path, fileName);
                if (file.exists())
                    file.delete();
            }
        }
    }

    public static boolean clearMicroMSGDownLoadDir() {
        File msgDir = new File(MICROMSG_DIR);
        if (isMicroMsgPathExist() && msgDir.isDirectory()) {
            for (File file : msgDir.listFiles()) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.endsWith(".data")
                            || fileName.endsWith(".itemupdate")
                            || fileName.endsWith(".update")
                            || (fileName.startsWith("shared") && fileName.endsWith(".xls"))) {
                        file.delete();
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMicroMsgPathExist() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File microMsgPath = new File(MICROMSG_DIR);
            if (!microMsgPath.exists()) {
                return microMsgPath.mkdirs();
            }
            return true;
        }
        return false;
    }

    public static boolean unzipAPKFile(File zipFile) throws IOException, JSONException {
        boolean isUpdated = false;
        if (zipFile == null)
            throw new IOException("ZipFile is null ! ");
        FileOutputStream fileOutputStream = null;
        ZipInputStream zipInputStream = null;
        FileInputStream fileInputStream = null;
        fileInputStream = new FileInputStream(zipFile);
        zipInputStream = new ZipInputStream(fileInputStream);
        int count;
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            String fileName = entry.getName();
            if (fileName.equals("output.json")) {
                byte[] versionBuffer = new byte[1024 * 4];
                zipInputStream.read(versionBuffer);
                String jsonString = (new String(versionBuffer)).trim();
                AppUpdateJSONHelper appUpdateJSONHelper = new AppUpdateJSONHelper(jsonString);
                long versionCode = appUpdateJSONHelper.getAPKVersion();
                if (versionCode < AppSetupOperator.getLastAppVersion()) {
                    isUpdated = true;
                    break;
                }
            } else {
                File apkFile = new File(TEMP_DIR, "update.apk");
                if (apkFile.exists())
                    apkFile.delete();
                int bufferLength = 1024 * 8;
                byte[] buffer = new byte[bufferLength];
                fileOutputStream = new FileOutputStream(apkFile);
                while ((count = zipInputStream.read(buffer, 0, bufferLength)) != -1) {
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.flush();
            }
        }
        try {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            zipInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isUpdated;
    }

    public static void writeTxtFile(String content, String path, String fileName) throws IOException {
        File contentFile = new File(path, fileName);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(contentFile))) {
            bufferedWriter.write(content);
            bufferedWriter.flush();
        }
    }

    public static void saveFile(InputStream is, String destDir, String fileName) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(new File(destDir, fileName)));
        int len = -1;
        while ((len = bis.read()) != -1) {
            bos.write(len);
            bos.flush();
        }
        bos.close();
        bis.close();
    }

    /**
     * 读取输入流中的数据保存至指定目录
     *
     * @param is       输入流
     * @param destDir  文件存储目录
     * @param fileName 文件名
     * @param fileList 返回保存的文件列表
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void saveFile(InputStream is, String destDir, String fileName, List<File> fileList) throws FileNotFoundException, IOException {
        File file = new File(destDir, fileName);
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(file));
        int len = -1;
        while ((len = bis.read()) != -1) {
            bos.write(len);
            bos.flush();
        }
        fileList.add(file);
        bos.close();
        bis.close();
    }

    public static String readEncryptFile(File file) throws IOException {
        return readEncryptFile(new FileInputStream(file));
    }


    public static String readEncryptFile(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        byte[] head = new byte[12];
        inputStream.read(head);
        String _head = new String(head);
        if (!EncryptAndDecrypt.MYFILE_HEAD.equals(_head)) {
            return "nothing";
        }
        byte[] buffer = new byte[1024];
        int len = inputStream.read(buffer);
        while (len > 0) {
            stringBuilder.append(new String(buffer, 0, len));
            len = inputStream.read(buffer);
        }
        inputStream.close();
        String s = stringBuilder.toString();
        return EncryptAndDecrypt.decryptPassword(s);
    }

    public static void writeDecryptFile(String content, File file) throws IOException {
        FileOutputStream outputStream = null;
        String _s = EncryptAndDecrypt.MYFILE_HEAD + EncryptAndDecrypt.encryptPassword(content);
        outputStream = new FileOutputStream(file);
        outputStream.write(_s.getBytes());
        outputStream.close();
    }

    public static String readContentText(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String s;
        StringBuilder builder = new StringBuilder();
        while ((s = bufferedReader.readLine()) != null) {
            builder.append(s).append("\n");
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString().trim();
    }

    public static File[] readEmailFile(String cachePath) {
        File dir = new File(cachePath);
        if (dir.exists()) {
            File[] result = new File[2];
            for (File file : dir.listFiles()) {
                String fileName = file.getName();
                if ("release.zip".equalsIgnoreCase(fileName)) {
                    FileOutputStream outputStream = null;
                    try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
                        ZipEntry zipEntry;
                        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                            String name = zipEntry.getName();
                            if ("content.txt".equalsIgnoreCase(name)) {
                                File textFile = new File(cachePath, name);
                                outputStream = new FileOutputStream(textFile);
                                byte[] buffer = new byte[1024 * 8];
                                int count;
                                while ((count = zipInputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, count);
                                    outputStream.flush();
                                }
                                result[0] = textFile;
                            } else {
                                File updateFile = new File(cachePath, name);
                                outputStream = new FileOutputStream(updateFile);
                                byte[] buffer = new byte[1024 * 8];
                                int count;
                                while ((count = zipInputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, count);
                                    outputStream.flush();
                                }
                                result[1] = updateFile;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (outputStream != null)
                                outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
//                    if (fileName.contains("content.txt")) {
//                        result[0] = file;
//                    } else {
//                        result[1] = file;
//                    }
            }
            return result;
        } else {
            return null;
        }
    }

    public static void cleanApp(Context context) {
        FileTools.clearFiles(context.getCacheDir());
        FileTools.clearFiles(context.getFilesDir());
        FileTools.clearMicroMSGDownLoadDir();
        FileTools.clearFiles(ITEMNAME_EXPORT_DIR);
        FileTools.clearFiles(TEMP_DIR);
        FileTools.clearFilesAndDir(new File(EXTERNAL_STORAGE_DIR, "MyBackup"));
        FileTools.clearFilesAndDir(new File(EXTERNAL_STORAGE_DIR, "ExportItemNameFile"));
    }

    public static void initialAPPFile() {
        createPath(TEMP_DIR);
        clearFiles(TEMP_DIR);
    }
}
