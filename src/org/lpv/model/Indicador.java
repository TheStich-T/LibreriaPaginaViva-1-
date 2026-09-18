package org.lpv.model;
 
import java.math.BigDecimal;
 
public class Indicador {
 
    private BigDecimal totalVentas;
    private int totalLibrosActivos;
    private int totalUsuariosActivos;
 
    public Indicador() {
        this.totalVentas = BigDecimal.ZERO;
    }
 
    public Indicador(BigDecimal totalVentas, int totalLibrosActivos, int totalUsuariosActivos) {
        this.totalVentas = totalVentas;
        this.totalLibrosActivos = totalLibrosActivos;
        this.totalUsuariosActivos = totalUsuariosActivos;
    }
 
    public BigDecimal getTotalVentas() {
        return totalVentas;
    }
 
    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }
 
    public int getTotalLibrosActivos() {
        return totalLibrosActivos;
    }
 
    public void setTotalLibrosActivos(int totalLibrosActivos) {
        this.totalLibrosActivos = totalLibrosActivos;
    }
 
    public int getTotalUsuariosActivos() {
        return totalUsuariosActivos;
    }
 
    public void setTotalUsuariosActivos(int totalUsuariosActivos) {
        this.totalUsuariosActivos = totalUsuariosActivos;
    }
}