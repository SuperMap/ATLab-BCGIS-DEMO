package com.supermap.atlab.blockchain;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;

public class BlockchainTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Blockchain.class);
    }

    @Test
    public void getRecordTest() {
        final String record = target("blockchain").request().get(String.class);
        System.out.println(record);
        Assert.assertNotNull(record);
    }

    @Test
    public void putRecordTest() {
        final String result = target("blockchain").request().post(Entity.text(""), String.class);
        Assert.assertEquals("success", result);
    }
}