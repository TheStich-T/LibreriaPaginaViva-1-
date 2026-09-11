package org.lpv.model;

public class Proveedor {

    private String nitProveedor;
    private String nombreProveedor;
    private String telefonoProveedor;
    private String direccionProveedor;

    public Proveedor() {
    }

    public Proveedor(String nitProveedor, String nombreProveedor,
                      String telefonoProveedor, String direccionProveedor) {
        this.nitProveedor = nitProveedor;
        this.nombreProveedor = nombreProveedor;
        this.telefonoProveedor = telefonoProveedor;
        this.direccionProveedor = direccionProveedor;
    }

    public String getNitProveedor() {
        return nitProveedor;
    }

    public void setNitProveedor(String nitProveedor) {
        this.nitProveedor = nitProveedor;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getTelefonoProveedor() {
        return telefonoProveedor;
    }

    public void setTelefonoProveedor(String telefonoProveedor) {
        this.telefonoProveedor = telefonoProveedor;
    }

    public String getDireccionProveedor() {
        return direccionProveedor;
    }

    public void setDireccionProveedor(String direccionProveedor) {
        this.direccionProveedor = direccionProveedor;
    }

    // usado por el ComboBox para mostrar "NIT - Nombre"
    @Override
    public String toString() {
        return nitProveedor + " - " + nombreProveedor;
    }
}