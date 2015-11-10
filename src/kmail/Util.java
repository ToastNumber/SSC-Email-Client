package kmail;

/**
 * Provides some common utility functions for the rest of the application.
 * 
 * @author Kelsey McKenna
 *
 */
public class Util {
	/**
	 * Restricts the given string to the given length and appends "..." <br>
	 * E.g. restrict("Bananas", 5) would give "Banan..."
	 * 
	 * @param s
	 *            the string to restrict
	 * @param length
	 *            the length to restrict it to
	 * @return the restricted string
	 */
	public static String restrict(String s, int length) {
		if (s.length() <= length) return s;
		else {
			return s.substring(0, length) + "...";
		}
	}

	/**
	 * Constructs the HTML content for the message strip/thumbnail in an inbox. 
	 * @param from the text to be displayed for the person who sent the email
	 * @param subject the subject line of the message
	 * @param time the time the message was sent
	 * @param read whether or not the message has been read. If unread, then it will be displayed in bold.
	 * @param flags the flags for the messaage, e.g. 'spam'
	 * @return the formatted text for the message
	 */
	public static String constructMessageStrip(String from, String subject, String time, boolean read, String[] flags) {
		String text = "<html>";

		if (read) {
			text += String.format("<span style='font-family: arial'>%s</span>", from);
			text += String.format("<span style='font-family: arial'> - <i>%s</i> </span>", time);
		} else {
			text += String.format("<span style='font-family: arial'><b>%s</b></span>", from);
			text += String.format("<span style='font-family: arial'> - <b><i>%s</i></b> </span>", time);
		}

		text += "<br>";
		text += String.format("<span style='font-family: arial'>%s</span>", subject);

		for (int i = 0; i < flags.length; ++i) {
			text += String.format(" <span style='font-family: courier'><b><i>[%s]</i><b></span>", flags[i]);
		}

		text += "</html>";

		return text;
	}

	/**
	 * Reverses the given array
	 * @param arr the array to be reversed
	 */
	public static <T> void reverse(T[] arr) {
		for (int i = 0, j = arr.length - 1; i < arr.length / 2; ++i, --j) {
			T temp = arr[i];
			arr[i] = arr[j];
			arr[j] = temp;
		}
	}

}
