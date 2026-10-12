package ui;

import dao.PayrollDAO;
import dao.PayrollDAO.PayrollRow;
import dao.PayslipDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PayrollFrame extends JFrame {

    private final JTextField fromField = new JTextField(10);
    private final JTextField toField = new JTextField(10);
    private final JLabel totalLabel = new JLabel("Total net pay: 0.00");
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Employee no", "Name", "Daily rate", "Present", "Late", "On leave",
            "Absent", "Gross", "Deductions", "Net pay"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final PayrollDAO dao = new PayrollDAO();

    // kept so a selected table row can be matched to its PayrollRow
    private List<PayrollRow> currentRows = new ArrayList<>();
    private LocalDate lastFrom, lastTo;

    // CHANGE later: the logged-in user's ID (used for PROCESSED_BY)
    private int currentUserId = 1;

    public PayrollFrame() {
        super("Payroll");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 460);
        setLocationRelativeTo(null);

        LocalDate today = LocalDate.now();
        fromField.setText(today.withDayOfMonth(1).toString());
        toField.setText(today.toString());

        JButton computeBtn = new JButton("Compute");
        computeBtn.addActionListener(e -> compute());

        JButton payslipBtn = new JButton("View Payslip");
        payslipBtn.addActionListener(e -> showPayslip());

        JButton saveBtn = new JButton("Save Payslips");
        saveBtn.addActionListener(e -> savePayslips());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("From (yyyy-MM-dd):"));
        top.add(fromField);
        top.add(new JLabel("To:"));
        top.add(toField);
        top.add(computeBtn);
        top.add(payslipBtn);
        top.add(saveBtn);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(totalLabel);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    /** Use this once the dashboard can pass the logged-in user's ID. */
    public PayrollFrame(int userId) {
        this();
        this.currentUserId = userId;
    }

    private void compute() {
        try {
            LocalDate from = LocalDate.parse(fromField.getText().trim());
            LocalDate to = LocalDate.parse(toField.getText().trim());
            if (to.isBefore(from)) {
                JOptionPane.showMessageDialog(this, "'To' date must not be before 'From' date.");
                return;
            }
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
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Use the date format yyyy-MM-dd, e.g. 2026-10-01.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showPayslip() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an employee row first.");
            return;
        }
        if (lastFrom == null || lastTo == null || currentRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Click Compute first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        PayrollRow r = currentRows.get(modelRow);

        PayslipFrame panel = new PayslipFrame();
        panel.load(r.name() + " · " + r.employeeNo(),
                lastFrom + " – " + lastTo,
                r.gross(), 0,
                new String[]{"Late (" + r.late() + " x " + money(PayrollDAO.LATE_DEDUCTION) + ")"},
                new double[]{r.deductions()});

        JDialog d = new JDialog(this, "Payslip", Dialog.ModalityType.APPLICATION_MODAL);
        d.setContentPane(panel);
        d.setSize(520, 420);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
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
}