package com.supermap.atlab.blockchain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.sdk.ATLChain;
import com.supermap.atlab.storage.Hdfs;
import com.supermap.atlab.utils.Kml;
import com.supermap.atlab.utils.Utils;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Path("/blockchain")
public class Blockchain {

    private ATLChain atlChain;

    //        final private File networkConfigFile = new File("/home/cy/Documents/ATL/SuperMap/ATLab-examples/server/src/main/resources/network-config-test.yaml");
//    final private String s3mDirPath = "/home/cy/Documents/ATL/SuperMap/ATLab-examples/server/target/server/s3m/";
    final private String s3mDirPath = "E:\\DemoRecording\\A_SuperMap\\ATLab-examples\\server\\target\\server\\s3m\\";
    final private File networkConfigFile = new File("E:\\DemoRecording\\A_SuperMap\\ATLab-examples\\server\\src\\main\\resources\\network-config-test.yaml");

    //    final private File networkConfigFile = new File(/this.getClass().getResource("/network-config-test.yaml").getPath());
    final private String chaincodeName = "bimcc";

    public Blockchain() {
        atlChain = new ATLChain(networkConfigFile);
    }

    @GET
    public String GetRecord(
            @QueryParam("modelid") String key
    ) {
//        String key = "modelidaa-sidaDa"; // "model002-doorl1";
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
        Hdfs hdfs = new Hdfs();
        JSONArray jsonArraySHash = new JSONArray();
        JSONArray jsonArrayS3m = new JSONArray();
        List<BodyPart> bodyParts = formDataMultiPart.getBodyParts();
        bodyParts.forEach(o -> {
            String mediaType = o.getMediaType().toString();
            String extName = "";
            if (!mediaType.equals(MediaType.TEXT_PLAIN)) {
                BodyPartEntity bodyPartEntity = (BodyPartEntity) o.getEntity();
                String fileName = o.getContentDisposition().getFileName();
                if (fileName.contains(".")) {
                    jsonArrayS3m.add(fileName.substring(0, fileName.lastIndexOf('.')));
                    extName = Utils.getExtNameFromStr(fileName);
                } else {
                    jsonArrayS3m.add(fileName);
                }
                InputStream inputStream = bodyPartEntity.getInputStream();
                String hash = null;
                try {
                    hash = Utils.getSHA256(Utils.inputStreamToByteArray(inputStream));
                    jsonArraySHash.add(hash);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                InputStream in = bodyPartEntity.getInputStream();
                // hdfs存储
                hdfs.hdfsUploadFile(in, extName, hash);
                // 保存本地文件
//                Utils.saveFile(in, s3mDirPath + hash + extName);
            }
        });
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < jsonArrayS3m.size(); i++){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("MID", modelid);
            jsonObject.put("SID", jsonArrayS3m.get(i));
            jsonObject.put("SHash", jsonArraySHash.get(i));
            jsonArray.add(i, jsonObject);
        }
        // 保存单个模块
        JSONArray jsonToAll = saveSingleS3m(modelid, jsonArray);
//        System.out.println(jsonToAll);
        //保存完整模块
        saveALLS3mMoudle(modelid, jsonToAll);
        return "save file success";
    }

    @Path("/history")
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

