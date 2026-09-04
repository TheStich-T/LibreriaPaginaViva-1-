package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lpv.dao.VentaDAO;
import org.lpv.model.Venta;
import org.lpv.model.detalleVenta;
import org.lpv.util.Conexion;

public class VentaDAOImpl implements VentaDAO {
    
     private static final Logger log = Logger.getLogger(VentaDAOImpl.class.getName());

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

    @Override
    public boolean registrarVenta(Venta venta, List<detalleVenta> detalles) {
        if (venta == null || detalles == null || detalles.isEmpty()) {
            log.warning("No se puede registrar una venta sin datos ni detalles.");
            return false;
        }

        String sqlVenta = "{call sp_insertarventa(?, ?, ?, ?, ?, ?)}";
        String sqlId = "select last_insert_id()";
        String sqlStock = "select stock_actual, stock_minimo, activo from libros where isbn = ? for update";
        String sqlDetalle = "{call sp_insertardetalleventa(?, ?, ?, ?)}";
        String sqlActualizarStock = "{call sp_actualizarstocklibro(?, ?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar()) {
            conexion.setAutoCommit(false);

            try {
                try (CallableStatement consultaVenta = conexion.prepareCall(sqlVenta)) {
                    consultaVenta.setDouble(1, venta.getSubtotal());
                    consultaVenta.setDouble(2, venta.getDescuento());
                    if (venta.getUsuarioAutorizaDescuento() > 0) {
                        consultaVenta.setInt(3, venta.getUsuarioAutorizaDescuento());
                    } else {
                        consultaVenta.setNull(3, Types.INTEGER);
                    }
                    consultaVenta.setDouble(4, venta.getTotal());
                    consultaVenta.setLong(5, venta.getCuiCliente());
                    if (venta.getIdUsuario() > 0) {
                        consultaVenta.setInt(6, venta.getIdUsuario());
                    } else {
                        consultaVenta.setNull(6, Types.INTEGER);
                    }
                    consultaVenta.executeUpdate();
                }

                int idVenta;
                try (PreparedStatement consultaId = conexion.prepareStatement(sqlId);
                     ResultSet resultadoId = consultaId.executeQuery()) {
                    if (!resultadoId.next()) {
                        throw new SQLException("No se pudo obtener el número de venta.");
                    }
                    idVenta = resultadoId.getInt(1);
                }

                for (detalleVenta detalle : detalles) {
                    int stockActual;
                    int stockMinimo;
                    boolean activo;

                    try (PreparedStatement consultaStock = conexion.prepareStatement(sqlStock)) {
                        consultaStock.setString(1, detalle.getIsbn());
                        try (ResultSet resultadoStock = consultaStock.executeQuery()) {
                            if (!resultadoStock.next()) {
                                throw new SQLException("El libro " + detalle.getIsbn() + " no existe.");
                            }
                            stockActual = resultadoStock.getInt("stock_actual");
                            stockMinimo = resultadoStock.getInt("stock_minimo");
                            activo = resultadoStock.getBoolean("activo");
                        }
                    }

                    if (!activo) {
                        throw new SQLException("El libro " + detalle.getIsbn() + " está inactivo.");
                    }
                    if (detalle.getCantidad() <= 0) {
                        throw new SQLException("La cantidad debe ser mayor a 0.");
                    }
                    if (stockActual < detalle.getCantidad()) {
                        throw new SQLException("Stock insuficiente para el libro " + detalle.getIsbn() + ".");
                    }

                    detalle.setIdVenta(idVenta);

                    try (CallableStatement consultaDetalle = conexion.prepareCall(sqlDetalle)) {
                        consultaDetalle.setInt(1, idVenta);
                        consultaDetalle.setString(2, detalle.getIsbn());
                        consultaDetalle.setInt(3, detalle.getCantidad());
                        consultaDetalle.setDouble(4, detalle.getPrecioUnitario());
                        consultaDetalle.executeUpdate();
                    }

                    int nuevoStock = stockActual - detalle.getCantidad();
                    try (CallableStatement consultaActualizar = conexion.prepareCall(sqlActualizarStock)) {
                        consultaActualizar.setString(1, detalle.getIsbn());
                        consultaActualizar.setInt(2, nuevoStock);
                        consultaActualizar.setInt(3, stockMinimo);
                        consultaActualizar.executeUpdate();
                    }
                }

                conexion.commit();
                venta.setIdVenta(idVenta);
                return true;

            } catch (SQLException e) {
                try {
                    conexion.rollback();
                } catch (SQLException rollbackError) {
                    log.log(Level.SEVERE, "Error al hacer rollback de la venta", rollbackError);
                }
                log.log(Level.SEVERE, "Error al registrar venta", e);
                return false;
            } finally {
                try {
                    conexion.setAutoCommit(true);
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "Error al restaurar autoCommit", e);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error de conexión al registrar venta", e);
            return false;
        }
    }
}
