package kmail;

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
		if (s.length() <= length)
			return s;
		else {
			return s.substring(0, length) + "...";
		}
	}

	public static String constructMessageStrip(String from, String subject,
			String time, boolean read, String[] flags) {
		String text = "<html>";

		if (read) {
			text += String.format("<span style='font-family: arial'>%s</span>",
					from);
			text += String.format(
					"<span style='font-family: arial'> - <i>%s</i> </span>",
					time);
		} else {
			text += String.format(
					"<span style='font-family: arial'><b>%s</b></span>", from);
			text += String
					.format("<span style='font-family: arial'> - <b><i>%s</i></b> </span>",
							time);
		}

		text += "<br>";
		text += String.format("<span style='font-family: arial'>%s</span>",
				subject);

		for (int i = 0; i < flags.length; ++i) {
			text += String
					.format(" <span style='font-family: courier'><b><i>[%s]</i><b></span>",
							flags[i]);
		}

		text += "</html>";

		return text;
	}

	public static <T> void reverse(T[] arr) {
		for (int i = 0, j = arr.length - 1; i < arr.length / 2; ++i, --j) {
			T temp = arr[i];
			arr[i] = arr[j];
			arr[j] = temp;
		}
	}

}
