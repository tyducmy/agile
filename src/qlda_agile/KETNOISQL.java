package qlda_agile;

import java.sql.*;

public class KETNOISQL {

    private static Connection conn = null;

    public static Connection getConnection(String user, String pass, String database) {
        try {
            String url = "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=" + database + ";encrypt=false";
            // kết nối
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Ket noi thanh cong");
        } catch (SQLException ex) {
            System.out.println("Loi ket noi " + ex.getMessage());
        }
        return conn;
    }

}
