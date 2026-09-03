package org.lpv.dao;

import java.util.List;
import org.lpv.model.Libros;

public interface LibrosDAO extends CRUD<Libros, String> {
    List<Libros> buscarPorTitulo(String titulo);
    List<Libros> buscarPorAutor(String autor);
}