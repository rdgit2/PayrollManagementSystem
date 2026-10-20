package ui;

import dao.PayslipDAO;
import dao.UserDAO;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PayslipFrame extends JPanel {

    private static final Color PRIMARY   = new Color(0x1D4ED8);
    private static final Color BG        = new Color(0xF8FAFC);
    private static final Color BORDER    = new Color(0xE2E8F0);
    private static final Color TEXT      = new Color(0x0F172A);
    private static final Color TEXT_SOFT = new Color(0x334155);
    private static final Color MUTED     = new Color(0x64748B);
    private static final Color ERROR_RED = new Color(0xDC2626);

    private static final int CARD_WIDTH = 520;

    private final Card card = new Card();
    private final PayslipDAO dao = new PayslipDAO();
    private final JComboBox<PayslipDAO.PayslipItem> cbEmployee = new JComboBox<>();
    private final JLabel periodLabel = new JLabel();
    private final JButton deleteBtn = secondaryButton("Delete");

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String periodText;
    private PayslipDAO.PeriodItem currentPeriod = null;

    private Integer onlyEmployeeId = null;
    private boolean isAdmin = false;
    private boolean updating = false;

    public PayslipFrame() {
        this(null);
    }

    public PayslipFrame(UserDAO.LoginResult user) {
        if (user != null && "EMPLOYEE".equals(user.role())) {
            onlyEmployeeId = user.employeeId() != null ? user.employeeId() : -1;
        }
        isAdmin = user != null && "ADMIN".equals(user.role());

        setPeriod(LocalDate.now());

        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(28, 32, 24, 32));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        showEmpty();
        loadEmployees();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadEmployees();
            }
        });
    }

    private JPanel buildHeader() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel h = new JLabel("Payslip");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 24f));
        h.setForeground(TEXT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(h);
        top.add(Box.createVerticalStrut(18));

        periodLabel.setFont(periodLabel.getFont().deriveFont(Font.BOLD, 14f));
        periodLabel.setForeground(TEXT);
        periodLabel.setHorizontalAlignment(SwingConstants.CENTER);
        periodLabel.setPreferredSize(new Dimension(220, 38));

        JPanel periodRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        periodRow.setOpaque(false);
        periodRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        periodRow.setBorder(new EmptyBorder(0, -8, 0, 0));
        periodRow.add(fieldLabel("Pay period", 80));
        periodRow.add(navButton("<<", "Previous month",
                () -> changePeriod(periodStart.minusMonths(1))));
        periodRow.add(navButton("<", "Previous 15 days",
                () -> changePeriod(periodStart.minusDays(1))));
        periodRow.add(periodLabel);
        periodRow.add(navButton(">", "Next 15 days",
                () -> changePeriod(periodEnd.plusDays(1))));
        periodRow.add(navButton(">>", "Next month",
                () -> changePeriod(periodStart.plusMonths(1))));
        top.add(periodRow);

        if (onlyEmployeeId == null) {
            cbEmployee.setPreferredSize(new Dimension(300, 38));
            cbEmployee.addActionListener(e -> {
                if (!updating) showSelected();
            });

            JPanel employeeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            employeeRow.setOpaque(false);
            employeeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            employeeRow.setBorder(new EmptyBorder(0, -8, 0, 0));
            employeeRow.add(fieldLabel("Employee", 80));
            employeeRow.add(cbEmployee);
            top.add(Box.createVerticalStrut(10));
            top.add(employeeRow);
        }
        top.add(Box.createVerticalStrut(20));

        return top;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(card);
        stack.add(Box.createVerticalStrut(16));

        JButton print = secondaryButton("Print");
        JButton pdf = secondaryButton("Export PDF");
        print.addActionListener(e -> onPrint());
        pdf.addActionListener(e -> onExportPdf());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setBorder(new EmptyBorder(0, -10, 0, 0));
        buttons.setMaximumSize(new Dimension(CARD_WIDTH, 40));
        buttons.add(print);
        buttons.add(pdf);
        if (isAdmin) {
            deleteBtn.setForeground(ERROR_RED);
            deleteBtn.setEnabled(false);
            deleteBtn.addActionListener(e -> deleteSelected());
            buttons.add(deleteBtn);
        }
        stack.add(buttons);

        body.add(stack, BorderLayout.NORTH);
        return body;
    }

    private JLabel fieldLabel(String text, int width) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setForeground(TEXT_SOFT);
        l.setPreferredSize(new Dimension(width, 38));
        return l;
    }

    private JButton navButton(String text, String tip, Runnable action) {
        JButton b = secondaryButton(text);
        b.setToolTipText(tip);
        b.setPreferredSize(new Dimension(52, 38));
        b.addActionListener(e -> action.run());
        return b;
    }

    private void setPeriod(LocalDate date) {
        YearMonth ym = YearMonth.from(date);
        if (date.getDayOfMonth() <= 15) {
            periodStart = ym.atDay(1);
            periodEnd = ym.atDay(15);
        } else {
            periodStart = ym.atDay(16);
            periodEnd = ym.atEndOfMonth();
        }
        DateTimeFormatter f = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
        periodText = f.format(periodStart) + " - " + periodEnd.getDayOfMonth()
                + ", " + periodEnd.getYear();
    }

    private void changePeriod(LocalDate dateInNewPeriod) {
        setPeriod(dateInNewPeriod);
        loadEmployees();
    }

    private JPanel row(String label, String value, boolean bold, boolean muted) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel l = new JLabel(label);
        JLabel v = new JLabel(value);
        l.setFont(l.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, 13f));
        v.setFont(v.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, 13f));
        l.setForeground(muted ? MUTED : TEXT);
        v.setForeground(muted ? TEXT_SOFT : TEXT);
        r.add(l, BorderLayout.WEST);
        r.add(v, BorderLayout.EAST);
        return r;
    }

    private JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        return sep;
    }

    private String money(double v) {
        return String.format("%,.2f", v);
    }

    private void loadEmployees() {
        updating = true;
        try {
            cbEmployee.removeAllItems();
            currentPeriod = dao.findPeriod(periodStart, periodEnd);
            if (currentPeriod != null) {
                for (PayslipDAO.PayslipItem it : dao.listPayslips(currentPeriod.periodId(), onlyEmployeeId)) {
                    cbEmployee.addItem(it);
                }
            }
        } catch (SQLException ex) {
            currentPeriod = null;
            showError("Cannot load payslips: ", ex);
        } finally {
            updating = false;
        }
        periodLabel.setText(currentPeriod == null
                ? periodText : periodText + " (" + currentPeriod.status() + ")");
        showSelected();
    }

    private void showSelected() {
        updateDeleteButton();
        PayslipDAO.PayslipItem item = (PayslipDAO.PayslipItem) cbEmployee.getSelectedItem();
        if (item == null) {
            showEmpty();
            return;
        }
        try {
            PayslipDAO.PayslipDetail d = dao.getDetail(item.payslipId());
            if (d == null) {
                showEmpty();
            } else {
                load(d.title(), d.subtitle(), d.basic(), d.overtime(),
                        d.deductionNames(), d.deductionAmounts());
            }
        } catch (SQLException ex) {
            showError("Cannot load the payslip: ", ex);
        }
    }

    private void updateDeleteButton() {
        boolean open = currentPeriod != null && "OPEN".equals(currentPeriod.status());
        deleteBtn.setEnabled(isAdmin && open && cbEmployee.getSelectedItem() != null);
    }

    private void deleteSelected() {
        PayslipDAO.PayslipItem item = (PayslipDAO.PayslipItem) cbEmployee.getSelectedItem();
        if (item == null) return;

        int ok = JOptionPane.showConfirmDialog(this,
                "Delete the payslip of " + item.name() + " for " + periodText + "?\nThis cannot be undone.",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            dao.deletePayslip(item.payslipId());
        } catch (SQLException ex) {
            showError("Delete failed: ", ex);
        }
        loadEmployees();
    }

    private void showEmpty() {
        card.removeAll();
        JLabel none = new JLabel("No payslip saved for this pay period.");
        none.setFont(none.getFont().deriveFont(Font.PLAIN, 13f));
        none.setForeground(MUTED);
        none.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(none);
        card.setMaximumSize(new Dimension(CARD_WIDTH, card.getPreferredSize().height));
        card.revalidate();
        card.repaint();
    }

    private void showError(String message, SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, message + ex.getMessage());
    }

    public void load(String title, String subtitle, double basic, double overtime,
                     String[] deductionNames, double[] deductionAmounts) {
        card.removeAll();

        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 16f));
        t.setForeground(TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel s = new JLabel(subtitle);
        s.setFont(s.getFont().deriveFont(Font.PLAIN, 12f));
        s.setForeground(MUTED);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(t);
        card.add(Box.createVerticalStrut(2));
        card.add(s);
        card.add(Box.createVerticalStrut(14));

        double gross = basic + overtime;
        card.add(row("Basic pay", money(basic), false, false));
        card.add(row("Overtime pay", money(overtime), false, false));
        card.add(separator());
        card.add(row("Gross pay", money(gross), true, false));

        double totalDeductions = 0;
        for (int i = 0; i < deductionNames.length; i++) {
            totalDeductions += deductionAmounts[i];
            card.add(row(deductionNames[i], "- " + money(deductionAmounts[i]), false, true));
        }
        card.add(separator());
        card.add(row("Net pay", money(gross - totalDeductions), true, false));

        card.setMaximumSize(new Dimension(CARD_WIDTH, card.getPreferredSize().height));
        card.revalidate();
        card.repaint();
    }

    private void onPrint() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((g, pf, page) -> {
            if (page > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            card.printAll(g2);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage());
            }
        }
    }

    private void onExportPdf() {
        JOptionPane.showMessageDialog(this, "Export PDF goes here.");
    }

    private static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 13f));
        b.setForeground(TEXT_SOFT);
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(120, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
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