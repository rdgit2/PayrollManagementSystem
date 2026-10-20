package ui;

import dao.UserDAO;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProfilePanel extends JPanel {

    private static final Color PRIMARY   = new Color(0x1D4ED8);
    private static final Color BG        = new Color(0xF8FAFC);
    private static final Color BORDER    = new Color(0xE2E8F0);
    private static final Color TEXT      = new Color(0x0F172A);
    private static final Color TEXT_SOFT = new Color(0x334155);
    private static final Color MUTED     = new Color(0x64748B);

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
        setBackground(BG);
        setBorder(new EmptyBorder(28, 32, 24, 32));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel h = new JLabel("My Account");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 24f));
        h.setForeground(TEXT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel who = new JLabel(user.fullName() + "  |  Username: " + user.username());
        who.setForeground(MUTED);
        who.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(h);
        top.add(Box.createVerticalStrut(4));
        top.add(who);
        top.add(Box.createVerticalStrut(22));

        JPanel cards = new JPanel(new GridLayout(1, 2, 20, 0));
        cards.setOpaque(false);
        cards.add(alignTop(buildUsernameCard()));
        cards.add(alignTop(buildPasswordCard()));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(cards, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel alignTop(JPanel card) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.NORTH);
        return wrap;
    }

    private JPanel newCard(String title) {
        Card card = new Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 22, 22, 22));

        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 16f));
        t.setForeground(TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(t);
        card.add(Box.createVerticalStrut(16));
        return card;
    }

    private void addField(JPanel card, String label, JTextField field, String placeholder) {
        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setForeground(TEXT_SOFT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l);
        card.add(Box.createVerticalStrut(6));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(300, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        card.add(field);
        card.add(Box.createVerticalStrut(14));
    }

    private void addButton(JPanel card, String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setForeground(Color.WHITE);
        b.setBackground(PRIMARY);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(160, 38));
        b.setMaximumSize(new Dimension(160, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> action.run());
        card.add(b);
    }

    private JPanel buildUsernameCard() {
        JPanel card = newCard("Change username");
        addField(card, "New username", txtNewUsername, "Enter your new username");
        addField(card, "Current password", txtUsernamePass, "Enter your current password");
        addButton(card, "Save username", this::onChangeUsername);
        return card;
    }

    private JPanel buildPasswordCard() {
        JPanel card = newCard("Change password");
        addField(card, "Current password", txtCurrent, "Enter your current password");
        addField(card, "New password", txtNew, "At least 6 characters");
        addField(card, "Confirm new password", txtConfirm, "Type the new password again");
        addButton(card, "Save password", this::onChangePassword);
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

    private static class Card extends JPanel {
        Card() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}