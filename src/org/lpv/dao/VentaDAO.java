package org.lpv.dao;

import java.util.List;
import org.lpv.model.Venta;

public interface VentaDAO extends CRUD<Venta, Integer> {

    boolean anularVenta(int idVenta, int usuarioAnulacion, String motivoAnulacion);

    List<Venta> listarVentasDelDiaPorUsuario(int idUsuario);
}