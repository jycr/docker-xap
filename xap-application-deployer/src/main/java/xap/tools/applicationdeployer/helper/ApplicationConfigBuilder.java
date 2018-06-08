package xap.tools.applicationdeployer.helper;

import org.openspaces.admin.application.ApplicationFileDeployment;
import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public class ApplicationConfigBuilder {
	private Map<String, String> contextProperties = new HashMap<>();
	private File application;
	private UserDetailsConfig userDetails;


	public ApplicationConfig create() {
		ApplicationConfig applicationConfig = new ApplicationFileDeployment(application).create();
		for (ProcessingUnitConfigHolder puConfig : applicationConfig.getProcessingUnits()) {
			if (!contextProperties.isEmpty()) {
				puConfig.setContextProperties(contextProperties);
			}
			if (userDetails != null) {
				puConfig.setUserDetails(userDetails);
			}
		}
		return applicationConfig;
	}

	public ApplicationConfigBuilder applicationPath(File application) {
		if (!application.isFile()&&!application.isDirectory()) {
			throw new IllegalArgumentException("must be a valid application File or Directory: " + application);
		}
		this.application = application;
		return this;
	}

	public ApplicationConfigBuilder applicationPath(Path store) {
		return this.applicationPath(store.toFile());
	}

	public ApplicationConfigBuilder applicationPath(String applicationPath) {
		return applicationPath(new File(applicationPath));
	}

	public ApplicationConfigBuilder addContextProperties(Path... propertyPaths) {
		return this.addContextProperties(Arrays.asList(propertyPaths));
	}

	public ApplicationConfigBuilder addContextProperties(List<Path> propertyPaths) {
		propertyPaths.forEach(this::addContextProperties);
		return this;
	}

	private ApplicationConfigBuilder addContextProperties(Path propertiesFilepath) {
		Properties props = new Properties();
		try (InputStream inputStream = new FileInputStream(propertiesFilepath.toFile())) {
			props.load(inputStream);
		} catch (IOException e) {
			throw new IllegalArgumentException("Bad property file", e);
		}
		return this.addContextProperties(props);
	}

	public ApplicationConfigBuilder addContextProperties(Map<? extends Object, ? extends Object> props) {
		if (props != null && !props.isEmpty()) {
			props.forEach((k, v) -> contextProperties.put( //
							k == null ? null : String.valueOf(k), //
							v == null ? null : String.valueOf(v) //
			));
		}
		return this;
	}

	public ApplicationConfigBuilder userDetails(UserDetailsConfig userDetails) {
		this.userDetails = userDetails;
		return this;
	}
}
