package kmail;

/**
 * Represents a flag name and a list of keywords that. If a piece of text
 * contains any of the keywords, then the flag name would be applied to the
 * text.
 * 
 * @author Kelsey McKenna
 *
 */
public class Filter {
	private String flag;
	private String[] keywords;

	public Filter(String flag, String[] keywords) {
		if (flag == null || keywords == null) throw new NullPointerException();
		else {
			this.flag = flag;
			this.keywords = trim(keywords);
		}
	}

	/**
	 * @param arr
	 *            the array of strings to be trimmed
	 * @return a list of trimmed strings, i.e. strings with no leading or
	 *         trailing spaces.
	 */
	private static String[] trim(String[] arr) {
		String[] svar = new String[arr.length];

		for (int i = 0; i < arr.length; ++i) {
			svar[i] = arr[i].trim();
		}

		return svar;
	}

	public String getFlagName() {
		return flag;
	}

	public String[] getKeywords() {
		return keywords;
	}

	/**
	 * Return the filter in the form "[flag]: [keyword 1], ..., [keyword n]"
	 */
	@Override
	public String toString() {
		String svar = flag + ": ";
		for (int i = 0; i < keywords.length; ++i) {
			svar += keywords[i];

			if (i < keywords.length - 1) svar += ", ";
		}

		return svar;
	}

	/**
	 * Returns the filter associated with the string. E.g.
	 * <code>parse("spam: lucky winner, x factor")</code> would return a filter
	 * with the flag name "spam" and keywords {"lucky winner", "x factor"}.
	 * Strict formatting is checked. There must be a space after the colon and
	 * after each comma. There must be at least one keyword
	 * 
	 * @param filter
	 * @return
	 */
	public static Filter parse(String filter) {
		// Check if the string matches the regular expression corresponding to
		// the rules.
		if (!filter.matches(".*: ([^,]+)(, [^,]+)*")) return null;
		else {
			int colonIndex = filter.indexOf(":");
			String flag = filter.substring(0, colonIndex);

			return new Filter(flag, filter.substring(colonIndex + 1).split(","));
		}
	}
}
