package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lpv.dao.AutorDAO;
import org.lpv.model.Autor;
import org.lpv.util.Conexion;

public class AutorDAOImpl implements AutorDAO {

    private static final Logger log = Logger.getLogger(AutorDAOImpl.class.getName());

    @Override
    public List<Autor> listar() {
        log.info("Listando autores");
        List<Autor> autores = new ArrayList<>();
        String sql = "{call sp_listarautores()}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            while (tablaResultado.next()) {
                autores.add(mapearAutor(tablaResultado));
            }
            log.info("Autores listados: " + autores.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar autores", e);
        }
        return autores;
    }

    @Override
    public Autor buscar(Integer idAutor) {
        log.info("Buscando autor por id: " + idAutor);
        Autor autor = null;
        String sql = "{call sp_buscarautor(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, idAutor);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    autor = mapearAutor(tablaResultado);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al buscar autor por id: " + idAutor, e);
        }
        return autor;
    }

    @Override
    public boolean insertar(Autor objeto) {
        log.info("Insertando autor: " + objeto.getNombreAutor() + " " + objeto.getApellidoAutor());
        String sql = "{call sp_insertarautor(?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, objeto.getNombreAutor());
            consulta.setString(2, objeto.getApellidoAutor());
            consulta.setString(3, objeto.getNacionalidad());
            consulta.setString(4, objeto.getBiografia());
            int filasAfectadas = consulta.executeUpdate();
            boolean creado = filasAfectadas > 0;
            if (creado) log.info("Autor insertado: " + objeto.getNombreAutor());
            return creado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al insertar autor: " + objeto.getNombreAutor(), e);
            return false;
        }
    }

    @Override
    public boolean actualizar(Autor objeto) {
        log.info("Actualizando autor: " + objeto.getIdAutor());
        String sql = "{call sp_actualizarautor(?, ?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, objeto.getIdAutor());
            consulta.setString(2, objeto.getNombreAutor());
            consulta.setString(3, objeto.getApellidoAutor());
            consulta.setString(4, objeto.getNacionalidad());
            consulta.setString(5, objeto.getBiografia());
            int filasAfectadas = consulta.executeUpdate();
            boolean actualizado = filasAfectadas > 0;
            if (actualizado) log.info("Autor actualizado: " + objeto.getIdAutor());
            return actualizado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al actualizar autor: " + objeto.getIdAutor(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer idAutor) {
        log.info("Eliminando autor: " + idAutor);
        String sql = "{call sp_eliminarautor(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, idAutor);
            int filasAfectadas = consulta.executeUpdate();
            boolean eliminado = filasAfectadas > 0;
            if (eliminado) log.info("Autor eliminado: " + idAutor);
            return eliminado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al eliminar autor: " + idAutor, e);
            return false;
        }
    }

    @Override
    public boolean asociarLibro(int idAutor, String isbn) {
        log.info("Asociando autor " + idAutor + " con libro " + isbn);
        String sql = "{call sp_insertarautorlibro(?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, idAutor);
            consulta.setString(2, isbn);
            int filasAfectadas = consulta.executeUpdate();
            boolean asociado = filasAfectadas > 0;
            if (asociado) log.info("Autor " + idAutor + " asociado con libro " + isbn);
            return asociado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al asociar autor " + idAutor + " con libro " + isbn, e);
            return false;
        }
    }

    private Autor mapearAutor(ResultSet rs) throws SQLException {
        Autor autor = new Autor();
        autor.setIdAutor(rs.getInt("id_autor"));
        autor.setNombreAutor(rs.getString("nombre_autor"));
        autor.setApellidoAutor(rs.getString("apellido_autor"));
        autor.setNacionalidad(rs.getString("nacionalidad"));
        autor.setBiografia(rs.getString("biografia"));
        return autor;
    }
}