// Testing and mocking
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.IOException;
import org.osgi.framework.BundleException;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Identification;
import lucee.runtime.osgi.BundleDownloader;

public class BundleDownloaderTest{

    @Test
    public void canInitialize() {
        BundleDownloader downloader = new BundleDownloader();
    }

    @Test
    public void willThrowOnDownloadDisabled() throws IOException, BundleException{
        CFMLEngineFactory mockEngineFactory = Mockito.mock( CFMLEngineFactory.class );
        Identification id = Mockito.mock(Identification.class);

        java.lang.System.setProperty("lucee.enable.bundle.download", "false");

        assertThrows( RuntimeException.class, () -> {
            BundleDownloader.downloadBundle(mockEngineFactory, "hibernate-extension", "5.4.29.Final", id );
        });
    }
}