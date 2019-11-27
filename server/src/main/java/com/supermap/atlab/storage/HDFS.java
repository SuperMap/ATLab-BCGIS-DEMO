package com.supermap.atlab.storage;

//import com.supermap.atlab.Utils;
//import org.apache.commons.io.IOUtils;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.FSDataOutputStream;
//import org.apache.hadoop.fs.FileSystem;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URI;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.logging.Logger;

public class HDFS {
//    private Logger logger = Logger.getLogger(HDFS.class.toString());
//    private final String ipAddress = "hdfs://172.16.15.65:9000";
//    private final String hdfsStorePath = "/user/bcgis/";
//    private FileSystem fs = null;
//    private String userName = "java";
//
//    public void HDFS() {
//        Configuration conf = new Configuration();
//        conf.set("bcgis", ipAddress);
//        conf.set("dfs.replication", "3");
//        try {
//            fs = FileSystem.get(new URI(ipAddress), conf, userName);
//        }catch (Exception e){
//        }
//    }
//
//    public String upload(InputStream inputStream, String fileExtName, String fileName) throws IOException {
//        String hash = Utils.getSHA256(fileName);
//        System.out.println("hash:" + hash );
//        String uploadPath = hdfsStorePath + hash + fileExtName;
//        String destpath = ipAddress + uploadPath;
//        org.apache.hadoop.fs.Path dst = new org.apache.hadoop.fs.Path(destpath);
//        FSDataOutputStream os = fs.create(dst);
//        IOUtils.copy(inputStream, os);
//        fs.close();
//        logger.info("sucessful upload file !");
//        return  hash + fileExtName;
//    }
//
//    public boolean download(String fileName, String storeLocalPath) throws JSONException {
//        String fileExtName = Utils.getExtName(fileName);
//        String downloadPath = ipAddress + hdfsStorePath + fileName + fileExtName;
//        try {
//            fs.copyToLocalFile(new org.apache.hadoop.fs.Path(downloadPath), new org.apache.hadoop.fs.Path(storeLocalPath));
//            fs.close();
//        }catch(Exception e){
//        }
//        logger.info("sucessful download file !");
//        return Files.exists(Paths.get(storeLocalPath));
//    }
//
//    public String delete(String deletePath, String fileExtName) throws JSONException {
//        deletePath = "/user/bcgis/"+ deletePath + fileExtName;
//        try {
//            fs.deleteOnExit(new org.apache.hadoop.fs.Path(deletePath));
//            fs.close();
//        }catch (Exception e){
//        }
//        logger.info("sucessful delete file");
//        return deletePath;
//    }

//    private String hdfsUploadFile(InputStream fileInputStream, String fileExtName, String hash) throws IOException {
//        FileSystem fs = new HDFS().getFs();
//        hash = Utils.getSHA256(hash);
//        System.out.println("hash:" + hash );
//        String uploadPath = hdfsStorePath + hash + fileExtName;
//        String destpath = ipAddress + uploadPath;
//        org.apache.hadoop.fs.Path dst = new org.apache.hadoop.fs.Path(destpath);
//        FSDataOutputStream os = fs.create(dst);
//        IOUtils.copy(fileInputStream, os);
//        fs.close();
//        logger.info("sucessful upload file !");
//        return  hash + fileExtName;
//    }
//
//    private void hdfsDownloadFile(String storeLocalPath, String downloadPath){
//        FileSystem fs = new HDFS().getFs();
//        downloadPath = ipAddress + hdfsStorePath + downloadPath;
//        try {
//            fs.copyToLocalFile(new org.apache.hadoop.fs.Path(downloadPath), new org.apache.hadoop.fs.Path(storeLocalPath));
//            fs.close();
//        }catch(Exception e){
//        }
//        logger.info("sucessful download file !");
//    }
//
//    private String hdfsDeleteFile(String delete_Path, String fileExtName){
//        FileSystem fs = new HDFS().getFs();
//        delete_Path = "/user/bcgis/"+ delete_Path + fileExtName;
//        try {
//            fs.deleteOnExit(new org.apache.hadoop.fs.Path(delete_Path));
//            fs.close();
//        }catch (Exception e){
//        }
//        logger.info("sucessful delete file");
//        return delete_Path;
//    }
}
