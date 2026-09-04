package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lpv.dao.detalleVentaDAO;
import org.lpv.model.detalleVenta;
import org.lpv.util.Conexion;

public class detalleVentaDAOImpl implements detalleVentaDAO {

    private static final Logger log = Logger.getLogger(detalleVentaDAOImpl.class.getName());

    @Override
    public boolean insertar(detalleVenta objeto) {
        
        log.info("Insertando detalle de venta para venta: " + objeto.getIdVenta());

        String sql = "{call sp_insertardetalleventa(?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, objeto.getIdVenta());
            consulta.setString(2, objeto.getIsbn());
            consulta.setInt(3, objeto.getCantidad());
            consulta.setDouble(4, objeto.getPrecioUnitario());
            int filasAfectadas = consulta.executeUpdate();
            boolean insertado = filasAfectadas > 0;
            if (insertado) {
               log.info("Detalle de venta insertado para venta: " + objeto.getIdVenta());
            }
            return insertado;
        } catch (SQLException e) {
            log.log(Level.SEVERE,"Error al insertar detalle de venta", e);
            return false;
        }
    }

    @Override
    public List<detalleVenta> listar() {

        log.info("Listando detalles de venta");
        return new ArrayList<>();
    }

    @Override
    public detalleVenta buscar(Integer id) {
        log.warning("Buscar detalle de venta aún no está " + "implementado: ID " + id);
        return null;
    }

    @Override
    public boolean actualizar(detalleVenta objeto) {
        log.warning("Actualizar detalle de venta aún " + "no está implementado");
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        log.info( "Eliminando detalle de venta: " + id);
        
        String sql ="{call sp_eliminardetalleventa(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, id);
            int filasAfectadas = consulta.executeUpdate();
            boolean eliminado = filasAfectadas > 0;
            if (eliminado) {
               log.info("Detalle de venta eliminado: " + id);
            }
            return eliminado;
        } catch (SQLException e) {
            log.log(Level.SEVERE,"Error al eliminar detalle de venta: " + id,e);
            return false;
        }
    }

    @Override
    public List<detalleVenta> listarPorVenta(int idVenta) {
        log.info("Listando detalles de la venta: " + idVenta);
        List<detalleVenta> detalles = new ArrayList<>();

        String sql = "{call sp_listardetalleventa(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, idVenta);

            try (ResultSet tablaResultado = consulta.executeQuery()) {
                while (tablaResultado.next()) {
                    detalleVenta detalle = new detalleVenta();
                    detalle.setIdDetalle( tablaResultado.getInt("id_detalle"));
                    detalle.setIdVenta(tablaResultado.getInt("id_venta"));
                    detalle.setIsbn(tablaResultado.getString("isbn"));
                    detalle.setCantidad(tablaResultado.getInt("cantidad"));
                    detalle.setPrecioUnitario(tablaResultado.getDouble("precio_unitario"));
                    detalle.setSubtotal(tablaResultado.getDouble("subtotal"));
                    detalles.add(detalle);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE,"Error al listar detalles de la venta: " + idVenta,e);
        }
        return detalles;
    }
}