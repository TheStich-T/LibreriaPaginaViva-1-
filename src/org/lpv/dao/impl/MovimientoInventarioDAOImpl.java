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
        log.info("Listando salidas de inventario");

        List<MovimientoInventario> movimientos = new ArrayList<>();
        String sql = "{call sp_listarsalidas()}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {

            while (tablaResultado.next()) {
                movimientos.add(mapearMovimiento(tablaResultado));
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar salidas de inventario", e);
        }
        return movimientos;
    }

    @Override
    public MovimientoInventario buscar(Integer id) {
        log.warning("operación no soportada");
        throw new UnsupportedOperationException("Los movimientos se consultan por libro (listarPorIsbn)");
    }

    @Override
    public boolean actualizar(MovimientoInventario objeto) {
        return actualizarMovimiento(objeto);
    }

    @Override
    public boolean eliminar(Integer id) {
        log.warning("operación no permitida");
        throw new UnsupportedOperationException("Los movimientos de inventario no se eliminan");
    }

    @Override
    public List<MovimientoInventario> listarIngresos() {
        log.info("Listando ingresos de inventario");

        List<MovimientoInventario> movimientos = new ArrayList<>();
        String sql = "select id_movimiento, isbn, tipo_movimiento, cantidad, fecha_movimiento, "
                + "id_usuario, observacion, nit_proveedor "
                + "from movimientos_inventario "
                + "where tipo_movimiento = 'INGRESO' "
                + "order by fecha_movimiento desc, id_movimiento desc";

        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {

            while (tablaResultado.next()) {
                movimientos.add(mapearMovimiento(tablaResultado));
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar ingresos de inventario", e);
        }
        return movimientos;
    }

    private int efectoEnStock(String tipoMovimiento, int cantidad) throws SQLException {
        if ("INGRESO".equals(tipoMovimiento)) {
            return cantidad;
        }
        if ("MERMA".equals(tipoMovimiento) || "TRASLADO".equals(tipoMovimiento) || "DEVOLUCION".equals(tipoMovimiento)) {
            return -cantidad;
        }
        throw new SQLException("El tipo de movimiento " + tipoMovimiento + " no se puede editar.");
    }

    private int[] leerStockLibro(Connection conexion, String isbn) throws SQLException {
        String sqlStock = "select stock_actual, stock_minimo, activo from libros where isbn = ? for update";
        try (PreparedStatement consultaStock = conexion.prepareStatement(sqlStock)) {
            consultaStock.setString(1, isbn);
            try (ResultSet resultado = consultaStock.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("El libro " + isbn + " no existe.");
                }
                return new int[]{
                    resultado.getInt("stock_actual"),
                    resultado.getInt("stock_minimo"),
                    resultado.getBoolean("activo") ? 1 : 0
                };
            }
        }
    }

    private void guardarStockLibro(Connection conexion, String isbn, int stock, int stockMinimo) throws SQLException {
        if (stock < 0) {
            throw new SQLException("El stock del libro " + isbn + " quedaría en negativo (" + stock + ").");
        }
        try (CallableStatement consultaActualizar = conexion.prepareCall("{call sp_actualizarstocklibro(?, ?, ?)}")) {
            consultaActualizar.setString(1, isbn);
            consultaActualizar.setInt(2, stock);
            consultaActualizar.setInt(3, stockMinimo);
            consultaActualizar.executeUpdate();
        }
    }

    @Override
    public boolean actualizarMovimiento(MovimientoInventario movimiento) {

        if (movimiento == null || movimiento.getIdMovimiento() <= 0 || movimiento.getIsbn() == null) {
            log.warning("No se puede actualizar un movimiento sin datos.");
            return false;
        }
        if (movimiento.getCantidad() <= 0) {
            log.warning("Cantidad inválida para actualizar el movimiento: " + movimiento.getCantidad());
            return false;
        }

        log.info("Actualizando movimiento de inventario: " + movimiento.getIdMovimiento());

        String sqlMovimientoActual = "select isbn, tipo_movimiento, cantidad "
                + "from movimientos_inventario where id_movimiento = ? for update";
        String sqlActualizarMovimiento = "update movimientos_inventario "
                + "set isbn = ?, tipo_movimiento = ?, cantidad = ?, observacion = ?, nit_proveedor = ? "
                + "where id_movimiento = ?";

        try (Connection conexion = Conexion.getInstancia().conectar()) {
            conexion.setAutoCommit(false);

            try {
                String isbnAnterior;
                String tipoAnterior;
                int cantidadAnterior;

                try (PreparedStatement consulta = conexion.prepareStatement(sqlMovimientoActual)) {
                    consulta.setInt(1, movimiento.getIdMovimiento());
                    try (ResultSet resultado = consulta.executeQuery()) {
                        if (!resultado.next()) {
                            throw new SQLException("El movimiento " + movimiento.getIdMovimiento() + " no existe.");
                        }
                        isbnAnterior = resultado.getString("isbn");
                        tipoAnterior = resultado.getString("tipo_movimiento");
                        cantidadAnterior = resultado.getInt("cantidad");
                    }
                }

                boolean eraIngreso = "INGRESO".equals(tipoAnterior);
                boolean esIngreso = "INGRESO".equals(movimiento.getTipoMovimiento());
                if (eraIngreso != esIngreso) {
                    throw new SQLException("No se puede cambiar un ingreso por una salida (ni al revés).");
                }

                int efectoAnterior = efectoEnStock(tipoAnterior, cantidadAnterior);
                int efectoNuevo = efectoEnStock(movimiento.getTipoMovimiento(), movimiento.getCantidad());

                int[] datosLibroAnterior = leerStockLibro(conexion, isbnAnterior);
                boolean mismoLibro = isbnAnterior.equals(movimiento.getIsbn());
                int[] datosLibroNuevo = mismoLibro ? datosLibroAnterior : leerStockLibro(conexion, movimiento.getIsbn());

                if (datosLibroNuevo[2] == 0) {
                    throw new SQLException("El libro " + movimiento.getIsbn() + " está inactivo.");
                }

                try (PreparedStatement consulta = conexion.prepareStatement(sqlActualizarMovimiento)) {
                    consulta.setString(1, movimiento.getIsbn());
                    consulta.setString(2, movimiento.getTipoMovimiento());
                    consulta.setInt(3, movimiento.getCantidad());
                    consulta.setString(4, movimiento.getObservacion());
                    if (movimiento.getNitProveedor() != null && !movimiento.getNitProveedor().isBlank()) {
                        consulta.setString(5, movimiento.getNitProveedor());
                    } else {
                        consulta.setNull(5, Types.VARCHAR);
                    }
                    consulta.setInt(6, movimiento.getIdMovimiento());
                    consulta.executeUpdate();
                }

                if (mismoLibro) {
                    int stockFinal = datosLibroAnterior[0] - efectoAnterior + efectoNuevo;
                    guardarStockLibro(conexion, isbnAnterior, stockFinal, datosLibroAnterior[1]);
                } else {
                    guardarStockLibro(conexion, isbnAnterior, datosLibroAnterior[0] - efectoAnterior, datosLibroAnterior[1]);
                    guardarStockLibro(conexion, movimiento.getIsbn(), datosLibroNuevo[0] + efectoNuevo, datosLibroNuevo[1]);
                }

                conexion.commit();
                log.info("Movimiento actualizado correctamente: " + movimiento.getIdMovimiento());
                return true;

            } catch (SQLException e) {
                try {
                    conexion.rollback();
                } catch (SQLException rollbackError) {
                    log.log(Level.SEVERE, "Error al hacer rollback de la actualización", rollbackError);
                }
                log.log(Level.SEVERE, "Error al actualizar movimiento de inventario", e);
                return false;

            } finally {
                try {
                    conexion.setAutoCommit(true);
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "Error al restaurar autoCommit", e);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error de conexión al actualizar movimiento de inventario", e);
            return false;
        }
    }

    @Override
    public boolean registrarSalida(MovimientoInventario movimiento) {

        if (movimiento == null || movimiento.getIsbn() == null) {
            log.warning("No se puede registrar una salida sin datos.");
            return false;
        }
        if (movimiento.getCantidad() <= 0) {
            log.warning("Cantidad inválida para la salida: " + movimiento.getCantidad());
            return false;
        }

        log.info("Registrando salida de inventario para el libro: " + movimiento.getIsbn());
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
                if (movimiento.getCantidad() > stockActual) {
                    throw new SQLException("La cantidad de salida supera el stock actual ("
                            + stockActual + ") del libro " + movimiento.getIsbn() + ".");
                }
                try (CallableStatement consultaMovimiento = conexion.prepareCall(sqlMovimiento)) {
                    consultaMovimiento.setString(1, movimiento.getIsbn());
                    consultaMovimiento.setString(2, movimiento.getTipoMovimiento());
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

                int nuevoStock = stockActual - movimiento.getCantidad();
                try (CallableStatement consultaActualizar = conexion.prepareCall(sqlActualizarStock)) {
                    consultaActualizar.setString(1, movimiento.getIsbn());
                    consultaActualizar.setInt(2, nuevoStock);
                    consultaActualizar.setInt(3, stockMinimo);
                    consultaActualizar.executeUpdate();
                }

                conexion.commit();
                log.info("Salida registrada correctamente para el libro: " + movimiento.getIsbn()
                        + ". Nuevo stock: " + nuevoStock);
                return true;
            } catch (SQLException e) {
                try {
                    conexion.rollback();
                } catch (SQLException rollbackError) {
                    log.log(Level.SEVERE, "Error al hacer rollback de la salida", rollbackError);
                }
                log.log(Level.SEVERE, "Error al registrar salida de inventario", e);
                return false;

            } finally {
                try {
                    conexion.setAutoCommit(true);
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "Error al restaurar autoCommit", e);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error de conexión al registrar salida de inventario", e);
            return false;
        }
    }
}