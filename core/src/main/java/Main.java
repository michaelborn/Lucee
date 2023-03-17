import com.google.inject.Guice;
import com.google.inject.Injector;

import lucee.loader.engine.CFMLEngine;
import lucee.runtime.ioc.*;

/**
 * Expirementing with a DI-first engine loader.
 */
public class Main {
    public static void main(String[] args ){

		Injector injector = Guice.createInjector(
            new CFMLEngineModule(),
			new OSGIModule()
		);
		CFMLEngine engine = injector.getInstance( CFMLEngine.class );
		engine.start();
    }
}
