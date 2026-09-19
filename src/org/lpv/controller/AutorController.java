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
import org.lpv.dao.AutorDAO;
import org.lpv.dao.impl.AutorDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.model.Autor;
import org.lpv.system.main;

public class AutorController implements Initializable {

    @FXML private TableView<Autor> tblAutores;
    @FXML private TableColumn<Autor, Integer> colIdAutor;
    @FXML private TableColumn<Autor, String> colNombreAutor;
    @FXML private TableColumn<Autor, String> colApellidoAutor;
    @FXML private TableColumn<Autor, String> colNacionalidad;
    @FXML private TableColumn<Autor, String> colBiografia;

    @FXML private TextField txtNombreAutor;
    @FXML private TextField txtApellidoAutor;
    @FXML private TextField txtNacionalidad;
    @FXML private TextField txtBiografia;

    @FXML private Button btnAgregar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Label lblMensaje;

    private AutorDAO autorDAO;
    private Autor seleccionadoActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        autorDAO = new AutorDAOImpl();
        lblMensaje.setText("");

        cargarAutores();

        tblAutores.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                seleccionadoActual = seleccionado;
                txtNombreAutor.setText(seleccionado.getNombreAutor());
                txtApellidoAutor.setText(seleccionado.getApellidoAutor());
                txtNacionalidad.setText(seleccionado.getNacionalidad());
                txtBiografia.setText(seleccionado.getBiografia());
            }
        });
    }

    private void cargarAutores() {
        ObservableList<Autor> autores = FXCollections.observableArrayList(autorDAO.listar());
        tblAutores.setItems(autores);
    }

    @FXML
    public void eventoAgregar(ActionEvent evento) {
        try {
            Autor nuevo = leerFormulario();

            boolean creado = autorDAO.insertar(nuevo);

            if (creado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Autor creado con éxito");
                limpiarCampos();
                cargarAutores();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear el autor");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        try {
            ValidarException.validarNulo(seleccionadoActual, "Selecciona un autor de la tabla");

            Autor datosActualizados = leerFormulario();
            datosActualizados.setIdAutor(seleccionadoActual.getIdAutor());

            boolean actualizado = autorDAO.actualizar(datosActualizados);

            if (actualizado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Autor actualizado con éxito");
                limpiarCampos();
                cargarAutores();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar el autor");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoEliminar(ActionEvent evento) {
        try {
            ValidarException.validarNulo(seleccionadoActual, "Selecciona un autor de la tabla");

            boolean eliminado = autorDAO.eliminar(seleccionadoActual.getIdAutor());

            if (eliminado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Autor eliminado con éxito");
                limpiarCampos();
                cargarAutores();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar el autor (puede tener libros asociados)");
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

    private Autor leerFormulario() throws ValidarException {
        ValidarException.validarNoVacio(txtNombreAutor.getText(), "nombre del autor");
        ValidarException.validarNoVacio(txtApellidoAutor.getText(), "apellido del autor");

        Autor autor = new Autor();
        autor.setNombreAutor(txtNombreAutor.getText().trim());
        autor.setApellidoAutor(txtApellidoAutor.getText().trim());
        autor.setNacionalidad(txtNacionalidad.getText() == null ? "" : txtNacionalidad.getText().trim());
        autor.setBiografia(txtBiografia.getText() == null ? "" : txtBiografia.getText().trim());

        return autor;
    }

    private void limpiarCampos() {
        seleccionadoActual = null;
        txtNombreAutor.clear();
        txtApellidoAutor.clear();
        txtNacionalidad.clear();
        txtBiografia.clear();
        tblAutores.getSelectionModel().clearSelection();
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}