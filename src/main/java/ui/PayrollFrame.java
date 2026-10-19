package ui;

import dao.PayrollDAO;
import dao.PayrollDAO.PayrollRow;
import dao.PayslipDAO;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class PayrollFrame extends JPanel {

    private static final Color PRIMARY   = new Color(0x1D4ED8);
    private static final Color BG        = new Color(0xF8FAFC);
    private static final Color BORDER    = new Color(0xE2E8F0);
    private static final Color TEXT      = new Color(0x0F172A);
    private static final Color TEXT_SOFT = new Color(0x334155);

    private final JLabel periodLabel = new JLabel();
    private LocalDate periodStart;
    private LocalDate periodEnd;

    private final JLabel totalLabel = new JLabel("Total net pay: 0.00");
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Employee no", "Name", "Daily rate", "Present", "Late", "On leave",
            "Absent", "Gross", "Deductions", "Net pay"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final PayrollDAO dao = new PayrollDAO();

    private List<PayrollRow> currentRows = new ArrayList<>();
    private LocalDate lastFrom, lastTo;

    private int currentUserId = 1;

    public PayrollFrame() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(28, 32, 24, 32));

        setPeriod(LocalDate.now());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    public PayrollFrame(int userId) {
        this();
        this.currentUserId = userId;
    }

    private JPanel buildHeader() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel h = new JLabel("Payroll");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 24f));
        h.setForeground(TEXT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(h);
        top.add(Box.createVerticalStrut(4));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(20, 0, 14, 0));
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);

        periodLabel.setFont(periodLabel.getFont().deriveFont(Font.BOLD, 14f));
        periodLabel.setForeground(TEXT);
        periodLabel.setHorizontalAlignment(SwingConstants.CENTER);
        periodLabel.setPreferredSize(new Dimension(160, 38));

        JButton computeBtn = secondaryButton("Compute");
        computeBtn.addActionListener(e -> compute());

        JButton saveBtn = primaryButton("Save Payslips");
        saveBtn.addActionListener(e -> savePayslips());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(navButton("<<", "Previous month",
                () -> changePeriod(periodStart.minusMonths(1))));
        left.add(navButton("<", "Previous 15 days",
                () -> changePeriod(periodStart.minusDays(1))));
        left.add(periodLabel);
        left.add(navButton(">", "Next 15 days",
                () -> changePeriod(periodEnd.plusDays(1))));
        left.add(navButton(">>", "Next month",
                () -> changePeriod(periodStart.plusMonths(1))));
        left.add(Box.createHorizontalStrut(10));
        left.add(computeBtn);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(saveBtn, BorderLayout.EAST);
        top.add(toolbar);

        return top;
    }

    private JScrollPane buildTable() {
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
        if (headerRenderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer) headerRenderer).setHorizontalAlignment(SwingConstants.CENTER);
        }
        table.setForeground(TEXT);

        int[] widths = {100, 170, 90, 70, 60, 80, 70, 100, 100, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 0, 0, 0));

        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 14f));
        totalLabel.setForeground(TEXT);

        footer.add(totalLabel, BorderLayout.EAST);
        return footer;
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
        periodLabel.setText(f.format(periodStart) + " - " + periodEnd.getDayOfMonth()
                + ", " + periodEnd.getYear());
    }

    private void changePeriod(LocalDate dateInNewPeriod) {
        setPeriod(dateInNewPeriod);
        clearResults();
        compute();
    }

    private void clearResults() {
        model.setRowCount(0);
        currentRows = new ArrayList<>();
        lastFrom = null;
        lastTo = null;
        totalLabel.setText("Total net pay: 0.00");
    }

    private JButton navButton(String text, String tip, Runnable action) {
        JButton b = secondaryButton(text);
        b.setToolTipText(tip);
        b.setPreferredSize(new Dimension(52, 38));
        b.addActionListener(e -> action.run());
        return b;
    }

    private void compute() {
        try {
            LocalDate from = periodStart;
            LocalDate to = periodEnd;
            List<PayrollRow> rows = dao.compute(from, to);
            currentRows = rows;
            lastFrom = from;
            lastTo = to;

            model.setRowCount(0);
            double total = 0;
            for (PayrollRow r : rows) {
                model.addRow(new Object[]{
                        r.employeeNo(), r.name(), money(r.dailyRate()),
                        r.present(), r.late(), r.onLeave(), r.absent(),
                        money(r.gross()), money(r.deductions()), money(r.net())});
                total += r.net();
            }
            totalLabel.setText("Total net pay: " + money(total));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void savePayslips() {
        if (lastFrom == null || lastTo == null || currentRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Click Compute first.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Save payslips for " + lastFrom + " to " + lastTo + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            PayslipDAO.SaveResult res =
                    new PayslipDAO().savePayslips(currentRows, lastFrom, lastTo, currentUserId);
            JOptionPane.showMessageDialog(this,
                    "Saved: " + res.saved
                    + "\nAlready saved earlier: " + res.alreadySaved
                    + "\nSkipped (no pay): " + res.noPay);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }

    private static String money(double v) {
        return String.format("%,.2f", v);
    }

    private static JButton primaryButton(String text) {
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
}