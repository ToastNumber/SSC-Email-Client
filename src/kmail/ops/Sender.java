package kmail.ops;

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

import kmail.auth.Authorisation;

public class Sender {
	private Session session;
	private static final String SMTP_HOST = "smtp.gmail.com";
	private Authorisation auth;
	
	public Sender(Authorisation auth) {
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
	 * @param attachment
	 *            the attachment file for the email
	 * @return a MIME message constructed using the given information
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public MimeMessage constructMimeMessage(String to, String cc,
			String subject, String content, File[] attachments) throws AddressException, MessagingException {
		MimeMessage message = new MimeMessage(session);
		
		message.setFrom(new InternetAddress(auth.getUsername()));

		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		if (cc.length() > 0) message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));

		message.setSubject(subject);

		
		if (attachments == null) {
			message.setText(content);
		} else {
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(content);

			ArrayList<MimeBodyPart> attachmentsBodyParts = new ArrayList<>();
			
			for (File f : attachments) {
				DataSource attachmentSource = new FileDataSource(f);
				MimeBodyPart attachmentBodyPart = new MimeBodyPart();
				attachmentBodyPart.setDataHandler(new DataHandler(attachmentSource));
				attachmentBodyPart.setFileName(f.getName());
				
				attachmentsBodyParts.add(attachmentBodyPart);
			}
			
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			
			for (MimeBodyPart c : attachmentsBodyParts) {
				multipart.addBodyPart(c);
			}

			message.setContent(multipart);
		}

		message.saveChanges();

		return message;
	}
	
	public void sendEmail(MimeMessage message) throws MessagingException {
		Transport tr = session.getTransport("smtp");
		tr.connect(SMTP_HOST, auth.getUsername(), auth.getPassword());
		tr.sendMessage(message, message.getAllRecipients());
		tr.close();
	}
	
	private Session getSMTPSession() {
		Properties props = System.getProperties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", SMTP_HOST);
		props.put("mail.smtp.port", "587");

		return Session.getDefaultInstance(props);
	}
}
