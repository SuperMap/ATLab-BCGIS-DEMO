package com.supermap.atlab.blockchain;

import com.alibaba.fastjson.JSONObject;
import com.atlchain.sdk.ATLChain;
import com.supermap.atlab.Utils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

@Path("blockchain")
public class Blockchain {

    Logger logger = Logger.getLogger(Blockchain.class.toString());
    private ATLChain atlChain;
    private final File networkConfigFile = new File("/home/cy/Documents/ATL/SuperMap/ATLab-examples/server/src/test/resources/network-config-test.yaml");

    public Blockchain() {
        atlChain = new ATLChain(networkConfigFile);
    }

    /**
     * 根据 Key 读取记录
     *
     * @param key
     * @return
     */
    @Path("record")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getRecord(
            @QueryParam("key") String key,
            @QueryParam("chaincode") String chaincode,
            @QueryParam("function") String function
    ) {
        String result = atlChain.query(
                "bimcc",
                "GetRecord",
                new String[]{"modelID"}
        );

        if (!result.equals("")) {
            JSONObject jsonObject = JSONObject.parseObject(result);
            switch (jsonObject.getString("storage").toLowerCase()) {
                case "chain":
                    break;
                case "hdfs":
                    break;
                case "hbase":
                    break;
                case "fastdfs":
                    break;
                case "ipfs":
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    // 根据范围读取数据，范围按字典顺序排序
//    public byte[][] getRecordByRange(String recordKey, String chaincodeName) {
//        String startKey = recordKey + "-0";
//        String endKey = recordKey + "-99999";
//
//        byte[][] result = atlChain.queryByte(
//                chaincodeName,
//                "GetRecordByKeyRange",
//                new byte[][]{startKey.getBytes(), endKey.getBytes()}
//        );
//        return result;
//    }

    // 向链上写数据
    @Path("record")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String putRecord(
            @FormDataParam("key") String key,
            @FormDataParam("chaincode") String chaincode,
            @FormDataParam("function") String function,
            @FormDataParam("record") String record,
            @FormDataParam("file") InputStream inputStream
    ) {
        if (null != inputStream) {
            ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int rc = 0;
            while (true){
                try {
                    if (!((rc = inputStream.read(buffer, 0, 1024)) > 0)) {
                        break;
                    } else {
                        byteArrayInputStream.write(buffer, 0, rc);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            byte[] fileBytes = byteArrayInputStream.toByteArray();
            String hashName = Utils.getSHA256(fileBytes);

            JSONObject jsonObject = JSONObject.parseObject(record);
            switch (jsonObject.getString("storage").toLowerCase()) {
                case "chain":
                    break;
                case "hdfs":
                    break;
                case "hbase":
                    break;
                case "fastdfs":
                    break;
                case "ipfs":
                    break;
            }
        }

        String result = atlChain.invoke(
                chaincode,
                function,
                new String[]{key, record}
        );
        return result;
    }


    @Path("history")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getHistory(
            @QueryParam("key") String key,
            @QueryParam("chaincode") String chaincode,
            @QueryParam("function") String function
    ) {
        String result = atlChain.query(
                "bimcc",
                "GetHistoryByKey",
                new String[]{"modelID"}
        );

        return result;
    }
}
