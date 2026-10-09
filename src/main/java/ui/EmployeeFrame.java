/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author alfre
 */
public class EmployeeFrame extends JPanel{
  private static final String ALL = "All departments";
 
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cboDept = new JComboBox<>(
            new String[]{ALL, "Human Resources", "Accounting", "IT"});
 
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Emp no.", "Name", "Department", "Position", "Monthly salary", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
 
    public EmployeeFrame() {
        setLayout(new BorderLayout());
        add(buildPanel(), BorderLayout.CENTER);
        loadEmployees();
    }
 
    // ---------- layout pieces ----------
 
    private JPanel buildPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
 
        JLabel h = new JLabel("Employees");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(10));
 
        // search + department filter
        txtSearch.setToolTipText("Search by name or employee number");
        txtSearch.putClientProperty("JTextField.placeholderText", "Search name or employee no."); // FlatLaf only
        txtSearch.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        p.add(txtSearch);
        p.add(Box.createVerticalStrut(8));
 
        cboDept.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboDept.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cboDept.addActionListener(e -> applyFilter());
        p.add(cboDept);
        p.add(Box.createVerticalStrut(8));
 
        // buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton add = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton deactivate = new JButton("Deactivate");
        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());
        deactivate.addActionListener(e -> onDeactivate());
        buttons.add(add);
        buttons.add(edit);
        buttons.add(deactivate);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(buttons);
        p.add(Box.createVerticalStrut(8));
 
        // table
        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        rightAlignColumn(4);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
 
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sp);
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
 
    /** Colours the Status column: green = ACTIVE, red = INACTIVE. */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean foc, int row, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, c);
            setFont(getFont().deriveFont(Font.BOLD, 11f));
            if (!sel) {
                setForeground("ACTIVE".equals(v) ? new Color(30, 140, 60) : new Color(200, 50, 50));
            }
            return this;
        }
    }
 
    private String money(double v) {
        return String.format("%,.2f", v);
    }
 
    // ---------- behavior ----------
 
    /** Replace the sample rows with DAO calls. */
    private void loadEmployees() {
        // TODO: for (Employee e : new EmployeeDAO().findAll()) { ... }
        model.setRowCount(0);
        model.addRow(new Object[]{"EMP-001", "Santos, Maria", "Human Resources", "HR Staff",   money(22000), "ACTIVE"});
        model.addRow(new Object[]{"EMP-002", "Reyes, Juan",   "Accounting",      "Accountant", money(28000), "ACTIVE"});
        model.addRow(new Object[]{"EMP-003", "Cruz, Ana",     "IT",              "Programmer", money(35000), "ACTIVE"});
        model.addRow(new Object[]{"EMP-004", "Lim, Paolo",    "IT",              "Programmer", money(35000), "INACTIVE"});
    }
 
    private void applyFilter() {
        final String q = txtSearch.getText().trim().toLowerCase();
        final String dept = String.valueOf(cboDept.getSelectedItem());
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                boolean okDept = dept.equals(ALL) || dept.equals(e.getStringValue(2));
                boolean okText = q.isEmpty()
                        || e.getStringValue(0).toLowerCase().contains(q)
                        || e.getStringValue(1).toLowerCase().contains(q);
                return okDept && okText;
            }
        });
    }
 
    private int selectedModelRow() {
        int viewRow = table.getSelectedRow();
        return viewRow < 0 ? -1 : table.convertRowIndexToModel(viewRow);
    }
 
    private void onAdd() {
        // TODO: open an EmployeeDialog, save through the DAO, then loadEmployees()
        JOptionPane.showMessageDialog(this, "Add employee dialog goes here.");
    }
 
    private void onEdit() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }
        // TODO: open an EmployeeDialog pre-filled with this employee
        JOptionPane.showMessageDialog(this, "Edit " + model.getValueAt(row, 1) + " dialog goes here.");
    }
 
    private void onDeactivate() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Deactivate " + model.getValueAt(row, 1) + "?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            // TODO: new EmployeeDAO().setActive(empNo, false);
            model.setValueAt("INACTIVE", row, 5);
        }
    }
}