package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lpv.dao.ClienteDAO;
import org.lpv.model.Clientes;
import org.lpv.util.Conexion;


public class ClientesDAOImpl implements ClienteDAO {

    private static final Logger log = Logger.getLogger(ClientesDAOImpl.class.getName());

    @Override
    public List<Clientes> listar() {
        List<Clientes> clientes = new ArrayList<>();
        String sql = "{call sp_listarclientes()}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {

            while (tablaResultado.next()) {
                clientes.add(mapearCliente(tablaResultado));
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar clientes", e);
        }
        return clientes;
    }

    @Override
    public Clientes buscar(Long cui) {
        Clientes cliente = null;
        String sql = "{call sp_buscarcliente(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setLong(1, cui);

            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    cliente = mapearCliente(tablaResultado);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al buscar cliente: CUI " + cui, e);
        }
        return cliente;
    }

    @Override
    public boolean insertar(Clientes objeto) {
        String sql = "{call sp_insertarcliente(?, ?, ?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setLong(1, objeto.getCui());
            consulta.setString(2, objeto.getNombreCliente());
            consulta.setString(3, objeto.getApellidoCliente());
            consulta.setString(4, objeto.getCorreoElectronico());

            int filasAfectadas = consulta.executeUpdate();
            boolean creado = filasAfectadas > 0;
            if (creado) {
                log.info("Cliente registrado: CUI " + objeto.getCui());
            }
            return creado;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al insertar cliente: CUI " + objeto.getCui(), e);
            return false;
        }
    }

    @Override
    public boolean actualizar(Clientes objeto) {
        String sql = "{call sp_actualizarcliente(?, ?, ?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setLong(1, objeto.getCui());
            consulta.setString(2, objeto.getNombreCliente());
            consulta.setString(3, objeto.getApellidoCliente());
            consulta.setString(4, objeto.getCorreoElectronico());

            int filasAfectadas = consulta.executeUpdate();
            boolean actualizado = filasAfectadas > 0;
            if (actualizado) {
                log.info("Cliente actualizado: CUI " + objeto.getCui());
            }
            return actualizado;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al actualizar cliente: CUI " + objeto.getCui(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(Long cui) {
        String sql = "{call sp_eliminarcliente(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setLong(1, cui);

            int filasAfectadas = consulta.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al eliminar cliente: CUI " + cui, e);
            return false;
        }
    }

    private Clientes mapearCliente(ResultSet rs) throws SQLException {
        Clientes cliente = new Clientes();
        cliente.setCui(rs.getLong("cui"));
        cliente.setNombreCliente(rs.getString("nombre_cliente"));
        cliente.setApellidoCliente(rs.getString("apellido_cliente"));
        cliente.setCorreoElectronico(rs.getString("correo_electronico"));
        return cliente;
    }
}
