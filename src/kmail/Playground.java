package kmail;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.junit.Test;

import com.sun.mail.imap.IMAPFolder;

public class Playground {
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

		for (int i = messages.length - 1; i >= 0; --i) {
			Message m = messages[i];

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

	/**
	 * @param session
	 * @param from
	 *            the email address which is sending the email
	 * @param to
	 *            a comma-separated list of email addresses to which to send the
	 *            email
	 * @param subject
	 *            the subject for the email
	 * @param content
	 *            the text content of the email
	 * @param attachment
	 *            the attachment file for the email
	 * @return a MIME message constructed using the given information
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public static MimeMessage constructMimeMessage(Session session, String from, String to, String cc,
			String subject, String content, File attachment) throws AddressException, MessagingException {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));

		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		if (cc.length() > 0) message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));

		message.setSubject(subject);

		if (attachment == null) {
			message.setText(content);
		} else {
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(content);

			DataSource attachmentSource = new FileDataSource(attachment);
			MimeBodyPart attachmentBodyPart = new MimeBodyPart();
			attachmentBodyPart.setDataHandler(new DataHandler(attachmentSource));
			attachmentBodyPart.setFileName(attachment.getName());

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			multipart.addBodyPart(attachmentBodyPart);

			message.setContent(multipart);
		}

		message.saveChanges();

		return message;
	}

	public static void sendEmail(Session session, String smtpHost, String username, String password,
			MimeMessage message) throws MessagingException {
		Transport tr = session.getTransport("smtp");
		tr.connect(smtpHost, username, password);
		tr.sendMessage(message, message.getAllRecipients());
	}

	public static Session getIMAPSession(String username, String password) {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		props.setProperty("mail.user", username);
		props.setProperty("mail.password", password);

		return Session.getDefaultInstance(props);
	}

	public static Session getSMTPSession(String username, String password, String smtpHost) {
		Properties props = System.getProperties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", "587");

		return Session.getDefaultInstance(props);
	}

	public void testGetInbox() throws MessagingException {
		String username = "k.m.playground55@googlemail.com";
		String password = "asdasd123$";

		Session session = getIMAPSession(username, password);
		Store store = null;

		store = session.getStore("imaps");
		store.connect("imap.googlemail.com", username, password);
		
		Message[] messages = getInboxMessages(store);
		display(messages);
	}
	
	@Test
	public void testChangeFlag() throws MessagingException {
		String username = "k.m.playground55@googlemail.com";
		String password = "asdasd123$";

		Session session = getIMAPSession(username, password);
		Store store = null;

		store = session.getStore("imaps");
		store.connect("imap.googlemail.com", username, password);
		
		Message[] messages = getInboxMessages(store);
		
		for (int i = 0; i < messages.length; ++i) {
			messages[i].setFlag(Flag.SEEN, i % 2 == 0);
		}
	}

	public void testSendMail() throws AddressException, MessagingException {
		String smtpHost = "smtp.gmail.com";

		String username = "k.m.playground55@googlemail.com";
		String password = "asdasd123$";

		Session session = getSMTPSession(username, password, smtpHost);

		MimeMessage message = constructMimeMessage(session, "k.m.playground55@googlemail.com",
				"kelseyguitar55@googlemail.com", "", "Holy Crap", "Markdown lol", null);

		sendEmail(session, smtpHost, username, password, message);
	}

	public void testSendAttachment() throws AddressException, MessagingException {
		String smtpHost = "smtp.gmail.com";

		String username = "k.m.playground55@googlemail.com";
		String password = "asdasd123$";

		Session session = getSMTPSession(username, password, smtpHost);

		final JFileChooser fc = new JFileChooser();
		int option = fc.showOpenDialog(null);

		if (option == JFileChooser.APPROVE_OPTION) {
			File attachment = fc.getSelectedFile();

			MimeMessage message = constructMimeMessage(session, "k.m.playground55@googlemail.com",
					"kelseyguitar55@googlemail.com", "", "Sending an attachment", "Is there an attachment?",
					attachment);

			sendEmail(session, smtpHost, username, password, message);
		}
	}

	public static Message[] search(Folder folder, String keyword) throws MessagingException {
		SearchTerm st = new SearchTerm() {
			@Override
			public boolean match(Message m) {
				try {
					return m.getContent().toString().contains(keyword) || m.getSubject().contains(keyword);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				}

				return false;
			}
		};

		return folder.search(st);
	}

	public void testSearch() throws MessagingException {
		String username = "k.m.playground55@googlemail.com";
		String password = "asdasd123$";

		Session session = getIMAPSession(username, password);
		Store store = null;

		store = session.getStore("imaps");
		store.connect("imap.googlemail.com", username, password);

		IMAPFolder folder = (IMAPFolder) store.getFolder("inbox");
		if (!folder.isOpen()) folder.open(Folder.READ_ONLY);

		Message[] desiredMessages = search(folder, "Rubik's");
		display(desiredMessages);
	}

}
