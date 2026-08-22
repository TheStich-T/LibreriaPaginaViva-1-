package org.lpv.exception;
 
public class ValidarException extends Exception {
 
    public ValidarException(String mensaje) {
        super(mensaje);
    }
 
    public static void validarNoVacio(String valor, String nombreCampo)
            throws ValidarException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidarException(
                    "El campo " + nombreCampo + "no puede estar vacio");
        }
    }
 
    public static void validarCoincidencia(String a, String b, String mensaje)
            throws ValidarException {
        if (!a.equals(b)) {
            throw new ValidarException(mensaje);
        }
    }
 
    public static void validarLongitudMinima(String valor, int min, String mensaje)
            throws ValidarException {
        if (valor.length() < min) {
            throw new ValidarException(mensaje);
        }
    }
 
    public static void validarNulo(Object obj, String mensaje)
            throws ValidarException {
        if (obj == null) {
            throw new ValidarException(mensaje);
        }
 
    }
}