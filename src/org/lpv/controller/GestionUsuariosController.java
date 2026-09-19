package org.lpv.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.cell.PropertyValueFactory;
import org.lpv.dao.UsuarioDAO;
import org.lpv.dao.impl.UsuarioDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.model.Usuario;
import org.lpv.system.main;
import org.lpv.util.SecurityUtil;

public class GestionUsuariosController implements Initializable {

    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombreCompleto;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, Boolean> colActivo;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRol;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtCorreo;

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

        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // nombre completo: concatenamos nombre y apellido dinámicamente
        colNombreCompleto.setCellValueFactory(dato -> {
            String nombre   = dato.getValue().getNombre()   != null ? dato.getValue().getNombre()   : "";
            String apellido = dato.getValue().getApellido() != null ? dato.getValue().getApellido() : "";
            return new SimpleStringProperty((nombre + " " + apellido).trim());
        });

        cargarUsuarios();

        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtUsername.setText(seleccionado.getUsername());
                txtUsername.setDisable(true); // el username no se edita una vez creado
                cmbRol.setValue(seleccionado.getRol());
                txtNombre.setText(seleccionado.getNombre()   != null ? seleccionado.getNombre()   : "");
                txtApellido.setText(seleccionado.getApellido() != null ? seleccionado.getApellido() : "");
                txtCorreo.setText(seleccionado.getCorreo()   != null ? seleccionado.getCorreo()   : "");
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
            // --- Validaciones ---
            ValidarException.validarNoVacio(txtUsername.getText(), "usuario");
            ValidarException.validarNoVacio(txtPassword.getText(), "contraseña");
            ValidarException.validarNulo(cmbRol.getValue(), "Debe seleccionar un rol");
            ValidarException.validarNoVacio(txtNombre.getText(),   "nombre");
            ValidarException.validarNoVacio(txtApellido.getText(), "apellido");
            ValidarException.validarNoVacio(txtCorreo.getText(),   "correo");

            // Sin espacios en el username
            if (txtUsername.getText().contains(" ")) {
                throw new ValidarException("El nombre de usuario no puede contener espacios.");
            }

            // Mínimo 8 caracteres en contraseña
            ValidarException.validarLongitudMinima(txtPassword.getText(), 8,
                    "La contraseña debe tener al menos 8 caracteres.");

            // --- Construir y guardar ---
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(txtUsername.getText().trim());
            nuevoUsuario.setPasswordHash(SecurityUtil.hashSHA256(txtPassword.getText()));
            nuevoUsuario.setRol(cmbRol.getValue());
            nuevoUsuario.setNombre(txtNombre.getText().trim());
            nuevoUsuario.setApellido(txtApellido.getText().trim());
            nuevoUsuario.setCorreo(txtCorreo.getText().trim());

            boolean creado = usuarioDAO.insertar(nuevoUsuario);

            if (creado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario creado con éxito.");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear el usuario. Es posible que el nombre de usuario ya exista.");
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
            ValidarException.validarNulo(seleccionado, "Selecciona un usuario de la tabla.");
            ValidarException.validarNulo(cmbRol.getValue(), "Debe seleccionar un rol.");
            ValidarException.validarNoVacio(txtNombre.getText(),   "nombre");
            ValidarException.validarNoVacio(txtApellido.getText(), "apellido");
            ValidarException.validarNoVacio(txtCorreo.getText(),   "correo");

            seleccionado.setRol(cmbRol.getValue());
            seleccionado.setNombre(txtNombre.getText().trim());
            seleccionado.setApellido(txtApellido.getText().trim());
            seleccionado.setCorreo(txtCorreo.getText().trim());
            boolean actualizado = usuarioDAO.actualizar(seleccionado);

            if (actualizado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario actualizado con éxito.");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar el usuario.");
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
            ValidarException.validarNulo(seleccionado, "Selecciona un usuario de la tabla.");

            boolean desactivado = usuarioDAO.eliminar(seleccionado.getId());

            if (desactivado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario desactivado con éxito.");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo desactivar el usuario.");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoActivar(ActionEvent evento) {
        try {
            Usuario seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un usuario de la tabla.");

            boolean activado = usuarioDAO.activar(seleccionado.getId());

            if (activado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario activado con éxito.");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo activar el usuario.");
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
        txtNombre.clear();
        txtApellido.clear();
        txtCorreo.clear();
        lblMensaje.setText("");
        tblUsuarios.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        new Alert(tipo, mensaje, ButtonType.OK).show();
    }

    @FXML
    public void eventoVolver(ActionEvent evento) {
        try {
            main.volverAlDashboard();
        } catch (IOException e) {
            System.err.println("Error al volver al dashboard: " + e.getMessage());
        }
    }
}