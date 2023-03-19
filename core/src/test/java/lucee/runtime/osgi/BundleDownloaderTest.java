package lucee.runtime.osgi;

// Testing and mocking
import static org.junit.jupiter.api.Assertions.*;

import org.apache.felix.framework.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import org.osgi.framework.BundleException;

import lucee.commons.io.log.Log;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Identification;
import lucee.runtime.osgi.BundleDownloader;

public class BundleDownloaderTest{

    private BundleDownloader downloader;

    @Test
    public void canInitialize() {
        Log mockLog = Mockito.mock( Log.class );
        downloader = new BundleDownloader( mockLog, new File(".", "bundles"));
    }

    @Test
    public void willThrowOnDownloadDisabled() throws IOException, BundleException{
        assertNotNull(downloader);
        CFMLEngineFactory mockEngineFactory = Mockito.mock( CFMLEngineFactory.class );
        Identification id = Mockito.mock(Identification.class);

        java.lang.System.setProperty("lucee.enable.bundle.download", "false");

        assertThrows( RuntimeException.class, () -> {
            downloader.downloadBundle("hibernate-extension", "5.4.29.Final", id );
        });
    }
}