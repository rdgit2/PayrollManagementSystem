package ui;

import dao.UserDAO;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {

    private static final Color PRIMARY   = new Color(0x1D4ED8);
    private static final Color BG        = new Color(0xF8FAFC);
    private static final Color BORDER    = new Color(0xE2E8F0);
    private static final Color TEXT      = new Color(0x0F172A);
    private static final Color TEXT_SOFT = new Color(0x334155);
    private static final Color MUTED     = new Color(0x64748B);
    private static final Color ERROR     = new Color(0xDC2626);

    private final JTextField txtUser = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();
    private final JLabel lblError = new JLabel(" ");
    private final JButton btnLogin = new JButton("Sign in");

    public LoginFrame() {
        super("AZM Payroll System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        getRootPane().setDefaultButton(btnLogin);

        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) {
                txtUser.requestFocusInWindow();
            }
        });
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 14, 12, 24)));

        JLabel title = new JLabel("AZM Payroll System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(TEXT);

        bar.add(title);
        return bar;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(BG);

        Card card = new Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 36, 32, 36));
        card.setPreferredSize(new Dimension(400, 430));

        JLabel title = new JLabel("Sign in");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Enter your credentials to continue");
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(28));

        card.add(fieldLabel("Username"));
        card.add(Box.createVerticalStrut(6));
        styleField(txtUser, "Enter your username");
        card.add(txtUser);
        card.add(Box.createVerticalStrut(16));

        card.add(fieldLabel("Password"));
        card.add(Box.createVerticalStrut(6));
        styleField(txtPass, "Enter your password");
        txtPass.putClientProperty("JPasswordField.showRevealButton", true);
        card.add(txtPass);
        card.add(Box.createVerticalStrut(10));

        lblError.setForeground(ERROR);
        lblError.setFont(lblError.getFont().deriveFont(Font.PLAIN, 12f));
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblError);
        card.add(Box.createVerticalStrut(14));

        btnLogin.setFont(btnLogin.getFont().deriveFont(Font.BOLD, 14f));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(PRIMARY);
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.putClientProperty("JButton.buttonType", "roundRect");
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setPreferredSize(new Dimension(328, 42));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.addActionListener(e -> doLogin());
        card.add(btnLogin);

        card.add(Box.createVerticalGlue());

        body.add(card);
        return body;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setForeground(TEXT_SOFT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleField(JTextField f, String placeholder) {
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setPreferredSize(new Dimension(328, 40));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.putClientProperty("JTextField.placeholderText", placeholder);
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

    private void doLogin() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter your username and password.");
            return;
        }

        try {
            UserDAO.LoginResult user = new UserDAO().login(username, password);
            if (user == null) {
                lblError.setText("Invalid username or password.");
                txtPass.setText("");
                txtPass.requestFocusInWindow();
                return;
            }

            dispose();
            new DashboardFrame(user).setVisible(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            lblError.setText("Cannot reach the database. Is Derby running?");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}