package org.lpv.dao;

import org.lpv.model.Usuario;

public interface UsuarioDAO extends CRUD<Usuario, Integer> {
    Usuario iniciarSesion(String username, String passwordHash);
    boolean registrarUsuario(String username, String passwordHash, String rol,String nombre, String apellido, String correo);
    Usuario validarCredenciales(String username, String passwordHash);
    boolean actualizarPassword(String username, String nuevoPasswordHash);
    Usuario buscarPorUsername(String username);
    boolean activar(int id);
}