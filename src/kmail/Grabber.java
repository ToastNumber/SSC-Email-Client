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
import javax.mail.search.HeaderTerm;
import javax.mail.search.SearchTerm;

import kmail.auth.Authorisation;

import com.sun.mail.imap.IMAPFolder;

/**
 * Provides functions for accessing the inbox of an email account.
 * 
 * @author Kelsey McKenna
 *
 */
public class Grabber {
	private Store store;
	private IMAPFolder inbox;

	/**
	 * Opens the inbox of the email account with the specified credentials.
	 * 
	 * @param auth
	 *            the credentials for the email account
	 * @throws MessagingException
	 */
	public Grabber(Authorisation auth) throws MessagingException {
		Session session = getIMAPSession(auth.getUsername(), auth.getPassword());

		store = session.getStore("imaps");
		store.connect("imap.googlemail.com", auth.getUsername(), auth.getPassword());

		inbox = (IMAPFolder) store.getFolder("inbox");
		inbox.open(Folder.READ_WRITE);
	}

	/**
	 * Closes the inbox and the store connection.
	 * 
	 * @throws MessagingException
	 */
	public void close() throws MessagingException {
		inbox.close(false);
		store.close();
	}

	/**
	 * Applies the specified filter to the relevant emails in the inbox (all
	 * emails are considered).
	 * 
	 * @param filter
	 *            the filter to be applied.
	 * @throws MessagingException
	 */
	public void applyCustomFilter(Filter filter) throws MessagingException {
		// Create a Flags object that wraps the filter's flag name.
		Flags customFlag = new Flags(filter.getFlagName());
		String[] keywords = filter.getKeywords();

		// Go through each keyword in the filter
		for (int i = 0; i < keywords.length; ++i) {
			// Find the messages that contain this keyword
			Message[] messages = this.search(keywords[i]);
			for (Message msg : messages) {
				// Apply the flag to each of the messages
				msg.setFlags(customFlag, true);
			}
		}
	}

	/**
	 * Applies each of the specified filters to only the unseen messages.
	 * 
	 * @param filters
	 *            the filters to be applied.
	 * @throws MessagingException
	 */
	public void applyFiltersToUnseen(ArrayList<Filter> filters) throws MessagingException {
		Message[] unseenMessages = inbox.search(new SearchTerm() {
			@Override
			public boolean match(Message m) {
				try {
					// Check if the flags for this message contains the seen
					// flag.
					Flags flags = m.getFlags();
					return !flags.contains(Flag.SEEN);
				} catch (MessagingException e) {
					e.printStackTrace();
				}

				return false;
			}
		});

		// Go through each of the filters
		for (Filter filter : filters) {
			// Create a Flags object for the filter name
			Flags customFlag = new Flags(filter.getFlagName());
			String[] keywords = filter.getKeywords();

			// Now go through each of the unseen messages
			for (Message msg : unseenMessages) {
				// And for each keyword
				for (String keyword : keywords) {
					// Check if the message contains that keyword
					if (searchMessage(msg, keyword)) {
						// And if it does, then give it the flag.
						msg.setFlags(customFlag, true);
					}
				}
			}
		}
	}

	/**
	 * @return the messages in the inbox
	 * @throws MessagingException
	 */
	public Message[] getInboxMessages() throws MessagingException {
		Message[] svar = inbox.getMessages();
		// To get newest first
		Util.reverse(svar);
		return svar;
	}

	/**
	 * @param keyword
	 *            the keyword to search for
	 * @return the list of messages which contain the specified keyword. About
	 *         to type
	 * @throws MessagingException
	 */
	public Message[] search(String keyword) throws MessagingException {
		SearchTerm st = new SearchTerm() {
			@Override
			public boolean match(Message m) {
				return searchMessage(m, keyword);
			}
		};

		return inbox.search(st);
	}

