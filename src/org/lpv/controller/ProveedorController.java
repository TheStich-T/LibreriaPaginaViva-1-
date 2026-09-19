package org.lpv.controller;

import java.io.IOException;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.lpv.dao.ProveedorDAO;
import org.lpv.dao.impl.ProveedorDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.model.Proveedor;
import org.lpv.system.main;

public class ProveedorController implements Initializable {

    @FXML private TableView<Proveedor> tblProveedores;
    @FXML private TableColumn<Proveedor, String> colNitProveedor;
    @FXML private TableColumn<Proveedor, String> colNombreProveedor;
    @FXML private TableColumn<Proveedor, String> colTelefonoProveedor;
    @FXML private TableColumn<Proveedor, String> colDireccionProveedor;

    @FXML private TextField txtNitProveedor;
    @FXML private TextField txtNombreProveedor;
    @FXML private TextField txtTelefonoProveedor;
    @FXML private TextField txtDireccionProveedor;

    @FXML private Button btnAgregar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Label lblMensaje;

    private ProveedorDAO proveedorDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        proveedorDAO = new ProveedorDAOImpl();
        lblMensaje.setText("");

        cargarProveedores();

        tblProveedores.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtNitProveedor.setText(seleccionado.getNitProveedor());
                txtNitProveedor.setDisable(true); // el NIT no se edita una vez creado
                txtNombreProveedor.setText(seleccionado.getNombreProveedor());
                txtTelefonoProveedor.setText(seleccionado.getTelefonoProveedor());
                txtDireccionProveedor.setText(seleccionado.getDireccionProveedor());
            }
        });
    }

    private void cargarProveedores() {
        ObservableList<Proveedor> proveedores = FXCollections.observableArrayList(proveedorDAO.listar());
        tblProveedores.setItems(proveedores);
    }

    @FXML
    public void eventoAgregar(ActionEvent evento) {
        try {
            Proveedor nuevo = leerFormulario(true);

            boolean creado = proveedorDAO.insertar(nuevo);

            if (creado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Proveedor creado con éxito");
                limpiarCampos();
                cargarProveedores();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear el proveedor");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        try {
            Proveedor seleccionado = tblProveedores.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un proveedor de la tabla");

            Proveedor datosActualizados = leerFormulario(false);
            datosActualizados.setNitProveedor(seleccionado.getNitProveedor());

            boolean actualizado = proveedorDAO.actualizar(datosActualizados);

            if (actualizado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Proveedor actualizado con éxito");
                limpiarCampos();
                cargarProveedores();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar el proveedor");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoEliminar(ActionEvent evento) {
        try {
            Proveedor seleccionado = tblProveedores.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un proveedor de la tabla");

            boolean eliminado = proveedorDAO.eliminar(seleccionado.getNitProveedor());

            if (eliminado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Proveedor eliminado con éxito");
                limpiarCampos();
                cargarProveedores();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar el proveedor");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoVolver(ActionEvent evento) {
        try {
            main.volverAlDashboard();
        } catch (IOException e) {
            System.err.println("Error al volver al dashboard: " + e.getMessage());
        }
    }

    private Proveedor leerFormulario(boolean validaNit) throws ValidarException {
        if (validaNit) {
            ValidarException.validarNoVacio(txtNitProveedor.getText(), "NIT");
        }
        ValidarException.validarNoVacio(txtNombreProveedor.getText(), "nombre del proveedor");
        ValidarException.validarNoVacio(txtTelefonoProveedor.getText(), "teléfono");
        ValidarException.validarNoVacio(txtDireccionProveedor.getText(), "dirección");

        Proveedor proveedor = new Proveedor();
        if (validaNit) {
            proveedor.setNitProveedor(txtNitProveedor.getText().trim());
        }
        proveedor.setNombreProveedor(txtNombreProveedor.getText().trim());
        proveedor.setTelefonoProveedor(txtTelefonoProveedor.getText().trim());
        proveedor.setDireccionProveedor(txtDireccionProveedor.getText().trim());

        return proveedor;
    }

    private void limpiarCampos() {
        txtNitProveedor.clear();
        txtNitProveedor.setDisable(false);
        txtNombreProveedor.clear();
        txtTelefonoProveedor.clear();
        txtDireccionProveedor.clear();
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}