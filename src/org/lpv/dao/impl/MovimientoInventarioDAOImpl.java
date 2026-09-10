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
import org.lpv.dao.MovimientoInventarioDAO;
import org.lpv.model.MovimientoInventario;
import org.lpv.util.Conexion;

public class MovimientoInventarioDAOImpl implements MovimientoInventarioDAO {

    private static final Logger log = Logger.getLogger(MovimientoInventarioDAOImpl.class.getName());

    @Override
    public boolean registrarIngreso(MovimientoInventario movimiento) {

        if (movimiento == null || movimiento.getIsbn() == null) {
            log.warning("No se puede registrar un ingreso sin datos.");
            return false;
        }

        if (movimiento.getCantidad() <= 0) {
            log.warning("Cantidad inválida para el ingreso: " + movimiento.getCantidad());
            return false;
        }

        log.info("Registrando ingreso de inventario para el libro: " + movimiento.getIsbn());

        String sqlStock = "select stock_actual, stock_minimo, activo "
                + "from libros where isbn = ? for update";
        String sqlMovimiento = "{call sp_registrarmovimiento(?, ?, ?, ?, ?, ?)}";
        String sqlActualizarStock = "{call sp_actualizarstocklibro(?, ?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar()) {
            conexion.setAutoCommit(false);

            try {
                int stockActual;
                int stockMinimo;
                boolean activo;

                try (PreparedStatement consultaStock = conexion.prepareStatement(sqlStock)) {
                    consultaStock.setString(1, movimiento.getIsbn());
                    try (ResultSet resultadoStock = consultaStock.executeQuery()) {
                        if (!resultadoStock.next()) {
                            throw new SQLException("El libro " + movimiento.getIsbn() + " no existe.");
                        }
                        stockActual = resultadoStock.getInt("stock_actual");
                        stockMinimo = resultadoStock.getInt("stock_minimo");
                        activo = resultadoStock.getBoolean("activo");
                    }
                }

                if (!activo) {
                    throw new SQLException("El libro " + movimiento.getIsbn() + " está inactivo.");
                }

               
                try (CallableStatement consultaMovimiento = conexion.prepareCall(sqlMovimiento)) {
                    consultaMovimiento.setString(1, movimiento.getIsbn());
                    consultaMovimiento.setString(2, "INGRESO");
                    consultaMovimiento.setInt(3, movimiento.getCantidad());
                    if (movimiento.getIdUsuario() > 0) {
                        consultaMovimiento.setInt(4, movimiento.getIdUsuario());
                    } else {
                        consultaMovimiento.setNull(4, Types.INTEGER);
                    }
                    consultaMovimiento.setString(5, movimiento.getObservacion());
                    if (movimiento.getNitProveedor() != null && !movimiento.getNitProveedor().isBlank()) {
                        consultaMovimiento.setString(6, movimiento.getNitProveedor());
                    } else {
                        consultaMovimiento.setNull(6, Types.VARCHAR);
                    }
                    consultaMovimiento.executeUpdate();
                }

                // nunca se actualiza el stock sin dejar registrado el movimiento
                int nuevoStock = stockActual + movimiento.getCantidad();
                try (CallableStatement consultaActualizar = conexion.prepareCall(sqlActualizarStock)) {
                    consultaActualizar.setString(1, movimiento.getIsbn());
                    consultaActualizar.setInt(2, nuevoStock);
                    consultaActualizar.setInt(3, stockMinimo);
                    consultaActualizar.executeUpdate();
                }

                conexion.commit();
                log.info("Ingreso registrado correctamente para el libro: " + movimiento.getIsbn()
                        + ". Nuevo stock: " + nuevoStock);
                return true;

            } catch (SQLException e) {
                try {
                    conexion.rollback();
                } catch (SQLException rollbackError) {
                    log.log(Level.SEVERE, "Error al hacer rollback del ingreso", rollbackError);
                }
                log.log(Level.SEVERE, "Error al registrar ingreso de inventario", e);
                return false;

            } finally {
                try {
                    conexion.setAutoCommit(true);
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "Error al restaurar autoCommit", e);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error de conexión al registrar ingreso de inventario", e);
            return false;
        }
    }

    @Override
    public List<MovimientoInventario> listarPorIsbn(String isbn) {

        log.info("Listando movimientos de inventario para el libro: " + isbn);

        List<MovimientoInventario> movimientos = new ArrayList<>();
        String sql = "{call sp_listarmovimientos(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setString(1, isbn);

            try (ResultSet tablaResultado = consulta.executeQuery()) {
                while (tablaResultado.next()) {
                    movimientos.add(mapearMovimiento(tablaResultado));
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar movimientos de inventario del libro: " + isbn, e);
        }
        return movimientos;
    }

    private MovimientoInventario mapearMovimiento(ResultSet rs) throws SQLException {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setIdMovimiento(rs.getInt("id_movimiento"));
        movimiento.setIsbn(rs.getString("isbn"));
        movimiento.setTipoMovimiento(rs.getString("tipo_movimiento"));
        movimiento.setCantidad(rs.getInt("cantidad"));
        if (rs.getTimestamp("fecha_movimiento") != null) {
            movimiento.setFechaMovimiento(rs.getTimestamp("fecha_movimiento").toLocalDateTime());
        }
        movimiento.setIdUsuario(rs.getInt("id_usuario"));
        movimiento.setObservacion(rs.getString("observacion"));
        movimiento.setNitProveedor(rs.getString("nit_proveedor"));
        return movimiento;
    }

    @Override
    public boolean insertar(MovimientoInventario objeto) {
        return registrarIngreso(objeto);
    }

    @Override
    public List<MovimientoInventario> listar() {
        log.warning("use listarPorIsbn(isbn)");
        throw new UnsupportedOperationException("Los movimientos se consultan por libro (listarPorIsbn)");
    }

    @Override
    public MovimientoInventario buscar(Integer id) {
        log.warning("operación no soportada");
        throw new UnsupportedOperationException("Los movimientos se consultan por libro (listarPorIsbn)");
    }

    @Override
    public boolean actualizar(MovimientoInventario objeto) {
        log.warning("operación no permitida");
        throw new UnsupportedOperationException("Los movimientos de inventario no se editan");
    }

    @Override
    public boolean eliminar(Integer id) {
        log.warning("operación no permitida");
        throw new UnsupportedOperationException("Los movimientos de inventario no se eliminan");
    }
}
