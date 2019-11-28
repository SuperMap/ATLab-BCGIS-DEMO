package com.supermap.atlab.blockchain;

import com.atlchain.sdk.ATLChain;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.InputStream;

@Path("/blockchain")
public class Blockchain {

    private ATLChain atlChain;
    final private File networkConfigFile = new File("/home/cy/Documents/ATL/SuperMap/ATLab-examples/server/src/main/resources/network-config-test.yaml");
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
        String key = "model001";
        String functionName = "PutRecord";
        String value = "\"GeoModel3D_0000000059470CB0.s3m\", \"GeoModel3D%23_514788146.s3m\", \"GeoModel3D_00000000401ABD30.s3m\", \"GeoModel3D_0000000059470B00.s3m\", \"GeoModel3D_00000000401AB6E0.s3m\", \"GeoModel3D%23_515870026.s3m\", \"GeoModel3D_000000005B306470.s3m\", \"GeoModel3D_00000000401AAA80.s3m\", \"GeoModel3D%23_515498919.s3m\", \"GeoModel3D_00000000401AC350.s3m\", \"GeoModel3D_00000000589B4A60.s3m\", \"GeoModel3D%23_515435356.s3m\", \"GeoModel3D_000000005B383EA0.s3m\", \"GeoModel3D_000000005B2CEDD0.s3m\", \"GeoModel3D_000000005B17AC00.s3m\", \"GeoModel3D%23_515748021.s3m\", \"GeoModel3D_000000003F8A7C70.s3m\", \"GeoModel3D%23_514864874.s3m\", \"GeoModel3D_000000005B305E90.s3m\", \"GeoModel3D_00000000583FDAB0.s3m\", \"GeoModel3D_000000005B306240.s3m\", \"GeoModel3D_000000005B2F29C0.s3m\", \"GeoModel3D_000000003F8A4290.s3m\", \"GeoModel3D_000000005B386AC0.s3m\", \"GeoModel3D_00000000401A94B0.s3m\", \"GeoModel3D_000000005B306450.s3m\", \"GeoModel3D_00000000401AC1B0.s3m\", \"GeoModel3D_0000000040126AC0.s3m\", \"GeoModel3D_000000005B17A6E0.s3m\", \"GeoModel3D_0000000058AD9BE0.s3m\", \"GeoModel3D_0000000040111230.s3m\", \"GeoModel3D%23_515652338.s3m\", \"GeoModel3D_000000005B2CEF70.s3m\", \"GeoModel3D%23_515394098.s3m\", \"GeoModel3D_00000000401A8600.s3m\", \"GeoModel3D%23_515362609.s3m\", \"GeoModel3D%23_514665277.s3m\", \"GeoModel3D%23_515605045.s3m\", \"GeoModel3D%23_514521583.s3m\", \"GeoModel3D_0000000059AFC230.s3m\", \"GeoModel3D_0000000058ADA250.s3m\"";

        String result = atlChain.invoke(
                chaincodeName,
                functionName,
                new String[]{key, value}
        );
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
}
