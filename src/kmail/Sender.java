package kmail;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import kmail.auth.Credentials;

/**
 * Provides functions for sending emails.
 * 
 * @author Kelsey McKenna
 *
 */
public class Sender {
	private Session session;
	private static final String SMTP_HOST = "smtp.gmail.com";
	private Credentials auth;

	public Sender(Credentials auth) {
		this.auth = auth;
		this.session = getSMTPSession();
	}

	/**
	 * @param session
	 * @param from
	 *            the email address which is sending the email
	 * @param to
	 *            a comma-separated list of email addresses to which to send the
	 *            email
	 * @param cc
	 *            a comma-separated list of email addresses for the cc field.
	 * @param subject
	 *            the subject for the email
	 * @param content
	 *            the text content of the email
	 * @param attachments
	 *            the attachments for the email
	 * @return a MIME message constructed using the given information
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public MimeMessage constructMimeMessage(String to, String cc, String subject, String content, File[] attachments)
			throws AddressException, MessagingException {
		MimeMessage message = new MimeMessage(session);

		message.setFrom(new InternetAddress(auth.getUsername()));

		// Set the 'to' recipients
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		// If there are Cc recipients, then add them
		if (cc.length() > 0) {
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
		}

		message.setSubject(subject);

		// If there are no attachments
		if (attachments == null || attachments.length == 0) {
			// Simply add the message content
			message.setText(content);
		} else {
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			// The the first body part to the be the message content
			messageBodyPart.setText(content);

			ArrayList<MimeBodyPart> attachmentsBodyParts = new ArrayList<>();

			// Go through each attachment
			for (File f : attachments) {
				// Create the data source for the attachment
				DataSource attachmentSource = new FileDataSource(f);
				MimeBodyPart attachmentBodyPart = new MimeBodyPart();
				// Give the body part a handler
				attachmentBodyPart.setDataHandler(new DataHandler(attachmentSource));
				attachmentBodyPart.setFileName(f.getName());

				// Add this body part containing the attachment to the list of
				// attachment body parts.
				attachmentsBodyParts.add(attachmentBodyPart);
			}

			// Now add all the parts to the email
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			for (MimeBodyPart c : attachmentsBodyParts) {
				multipart.addBodyPart(c);
			}

			message.setContent(multipart);
		}

		// Save changes to the message before it is returned
		message.saveChanges();

		return message;
	}

	/**
	 * Send the specified message from the email account for this Sender.
	 * 
	 * Many thanks to Dr Shan He for excerpts of the following code.
	 * 
	 * @param message
	 *            the message to be sent
	 * @throws MessagingException
	 */
	public void sendEmail(MimeMessage message) throws MessagingException {
		Transport tr = session.getTransport("smtp");
		tr.connect(SMTP_HOST, auth.getUsername(), auth.getPassword());
		tr.sendMessage(message, message.getAllRecipients());
		tr.close();
	}

	/**
	 * Get the SMTPSession in order to send emails
	 * 
	 * Many thanks to Dr Shan He for excerpts of the following code.
	 * 
	 * @return
	 */
	private Session getSMTPSession() {
		Properties props = System.getProperties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", SMTP_HOST);
		props.put("mail.smtp.port", "587");

		return Session.getInstance(props);
	}
}
