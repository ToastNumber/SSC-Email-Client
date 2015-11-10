package kmail.ui;

import java.awt.EventQueue;

import javax.mail.MessagingException;

import kmail.auth.Authorisation;
import kmail.ui.inbox.InboxWindow;

/**
 * The main class for the KMail client. It launches a password dialog and has
 * central control.
 * 
 * @author Kelsey McKenna
 *
 */
public class KMailClient {
	private PasswordDialog pDialog;
	// Stores the path of the file containing the filter rules.
	public static final String FILTER_FILE_PATH = "res/filter-rules.txt";

	/**
	 * Re/starts the KMail client by launching a password dialog.
	 */
	public KMailClient() {
		restart();
	}

	/**
	 * Attempts to open the inbox for the email account with the specified
	 * username and password.
	 * 
	 * @param username
	 *            the username for the email account
	 * @param password
	 *            the password for the email account
	 * @throws MessagingException
	 *             usually if the login credentials are invalid.
	 */
	public void login(String username, String password) throws MessagingException {
		Authorisation auth = new Authorisation(username, password);

		InboxWindow frame = new InboxWindow(this, auth);
		//Close the password dialog
		pDialog.dispose();
		frame.setVisible(true);
	}

	/**
	 * Launch the password dialog
	 */
	public void restart() {
		pDialog = new PasswordDialog(null, true, this);
		pDialog.setVisible(true);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new KMailClient();
			}
		});
	}
}
