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
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioDAOImpl implements UsuarioDAO {

    private static final Logger log =
            Logger.getLogger(UsuarioDAOImpl.class.getName());

    public Usuario iniciarSesion(
            String username,
            String passwordHash) {

        log.info(
                "Intentando iniciar sesión para usuario: "
                + username);

        Usuario usuario = null;

        String sql =
                "{call sp_iniciar_sesion(?, ?)}";

        try (Connection conexion =
                Conexion.getInstancia().conectar();
             CallableStatement consultaCall =
                conexion.prepareCall(sql)) {

            consultaCall.setString(
                    1, username);

            consultaCall.setString(
                    2, passwordHash);

            try (ResultSet tablaResultado =
                    consultaCall.executeQuery()) {

                if (tablaResultado.next()) {

                    usuario = new Usuario();

                    usuario.setId(
                            tablaResultado.getInt(1));

                    usuario.setUsername(
                            tablaResultado.getString(2));

                    usuario.setRol(
                            tablaResultado.getString(3));

                    log.info(
                            "Inicio de sesión exitoso para usuario: "
                            + username);

                } else {

                    log.warning(
                            "Inicio de sesión fallido para usuario: "
                            + username);
                }
            }

        } catch (SQLException e) {

            log.log(
                    Level.SEVERE,
                    "Error al iniciar sesión para usuario: "
                    + username,
                    e);
        }

        return usuario;
    }

    public boolean registrarUsuario(
            String username,
            String password,
            String rol) {

        log.info(
                "Registrando usuario: "
                + username
                + ", rol: "
                + rol);

        String sql =
                "{call sp_registrar_usuario(?, ?, ?)}";

        try (Connection conexion =
                Conexion.getInstancia().conectar();
             CallableStatement consulta =
                conexion.prepareCall(sql)) {

            consulta.setString(
                    1, username);

            consulta.setString(
                    2, password);

            consulta.setString(
                    3, rol);

            int filasAfectadas =
                    consulta.executeUpdate();

            boolean registrado =
                    filasAfectadas > 0;
            if (registrado) {
                log.info("Usuario registrado: " + username);
            }

            return registrado;

        } catch (SQLException e) {

            log.log(
                    Level.SEVERE,
                    "Error al registrar usuario: "
                    + username,
                    e);

            return false;
        }
    }

    @Override
    public boolean actualizarPassword(
            String username,
            String nuevoPasswordHash) {

        log.info(
                "Actualizando contraseña para usuario: "
                + username);

        String sql =
                "{call sp_actualizar_password(?, ?)}";

        try (Connection conexion =
                Conexion.getInstancia().conectar();
             CallableStatement consulta =
                conexion.prepareCall(sql)) {

            consulta.setString(
                    1, username);

            consulta.setString(
                    2, nuevoPasswordHash);

            int filasAfectadas =
                    consulta.executeUpdate();

            boolean actualizado =
                    filasAfectadas > 0;

            if (actualizado) {

                log.info(
                        "Contraseña actualizada para usuario: "
                        + username);
            }

            return actualizado;

        } catch (SQLException e) {

            log.log(
                    Level.SEVERE,
                    "Error al actualizar contraseña para usuario: "
                    + username,
                    e);

            return false;
        }
    }

    @Override
    public Usuario buscarPorUsername(
            String username) {

        log.info(
                "Buscando usuario por username: "
                + username);

        Usuario usuario = null;

        String sql =
                "{call sp_buscar_usuario_por_username(?)}";

        try (Connection conexion =
                Conexion.getInstancia().conectar();
             CallableStatement consulta =
                conexion.prepareCall(sql)) {

            consulta.setString(
                    1, username);

            try (ResultSet tablaResultado =
                    consulta.executeQuery()) {

                if (tablaResultado.next()) {

                    usuario = new Usuario();

                    usuario.setId(
                            tablaResultado.getInt(1));

                    usuario.setUsername(
                            tablaResultado.getString(2));

                    usuario.setPasswordHash(
                            tablaResultado.getString(3));

                    usuario.setRol(
                            tablaResultado.getString(4));

                    usuario.setActivo(
                            tablaResultado.getBoolean(5));
                }
            }

        } catch (SQLException e) {

            log.log(
                    Level.SEVERE,
                    "Error al buscar usuario por username: "
                    + username,
                    e);
        }

        return usuario;
    }

    @Override
    public boolean insertar(Usuario objeto) {

        return registrarUsuario(
                objeto.getUsername(),
                objeto.getPasswordHash(),
                objeto.getRol());
    }

    @Override
    public List<Usuario> listar() {

        log.info("Listando usuarios");

        List<Usuario> usuarios =
                new ArrayList<>();

        String sql =
                "{call sp_listar_usuarios()}";

        try (Connection conexion =
                Conexion.getInstancia().conectar();
             CallableStatement consulta =
                conexion.prepareCall(sql);
             ResultSet tablaResultado =
                consulta.executeQuery()) {

            while (tablaResultado.next()) {

                Usuario usuario =
                        new Usuario();

                usuario.setId(
                        tablaResultado.getInt(1));

                usuario.setUsername(
                        tablaResultado.getString(2));

                usuario.setRol(
                        tablaResultado.getString(3));

                usuario.setActivo(
                        tablaResultado.getBoolean(4));

                usuarios.add(usuario);
            }

            log.info(
                    "Usuarios listados correctamente: "
                    + usuarios.size());

        } catch (SQLException e) {

            log.log(
                    Level.SEVERE,
                    "Error al listar usuarios",
                    e);
        }

        return usuarios;
    }

    @Override
    public Usuario buscar(Integer id) {

        log.info(
                "Buscando usuario por ID: "
                + id);

        Usuario usuario = null;

        String sql =
                "{call sp_buscar_usuario_por_id(?)}";

        try (Connection conexion =
                Conexion.getInstancia().conectar();
             CallableStatement consulta =
                conexion.prepareCall(sql)) {

            consulta.setInt(
                    1, id);

            try (ResultSet tablaResultado =
                    consulta.executeQuery()) {

                if (tablaResultado.next()) {

                    usuario =
                            new Usuario();

                    usuario.setId(
                            tablaResultado.getInt(1));

                    usuario.setUsername(
                            tablaResultado.getString(2));

                    usuario.setRol(
                            tablaResultado.getString(3));

                    usuario.setActivo(
                            tablaResultado.getBoolean(4));
                }
            }

        } catch (SQLException e) {

            log.log(
                    Level.SEVERE,
                    "Error al buscar usuario por ID: "
                    + id,
                    e);
        }

        return usuario;
    }

    @Override
    public boolean actualizar(Usuario objeto) {

        log.info(
                "Actualizando usuario: "
                + objeto.getUsername());

        String sql =
                "{call sp_actualizar_usuario(?, ?)}";

        try (Connection conexion =
                Conexion.getInstancia().conectar();
             CallableStatement consulta =
                conexion.prepareCall(sql)) {

            consulta.setInt(
                    1, objeto.getId());

            consulta.setString(
                    2, objeto.getRol());

            int filasAfectadas =
                    consulta.executeUpdate();

            boolean actualizado =
                    filasAfectadas > 0;

            if (actualizado) {

                log.info(
                        "Usuario actualizado: "
                        + objeto.getUsername());
            }

            return actualizado;

        } catch (SQLException e) {

            log.log(
                    Level.SEVERE,
                    "Error al actualizar usuario: "
                    + objeto.getUsername(),
                    e);

            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {

        log.info(
                "Desactivando usuario: "
                + id);

        // "eliminar" aquí es desactivar
        // nunca se borra un usuario,
        // solo se marca inactivo

        String sql =
                "{call sp_desactivar_usuario(?)}";

        try (Connection conexion =
                Conexion.getInstancia().conectar();
             CallableStatement consulta =
                conexion.prepareCall(sql)) {

            consulta.setInt(
                    1, id);

            int filasAfectadas =
                    consulta.executeUpdate();

            boolean desactivado =
                    filasAfectadas > 0;

            if (desactivado) {

                log.info(
                        "Usuario desactivado: "
                        + id);
            }

            return desactivado;

        } catch (SQLException e) {

            log.log(
                    Level.SEVERE,
                    "Error al desactivar usuario: "
                    + id,
                    e);

            return false;
        }
    }

    @Override
    public Usuario validarCredenciales(
            String username,
            String passwordHash) {

        log.info(
                "Validando credenciales para usuario: "
                + username);

        Usuario usuario = null;

        String sql =
                "{call sp_iniciar_sesion(?, ?)}";

        try (Connection conexion =
                Conexion.getInstancia().conectar();
             CallableStatement consultaCall =
                conexion.prepareCall(sql)) {

            consultaCall.setString(
                    1, username);

            consultaCall.setString(
                    2, passwordHash);

            try (ResultSet tablaResultado =
                    consultaCall.executeQuery()) {

                if (tablaResultado.next()) {

                    usuario = new Usuario();

                    usuario.setId(
                            tablaResultado.getInt(1));

                    usuario.setUsername(
                            tablaResultado.getString(2));

                    usuario.setRol(
                            tablaResultado.getString(3));

                    log.info(
                            "Credenciales válidas para usuario: "
                            + username);

                } else {

                    log.warning(
                            "Credenciales no válidas para usuario: "
                            + username);
                }
            }

        } catch (SQLException e) {

            log.log(
                    Level.SEVERE,
                    "Error al validar credenciales para usuario: "
                    + username,
                    e);
        }

        return usuario;
    }

    @Override
    public boolean activar(int id) {

        log.info(
                "Activando usuario: "
                + id);

        String sql =
                "{call sp_activar_usuario(?)}";

        try (Connection conexion =
                Conexion.getInstancia().conectar();
             CallableStatement consulta =
                conexion.prepareCall(sql)) {

            consulta.setInt(
                    1, id);

            int filasAfectadas =
                    consulta.executeUpdate();

            boolean activado =
                    filasAfectadas > 0;

            if (activado) {

                log.info(
                        "Usuario activado: "
                        + id);
            }

            return activado;

        } catch (SQLException e) {

            log.log(
                    Level.SEVERE,
                    "Error al activar usuario: "
                    + id,
                    e);

            return false;
        }
    }
}