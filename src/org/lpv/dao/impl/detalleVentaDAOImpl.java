package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.lpv.dao.detalleVentaDAO;
import org.lpv.model.detalleVenta;
import org.lpv.util.Conexion;

public class detalleVentaDAOImpl implements detalleVentaDAO {

    @Override
    public boolean insertar(detalleVenta objeto) {
        String sql = "{call sp_insertardetalleventa(?, ?, ?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, objeto.getIdVenta());
            consulta.setString(2, objeto.getIsbn());
            consulta.setInt(3, objeto.getCantidad());
            consulta.setDouble(4, objeto.getPrecioUnitario());

            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar detalle de venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<detalleVenta> listar() {
        return new ArrayList<>();
    }

    @Override
    public detalleVenta buscar(Integer id) {
        return null;
    }

    @Override
    public boolean actualizar(detalleVenta objeto) {
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        String sql = "{call sp_eliminardetalleventa(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, id);

            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar detalle de venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<detalleVenta> listarPorVenta(int idVenta) {
        List<detalleVenta> detalles = new ArrayList<>();
        String sql = "{call sp_listardetalleventa(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, idVenta);

            try (ResultSet tablaResultado = consulta.executeQuery()) {
                while (tablaResultado.next()) {
                    detalleVenta detalle = new detalleVenta();
                    detalle.setIdDetalle(tablaResultado.getInt("id_detalle"));
                    detalle.setIdVenta(tablaResultado.getInt("id_venta"));
                    detalle.setIsbn(tablaResultado.getString("isbn"));
                    detalle.setCantidad(tablaResultado.getInt("cantidad"));
                    detalle.setPrecioUnitario(tablaResultado.getDouble("precio_unitario"));
                    detalle.setSubtotal(tablaResultado.getDouble("subtotal"));

                    detalles.add(detalle);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar detalle de venta: " + e.getMessage());
        }
        return detalles;
    }
}
