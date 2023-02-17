package lucee.loader.engine;

import static org.junit.jupiter.api.Assertions.*;

import javax.servlet.ServletConfig;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import lucee.loader.engine.CFMLEngineFactory;

public class CFMLEngineFactoryTest {
    
    @Test
    public void canInitialize() {
        ServletConfig mockServletConfig = Mockito.mock( ServletConfig.class );
        CFMLEngineFactory factory = new CFMLEngineFactory( mockServletConfig );

        assertNotNull(factory);
    }
}