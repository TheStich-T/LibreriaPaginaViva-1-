package org.lpv.manager;

public class RolPermisos {
    
public static final String VENTAS = "VENTAS";
    public static final String COMPRAS = "COMPRAS";
    public static final String LIBROS_CONSULTAR = "LIBROS_CONSULTAR";
    public static final String LIBROS_GESTIONAR = "LIBROS_GESTIONAR";
    public static final String STOCK_CONSULTAR = "STOCK_CONSULTAR";
    public static final String STOCK_GESTIONAR = "STOCK_GESTIONAR";
    public static final String USUARIOS = "USUARIOS";

    
    public static boolean tienePermiso(String rol, String modulo) {
        if (rol == null || modulo == null) {
            return false;
        }
        switch (rol.toLowerCase()) {
            case "admin":
                return true; 

            case "bodega":
                return modulo.equals(LIBROS_CONSULTAR)
                        || modulo.equals(LIBROS_GESTIONAR)
                        || modulo.equals(STOCK_CONSULTAR)
                        || modulo.equals(STOCK_GESTIONAR);
           

            case "cajero":
                return modulo.equals(VENTAS)
                        || modulo.equals(COMPRAS)
                        || modulo.equals(STOCK_CONSULTAR);
            

            default:
                return false;
        }
    }

   
    public static String getDashboardPorRol(String rol) {
        if (rol == null) {
            return null;
        }
        switch (rol.toLowerCase()) {
            case "admin":
                return "/org/lpv/view/AdminDashboardView.fxml";
            case "bodega":
                return "/org/lpv/view/BodegaDashboardView.fxml";
            case "cajero":
                return "/org/lpv/view/CajeroDashboardView.fxml";
            default:
                return null;
        }
    }
}
