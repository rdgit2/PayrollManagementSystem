package ui;

import dao.UserDAO;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProfilePanel extends JPanel {

    private static final int CARD_WIDTH = 460;

    private final UserDAO dao = new UserDAO();
    private final String username;
    private final Runnable onLogout;

    private final JTextField txtNewUsername = new JTextField();
    private final JPasswordField txtUsernamePass = new JPasswordField();
    private final JPasswordField txtCurrent = new JPasswordField();
    private final JPasswordField txtNew = new JPasswordField();
    private final JPasswordField txtConfirm = new JPasswordField();

    public ProfilePanel(UserDAO.LoginResult user, Runnable onLogout) {
        this.username = user.username();
        this.onLogout = onLogout;

        setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel h = new JLabel("My Account");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(4));

        JLabel who = new JLabel(user.fullName() + "  |  Username: " + user.username());
        who.setForeground(Color.GRAY);
        who.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(who);
        p.add(Box.createVerticalStrut(14));

        p.add(buildUsernameCard());
        p.add(Box.createVerticalStrut(14));
        p.add(buildPasswordCard());
        p.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel newCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(14, 16, 14, 16)));
        card.setBackground(new Color(248, 248, 248));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 15f));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(t);
        card.add(Box.createVerticalStrut(10));
        return card;
    }

    private void addField(JPanel card, String label, JTextField field) {
        JLabel l = new JLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l);
        card.add(Box.createVerticalStrut(4));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(300, 32));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        card.add(field);
        card.add(Box.createVerticalStrut(10));
    }

    private void finishCard(JPanel card, JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(button);
        card.setMaximumSize(new Dimension(CARD_WIDTH, card.getPreferredSize().height));
    }

    private JPanel buildUsernameCard() {
        JPanel card = newCard("Change username");
        addField(card, "New username", txtNewUsername);
        addField(card, "Current password", txtUsernamePass);
        JButton save = new JButton("Save username");
        save.addActionListener(e -> onChangeUsername());
        finishCard(card, save);
        return card;
    }

    private JPanel buildPasswordCard() {
        JPanel card = newCard("Change password");
        addField(card, "Current password", txtCurrent);
        addField(card, "New password", txtNew);
        addField(card, "Confirm new password", txtConfirm);
        JButton save = new JButton("Save password");
        save.addActionListener(e -> onChangePassword());
        finishCard(card, save);
        return card;
    }

    private void onChangeUsername() {
        String newName = txtNewUsername.getText().trim();
        String pass = new String(txtUsernamePass.getPassword());

        if (!newName.matches("[A-Za-z0-9_.-]{3,30}")) {
            warn("Username must be 3 to 30 characters: letters, numbers, dot, dash or underscore.");
            return;
        }
        if (newName.equals(username)) {
            warn("That is already your username.");
            return;
        }
        if (pass.isEmpty()) {
            warn("Enter your current password to confirm.");
            return;
        }

        try {
            if (!dao.verifyPassword(username, pass)) {
                warn("Current password is incorrect.");
                return;
            }
            if (dao.usernameExists(newName)) {
                warn("That username is already taken.");
                return;
            }
            if (dao.changeUsername(username, newName)) {
                JOptionPane.showMessageDialog(this,
                        "Username changed. Please sign in again with your new username.");
                onLogout.run();
            } else {
                warn("Username was not changed.");
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void onChangePassword() {
        String current = new String(txtCurrent.getPassword());
        String next = new String(txtNew.getPassword());
        String confirm = new String(txtConfirm.getPassword());

        if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
            warn("Please fill in all password fields.");
            return;
        }
        if (next.length() < 6) {
            warn("New password must be at least 6 characters.");
            return;
        }
        if (!next.equals(confirm)) {
            warn("New password and confirmation do not match.");
            return;
        }
        if (next.equals(current)) {
            warn("New password must be different from the current one.");
            return;
        }

        try {
            if (!dao.verifyPassword(username, current)) {
                warn("Current password is incorrect.");
                return;
            }
            if (dao.changePassword(username, next)) {
                txtCurrent.setText("");
                txtNew.setText("");
                txtConfirm.setText("");
                JOptionPane.showMessageDialog(this, "Password changed successfully.");
            } else {
                warn("Password was not changed.");
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "My Account", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "My Account", JOptionPane.ERROR_MESSAGE);
    }
}