package com.supermap.atlab.storage;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Logger;

public class Hdfs {

    private Logger logger = Logger.getLogger(Hdfs.class.toString());
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
        FileSystem fs = new Hdfs().getFs();
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
//        logger.info("sucessful upload file !");
        return  hash + fileExtName;
    }
}
