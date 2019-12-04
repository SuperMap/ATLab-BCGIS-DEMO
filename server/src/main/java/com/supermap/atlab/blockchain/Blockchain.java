package com.supermap.atlab.blockchain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.sdk.ATLChain;
import com.supermap.atlab.Utils;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//import com.supermap.atlab.storage.Hdfs;

@Path("/blockchain")
public class Blockchain {

    private ATLChain atlChain;
    final private File networkConfigFile = new File("/home/cy/Documents/ATL/SuperMap/ATLab-examples/server/src/main/resources/network-config-test.yaml");
    final private String s3mDirPath = "/home/cy/Documents/ATL/SuperMap/ATLab-examples/server/target/server/s3m/";
    //    final private File networkConfigFile = new File("E:\\DemoRecording\\A_SuperMap\\ATLab-examples\\server\\src\\main\\resources\\network-config-test.yaml");

    //    final private File networkConfigFile = new File(/this.getClass().getResource("/network-config-test.yaml").getPath());
    final private String chaincodeName = "bimcc";

    public Blockchain() {
        atlChain = new ATLChain(networkConfigFile);
    }

    @GET
    public String GetRecord(
            @QueryParam("modelid") String key
    ) {
//        String key = "modelidaa-sidaa"; // "model002-doorl1";
        String functionName = "GetRecord";

        String result = atlChain.query(
                chaincodeName,
                functionName,
                new String[]{key}
        );
        return result;
    }

    // TODO 接收json参数
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String PutRecord(
            @FormDataParam("modelid") String modelid,
            FormDataMultiPart formDataMultiPart
    ) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArraySHash = new JSONArray();
        JSONArray jsonArrayS3m = new JSONArray();
        List<BodyPart> bodyParts = formDataMultiPart.getBodyParts();
        bodyParts.forEach(o -> {
            String mediaType = o.getMediaType().toString();
            if (!mediaType.equals(MediaType.TEXT_PLAIN)) {
                BodyPartEntity bodyPartEntity = (BodyPartEntity) o.getEntity();
                String fileName = o.getContentDisposition().getFileName();
                jsonArrayS3m.add(fileName.substring(0, fileName.lastIndexOf('.')));
                InputStream inputStream = bodyPartEntity.getInputStream();
                String hash = null;
                try {
                    hash = Utils.getSHA256(Utils.inputStreamToByteArray(inputStream));
                    jsonArraySHash.add(hash);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // TODO hdfs存储
                // 保存文件
                Utils.saveFile(inputStream, s3mDirPath + hash);
            }
        });

        String functionName = "PutRecord";
        jsonObject.put("SHash", jsonArraySHash);
        String value = jsonObject.toString();

        // 保存完整模型记录
        String result = atlChain.invoke(
                chaincodeName,
                functionName,
                new String[]{modelid, value}
        );

        // 保存单个s3m记录
        for (int i = 0; i < jsonArrayS3m.size(); i++) {
            String key = modelid + "-" + jsonArrayS3m.get(i);
            jsonObject = new JSONObject();
            jsonObject.put("MID", modelid);
            jsonObject.put("SID", jsonArrayS3m.get(i));
            jsonObject.put("SHash", jsonArraySHash.get(i));
            value = jsonObject.toString();

            result = atlChain.invoke(
                    chaincodeName,
                    functionName,
                    new String[]{key, value}
            );
        }

        return result;
    }

    @Path("history")
    @GET
    public String GetHistory(
            @QueryParam("key") String key
    ) {
//        String key = "modelidaa-sidaa";
        String functionName = "GetHistoryByKey";

        String result = atlChain.query(
                chaincodeName,
                functionName,
                new String[]{key}
        );
        return result;
    }

    @Path("selector")
    @GET
    public String GetRecordBySelector(
            @QueryParam("modelid") String modelid,
            @QueryParam("s3mid") String s3mid

    ) {
        String selector = "";
        if (s3mid == null) {
            selector = "{\"MID\":\"" + modelid + "\"}";
        } else {
            selector = "{\"MID\":\"" + modelid + "\",\"SID\":\"" + s3mid + "\"}";
        }
        String functionName = "GetRecordBySelector";

        String result = atlChain.query(
                chaincodeName,
                functionName,
                new String[]{selector}
        );

        // 获取s3m文件名
        JSONArray resultJsonArray = JSONArray.parseArray(result);
        resultJsonArray.get(0);
        for (Object o : resultJsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            JSONObject recordJSON = (JSONObject) jsonObject.get("Record");
            JSONArray shashList = (JSONArray) recordJSON.get("SHash");
            for (Object hashObj : shashList) {
                if(!Files.exists(Paths.get(s3mDirPath, hashObj.toString()))) {
                    System.out.println(hashObj + "不存在，需要下载");
                    if (!getFileFromHdfs()){
                        return "{\"message\":\"" + hashObj + "不存在\"}";
                    }
                }
            }
        }

        // 直接返回区块链查询结果，具体使用由客户端处理
        return result;
    }

    @DELETE
    public String DelRecord(
            @QueryParam("modelid") String key
    ) {
        String functionName = "DelRecord";

        String result = atlChain.query(
                chaincodeName,
                functionName,
                new String[]{key}
        );
        return result;
    }

    private boolean getFileFromHdfs() {
        return false;
    }
}
