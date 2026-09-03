package org.lpv.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.model.Libros;

public class BuscarLibrosController implements Initializable {

    @FXML private TableView<Libros> tblLibros;
    @FXML private TableColumn<Libros, String> colIsbn;
    @FXML private TableColumn<Libros, String> colTitulo;
    @FXML private TableColumn<Libros, Double> colPrecio;
    @FXML private TableColumn<Libros, Integer> colStock;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbCriterio;
    @FXML private Button btnBuscar;
    @FXML private Label lblMensaje;

    private LibrosDAO librosDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        librosDAO = new LibrosDAOImpl();
        cmbCriterio.setItems(FXCollections.observableArrayList("ISBN", "Título", "Autor"));
        cmbCriterio.setValue("Título");
        lblMensaje.setText("");
    }

    @FXML
    public void eventoBuscar(ActionEvent evento) {
        try {
            ValidarException.validarNoVacio(txtBuscar.getText(), "criterio de búsqueda");

            String criterio = cmbCriterio.getValue();
            String valor = txtBuscar.getText().trim();
            List<Libros> resultados;

            switch (criterio) {
                case "ISBN":
                    Libros libro = librosDAO.buscar(valor);
                    resultados = libro != null ? List.of(libro) : List.of();
                    break;
                case "Autor":
                    resultados = librosDAO.buscarPorAutor(valor);
                    break;
                default:
                    resultados = librosDAO.buscarPorTitulo(valor);
                    break;
            }

            tblLibros.setItems(FXCollections.observableArrayList(resultados));
            lblMensaje.setText(resultados.isEmpty() ? "No se encontraron libros con ese criterio." : "");

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}