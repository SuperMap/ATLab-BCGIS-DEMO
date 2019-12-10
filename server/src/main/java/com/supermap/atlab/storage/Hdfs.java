package com.supermap.atlab.storage;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class Hdfs {
    private final String ipAddress = "hdfs://172.16.15.65:9000";
    private final String hdfsStorePath = "/user/bcgis/";

    private FileSystem getFs() {
        Configuration conf = new Configuration();
        conf.set("bcgis", ipAddress);
        conf.set("dfs.replication", "3");
        FileSystem fs = null;
        try {
            String userName = "java";
            fs = FileSystem.get(new URI(ipAddress), conf, userName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return fs;
    }

    public String hdfsUploadFile(InputStream inputStream, String fileName) {
        FileSystem fs = getFs();
        String uploadPath = hdfsStorePath + fileName;
        String destpath = ipAddress + uploadPath;
        Path dst = new Path(destpath);
        FSDataOutputStream os = null;
        int count = 0;
        try {
            // HDFS 中没有该文件
//            if (fs.exists(dst)) {
//                return "File Already Exists In HDFS";
//            }

            os = fs.create(dst);
            IOUtils.copy(inputStream, os);
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Success";
    }

    public String hdfsDownloadFile(String remotePath, String localPath) {
        remotePath = hdfsStorePath + remotePath;

        try {
            FileSystem fs = getFs();
            // HDFS 中没有该文件
            if (!fs.exists(new Path(remotePath))) {
                return "File Not Exists In HDFS";
            }
            fs.copyToLocalFile(new Path(remotePath), new Path(localPath));
            fs.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return "Success";
    }

    public String hdfsDeleteFile(String deletePath) {
        deletePath = hdfsStorePath + deletePath;

        try {
            FileSystem fs = getFs();
            // HDFS 中没有该文件
            if (!fs.exists(new Path(deletePath))) {
                return "File Not Exists In HDFS";
            }
            fs.deleteOnExit(new Path(deletePath));
            fs.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return "Success";
    }
}
