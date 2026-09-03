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
import java.util.ArrayList; 
import java.util.logging.Logger;


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
            consulta.setString(2, password);
            consulta.setString(3, rol);
            
            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error en Registrar Usuario: " + e.getMessage());
            return false;
        }
    } 
    
    @Override
    public boolean actualizarPassword(String username, String nuevoPasswordHash) {
        String sql = "{call sp_actualizar_password(?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar(); CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setString(1, username);
            consulta.setString(2, nuevoPasswordHash);

            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error en Actualizar Password: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Usuario buscarPorUsername(String username) {
        Usuario usuario = null;
        String sql = "{call sp_buscar_usuario_por_username(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar(); CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setString(1, username);

            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    usuario = new Usuario();
                    usuario.setId(tablaResultado.getInt(1));
                    usuario.setUsername(tablaResultado.getString(2));
                    usuario.setPasswordHash(tablaResultado.getString(3));
                    usuario.setRol(tablaResultado.getString(4));
                    usuario.setActivo(tablaResultado.getBoolean(5));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por username: " + e.getMessage());
        }
        return usuario;
    }
    
    @Override
    public boolean insertar(Usuario objeto) {
    return registrarUsuario(objeto.getUsername(), objeto.getPasswordHash(), objeto.getRol());
    }
   
    @Override
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "{call sp_listar_usuarios()}";

        try (Connection conexion = Conexion.getInstancia().conectar(); CallableStatement consulta = conexion.prepareCall(sql); ResultSet tablaResultado = consulta.executeQuery()) {

            while (tablaResultado.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(tablaResultado.getInt(1));
                usuario.setUsername(tablaResultado.getString(2));
                usuario.setRol(tablaResultado.getString(3));
                usuario.setActivo(tablaResultado.getBoolean(4));
                usuarios.add(usuario);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    @Override
    public Usuario buscar(Integer id) {
        Usuario usuario = null;
        String sql = "{call sp_buscar_usuario_por_id(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar(); CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, id);

            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    usuario = new Usuario();
                    usuario.setId(tablaResultado.getInt(1));
                    usuario.setUsername(tablaResultado.getString(2));
                    usuario.setRol(tablaResultado.getString(3));
                    usuario.setActivo(tablaResultado.getBoolean(4));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por id: " + e.getMessage());
        }
        return usuario;
    }

    @Override
    public boolean actualizar(Usuario objeto) {
        String sql = "{call sp_actualizar_usuario(?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar(); CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, objeto.getId());
            consulta.setString(2, objeto.getRol());

            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        // "eliminar" aquí es desactivar (T1.14) — nunca se borra un usuario, solo se marca inactivo
        String sql = "{call sp_desactivar_usuario(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar(); CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, id);

            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al desactivar usuario: " + e.getMessage());
            return false;
        }
    }
     
   @Override
    public Usuario validarCredenciales(String username, String passwordHash) {
        Usuario usuario = null;
        String sql = "{call sp_iniciar_sesion(?, ?)}"; 
        
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consultaCall = conexion.prepareCall(sql)) {
            
            consultaCall.setString(1, username);
            consultaCall.setString(2, passwordHash);
            
            try (ResultSet tablaResultado = consultaCall.executeQuery()) {
                if (tablaResultado.next()) {
                    usuario = new Usuario();
                    usuario.setId(tablaResultado.getInt(1)); 
                    usuario.setUsername(tablaResultado.getString(2)); 
                    usuario.setRol(tablaResultado.getString(3));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar credenciales: " + e.getMessage());
        }
        return usuario; 
    }

    @Override
    public boolean activar(int id) {
          String sql = "{call sp_activar_usuario(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar(); CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, id);

            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al activar usuario: " + e.getMessage());
            return false;
        }
    }

}

