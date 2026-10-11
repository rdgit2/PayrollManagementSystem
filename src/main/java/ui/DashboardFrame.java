package ui;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DashboardFrame extends JFrame {

    // ---------- palette (one blue accent, neutral greys) ----------
    private static final Color PRIMARY   = new Color(0x1D4ED8);
    private static final Color BG        = new Color(0xF8FAFC);
    private static final Color BORDER    = new Color(0xE2E8F0);
    private static final Color TEXT      = new Color(0x0F172A);
    private static final Color TEXT_SOFT = new Color(0x334155);
    private static final Color MUTED     = new Color(0x64748B);
    private static final Color ACTIVE_BG = new Color(0xEFF6FF);
    private static final Color HOVER_BG  = new Color(0xF1F5F9);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final JPanel nav = new JPanel();

    // stat labels (filled by refreshStats)
    private final JLabel lblActive = new JLabel("0");
    private final JLabel lblPresent = new JLabel("0");
    private final JLabel lblLate = new JLabel("0");
    private final JLabel lblAbsent = new JLabel("0");
    private final JLabel lblPeriod = new JLabel("No open pay period");
    private final JLabel lblGross = new JLabel("0.00");
    private final JLabel lblDeductions = new JLabel("0.00");
    private final JLabel lblNet = new JLabel("0.00");
    private final JLabel lblPayslips = new JLabel("0 / 0");

    private JButton selectedNav;

    public DashboardFrame(String username) {
        super("Payroll Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        content.setBackground(BG);

        add(buildTopBar(username), BorderLayout.NORTH);
        add(buildNav(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        content.add(buildDashboardPanel(), "dashboard");
        content.add(new EmployeeFrame(), "employees");
        content.add(new AttendanceFrame(), "attendance");
        content.add(new PayrollFrame(), "payroll");
        content.add(new PayslipFrame(), "payslip");
        content.add(new Setup(), "setup");

        refreshStats();
        showScreen("dashboard");
    }

    // ---------- layout pieces ----------

    private JPanel buildTopBar(String username) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 24, 12, 24)));

        // left: logo mark + system name
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel logo = new JLabel("P", SwingConstants.CENTER);
        logo.setOpaque(true);
        logo.setBackground(PRIMARY);
        logo.setForeground(Color.WHITE);
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 15f));
        logo.setPreferredSize(new Dimension(30, 30));
        JLabel title = new JLabel("Payroll Management System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(TEXT);
        left.add(logo);
        left.add(title);

        // right: signed-in user
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        JLabel signed = new JLabel("Signed in as");
        signed.setForeground(MUTED);
        JLabel user = new JLabel(username);
        user.setFont(user.getFont().deriveFont(Font.BOLD, 13f));
        user.setForeground(TEXT);
        right.add(signed);
        right.add(user);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildNav() {
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER),
                new EmptyBorder(20, 12, 16, 12)));
        nav.setPreferredSize(new Dimension(220, 0));

        JLabel menu = new JLabel("MENU");
        menu.setFont(menu.getFont().deriveFont(Font.BOLD, 11f));
        menu.setForeground(MUTED);
        menu.setBorder(new EmptyBorder(0, 17, 8, 0));
        menu.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(menu);

        addNavButton("Dashboard", "dashboard");
        addNavButton("Employees", "employees");
        addNavButton("Attendance", "attendance");
        addNavButton("Payroll", "payroll");
        addNavButton("Payslip", "payslip");
        addNavButton("Setup", "setup");

        nav.add(Box.createVerticalGlue());

        JButton logout = navButton("Log out");
        logout.addActionListener(e -> {
            dispose();
            // TODO: new LoginFrame().setVisible(true);
        });
        nav.add(logout);
        return nav;
    }

    private void addNavButton(String text, String screen) {
        JButton b = navButton(text);
        b.addActionListener(e -> showScreen(screen));
        b.putClientProperty("screen", screen);
        nav.add(b);
        nav.add(Box.createVerticalStrut(2));
    }

    private JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setPreferredSize(new Dimension(196, 40));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        styleNav(b, false);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (b != selectedNav) b.setBackground(HOVER_BG);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (b != selectedNav) b.setBackground(Color.WHITE);
            }
        });
        return b;
    }

    /** Active item: light blue fill, blue bar on the left, blue bold text. */
    private void styleNav(JButton b, boolean on) {
        b.setBackground(on ? ACTIVE_BG : Color.WHITE);
        b.setForeground(on ? PRIMARY : TEXT_SOFT);
        b.setFont(b.getFont().deriveFont(on ? Font.BOLD : Font.PLAIN, 14f));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, on ? PRIMARY : Color.WHITE),
                new EmptyBorder(0, 14, 0, 8)));
    }

    private JPanel buildDashboardPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(28, 32, 28, 32));
        p.setBackground(BG);

        // page heading
        JLabel h = new JLabel("Dashboard");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 24f));
        h.setForeground(TEXT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);

        JLabel date = new JLabel(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        date.setForeground(MUTED);
        date.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(Box.createVerticalStrut(4));
        p.add(date);
        p.add(Box.createVerticalStrut(28));

        // attendance
        p.add(sectionHeader("Attendance today", null));
        p.add(Box.createVerticalStrut(12));
        JPanel row1 = new JPanel(new GridLayout(1, 4, 16, 0));
        row1.setOpaque(false);
        row1.add(statCard("Active employees", lblActive));
        row1.add(statCard("Present", lblPresent));
        row1.add(statCard("Late", lblLate));
        row1.add(statCard("Absent", lblAbsent));
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        p.add(row1);
        p.add(Box.createVerticalStrut(32));

        // payroll summary
        lblPeriod.setForeground(MUTED);
        p.add(sectionHeader("Payroll summary", lblPeriod));
        p.add(Box.createVerticalStrut(12));
        JPanel row2 = new JPanel(new GridLayout(1, 4, 16, 0));
        row2.setOpaque(false);
        row2.add(statCard("Gross pay", lblGross));
        row2.add(statCard("Deductions", lblDeductions));
        row2.add(statCard("Net pay", lblNet));
        row2.add(statCard("Payslips generated", lblPayslips));
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        p.add(row2);
        p.add(Box.createVerticalStrut(32));

        // quick actions
        p.add(sectionHeader("Quick actions", null));
        p.add(Box.createVerticalStrut(12));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton add = primaryButton("Add employee");
        JButton mark = secondaryButton("Mark attendance");
        JButton gen = secondaryButton("Generate payslips");
        add.addActionListener(e -> showScreen("employees"));
        mark.addActionListener(e -> showScreen("attendance"));
        gen.addActionListener(e -> showScreen("payroll"));
        actions.add(add);
        actions.add(mark);
        actions.add(gen);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        // FlowLayout adds 10px left gap; pull it back so buttons align with the cards
        actions.setBorder(new EmptyBorder(0, -10, 0, 0));
        p.add(actions);

        p.add(Box.createVerticalGlue());
        return p;
    }

    /** Section title on the left, optional muted note on the right. */
    private JPanel sectionHeader(String title, JLabel note) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel l = new JLabel(title);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        l.setForeground(TEXT);
        row.add(l, BorderLayout.WEST);
        if (note != null) row.add(note, BorderLayout.EAST);
        return row;
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setForeground(Color.WHITE);
        b.setBackground(PRIMARY);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(150, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
    }

    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 13f));
        b.setForeground(TEXT_SOFT);
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(160, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
    }

    private JPanel statCard(String title, JLabel value) {
        Card card = new Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.PLAIN, 12f));
        t.setForeground(MUTED);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 24f));
        value.setForeground(TEXT);

        card.add(t);
        card.add(Box.createVerticalStrut(6));
        card.add(value);
        return card;
    }

    /** Flat white card with a hairline border. */
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

    // ---------- behavior ----------

    private void showScreen(String screen) {
        cardLayout.show(content, screen);
        for (Component c : nav.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                boolean on = screen.equals(b.getClientProperty("screen"));
                if (on) selectedNav = b;
                styleNav(b, on);
            }
        }
        if (screen.equals("dashboard")) {
            refreshStats();
        }
    }

    /** Reload the numbers. Replace the sample values with DAO calls. */
    public void refreshStats() {
        // TODO: DashboardStats s = new DashboardDAO().load();
    }
}