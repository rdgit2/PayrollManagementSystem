package ui;

import dao.PayslipDAO;
import dao.UserDAO;
import java.awt.*;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class PayslipFrame extends JPanel {

    private static final int CARD_WIDTH = 460;

    private final JPanel card = new JPanel();
    private final PayslipDAO dao = new PayslipDAO();
    private final JComboBox<PayslipDAO.PeriodItem> cbPeriod = new JComboBox<>();
    private final JComboBox<PayslipDAO.PayslipItem> cbEmployee = new JComboBox<>();

    private Integer onlyEmployeeId = null; 
    private boolean updating = false;        

    public PayslipFrame() {
        this(null);
    }

    public PayslipFrame(UserDAO.LoginResult user) {
        if (user != null && "EMPLOYEE".equals(user.role())) {
            onlyEmployeeId = user.employeeId() != null ? user.employeeId() : -1;
        }
        setLayout(new BorderLayout());
        add(buildPanel(), BorderLayout.CENTER);
        showEmpty();
        loadPeriods();
    }

 

    private JPanel buildPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel h = new JLabel("Payslip");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(10));

   
        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selectors.add(new JLabel("Pay period"));
        selectors.add(cbPeriod);
        if (onlyEmployeeId == null) {
            selectors.add(new JLabel("Employee"));
            selectors.add(cbEmployee);
        }
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadPeriods());
        selectors.add(refresh);
        selectors.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectors.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(selectors);
        p.add(Box.createVerticalStrut(10));

        cbPeriod.addActionListener(e -> {
            if (!updating) loadEmployees();
        });
        cbEmployee.addActionListener(e -> {
            if (!updating) showSelected();
        });

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(14, 16, 14, 16)));
        card.setBackground(new Color(248, 248, 248));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(card);
        p.add(Box.createVerticalStrut(10));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton print = new JButton("Print");
        JButton pdf = new JButton("Export PDF");
        print.addActionListener(e -> onPrint());
        pdf.addActionListener(e -> onExportPdf());
        buttons.add(print);
        buttons.add(pdf);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(CARD_WIDTH, 40));
        p.add(buttons);

        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel row(String label, String value, boolean bold, boolean muted) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel l = new JLabel(label);
        JLabel v = new JLabel(value);
        if (bold) {
            l.setFont(l.getFont().deriveFont(Font.BOLD));
            v.setFont(v.getFont().deriveFont(Font.BOLD));
        }
        if (muted) {
            l.setForeground(Color.GRAY);
        }
        r.add(l, BorderLayout.WEST);
        r.add(v, BorderLayout.EAST);
        return r;
    }

    private JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        return sep;
    }

    private String money(double v) {
        return String.format("%,.2f", v);
    }


    private void loadPeriods() {
        updating = true;
        try {
            cbPeriod.removeAllItems();
            for (PayslipDAO.PeriodItem p : dao.listPeriods()) {
                cbPeriod.addItem(p);
            }
        } catch (SQLException ex) {
            showError("Cannot load pay periods: ", ex);
        } finally {
            updating = false;
        }
        loadEmployees();
    }

    private void loadEmployees() {
        updating = true;
        try {
            cbEmployee.removeAllItems();
            PayslipDAO.PeriodItem period = (PayslipDAO.PeriodItem) cbPeriod.getSelectedItem();
            if (period != null) {
                for (PayslipDAO.PayslipItem it : dao.listPayslips(period.periodId(), onlyEmployeeId)) {
                    cbEmployee.addItem(it);
                }
            }
        } catch (SQLException ex) {
            showError("Cannot load payslips: ", ex);
        } finally {
            updating = false;
        }
        showSelected();
    }

    private void showSelected() {
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

    private void showEmpty() {
        card.removeAll();
        JLabel none = new JLabel("No payslip to show. Generate and save payslips in Payroll first.");
        none.setForeground(Color.GRAY);
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
        t.setFont(t.getFont().deriveFont(Font.BOLD, 15f));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel s = new JLabel(subtitle);
        s.setForeground(Color.GRAY);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(t);
        card.add(s);
        card.add(Box.createVerticalStrut(10));

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

    // ---------- print / export ----------

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
}