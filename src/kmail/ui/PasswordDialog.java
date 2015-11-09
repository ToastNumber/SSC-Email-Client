package kmail.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.mail.MessagingException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class PasswordDialog extends JDialog implements KeyListener {
    private final JLabel jlblEmail = new JLabel("Email");
    private final JLabel jlblPassword = new JLabel("Password");

    private final JTextField jtfEmail = new JTextField(15);
    private final JPasswordField jpfPassword = new JPasswordField();

    private final JButton jbtOk = new JButton("Login");
    private final JButton jbtCancel = new JButton("Cancel");

    private final JLabel jlblStatus = new JLabel(" ");

    public PasswordDialog(final JFrame parent, boolean modal, KMailClient handler) {
        super(parent, modal);
        
        setTitle("KMail Login");
        
        jlblEmail.setFont(new Font("arial", 0, 14));
        jlblPassword.setFont(new Font("arial", 0, 14));
        
        jtfEmail.setFont(new Font("arial", 0, 14));
        jtfEmail.addKeyListener(this);
        jpfPassword.setFont(new Font("arial", 0, 14));
        jpfPassword.addKeyListener(this);
        
        JPanel p3 = new JPanel(new GridLayout(2, 1));
        p3.add(jlblEmail);
        p3.add(jlblPassword);

        JPanel p4 = new JPanel(new GridLayout(2, 1));
        p4.add(jtfEmail);
        p4.add(jpfPassword);

        JPanel p1 = new JPanel();
        p1.add(p3);
        p1.add(p4);

        JPanel p2 = new JPanel();
        p2.add(jbtOk);
        p2.add(jbtCancel);

        JPanel p5 = new JPanel(new BorderLayout());
        p5.add(p2, BorderLayout.CENTER);
        p5.add(jlblStatus, BorderLayout.NORTH);
        jlblStatus.setForeground(Color.RED);
        jlblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        setLayout(new BorderLayout());
        add(p1, BorderLayout.CENTER);
        add(p5, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {  
            @Override
            public void windowClosing(WindowEvent e) {  
                System.exit(0);  
            }  
        });


        jbtOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	String username = jtfEmail.getText();
            	String password = new String(jpfPassword.getPassword());
            	
            	try {
        			handler.login(username, password);
        		} catch (MessagingException e1) {
        			if (e1.getLocalizedMessage().startsWith("[ALERT]")) {
        				JOptionPane.showMessageDialog(null, e1.getLocalizedMessage());
        			} else {
        				jlblStatus.setText("Invalid email or password");
        			}
        		}
            }
        });
        jbtCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                System.exit(0);
            }
        });
    }
    
	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		jlblStatus.setText(" ");
		
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			jbtOk.doClick();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
}