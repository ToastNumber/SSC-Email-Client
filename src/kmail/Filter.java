package kmail;

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
	
	@Override
	public String toString() {
		String svar = flag + ": ";
		for (int i = 0; i < keywords.length; ++i) {
			svar += keywords[i];
			
			if (i < keywords.length - 1) svar += ", ";
		}
		
		return svar;
	}
	
	public static Filter parse(String filter) {
		if (!filter.matches(".*: ([^,]+)(, [^,]+)*"))
			return null;
		else {
			int colonIndex = filter.indexOf(":");
			String flag = filter.substring(0, colonIndex);

			return new Filter(flag, filter.substring(colonIndex + 1).split(","));
		}
	}
}
