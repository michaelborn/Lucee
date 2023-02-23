package lucee.runtime.osgi;

// Testing and mocking
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.runtime.osgi.OSGiUtil;

public class OSGIUtilTest {

    @Test
    public void canInitialize() {
        OSGiUtil util = new OSGiUtil();
    }

    @Test
    public void canConvertVersion() throws BundleException{
        Version result = OSGiUtil.toVersion( "1.2.3.4" );

        assertEquals( 1, result.getMajor());
        assertEquals( 2, result.getMinor());
        assertEquals( 3, result.getMicro());
        assertEquals( "4", result.getQualifier());
    }

    @Test
    public void canConvertVersionWithDefault() throws BundleException{
        Version result = OSGiUtil.toVersion( "1.2.bla.bla", new Version(0, 0, 0, "bla") );

        assertEquals( 1, result.getMajor());
        assertEquals( 2, result.getMinor());
        assertEquals( 0, result.getMicro());
        assertEquals( "bla_bla", result.getQualifier());
    }
    @Test
    void canParseRangeString(){
        assertEquals( "5.2.9.31",OSGiUtil.toVersionRange("5.2.9.31").toString());
        assertEquals( "5.3.0.36-ALPHA",OSGiUtil.toVersionRange("5.3.0.36-ALPHA").toString());
        assertEquals( "5.0.0.244-SNAPSHOT",OSGiUtil.toVersionRange("5.0.0.244-SNAPSHOT").toString());
    }
	@Test
	void testIncludes() throws BundleException{
        assertTrue(
            OSGiUtil.toVersionRange( "5.3.2.63-ALPHA" )
                        .includes(OSGiUtil.toVersion("6.0.0.1") )
        );
        assertTrue(
            OSGiUtil.toVersionRange( "5.3.2.63,5.4.0.0" )
                        .includes(OSGiUtil.toVersion("5.3.2.64") )
        );
        assertTrue(
            OSGiUtil.toVersionRange( "5.3.2.63,5.4.0.0-SNAPSHOT" )
                        .includes(OSGiUtil.toVersion("5.4.0.0-ALPHA") )
        );
        assertTrue(
            OSGiUtil.toVersionRange( "5.3.2.63-SNAPSHOT,6.0.0.0-SNAPSHOT" )
                        .includes(OSGiUtil.toVersion("5.4.0.1") )
        );
        assertFalse(
            OSGiUtil.toVersionRange( "5.3.2.63,0.0.0.SNAPSHOT" )
                        .includes(OSGiUtil.toVersion("6.0.0.346-SNAPSHOT") )
        );
        assertTrue(
            OSGiUtil.toVersionRange( "5.3.2.63-ALPHA" )
                        .includes(OSGiUtil.toVersion("6.0.0.1") )
        );

        assertFalse(
            OSGiUtil.toVersionRange( "5.3.2.63-ALPHA" )
                        .includes(OSGiUtil.toVersion("1.0.0.1") )
                        , "exceeds lower limit"
        );

        // exceeds upper limit
        assertFalse(
            OSGiUtil.toVersionRange( "5.3.2.63-ALPHA,5.40.0.0" )
                        .includes(OSGiUtil.toVersion("5.40.1.0") )
                        , "exceeds upper limit"
        );
	}
}