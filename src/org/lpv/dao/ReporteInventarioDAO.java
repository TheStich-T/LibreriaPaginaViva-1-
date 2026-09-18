package org.lpv.dao;

import java.util.List;
import org.lpv.model.LibroMasVendido;
import org.lpv.model.StockValorizado;

public interface ReporteInventarioDAO {

    List<LibroMasVendido> listarLibrosMasVendidos(int limite);

    List<StockValorizado> listarStockValorizado();
}