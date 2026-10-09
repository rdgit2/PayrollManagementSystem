/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import java.awt.*;

/**
 *
 * @author alfre
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.SwingUtilities;

public class LoginFrame {
    public static void main(String[] args) {
        Frame frame = new Frame("Login");
        frame.setLayout(null);

        Label lblUser = new Label("Username");
        Label lblPass = new Label("Password");
        lblUser.setBounds(452, 275, 150, 20);
        lblPass.setBounds(452, 335, 150, 20);

        TextField tfUser = new TextField(25);
        TextField tfPass = new TextField(25);
        tfPass.setEchoChar('*');
        tfUser.setBounds(452, 300, 150, 25);
        tfPass.setBounds(452, 360, 150, 25);

        Button button = new Button("Login");
        button.setBounds(477, 410, 100, 30);

        Label lblError = new Label("");
        lblError.setForeground(Color.RED);
        lblError.setBounds(452, 450, 200, 20);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String user = tfUser.getText();
                String pass = tfPass.getText();

                // Temporary check; replace with a database lookup later
                if (user.equals("admin") && pass.equals("1234")) {
                    new DashboardFrame(user).setVisible(true);  // open dashboard
                    frame.dispose();                            // close login window
                } else {
                    lblError.setText("Invalid username or password");
                }
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.add(lblUser);
        frame.add(tfUser);
        frame.add(lblPass);
        frame.add(tfPass);
        frame.add(button);
        frame.add(lblError);

        frame.setSize(1000, 700);
        frame.setVisible(true);
    }
}