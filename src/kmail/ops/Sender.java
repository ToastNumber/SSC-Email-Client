package kmail.ops;

import java.io.File;
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

public class Sender {
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
		tr.close();
	}
	
	public Session getSMTPSession(String username, String password, String smtpHost) {
		Properties props = System.getProperties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", "587");

		return Session.getDefaultInstance(props);
	}
}
