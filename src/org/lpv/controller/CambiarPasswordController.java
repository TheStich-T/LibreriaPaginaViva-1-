package org.lpv.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.lpv.dao.UsuarioDAO;
import org.lpv.dao.impl.UsuarioDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.manager.SessionContext;
import org.lpv.model.Usuario;
import org.lpv.util.SecurityUtil;

public class CambiarPasswordController implements Initializable {

    @FXML private PasswordField txtPasswordActual;
    @FXML private PasswordField txtPasswordNueva;
    @FXML private PasswordField txtConfirmarPassword;
    @FXML private Button btnCambiar;
    @FXML private Button btnRegresar;
    @FXML private Label lblMensaje;

    private UsuarioDAO usuarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usuarioDAO = new UsuarioDAOImpl();
        lblMensaje.setText("");
    }

    @FXML
    public void eventoCambiarPassword(ActionEvent evento) {
        try {
            ValidarException.validarNoVacio(txtPasswordActual.getText(), "contraseña actual");
            ValidarException.validarNoVacio(txtPasswordNueva.getText(), "contraseña nueva");
            ValidarException.validarNoVacio(txtConfirmarPassword.getText(), "confirmar contraseña");

            ValidarException.validarCoincidencia(txtPasswordNueva.getText(),
                    txtConfirmarPassword.getText(), "Las contraseñas no coinciden");
            ValidarException.validarLongitudMinima(txtPasswordNueva.getText(), 6,
                    "La contraseña debe tener al menos 6 caracteres");

            Usuario usuarioActual = SessionContext.getInstancia().getUsuairoActual();
            ValidarException.validarNulo(usuarioActual, "No hay una sesión activa");

            String hashActual = SecurityUtil.hashSHA256(txtPasswordActual.getText());
            Usuario validado = usuarioDAO.validarCredenciales(usuarioActual.getUsername(), hashActual);

            if (validado == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "La contraseña actual es incorrecta");
                lblMensaje.setText("La contraseña actual es incorrecta");
                return;
            }

            String hashNuevo = SecurityUtil.hashSHA256(txtPasswordNueva.getText());
            boolean actualizado = usuarioDAO.actualizarPassword(usuarioActual.getUsername(), hashNuevo);

            if (actualizado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Contraseña actualizada con éxito");
                limpiarCampos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar la contraseña");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoRegresar(ActionEvent evento) {
        limpiarCampos();
    }

    private void limpiarCampos() {
        txtPasswordActual.clear();
        txtPasswordNueva.clear();
        txtConfirmarPassword.clear();
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}