package lucee.runtime.osgi;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.felix.framework.Logger;
import org.osgi.framework.BundleException;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Identification;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;

public class BundleDownloader {

    /**
     * The update provider to download bundles from.
     */
	private final String UPDATE_LOCATION = "https://update.lucee.org";  // MUST from server.xml

    /**
     * Max number of redirects to follow when downloading a bundle and the URL returns 301 Found
     */
	private static final int MAX_REDIRECTS = 5;

    /**
     * Lucee logger to use, probably "application" log
     * 
     * @see lucee.commons.io.log.Log;
     */
    private Log logger;
    private File bundleDirectory;
    private URL updateLocation;

	public BundleDownloader( Log logger, File bundleDirectory ){
		try{
			this.logger = logger;
			// default update location... should get from server configuration.
			this.updateLocation = new URL(UPDATE_LOCATION);
			this.bundleDirectory = bundleDirectory;
		} catch( Exception e ){
			throw new RuntimeException(e);
		}
	}

    /**
     * Download the bundle from the bundle provider
     * @param factory
     * @param symbolicName
     * @param symbolicVersion
     * @param id
     * @return
     * @throws IOException
     * @throws BundleException
     */
    public Resource downloadBundle(
        final String symbolicName,
        String symbolicVersion,
        Identification id
    ) throws IOException, BundleException {
        validateDownloadsEnabled(symbolicName, symbolicVersion);

        final Resource jarDir = ResourceUtil.toResource(bundleDirectory);
        final URL updateProvider = updateLocation;
        if (symbolicVersion == null) symbolicVersion = "latest";
        final URL updateUrl = getDownloadURL(symbolicName, symbolicVersion, id, updateProvider);
        logger.log(Logger.LOG_INFO, "OSGI", "Downloading bundle [" + symbolicName + ":" + symbolicVersion + "] from [" + updateUrl + "]");

        HttpURLConnection conn = establishConnection(updateUrl, symbolicName, symbolicVersion);
        // the update provider is not providing a download for this
        if (conn.getResponseCode() != 200) {
            int count = 1;
            // the update provider can also provide a different (final) location for this
            while ((conn.getResponseCode() == 301 || conn.getResponseCode() == 302) && count++ <= MAX_REDIRECTS) {
                String location = conn.getHeaderField("Location");
                // just in case we check invalid names
                if (location == null) location = conn.getHeaderField("location");
                if (location == null) location = conn.getHeaderField("LOCATION");
                LogUtil.log(Log.LEVEL_INFO, OSGiUtil.class.getName(), "Download redirected: " + location); // MUST remove

                conn.disconnect();
                conn = establishConnection( new URL(location), symbolicName, symbolicVersion );
            }

            // no download available!
            if (conn.getResponseCode() != 200) {
                final String msg = "Download bundle failed for [" + symbolicName + "] in version [" + symbolicVersion + "] from [" + updateUrl
                        + "], please download manually and copy to [" + jarDir + "]";
                logger.log(Logger.LOG_ERROR, "OSGI", msg);
                conn.disconnect();
                throw new IOException(msg);
            }

        }

        // extract version if necessary
        if ("latest".equals(symbolicVersion)) {
            // copy to temp file
            Resource temp = SystemUtil.getTempFile("jar", false);
            IOUtil.copy((InputStream) conn.getContent(), temp, true);
            try {
                conn.disconnect();

                // extract version and create file with correct name
                BundleFile bf = BundleFile.getInstance(temp);
                Resource jar = jarDir.getRealResource(symbolicName + "-" + bf.getVersionAsString() + ".jar");
                IOUtil.copy(temp, jar);
                return jar;
            }
            finally {
                temp.delete();
            }
        }
        else {
            Resource jar = jarDir.getRealResource(symbolicName + "-" + symbolicVersion + ".jar");
            IOUtil.copy((InputStream) conn.getContent(), jar, true);
            conn.disconnect();
            return jar;
        }
    }

    /**
     * Build and return a URL pointing to the proper download location of the bundle we are looking for.
     * 
     * @param bundleName Name of the bundle to download
     * @param bundleVersion Version of the bundle to download
     * @param bundleID ID of the bundle to download. Optional.
     * @param updateProvider URL to the bundle provider we'd like to download from.
     * @return The fully-formed URL to the bundle on the bundle provider web host.
     * @throws MalformedURLException
     */
    private static URL getDownloadURL(final String bundleName, String bundleVersion, Identification bundleID, final URL updateProvider) throws MalformedURLException {
        String path = "/rest/update/provider/download/"
            + bundleName
            + "/"
            + bundleVersion
            + "/"
            + (bundleID != null ? bundleID.toQueryString() : "")
            + (bundleID == null ? "?" : "&") + "allowRedirect=true";
        return new URL(updateProvider, path);
    }

    /**
     * Check the <code>lucee.enable.bundle.download</code> system property to ensure that bundle downloading is enabled.
     * 
     * @throws java.lang.RuntimeException if bundle downloading is disabled.
     * 
     * @param bundleName Name of the bundle, for debugging purposes
     * @param bundleVersion Bundle version, for debugging purposes
     */
    private void validateDownloadsEnabled(final String bundleName, String bundleVersion) {
        if (!Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.enable.bundle.download", null), true)) {
            boolean printExceptions = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.cli.printExceptions", null), false);
            String bundleError = "Lucee is missing the Bundle jar [" + (bundleVersion != null ? bundleName + ":" + bundleVersion : bundleName)
                    + "], and has been prevented from downloading it. If this jar is not a core jar,"
                    + " it will need to be manually downloaded and placed in the {{lucee-server}}/context/bundles directory.";
            try {
                throw new RuntimeException(bundleError);
            }
            catch (RuntimeException re) {
                if (printExceptions) re.printStackTrace();
                throw re;
            }
        }
    }

    /**
     * Establish an HTTP connection to the download location. On error, log and rethrow with a useful error message.
     * 
     * @param downloadLocation URL to establish a connection to for downloading the bundle.
     * @param symbolicName Bundle name for error messages
     * @param symbolicVersion Bundle version for error messages
     * @return An HTTP connection to use for downloading the bundle.
     * @throws IOException
     */
    private HttpURLConnection establishConnection(URL downloadLocation, String symbolicName, String symbolicVersion) throws IOException{
        try {
            HttpURLConnection conn = (HttpURLConnection) downloadLocation.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.connect();
            return conn;
        }
        catch (IOException e) {
            logger.log(Logger.LOG_ERROR, "OSGI", "Unable to connect to bundle provider: " + downloadLocation.toString(), e);
            throw new IOException("Failed to download the bundle  [" + symbolicName + ":" + symbolicVersion + "] from [" + downloadLocation.toString() + "]", e);
        }
    }
}