	/**
	 * @param m
	 *            the message to be searched
	 * @param keyword
	 *            the keyword to find
	 * @return <b>true</b> if the message contains the keyword<br>
	 *         <b>false</b> otherwise
	 */
	public boolean searchMessage(Message m, String keyword) {
		// Convert the keyword to lowercase for normalised searching
		final String lowerKeyword = keyword.toLowerCase();

		try {
			// Save the seen-state of the message, as the
			// following code will change its seen-state to true.
			Flags flags = m.getFlags();
			boolean seen = flags.contains(Flag.SEEN);

			boolean svar = false;

			// If the subject contains the keyword then set the result to true.
			if (m.getSubject().toLowerCase().contains(lowerKeyword)) {
				svar = true;
			} else {
				// If the content type if plain text then
				if (m.getContentType().toUpperCase().contains("TEXT/PLAIN")) {
					// Directly check if the content contains the keyword
					svar = m.getContent().toString().toLowerCase().contains(lowerKeyword);
				} else {
					// Get all parts of the email
					Multipart multipart = (Multipart) m.getContent();

					// Go through each part
					for (int i = 0; i < multipart.getCount(); i++) {
						BodyPart bodyPart = multipart.getBodyPart(i);
						// If the current body part is plain text then
						if (bodyPart.getContentType().toUpperCase().contains("TEXT/PLAIN")) {
							// Directly check if the content contains the
							// keyword
							if (bodyPart.getContent().toString().toLowerCase().contains(lowerKeyword)) {
								svar = true;
							}
						}
					}
				}
			}

			// Reset the seen-state of the message
			m.setFlag(Flag.SEEN, seen);

			return svar;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		// If there was an exception, just return false.
		return false;
	}

	/**
	 * @param username
	 *            the username for the email account
	 * @param password
	 *            the password for the email account
	 * @return the IMAP session for the email with the given username and
	 *         password
	 */
	private Session getIMAPSession(String username, String password) {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		props.setProperty("mail.user", username);
		props.setProperty("mail.password", password);

		return Session.getDefaultInstance(props);
	}

	/**
	 * @param m
	 *            the message whose subject will be returned
	 * @return the subject line of the given message. If there is no subject,
	 *         return the empty string.
	 * @throws MessagingException
	 */
	public static String getSubject(Message m) throws MessagingException {
		String temp = m.getSubject();
		return temp == null ? "" : temp;
	}

	/**
	 * @param m
	 *            the message whose <i>from</i> line will be returned.
	 * @return the <i>from</i> line of the message. Returns the first contact if
	 *         there are multiple, or the empty string if there are no contacts.
	 * @throws MessagingException
	 */
	public static String getFrom(Message m) throws MessagingException {
		// Get the list of from addresses
		Address[] froms = m.getFrom();

		if (froms == null || froms.length == 0) return "";
		else {
			String personal = ((InternetAddress) froms[0]).getPersonal();
			String address = ((InternetAddress) froms[0]).getAddress();

			if (personal == null) {
				if (address == null) return "";
				else return address;
			} else return personal;
		}
	}

	/**
	 * @param m
	 *            the message whose sent date will be returned
	 * @return the sent date of the message. <br>
	 *         <ul>
	 *         <li>If it was sent in the past 24 hours, then return in the
	 *         format hh:mm</li>
	 * 
	 *         <li>Otherwise, if it was sent in the past week, then return in
	 *         the format "xxx dd/mm", where xxx is the abbreviated name of the
	 *         day, e.g. Sun 08/11</li>
	 * 
	 *         <li>Otherwise return in the format dd/mm/yyyy</li>
	 *         </ul>
	 * @throws MessagingException
	 */
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

	/**
	 * @param m
	 *            the message whose date will be returned
	 * @return the date the message was sent in the format
	 *         "xxx dd/mm/yyyy hh:mm", where xxx is the abbreviated name of the
	 *         day.
	 * @throws MessagingException
	 */
	public static String getFullDate(Message m) throws MessagingException {
		Date sentDate = m.getSentDate();
		if (sentDate == null) return "";
		else return String.format("%1$ta %1$td/%1$tm/%1$tY %1$tH:%1$tM", m.getSentDate());
	}

	/**
	 * @param m
	 *            the message whose seen-state will be returned
	 * @return <b>true</b> if the message has been seen<br>
	 *         <b>false</b> otherwise
	 * @throws MessagingException
	 */
	public static boolean isSeen(Message m) throws MessagingException {
		Flags flags = m.getFlags();
		return flags.contains(Flag.SEEN);
	}

	/**
	 * Toggle the seen-state of the specified message, i.e. if the message has
	 * been seen, then set its seen-state to false; if the message hasn't been
	 * seen, set its seen-state to true.
	 * 
	 * @param m
	 *            the message whose seen-state will be toggled
	 * @throws MessagingException
	 */
	public static void toggleSeen(Message m) throws MessagingException {
		Flags flags = m.getFlags();
		m.setFlag(Flag.SEEN, !flags.contains(Flag.SEEN));
	}
}