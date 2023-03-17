package lucee.runtime.ioc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import lucee.cli.servlet.ServletConfigImpl;
import lucee.cli.servlet.ServletContextImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.osgi.BundleCollection;
import lucee.runtime.engine.CFMLEngineImpl;

public class CFMLEngineModule extends AbstractModule {
    static CFMLEngine engine;
    static CFMLEngineFactory factory;

    @Override
    protected void configure(){
        // ;
        // bind( CFMLEngine.class)
        //     .to( CFMLEngineImpl.class );
        // bind( CFMLEngineFactory.class)
        //     .toInstance(getCFMLEngineFactory());
    }

    @Provides
    @Singleton
    @Inject
    CFMLEngine getCFMLEngine( CFMLEngineFactory factory ){
        return factory.getEngine();
    }

    // @Provides
    // @Singleton
    // CFMLEngineFactory getCFMLEngineFactory(){
    //     try{
    //         return CFMLEngineFactory.getOrBuild( getServletConfig() );
    //     } catch( ServletException e ){
    //         throw new RuntimeException( e );
    //     }
    // }

    ServletContext getServletContext(){
        File root = new File(".");
        Map<String, Object> attributes = new HashMap<String, Object>();
		Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("lucee-server-directory", new File(root, "WEB-INF").getAbsolutePath());
        return new ServletContextImpl(root, attributes, initParams, 1, 0);
    }

    @Provides
    @Singleton
    ServletConfig getServletConfig(){
		return new ServletConfigImpl((ServletContextImpl) getServletContext(), "CFMLServlet");
    }
}
