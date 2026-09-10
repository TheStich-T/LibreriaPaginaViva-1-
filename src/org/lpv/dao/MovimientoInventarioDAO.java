package org.lpv.dao;

import java.util.List;
import org.lpv.model.MovimientoInventario;

public interface MovimientoInventarioDAO extends CRUD<MovimientoInventario, Integer> {

    List<MovimientoInventario> listarPorIsbn(String isbn);

    boolean registrarIngreso(MovimientoInventario movimiento);
}
