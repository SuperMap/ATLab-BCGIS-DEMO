package com.supermap.atlab.storage;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;

import static org.junit.Assert.assertEquals;

public class HDFSTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(HDFS.class);
    }

    @Test
    public void testGetIt() {
        final String hello = target("myresource").request().get(String.class);
        assertEquals("Got it!", hello);
    }
}