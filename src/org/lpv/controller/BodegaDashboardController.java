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
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.lpv.dao.AutorDAO;
import org.lpv.dao.EditorialDAO;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.impl.AutorDAOImpl;
import org.lpv.dao.impl.EditorialDAOImpl;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.manager.SessionContext;
import org.lpv.model.Autor;
import org.lpv.model.Editorial;
import org.lpv.model.Libros;
import org.lpv.model.Usuario;
import org.lpv.system.main;

public class BodegaDashboardController implements Initializable {

    @FXML private Label lblUsuarioActual;

    @FXML private Label lblContadorStockCritico;
    @FXML private TableView<Libros> tblStockCriticoDashboard;
    @FXML private TableColumn<Libros, String> colDashIsbn;
    @FXML private TableColumn<Libros, String> colDashTitulo;
    @FXML private TableColumn<Libros, Integer> colDashStockActual;
    @FXML private TableColumn<Libros, Integer> colDashStockMinimo;
    @FXML private Button btnAbrirFicha;
    @FXML private ListView<Editorial> lstEditorialesMini;
    @FXML private ListView<Autor> lstAutoresMini;

    private LibrosDAO librosDAO;
    private EditorialDAO editorialDAO;
    private AutorDAO autorDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Usuario actual = SessionContext.getInstancia().getUsuairoActual();

        if (actual == null || !"bodega".equalsIgnoreCase(actual.getRol())) {
            mostrarAlerta(Alert.AlertType.ERROR, "No tenés permiso para acceder a esta pantalla");
            volverAlLogin();
            return;
        }

        lblUsuarioActual.setText(actual.getUsername() + " (Bodega)");

        librosDAO = new LibrosDAOImpl();
        editorialDAO = new EditorialDAOImpl();
        autorDAO = new AutorDAOImpl();

        tblStockCriticoDashboard.setRowFactory(tv -> {
            TableRow<Libros> fila = new TableRow<Libros>() {
                @Override
                protected void updateItem(Libros libro, boolean vacio) {
                    super.updateItem(libro, vacio);
                    if (libro == null || vacio) {
                        setStyle("");
                    } else {
                        setStyle("-fx-background-color: #fbe4e2;");
                    }
                }
            };

            return fila;
        });

        cargarStockCritico();
        cargarEditorialesMini();
        cargarAutoresMini();
    }

    private void cargarStockCritico() {
        ObservableList<Libros> libros = FXCollections.observableArrayList(librosDAO.listarStockCritico());
        tblStockCriticoDashboard.setItems(libros);
        lblContadorStockCritico.setText(libros.size() + " libro(s) con stock igual o por debajo del mínimo");
    }

    private void cargarEditorialesMini() {
        if (lstEditorialesMini == null) return;
        ObservableList<Editorial> editoriales = FXCollections.observableArrayList(editorialDAO.listar());
        lstEditorialesMini.setItems(editoriales);
    }

    private void cargarAutoresMini() {
        if (lstAutoresMini == null) return;
        ObservableList<Autor> autores = FXCollections.observableArrayList(autorDAO.listar());
        lstAutoresMini.setItems(autores);
    }

    @FXML
    public void eventoAbrirFicha(ActionEvent evento) {
        Libros seleccionado = tblStockCriticoDashboard.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona un libro de la tabla de stock crítico");
            return;
        }
        abrirFichaDelLibro(seleccionado);
    }

    private void abrirFichaDelLibro(Libros libro) {
        LibrosFormController.isbnAAbrir = libro.getIsbn();
        cambiarEscena("/org/lpv/view/LibrosFormView.fxml");
    }

    @FXML
    public void eventoCerrarSesion(ActionEvent evento) {
        SessionContext.getInstancia().cerrarSesion();
        volverAlLogin();
    }

    @FXML
    public void eventoConsultarLibros(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/BuscarLibrosView.fxml");
    }

    @FXML
    public void eventoGestionarLibros(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/LibrosFormView.fxml");
    }

    @FXML
    public void eventoRegistrarIngreso(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/IngresoInventarioView.fxml");
    }

    @FXML
    public void eventoConsultarStockActual(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/ConsultarStockView.fxml");
    }

    @FXML
    public void eventoConsultarStockBajo(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/StockCriticoView.fxml");
    }

    @FXML
    public void eventoRegistrarSalida(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/SalidaInventarioView.fxml");
    }

    public void eventoGestionarEditoriales(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/EditorialView.fxml");
    }

    @FXML
    public void eventoGestionarAutores(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/AutorView.fxml");
    }

    private void cambiarEscena(String rutaFXML) {
        try {
            main.cambiarEscena(rutaFXML);
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir el módulo: " + e.getMessage());
        }
    }

    private void volverAlLogin() {
        try {
            main.cambiarEscena("/org/lpv/view/LoginView.fxml");
        } catch (IOException e) {
            System.err.println("Error al redirigir al login: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}