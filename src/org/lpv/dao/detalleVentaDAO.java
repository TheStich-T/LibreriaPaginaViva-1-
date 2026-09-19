package org.lpv.dao;

import java.util.List;
import org.lpv.model.detalleVenta;

public interface detalleVentaDAO extends CRUD<detalleVenta, Integer> {

    List<detalleVenta> listarPorVenta(int idVenta);
}
