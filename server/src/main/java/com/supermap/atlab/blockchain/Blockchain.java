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
//    final private File networkConfigFile = new File("/home/cy/Documents/ATL/SuperMap/ATLab-examples/server/src/main/resources/network-config-test.yaml");
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
}
