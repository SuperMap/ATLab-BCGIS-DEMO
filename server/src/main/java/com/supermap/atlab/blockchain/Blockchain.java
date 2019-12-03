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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//import com.supermap.atlab.storage.Hdfs;

@Path("/blockchain")
public class Blockchain {

    private ATLChain atlChain;
    final private File networkConfigFile = new File("/home/cy/Documents/ATL/SuperMap/ATLab-examples/server/src/main/resources/network-config-test.yaml");
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
            @FormDataParam("s3mid") String s3mid,
            FormDataMultiPart formDataMultiPart
    ) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        List<BodyPart> bodyParts = formDataMultiPart.getBodyParts();
        bodyParts.forEach(o -> {
//            String name = o.getContentDisposition().getParameters().get("name");
            String mediaType = o.getMediaType().toString();

            if (!mediaType.equals(MediaType.TEXT_PLAIN)) {
//                String fileName = o.getContentDisposition().getFileName();
                BodyPartEntity bodyPartEntity = (BodyPartEntity) o.getEntity();
                InputStream inputStream = bodyPartEntity.getInputStream();
                String hash = null;
                try {
                    hash = Utils.getSHA256(Utils.inputStreamToByteArray(inputStream));
                    jsonArray.add(hash);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // TODO hdfs存储
                // 保存文件
                Utils.saveFile(inputStream, "/home/cy/Documents/ATL/SuperMap/ATLab-examples/server/target/server/s3m/" + hash);
            }
        });

        String functionName = "PutRecord";
        String key = modelid + "-" + s3mid;
        jsonObject.put("MID", modelid);
        jsonObject.put("SID", s3mid);
        jsonObject.put("SHash", jsonArray);

        // example value:
        // {"SHash":["b4ed34caf6b47ab999900760cb15f8b61b50560605291f65d1d52fcc155bd770","1cbab4479058ddc075f39938444dd516f15099d57597f725942b71e6bc11e994","b4ed34caf6b47ab999900760cb15f8b61b50560605291f65d1d52fcc155bd770","1cbab4479058ddc075f39938444dd516f15099d57597f725942b71e6bc11e994"],"MID":"modelid","SID":"sid"}
        String value = jsonObject.toString();
        String result = atlChain.invoke(
                chaincodeName,
                functionName,
                new String[]{key, value}
        );

        return result;
    }

    @Path("history")
    @GET
    public String GetHistory(
            @QueryParam("modelid") String key
    ) {
//        String key = "modelidaa-sidaa";
        String functionName = "GetHistoryByKey2";

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

    /**
     * 根据文件夹路径解析 kml 文件，然后将 kml 文件名赋予给对应的 s3m 文件 ，最后保存在指定的文件夹里面
     * 返回值：Json
     */
    private JSONArray readFile(String filePath, String saveFileSting) {
        String modelID = "model002";
        // 第一步 得到该文件下所有文件名
        File file = new File(filePath);
        String[] fileName = file.list();
        List<String> kmlFileNameList = new ArrayList<>();
        for (String str : fileName) {
            if (str.contains(".") && ".kml".equals(str.substring(str.lastIndexOf('.')))) {
                kmlFileNameList.add(str);
            }
        }
        // 第二步 解析 kml 文件，拿到 s3m 的 name 属性
        List<String> s3mNameList = new ArrayList<>();
        List<String> list = new ArrayList();
        for (int i = 0; i < kmlFileNameList.size(); i++) {
            String kmlFilePath = filePath + File.separator + kmlFileNameList.get(i);
            list.add("Link");
            JSONObject jsonObject = creatNodeList(new File(kmlFilePath), list);
            s3mNameList.add(jsonObject.get("href").toString());
        }
        // 第三步 根据 s3m 文件信息生成 Json 字符串
//        Hdfs hdfs = new Hdfs();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < kmlFileNameList.size(); i++) {
            String tempSID = kmlFileNameList.get(i);
            String SID = tempSID.substring(0, tempSID.lastIndexOf('.'));
            String tempS3mPath = s3mNameList.get(i);
            String s3mPath = tempS3mPath.substring(1);
            String absoluteS3mFilePath = filePath + s3mPath;
            File tmp = new File(absoluteS3mFilePath);
            String hash = null;
            String fileExtName = Utils.getExtName(s3mPath);
            try {
                FileInputStream in = new FileInputStream(tmp);
//                FileInputStream inHdfs = new FileInputStream(tmp);
                hash = Utils.getSHA256(Utils.inputStreamToByteArray(in));

                // 将 s3m 文件存储到 hdfs
//                hdfs.hdfsUploadFile(inHdfs, fileExtName, hash);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("MID", modelID);
            jsonObject.put("SID", SID);
            jsonObject.put("SHash", hash);
            jsonArray.add(i, jsonObject);

            try {
                if (Files.exists(Paths.get(saveFileSting + "/" + hash))) {
                    continue;
                }
                Files.copy(Paths.get(absoluteS3mFilePath), Paths.get(saveFileSting + "/" + hash));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        System.out.println(jsonArray);
        return jsonArray;
    }

    // 给文件的绝对路径  修改成新的文件名
    private String fixFileName(String filePath, String newFileName) {
        File f = new File(filePath);
        if (!f.exists()) {
            return null;
        }
        newFileName = newFileName.trim();
        if ("".equals(newFileName) || newFileName == null) // 文件名不能为空
            return null;
        String newFilePath = null;
        if (f.isDirectory()) { // 判断是否为文件夹
            newFilePath = filePath.substring(0, filePath.lastIndexOf("/")) + "/" + newFileName;
        } else {
            newFilePath = filePath.substring(0, filePath.lastIndexOf("/")) + "/" + newFileName
                    + filePath.substring(filePath.lastIndexOf("."));
        }
        File nf = new File(newFilePath);
        try {
            f.renameTo(nf); // 修改文件名
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
        return newFilePath;
    }

    /**
     * 解析 kml 文件
     *
     * @param file
     * @param list
     * @return
     */
    private JSONObject creatNodeList(File file, List<String> list) {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < list.size(); i++) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            NodeList nodeList = null;
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document d = builder.parse(file);
                nodeList = d.getElementsByTagName(list.get(i));
                JSONObject jsonTemp = node(nodeList);
                jsonObject.putAll(jsonTemp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    private JSONObject node(NodeList list) {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    try {
                        jsonObject.put(childNodes.item(j).getNodeName(), childNodes.item(j).getFirstChild().getNodeValue());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return jsonObject;
    }
}
