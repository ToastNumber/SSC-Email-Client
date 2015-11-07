package kmail.ui.inbox;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

public class MessageStripRenderer implements ListCellRenderer<String> {
	
	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
			boolean isSelected, boolean cellHasFocus) {
		JTextPane svar = new JTextPane();

		svar.setBorder(new EmptyBorder(7, 0, 7, 0));
		
		svar.setContentType("text/html");
		svar.setText(value);
		
		if (isSelected) {
			svar.setBackground(new Color(0x21C8FF));;
		}
		
		
		return svar;
	}
	
}
