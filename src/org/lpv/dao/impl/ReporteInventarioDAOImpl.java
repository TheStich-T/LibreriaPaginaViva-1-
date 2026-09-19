package org.lpv.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lpv.dao.ReporteInventarioDAO;
import org.lpv.model.LibroMasVendido;
import org.lpv.model.StockValorizado;
import org.lpv.util.Conexion;

public class ReporteInventarioDAOImpl implements ReporteInventarioDAO {

    private static final Logger log = Logger.getLogger(ReporteInventarioDAOImpl.class.getName());

    @Override
    public List<LibroMasVendido> listarLibrosMasVendidos(int limite) {
        log.info("Obteniendo ranking de libros más vendidos (límite=" + limite + ")");
        List<LibroMasVendido> ranking = new ArrayList<>();
        String sql = "{call sp_libromasvendidos(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {

            consulta.setInt(1, limite);

            try (ResultSet tablaResultado = consulta.executeQuery()) {
                while (tablaResultado.next()) {
                    LibroMasVendido libro = new LibroMasVendido(
                            tablaResultado.getString("isbn"),
                            tablaResultado.getString("titulo"),
                            tablaResultado.getInt("unidades_vendidas")
                    );
                    ranking.add(libro);
                }
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al obtener el ranking de libros más vendidos", e);
        }

        return ranking;
    }

    @Override
    public List<StockValorizado> listarStockValorizado() {
        log.info("Obteniendo stock valorizado del inventario");
        List<StockValorizado> stockValorizado = new ArrayList<>();
        String sql = "{call sp_stockvalorizado()}";

        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {

            while (tablaResultado.next()) {
                StockValorizado item = new StockValorizado(
                        tablaResultado.getString("isbn"),
                        tablaResultado.getString("titulo"),
                        tablaResultado.getInt("stock_actual"),
                        tablaResultado.getDouble("precio"),
                        tablaResultado.getDouble("valor_inventario")
                );
                stockValorizado.add(item);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al obtener el stock valorizado", e);
        }

        return stockValorizado;
    }
}