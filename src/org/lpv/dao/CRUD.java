package org.lpv.dao;

import java.util.List; 

public interface CRUD<T, ID> {
    
    boolean insertar (T objeto); 
    List<T> listar(); 
    T buscar(ID id);
    boolean actualizar(T objeto); 
    boolean eliminar(ID id); 
}
