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

public class JobPositionsPanel extends JPanel {

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
    private final JButton btnAdd = new JButton("Add");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnClear = new JButton("Clear");

    private Integer selectedId = null;

    public JobPositionsPanel() {
        setLayout(new BorderLayout(0, 12));
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
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(248, 248, 248));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 14, 12, 14)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        card.add(new JLabel("Job position"), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        txtName.setPreferredSize(new Dimension(260, 30));
        card.add(txtName, gc);

        gc.gridx = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        card.add(new JLabel("Monthly salary"), gc);
        gc.gridx = 3; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        txtSalary.setPreferredSize(new Dimension(160, 30));
        card.add(txtSalary, gc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);

        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 4; gc.weightx = 1;
        gc.fill = GridBagConstraints.NONE;
        card.add(buttons, gc);
        return card;
    }

    private JScrollPane buildTable() {
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setMaxWidth(70);

        DefaultTableCellRenderer money = new DefaultTableCellRenderer() {
            @Override protected void setValue(Object v) {
                setText(v instanceof BigDecimal ? String.format("%,.2f", (BigDecimal) v) : "");
                setHorizontalAlignment(RIGHT);
            }
        };
        table.getColumnModel().getColumn(2).setCellRenderer(money);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
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
}