package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lpv.dao.CategoriaDAO;
import org.lpv.model.Categoria;
import org.lpv.util.Conexion;

public class CategoriaDAOImpl implements CategoriaDAO {

    private static final Logger log = Logger.getLogger(CategoriaDAOImpl.class.getName());

    // usado para llenar el ComboBox<Categoria> en LibrosFormView (patrón ComboBox ligado a FK)
    @Override
    public List<Categoria> listar() {
        log.info("Listando categorías");
        List<Categoria> categorias = new ArrayList<>();
        String sql = "{call sp_listarcategorias()}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            while (tablaResultado.next()) {
                categorias.add(mapearCategoria(tablaResultado));
            }
            log.info("Categorías listadas: " + categorias.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar categorías", e);
        }
        return categorias;
    }

    @Override
    public Categoria buscar(Integer idCategoria) {
        log.info("Buscando categoría por id: " + idCategoria);
        Categoria categoria = null;
        String sql = "{call sp_buscarcategoria(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, idCategoria);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    categoria = mapearCategoria(tablaResultado);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al buscar categoría por id: " + idCategoria, e);
        }
        return categoria;
    }

    @Override
    public boolean insertar(Categoria objeto) {
        log.info("Insertando categoría: " + objeto.getNombreCategoria());
        String sql = "{call sp_insertarcategoria(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, objeto.getNombreCategoria());
            int filasAfectadas = consulta.executeUpdate();
            boolean creada = filasAfectadas > 0;
            if (creada) log.info("Categoría insertada: " + objeto.getNombreCategoria());
            return creada;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al insertar categoría: " + objeto.getNombreCategoria(), e);
            return false;
        }
    }

    @Override
    public boolean actualizar(Categoria objeto) {
        log.info("Actualizando categoría: " + objeto.getIdCategoria());
        String sql = "{call sp_actualizarcategoria(?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, objeto.getIdCategoria());
            consulta.setString(2, objeto.getNombreCategoria());
            int filasAfectadas = consulta.executeUpdate();
            boolean actualizada = filasAfectadas > 0;
            if (actualizada) log.info("Categoría actualizada: " + objeto.getIdCategoria());
            return actualizada;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al actualizar categoría: " + objeto.getIdCategoria(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer idCategoria) {
        log.info("Eliminando categoría: " + idCategoria);
        String sql = "{call sp_eliminarcategoria(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, idCategoria);
            int filasAfectadas = consulta.executeUpdate();
            boolean eliminada = filasAfectadas > 0;
            if (eliminada) log.info("Categoría eliminada: " + idCategoria);
            return eliminada;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al eliminar categoría: " + idCategoria, e);
            return false;
        }
    }

    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNombreCategoria(rs.getString("nombre_categoria"));
        return categoria;
    }
}