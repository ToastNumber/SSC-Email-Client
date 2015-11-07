package kmail.ops;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags.Flag;
import javax.mail.internet.InternetAddress;
import javax.mail.search.SearchTerm;

import kmail.Util;
import kmail.auth.Authorisation;

import com.sun.mail.imap.IMAPFolder;

public class Grabber {
	private Store store;
	
	public Grabber(Authorisation auth) throws MessagingException {
		Session session = getIMAPSession(auth.getUsername(), auth.getPassword());

		store = session.getStore("imaps");
		store.connect("imap.googlemail.com", auth.getUsername(), auth.getPassword());
	}
	
	public Message[] getInboxMessages() throws MessagingException {
		IMAPFolder folder = (IMAPFolder) store.getFolder("inbox");
		if (!folder.isOpen()) folder.open(Folder.READ_WRITE);
		Message[] svar = folder.getMessages();
		Util.reverse(svar); // To get newest first
		return svar;
	}
	
	public Message[] search(String keyword) throws MessagingException {
		Folder folder = store.getFolder("inbox");
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
	
	private Session getIMAPSession(String username, String password) {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		props.setProperty("mail.user", username);
		props.setProperty("mail.password", password);

		return Session.getDefaultInstance(props);
	}
	
	public static String getSubject(Message m) throws MessagingException {
		String temp = m.getSubject();
		return temp == null ? "" : temp;
	}
	
	public static String getFrom(Message m) throws MessagingException {
		Address[] froms = m.getFrom();
		return froms == null ? null : ((InternetAddress) froms[0]).getPersonal();
	}
	
	public static String getDateSent(Message m) throws MessagingException {
		long nowTime = (new Date()).getTime();
		
		String sentDateString = "";
		Date sentDate = m.getSentDate();
		long numDaysElapsed = (nowTime - sentDate.getTime())
				/ (1000 * 60 * 60 * 24);
		if (numDaysElapsed < 1) {
			sentDateString = String.format("%1$tH:%1$tM", sentDate);
		} else if (numDaysElapsed < 7) {
			sentDateString = String.format("%1$ta %1$td/%1$tm", sentDate);
		} else {
			sentDateString = String.format("%1$td/%1$tm/%1$tY", sentDate);
		}

		return sentDateString;
	}
	
	public static boolean isSeen(Message m) throws MessagingException {
		Flags flags = m.getFlags();
		return flags.contains(Flag.SEEN);
	}
	
	public static void toggleSeen(Message m) throws MessagingException {
		Flags flags = m.getFlags();
		
		m.setFlag(Flag.SEEN, !flags.contains(Flag.SEEN));
	}
}
