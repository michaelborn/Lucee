/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package lucee.loader.engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.Logger;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intergral.fusiondebug.server.FDControllerFactory;

import lucee.VersionInfo;
import lucee.commons.io.log.Log;
import lucee.commons.lang.ConcurrentHashMapAsHashtable;
import lucee.loader.TP;
import lucee.loader.osgi.BundleCollection;
import lucee.loader.osgi.BundleLoader;
import lucee.loader.osgi.BundleUtil;
import lucee.loader.osgi.LoggerImpl;
import lucee.loader.util.ExtensionFilter;
import lucee.loader.util.Util;
import lucee.loader.util.ZipUtil;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.Identification;
import lucee.runtime.config.Password;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.util.Pack200Util;

/**
 * Factory to load CFML Engine
 */
@Singleton
public class CFMLEngineFactory extends CFMLEngineFactorySupport {

	public  static final Version VERSION_ZERO   = new Version(0, 0, 0, "0");
	private static final long GB1               = 1024 * 1024 * 1024;
	private static final long MB100             = 1024 * 1024 * 100;

	private static CFMLEngineFactory factory;
	private static CFMLEngine engine;

	private static File luceeServerRoot;

	private Felix felix;
	private BundleCollection bundleCollection;
	private final ClassLoader mainClassLoader = new TP().getClass().getClassLoader();
	private Version version;
	private final List<EngineChangeListener> listeners = new ArrayList<EngineChangeListener>();
	private File resourceRoot;

	private final LoggerImpl logger;

	// do not remove/ranme, grapped by core directly
	protected ServletConfig config;
	private BundleLoader bundleLoader;

	@Inject
	public CFMLEngineFactory(final ServletConfig config) {
		try{
			this.config = config;
			// this.engine = engine;
			System.setProperty("org.apache.commons.logging.LogFactory.HashtableImpl", ConcurrentHashMapAsHashtable.class.getName());
			this.logger = buildRootLogger();
			this.logger.setLogLevel( LoggerImpl.LOG_DEBUG );
			this.bundleLoader = new BundleLoader(this.logger, getResourceRoot(), new Pack200Util(),getBundleDirectory());
		} catch( Exception e ){
			throw new RuntimeException(e);
		}
	}

	public BundleLoader getBundleLoader(){
		return this.bundleLoader;
	}

	/**
	 * returns instance of this factory (engine = always the same instance) do auto update when
	 * changes occur
	 *
	 * @param config servlet config
	 * @return engine Instance of the Factory
	 * @throws ServletException servlet exception
	 */
	public synchronized static CFMLEngine getInstance(final ServletConfig config) throws ServletException {

		if (engine != null) {
			if (factory == null) factory = engine.getCFMLEngineFactory(); // not sure if this ever is done, but it does not hurt
			return engine;
		}

		if (factory == null) {
			factory = new CFMLEngineFactory(config);
		}

		// read init param from config
		factory.readInitParam(config);

		factory.initEngineIfNecessary();
		engine.addServletConfig(config);

		// add listener for update
		// factory.addListener(engine);
		return engine;
	}

	private static LoggerImpl buildRootLogger(){
		File logFile = null;
		logFile = new File(".", "felix.log");
		if (logFile.isFile()) {
			// more than a GB (from the time we did not control it)
			if (logFile.length() > GB1) {
				logFile.delete(); // we simply delete it
			}
			else if (logFile.length() > MB100) {
				File bak = new File(logFile.getParentFile(), "felix.1.log");
				if (bak.isFile()) bak.delete();
				logFile.renameTo(bak);
			}

		}
		logFile.getParentFile().mkdirs();
		return new LoggerImpl(logFile);
	}

	/**
	 * returns instance of this factory (engine = always the same instance) do auto update when
	 * changes occur
	 *
	 * @return engine Instance of the Factory
	 * @throws RuntimeException runtime exception
	 */
	public static CFMLEngine getEngine() throws RuntimeException {
		if (engine != null) return engine;
		throw new RuntimeException("Engine is not initialized, you must first call getInstance(ServletConfig)");
	}
	public static CFMLEngine getInstance() throws RuntimeException {
		return getEngine();
	}

