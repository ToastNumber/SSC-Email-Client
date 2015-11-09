package kmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.SearchTerm;

import kmail.auth.Authorisation;

import com.sun.mail.imap.IMAPFolder;

public class Grabber {
	private Store store;
	private IMAPFolder inbox;
	
	public Grabber(Authorisation auth) throws MessagingException {
		Session session = getIMAPSession(auth.getUsername(), auth.getPassword());

		store = session.getStore("imaps");
		store.connect("imap.googlemail.com", auth.getUsername(), auth.getPassword());
		
		inbox = (IMAPFolder) store.getFolder("inbox");
		inbox.open(Folder.READ_WRITE);
	}

	public void close() throws MessagingException {
		store.close();
	}

	public void applyCustomFilter(Filter filter) throws MessagingException {
		Flags customFlag = new Flags(filter.getFlagName());
		String[] keywords = filter.getKeywords();

		for (int i = 0; i < keywords.length; ++i) {
			Message[] messages = this.search(keywords[i], true);
			for (Message msg : messages) {
				msg.setFlags(customFlag, true);
			}
		}
	}

	public void applyFiltersToUnseen(ArrayList<Filter> filters) throws MessagingException {
		Message[] unseenMessages = inbox.search(new SearchTerm() {
			@Override
			public boolean match(Message m) {
				try {
					Flags flags = m.getFlags();
					return !flags.contains(Flag.SEEN);
				} catch (MessagingException e) {
					e.printStackTrace();
				}

				return false;
			}
		});

		for (Filter filter : filters) {
			Flags customFlag = new Flags(filter.getFlagName());
			String[] keywords = filter.getKeywords();

			for (Message msg : unseenMessages) {
				for (String keyword : keywords) {
					if (searchMessage(msg, keyword, true)) {
						msg.setFlags(customFlag, true);
					}
				}
			}
		}
	}

	public Message[] getInboxMessages() throws MessagingException {
		Message[] svar = inbox.getMessages();
		Util.reverse(svar); // To get newest first
		return svar;
	}

	public Message[] search(String keyword) throws MessagingException {
		return search(keyword, false);
	}

	public Message[] search(String keyword, boolean writable) throws MessagingException {
		SearchTerm st = new SearchTerm() {
			@Override
			public boolean match(Message m) {
				return searchMessage(m, keyword, writable);
			}
		};

		return inbox.search(st);
	}

	public boolean searchMessage(Message m, String keyword, boolean writable) {
		final String lowerKeyword = keyword.toLowerCase();

		try {
			Flags flags = m.getFlags();
			boolean seen = flags.contains(Flag.SEEN);
			boolean svar = false;

			if (m.getSubject().toLowerCase().contains(lowerKeyword)) {
				svar = true;
			} else {
				if (m.getContentType().toUpperCase().contains("TEXT/PLAIN")) {
					svar = m.getContent().toString().toLowerCase().contains(lowerKeyword);
				} else {
					// How to get parts from multiple body parts of MIME message
					Multipart multipart = (Multipart) m.getContent();

					for (int i = 0; i < multipart.getCount(); i++) {
						BodyPart bodyPart = multipart.getBodyPart(i);
						// If the part is a plain text message, then print it
						// out.
						if (bodyPart.getContentType().toUpperCase().contains("TEXT/PLAIN")) {
							if (bodyPart.getContent().toString().toLowerCase().contains(lowerKeyword)) svar = true;
						}
					}
				}
			}

			if (writable) m.setFlag(Flag.SEEN, seen);

			return svar;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return false;
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

		if (froms == null) return "";
		else {
			String personal = ((InternetAddress) froms[0]).getPersonal();
			String address = ((InternetAddress) froms[0]).getAddress();

			if (personal == null) {
				if (address == null) return "";
				else return address;
			} else return personal;
		}
	}

	public static String getDateSent(Message m) throws MessagingException {
		long nowTime = (new Date()).getTime();

		String sentDateString = "";
		Date sentDate = m.getSentDate();
		long numDaysElapsed = (nowTime - sentDate.getTime()) / (1000 * 60 * 60 * 24);
		if (numDaysElapsed < 1) {
			sentDateString = String.format("%1$tH:%1$tM", sentDate);
		} else if (numDaysElapsed < 7) {
			sentDateString = String.format("%1$ta %1$td/%1$tm", sentDate);
		} else {
			sentDateString = String.format("%1$td/%1$tm/%1$tY", sentDate);
		}

		return sentDateString;
	}

	public static String getFullDate(Message m) throws MessagingException {
		Date sentDate = m.getSentDate();
		if (sentDate == null) return "";
		else return String.format("%1$ta %1$td/%1$tm/%1$tY %1$tH:%1$tM", m.getSentDate());
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
