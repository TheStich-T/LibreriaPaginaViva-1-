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
import javafx.scene.control.TextField;
import org.lpv.dao.UsuarioDAO;
import org.lpv.dao.impl.UsuarioDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.manager.SessionContext;
import org.lpv.model.Usuario;
import org.lpv.util.SecurityUtil;
import java.io.IOException;
import org.lpv.manager.RolPermisos;
import org.lpv.system.main;

public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnIniciarSesion;
    @FXML private Label lblMensaje;

    private UsuarioDAO usuarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usuarioDAO = new UsuarioDAOImpl();
        lblMensaje.setText("");
    }
    
    @FXML
    public void eventoIniciarSesion(ActionEvent evento) {
        try {
            ValidarException.validarNoVacio(txtUsuario.getText(), "usuario");
            ValidarException.validarNoVacio(txtPassword.getText(), "contraseña");

            String username = txtUsuario.getText().trim();
            String passwordHash = SecurityUtil.hashSHA256(txtPassword.getText());

            Usuario usuarioEncontrado = usuarioDAO.buscarPorUsername(username);

            if (usuarioEncontrado == null || !usuarioEncontrado.getPasswordHash().equals(passwordHash)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Usuario o contraseña incorrectos");
                lblMensaje.setText("Usuario o contraseña incorrectos");
            } else if (!usuarioEncontrado.isActivo()) {
                mostrarAlerta(Alert.AlertType.ERROR, "El usuario está inactivo. Contacta al administrador");
                lblMensaje.setText("Usuario inactivo");
            } else {
                SessionContext.getInstancia().setUsuairoActual(usuarioEncontrado);

                String dashboard = RolPermisos.getDashboardPorRol(usuarioEncontrado.getRol());
                if (dashboard == null) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Rol no reconocido: " + usuarioEncontrado.getRol());
                    return;
                }

                try {
                    main.cambiarEscena(dashboard);
                } catch (IOException e) {
                mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
 
        }
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
    
    @FXML
    public void eventoRegistrarse(ActionEvent evento) {
        try {
            main.cambiarEscena("/org/lpv/view/GestionUsuariosView.fxml");
        } catch (Exception e) {
            System.err.println("Error al abrir el registro: " + e.getMessage());
            lblMensaje.setText("No fue posible abrir el registro.");
        }
    }
    
}
 

