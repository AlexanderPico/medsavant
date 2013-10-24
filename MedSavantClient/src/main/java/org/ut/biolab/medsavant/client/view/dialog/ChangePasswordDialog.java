/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SpringLayout;
import org.ut.biolab.medsavant.*;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.SpringUtilities;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 *
 */
public class ChangePasswordDialog extends JDialog {

    private static final String SQLSTATE_ACCESSDENIED = "08001";
    private static final int INIT_X = 6;
    private static final int INIT_Y = 6;
    private static final int XPAD = 6;
    private static final int YPAD = 6;
    private static final int PASSWORD_FIELD_NUMCHARS = 25;
    //private static final int PASSWORD_FIELD_WIDTH = 160;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 0;
    //Allow any characters for password except whitespace.  The server side should take care of
    //enforcing password policy.  This regexp is therefore very permissive.
    private static Pattern REGEXP_PASSWORD = Pattern.compile("\\S+");
    private static final String[] LABELS = {"Enter Current Password",
        "Enter New Password",
        "Confirm New Password"};

    public ChangePasswordDialog() {
        super(MedSavantFrame.getInstance());
        final JPasswordField[] textFields = new JPasswordField[LABELS.length];

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel p = new JPanel(new SpringLayout());
        for (int i = 0; i < LABELS.length; ++i) {
            JLabel l = new JLabel(LABELS[i]); //, JLabel.TRAILING);
            p.add(l);
            textFields[i] = new JPasswordField(PASSWORD_FIELD_NUMCHARS);
            l.setLabelFor(textFields[i]);
            p.add(textFields[i]);
        }
        SpringUtilities.makeCompactGrid(p,
                LABELS.length, 2,
                INIT_X, INIT_Y,
                XPAD, YPAD);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JButton OKButton = new JButton("OK");
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                submit(textFields);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(OKButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalGlue());
        mainPanel.add(p);
        mainPanel.add(buttonPanel);
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(MedSavantFrame.getInstance());

    }

    protected void submit(JPasswordField[] textfields) {
        String newPass1 = String.copyValueOf(textfields[1].getPassword());
        String newPass2 = String.copyValueOf(textfields[2].getPassword());

        if (newPass1 != null && newPass2 != null) {
            if (!newPass1.equals(newPass2)) {
                DialogUtils.displayError("Invalid Password!", "The entered passwords do not match!");
                return;
            }
            if (newPass1.length() < PASSWORD_MIN_LENGTH) {
                String msg = "New Password is too short -- must be ";
                if (PASSWORD_MAX_LENGTH > 0) {
                    msg += " " + PASSWORD_MIN_LENGTH + " - " + PASSWORD_MAX_LENGTH + " characters.";
                } else {
                    msg += " at least " + PASSWORD_MIN_LENGTH + " characters";
                }
                DialogUtils.displayError("Invalid Password!", msg);
                return;
            }
            if (PASSWORD_MAX_LENGTH > 0 && newPass1.length() > PASSWORD_MAX_LENGTH) {
                DialogUtils.displayError("Invalid Password!", "New password is too long -- use at most " + PASSWORD_MAX_LENGTH + " characters.");
                return;
            }

            if (!REGEXP_PASSWORD.matcher(newPass1).matches()) {
                DialogUtils.displayError("Invalid Password!", "New password contains illegal characters -- password must not contain spaces");
                return;
            }

            try {
                MedSavantClient.UserManager.changePassword(LoginController.getInstance().getSessionID(),
                        LoginController.getInstance().getUserName(),
                        textfields[0].getPassword(),
                        textfields[1].getPassword());
                DialogUtils.displayMessage("Password successfully changed.");
            } catch (RemoteException re) {
                DialogUtils.displayException("Error changing password", re.getMessage(), re);
            } catch (SQLException se) {
                if (se.getSQLState().equals(SQLSTATE_ACCESSDENIED)) {
                    DialogUtils.displayError("Access Denied", "The current password is invalid, password NOT changed");
                } else {
                    DialogUtils.displayException("Error changing password", "Error changing password", se);
                }
            } catch (SessionExpiredException see) {
                MedSavantExceptionHandler.handleSessionExpiredException(see);
            }

        }
    }
}
