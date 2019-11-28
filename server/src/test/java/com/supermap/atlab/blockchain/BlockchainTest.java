package com.supermap.atlab.blockchain;

import org.junit.Test;

import java.io.File;

public class BlockchainTest {

    @Test
    public void getRecord() {

        String path = "/home/cy/Desktop/stair-bim/KMLModels";
        File files = new File(path);
        File[] fileList = files.listFiles();
        String names = "";
        for (File file : fileList) {
            String res =  file.getName().replace("#", "%23");
            names += "\"" + res + "\", ";
        }
        System.out.println(names);
    }
}