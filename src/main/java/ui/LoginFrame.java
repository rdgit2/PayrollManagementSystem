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
public class LoginFrame {
    public static void main(String[] args) { 
Frame frame = new Frame("Login"); 
CardLayout cardLayout = new CardLayout(); 
Panel panel = new Panel(cardLayout); 

Button button = new Button("Login");

// Setting the position for the button in frame
button.setBounds(475, 400, 100, 30);

TextField tf1 = new TextField(25); 
TextField tf2 = new TextField(25);

frame.setLayout(null);
tf1.setBounds(452, 350, 150, 25);
tf2.setBounds(452, 300, 150, 25);

frame.add(tf1);
frame.add(tf2);
frame.add(button);
frame.add(panel); 
frame.setSize(1000, 700); 
frame.setVisible(true); 
}
}
