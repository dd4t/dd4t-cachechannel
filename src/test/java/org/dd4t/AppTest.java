package org.dd4t;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
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

    public static DeserializationTest() {
        String s = "{\"regionPath\":\"/com.tridion.storage.BinaryContent\",\"key\":\"23:2755721:tcm:23-2750225-32\",\"type\":1}";
        
    }


    /**
     *
     *
     *
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
