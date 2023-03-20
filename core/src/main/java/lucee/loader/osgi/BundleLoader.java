/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.loader.osgi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.io.res.ResourcesImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngineFactorySupport;
import lucee.loader.util.Util;
import lucee.runtime.config.Identification;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.osgi.BundleDownloader;
import lucee.runtime.util.Pack200Util;

public class BundleLoader {

	private final String UPDATE_LOCATION = "https://update.lucee.org";  // MUST from server.xml
	private final int MAX_REDIRECTS      = 5;
	private String pack20Ext = ".jar.pack.gz";

	private Logger logger;
	private File serverRoot;
	private URL updateLocation;
	private Pack200Util pack200Util;
	private File bundleDirectory;
	// private BundleDownloader bundleDownloader;

	public BundleLoader( Logger logger, File serverRoot, Pack200Util pack200Util, File bundleDirectory ){
		try{
			this.logger = logger;
			// default update location... should get from server configuration.
			this.updateLocation = getUpdateLocation();
			this.serverRoot = serverRoot;
			this.pack200Util = pack200Util;
			this.bundleDirectory = bundleDirectory;

			// TODO: Change to constructor parameter
			// this.bundleDownloader = new BundleDownloader( ThreadLocalPageContext.getLog("application"), bundleDirectory);
		} catch( Exception e ){
			throw new RuntimeException(e);
		}
	}

