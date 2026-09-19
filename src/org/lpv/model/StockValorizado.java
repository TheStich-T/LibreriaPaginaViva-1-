package org.lpv.model;

public class StockValorizado {

    private String isbn;
    private String titulo;
    private int stockActual;
    private double precio;
    private double valorInventario;

    public StockValorizado() {
    }

    public StockValorizado(String isbn, String titulo, int stockActual, double precio, double valorInventario) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.stockActual = stockActual;
        this.precio = precio;
        this.valorInventario = valorInventario;
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

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getValorInventario() {
        return valorInventario;
    }

    public void setValorInventario(double valorInventario) {
        this.valorInventario = valorInventario;
    }
}