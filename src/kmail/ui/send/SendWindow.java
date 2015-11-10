package kmail.ui.send;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import kmail.Sender;
import kmail.auth.Credentials;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * A JFrame that shows allows the user to enter the details for an email and
 * send the email.
 * 
 * @author Kelsey McKenna
 *
 */
public class SendWindow extends JFrame {
	// The list of attachments to be sent with the email being constructed.
	private File[] attachments;

	private JPanel contentPane;
	private JLabel lblTo;
	private JLabel lblCc;
	private JLabel lblSubject;
	private JTextField fldTo;
	private JTextField fldCc;
	private JTextField fldSubject;
	private JTextArea fldBody;
	private final JButton btnSend = new JButton("Send");
	private JButton btnAttach;
	private JLabel lblAttached;

	/**
	 * Construct the window
	 * 
	 * @param auth
	 *            the credentials for the account that will send the email
	 */
	public SendWindow(Credentials auth) {
		Sender sender = new Sender(auth);

		setTitle("KMail - Create Message");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 615, 599);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(
				new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[] {
						FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
						RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		lblTo = new JLabel("To");
		contentPane.add(lblTo, "2, 2, right, default");

		fldTo = new JTextField();
		fldTo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		contentPane.add(fldTo, "4, 2, fill, default");
		fldTo.setColumns(10);

		lblCc = new JLabel("Cc");
		contentPane.add(lblCc, "2, 4, right, default");

		fldCc = new JTextField();
		fldCc.setFont(new Font("Tahoma", Font.PLAIN, 12));
		contentPane.add(fldCc, "4, 4");
		fldCc.setColumns(10);

		lblSubject = new JLabel("Subject");
		contentPane.add(lblSubject, "2, 6, right, default");

		fldSubject = new JTextField();
		fldSubject.setFont(new Font("Tahoma", Font.PLAIN, 12));
		contentPane.add(fldSubject, "4, 6, fill, default");
		fldSubject.setColumns(10);

		btnAttach = new JButton("Attach Files");
		btnAttach.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Set the file chooser to open at the user's last directory
				final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
				// Allow the user to select multiple files
				fc.setMultiSelectionEnabled(true);
				// Open the file chooser dialog
				int option = fc.showOpenDialog(null);

				// If the user actually selected some files
				if (option == JFileChooser.APPROVE_OPTION) {
					attachments = fc.getSelectedFiles();
					// Save the directory the user last opened (for convenience)
					System.setProperty("user.dir", attachments[0].getAbsolutePath());
					// Show the attachments in the attachments list
					refreshAttachmentsList();
				}
			}
		});
		btnAttach.setFocusable(false);
		contentPane.add(btnAttach, "4, 8, left, default");

		lblAttached = new JLabel();
		contentPane.add(lblAttached, "4, 10");

		fldBody = new JTextArea();
		fldBody.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		fldBody.setLineWrap(true);
		contentPane.add(fldBody, "4, 12, fill, fill");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Construct the MIMEMessage from the information entered by
					// the user
					MimeMessage message = sender.constructMimeMessage(fldTo.getText(), fldCc.getText(), fldSubject.getText(),
							fldBody.getText(), attachments);
					sender.sendEmail(message);

					// Close the window and dipose it
					setVisible(false);
					dispose();
				} catch (AddressException e1) {
					JOptionPane.showMessageDialog(null, "Make sure you enter valid email addresses.");
				} catch (MessagingException e1) {
					JOptionPane.showMessageDialog(null, "There was an error. Please check the information you have entered and try again.");
				}
			}
		});
		btnSend.setFocusable(false);
		contentPane.add(btnSend, "4, 14, center, default");
	}

	/**
	 * Refreshes the list of attachment names displayed in the window.
	 */
	private void refreshAttachmentsList() {
		String svar = "";
		for (int i = 0; i < attachments.length; ++i) {
			svar += attachments[i].getName();

			// Only add a comma if this is not the last attachment name
			if (i < attachments.length - 1) svar += ", ";
		}

		lblAttached.setText(svar);
	}
}
