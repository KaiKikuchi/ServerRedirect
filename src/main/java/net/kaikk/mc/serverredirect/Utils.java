package net.kaikk.mc.serverredirect;

import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class Utils {
	public static final Pattern ADDRESS_PREVALIDATOR = Pattern.compile("^[A-Za-z0-9-_.:]+$"); // allowed characters in a server address

	public static byte[] generateAddressMessage(String address) {
		final byte[] addressBytes = address.getBytes(StandardCharsets.UTF_8);
		final byte[] message = new byte[addressBytes.length + 1];
		message[0] = 0; // discriminator
		System.arraycopy(addressBytes, 0, message, 1, addressBytes.length);
		return message;
	}

	public static String join(String[] stringArray, int initialIndex) {
		return join(" ", stringArray, initialIndex);
	}

	public static String join(CharSequence separator, String[] stringArray, int initialIndex) {
		final StringJoiner joiner = new StringJoiner(separator);
		for(; initialIndex < stringArray.length; initialIndex++){
			joiner.add(stringArray[initialIndex]);
		}

		return joiner.toString();
	}
}
