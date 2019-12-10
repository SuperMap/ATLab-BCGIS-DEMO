package com.supermap.atlab.storage;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class Hdfs {
    private final String ipAddress = "hdfs://172.16.15.65:9000";
    private final String hdfsStorePath = "/user/bcgis/";
    private FileSystem fs = null;
    private String userName = "java";

    private FileSystem getFs() {
        Configuration conf = new Configuration();
        conf.set("bcgis",ipAddress);
        conf.set("dfs.replication", "3");
        try {
            fs = FileSystem.get(new URI(ipAddress), conf, userName);
        }catch (Exception e){
        }
        return fs;
    }

    public String hdfsUploadFile(InputStream fileInputStream, String fileExtName, String hash)  {
        FileSystem fs = getFs();
        String uploadPath = hdfsStorePath + hash + fileExtName;
        String destpath = ipAddress + uploadPath;
        org.apache.hadoop.fs.Path dst = new org.apache.hadoop.fs.Path(destpath);
        FSDataOutputStream os = null;
        try {
            os = fs.create(dst);
            IOUtils.copy(fileInputStream, os);
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  hash + fileExtName;
    }

    public void hdfsDownloadFile(String storeLocalPath, String downloadPath){
        FileSystem fs = getFs();
        downloadPath = ipAddress + hdfsStorePath + downloadPath;
        try {
            fs.copyToLocalFile(new org.apache.hadoop.fs.Path(downloadPath), new org.apache.hadoop.fs.Path(storeLocalPath));
            fs.close();
        }catch(Exception e){
        }
    }

    public String hdfsDeleteFile(String delete_Path, String fileExtName){
        FileSystem fs = getFs();
        delete_Path = hdfsStorePath + delete_Path + fileExtName;
        try {
            fs.deleteOnExit(new org.apache.hadoop.fs.Path(delete_Path));
            fs.close();
        }catch (Exception e){
        }
        return delete_Path;
    }
}
