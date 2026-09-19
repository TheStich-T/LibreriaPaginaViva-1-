package org.lpv.dao;

import org.lpv.model.Autor;

public interface AutorDAO extends CRUD<Autor, Integer> {

    boolean asociarLibro(int idAutor, String isbn);
}