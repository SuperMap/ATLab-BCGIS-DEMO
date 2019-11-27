package com.supermap.atlab.blockchain;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;

import static org.junit.Assert.assertEquals;

public class BlockchainTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Blockchain.class);
    }

    @Test
    public void testGetIt() {
        final String hello = target("myresource").request().get(String.class);
        assertEquals("Got it!", hello);
    }

}