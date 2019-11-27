package com.supermap.atlab.common;

import com.supermap.atlab.Utils;
import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void getExtNameTestEmpty() {
        String fileName = "";
        Assert.assertEquals("", Utils.getExtName(fileName));
    }

    @Test
    public void getExtNameTestNull() {
        String fileName = null;
        Assert.assertEquals(null, Utils.getExtName(fileName));
    }

    @Test
    public void getExtNameTestNomal() {
        String fileName = "abc.txt";
        Assert.assertEquals(".txt", Utils.getExtName(fileName));
    }

    @Test
    public void getExtNameTestTwoDots() {
        String fileName = "abc.mp3.txt";
        Assert.assertEquals(".txt", Utils.getExtName(fileName));
    }

    @Test
    public void getExtNameTestEndWithDot() {
        String fileName = "abc.mp3.txt.";
        Assert.assertEquals(".", Utils.getExtName(fileName));
    }
}