	/**
	 * returns instance of this factory (engine always the same instance)
	 *
	 * @param config servlet config
	 * @param listener listener
	 * @return engine Instance of the Factory
	 * @throws ServletException servlet exception
	 */
	public static CFMLEngine getInstance(final ServletConfig config, final EngineChangeListener listener) throws ServletException {
		getInstance(config);

		// add listener for update
		factory.addListener(listener);

		// read init param from config
		factory.readInitParam(config);

		factory.initEngineIfNecessary();
		engine.addServletConfig(config);

		// make the FDController visible for the FDClient
		FDControllerFactory.makeVisible();

		return engine;
	}

	void readInitParam(final ServletConfig config) {
		if (luceeServerRoot != null) return;

		String initParam = config.getInitParameter("lucee-server-directory");
		if (Util.isEmpty(initParam)) initParam = config.getInitParameter("lucee-server-root");
		if (Util.isEmpty(initParam)) initParam = config.getInitParameter("lucee-server-dir");
		if (Util.isEmpty(initParam)) initParam = config.getInitParameter("lucee-server");
		if (Util.isEmpty(initParam)) initParam = Util._getSystemPropOrEnvVar("lucee.server.dir", null);

		initParam = parsePlaceHolder(removeQuotes(initParam, true));
		try {
			if (!Util.isEmpty(initParam)) {
				final File root = new File(initParam);
				if (!root.exists()) {
					if (root.mkdirs()) {
						luceeServerRoot = root.getCanonicalFile();
						return;
					}
				}
				else if (root.canWrite()) {
					luceeServerRoot = root.getCanonicalFile();
					return;
				}
			}
		}
		catch (final IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * adds a listener to the factory that will be informed when a new engine will be loaded.
	 *
	 * @param listener
	 */
	private void addListener(final EngineChangeListener listener) {
		if (!listeners.contains(listener)) listeners.add(listener);
	}

	/**
	 * @throws ServletException
	 */
	private void initEngineIfNecessary() throws ServletException {
		if (engine == null) initEngine();
	}

	/**
	 * !!! TODO: Move this to an EventListener for an `EngineShutdownEvent`
	 * @throws BundleException
	 */
	public void shutdownFelix() throws BundleException {
		log(Logger.LOG_DEBUG, "---- Shutdown Felix ----");

		BundleCollection bc = engine.getBundleCollection();
		if (bc == null || bc.getOSGIFramework() == null) return;

		// stop
		bundleLoader.removeBundles(bc);

		// we give it some time
		try {
			Thread.sleep(5000);
		}
		catch (InterruptedException e) {
		}

		BundleUtil.stop(felix, false);
	}

	private void initEngine() throws ServletException {
		final Version coreVersion = VersionInfo.getIntVersion();
		final long coreCreated = VersionInfo.getCreateTime();

		// Load Lucee
		// URL url=null;
		try {
				log(Logger.LOG_DEBUG, "Load built-in Core");

				final String coreExt = "lco";
				final String coreExtPack = "lco.pack.gz";
				boolean isPack200 = false;
				// copy core

				final File rc = new File(getTempDirectory(), "tmp_" + System.currentTimeMillis() + "." + coreExt);
				File rcPack200 = new File(getTempDirectory(), "tmp_" + System.currentTimeMillis() + "." + coreExtPack);
				InputStream is = null;
				OutputStream os = null;
				try {
					is = new TP().getClass().getResourceAsStream("/core/core." + coreExt);
					if (is == null) {
						is = new TP().getClass().getResourceAsStream("/core/core." + coreExtPack);
						if (is != null) {
							isPack200 = true;
						}
					}

					if (is == null) {
						// check for custom path of Lucee core
						is = findCoreInPath(System.getProperty("lucee.core.path"), coreExt);
					}

					/**
					 * If no core.lco found, fall back to the currently executing .jar
					 */
					if ( is == null ){
						String jarPath = getClassLoaderPath(mainClassLoader );
						is = new FileInputStream( new File( jarPath ) );
					}

					/**
					 * Copy to rc var
					 */
					if (is != null) {
						os = new BufferedOutputStream(new FileOutputStream(isPack200 ? rcPack200 : rc));
						copy(is, os);
					}
				}
				finally {
					closeEL(is);
					closeEL(os);
				}

				// unpack if necessary
				if (isPack200) uncompressPackedCore(rc, rcPack200);

				CFMLEngine engine = null;
				if (rc.exists()) {
					engine = _getCore(rc);
					loadBundlesFromCore(rc);
				}
				else {
					// TODO: LDEV-2805 set engine's classloader to use local class files
					// engine =
				}

			version = engine.getInfo().getVersion();

			log(Logger.LOG_DEBUG, "Loaded Lucee Version [" + engine.getInfo().getVersion() + "]");
		}
		catch (final InvocationTargetException e) {
			log(e.getTargetException());
			// e.getTargetException().printStackTrace();
			throw new ServletException(e.getTargetException());
		}
		catch (final Exception e) {
			throw new ServletException(e);
		}

	}

	/**
	 * Given a loaded Lucee core jar, load the bundles into the current bundle context.
	 * 
	 * @param lucee
	 * @throws IOException
	 * @throws BundleException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void loadBundlesFromCore(File lucee) throws IOException, BundleException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		bundleCollection = bundleLoader.loadBundles(this, getFelixCacheDirectory(), getBundleDirectory(), lucee, bundleCollection);
		// bundle=loadBundle(lucee);
		log(Logger.LOG_DEBUG, "Loaded bundle: [" + bundleCollection.getCoreBundle().getSymbolicName() + "]");
		engine = buildEngine(bundleCollection);
		log(Logger.LOG_DEBUG, "Loaded engine: [" + engine + "]");
	}

	/**
	 * Given a pack200-compressed Lucee core file, unpack it to a normal .jar file
	 * @param rc
	 * @param rcPack200
	 * @throws IOException
	 */
	private void uncompressPackedCore(final File rc, File rcPack200) throws IOException {
		Pack200Util.pack2Jar(rcPack200, rc);
		log(Logger.LOG_DEBUG, "unpack " + rcPack200 + " to " + rc);
		rcPack200.delete();
	}

	/**
	 * Look for the lucee core .lco (or other file extension) at the provided location.
	 * @param path
	 * @param coreExt
	 * @return
	 * @throws FileNotFoundException
	 */
	private InputStream findCoreInPath(String path,final String coreExt ) throws FileNotFoundException {
		if (path != null) {
			File dir = new File(path);
			File[] files = dir.listFiles(new ExtensionFilter(new String[]{ coreExt }));
			if (files.length > 0) {
				return new FileInputStream(files[0]);
			}
		}
		return null;
	}

	/**
	 * Build or retrieve the Felix OSGI framework for bundle loading purposes.
	 * 
	 * @param felixCacheDirectory
	 * @param config
	 * @return Felix framework
	 * @throws BundleException
	 */
	public Felix getFelix(File felixCacheDirectory, Map<String, Object> config) throws BundleException {
		this.felix = new OSGIFrameworkBuilder()
			.withConfig( config )
			.withLogger( logger )
			.withCacheDirectory(felixCacheDirectory)
			.build();

		return felix;
	}

	protected static String getSystemPropOrEnvVar(String name, String defaultValue) {
		// env
		String value = System.getenv(name);
		if (!Util.isEmpty(value)) return value;

		// prop
		value = System.getProperty(name);
		if (!Util.isEmpty(value)) return value;

		// env 2
		name = name.replace('.', '_').toUpperCase();
		value = System.getenv(name);
		if (!Util.isEmpty(value)) return value;

		return defaultValue;
	}

	public void log(final Throwable t) {
		if (logger != null) logger.log(Logger.LOG_ERROR, "", t);
	}

	public void log(final int level, final String msg) {
		if (logger != null) logger.log(level, msg);
	}

	private CFMLEngine _getCore(File rc) throws IOException, BundleException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		bundleCollection = bundleLoader.loadBundles(this, getFelixCacheDirectory(), getBundleDirectory(), rc, bundleCollection);
		return buildEngine(bundleCollection);

	}

	public boolean restart(final Password password) throws IOException, ServletException {
		if (!engine.can(CFMLEngine.CAN_RESTART_ALL, password)) throw new IOException("Access denied to restart CFMLEngine");

		return _restart();
	}

	public boolean restart(final String configId, final Password password) throws IOException, ServletException {
		if (!engine.can(CFMLEngine.CAN_RESTART_CONTEXT, password))// TODO restart single context
			throw new IOException("Access denied to restart CFML Context (configId:" + configId + ")");

		return _restart();
	}

	/**
	 * restart the cfml engine
	 *
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	private synchronized boolean _restart() throws ServletException {
		if (engine != null) engine.reset();

		initEngine();

		ConfigServer cs = getConfigServer(engine);
		if (cs != null) {
			Log log = cs.getLog("application");
			log.info("loader", "Lucee restarted");
		}
		System.gc();
		return true;
	}

	private ConfigServer getConfigServer(CFMLEngine engine) {
		if (engine == null) return null;
		if (engine instanceof CFMLEngineWrapper) engine = ((CFMLEngineWrapper) engine).getEngine();

		try {
			Method m = engine.getClass().getDeclaredMethod("getConfigServerImpl", new Class[] {});
			m.setAccessible(true);
			return (ConfigServer) m.invoke(engine, new Object[] {});
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public File getBundleDirectory() throws IOException {
		File bd = getDirectoryByPropOrEnv("lucee.bundles.dir");
		if (bd != null) return bd;

		bd = new File(getResourceRoot(), "bundles");
		if (!bd.exists()) bd.mkdirs();
		return bd;
	}

	private File getFelixCacheDirectory() throws IOException {
		return getResourceRoot();
		// File bd = new File(getResourceRoot(),"felix-cache");
		// if(!bd.exists())bd.mkdirs();
		// return bd;
	}

	/**
	 * return directory to lucee resource root
	 *
	 * @return lucee root directory
	 * @throws IOException exception thrown
	 */
	public File getResourceRoot() throws IOException {
		if (resourceRoot == null) {
			resourceRoot = new File(_getResourceRoot(), "lucee-server");
			if (!resourceRoot.exists()) resourceRoot.mkdirs();
		}
		return resourceRoot;
	}

	/**
	 * @return return running context root
	 * @throws IOException
	 * @throws IOException
	 */
	private File _getResourceRoot() throws IOException {

		// custom configuration
		if (luceeServerRoot == null) readInitParam(config);
		if (luceeServerRoot != null) return luceeServerRoot;

		File lbd = getDirectoryByPropOrEnv("lucee.base.dir"); // directory defined by the caller

		File root = lbd;
		// get the root directory
		if (root == null) root = getDirectoryByProp("jboss.server.home.dir"); // Jboss/Jetty|Tomcat
		if (root == null) root = getDirectoryByProp("jonas.base"); // Jonas
		if (root == null) root = getDirectoryByProp("catalina.base"); // Tomcat
		if (root == null) root = getDirectoryByProp("jetty.home"); // Jetty
		if (root == null) root = getDirectoryByProp("org.apache.geronimo.base.dir"); // Geronimo
		if (root == null) root = getDirectoryByProp("com.sun.aas.instanceRoot"); // Glassfish
		if (root == null) root = getDirectoryByProp("env.DOMAIN_HOME"); // weblogic
		if (root == null) root = getClassLoaderRoot(mainClassLoader).getParentFile().getParentFile();

		final File classicRoot = getClassLoaderRoot(mainClassLoader);

		// in case of a war file the server root need to be with the context
		if (lbd == null) {
			File webInf = getWebInfFolder(classicRoot);
			if (webInf != null) {
				root = webInf;
				if (!root.exists()) root.mkdir();
				log(Logger.LOG_DEBUG, "war-root-directory:" + root);
			}
		}

		log(Logger.LOG_DEBUG, "root-directory:" + root);

		if (root == null) throw new IOException("Can't locate the root of the servlet container, please define a location (physical path) for the server configuration"
				+ " with help of the servlet init param [lucee-server-directory] in the web.xml where the Lucee Servlet is defined" + " or the system property [lucee.base.dir].");

		final File modernDir = new File(root, "lucee-server");
		if (true) {
			// there is a server context in the old lucee location, move that one
			File classicDir;
			log(Logger.LOG_DEBUG, "classic-root-directory:" + classicRoot);
			boolean had = false;
			if (classicRoot.isDirectory() && (classicDir = new File(classicRoot, "lucee-server")).isDirectory()) {
				log(Logger.LOG_DEBUG, "had lucee-server classic" + classicDir);
				moveContent(classicDir, modernDir);
				had = true;
			}
			// there is a railo context
			if (!had && classicRoot.isDirectory() && (classicDir = new File(classicRoot, "railo-server")).isDirectory()) {
				log(Logger.LOG_DEBUG, "Had railo-server classic" + classicDir);
				// check if there is a Railo context
				copyRecursiveAndRename(classicDir, modernDir);
				// zip the railo-server di and delete it (optional)
				try {
					ZipUtil.zip(classicDir, new File(root, "railo-server-context-old.zip"));
					Util.delete(classicDir);
				}
				catch (final Throwable t) {
					t.printStackTrace();
				}
				// moveContent(classicDir,new File(root,"lucee-server"));
			}
		}

		return root;
	}

	private static File getWebInfFolder(File file) {
		File parent;
		while (file != null && !file.getName().equals("WEB-INF")) {
			parent = file.getParentFile();
			if (file.equals(parent)) return null; // this should not happen, simply to be sure
			file = parent;
		}
		return file;
	}

	private static void copyRecursiveAndRename(final File src, File trg) throws IOException {
		if (!src.exists()) return;

		if (src.isDirectory()) {
			if (!trg.exists()) trg.mkdirs();

			final File[] files = src.listFiles();
			for (final File file: files)
				copyRecursiveAndRename(file, new File(trg, file.getName()));
		}
		else if (src.isFile()) {
			if (trg.getName().endsWith(".rc") || trg.getName().startsWith(".")) return;

			if (trg.getName().equals("railo-server.xml")) {
				trg = new File(trg.getParentFile(), "lucee-server.xml");
				// cfLuceeConfiguration
				final FileInputStream is = new FileInputStream(src);
				final FileOutputStream os = new FileOutputStream(trg);
				try {
					String str = Util.toString(is);
					str = str.replace("<cfRailoConfiguration", "<!-- copy from Railo context --><cfLuceeConfiguration");
					str = str.replace("</cfRailoConfiguration", "</cfLuceeConfiguration");

					str = str.replace("<railo-configuration", "<!-- copy from Railo context --><cfLuceeConfiguration");
					str = str.replace("</railo-configuration", "</cfLuceeConfiguration");

					str = str.replace("{railo-config}", "{lucee-config}");
					str = str.replace("{railo-server}", "{lucee-server}");
					str = str.replace("{railo-web}", "{lucee-web}");
					str = str.replace("\"railo.commons.", "\"lucee.commons.");
					str = str.replace("\"railo.runtime.", "\"lucee.runtime.");
					str = str.replace("\"railo.cfx.", "\"lucee.cfx.");
					str = str.replace("/railo-context.ra", "/lucee-context.lar");
					str = str.replace("/railo-context", "/lucee");
					str = str.replace("railo-server-context", "lucee-server");
					str = str.replace("http://www.getrailo.org", "https://release.lucee.org");
					str = str.replace("http://www.getrailo.com", "https://release.lucee.org");

					final ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());

					try {
						Util.copy(bais, os);
						bais.close();
					}
					finally {
						Util.closeEL(is, os);
					}
				}
				finally {
					Util.closeEL(is, os);
				}
				return;
			}

			final FileInputStream is = new FileInputStream(src);
			final FileOutputStream os = new FileOutputStream(trg);
			try {
				Util.copy(is, os);
			}
			finally {
				Util.closeEL(is, os);
			}
		}
	}

