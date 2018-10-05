package xap.tools.applicationdeployer.helper;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class XapHelper {

	private static final Logger LOG = LoggerFactory.getLogger(XapHelper.class);

	public static void awaitDeployment(ApplicationConfig applicationConfig, Application dataApp, long deploymentStartTime, Duration timeout) throws TimeoutException {
		long timeoutTime = deploymentStartTime + timeout.toMillis();

		final String applicationConfigName = applicationConfig.getName();
		LOG.info("Waiting for application {} to deploy ...", applicationConfigName);

		Set<String> deployedPuNames = new LinkedHashSet<>();

		for (ProcessingUnit pu : dataApp.getProcessingUnits().getProcessingUnits()) {
			final int plannedNumberOfInstances = pu.getPlannedNumberOfInstances();
			String puName = pu.getName();
			LOG.info("Waiting for PU {} to deploy {} instances ...", puName, plannedNumberOfInstances);

			long remainingDelayUntilTimeout = timeoutTime - System.currentTimeMillis();
			if (remainingDelayUntilTimeout < 0L) {
				throw new TimeoutException("Application " + applicationConfigName + " deployment timed out after " + timeout);
			}
			boolean finished = pu.waitFor(plannedNumberOfInstances, remainingDelayUntilTimeout, TimeUnit.MILLISECONDS);

			final int currentInstancesCount = pu.getInstances().length;
			LOG.info("PU {} now has {} running instances", puName, currentInstancesCount);

			if (!finished) {
				throw new TimeoutException("Application " + applicationConfigName + " deployment timed out after " + timeout);
			}
			deployedPuNames.add(puName);
			LOG.info("PU {} deployed successfully", puName);
		}

		long appDeploymentEndTime = System.currentTimeMillis();
		long appDeploymentDuration = appDeploymentEndTime - deploymentStartTime;

		LOG.info("Deployed PUs: {}", deployedPuNames);
		LOG.info("Application deployed in: {} ms", appDeploymentDuration);
	}

	// By default : 1 minutes
	private Duration timeout = Duration.of(1, ChronoUnit.MINUTES);
	private final GridServiceManagers gsm;
	private UserDetailsConfig userDetails;

	public XapHelper(GridServiceManagers gsm) {
		this.gsm = gsm;
	}

	public XapHelper setTimeout(Duration timeout) {
		this.timeout = timeout;
		return this;
	}

	public XapHelper setUserDetails(UserDetailsConfig userDetails) {
		this.userDetails = userDetails;
		return this;
	}

	public void deploy(ApplicationConfig applicationConfig) throws TimeoutException {
		LOG.info("Launch deployment of: {} [{}] (timeout: {})"
				, new Object[]{
						applicationConfig.getName()
						, stream(applicationConfig.getProcessingUnits())
						.map(ProcessingUnitConfigHolder::getName)
						.collect(joining(","))
						, timeout
				}
		);

		long deployRequestStartTime = System.currentTimeMillis();
		Application dataApp = gsm.deploy(applicationConfig);
		long deployRequestEndTime = System.currentTimeMillis() + timeout.toMillis();
		long deployRequestDuration = deployRequestEndTime - deployRequestStartTime;
		LOG.info("Requested deployment of application : duration = {} ms", deployRequestDuration);

		long deploymentStartTime = deployRequestEndTime;

		awaitDeployment(applicationConfig, dataApp, deploymentStartTime, timeout);
	}


	public void undeploy(String applicationName) {
		LOG.info("Launch undeploy of: {} (timeout: {})", applicationName, timeout);
		retrieveApplication(
				applicationName,
				timeout,
				application -> {
					LOG.info("Undeploying application: {}", applicationName);
					application.undeployAndWait(timeout.toMillis(), TimeUnit.MILLISECONDS);
					LOG.info("{} has been successfully undeployed.", applicationName);
				},
				appName -> {
					throw new IllegalStateException(new TimeoutException(
							"Application " + appName + " discovery timed-out. Check if application is deployed."));
				}
		);
	}

	public void retrieveApplication(String name, Duration timeout, Consumer<Application> ifFound, Consumer<String> ifNotFound) {
		Application application = gsm.getAdmin().getApplications().waitFor(name, timeout.toMillis(), TimeUnit.MILLISECONDS);
		if (application == null) {
			ifNotFound.accept(name);
		} else {
			ifFound.accept(application);
		}
	}

	public void undeployIfExists(String name) {
		retrieveApplication(
				name,
				Duration.of(1, ChronoUnit.SECONDS),
				app -> undeploy(app.getName()),
				appName -> {
				});
	}


	public static class Builder {
		private String[] locators;
		private String[] groups;
		private UserDetailsConfig userDetails;
		private Duration timeout;

		public Builder locators(String... locators) {
			this.locators = locators;
			return this;
		}

		public Builder groups(String... groups) {
			this.groups = groups;
			return this;
		}

		public Builder userDetails(UserDetailsConfig userDetails) {
			this.userDetails = userDetails;
			return this;
		}

		public Builder timeout(Duration timeout) {
			this.timeout = timeout;
			return this;
		}

		public XapHelper create() {
			GridServiceManagers gsm = createGsm();
			LOG.info("GridServiceManagers: {}", Arrays.toString(gsm.getManagers()));
			LOG.info("GridServiceManagers: {}", Arrays.toString(gsm.getManagers()));
			return new XapHelper(gsm) //
					.setTimeout(timeout) //
					.setUserDetails(userDetails) //
					;
		}


		public GridServiceManagers createGsm() {
			AdminFactory factory = new AdminFactory().useDaemonThreads(true);

			if (locators != null) {
				for (String locator : locators) {
					if (!locator.isEmpty()) {
						factory.addLocator(locator);
					}
				}
			}
			if (groups != null) {
				for (String group : groups) {
					if (!group.isEmpty()) {
						factory.addGroup(group);
					}
				}
			}
			if (userDetails != null) {
				factory = factory.credentials(userDetails.getUsername(), userDetails.getPassword());
			}

			Admin admin = factory.createAdmin();

			LOG.info("Using Admin> locators: {} ; groups: {}"
					, stream(admin.getLocators())
							.map(l -> l.getHost() + ":" + l.getPort())
							.collect(joining(","))
					, stream(admin.getGroups())
							.collect(joining(","))
			);
			return admin.getGridServiceManagers();
//			GridServiceManager gsm = admin.getGridServiceManagers().waitForAtLeastOne(5, TimeUnit.MINUTES);
//			LOG.info("Retrieved GridServiceManager> locators: {} ; groups: {}");
//			return gsm;
		}
	}
}
