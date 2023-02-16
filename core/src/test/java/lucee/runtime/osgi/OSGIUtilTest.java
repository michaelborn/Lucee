// Testing and mocking
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import lucee.runtime.osgi.OSGiUtil;

public class OSGIUtilTest {

    @Test
    public void canInitialize() {
        OSGiUtil util = new OSGiUtil();
    }

}