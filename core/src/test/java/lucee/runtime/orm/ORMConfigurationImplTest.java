package lucee.runtime.orm;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import lucee.commons.io.res.Resource;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.type.StructImpl;

public class ORMConfigurationImplTest {
	@Test
	void testLoad() throws PageException{
        ORMConfigurationImpl defaultORMConfig = Mockito.mock( ORMConfigurationImpl.class );
        Config mockServerConfig = Mockito.mock( Config.class );
        ApplicationContext mockApplicationContext = Mockito.mock( ApplicationContext.class );
        Resource defaultCFCLocation = Mockito.mock( Resource.class );

        StructImpl theSettings = new StructImpl();
        theSettings.set(ORMConfigurationImpl.AUTO_GEN_MAP, true );
        theSettings.set(ORMConfigurationImpl.EVENT_HANDLING, true );
        theSettings.set(ORMConfigurationImpl.EVENT_HANDLER, "bla.cfc" );
        // theSettings.set(ORMConfigurationImpl.ORM_CONFIG, "config.xml" );

        ORMConfiguration config = ORMConfigurationImpl.load(
            mockServerConfig,
            mockApplicationContext, 
            theSettings,
            defaultCFCLocation,
            defaultORMConfig);
        assertNotNull(config);
	}
}
