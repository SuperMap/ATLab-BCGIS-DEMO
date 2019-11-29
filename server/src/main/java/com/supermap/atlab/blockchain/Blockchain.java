package com.supermap.atlab.blockchain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.sdk.ATLChain;
import com.supermap.atlab.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Path("/blockchain")
public class Blockchain {

    private ATLChain atlChain;
    //    final private File networkConfigFile = new File("/home/cy/Documents/ATL/SuperMap/ATLab-examples/server/src/main/resources/network-config-test.yaml");
    final private File networkConfigFile = new File("E:\\DemoRecording\\A_SuperMap\\ATLab-examples\\server\\src\\main\\resources\\network-config-test.yaml");


    //    final private File networkConfigFile = new File(/this.getClass().getResource("/network-config-test.yaml").getPath());
    final private String chaincodeName = "bimcc";

    public Blockchain() {
        atlChain = new ATLChain(networkConfigFile);
    }

    @GET
    public String GetRecord() {
        String key = "model001";
        String functionName = "GetRecord";

        String result = atlChain.query(
                chaincodeName,
                functionName,
                new String[]{key}
        );
        return result;
    }

    @POST
    public String PutRecord() {
        File file = new File("E:\\SuperMapData\\test\\testBim");
        String filePath = "E:\\SuperMapData\\test\\testBim";
        JSONArray jsonArray = readFile(filePath, "E:\\SuperMapData\\test");

//        String key = "model001";
//        String functionName = "PutRecord";
//        String value = "\"GeoModel3D_0000000059470CB0.s3m\", \"GeoModel3D%23_514788146.s3m\", \"GeoModel3D_00000000401ABD30.s3m\", \"GeoModel3D_0000000059470B00.s3m\", \"GeoModel3D_00000000401AB6E0.s3m\", \"GeoModel3D%23_515870026.s3m\", \"GeoModel3D_000000005B306470.s3m\", \"GeoModel3D_00000000401AAA80.s3m\", \"GeoModel3D%23_515498919.s3m\", \"GeoModel3D_00000000401AC350.s3m\", \"GeoModel3D_00000000589B4A60.s3m\", \"GeoModel3D%23_515435356.s3m\", \"GeoModel3D_000000005B383EA0.s3m\", \"GeoModel3D_000000005B2CEDD0.s3m\", \"GeoModel3D_000000005B17AC00.s3m\", \"GeoModel3D%23_515748021.s3m\", \"GeoModel3D_000000003F8A7C70.s3m\", \"GeoModel3D%23_514864874.s3m\", \"GeoModel3D_000000005B305E90.s3m\", \"GeoModel3D_00000000583FDAB0.s3m\", \"GeoModel3D_000000005B306240.s3m\", \"GeoModel3D_000000005B2F29C0.s3m\", \"GeoModel3D_000000003F8A4290.s3m\", \"GeoModel3D_000000005B386AC0.s3m\", \"GeoModel3D_00000000401A94B0.s3m\", \"GeoModel3D_000000005B306450.s3m\", \"GeoModel3D_00000000401AC1B0.s3m\", \"GeoModel3D_0000000040126AC0.s3m\", \"GeoModel3D_000000005B17A6E0.s3m\", \"GeoModel3D_0000000058AD9BE0.s3m\", \"GeoModel3D_0000000040111230.s3m\", \"GeoModel3D%23_515652338.s3m\", \"GeoModel3D_000000005B2CEF70.s3m\", \"GeoModel3D%23_515394098.s3m\", \"GeoModel3D_00000000401A8600.s3m\", \"GeoModel3D%23_515362609.s3m\", \"GeoModel3D%23_514665277.s3m\", \"GeoModel3D%23_515605045.s3m\", \"GeoModel3D%23_514521583.s3m\", \"GeoModel3D_0000000059AFC230.s3m\", \"GeoModel3D_0000000058ADA250.s3m\"";
//        String result = atlChain.invoke(
//                chaincodeName,
//                functionName,
//                new String[]{key, value}
//        );


        String result =null;
        for(int i = 0; i < jsonArray.size(); i++){
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String key = jsonObject.get("MID").toString() +  "-"  + jsonObject.get("SID").toString();
            String functionName = "PutRecord";
            String value = jsonArray.get(i).toString();
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
    public String GetHistory() {
        String key = "model001";
        String functionName = "GetHistoryByKey";

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
     *
     */
    public JSONArray readFile(String filePath, String saveFileSting){
        // 第一步 得到该文件下所有文件名
        File file = new File(filePath);
        String [] fileName = file.list();
        List<String> kmlFileNameList = new ArrayList<>();
        for(String str : fileName){
            if(str.equals("KMLModels")){
                continue;
            }else {
                kmlFileNameList.add(str);
            }
        }
        // 第二步 解析 kml 文件，拿到 s3m 的 name 属性
        List<String> s3mNameList = new ArrayList<>();
        List<String> list = new ArrayList();
        for(int i = 0; i < kmlFileNameList.size(); i++) {
            String kmlFilePath = filePath + File.separator + kmlFileNameList.get(i);
            list.add("Link");
            JSONObject jsonObject = creatNodeList(new File(kmlFilePath), list);
            s3mNameList.add(jsonObject.get("href").toString());
        }
        // 第三步 根据信息生成 Json 字符串
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < kmlFileNameList.size(); i++){
            String tempSID = kmlFileNameList.get(i);
            String SID = tempSID.substring(0, tempSID.lastIndexOf('.'));
            String tempS3mPath = s3mNameList.get(i);
            String s3mPath = tempS3mPath.substring(1, tempS3mPath.length());
            String absoluteS3mFilePath = filePath + s3mPath;
            File tmp = new File(absoluteS3mFilePath);
            String hash = null;
            try {
                FileInputStream in = new FileInputStream(tmp);
                hash = Utils.getSHA256(Utils.inputStreamToString(in));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("MID", "model002");
            jsonObject.put("SID", SID);
            jsonObject.put("SHash", hash);
            jsonArray.add(i, jsonObject);
            //TODO 这里需添加一步，在根据 s3m 文件计算 hash 时就应该将文件以 hash 为名存储到 hdfs
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
     * @param file
     * @param list
     * @return
     */
    public JSONObject creatNodeList(File file, List<String> list){
        JSONObject jsonObject = new JSONObject();
        for(int i = 0; i < list.size(); i++) {
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

    public JSONObject node(NodeList list) {
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
