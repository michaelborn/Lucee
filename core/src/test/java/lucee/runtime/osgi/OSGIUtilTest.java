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
}