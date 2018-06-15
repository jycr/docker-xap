package xap.tools.applicationdeployer;

import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xap.tools.applicationdeployer.helper.ApplicationConfigBuilder;
import xap.tools.applicationdeployer.helper.UserDetailsHelper;
import xap.tools.applicationdeployer.helper.XapHelper;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.logging.Level;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	private static final String PROP_CREDENTIAL_USERNAME = "credential.username";
	private static final String PROP_CREDENTIAL_SECRET = "credential.password";

	private static final String PROP_LOOKUP_GROUPS = "lookup.groups";
	private static final String PROP_LOOKUP_GROUPS_DEFAULT = "localhost";

	private static final String PROP_LOOKUP_LOCATORS = "lookup.locators";
	private static final String PROP_LOOKUP_LOCATORS_DEFAULT = "localhost";

	private static final String PROP_LOG_LEVEL_ROOT = "log.level.root";
	private static final String PROP_LOG_LEVEL_ROOT_DEFAULT = Level.INFO.getName();

	private static final String PROP_TIMEOUT = "timeout";
	private static final String PROP_TIMEOUT_DEFAULT = "PT1M";

	private static final String USAGE = "args: <zipFile> (<propsFile>)"
			+ "\nAvailable system properties:"
			+ "\n -D" + PROP_LOOKUP_GROUPS + " (comma separated multi-values. Default value: " + PROP_LOOKUP_GROUPS_DEFAULT + ")"
			+ "\n -D" + PROP_LOOKUP_LOCATORS + " (comma separated multi-values. Default value: " + PROP_LOOKUP_LOCATORS_DEFAULT + ")"
			+ "\n -D" + PROP_CREDENTIAL_USERNAME + " (URL Encoded value)"
			+ "\n -D" + PROP_CREDENTIAL_SECRET + " (URL Encoded value)"
			+ "\n -D" + PROP_LOG_LEVEL_ROOT + " (Default value: " + PROP_LOG_LEVEL_ROOT_DEFAULT + ")"
			+ "\n -D" + PROP_TIMEOUT + " (ISO-8601 Duration. Default value: " + PROP_TIMEOUT_DEFAULT + ")";

	public static void main(String... args) throws Exception {
		if (args.length < 1) {
			throw new IllegalArgumentException(USAGE);
		}
		String zipFile = args[0];

		String logLevel = System.getProperty(PROP_LOG_LEVEL_ROOT, PROP_LOG_LEVEL_ROOT_DEFAULT);
		setupLogger("", logLevel);

		String[] locator = System.getProperty(PROP_LOOKUP_LOCATORS, PROP_LOOKUP_LOCATORS_DEFAULT).split(",");
		String[] groups = System.getProperty(PROP_LOOKUP_GROUPS, PROP_LOOKUP_GROUPS_DEFAULT).split(",");
		Duration timeout = Duration.parse(System.getProperty(PROP_TIMEOUT, PROP_TIMEOUT_DEFAULT));

		LOG.info("ZIP: {}\n"
						+ "\nOptions:"
						+ "\n -D" + PROP_LOOKUP_GROUPS + " : {}"
						+ "\n -D" + PROP_LOOKUP_LOCATORS + " : {}"
						+ "\n -D" + PROP_LOG_LEVEL_ROOT + " : {}"
						+ "\n -D" + PROP_TIMEOUT + " : {}"
				, zipFile
				, Arrays.toString(locator)
				, Arrays.toString(groups)
				, logLevel
				, timeout
		);

		UserDetailsConfig userDetails = UserDetailsHelper.createFromUrlEncodedValue(
				System.getProperty(PROP_CREDENTIAL_USERNAME),
				System.getProperty(PROP_CREDENTIAL_SECRET, "")
		);

		ApplicationConfigBuilder appDeployBuilder = new ApplicationConfigBuilder()
				.applicationPath(zipFile)
				.userDetails(userDetails);

		if (args.length > 1) {
			appDeployBuilder.addContextProperties(Paths.get(args[1]));
		}

		XapHelper xapHelper = new XapHelper.Builder()
				.locatorName(locator)
				.groups(groups)
				.timeout(timeout)
				.userDetails(userDetails)
				.create();

		ApplicationConfig applicationConfig = appDeployBuilder.create();

		xapHelper.undeployIfExists(applicationConfig.getName());

		xapHelper.deploy(applicationConfig);
	}

	private static void setupLogger(String loggerName, String level) {
		level = level == null ? "" : level.toUpperCase().trim();
		if (level.isEmpty()) {
			return;
		}
		if ("DEBUG".equalsIgnoreCase(level)) {
			level = Level.FINE.getName();
		} else if ("TRACE".equalsIgnoreCase(level)) {
			level = Level.FINEST.getName();
		} else if ("warn".equalsIgnoreCase(level)) {
			level = Level.WARNING.getName();
		} else if ("err".equalsIgnoreCase(level) || "error".equalsIgnoreCase(level) || "critical".equalsIgnoreCase(level)) {
			level = Level.SEVERE.getName();
		}
		java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger(loggerName);
		try {
			Level newLevel = Level.parse(level);
			if (!rootLogger.getLevel().equals(newLevel)) {
				System.out.println("Set log level to: " + newLevel.getName());
				rootLogger.setLevel(Level.parse(level));
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
}
