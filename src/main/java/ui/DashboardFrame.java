/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
/**
 *
 * @author alfre
 */
public class DashboardFrame extends JFrame
{
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
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildTopBar(username), BorderLayout.NORTH);
        add(buildNav(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        // screens (replace the placeholders with your real panels later)
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
        bar.setBorder(new EmptyBorder(10, 16, 10, 16));
        bar.setBackground(new Color(245, 245, 245));
        JLabel title = new JLabel("Payroll Management System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        JLabel user = new JLabel("Logged in as: " + username);
        bar.add(title, BorderLayout.WEST);
        bar.add(user, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildNav() {
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(10, 8, 10, 8));
        nav.setBackground(new Color(250, 250, 250));
        nav.setPreferredSize(new Dimension(170, 0));

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
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(nav.getBackground());
        return b;
    }

    private JPanel buildDashboardPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel h = new JLabel("Dashboard");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(14));

        // today's attendance
        JPanel row1 = new JPanel(new GridLayout(1, 4, 12, 0));
        row1.add(statCard("Active employees", lblActive));
        row1.add(statCard("Present today", lblPresent));
        row1.add(statCard("Late today", lblLate));
        row1.add(statCard("Absent today", lblAbsent));
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        p.add(row1);
        p.add(Box.createVerticalStrut(18));

        // current pay period
        lblPeriod.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblPeriod);
        p.add(Box.createVerticalStrut(8));

        JPanel row2 = new JPanel(new GridLayout(1, 4, 12, 0));
        row2.add(statCard("Gross pay", lblGross));
        row2.add(statCard("Deductions", lblDeductions));
        row2.add(statCard("Net pay", lblNet));
        row2.add(statCard("Payslips generated", lblPayslips));
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        p.add(row2);
        p.add(Box.createVerticalStrut(18));

        // quick actions
        JLabel qa = new JLabel("Quick actions");
        qa.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(qa);
        p.add(Box.createVerticalStrut(8));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton add = new JButton("Add employee");
        JButton mark = new JButton("Mark attendance");
        JButton gen = new JButton("Generate payslips");
        add.addActionListener(e -> showScreen("employees"));
        mark.addActionListener(e -> showScreen("attendance"));
        gen.addActionListener(e -> showScreen("payroll"));
        actions.add(add);
        actions.add(mark);
        actions.add(gen);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(actions);

        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel statCard(String title, JLabel value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 12, 10, 12)));
        card.setBackground(new Color(248, 248, 248));

        JLabel t = new JLabel(title);
        t.setForeground(Color.GRAY);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 22f));
        card.add(t);
        card.add(Box.createVerticalStrut(4));
        card.add(value);
        return card;
    }

    private JPanel placeholder(String name) {
        JPanel p = new JPanel(new GridBagLayout());
        p.add(new JLabel(name + " screen goes here"));
        return p;
    }

    // ---------- behavior ----------

    private void showScreen(String screen) {
        cardLayout.show(content, screen);
        for (Component c : nav.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                boolean on = screen.equals(b.getClientProperty("screen"));
                b.setBackground(on ? new Color(220, 235, 250) : nav.getBackground());
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