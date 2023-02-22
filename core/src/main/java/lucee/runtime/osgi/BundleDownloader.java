package lucee.runtime.osgi;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

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
     * Max number of redirects to follow when downloading a bundle and the URL returns 301 Found
     */
	private static final int MAX_REDIRECTS = 5;

    public static Resource downloadBundle(
        CFMLEngineFactory factory,
        final String symbolicName,
        String symbolicVersion,
        Identification id
    ) throws IOException, BundleException {
        if (!Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.enable.bundle.download", null), true)) {
            boolean printExceptions = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.cli.printExceptions", null), false);
            String bundleError = "Lucee is missing the Bundle jar [" + (symbolicVersion != null ? symbolicName + ":" + symbolicVersion : symbolicName)
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

        final Resource jarDir = ResourceUtil.toResource(factory.getBundleDirectory());
        final URL updateProvider = factory.getUpdateLocation();
        if (symbolicVersion == null) symbolicVersion = "latest";
        final URL updateUrl = new URL(updateProvider, "/rest/update/provider/download/" + symbolicName + "/" + symbolicVersion + "/" + (id != null ? id.toQueryString() : "")
                + (id == null ? "?" : "&") + "allowRedirect=true"

        );
        log(Logger.LOG_INFO, "Downloading bundle [" + symbolicName + ":" + symbolicVersion + "] from [" + updateUrl + "]");

        int code;
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) updateUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.connect();
            code = conn.getResponseCode();
        }
        catch (UnknownHostException e) {
            throw new IOException("Downloading the bundle  [" + symbolicName + ":" + symbolicVersion + "] from [" + updateUrl + "] failed", e);
        }
        // the update provider is not providing a download for this
        if (code != 200) {
            int count = 1;
            // the update provider can also provide a different (final) location for this
            while ((code == 301 || code == 302) && count++ <= MAX_REDIRECTS) {
                String location = conn.getHeaderField("Location");
                // just in case we check invalid names
                if (location == null) location = conn.getHeaderField("location");
                if (location == null) location = conn.getHeaderField("LOCATION");
                LogUtil.log(Log.LEVEL_INFO, OSGiUtil.class.getName(), "Download redirected: " + location); // MUST remove

                conn.disconnect();
                URL url = new URL(location);
                try {
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.connect();
                    code = conn.getResponseCode();
                }
                catch (final UnknownHostException e) {
                    log(e);
                    throw new IOException("Failed to download the bundle  [" + symbolicName + ":" + symbolicVersion + "] from [" + location + "]", e);
                }
            }

            // no download available!
            if (code != 200) {
                final String msg = "Download bundle failed for [" + symbolicName + "] in version [" + symbolicVersion + "] from [" + updateUrl
                        + "], please download manually and copy to [" + jarDir + "]";
                log(Logger.LOG_ERROR, msg);
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
	private static void log(int level, String msg) {
		try {
			Log log = ThreadLocalPageContext.getLog("application");
			if (log != null) log.log(level, "OSGi", msg);
		}
		catch (Exception t) {
			LogUtil.log(level, BundleBuilderFactory.class.getName(), msg);
		}
	}

	private static void log(Throwable t) {
		try {
			Log log = ThreadLocalPageContext.getLog("application");
			if (log != null) log.log(Log.LEVEL_ERROR, "OSGi", t);
		}
		catch (Exception _t) {
			/* this can fail when called from an old loader */
			LogUtil.log(OSGiUtil.class.getName(), _t);
		}
	}
}
