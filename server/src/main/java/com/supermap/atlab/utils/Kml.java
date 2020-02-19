package com.supermap.atlab.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Kml {
    /**
     * 解析 kml 文件，然后将 kml 文件名赋予给对应的 s3m 文件 ，最后保存在指定的文件夹里面
     * @param filePath
     * @param saveFilePath
     * @return
     */
    public static String parsingKmlToModifyS3mFileName(String filePath, String saveFilePath) {
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
        // 第三步 根据 kml 文件修改 s3m 文件名
        for (int i = 0; i < kmlFileNameList.size(); i++) {
            String tempSID = kmlFileNameList.get(i);
            String SID = tempSID.substring(0, tempSID.lastIndexOf('.'));
            String tempS3mPath = s3mNameList.get(i);
            String s3mPath = tempS3mPath.substring(1);
            String absoluteS3mFilePath = filePath + s3mPath;
            String fileExtName = Utils.getExtNameFromStr(s3mPath);
            try {
                if (Files.exists(Paths.get(saveFilePath + File.separator + SID + fileExtName))) {
                    continue;
                }
                Files.copy(Paths.get(absoluteS3mFilePath), Paths.get(saveFilePath + File.separator + SID + fileExtName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "the save file location is : " + saveFilePath;
    }

    /**
     * 根据 s3m 文件路径获取其信息，将其保存为 JsonArray 输出
     * 返回值：Json
     */
    public static JSONArray readS3m(String modelID, String filePath) {
        File file = new File(filePath);
        String[] fileName = file.list();
        String hash = null;
        int i = 0;
        JSONArray jsonArray = new JSONArray();
        for(String str : fileName){
            File tempFile = new File(filePath + File.separator + str);
            try{
                // TODO 将 s3m 文件存储到 hdfs
//                FileInputStream inHdfs = new FileInputStream(tempFile);
//                hdfs.hdfsUploadFile(inHdfs, fileExtName, hash);

                FileInputStream in = new FileInputStream(tempFile);
                hash = Utils.getSHA256(Utils.inputStreamToByteArray(in));
                JSONArray jsonHash = new JSONArray();
                jsonHash.add(hash);
                String SID = str.substring(0, str.lastIndexOf('.'));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("MID", modelID);
                jsonObject.put("SID", SID);
                jsonObject.put("SHash", jsonHash);
                jsonArray.add(i, jsonObject);
                i++;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonArray ;
    }

    /**
     * 解析 kml 文件
     *
     * @param file
     * @param list
     * @return
     */
    private static JSONObject creatNodeList(File file, List<String> list) {
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

    private static JSONObject node(NodeList list) {
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
