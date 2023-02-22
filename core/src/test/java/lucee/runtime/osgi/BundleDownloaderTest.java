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
}