    @Path("/selector")
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
                if (!Files.exists(Paths.get(s3mDirPath, hashObj.toString()))) {
                    System.out.println(hashObj + "不存在，需要下载");
                    if (!getFileFromHdfs()) {
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

    /**
     * 测试 saveSingleS3m && saveALLS3mMoudle
     *
     * @param modelid
     */
    public void storageS3mFile(String modelid) {
        // TODO 单个与整体的存储测试
//        JSONArray jsonArrayS3m = Kml.readS3m("modelidaa", "E:\\SuperMapData\\test\\saveTest");
//        System.out.println(jsonArrayS3m);
//        JSONArray jsonToAll = saveSingleS3m(modelid, jsonArrayS3m);
//        System.out.println(jsonToAll);
//        saveALLS3mMoudle(modelid, jsonToAll);
        // TODO 单个与整体的删除测试
//        File file = new File("E:\\SuperMapData\\test\\saveTest");
//        String[] fileName = file.list();
//        List<String> deleteList = new ArrayList<>();
//        for(String str : fileName){
//            deleteList.add(str.substring(0, str.lastIndexOf(".")));
//        }
//        JSONArray deleteJson = deleteSingleS3m(modelid, deleteList);
//        deletesALLS3mMoudle(modelid, deleteJson);
    }

    /**
     * 存储单个 s3m 信息，并返回需要修改的值 ---->修改整体存储
     * @param modelid
     * @param jsonArrayS3m
     * @return
     */
    private JSONArray saveSingleS3m(String modelid, JSONArray jsonArrayS3m) {
        JSONArray jsonArray = new JSONArray();
        String getRecord = "GetRecord";
        String putRecord = "PutRecord";
        for (Object object : jsonArrayS3m) {
            JSONObject jsonObject = (JSONObject) object;
            String SID = (String) jsonObject.get("SID");
            String modifySHash = (String) jsonObject.get("SHash");
            String key = modelid + "-" + SID;
            String result = atlChain.query(
                    chaincodeName,
                    getRecord,
                    new String[]{key}
            );
            if (result.length() != 0) {
                JSONObject jsonResult = JSONObject.parseObject(result);
                String oldSHash = (String) jsonResult.get("SHash");
                if (!modifySHash.equals(oldSHash)) {
                    String value = jsonObject.toString().replace(oldSHash, modifySHash);
                    String newResult = atlChain.invoke(
                            chaincodeName,
                            putRecord,
                            new String[]{key, value}
                    );
                    // 将修改之前的 hash 和修改之后的都要传入到整体信息里面
                    JSONObject temp = new JSONObject();
                    temp.put("old", oldSHash);
                    temp.put("modify", modifySHash);
                    jsonArray.add(temp);
                }
            } else {
                String value = jsonObject.toString();
                String newResult = atlChain.invoke(
                        chaincodeName,
                        putRecord,
                        new String[]{key, value}
                );
                JSONObject temp = new JSONObject();
                temp.put("old", "empty");
                temp.put("modify", modifySHash);
                jsonArray.add(temp);
            }
        }
        JSONObject jsonToAll = new JSONObject();
        jsonToAll.put("SHash", jsonArray);
//        System.out.println(jsonArray);
        return jsonArray;
    }

    /**
     * 模型整体信息存储
     * @param modelid
     * @param jsonArrayS3m
     */
    private void saveALLS3mMoudle(String modelid, JSONArray jsonArrayS3m) {
        String getRecord = "GetRecord";
        String putRecord = "PutRecord";
        String result = atlChain.query(
                chaincodeName,
                getRecord,
                new String[]{modelid}
        );
        JSONArray jsonArray = new JSONArray();
        if (result.length() != 0) {
            JSONObject jsonResult = JSONObject.parseObject(result);
            String hash = jsonResult.get("SHash").toString();
            jsonArray = JSONArray.parseArray(hash);
        }
        JSONArray newJsonArray = (JSONArray) jsonArray.clone();
        for (Object json : jsonArrayS3m) {
            JSONObject temp = (JSONObject) JSONObject.parse(json.toString());
            String oldHash = temp.get("old").toString();
            String modifyHash = temp.get("modify").toString();
            // 该信息不为空 即代表之前存储过信息 ----> 分为修改和添加
            if (result.length() != 0) {
                if (oldHash.equals("empty")) {
                    newJsonArray.add(modifyHash);
                } else {
                    newJsonArray.remove(oldHash);
                    newJsonArray.add(modifyHash);
                }
            } else {
                newJsonArray.add(modifyHash);
            }
        }
        if( !jsonArray.equals(newJsonArray) || result.length() == 0){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("SHash", newJsonArray);
            String newValue = jsonObject.toString();
//            System.out.println("newValue" + newValue);
            atlChain.invoke(
                    chaincodeName,
                    putRecord,
                    new String[]{modelid, newValue}
            );
        }
    }

    /**
     * 删除单个 s3m 信息
     * @param modelid
     * @param listKey
     * @return
     */
    private JSONArray deleteSingleS3m(String modelid, List<String> listKey) {
        String getRecord = "GetRecord";
        String delRecord = "DelRecord";
        JSONArray deleteJson = new JSONArray();
        String key = null;
        for (String str : listKey) {
            key = modelid + "-" + str;
            String result = atlChain.query(
                    chaincodeName,
                    getRecord,
                    new String[]{key}
            );
            if (result.length() != 0) {
                JSONObject jsonResult = JSONObject.parseObject(result);
                String tempHash = (String) jsonResult.get("SHash");
                deleteJson.add(tempHash);
            }
//            atlChain.query(
//                    chaincodeName,
//                    delRecord,
//                    new String[]{key}
//            );
        }
        return deleteJson;
    }

    /**
     * 模型整体信息改变
     * @param modelid
     * @param deleteJson
     */
    private void deletesALLS3mMoudle(String modelid, JSONArray deleteJson){
        String getRecord = "GetRecord";
        String result = atlChain.query(
                chaincodeName,
                getRecord,
                new String[]{modelid}
        );
        JSONObject jsonResult = JSONObject.parseObject(result);
        String hash = jsonResult.get("SHash").toString();
        JSONArray jsonArray = JSONArray.parseArray(hash);
        for(Object o : deleteJson){
            jsonArray.remove(o);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("SHash", jsonArray.toString());
        String newValue = jsonObject.toString();
        System.out.println(newValue);
        System.out.println(jsonArray.size());
//        atlChain.query(
//                chaincodeName,
//                putRecord,
//                new String[]{modelid, newValue}
//        );
    }
}
