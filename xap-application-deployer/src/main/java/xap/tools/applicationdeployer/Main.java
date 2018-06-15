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
	private static final String PROP_LOOKUP_GROUPS_ENV = "XAP_LOOKUP_GROUPS";
	private static final String PROP_LOOKUP_GROUPS_DEFAULT = System.getenv().getOrDefault(PROP_LOOKUP_GROUPS_ENV, "xap");

	private static final String PROP_LOOKUP_LOCATORS = "lookup.locators";
	private static final String PROP_LOOKUP_LOCATORS_ENV = "XAP_LOOKUP_LOCATORS";
	private static final String PROP_LOOKUP_LOCATORS_DEFAULT = System.getenv().getOrDefault(PROP_LOOKUP_LOCATORS_ENV, "");

	private static final String PROP_LOG_LEVEL_ROOT = "log.level.root";
	private static final String PROP_LOG_LEVEL_ROOT_DEFAULT = Level.INFO.getName();

	private static final String PROP_TIMEOUT = "timeout";
	private static final String PROP_TIMEOUT_DEFAULT = "PT1M";

	private static final String USAGE = "args: <zipFile> (<propsFile>)"
			+ "\nAvailable system properties:"
			+ "\n -D" + PROP_LOOKUP_GROUPS + " (comma separated multi-values. Default value (cf. env:" + PROP_LOOKUP_GROUPS_ENV + ") : " + PROP_LOOKUP_GROUPS_DEFAULT + ")"
			+ "\n -D" + PROP_LOOKUP_LOCATORS + " (comma separated multi-values. Default (cf. env:" + PROP_LOOKUP_LOCATORS_ENV + ") : " + PROP_LOOKUP_LOCATORS_DEFAULT + ")"
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
		setupLogger(logLevel);

		String[] locators = System.getProperty(PROP_LOOKUP_LOCATORS, PROP_LOOKUP_LOCATORS_DEFAULT).split(",");
		String[] groups = System.getProperty(PROP_LOOKUP_GROUPS, PROP_LOOKUP_GROUPS_DEFAULT).split(",");
		Duration timeout = Duration.parse(System.getProperty(PROP_TIMEOUT, PROP_TIMEOUT_DEFAULT));

		LOG.info("ZIP: {}\n"
						+ "\nOptions:"
						+ "\n -D" + PROP_LOOKUP_GROUPS + " : {}"
						+ "\n -D" + PROP_LOOKUP_LOCATORS + " : {}"
						+ "\n -D" + PROP_LOG_LEVEL_ROOT + " : {}"
						+ "\n -D" + PROP_TIMEOUT + " : {}"
				, zipFile
				, Arrays.toString(locators)
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
				.locators(locators)
				.groups(groups)
				.timeout(timeout)
				.userDetails(userDetails)
				.create();

		ApplicationConfig applicationConfig = appDeployBuilder.create();

		xapHelper.undeployIfExists(applicationConfig.getName());

		xapHelper.deploy(applicationConfig);
	}

	private static void setupLogger(String levelStr) {
		levelStr = levelStr == null ? "" : levelStr.toUpperCase().trim();
		if (levelStr.isEmpty()) {
			return;
		}
		if ("DEBUG".equalsIgnoreCase(levelStr)) {
			levelStr = Level.FINE.getName();
		} else if ("TRACE".equalsIgnoreCase(levelStr)) {
			levelStr = Level.FINEST.getName();
		} else if ("warn".equalsIgnoreCase(levelStr)) {
			levelStr = Level.WARNING.getName();
		} else if ("err".equalsIgnoreCase(levelStr) || "error".equalsIgnoreCase(levelStr) || "critical".equalsIgnoreCase(levelStr)) {
			levelStr = Level.SEVERE.getName();
		}
		Level level;
		try {
			level = Level.parse(levelStr);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return;
		}
		for (String loggerName : Arrays.asList(
				"",
				"java.util.logging.ConsoleHandler",
				"com.gigaspaces.logger.RollingFileHandler",
				"com.gigaspaces.exceptions",
				"com.gigaspaces.common.spaceurl",
				"com.gigaspaces.common.spacefinder",
				"com.gigaspaces.common.lookupfinder",
				"com.gigaspaces.common.resourceloader",
				"com.gigaspaces.space",
				"com.gigaspaces.space.engine",
				"com.gigaspaces.space.operations",
				"com.gigaspaces.space.typemanager",
				"com.gigaspaces.space.active-election",
				"com.gigaspaces.space.active-election.xbackup",
				"com.gigaspaces.cache",
				"com.gigaspaces.container",
				"com.gigaspaces.filters",
				"com.gigaspaces.query",
				"com.gigaspaces.jms",
				"com.gigaspaces.kernel",
				"com.gigaspaces.worker.multicast",
				"com.gigaspaces.spring",
				"com.gigaspaces.metadata",
				"com.gigaspaces.metadata.pojo",
				"com.gigaspaces.os.statistics",
				"com.gigaspaces.security",
				"com.gigaspaces.jmx",
				"com.gigaspaces.core.common",
				"com.gigaspaces.core.config",
				"com.gigaspaces.core.lease",
				"com.gigaspaces.core.lookupmanager",
				"com.gigaspaces.core.notify",
				"com.gigaspaces.core.fifo",
				"com.gigaspaces.core.cluster.partition",
				"com.gigaspaces.core.xa",
				"com.gigaspaces.core.classloadercleaner",
				"com.gigaspaces.core.classloadercache",
				"com.gigaspaces.lrmi.nio.filters.SSLFilterFactory",
				"com.gigaspaces.lrmi",
				"com.gigaspaces.lrmi.stubcache",
				"com.gigaspaces.lrmi.context",
				"com.gigaspaces.lrmi.marshal",
				"com.gigaspaces.lrmi.watchdog",
				"com.gigaspaces.lrmi.classloading",
				"com.gigaspaces.lrmi.slow_consumer",
				"com.gigaspaces.lrmi.exporter",
				"com.gigaspaces.lrmi.filters",
				"com.gigaspaces.persistent",
				"com.gigaspaces.persistent.shared_iterator",
				"com.gigaspaces.replication.channel",
				"com.gigaspaces.replication.channel.verbose",
				"com.gigaspaces.replication.replica",
				"com.gigaspaces.replication.node",
				"com.gigaspaces.replication.router",
				"com.gigaspaces.replication.group",
				"com.gigaspaces.replication.backlog",
				"com.gigaspaces.metrics.manager",
				"com.gigaspaces.metrics.registry",
				"com.gigaspaces.metrics.sampler",
				"com.gigaspaces.client",
				"com.gigaspaces.spaceproxy.router",
				"com.gigaspaces.spaceproxy.router.lookup",
				"org.openspaces",
				"org.openspaces.pu.container.support",
				"org.openspaces.pu.container.jee.context.ProcessingUnitWebApplicationContext",
				"org.openspaces.esb.mule",
				"com.sun.jini.mahalo.startup",
				"com.sun.jini.mahalo.destroy",
				"com.sun.jini.reggie",
				"com.sun.jini.start.service.starter",
				"com.sun.jini.thread.TaskManager",
				"net.jini.discovery.LookupLocatorDiscovery",
				"net.jini.lookup.ServiceDiscoveryManager",
				"net.jini.discovery.LookupDiscovery",
				"net.jini.lookup.JoinManager",
				"net.jini.config",
				"org.jini.rio.tools.webster",
				"org.springframework",
				"org.hibernate",
				"org.mule",
				"org.mule.MuleServer",
				"org.mule.RegistryContext",
				"org.eclipse.jetty",
				"com.gigaspaces.license",
				"com.gigaspaces.admin",
				"com.gigaspaces.admin.ui",
				"com.gigaspaces.admin.ui.cluster.view",
				"com.gigaspaces.admin.ui.spacebrowser",
				"com.gigaspaces.admin.cli",
				"com.gigaspaces.metrics",
				"com.gigaspaces.replication.gateway",
				"com.gigaspaces.externaldatasource.dotnet",
				"com.gigaspaces.bridge.dispatcher",
				"com.gigaspaces.bridge.pbsexecuter",
				"com.gigaspaces.dotnet.pu",
				"com.gigaspaces.cpp.proxy",
				"com.gigaspaces.localcache",
				"com.gigaspaces.localview",
				"com.gigaspaces.webui.admin",
				"com.gigaspaces.webui.rest",
				"com.gigaspaces.webui.common",
				"com.gigaspaces.webui.lifecycle",
				"com.gigaspaces.webui.security",
				"com.gigaspaces.webui.statistics.puinstance",
				"com.gigaspaces.webui.statistics.spaceinstance",
				"com.gigaspaces.webui.statistics.vm",
				"com.gigaspaces.webui.statistics.os",
				"com.gigaspaces.webui.statistics.provider",
				"com.gigaspaces.webui.spacemode",
				"com.gigaspaces.webui.alerts",
				"com.gigaspaces.webui.pu.events",
				"com.gigaspaces.webui.pui.status.events",
				"com.gigaspaces.webui.elastic.events",
				"com.gigaspaces.webui.runtimeinfo",
				"com.gigaspaces.webui.remote.activities",
				"org.jini.rio",
				"com.gigaspaces.start",
				"com.gigaspaces.management",
				"com.gigaspaces.grid.lookup",
				"com.gigaspaces.manager",
				"com.gigaspaces.grid.gsc",
				"com.gigaspaces.grid.gsm",
				"com.gigaspaces.grid.gsm.peer",
				"com.gigaspaces.grid.gsm.feedback",
				"com.gigaspaces.grid.gsm.provision",
				"com.gigaspaces.grid.gsm.services",
				"com.gigaspaces.grid.gsm.service-instances",
				"com.gigaspaces.grid.gsm.dependency",
				"com.gigaspaces.grid.gsm.selector",
				"com.gigaspaces.grid.gsm.xhandler",
				"com.gigaspaces.grid.gsm.leader",
				"com.gigaspaces.grid.gsa",
				"org.openspaces.grid.gsm",
				"org.openspaces.grid.gsm.containers",
				"org.openspaces.grid.gsm.rebalancing",
				"org.openspaces.grid.gsm.machines",
				"com.gigaspaces.grid.gsc.GSCFaultDetectionHandler",
				"com.gigaspaces.grid.gsm.GSMFaultDetectionHandler",
				"org.openspaces.pu.container.servicegrid.PUFaultDetectionHandler",
				"org.openspaces.admin.lifecycle"
		)) {
			setupLogger(loggerName, level);
		}
	}

	private static void setupLogger(String loggerName, Level level) {
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(loggerName);
		LOG.debug("Set log level: {} = {}", loggerName, level.getName());
		logger.setLevel(level);
	}

}
