package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lpv.dao.EditorialDAO;
import org.lpv.model.Editorial;
import org.lpv.util.Conexion;

public class EditorialDAOImpl implements EditorialDAO {

    private static final Logger log = Logger.getLogger(EditorialDAOImpl.class.getName());

    @Override
    public List<Editorial> listar() {
        log.info("Listando editoriales");
        List<Editorial> editoriales = new ArrayList<>();
        String sql = "{call sp_listareditoriales()}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            while (tablaResultado.next()) {
                editoriales.add(mapearEditorial(tablaResultado));
            }
            log.info("Editoriales listadas: " + editoriales.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar editoriales", e);
        }
        return editoriales;
    }

    @Override
    public Editorial buscar(String nit) {
        log.info("Buscando editorial: NIT " + nit);
        Editorial editorial = null;
        String sql = "{call sp_buscareditorial(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, nit);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    editorial = mapearEditorial(tablaResultado);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al buscar editorial: NIT " + nit, e);
        }
        return editorial;
    }

    @Override
    public boolean insertar(Editorial objeto) {
        log.info("Insertando editorial: NIT " + objeto.getNit());
        String sql = "{call sp_insertareditorial(?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, objeto.getNit());
            consulta.setString(2, objeto.getNombreEditorial());
            consulta.setString(3, objeto.getTelefonoEditorial());
            consulta.setString(4, objeto.getDireccionEditoria());
            int filasAfectadas = consulta.executeUpdate();
            boolean creada = filasAfectadas > 0;
            if (creada) log.info("Editorial insertada: NIT " + objeto.getNit());
            return creada;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al insertar editorial: NIT " + objeto.getNit(), e);
            return false;
        }
    }

    @Override
    public boolean actualizar(Editorial objeto) {
        log.info("Actualizando editorial: NIT " + objeto.getNit());
        String sql = "{call sp_actualizareditorial(?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, objeto.getNit());
            consulta.setString(2, objeto.getNombreEditorial());
            consulta.setString(3, objeto.getTelefonoEditorial());
            consulta.setString(4, objeto.getDireccionEditoria());
            int filasAfectadas = consulta.executeUpdate();
            boolean actualizada = filasAfectadas > 0;
            if (actualizada) log.info("Editorial actualizada: NIT " + objeto.getNit());
            return actualizada;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al actualizar editorial: NIT " + objeto.getNit(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(String nit) {
        log.info("Eliminando editorial: NIT " + nit);
        String sql = "{call sp_eliminareditorial(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, nit);
            int filasAfectadas = consulta.executeUpdate();
            boolean eliminada = filasAfectadas > 0;
            if (eliminada) log.info("Editorial eliminada: NIT " + nit);
            return eliminada;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al eliminar editorial: NIT " + nit, e);
            return false;
        }
    }

    private Editorial mapearEditorial(ResultSet rs) throws SQLException {
        Editorial editorial = new Editorial();
        editorial.setNit(rs.getString("nit"));
        editorial.setNombreEditorial(rs.getString("nombre_editorial"));
        editorial.setTelefonoEditorial(rs.getString("telefono_editorial"));
        editorial.setDireccionEditoria(rs.getString("direccion_editoria"));
        return editorial;
    }
}