package net.kaikk.mc.serverredirect;

import java.util.regex.Pattern;

public class Utils {
	public static final Pattern ADDRESS_PREVALIDATOR = Pattern.compile("^[A-Za-z0-9-_.:]+$"); // allowed characters in a server address
}
