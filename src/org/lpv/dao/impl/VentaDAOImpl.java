package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.lpv.dao.VentaDAO;
import org.lpv.model.Venta;
import org.lpv.util.Conexion;

public class VentaDAOImpl implements VentaDAO {

    @Override
    public boolean insertar(Venta objeto) {
        String sql = "{call sp_insertarventa(?, ?, ?, ?, ?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setDouble(1, objeto.getSubtotal());
            consulta.setDouble(2, objeto.getDescuento());

            if (objeto.getUsuarioAutorizaDescuento() > 0) {
                consulta.setInt(3, objeto.getUsuarioAutorizaDescuento());
            } else {
                consulta.setNull(3, Types.INTEGER);
            }

            consulta.setDouble(4, objeto.getTotal());
            consulta.setLong(5, objeto.getCuiCliente());

            if (objeto.getIdUsuario() > 0) {
                consulta.setInt(6, objeto.getIdUsuario());
            } else {
                consulta.setNull(6, Types.INTEGER);
            }

            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Venta> listar() {
        List<Venta> ventas = new ArrayList<>();
        String sql = "{call sp_listarventas()}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {

            while (tablaResultado.next()) {
                Venta venta = new Venta();
                venta.setIdVenta(tablaResultado.getInt("id_venta"));

                if (tablaResultado.getTimestamp("fecha_venta") != null) {
                    venta.setFechaVenta(tablaResultado.getTimestamp("fecha_venta").toLocalDateTime());
                }

                venta.setSubtotal(tablaResultado.getDouble("subtotal"));
                venta.setDescuento(tablaResultado.getDouble("descuento"));
                venta.setTotal(tablaResultado.getDouble("total"));
                venta.setEstado(tablaResultado.getString("estado"));
                venta.setCuiCliente(tablaResultado.getLong("cui_cliente"));
                venta.setIdUsuario(tablaResultado.getInt("id_usuario"));

                ventas.add(venta);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ventas: " + e.getMessage());
        }
        return ventas;
    }

    @Override
    public Venta buscar(Integer id) {
        Venta venta = null;
        String sql = "{call sp_buscarventa(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, id);

            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    venta = new Venta();
                    venta.setIdVenta(tablaResultado.getInt("id_venta"));

                    if (tablaResultado.getTimestamp("fecha_venta") != null) {
                        venta.setFechaVenta(tablaResultado.getTimestamp("fecha_venta").toLocalDateTime());
                    }

                    venta.setSubtotal(tablaResultado.getDouble("subtotal"));
                    venta.setDescuento(tablaResultado.getDouble("descuento"));
                    venta.setTotal(tablaResultado.getDouble("total"));
                    venta.setEstado(tablaResultado.getString("estado"));
                    venta.setCuiCliente(tablaResultado.getLong("cui_cliente"));
                    venta.setIdUsuario(tablaResultado.getInt("id_usuario"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar venta por ID: " + e.getMessage());
        }
        return venta;
    }

    @Override
    public boolean actualizar(Venta objeto) {
        return anularVenta(objeto.getIdVenta(), objeto.getUsuarioAnulacion(), objeto.getMotivoAnulacion());
    }

    @Override
    public boolean eliminar(Integer id) {
        String sql = "{call sp_eliminarventa(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, id);

            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean anularVenta(int idVenta, int usuarioAnulacion, String motivoAnulacion) {
        String sql = "{call sp_anularventa(?, ?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, idVenta);
            consulta.setInt(2, usuarioAnulacion);
            consulta.setString(3, motivoAnulacion);

            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al anular venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Venta> listarVentasDelDiaPorUsuario(int idUsuario) {
        List<Venta> ventas = new ArrayList<>();
        String sql = "{call sp_ventasdeldiaporusuario(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, idUsuario);

            try (ResultSet tablaResultado = consulta.executeQuery()) {
                while (tablaResultado.next()) {
                    Venta venta = new Venta();
                    venta.setIdVenta(tablaResultado.getInt("id_venta"));

                    if (tablaResultado.getTimestamp("fecha_venta") != null) {
                        venta.setFechaVenta(tablaResultado.getTimestamp("fecha_venta").toLocalDateTime());
                    }

                    venta.setSubtotal(tablaResultado.getDouble("subtotal"));
                    venta.setDescuento(tablaResultado.getDouble("descuento"));
                    venta.setTotal(tablaResultado.getDouble("total"));
                    venta.setEstado(tablaResultado.getString("estado"));

                    ventas.add(venta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ventas del día por usuario: " + e.getMessage());
        }
        return ventas;
    }
}
