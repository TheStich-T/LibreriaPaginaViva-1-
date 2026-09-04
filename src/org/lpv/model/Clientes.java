package org.lpv.model;

public class Clientes {

    private long cui;
    private String nombreCliente;
    private String apellidoCliente;
    private String correoElectronico;

    public Clientes() {
    }

    public Clientes(long cui, String nombreCliente, String apellidoCliente, String correoElectronico) {
        this.cui = cui;
        this.nombreCliente = nombreCliente;
        this.apellidoCliente = apellidoCliente;
        this.correoElectronico = correoElectronico;
    }

    public long getCui() {
        return cui;
    }

    public void setCui(long cui) {
        this.cui = cui;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getApellidoCliente() {
        return apellidoCliente;
    }

    public void setApellidoCliente(String apellidoCliente) {
        this.apellidoCliente = apellidoCliente;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    // el ComboBox<Cliente> usa este texto para mostrar la opción (patrón ComboBox_con_FK.pdf)
    @Override
    public String toString() {
        return cui + " - " + nombreCliente + " " + apellidoCliente;
    }
}
