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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.manager.RolPermisos;
import org.lpv.manager.SessionContext;
import org.lpv.model.Libros;
import org.lpv.model.Usuario;
import org.lpv.system.main;

public class StockCriticoController implements Initializable {

    @FXML private TableView<Libros> tblStockCritico;
    @FXML private TableColumn<Libros, String> colIsbn;
    @FXML private TableColumn<Libros, String> colTitulo;
    @FXML private TableColumn<Libros, Integer> colStockActual;
    @FXML private TableColumn<Libros, Integer> colStockMinimo;
    @FXML private Label lblContador;
    @FXML private Label lblMensaje;

    private LibrosDAO librosDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Usuario actual = SessionContext.getInstancia().getUsuairoActual();
        if (actual == null || !RolPermisos.tienePermiso(actual.getRol(), RolPermisos.STOCK_CONSULTAR)) {
            mostrarAlerta(Alert.AlertType.ERROR, "No tenés permiso para acceder a esta pantalla");
            volverAlLogin();
            return;
        }

        librosDAO = new LibrosDAOImpl();
        lblMensaje.setText("");

        // T3.4.6/T3.4.5 (visual) — resalta en rojo cada fila de libro con stock crítico
        tblStockCritico.setRowFactory(tv -> new TableRow<Libros>() {
            @Override
            protected void updateItem(Libros libro, boolean vacio) {
                super.updateItem(libro, vacio);
                if (libro == null || vacio) {
                    setStyle("");
                } else {
                    setStyle("-fx-background-color: #fbe4e2;");
                }
            }
        });

        cargarStockCritico();
    }

    private void cargarStockCritico() {
        ObservableList<Libros> libros = FXCollections.observableArrayList(librosDAO.listarStockCritico());
        tblStockCritico.setItems(libros);
        lblContador.setText(libros.size() + " libro(s) con stock igual o por debajo del mínimo");
        lblMensaje.setText(libros.isEmpty() ? "No hay libros con stock crítico en este momento." : "");
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        cargarStockCritico();
    }

    @FXML
    public void eventoVolver(ActionEvent evento) {
        try {
            main.volverAlDashboard();
        } catch (IOException e) {
            System.err.println("Error al volver al dashboard: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }

    private void volverAlLogin() {
        try {
            main.cambiarEscena("/org/lpv/view/LoginView.fxml");
        } catch (IOException e) {
            System.err.println("Error al redirigir al login: " + e.getMessage());
        }
    }
}