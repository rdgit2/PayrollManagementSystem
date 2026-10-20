package ui;

import dao.DBConnection;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class JobPositionsPanel extends JPanel {

    private static final Color PRIMARY    = new Color(0x1D4ED8);
    private static final Color PRIMARY_OFF = new Color(0x93C5FD);
    private static final Color BORDER     = new Color(0xE2E8F0);
    private static final Color TEXT       = new Color(0x0F172A);
    private static final Color TEXT_SOFT  = new Color(0x334155);
    private static final Color ERROR_RED  = new Color(0xDC2626);

    private static final String SQL_LIST   = "SELECT position_id, position_title, monthly_salary FROM JOB_POSITIONS ORDER BY position_title";
    private static final String SQL_INSERT = "INSERT INTO JOB_POSITIONS (position_title, monthly_salary) VALUES (?, ?)";
    private static final String SQL_UPDATE = "UPDATE JOB_POSITIONS SET position_title = ?, monthly_salary = ? WHERE position_id = ?";
    private static final String SQL_DELETE = "DELETE FROM JOB_POSITIONS WHERE position_id = ?";
    private static final String SQL_EXISTS = "SELECT position_id FROM JOB_POSITIONS WHERE UPPER(position_title) = UPPER(?)";

    private static final int NAME_MAX = 50;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Job position", "Monthly salary"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField txtName = new JTextField();
    private final JTextField txtSalary = new JTextField();
    private final JButton btnAdd = primaryButton("Add");
    private final JButton btnUpdate = secondaryButton("Update");
    private final JButton btnDelete = secondaryButton("Delete");
    private final JButton btnClear = secondaryButton("Clear");

    private Integer selectedId = null;

    public JobPositionsPanel() {
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);

        add(buildForm(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> clearForm());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        clearForm();
        reload();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(6, 0, 4, 0));

        txtName.setPreferredSize(new Dimension(260, 38));
        txtName.putClientProperty("JTextField.placeholderText", "Job position name");
        txtSalary.setPreferredSize(new Dimension(170, 38));
        txtSalary.putClientProperty("JTextField.placeholderText", "25000.00");

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fields.setOpaque(false);
        fields.setAlignmentX(Component.LEFT_ALIGNMENT);
        fields.setBorder(new EmptyBorder(0, -10, 0, 0));
        fields.add(fieldLabel("Job position"));
        fields.add(txtName);
        fields.add(fieldLabel("Monthly salary"));
        fields.add(txtSalary);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setBorder(new EmptyBorder(0, -10, 0, 0));
        btnDelete.setForeground(ERROR_RED);
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);

        form.add(fields);
        form.add(Box.createVerticalStrut(12));
        form.add(buttons);
        return form;
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
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setCellRenderer(center);

        DefaultTableCellRenderer money = new DefaultTableCellRenderer() {
            @Override protected void setValue(Object v) {
                setText(v instanceof BigDecimal ? String.format("%,.2f", (BigDecimal) v) : "");
            }
        };
        money.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(money);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private void reload() {
        model.setRowCount(0);
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SQL_LIST);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getBigDecimal(3)
                });
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (Integer) model.getValueAt(row, 0);
        txtName.setText((String) model.getValueAt(row, 1));
        txtSalary.setText(((BigDecimal) model.getValueAt(row, 2)).toPlainString());
        setEditing(true);
    }

    private void clearForm() {
        selectedId = null;
        table.clearSelection();
        txtName.setText("");
        txtSalary.setText("");
        setEditing(false);
        txtName.requestFocusInWindow();
    }

    private void setEditing(boolean editing) {
        btnAdd.setEnabled(!editing);
        btnAdd.setBackground(editing ? PRIMARY_OFF : PRIMARY);
        btnUpdate.setEnabled(editing);
        btnDelete.setEnabled(editing);
    }

    private String readName() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            warn("Enter the job position name.");
            return null;
        }
        if (name.length() > NAME_MAX) {
            warn("Job position name must be " + NAME_MAX + " characters or less.");
            return null;
        }
        return name;
    }

    private BigDecimal readSalary() {
        String raw = txtSalary.getText().trim().replace(",", "");
        if (raw.isEmpty()) {
            warn("Enter the monthly salary.");
            return null;
        }
        try {
            BigDecimal v = new BigDecimal(raw);
            if (v.signum() <= 0) {
                warn("Monthly salary must be greater than zero.");
                return null;
            }
            if (v.scale() > 2) {
                warn("Monthly salary can have at most 2 decimal places.");
                return null;
            }
            return v;
        } catch (NumberFormatException ex) {
            warn("Monthly salary must be a number, for example 25000.00");
            return null;
        }
    }

    private boolean nameTaken(Connection c, String name, Integer exceptId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_EXISTS)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (exceptId == null || rs.getInt(1) != exceptId) return true;
                }
            }
        }
        return false;
    }

    private void onAdd() {
        String name = readName();
        if (name == null) return;
        BigDecimal salary = readSalary();
        if (salary == null) return;

        try (Connection c = DBConnection.get()) {
            if (nameTaken(c, name, null)) {
                warn("That job position already exists.");
                return;
            }
            try (PreparedStatement ps = c.prepareStatement(SQL_INSERT)) {
                ps.setString(1, name);
                ps.setBigDecimal(2, salary);
                ps.executeUpdate();
            }
            clearForm();
            reload();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void onUpdate() {
        if (selectedId == null) return;
        String name = readName();
        if (name == null) return;
        BigDecimal salary = readSalary();
        if (salary == null) return;

        try (Connection c = DBConnection.get()) {
            if (nameTaken(c, name, selectedId)) {
                warn("Another job position already uses that name.");
                return;
            }
            try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                ps.setString(1, name);
                ps.setBigDecimal(2, salary);
                ps.setInt(3, selectedId);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this,
                    "Saved. New payslips will use this salary. Payslips already generated are not changed.");
            clearForm();
            reload();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void onDelete() {
        if (selectedId == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete the job position \"" + txtName.getText().trim() + "\"?",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            clearForm();
            reload();
        } catch (SQLException ex) {
            if ("23503".equals(ex.getSQLState())) {
                warn("This job position is assigned to one or more employees, so it cannot be deleted. "
                   + "Reassign those employees first.");
            } else {
                showError(ex);
            }
        }
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Job positions", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "Job positions", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setForeground(TEXT_SOFT);
        return l;
    }

    private static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setForeground(Color.WHITE);
        b.setBackground(PRIMARY);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(110, 38));
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
        b.setPreferredSize(new Dimension(110, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
    }
}