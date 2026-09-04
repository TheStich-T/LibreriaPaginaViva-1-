package org.lpv.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.lpv.dao.VentaDAO;
import org.lpv.dao.detalleVentaDAO;
import org.lpv.dao.impl.VentaDAOImpl;
import org.lpv.dao.impl.detalleVentaDAOImpl;
import org.lpv.model.Venta;
import org.lpv.model.detalleVenta;
import org.lpv.exception.ValidarException;
import org.lpv.system.main;

public class DetalleVentaController implements Initializable {

    @FXML private TextField txtIdVenta;
    @FXML private Label lblVenta;
    @FXML private Label lblTotal;
    @FXML private TableView<detalleVenta> tblDetalles;
    @FXML private TableColumn<detalleVenta, String> colIsbn;
    @FXML private TableColumn<detalleVenta, Integer> colCantidad;
    @FXML private TableColumn<detalleVenta, Double> colPrecio;
    @FXML private TableColumn<detalleVenta, Double> colSubtotal;

    private VentaDAO ventaDAO;
    private detalleVentaDAO detalleDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ventaDAO = new VentaDAOImpl();
        detalleDAO = new detalleVentaDAOImpl();
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        lblVenta.setText("");
        lblTotal.setText("Q 0.00");
    }

    @FXML
    public void eventoBuscar(ActionEvent evento) {
        try {
            ValidarException.validarNoVacio(txtIdVenta.getText(), "número de venta");
            int idVenta = Integer.parseInt(txtIdVenta.getText().trim());
            Venta venta = ventaDAO.buscar(idVenta);
            if (venta == null) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "No se encontró la venta indicada.");
                tblDetalles.setItems(FXCollections.observableArrayList());
                lblVenta.setText("");
                lblTotal.setText("Q 0.00");
                return;
            }

            lblVenta.setText("Venta #" + venta.getIdVenta() + " - " + venta.getEstado());
            lblTotal.setText(String.format("Q %.2f", venta.getTotal()));
            tblDetalles.setItems(FXCollections.observableArrayList(detalleDAO.listarPorVenta(idVenta)));
        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "El número de venta debe ser un entero válido.");
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

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        new Alert(tipo, mensaje, ButtonType.OK).show();
    }
}

