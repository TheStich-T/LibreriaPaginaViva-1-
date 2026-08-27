package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.lpv.dao.UsuarioDAO;
import org.lpv.model.Usuario;
import org.lpv.util.Conexion;


public class UsuarioDAOImpl implements UsuarioDAO{
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
        String sql = "{call sp_registrar_usuario(?, ?, ?)}";
        
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            
            consulta.setString(1, username);
            consulta.setString(2, password); // Asegúrate de enviar el hash si ya viene cifrado
            consulta.setString(3, rol);
            
            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error en Registrar Usuario: " + e.getMessage());
            return false;
        }
    }                             

    @Override
    public boolean insertar(Usuario objeto) {
    return registrarUsuario(objeto.getUsername(), objeto.getPasswordHash(), objeto.getRol());
    }

    @Override
    public List<Usuario> listar() {
        return null; 
    }

    @Override
    public Usuario buscar(Integer id) {
        return null; 
    }

    @Override
    public boolean actualizar(Usuario objeto) {
        return false; 
    }

    @Override
    public boolean eliminar(Integer id) {
        return false; 
    }

}

