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
import org.lpv.dao.CategoriaDAO;
import org.lpv.dao.impl.CategoriaDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.model.Categoria;
import org.lpv.system.main;

public class CategoriaController implements Initializable {

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colIdCategoria;
    @FXML private TableColumn<Categoria, String> colNombreCategoria;

    @FXML private TextField txtNombreCategoria;

    @FXML private Button btnAgregar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Label lblMensaje;

    private CategoriaDAO categoriaDAO;
    private Categoria seleccionadaActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        categoriaDAO = new CategoriaDAOImpl();
        lblMensaje.setText("");

        cargarCategorias();

        tblCategorias.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {
            if (seleccionada != null) {
                seleccionadaActual = seleccionada;
                txtNombreCategoria.setText(seleccionada.getNombreCategoria());
            }
        });
    }

    private void cargarCategorias() {
        ObservableList<Categoria> categorias = FXCollections.observableArrayList(categoriaDAO.listar());
        tblCategorias.setItems(categorias);
    }

    @FXML
    public void eventoAgregar(ActionEvent evento) {
        try {
            ValidarException.validarNoVacio(txtNombreCategoria.getText(), "nombre de la categoría");

            Categoria nueva = new Categoria();
            nueva.setNombreCategoria(txtNombreCategoria.getText().trim());

            boolean creada = categoriaDAO.insertar(nueva);

            if (creada) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Categoría creada con éxito");
                limpiarCampos();
                cargarCategorias();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear la categoría");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        try {
            ValidarException.validarNulo(seleccionadaActual, "Selecciona una categoría de la tabla");
            ValidarException.validarNoVacio(txtNombreCategoria.getText(), "nombre de la categoría");

            Categoria datosActualizados = new Categoria();
            datosActualizados.setIdCategoria(seleccionadaActual.getIdCategoria());
            datosActualizados.setNombreCategoria(txtNombreCategoria.getText().trim());

            boolean actualizada = categoriaDAO.actualizar(datosActualizados);

            if (actualizada) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Categoría actualizada con éxito");
                limpiarCampos();
                cargarCategorias();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar la categoría");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoEliminar(ActionEvent evento) {
        try {
            ValidarException.validarNulo(seleccionadaActual, "Selecciona una categoría de la tabla");

            boolean eliminada = categoriaDAO.eliminar(seleccionadaActual.getIdCategoria());

            if (eliminada) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Categoría eliminada con éxito");
                limpiarCampos();
                cargarCategorias();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar la categoría (puede estar en uso por algún libro)");
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

    private void limpiarCampos() {
        seleccionadaActual = null;
        txtNombreCategoria.clear();
        tblCategorias.getSelectionModel().clearSelection();
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}