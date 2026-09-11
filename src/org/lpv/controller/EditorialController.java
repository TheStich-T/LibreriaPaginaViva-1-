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
import org.lpv.dao.EditorialDAO;
import org.lpv.dao.impl.EditorialDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.model.Editorial;
import org.lpv.system.main;

public class EditorialController implements Initializable {

    @FXML private TableView<Editorial> tblEditoriales;
    @FXML private TableColumn<Editorial, String> colNit;
    @FXML private TableColumn<Editorial, String> colNombreEditorial;
    @FXML private TableColumn<Editorial, String> colTelefonoEditorial;
    @FXML private TableColumn<Editorial, String> colDireccionEditoria;

    @FXML private TextField txtNit;
    @FXML private TextField txtNombreEditorial;
    @FXML private TextField txtTelefonoEditorial;
    @FXML private TextField txtDireccionEditoria;

    @FXML private Button btnAgregar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Label lblMensaje;

    private EditorialDAO editorialDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        editorialDAO = new EditorialDAOImpl();
        lblMensaje.setText("");

        cargarEditoriales();

        tblEditoriales.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtNit.setText(seleccionado.getNit());
                txtNit.setDisable(true); // el NIT no se edita una vez creado
                txtNombreEditorial.setText(seleccionado.getNombreEditorial());
                txtTelefonoEditorial.setText(seleccionado.getTelefonoEditorial());
                txtDireccionEditoria.setText(seleccionado.getDireccionEditoria());
            }
        });
    }

    private void cargarEditoriales() {
        ObservableList<Editorial> editoriales = FXCollections.observableArrayList(editorialDAO.listar());
        tblEditoriales.setItems(editoriales);
    }

    @FXML
    public void eventoAgregar(ActionEvent evento) {
        try {
            Editorial nueva = leerFormulario(true);

            boolean creada = editorialDAO.insertar(nueva);

            if (creada) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Editorial creada con éxito");
                limpiarCampos();
                cargarEditoriales();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear la editorial");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        try {
            Editorial seleccionada = tblEditoriales.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionada, "Selecciona una editorial de la tabla");

            Editorial datosActualizados = leerFormulario(false);
            datosActualizados.setNit(seleccionada.getNit());

            boolean actualizada = editorialDAO.actualizar(datosActualizados);

            if (actualizada) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Editorial actualizada con éxito");
                limpiarCampos();
                cargarEditoriales();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar la editorial");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoEliminar(ActionEvent evento) {
        try {
            Editorial seleccionada = tblEditoriales.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionada, "Selecciona una editorial de la tabla");

            boolean eliminada = editorialDAO.eliminar(seleccionada.getNit());

            if (eliminada) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Editorial eliminada con éxito");
                limpiarCampos();
                cargarEditoriales();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar la editorial");
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

    private Editorial leerFormulario(boolean validaNit) throws ValidarException {
        if (validaNit) {
            ValidarException.validarNoVacio(txtNit.getText(), "NIT");
        }
        ValidarException.validarNoVacio(txtNombreEditorial.getText(), "nombre de la editorial");
        ValidarException.validarNoVacio(txtTelefonoEditorial.getText(), "teléfono");
        ValidarException.validarNoVacio(txtDireccionEditoria.getText(), "dirección");

        Editorial editorial = new Editorial();
        if (validaNit) {
            editorial.setNit(txtNit.getText().trim());
        }
        editorial.setNombreEditorial(txtNombreEditorial.getText().trim());
        editorial.setTelefonoEditorial(txtTelefonoEditorial.getText().trim());
        editorial.setDireccionEditoria(txtDireccionEditoria.getText().trim());

        return editorial;
    }

    private void limpiarCampos() {
        txtNit.clear();
        txtNit.setDisable(false);
        txtNombreEditorial.clear();
        txtTelefonoEditorial.clear();
        txtDireccionEditoria.clear();
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}