package org.lpv.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ValidarException extends Exception {

    private static final Logger log = Logger.getLogger(ValidarException.class.getName());

    public ValidarException(String mensaje) {
        super(mensaje);
    }

    public static void validarNoVacio(String valor, String nombreCampo)
            throws ValidarException {

        if (valor == null || valor.trim().isEmpty()) {
            log.log(Level.WARNING,
                    "Validación fallida: el campo {0} está vacío",
                    nombreCampo);

            throw new ValidarException(
                    "El campo " + nombreCampo + "no puede estar vacio");
        }
    }

    public static void validarCoincidencia(String a, String b, String mensaje)
            throws ValidarException {

        if (!a.equals(b)) {
            log.warning("Validación fallida: los valores no coinciden");
            throw new ValidarException(mensaje);
        }
    }

    public static void validarLongitudMinima(
            String valor, int min, String mensaje)
            throws ValidarException {

        if (valor.length() < min) {
            log.log(Level.WARNING,
                    "Validación fallida: longitud menor al mínimo de {0}",
                    min);

            throw new ValidarException(mensaje);
        }
    }

    public static void validarNulo(Object obj, String mensaje)
            throws ValidarException {

        if (obj == null) {
            log.warning("Validación fallida: se recibió un valor nulo");
            throw new ValidarException(mensaje);
        }
    }
}