package lucee.runtime.ioc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import org.osgi.framework.Bundle;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.osgi.BundleLoader;
import lucee.loader.osgi.BundleUtil;

/**
 * Defines database-related Guice bindings
 */
public class OSGIModule extends AbstractModule {
    protected void configure(){
      // bind( BundleLoader.class )
      //   .to( BundleLoader.class );
        // bind(Bundle.class)
        //     .annotatedWith(Names.named("LuceeCore"))
        //     .to(provideLuceeBundle())
    }
    private JarFile getLuceeCoreJar() throws IOException{
      String jarPath = CFMLEngineFactory.getClassLoaderPath( this.getClass().getClassLoader() );
      return new JarFile( new File( jarPath ) );
    }
    // This uses binding annotation with a @Provides method
    @Provides
    @Named("LuceeCore")
    @Singleton
    Bundle provideLuceeBundle() throws IOException {
      return (Bundle) getLuceeCoreJar();
      // return BundleContext.installBundle(jarPath);
    }

    @Provides
    @Named("FelixConfig")
    @Singleton
    Map<String,Object> provideFelixConfig() throws IOException{
      return BundleUtil.loadJarPropertiesFile( getLuceeCoreJar() );
    }

    // @Provides
    // @Named("FelixCacheDirectory")
    // @Singleton
    // Map<String,Object> provideFelixCacheDirectory() throws IOException{
    //   return CFMLEngineFactory.getResourceRoot();
    // }
}
