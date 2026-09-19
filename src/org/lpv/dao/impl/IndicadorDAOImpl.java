package org.lpv.dao.impl;
 
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lpv.dao.IndicadorDAO;
import org.lpv.model.Indicador;
import org.lpv.util.Conexion;
 
public class IndicadorDAOImpl implements IndicadorDAO {
 
    private static final Logger log = Logger.getLogger(IndicadorDAOImpl.class.getName());
 
    @Override
    public Indicador obtenerIndicadores() {
        log.info("Obteniendo indicadores del Dashboard Administrativo");
        Indicador indicadores = new Indicador();
 
        try (Connection conexion = Conexion.getInstancia().conectar()) {
            // totalVentas ahora representa las ventas del día actual
            indicadores.setTotalVentas(obtenerVentasDelDia(conexion));
            indicadores.setTotalLibrosActivos(obtenerTotalLibrosActivos(conexion));
            indicadores.setTotalUsuariosActivos(obtenerTotalUsuariosActivos(conexion));
            log.info("Indicadores cargados: ventasDelDia=" + indicadores.getTotalVentas()
                    + ", libros=" + indicadores.getTotalLibrosActivos()
                    + ", usuarios=" + indicadores.getTotalUsuariosActivos());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al obtener los indicadores del dashboard", e);
            return null;
        }
 
        return indicadores;
    }
 
    private BigDecimal obtenerVentasDelDia(Connection conexion) throws SQLException {
        String sql = "{call sp_totalventasdia()}";
        try (CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            if (tablaResultado.next()) {
                BigDecimal total = tablaResultado.getBigDecimal("total_ventas");
                return total != null ? total : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
 
    private int obtenerTotalLibrosActivos(Connection conexion) throws SQLException {
        String sql = "{call sp_totallibrosactivos()}";
        try (CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            if (tablaResultado.next()) {
                return tablaResultado.getInt("total_libros");
            }
        }
        return 0;
    }
 
    private int obtenerTotalUsuariosActivos(Connection conexion) throws SQLException {
        String sql = "{call sp_totalusuariosactivos()}";
        try (CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            if (tablaResultado.next()) {
                return tablaResultado.getInt("total_usuarios");
            }
        }
        return 0;
    }
}