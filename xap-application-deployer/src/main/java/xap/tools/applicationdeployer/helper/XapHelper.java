package xap.tools.applicationdeployer.helper;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class XapHelper {
    private static final Logger LOG = LoggerFactory.getLogger(XapHelper.class);

    // By default : 1 minutes
    private Duration timeout = Duration.of(1, ChronoUnit.MINUTES);
    private final GridServiceManager gsm;
    private UserDetailsConfig userDetails;

    public XapHelper(GridServiceManager gsm) {
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
        long start = System.currentTimeMillis();
        LOG.info("Launch deploy of: {} (timeout: {})", applicationConfig.getName(), timeout);

        Application dataApp = gsm.deploy(applicationConfig);

        long end = System.currentTimeMillis() + timeout.toMillis();

        LOG.info("waiting availability of PUs: ", dataApp.getProcessingUnits().getNames().keySet());

        for (ProcessingUnit pu : dataApp.getProcessingUnits()) {
            long remaining = end - System.currentTimeMillis();
            if ((remaining < 0L) ||
                    (!pu.waitFor(pu.getPlannedNumberOfInstances(), remaining, TimeUnit.MILLISECONDS))) {
                throw new TimeoutException("Application " + applicationConfig.getName() + " deployment timed out after "
                        + timeout);
            }
        }
        LOG.info("Application deployed in: {}", Duration.of(System.currentTimeMillis() - start, ChronoUnit.MILLIS));
        LOG.info("Deployed PUs: {}", dataApp.getProcessingUnits().getNames().keySet());
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

    public static GridServiceManager getGsm(String locators, String[] groups, UserDetailsConfig userDetails) {
        AdminFactory factory = new AdminFactory().useDaemonThreads(true);

        if (locators != null && !locators.isEmpty()) {
            factory.addLocators(locators);
        }
        if (groups != null && groups.length > 0) {
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

        LOG.info("XAP admin: {}", admin);

        return admin.getGridServiceManagers().waitForAtLeastOne(5, TimeUnit.MINUTES);
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
        private String locator;
        private String[] groups;
        private UserDetailsConfig userDetails;
        private Duration timeout;

        public Builder locatorName(String locator) {
            this.locator = locator;
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

        public Builder timeout(long milliseconds) {
            return this.timeout(Duration.of(milliseconds, ChronoUnit.MILLIS));
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public XapHelper create() {
            GridServiceManager gsm = getGsm(locator, groups, userDetails);
            LOG.info("XAP gsm: {}", gsm);
            return new XapHelper(gsm) //
                    .setTimeout(timeout) //
                    .setUserDetails(userDetails) //
                    ;
        }
    }
}
