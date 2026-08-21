package org.lpv.util;
import org.lpv.model.Usuario;
 
public class SessionContext {
    private static SessionContext instancia;
    private Usuario usuairoActual;
 
    public SessionContext() {
    }
    public static synchronized SessionContext getInstancia(){
        if (instancia == null) {
            instancia = new SessionContext();
        }
        return instancia;
    }
 
    public Usuario getUsuairoActual() {
        return usuairoActual;
    }
 
    public void setUsuairoActual(Usuario usuairoActual) {
        this.usuairoActual = usuairoActual;
    }
    public void cerrarSesion(){
        this.usuairoActual = null;
    }
}