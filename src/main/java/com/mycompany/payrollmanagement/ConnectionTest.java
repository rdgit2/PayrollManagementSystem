package com.mycompany.payrollmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConnectionTest {

    private static final String URL = "jdbc:derby://localhost:1527/pmdb";
    private static final String USER = "app";
    private static final String PASSWORD = "app";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connected to: " + conn.getMetaData().getURL());
            System.out.println("Derby version: " + conn.getMetaData().getDatabaseProductVersion());
            System.out.println("Current schema: " + conn.getSchema());

            String sql = "SELECT t.TABLENAME, t.TABLETYPE "
                       + "FROM SYS.SYSTABLES t JOIN SYS.SYSSCHEMAS s ON t.SCHEMAID = s.SCHEMAID "
                       + "WHERE s.SCHEMANAME = 'APP' AND t.TABLETYPE IN ('T', 'V') "
                       + "ORDER BY t.TABLETYPE, t.TABLENAME";

            int count = 0;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String type = rs.getString("TABLETYPE").equals("T") ? "TABLE" : "VIEW ";
                    System.out.println("  " + type + "  " + rs.getString("TABLENAME"));
                    count++;
                }
            }
            System.out.println("Total objects in APP schema: " + count + " (expected 10)");
        } catch (Exception e) {
            System.out.println("Connection FAILED:");
            e.printStackTrace();
        }
    }
}