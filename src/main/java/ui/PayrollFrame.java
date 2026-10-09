/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
/**
 *
 * @author alfre
 */
public class PayrollFrame extends JPanel {
    
    private final JComboBox<String> cboPeriod = new JComboBox<>(
            new String[]{"Oct 1 – Oct 31, 2026 (OPEN)", "Sep 1 – Sep 30, 2026 (CLOSED)"});
    private final JButton btnGenerate = new JButton("Generate payslips");
    private final JButton btnClose = new JButton("Close period");
 
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Employee", "Basic", "Overtime", "Gross", "Deductions", "Net pay"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
 
    public PayrollFrame() {
        setLayout(new BorderLayout());
        add(buildPanel(), BorderLayout.CENTER);
        loadPeriod();
    }
 
    // ---------- layout pieces ----------
 
    private JPanel buildPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
 
        JLabel h = new JLabel("Payroll processing");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(10));
 
        JLabel lbl = new JLabel("Pay period");
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
 
        cboPeriod.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboPeriod.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cboPeriod.addActionListener(e -> loadPeriod());
        p.add(cboPeriod);
        p.add(Box.createVerticalStrut(8));
 
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGenerate.addActionListener(e -> onGenerate());
        btnClose.addActionListener(e -> onClosePeriod());
        buttons.add(btnGenerate);
        buttons.add(btnClose);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(buttons);
        p.add(Box.createVerticalStrut(8));
 
        // table
        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        for (int col = 1; col <= 5; col++) {
            rightAlignColumn(col);
        }
 
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sp);
        p.add(Box.createVerticalStrut(8));
 
        JLabel note = new JLabel("Columns come from the PAYSLIP_SUMMARY view.");
        note.setForeground(Color.GRAY);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(note);
        return p;
    }
 
    private void rightAlignColumn(int col) {
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer();
        cell.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(col).setCellRenderer(cell);
 
        TableCellRenderer base = table.getTableHeader().getDefaultRenderer();
        table.getColumnModel().getColumn(col).setHeaderRenderer((t, v, sel, foc, row, c) -> {
            Component comp = base.getTableCellRendererComponent(t, v, sel, foc, row, c);
            if (comp instanceof JLabel) {
                ((JLabel) comp).setHorizontalAlignment(SwingConstants.RIGHT);
            }
            return comp;
        });
    }
 
    private String money(double v) {
        return String.format("%,.2f", v);
    }
 
    // ---------- behavior ----------
 
    private boolean isOpen() {
        return String.valueOf(cboPeriod.getSelectedItem()).contains("(OPEN)");
    }
 
    /** Reload the table for the selected period. Replace with a query on PAYSLIP_SUMMARY. */
    private void loadPeriod() {
        // TODO: new PayslipDAO().findSummaryByPeriod(periodId)
        model.setRowCount(0);
        addRow("Santos, Maria", 22000, 0,   22000, 2100);
        addRow("Reyes, Juan",   28000, 500, 28500, 3200);
        addRow("Cruz, Ana",     35000, 0,   35000, 5400);
 
        btnGenerate.setEnabled(isOpen());
        btnClose.setEnabled(isOpen());
    }
 
    private void addRow(String name, double basic, double ot, double gross, double ded) {
        model.addRow(new Object[]{name, money(basic), money(ot),
                money(gross), money(ded), money(gross - ded)});
    }
 
    private void onGenerate() {
        // TODO: new PayrollService().generatePayslips(periodId); then loadPeriod()
        JOptionPane.showMessageDialog(this, "Payslips generated for the selected period.");
    }
 
    private void onClosePeriod() {
        int ok = JOptionPane.showConfirmDialog(this,
                "Close this pay period? It can no longer be changed.", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            // TODO: new PayPeriodDAO().close(periodId);
            btnGenerate.setEnabled(false);
            btnClose.setEnabled(false);
        }
    }
}
