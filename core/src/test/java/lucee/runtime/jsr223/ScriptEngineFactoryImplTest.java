package lucee.runtime.jsr223;
// Testing and mocking
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import lucee.loader.engine.CFMLEngine;

public class ScriptEngineFactoryImplTest {

    @Test
    public void canInitialize() {
        CFMLEngine mockEngine = Mockito.mock( CFMLEngine.class );
        Boolean isTagSyntax = false;
        ScriptEngineFactoryImpl scriptFactory = new ScriptEngineFactoryImpl(mockEngine, isTagSyntax, CFMLEngine.DIALECT_CFML );

        assertEquals( Arrays.asList( "cfm", "cfc" ), scriptFactory.getExtensions() );
        assertEquals( Arrays.asList( "text/cfml", "application/cfml" ), scriptFactory.getMimeTypes() );
    }
    
}
