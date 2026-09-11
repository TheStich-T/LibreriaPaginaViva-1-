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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.model.Libros;
import org.lpv.system.main;
 
public class ConsultarStockController implements Initializable {
 
    @FXML private TableView<Libros> tblStock;
    @FXML private TableColumn<Libros, String> colIsbn;
    @FXML private TableColumn<Libros, String> colTitulo;
    @FXML private TableColumn<Libros, Integer> colStockActual;
    @FXML private TableColumn<Libros, Integer> colStockMinimo;
    @FXML private TableColumn<Libros, String> colEstado;
    @FXML private Label lblMensaje;
 
    private LibrosDAO librosDAO;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        librosDAO = new LibrosDAOImpl();
        lblMensaje.setText("");
        colEstado.setCellValueFactory(data -> {
            Libros libro = data.getValue();
            String estado = libro.getStockActual() <= libro.getStockMinimo() ? "BAJO" : "NORMAL";
            return new SimpleStringProperty(estado);
        });
 
        tblStock.setRowFactory(tv -> new TableRow<Libros>() {
            @Override
            protected void updateItem(Libros libro, boolean vacio) {
                super.updateItem(libro, vacio);
                if (libro == null || vacio) {
                    setStyle("");
                } else if (libro.getStockActual() <= libro.getStockMinimo()) {
                    setStyle("-fx-background-color: #fbe4e2;");
                } else {
                    setStyle("");
                }
            }
        });
 
        cargarStock();
    }
 
    private void cargarStock() {
        ObservableList<Libros> libros = FXCollections.observableArrayList(
                librosDAO.listar().stream().filter(Libros::isActivo).toList());
        tblStock.setItems(libros);
        lblMensaje.setText(libros.isEmpty() ? "No hay libros activos registrados." : "");
    }
 
    @FXML
    public void eventoActualizar(ActionEvent evento) {
        cargarStock();
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