package kmail.ui.inbox;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import kmail.Filter;
import kmail.Grabber;
import kmail.Util;
import kmail.auth.Authorisation;
import kmail.ui.KMailClient;
import kmail.ui.send.SendWindow;

public class InboxWindow extends JFrame {

	private Grabber grabber;
	private SendWindow sendWindow;
	private ArrayList<Filter> filters;

	private JSplitPane splitPane;
	private JPanel contentPane;
	private Message[] messages;
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
	 * Create the frame.
	 * 
	 * @throws MessagingException
	 */
	public InboxWindow(KMailClient handler, Authorisation auth) throws MessagingException {
		this.grabber = new Grabber(auth);
		loadFiltersFromFile();

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
					String s = fldSearch.getText();
					if (s.isEmpty()) {
						refreshInbox();
					} else {
						filterInbox(s);
					}
				} catch (MessagingException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.add(fldSearch);

		btnCompose = new JButton("Compose");
		btnCompose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendWindow = new SendWindow(auth);
				sendWindow.setVisible(true);
			}
		});
		btnCompose.setFocusable(false);
		panel.add(btnCompose);

		btnRead = new JButton("Unread/Read");
		btnRead.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = messageList.getSelectedIndex();

				if (index < 0) return;

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
					e1.printStackTrace();
				}
			}
		});
		btnRefresh.setFocusable(false);
		panel.add(btnRefresh);

		btnFilterOptions = new JButton("Filter Options");
		btnFilterOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog("Enter filter", "<flag>: <keyword>, <keyword>, ...");
				if (input == null) return;
				else {
					Filter filter = Filter.parse(input);

					if (filter == null) {
						JOptionPane.showMessageDialog(null,
								"Please enter a filter in the form <flag>: <keyword>, <keyword>, ... \n(Be careful with spaces");
					} else {
						try {
							grabber.applyCustomFilter(filter);
							addFilter(filter);
							refreshInbox();
						} catch (MessagingException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		btnFilterOptions.setFocusable(false);
		panel.add(btnFilterOptions);

		btnLogout = new JButton("Logout");
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					grabber.close();
					if (sendWindow != null) sendWindow.dispose();
					dispose();
					handler.restart();
				} catch (MessagingException ex) {
					ex.printStackTrace();
				}
			}
		});
		btnLogout.setFocusable(false);
		panel.add(btnLogout);

		try {
			refreshInbox();
		} catch (MessagingException e1) {
			e1.printStackTrace();
		}
	}

	private void loadFiltersFromFile() {
		filters = new ArrayList<Filter>();

		File f = new File(KMailClient.FILTER_FILE_PATH);
		if (!f.exists()) return;
		else {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(f));

				String line;
				while ((line = reader.readLine()) != null) {
					filters.add(Filter.parse(line));
				}

				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void addFilter(Filter filter) {
		filters.add(filter);

		File f = new File(KMailClient.FILTER_FILE_PATH);

		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f, true));
			writer.println(filter.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void refreshInbox() throws MessagingException {
		grabber.applyFiltersToUnseen(filters);
		filterInbox("");
		fldSearch.setText("");
	}

	private void filterInbox(String keyword) throws MessagingException {
		refreshing = true;

		if (keyword.isEmpty()) {
			messages = grabber.getInboxMessages();
		} else {
			messages = grabber.search(keyword, true);
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

		String[] userFlags = m.getFlags().getUserFlags();
		model.set(i, Util.constructMessageStrip(from, subject, dateSent, seen, userFlags));
	}

	private void displayMessageContent(int index) throws MessagingException, IOException {
		Message message = messages[index];

		String svar = String.format("<html>" + "<h1 style='font-family: helvetica'>%s</h1>" + "<p style='font-family: arial'>From: %s</p>"
				+ "<p style='font-family: arial'>Received: %s</p>" + "<br>" + "<hr size=2>" + "<br>", Grabber.getSubject(message),
				Grabber.getFrom(message), Grabber.getFullDate(message));

		svar += "<span style='font-family: arial'>";

		if (message.getContentType().contains("TEXT/PLAIN")) {
			svar += message.getContent().toString().replaceAll("\n", "<br>");
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
