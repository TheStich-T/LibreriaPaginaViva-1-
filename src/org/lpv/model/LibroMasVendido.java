package org.lpv.model;

public class LibroMasVendido {

    private String isbn;
    private String titulo;
    private int unidadesVendidas;

    public LibroMasVendido() {
    }

    public LibroMasVendido(String isbn, String titulo, int unidadesVendidas) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.unidadesVendidas = unidadesVendidas;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public void setUnidadesVendidas(int unidadesVendidas) {
        this.unidadesVendidas = unidadesVendidas;
    }
}