// Testing and mocking
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.exp.PageException;

public class JavaSettingsImplTest {
    private JavaSettingsImpl javaSettings;

    @Test
    public void canInitialize() {
        Struct settings = (Struct) new StructImpl();
        javaSettings = JavaSettingsImpl.newInstance( new JavaSettingsImpl(), settings );
    }

    @Test
    public void throwsOnBadPath() throws PageException {
        Struct settings = (Struct) new StructImpl();
        settings.set( new KeyImpl( "loadPaths" ), "thisPathDoesNotExist" );
        JavaSettingsImpl.newInstance( new JavaSettingsImpl(), settings );
        assertThrows(
            java.io.IOException.class,
            () -> JavaSettingsImpl.newInstance( new JavaSettingsImpl(), settings )
        );
    }

}