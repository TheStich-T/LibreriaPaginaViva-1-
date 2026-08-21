package org.lpv.util;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
public class Conexion {
 
    private static Conexion instancia;
 
    private static final String URL =
            "jdbc:mysql://localhost:3306/libreriadb_in4cm?serverTimezone=UTC";
 
    private static final String USER = "IN4CM";
    private static final String PASSWORD = "#NdimAM4";
 
    private Conexion() {
 
        try {
 
            Class.forName("com.mysql.cj.jdbc.Driver");
 
        } catch (ClassNotFoundException e) {
 
            System.err.println(
                    "Error al cargar Driver"
                    + e.getMessage()
            );
        }
    }
 
    public static synchronized Conexion getInstancia() {
 
        if (instancia == null) {
            instancia = new Conexion();
        }
 
        return instancia;
    }
 
    public Connection conectar() throws SQLException {
 
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

