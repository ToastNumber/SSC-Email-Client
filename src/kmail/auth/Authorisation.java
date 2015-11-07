package kmail.auth;


public class Authorisation {
	public final String username;
	private final String password;

	public Authorisation(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	// public String getPassword() {
	// JPasswordField pass = new JPasswordField(10) {
	// public void addNotify() {
	// super.addNotify();
	// requestFocusInWindow();
	// }
	// };
	//
	// String password = "";
	//
	// int action = JOptionPane.showConfirmDialog(null, pass, "Enter Password",
	// JOptionPane.OK_CANCEL_OPTION);
	// if (action != 0) {
	// JOptionPane.showMessageDialog(null, "Cancel, X or escape key selected");
	// } else password = new String(pass.getPassword());
	//
	// return password;
	// }

}