	/**
	 * https://stackoverflow.com/a/71913093
	 * @param jar
	 */
	public List<String> loadBundlesFromJar( JarFile jar, File toDirectory ){
		List<String> bundles = new ArrayList<String>();
		List<JarEntry> jarEntries = Collections.list(jar.entries());
		for (JarEntry entry : jarEntries) {
			if (entry.getName().startsWith("bundles/") && entry.getName().endsWith(".jar")) {
				try (InputStream is = jar.getInputStream(entry)) {
					Path destinationPath = Paths.get(toDirectory.getAbsolutePath(), entry.getName().replace("bundles/", ""));
					Files.createDirectories(destinationPath.getParent());
					Files.copy(is, destinationPath, StandardCopyOption.REPLACE_EXISTING);
					bundles.add(destinationPath.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bundles;
	}

	public BundleCollection loadBundles(final CFMLEngineFactory engFac, final File cacheRootDir, final File bundleDirectory, final File rc, final BundleCollection old)
			throws IOException, BundleException {
		// if (rc.getName().toLowerCase().toLowerCase().indexOf("ehcache") != -1)
		// System. err.println(rc.getName());

		try( JarFile jf = new JarFile(rc) ) {
			// Manifest
			final Manifest mani = jf.getManifest();
			if (mani == null) throw new IOException("lucee core [" + rc + "] is invalid, there is no META-INF/MANIFEST.MF File");
			final Attributes attrs = mani.getMainAttributes();

			Map<String, Object> felixConfig = BundleUtil.loadJarPropertiesFile(jf);

			// close all bundles
			Felix felix;
			if (old != null) {
				removeBundlesEL(old);
				felix = old.getOSGIFramework();
				// stops felix (wait for it)
				BundleUtil.stop(felix, false);
				felix = engFac.getFelix(cacheRootDir, felixConfig);
			}
			else felix = engFac.getFelix(cacheRootDir, felixConfig);
			final BundleContext bc = felix.getBundleContext();

			// deploys bundled bundles to bundle directory
			List<String> bundlePaths = loadBundlesFromJar(jf, bundleDirectory);
			List<Bundle> bundles = new ArrayList<Bundle>();
			bundlePaths.stream().forEach( filename -> {
				try{
					bundles.add( bc.installBundle( "file:" + filename ) );
				}catch( BundleException e ){
					throw new RuntimeException("Unable to install bundle " + filename,e);
				}
			});
			bundles.stream().forEach( bundle -> {
				try{
					bundle.start();
				}catch( BundleException e ){
					throw new RuntimeException("Unable to start bundle " + bundle.getSymbolicName(),e);
				}
			});

			// // get bundle needed for that core
			// final String rb = attrs.getValue("Require-Bundle");
			// // if (Util.isEmpty(rb)) throw new IOException("lucee core [" + rc + "] is invalid, no Require-Bundle definition found in the META-INF/MANIFEST.MF File");

			// // get fragments needed for that core (Lucee specific Key)
			// final String rbf = attrs.getValue("Require-Bundle-Fragment");

			// // load Required/Available Bundles
			// final Map<String, String> requiredBundles = readRequireBundle(rb); // Require-Bundle
			// final Map<String, String> requiredBundleFragments = readRequireBundle(rbf); // Require-Bundle-Fragment
			// System.out.println("required bundles are [" + rb + "]" );
			// System.out.println("required bundle fragments are [" + rbf + "]" );
			// requiredBundles.entrySet().stream()
			// 	.forEach( requirement -> {
			// 		String name = requirement.getKey();
			// 		String version = requirement.getValue();
			// 		System.out.println("Deploying bundle " + name + "." + version + " from lucee.jar to bundle directory");
			// 		deployBundledBundle(bundleDirectory, name, version);
			// 	});

			// System.out.println("Looking for bundles in bundle directory:" + bundleDirectory.toString() );
			// final Map<String, File> bundledJars = findJarsAtPath(bundleDirectory);
			// System.out.println("Bundles already available in the bundle/ directory: " + bundledJars.keySet().toString());
				
			// // deployBundledBundles(jarDirectory, availableBundles);

			// // Add Required Bundles
			// Entry<String, String> e;
			// File f;
			// String id;
			// final List<Bundle> bundles = new ArrayList<Bundle>();
			// Iterator<Entry<String, String>> it = requiredBundles.entrySet().iterator();
			// while (it.hasNext()) {
			// 	e = it.next();
			// 	f = bundledJars.get(e.getKey() + "|" + e.getValue());
			// 	// StringBuilder sb=new StringBuilder();
			// 	if (f == null) {
			// 		System.out.println( "Unable to locate bundle " + e.getKey() + "." + e.getValue() + ".jar" );
			// 		// throw new BundleException( "Unable to locate bundle" + id );
			// 	}
			// 	// if (f == null) f = engFac.downloadBundle(e.getKey(), e.getValue(), null);
			// 	if ( f != null ){
			// 		bundles.add(BundleUtil.addBundle(engFac, bc, f, null));
			// 	}
			// }

			// // Add Required Bundle Fragments
			// final List<Bundle> fragments = new ArrayList<Bundle>();
			// it = requiredBundleFragments.entrySet().iterator();
			// while (it.hasNext()) {
			// 	e = it.next();
			// 	id = e.getKey() + "|" + e.getValue();
			// 	f = bundledJars.get(id);

			// 	// TODO: Consider getting this working...
			// 	// if (f == null) f = bundleDownloader.downloadBundle(e.getKey(), e.getValue(), null); // if identification is not defined, it is loaded from the CFMLEngine
			// 	fragments.add(BundleUtil.addBundle(engFac, bc, f, null));
			// }

			// Add Lucee core Bundle
			Bundle bundle = BundleUtil.addBundle(engFac, bc, rc, null);

			// Start the bundles
			// BundleUtil.start(engFac, bundles);
			BundleUtil.start(engFac, bundle);

			return new BundleCollection(felix, bundle, bundles);
		}
		
	}

	/**
	 * Find .jar files in the given directory - probably a bundles/ path in the lucee.jar core bundle.
	 * 
	 * @param bundleDirectory
	 */
	private Map<String, File> findJarsAtPath(File bundleDirectory) {
		Map<String, File> rtn = new HashMap<String, File>();
		Arrays.stream( bundleDirectory.listFiles() )
			.filter( jar -> {
				return jar.isFile() && jar.getName().endsWith(".jar");
			})
			.forEach( jar -> rtn.put(getKeyNameForJar(jar), jar));
		return rtn;
	}

	/**
	 * Read the manifest from the given jar and return a cacheable key name.
	 * 
	 * @param jar
	 * @return A jar key name of <NAME>|<VERSION>. For example, <code>org.apache.commons.net|3.3.0</code>
	 */
	private String getKeyNameForJar(File jar) {
		try{
			try( JarFile jf = new JarFile(jar) ) {
				Manifest manifest = jf.getManifest();
				if ( manifest == null ) {
					logger.log(Logger.LOG_WARNING,"Unable to load manifest for jar file " + jar.toPath() );
				}
				Attributes attrs = manifest.getMainAttributes();
				String symbolicName = attrs.getValue("Bundle-SymbolicName");
				String version = attrs.getValue("Bundle-Version");
				if (Util.isEmpty(symbolicName))
					logger.log(Logger.LOG_WARNING,"OSGi bundle [" + jar + "] is invalid, META-INF/MANIFEST.MF does not contain a \"Bundle-SymbolicName\"");
				if (Util.isEmpty(version)) logger.log(Logger.LOG_WARNING,"OSGi bundle [" + jar + "] is invalid, META-INF/MANIFEST.MF does not contain a \"Bundle-Version\"");

				return symbolicName + "|" + version;
			}
		} catch( IOException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parse the given <code>Require-Bundle</code> manifest entry to check the required bundles.
	 * 
	 * @param rb
	 * @throws IOException
	 * @returns map of Name,Version values.
	 */
	private Map<String, String> readRequireBundle(final String rb) throws IOException {
		final HashMap<String, String> rtn = new HashMap<String, String>();
		if (Util.isEmpty(rb)) return rtn;

		final StringTokenizer st = new StringTokenizer(rb, ",");
		StringTokenizer stl;
		String line, jarName, jarVersion = null, token;
		int index;
		while (st.hasMoreTokens()) {
			line = st.nextToken().trim();
			if (Util.isEmpty(line)) continue;

			stl = new StringTokenizer(line, ";");

			// first is the name
			jarName = stl.nextToken().trim();

			while (stl.hasMoreTokens()) {
				token = stl.nextToken().trim();
				if (token.startsWith("bundle-version") && (index = token.indexOf('=')) != -1) jarVersion = token.substring(index + 1).trim();
			}
			if (jarVersion != null) {
				rtn.put(jarName, jarVersion);
			} else {
				// throw new IOException("missing \"bundle-version\" info in the following \"Require-Bundle\" record: \"" + jarName + "\"");
			}
		}
		return rtn;
	}

	private File deployBundledBundle(File bundleDirectory, String symbolicName, String symbolicVersion) {
		String sub = "bundles/";
		String nameAndVersion = symbolicName + "|" + symbolicVersion;
		String osgiFileName = symbolicName + "." + symbolicVersion + ".jar";
		boolean isPack200 = false;

		// first we look for an exact match
		InputStream is = getClass().getResourceAsStream("bundles/" + osgiFileName);
		if (is == null) is = getClass().getResourceAsStream("/bundles/" + osgiFileName);

		if (is != null) System.out.println( "Found [/bundles/" + osgiFileName + "] in lucee.jar");
		else System.out.println("Could not find [/bundles/" + osgiFileName + "] in lucee.jar");

		if (is == null) {
			is = findPack200CompressedBundle(osgiFileName);

			if (is != null) System.out.println( "Found [/bundles/" + osgiFileName + pack20Ext + "] in lucee.jar");
			else System.out.println( "Could not find [/bundles/" + osgiFileName + pack20Ext + "] in lucee.jar");
		}
		if (is != null) {
			File temp = null;
			try {
				// copy to temp file
				temp = File.createTempFile("bundle", ".tmp");
				System.out.println( "Copying [lucee.jar!/bundles/" + osgiFileName + pack20Ext + "] to [" + temp + "]");
				Util.copy(new BufferedInputStream(is), new FileOutputStream(temp), true, true);

				if (isPack200) temp = unpackCompressedJar(temp);

				// adding bundle
				File trg = new File(bundleDirectory, osgiFileName);
				fileMove(temp, trg);
				System.out.println( "Adding bundle [" + symbolicName + "] in version [" + symbolicVersion + "] to [" + trg + "]");
				return trg;
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
			finally {
				if (temp != null && temp.exists()) temp.delete();
			}
		}

		// now we search the current jar as an external zip what is slow (we do not support pack200 in this
		// case)
		// this also not works with windows
		if (isWindows()) return null;
		ZipEntry entry;
		File temp;
		CodeSource src = CFMLEngineFactory.class.getProtectionDomain().getCodeSource();
		if (src == null) return null;
		URL loc = src.getLocation();
		try(
			ZipInputStream zis = new ZipInputStream(loc.openStream());
		) {

			
			String path, name, bundleInfo;
			int index;
			while ((entry = zis.getNextEntry()) != null) {
				temp = null;
				path = entry.getName().replace('\\', '/');
				if (path.startsWith("/")) path = path.substring(1); // some zip path start with "/" some not
				isPack200 = false;
				if (path.startsWith(sub) && (path.endsWith(".jar") /* || (isPack200=path.endsWith(".jar.pack.gz")) */)) { // ignore non jar files or file from elsewhere
					index = path.lastIndexOf('/') + 1;
					if (index == sub.length()) { // ignore sub directories
						name = path.substring(index);
						temp = null;
						try {
							temp = File.createTempFile("bundle", ".tmp");
							Util.copy(zis, new FileOutputStream(temp), false, true);

							bundleInfo = getKeyNameForJar(temp);
							if (bundleInfo != null && nameAndVersion.equals(bundleInfo)) {
								File trg = new File(bundleDirectory, name);
								temp.renameTo(trg);
								logger.log(Logger.LOG_DEBUG, "Adding bundle [" + symbolicName + "] in version [" + symbolicVersion + "] to [" + trg + "]");

								return trg;
							}
						}
						finally {
							if (temp != null && temp.exists()) temp.delete();
						}

					}
				}
				zis.closeEntry();
			}
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
		}
		return null;
	}

	private File unpackCompressedJar(File temp) throws IOException {
		File temp2 = File.createTempFile("bundle", ".tmp2");
		Pack200Util.pack2Jar(temp, temp2);
		logger.log(Logger.LOG_DEBUG, "Upack [" + temp + "] to [" + temp2 + "]");
		temp.delete();
		temp = temp2;
		return temp;
	}

	private InputStream findPack200CompressedBundle( String osgiFileName ){
		InputStream is = getClass().getResourceAsStream("bundles/" + osgiFileName + pack20Ext);
		if (is == null) is = getClass().getResourceAsStream("/bundles/" + osgiFileName + pack20Ext);
		return is;
	}

	// FUTURE move to Util class
	private final void fileMove(File src, File dest) throws IOException {
		boolean moved = src.renameTo(dest);
		if (!moved) {
			try(
				BufferedInputStream is = new BufferedInputStream(new FileInputStream(src));
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(dest));
			) {
				Util.copy(is, os, false, false); // is set false here, because copy does not close in case of an exception
			}
			if (!src.delete()) src.deleteOnExit();
		}
	}

	// public File downloadBundle(final String symbolicName, final String symbolicVersion, Identification id) throws IOException {

	// 	// before we download we check if we have it bundled
	// 	File jar = deployBundledBundle(bundleDirectory, symbolicName, symbolicVersion);
	// 	if (jar != null && jar.isFile()) return jar;
	// 	if (jar != null) {
	// 		logger.log( Logger.LOG_INFO,  jar + " should exist but does not (exist?" + jar.exists() + ";file?" + jar.isFile() + ";hidden?" + jar.isHidden() + ")");
	// 	}

	// 	String str = Util._getSystemPropOrEnvVar("lucee.enable.bundle.download", null);
	// 	if (str != null && ("false".equalsIgnoreCase(str) || "no".equalsIgnoreCase(str))) { // we do not use CFMLEngine to cast, because the engine may not exist yet
	// 		throw (new RuntimeException("Lucee is missing the Bundle jar, " + symbolicName + ":" + symbolicVersion
	// 				+ ", and has been prevented from downloading it. If this jar is not a core jar, it will need to be manually downloaded and placed in the {{lucee-server}}/context/bundles directory."));
	// 	}

	// 	jar = new File(jarDir, symbolicName.replace('.', '-') + "-" + symbolicVersion.replace('.', '-') + (".jar"));

	// 	final URL updateProvider = getUpdateLocation();
	// 	// if (id == null && engine != null) id = engine.getIdentification();

	// 	final URL updateUrl = new URL(updateProvider, "/rest/update/provider/download/" + symbolicName + "/" + symbolicVersion + "/" + (id != null ? id.toQueryString() : "")
	// 			+ (id == null ? "?" : "&") + "allowRedirect=true&jv=" + System.getProperty("java.version")

	// 	);
	// 	logger.log( Logger.LOG_INFO,  "Downloading bundle [" + symbolicName + ":" + symbolicVersion + "] from " + updateUrl + " and copying to " + jar);

	// 	int code;
	// 	HttpURLConnection conn;
	// 	try {
	// 		conn = (HttpURLConnection) updateUrl.openConnection();
	// 		conn.setRequestMethod("GET");
	// 		conn.setConnectTimeout(10000);
	// 		conn.connect();
	// 		code = conn.getResponseCode();
	// 	}
	// 	catch (UnknownHostException e) {
	// 		logger.log(Logger.LOG_ERROR, "Failed to download the bundle  [" + symbolicName + ":" + symbolicVersion + "] from [" + updateUrl + "] and copy to [" + jar + "]"); // MUST
	// 																																									// remove
	// 		throw new IOException("Failed to download the bundle  [" + symbolicName + ":" + symbolicVersion + "] from [" + updateUrl + "] and copy to [" + jar + "]", e);
	// 	}
	// 	// the update provider is not providing a download for this
	// 	if (code != 200) {

	// 		// the update provider can also provide a different (final) location for this
	// 		int count = 1;
	// 		while ((code == 302 || code == 301) && count++ <= MAX_REDIRECTS) {
	// 			String location = conn.getHeaderField("Location");
	// 			// just in case we check invalid names
	// 			if (location == null) location = conn.getHeaderField("location");
	// 			if (location == null) location = conn.getHeaderField("LOCATION");
	// 			logger.log( Logger.LOG_INFO,  "download redirected:" + location);

	// 			conn.disconnect();
	// 			URL url = new URL(location);
	// 			try {
	// 				conn = (HttpURLConnection) url.openConnection();
	// 				conn.setRequestMethod("GET");
	// 				conn.setConnectTimeout(10000);
	// 				conn.connect();
	// 				code = conn.getResponseCode();
	// 			}
	// 			catch (final UnknownHostException e) {
	// 				// logger.log(e);
	// 				throw new IOException("Failed to download the bundle  [" + symbolicName + ":" + symbolicVersion + "] from [" + location + "] and copy to [" + jar + "]", e);
	// 			}
	// 		}

	// 		// no download available!
	// 		if (code != 200) {
	// 			final String msg = "Failed to download the bundle for [" + symbolicName + "] in version [" + symbolicVersion + "] from [" + updateUrl
	// 					+ "], please download manually and copy to [" + jarDir + "]";
	// 			logger.log(Logger.LOG_ERROR, msg);
	// 			conn.disconnect();
	// 			throw new IOException(msg);
	// 		}

	// 	}

	// 	// if(jar.createNewFile()) {
	// 	copy((InputStream) conn.getContent(), new FileOutputStream(jar));
	// 	conn.disconnect();
	// 	return jar;
	// }

	public URL getUpdateLocation() {
		URL location = null;
		// location = CFMLEngineImpl.getInstance().getUpdateLocation();

		// read location directly from xml
		if (location == null) {
			try {
				// TODO: Get this working
				// final File xml = new File(this.serverRoot, "context/lucee-server.xml");
				// if (xml.exists() || xml.length() > 0) {
				// 	final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				// 	final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				// 	final Document doc = dBuilder.parse(xml);
				// 	final Element root = doc.getDocumentElement();

				// 	final NodeList children = root.getChildNodes();

				// 	for (int i = children.getLength() - 1; i >= 0; i--) {
				// 		final Node node = children.item(i);
				// 		if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("update")) {
				// 			final String loc = ((Element) node).getAttribute("location");
				// 			if (!Util.isEmpty(loc)) location = new URL(loc);
				// 		}
				// 	}
				// }
				// if there is no lucee-server.xml
				if (location == null) location = new URL(UPDATE_LOCATION);
			} catch( MalformedURLException e){
				throw new RuntimeException(e);
			}
		}

		return location;
	}

	private boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.startsWith("windows");
	}

	public void removeBundles(final BundleContext bc) throws BundleException {
		final Bundle[] bundles = bc.getBundles();
		for (final Bundle bundle: bundles)
			removeBundle(bundle);
	}

	public void removeBundles(final BundleCollection bc) throws BundleException {
		BundleContext bcc = bc.getBundleContext();
		final Bundle[] bundles = bcc == null ? new Bundle[0] : bcc.getBundles();

		// stop
		for (final Bundle bundle: bundles) {
			if (!BundleUtil.isSystemBundle(bundle)) {
				stopBundle(bundle);
			}
		}
		// uninstall
		for (final Bundle bundle: bundles) {
			if (!BundleUtil.isSystemBundle(bundle)) {
				uninstallBundle(bundle);
			}
		}
	}

	public void removeBundlesEL(final BundleCollection bc) {
		BundleContext bcc = bc.getBundleContext();
		final Bundle[] bundles = bcc == null ? new Bundle[0] : bcc.getBundles();

		for (final Bundle bundle: bundles) {
			if (!BundleUtil.isSystemBundle(bundle)) {
				try {
					stopBundle(bundle);
				}
				catch (final BundleException e) {
					e.printStackTrace();
				}
			}
		}
		for (final Bundle bundle: bundles) {
			if (!BundleUtil.isSystemBundle(bundle)) {
				try {
					uninstallBundle(bundle);
				}
				catch (final BundleException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void removeBundle(final Bundle bundle) throws BundleException {
		stopBundle(bundle);
		uninstallBundle(bundle);
	}

	public void uninstallBundle(final Bundle bundle) throws BundleException {
		if (bundle == null) return;

		if (bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.STARTING || bundle.getState() == Bundle.STOPPING) stopBundle(bundle);

		if (bundle.getState() != Bundle.UNINSTALLED) {
			bundle.uninstall();
		}
	}

	public void stopBundle(final Bundle bundle) throws BundleException {
		if (bundle == null) return;

		// wait for starting/stopping
		int sleept = 0;
		while (bundle.getState() == Bundle.STOPPING || bundle.getState() == Bundle.STARTING) {
			try {
				Thread.sleep(10);
			}
			catch (final InterruptedException e) {
				break;
			}
			sleept += 10;
			if (sleept > 5000) break; // only wait for 5 seconds
		}

		// force stopping (even when still starting)
		if (bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.STARTING) BundleUtil.stop(bundle, false);

	}

}