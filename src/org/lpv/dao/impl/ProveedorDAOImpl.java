package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lpv.dao.ProveedorDAO;
import org.lpv.model.Proveedor;
import org.lpv.util.Conexion;

public class ProveedorDAOImpl implements ProveedorDAO {

    private static final Logger log = Logger.getLogger(ProveedorDAOImpl.class.getName());

    @Override
    public boolean insertar(Proveedor objeto) {
        log.info("Insertando proveedor: " + objeto.getNitProveedor());
        String sql = "{call sp_insertarproveedor(?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, objeto.getNitProveedor());
            consulta.setString(2, objeto.getNombreProveedor());
            consulta.setString(3, objeto.getTelefonoProveedor());
            consulta.setString(4, objeto.getDireccionProveedor());
            int filasAfectadas = consulta.executeUpdate();
            boolean creado = filasAfectadas > 0;
            if (creado) log.info("Proveedor insertado: " + objeto.getNitProveedor());
            return creado; 
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al insertar proveedor: " + objeto.getNitProveedor(), e);
            return false;
        }
    }

    @Override
    public List<Proveedor> listar() {
        log.info("Listando proveedores");
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "{call sp_listarproveedores()}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            while (tablaResultado.next()) {
                proveedores.add(mapearProveedor(tablaResultado));
            }
            log.info("Proveedores listados: " + proveedores.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar proveedores", e);
        }
        return proveedores;
    }

    // no existe un sp_buscarproveedor individual, así que se filtra
    // en memoria sobre el listado (mismo patrón que usa la búsqueda de libros)
    @Override
    public Proveedor buscar(String nitProveedor) {
        return listar().stream()
                .filter(p -> p.getNitProveedor().equals(nitProveedor))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean actualizar(Proveedor objeto) {
        log.info("Actualizando proveedor: " + objeto.getNitProveedor());
        String sql = "{call sp_actualizarproveedor(?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, objeto.getNitProveedor());
            consulta.setString(2, objeto.getNombreProveedor());
            consulta.setString(3, objeto.getTelefonoProveedor());
            consulta.setString(4, objeto.getDireccionProveedor());
            int filasAfectadas = consulta.executeUpdate();
            boolean actualizado = filasAfectadas > 0;
            if (actualizado) log.info("Proveedor actualizado: " + objeto.getNitProveedor());
            return actualizado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al actualizar proveedor: " + objeto.getNitProveedor(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(String nitProveedor) {
        log.info("Eliminando proveedor: " + nitProveedor);
        String sql = "{call sp_eliminarproveedor(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, nitProveedor);
            int filasAfectadas = consulta.executeUpdate();
            boolean eliminado = filasAfectadas > 0;
            if (eliminado) log.info("Proveedor eliminado: " + nitProveedor);
            return eliminado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al eliminar proveedor: " + nitProveedor, e);
            return false;
        }
    }

    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        Proveedor proveedor = new Proveedor();
        proveedor.setNitProveedor(rs.getString("nit_proveedor"));
        proveedor.setNombreProveedor(rs.getString("nombre_proveedor"));
        proveedor.setTelefonoProveedor(rs.getString("telefono_proveedor"));
        proveedor.setDireccionProveedor(rs.getString("direccion_proveedor"));
        return proveedor;
    }
}