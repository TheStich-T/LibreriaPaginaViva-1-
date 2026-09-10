package org.lpv.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.model.Libros;
import org.lpv.system.main;

public class BuscarLibrosController implements Initializable {

    @FXML private TableView<Libros> tblLibros;
    @FXML private TableColumn<Libros, String> colIsbn;
    @FXML private TableColumn<Libros, String> colTitulo;
    @FXML private TableColumn<Libros, Double> colPrecio;
    @FXML private TableColumn<Libros, Integer> colStock;

    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Label lblMensaje;

    private LibrosDAO librosDAO;
    private ObservableList<Libros> librosData;
    private FilteredList<Libros> librosFiltrados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        librosDAO = new LibrosDAOImpl();
        lblMensaje.setText("");

        cargarLibros();

        // filtra en memoria cada vez que el usuario escribe, sin volver a consultar la BD
        txtBuscar.textProperty().addListener((obs, textoAnterior, textoNuevo) -> filtrarLibros(textoNuevo));
    }

    private void cargarLibros() {
        librosData = FXCollections.observableArrayList(librosDAO.listar());
        librosFiltrados = new FilteredList<>(librosData, libro -> true);
        tblLibros.setItems(librosFiltrados);
        lblMensaje.setText(librosData.isEmpty() ? "No hay libros registrados." : "");
    }

    private void filtrarLibros(String texto) {
        if (librosFiltrados == null) {
            return;
        }
        if (texto == null || texto.isBlank()) {
            librosFiltrados.setPredicate(libro -> true);
            lblMensaje.setText("");
            return;
        }
        String textoBusqueda = texto.trim().toLowerCase();
        librosFiltrados.setPredicate(libro ->
                libro.getTitulo().toLowerCase().contains(textoBusqueda)
                || libro.getIsbn().toLowerCase().contains(textoBusqueda));

        lblMensaje.setText(librosFiltrados.isEmpty() ? "No se encontraron libros con ese criterio." : "");
    }

    @FXML
    public void eventoBuscar(ActionEvent evento) {
        // vuelve a consultar la BD (por si se agregaron libros nuevos)
        // y reaplica el filtro que el usuario ya tenía escrito
        cargarLibros();
        filtrarLibros(txtBuscar.getText());
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