/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LoginForm.java
 *
 * Created on Jun 20, 2011, 11:11:22 AM
 */
package org.ut.biolab.medsavant.view.login;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import org.ut.biolab.medsavant.controller.LoginController;

/**
 *
 * @author mfiume
 */
public class LoginForm extends javax.swing.JPanel {

    /** Creates new form LoginForm */
    public LoginForm() {
        initComponents();
        //this.setOpaque(false);
        this.setMaximumSize(new Dimension(400, 400));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        field_username = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        field_password = new javax.swing.JPasswordField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        label_status = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Username:");

        field_username.setText("root");
        field_username.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                field_usernameKeyPressed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Password:");

        field_password.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                field_passwordKeyPressed(evt);
            }
        });

        jCheckBox1.setText("Remember my password");
        jCheckBox1.setOpaque(false);

        jCheckBox2.setText("Sign me in automatically");
        jCheckBox2.setOpaque(false);

        label_status.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_status.setText("Sign in to MedSavant");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox2)
                    .addComponent(jCheckBox1)
                    .addComponent(field_password, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                    .addComponent(field_username, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                    .addComponent(label_status, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label_status)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(field_username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(field_password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void field_usernameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_field_usernameKeyPressed
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            loginUsingEnteredUsernameAndPassword();
        }
    }//GEN-LAST:event_field_usernameKeyPressed

    private void field_passwordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_field_passwordKeyPressed
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            loginUsingEnteredUsernameAndPassword();
        }
    }//GEN-LAST:event_field_passwordKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField field_password;
    private javax.swing.JTextField field_username;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel label_status;
    // End of variables declaration//GEN-END:variables

    private void loginUsingEnteredUsernameAndPassword() {
        this.label_status.setText("Signing in...");
        this.label_status.setFont(new Font("Tahoma",Font.BOLD,13));
        this.label_status.setForeground(Color.black);
        LoginController.login(field_username.getText(), field_password.getText());
        if (!LoginController.isLoggedIn()) {
            this.label_status.setText("Sign in information incorrect");
            this.label_status.setFont(new Font("Tahoma",Font.BOLD,13));
            this.label_status.setForeground(Color.red);
            this.field_username.requestFocus();
        }
    }
}
