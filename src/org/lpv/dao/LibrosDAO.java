package org.lpv.dao;

import java.util.List;
import org.lpv.model.Libros;

public interface LibrosDAO extends CRUD<Libros, String> {
    boolean activar(String isbn);
    List<Libros> listarStockCritico();
    boolean actualizarPrecio(String isbn, double precio);
}