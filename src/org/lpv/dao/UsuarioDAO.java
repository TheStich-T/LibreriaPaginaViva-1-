package org.lpv.dao;

import org.lpv.model.Usuario;

public interface UsuarioDAO extends CRUD<Usuario,Integer> {
    Usuario iniciarSesion(String username, String passwordHash);
    boolean registrarUsuario(String username, String password, String rol);
    Usuario validarCredenciales(String username, String passwordHash);
    boolean actualizarPassword(String username, String nuevoPasswordHash);
    Usuario buscarPorUsername(String username);
}
   