	private void moveContent(final File src, final File trg) throws IOException {
		if (src.isDirectory()) {
			final File[] children = src.listFiles();
			if (children != null) for (final File element: children)
				moveContent(element, new File(trg, element.getName()));
			src.delete();
		}
		else if (src.isFile()) {
			trg.getParentFile().mkdirs();
			src.renameTo(trg);
		}
	}

	private File getDirectoryByPropOrEnv(final String name) {
		File file = getDirectoryByProp(name);
		if (file != null) return file;
		return getDirectoryByEnv(name);
	}

	private File getDirectoryByProp(final String name) {
		return _getDirectoryBy(System.getProperty(name));
	}

	private File getDirectoryByEnv(final String name) {
		return _getDirectoryBy(System.getenv(name));
	}

	private File _getDirectoryBy(final String value) {
		if (Util.isEmpty(value, true)) return null;

		final File dir = new File(value);
		dir.mkdirs();
		if (dir.isDirectory()) return dir;

		return null;
	}

	/**
	 * returns the path where the classloader is located
	 *
	 * @param cl ClassLoader
	 * @return file of the classloader root
	 */
	public static File getClassLoaderRoot(final ClassLoader cl) {
		String strFile = CFMLEngineFactory.getClassLoaderPath( cl );

		// remove lucee.jar at the end
		if (strFile.endsWith("lucee.jar")) strFile = strFile.substring(0, strFile.length() - 9);

		File file = new File(strFile);
		if (file.isFile()) file = file.getParentFile();

		return file;
	}

