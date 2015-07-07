package com.thoughtworks.jira.util;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

public class DialogReader implements PasswordReader {

	@Override
	public String readPassword() {
		final JPasswordField jpf = new JPasswordField();
		JOptionPane jop = new JOptionPane(jpf, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = jop.createDialog("Password:");
		addComponentListener(jpf, dialog);
		dialog.setVisible(true);
		int result = (Integer) jop.getValue();
		dialog.dispose();
		
		char[] password = null;
		if (result == JOptionPane.OK_OPTION)
		    password = jpf.getPassword();
		
		return new String(password);
	}

	private void addComponentListener(final JPasswordField jpf, JDialog dialog) {
		dialog.addComponentListener(new ComponentAdapter() {
		    @Override
		    public void componentShown(ComponentEvent e) {
		        SwingUtilities.invokeLater(new Runnable() {
		            @Override
		            public void run() {
		                jpf.requestFocusInWindow();
		            }
		        });
		    }
		});
	}

}
