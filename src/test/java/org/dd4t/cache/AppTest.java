package org.dd4t.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tridion.cache.CacheEvent;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public void testDeserialize() throws IOException {
        String[] messages = {
                "{\"regionPath\":\"/com.tridion.storage.BinaryContent\",\"key\":\"23:2755721:tcm:23-2750225-32\",\"type\":1}"
        };

        for(int i = 0, l = messages.length; i < l; i++) {
            CacheEvent cacheEvent = CacheEventSerializerService.deserialize(messages[i]);
            assertNotNull(cacheEvent.getKey());
            assertNotNull(cacheEvent);
        }
    }
    public void testSerialize() throws JsonProcessingException {
        CacheEvent cacheEvent = new CacheEvent("regionpathX", "my:key", 1);
        String json = CacheEventSerializerService.serialize(cacheEvent);
        assertTrue(json.contains("regionpathX"));
    }
}
