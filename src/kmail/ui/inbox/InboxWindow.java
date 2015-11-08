package kmail.ui.inbox;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import kmail.Util;
import kmail.auth.Authorisation;
import kmail.ops.Grabber;
import kmail.ui.send.SendWindow;

public class InboxWindow extends JFrame {

	private JSplitPane splitPane;
	private JPanel contentPane;
	private Message[] messages;
	private Grabber grabber;
	private DefaultListModel<String> model = new DefaultListModel<>();
	private JList<String> messageList;
	private boolean refreshing = false;
	private JScrollPane paneMessageList;
	private JTextPane txtMessageContent;
	private JScrollPane paneMessageContent;
	private JPanel panel;
	private JLabel lblSearch;
	private JTextField fldSearch;
	private JButton btnRead;
	private JButton btnRefresh;
	private JButton btnFilterOptions;
	private JButton btnLogout;
	private JButton btnCompose;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					String username = "k.m.playground55@googlemail.com";
					String password = "asdasd123$";
					Authorisation auth = new Authorisation(username, password);

					InboxWindow frame = new InboxWindow(auth);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws MessagingException 
	 */
	public InboxWindow(Authorisation auth) throws MessagingException {
		this.grabber = new Grabber(auth);

		setTitle("KMail");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 882, 647);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.1);
		contentPane.add(splitPane);

		messageList = new JList<>(model);
		messageList.setCellRenderer(new MessageStripRenderer());
		messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		paneMessageList = new JScrollPane(messageList);
		splitPane.setLeftComponent(paneMessageList);

		txtMessageContent = new JTextPane();
		txtMessageContent.setFont(new Font("arial", 0, 14));
		txtMessageContent.setEditable(false);
		txtMessageContent.setContentType("text/html");

		paneMessageContent = new JScrollPane(txtMessageContent);
		splitPane.setRightComponent(paneMessageContent);
		messageList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (refreshing) return;
				
				int index = messageList.getSelectedIndex();
				
				try {
					displayMessageContent(index);
					refreshMessageStrip(index);
				} catch (MessagingException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
		});
		
		panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		lblSearch = new JLabel("Search: ");
		panel.add(lblSearch);

		fldSearch = new JTextField(20);
		fldSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					filterInbox(fldSearch.getText());
				} catch (MessagingException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.add(fldSearch);

		btnCompose = new JButton("Compose");
		btnCompose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SendWindow sendWindow = new SendWindow(auth);
				sendWindow.setVisible(true);
			}
		});
		btnCompose.setFocusable(false);
		panel.add(btnCompose);
		
		btnRead = new JButton("Unread/Read");
		btnRead.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = messageList.getSelectedIndex();
				try {
					Grabber.toggleSeen(messages[index]);
					refreshMessageStrip(index);
				} catch (MessagingException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnRead.setFocusable(false);
		panel.add(btnRead);

		btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					refreshInbox();
				} catch (MessagingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnRefresh.setFocusable(false);
		panel.add(btnRefresh);

		btnFilterOptions = new JButton("Filter Options");
		btnFilterOptions.setFocusable(false);
		panel.add(btnFilterOptions);

		btnLogout = new JButton("Logout");
		btnLogout.setFocusable(false);
		panel.add(btnLogout);

		/************ FINAL OPERATIONS **************/
		try {
			refreshInbox();
		} catch (MessagingException e1) {
			e1.printStackTrace();
		}
	}

	private void refreshInbox() throws MessagingException {
		filterInbox("");
		fldSearch.setText("");
	}
	
	private void filterInbox(String keyword) throws MessagingException {
		refreshing = true;
		
		if (keyword.isEmpty()) {
			messages = grabber.getInboxMessages();
		} else {
			messages = grabber.search(keyword);
		}

		this.messageList.clearSelection();
		
		model.clear();
		model.setSize(messages.length);
		
		for (int i = 0; i < messages.length; ++i) {
			refreshMessageStrip(i);
		}
		
		refreshing = false;		
	}
	
	private void refreshMessageStrip(int i) throws MessagingException {
		Message m = messages[i];
		
		String from = Grabber.getFrom(m);
		String subject = Grabber.getSubject(m);
		String dateSent = Grabber.getDateSent(m);
		boolean seen = Grabber.isSeen(m);
		
		model.set(i, Util.constructMessageStrip(from, subject, dateSent, seen));
	}

	private void displayMessageContent(int index)
			throws MessagingException, IOException {
		Message message = messages[index];

		String svar = String.format(
				"<html>"
				+ "<h1 style='font-family: helvetica'>%s</h1>"
				+ "<p style='font-family: arial'>From: %s</p>"
				+ "<p style='font-family: arial'>Received: %s</p>"
				+ "<br>"
				+ "<hr size=2>"
				+ "<br>",
				Grabber.getSubject(message), Grabber.getFrom(message), Grabber.getFullDate(message));

		svar += "<span style='font-family: arial'>";
		
		if (message.getContentType().contains("TEXT/PLAIN")) {
			svar += "" + message.getContent();
		} else {
			// How to get parts from multiple body parts of MIME message
			Multipart multipart = (Multipart) message.getContent();

			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				// If the part is a plain text message, then print it out.
				if (bodyPart.getContentType().contains("TEXT/PLAIN")) {
					String temp = bodyPart.getContent().toString();
					svar += temp.replaceAll("\n", "<br>");
				}
			}
		}
		
		svar += "</span></html>";

		txtMessageContent.setText(svar);
		txtMessageContent.setCaretPosition(0);
	}
	

}
