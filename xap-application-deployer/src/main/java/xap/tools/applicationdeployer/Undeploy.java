package xap.tools.applicationdeployer;

import org.openspaces.admin.pu.config.UserDetailsConfig;
import xap.tools.applicationdeployer.helper.UserDetailsHelper;
import xap.tools.applicationdeployer.helper.XapHelper;

import java.time.Duration;

public class Undeploy {
	private static final String PROP_CREDENTIAL_USERNAME = "credential.username";
	private static final String PROP_CREDENTIAL_SECRET = "credential.password";

	private static final String PROP_LOOKUP_GROUP = "lookup.group";
	private static final String PROP_LOOKUP_GROUP_DEFAULT = "localhost";

	private static final String PROP_LOOKUP_LOCATOR = "lookup.locator";
	private static final String PROP_LOOKUP_LOCATOR_DEFAULT = "localhost";

	private static final String PROP_TIMEOUT = "timeout";
	private static final String PROP_TIMEOUT_DEFAULT = "PT1M";

	private static final String USAGE = "args: <applicationName>\n" +
					"Available system properties:\n" +
					" -D" + PROP_CREDENTIAL_USERNAME + " (URL Encoded value)\n" +
					" -D" + PROP_CREDENTIAL_SECRET + " (URL Encoded value)\n" +
					" -D" + PROP_LOOKUP_GROUP + " (comma separated multi-values. Default value: " + PROP_LOOKUP_GROUP_DEFAULT + ")\n" +
					" -D" + PROP_LOOKUP_LOCATOR + " (Default value: " + PROP_LOOKUP_LOCATOR_DEFAULT + ")\n" +
					" -D" + PROP_TIMEOUT + " (Default value: " + PROP_TIMEOUT_DEFAULT + ")\n" +
					"";

	public static void main(String... args) throws Exception {
		if (args.length < 1) {
			throw new IllegalArgumentException(USAGE);
		}
		String applicationName = args[0];

		UserDetailsConfig userDetails = UserDetailsHelper.createFromUrlEncodedValue(
						System.getProperty(PROP_CREDENTIAL_USERNAME),
						System.getProperty(PROP_CREDENTIAL_SECRET, "")
		);

		XapHelper xapHelper = new XapHelper.Builder()
						.locatorName(System.getProperty(PROP_LOOKUP_LOCATOR, PROP_LOOKUP_LOCATOR_DEFAULT))
						.groups(System.getProperty(PROP_LOOKUP_GROUP, PROP_LOOKUP_GROUP_DEFAULT).split(","))
						.timeout(Duration.parse(System.getProperty(PROP_TIMEOUT, PROP_TIMEOUT_DEFAULT)))
						.userDetails(userDetails)
						.create();

		xapHelper.undeploy(applicationName);
	}
}