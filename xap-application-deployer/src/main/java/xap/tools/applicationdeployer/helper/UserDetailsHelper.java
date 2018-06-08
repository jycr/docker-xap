package xap.tools.applicationdeployer.helper;

import org.openspaces.admin.pu.config.UserDetailsConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class UserDetailsHelper {
	private UserDetailsHelper() {
	}

	public static UserDetailsConfig create(String userLogin, String userPass) {
		if (userLogin == null || userLogin.isEmpty()) {
			return null;
		}

		UserDetailsConfig result = new UserDetailsConfig();
		result.setUsername(userLogin);
		result.setPassword(userPass);
		return result;
	}

	public static UserDetailsConfig createFromUrlEncodedValue(String userLogin, String userPass) {
		if (userLogin == null || userLogin.isEmpty()) {
			return null;
		}
		try {
			return create(
							URLDecoder.decode(userLogin, "UTF-8"),
							URLDecoder.decode(userPass, "UTF-8")
			);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
