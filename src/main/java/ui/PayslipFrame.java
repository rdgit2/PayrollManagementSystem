/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import java.awt.*;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
/**
 *
 * @author alfre
 */
public class PayslipFrame extends JPanel{
     private static final int CARD_WIDTH = 460;
 
    private final JPanel card = new JPanel();
 
    public PayslipFrame() {
        setLayout(new BorderLayout());
        add(buildPanel(), BorderLayout.CENTER);
 
        // sample data - call load(...) with real data from PAYSLIPS when a payslip is picked
        load("Reyes, Juan · EMP-002",
             "Accounting · Accountant · Oct 1 – Oct 31, 2026",
             28000, 500,
             new String[]{"SSS", "PhilHealth", "Pag-IBIG", "Withholding tax"},
             new double[]{1350, 700, 200, 950});
    }
 
    // ---------- layout pieces ----------
 
    private JPanel buildPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
 
        JLabel h = new JLabel("Payslip");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(10));
 
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
 
    // ---------- behavior ----------
 
    /** Fill the card. Gross and net are computed here from the figures you pass in. */
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
            card.add(row(deductionNames[i], "– " + money(deductionAmounts[i]), false, true));
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
        // TODO: needs a PDF library (e.g. OpenPDF or Apache PDFBox) - build the PDF from the same figures
        JOptionPane.showMessageDialog(this, "Export PDF goes here.");
    }
}
