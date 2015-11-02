package kmail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import com.sun.mail.imap.IMAPFolder;

public class Test {
	public static String getPassword() {
		JPasswordField pass = new JPasswordField(10) {
			public void addNotify() {
				super.addNotify();
				requestFocusInWindow();
			}
		};

		String password = "";

		int action = JOptionPane.showConfirmDialog(null, pass, "Enter Password", JOptionPane.OK_CANCEL_OPTION);
		if (action != 0) {
			JOptionPane.showMessageDialog(null, "Cancel, X or escape key selected");
			System.exit(0);
		} else password = new String(pass.getPassword());

		return password;
	}

	public static Message[] getInboxMessages(Store store) throws MessagingException {
		IMAPFolder folder = (IMAPFolder) store.getFolder("inbox");
		if (!folder.isOpen()) folder.open(Folder.READ_WRITE);
		return folder.getMessages();
	}

	public static String restrict(String s, int length) {
		if (s.length() <= length) return s;
		else {
			return s.substring(0, length) + "...";
		}
	}

	public static void display(Message[] messages) throws MessagingException {
		long nowTime = (new Date()).getTime();

		Flags flag = new Flags("asdasd");

		for (int i = messages.length - 1; i >= 0; --i) {
			Message m = messages[i];

			m.getFlags().add(flag);
			m.saveChanges();
			
			m.setFlags(flag, true);

			// Get the subject of the email
			// If null, set it to the empty string
			String temp = m.getSubject();
			String subject = temp == null ? "" : temp;

			String fromEmailMessage = "";
			Address[] froms = m.getFrom();
			fromEmailMessage = froms == null ? null : ((InternetAddress) froms[0]).getPersonal();

			String sentDateMessage = "";
			Date sentDate = m.getSentDate();
			long numDaysElapsed = (nowTime - sentDate.getTime()) / (1000 * 60 * 60 * 24);
			if (numDaysElapsed < 1) {
				sentDateMessage = String.format("%1$ta %1$tH:%1$tM", sentDate);
			} else if (numDaysElapsed < 7) {
				sentDateMessage = String.format("%1$ta %1$td/%1$tm", sentDate);
			} else {
				sentDateMessage = String.format("%1$td/%1$tm/%1$tY", sentDate);
			}

			Flags flags = m.getFlags();
			String flagMessage = flags.contains(Flag.SEEN) ? "Seen" : "Unseen";
			flagMessage += flags.contains(Flag.RECENT) ? ", Recent" : "";

			System.out.printf("%-6s | %-13s - %s - %s%n", flagMessage, restrict(subject, 10), fromEmailMessage,
					sentDateMessage);
		}
	}

	public static void main(String[] args) {
		String username = "KJM409@student.bham.ac.uk";
		String password = getPassword();

		// Step 1.1: Set all Properties
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		props.setProperty("mail.user", username);
		props.setProperty("mail.password", password);

		Session session = Session.getDefaultInstance(props);
		Store store = null;

		try {
			store = session.getStore("imaps");
			store.connect("outlook.office365.com", username, password);
			Message[] messages = getInboxMessages(store);

			display(messages);

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		System.out.println("Terminated");
	}
}
