package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lpv.dao.LibrosDAO;
import org.lpv.model.Libros;
import org.lpv.util.Conexion;

public class LibrosDAOImpl implements LibrosDAO {

    private static final Logger log = Logger.getLogger(LibrosDAOImpl.class.getName());

    @Override
    public Libros buscar(String isbn) {

        log.info("Buscando libro por ISBN: " + isbn);

        Libros libro = null;
        String sql = "{call sp_buscarlibroporisbn(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, isbn);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    libro = mapearLibro(tablaResultado);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE,"Error al buscar libro por ISBN: " + isbn, e);
        }
        return libro;
    }

    @Override
    public List<Libros> buscarPorTitulo(String titulo) {

        log.info("Buscando libros por título: " + titulo);

        List<Libros> libros = new ArrayList<>();
        String sql = "{call sp_buscarlibropotitulo(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, titulo);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                while (tablaResultado.next()) {libros.add(mapearLibro(tablaResultado));            
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE,"Error al buscar libros por título: " + titulo, e);
        }
        return libros;
    }

    @Override
    public List<Libros> buscarPorAutor(String autor) {

        log.info("Buscando libros por autor: " + autor);

        List<Libros> libros = new ArrayList<>();
        String sql = "{call sp_buscarlibrosporautor(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, autor);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                while (tablaResultado.next()) {
                    Libros libro = new Libros();
                    libro.setIsbn(tablaResultado.getString("isbn"));
                    libro.setTitulo(tablaResultado.getString("titulo"));
                    libro.setPrecio(tablaResultado.getDouble("precio"));
                    libro.setStockActual(tablaResultado.getInt("stock_actual"));
                    libros.add(libro);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE,"Error al buscar libros por autor: " + autor, e);
        }
        return libros;
    }

    private Libros mapearLibro(ResultSet rs) throws SQLException {
        Libros libro = new Libros();
        libro.setIsbn(rs.getString("isbn"));
        libro.setTitulo(rs.getString("titulo"));
        libro.setFechaPublicacion( rs.getDate("fecha_publicacion") != null? rs.getDate("fecha_publicacion").toLocalDate(): null);
        libro.setPrecio(rs.getDouble("precio"));
        libro.setIdCategoria(rs.getInt("id_categoria"));
        libro.setNitEditorial(rs.getString("nit_editorial"));
        libro.setStockActual(rs.getInt("stock_actual"));
        libro.setStockMinimo(rs.getInt("stock_minimo"));
        libro.setActivo(rs.getBoolean("activo"));
        return libro;
    }

    @Override
    public boolean insertar(Libros objeto) {

        log.info("Insertando libro: " + objeto.getIsbn());

        String sql = "{call sp_insertarlibro(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setString(1, objeto.getIsbn());
            consulta.setString(2, objeto.getTitulo());
            if (objeto.getFechaPublicacion() != null) {
                consulta.setDate(3, java.sql.Date.valueOf(objeto.getFechaPublicacion()));
            } else {
                consulta.setNull(3, java.sql.Types.DATE);
            }
            consulta.setDouble(4, objeto.getPrecio());
            consulta.setInt(5, objeto.getIdCategoria());
            consulta.setString(6, objeto.getNitEditorial());
            consulta.setInt(7, objeto.getStockActual());
            consulta.setInt(8, objeto.getStockMinimo());

            int filasAfectadas = consulta.executeUpdate();
            boolean creado = filasAfectadas > 0;

            if (creado) {
                log.info("Libro insertado: " + objeto.getIsbn());
            }

            return creado;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al insertar libro: " + objeto.getIsbn(), e);
            return false;
        }
    }

    @Override
    public List<Libros> listar() {

        log.info("Listando libros");

        List<Libros> libros = new ArrayList<>();
        String sql = "{call sp_listarlibros()}";

        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql);
            ResultSet tablaResultado = consulta.executeQuery()) {

            while (tablaResultado.next()) {
                libros.add(mapearLibro(tablaResultado));
            }

            log.info("Libros listados correctamente: " + libros.size());

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar libros", e);
        }
        return libros;
    }

    @Override
    public boolean actualizar(Libros objeto) {

        log.info("Actualizando libro: " + objeto.getIsbn());

        String sql = "{call sp_actualizarlibro(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setString(1, objeto.getIsbn());
            consulta.setString(2, objeto.getTitulo());
            if (objeto.getFechaPublicacion() != null) {
                consulta.setDate(3, java.sql.Date.valueOf(objeto.getFechaPublicacion()));
            } else {
                consulta.setNull(3, java.sql.Types.DATE);
            }
            consulta.setDouble(4, objeto.getPrecio());
            consulta.setInt(5, objeto.getIdCategoria());
            consulta.setString(6, objeto.getNitEditorial());
            consulta.setInt(7, objeto.getStockMinimo());

            int filasAfectadas = consulta.executeUpdate();
            boolean actualizado = filasAfectadas > 0;

            if (actualizado) {
                log.info("Libro actualizado: " + objeto.getIsbn());
            }

            return actualizado;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al actualizar libro: " + objeto.getIsbn(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(String isbn) {

        log.info("Desactivando libro: " + isbn);

        String sql = "{call sp_desactivarlibro(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setString(1, isbn);

            int filasAfectadas = consulta.executeUpdate();
            boolean desactivado = filasAfectadas > 0;

            if (desactivado) {
                log.info("Libro desactivado: " + isbn);
            }

            return desactivado;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al desactivar libro: " + isbn, e);
            return false;
        }
    }

    @Override
    public boolean activar(String isbn) {

        log.info("Activando libro: " + isbn);

        String sql = "{call sp_activarlibro(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setString(1, isbn);

            int filasAfectadas = consulta.executeUpdate();
            boolean activado = filasAfectadas > 0;

            if (activado) {
                log.info("Libro activado: " + isbn);
            }

            return activado;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al activar libro: " + isbn, e);
            return false;
        }
    }
}