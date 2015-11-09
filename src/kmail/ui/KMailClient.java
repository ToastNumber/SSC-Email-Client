package kmail.ui;

import java.awt.EventQueue;

import javax.mail.MessagingException;

import kmail.auth.Authorisation;
import kmail.ui.inbox.InboxWindow;

public class KMailClient {
	/*
	 * Many thanks to http://stackoverflow.com/users/2587435/peeskillet for the
	 * code for the password dialog
	 */
	PasswordDialog pDialog;
	
	public KMailClient() {
		restart();
	}
	
	public void login(String username, String password) throws MessagingException {
		Authorisation auth = new Authorisation(username, password);

		InboxWindow frame = new InboxWindow(this, auth);
		pDialog.dispose();
		frame.setVisible(true);
	}
	
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
