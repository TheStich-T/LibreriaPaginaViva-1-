package org.lpv.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.lpv.dao.UsuarioDAO;
import org.lpv.dao.impl.UsuarioDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.model.Usuario;
import org.lpv.util.SecurityUtil;

public class GestionUsuariosController implements Initializable {

    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, Boolean> colActivo;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRol;

    @FXML private Button btnAgregar;
    @FXML private Button btnActualizar;
    @FXML private Button btnDesactivar;
    @FXML private Label lblMensaje;

    private UsuarioDAO usuarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usuarioDAO = new UsuarioDAOImpl();
        cmbRol.setItems(FXCollections.observableArrayList("admin", "cajero", "bodega"));
        lblMensaje.setText("");

        cargarUsuarios();

        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtUsername.setText(seleccionado.getUsername());
                txtUsername.setDisable(true); // el username no se edita una vez creado
                cmbRol.setValue(seleccionado.getRol());
                txtPassword.clear();
            }
        });
    }

    private void cargarUsuarios() {
        ObservableList<Usuario> usuarios = FXCollections.observableArrayList(usuarioDAO.listar());
        tblUsuarios.setItems(usuarios);
    }

    @FXML
    public void eventoAgregar(ActionEvent evento) {
        try {
            ValidarException.validarNoVacio(txtUsername.getText(), "usuario");
            ValidarException.validarNoVacio(txtPassword.getText(), "contraseña");
            ValidarException.validarNulo(cmbRol.getValue(), "Debe seleccionar un rol");
            ValidarException.validarLongitudMinima(txtPassword.getText(), 6,
                    "La contraseña debe tener al menos 6 caracteres");

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(txtUsername.getText().trim());
            nuevoUsuario.setPasswordHash(SecurityUtil.hashSHA256(txtPassword.getText()));
            nuevoUsuario.setRol(cmbRol.getValue());

            boolean creado = usuarioDAO.insertar(nuevoUsuario);

            if (creado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario creado con éxito");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear el usuario");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        try {
            Usuario seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un usuario de la tabla");
            ValidarException.validarNulo(cmbRol.getValue(), "Debe seleccionar un rol");

            seleccionado.setRol(cmbRol.getValue());
            boolean actualizado = usuarioDAO.actualizar(seleccionado);

            if (actualizado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario actualizado con éxito");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar el usuario");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoDesactivar(ActionEvent evento) {
        try {
            Usuario seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un usuario de la tabla");

            boolean desactivado = usuarioDAO.eliminar(seleccionado.getId());

            if (desactivado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario desactivado con éxito");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo desactivar el usuario");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }
    
    @FXML
        public void eventoActivar(ActionEvent evento) {
          String sql = "{call sp_activar_usuario(?)}";
                   
        try {
            Usuario seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un usuario de la tabla");

            boolean activado = usuarioDAO.activar(seleccionado.getId());

            if (activado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario activado con éxito");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo activar el usuario");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }
    

    private void limpiarCampos() {
        txtUsername.clear();
        txtUsername.setDisable(false);
        txtPassword.clear();
        cmbRol.setValue(null);
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}