	/**
	 * Get the path to the location of the passed classloader - hopefully, the lucee.jar path.
	 * 
	 * @param cl Classloader to look at
	 * @return A String that (probably) terminates with the filename of the executing jar, i.e. <code>.../WEB-INF/lib/lucee.jar</code>
	 */
	public static String getClassLoaderPath( ClassLoader cl ){
		final String path = "lucee/loader/engine/CFMLEngine.class";
		final URL res = cl.getResource(path);
		if (res == null) return null;
		// get file and remove all after !
		String strFile = null;
		try {
			strFile = URLDecoder.decode(res.getFile().trim(), "iso-8859-1");
		}
		catch (final UnsupportedEncodingException e) {

		}
		int index = strFile.indexOf('!');
		if (index != -1) strFile = strFile.substring(0, index);

		return strFile
				.replace("lucee/loader/engine/CFMLEngine.class", "")
				.replace("file:", "");
	}

	/**
	 * Load CFMl Engine Implementation (lucee.runtime.engine.CFMLEngineImpl) from a Classloader
	 *
	 * @param bundle
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private CFMLEngine buildEngine(final BundleCollection bc)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		log(Logger.LOG_DEBUG, "state: " + BundleUtil.bundleState(bc.getCoreBundle().getState(), ""));
		// bundle.getBundleContext().getServiceReference(CFMLEngine.class.getName());
		log(Logger.LOG_DEBUG, Constants.FRAMEWORK_BOOTDELEGATION + ":" + bc.getBundleContext().getProperty(Constants.FRAMEWORK_BOOTDELEGATION));
		log(Logger.LOG_DEBUG, "felix.cache.rootdir: " + bc.getBundleContext().getProperty("felix.cache.rootdir"));

		return CFMLEngineImpl.getInstance(this, bc);

	}

	public Logger getLogger() {
		return logger;
	}

}
