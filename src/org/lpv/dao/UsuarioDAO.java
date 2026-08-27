package org.lpv.dao;

import org.lpv.model.Usuario;
import org.lpv.util.Conexion;
import java.sql.ResultSet; 
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.time.LocalDateTime;
public interface UsuarioDAO {
   
public class UsuarioDao {

    public Usuario iniciarSesion(String username, String passwordHash) {
        Usuario usuario = null;
        String sql = "{call sp_iniciar_sesion(?, ?)}";

        
        try(Connection conexion = Conexion.getInstancia().conectar();
                CallableStatement consultaCall = conexion.prepareCall(sql)) {
            
            consultaCall.setString(1, username);
            consultaCall.setString(2, passwordHash);
            
            try(ResultSet tablaResultado = consultaCall.executeQuery()) {
                if (tablaResultado.next()) {
                    usuario = new Usuario();
                    usuario.setId(tablaResultado.getInt(1)); 
                    usuario.setUsername(tablaResultado.getString(2)); 
                    usuario.setRol(tablaResultado.getString(3));
                    usuario.setPasswordHash(tablaResultado.getString(4));
                    usuario.setActivo(tablaResultado.getBoolean(5));
                    usuario.setFechaCreacion(tablaResultado.getObject(6, LocalDateTime.class));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en iniciar sesion: " + e.getMessage());
        }

        return usuario;
    }

    public boolean registrarUsuario(String username, String password, String rol) {

        return false;
    }                             
}
}
