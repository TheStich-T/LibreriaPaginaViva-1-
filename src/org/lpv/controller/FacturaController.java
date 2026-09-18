package org.lpv.controller;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.lpv.model.detalleVenta;
import org.lpv.model.Venta;
import org.lpv.model.Clientes;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import org.lpv.system.main;

public class FacturaController implements Initializable {

    @FXML private Label lblNoFactura;
    @FXML private Label lblFecha;
    @FXML private Label lblCui;
    @FXML private Label lblCliente;
    @FXML private Label lblCorreo;
    @FXML private Label lblDescuento;
    @FXML private Label lblTotal;

    @FXML private TableView<detalleVenta> tblDetalleFactura;
    @FXML private TableColumn<detalleVenta, String> colLibro;
    @FXML private TableColumn<detalleVenta, Integer> colCompra;
    @FXML private TableColumn<detalleVenta, Double> colPrecio;
    @FXML private TableColumn<detalleVenta, Double> colSubtotal;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLibro.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colCompra.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
    }

    public void cargarDatosFactura(Venta venta, Clientes clientes, ObservableList<detalleVenta> detalles) {

        lblNoFactura.setText(String.valueOf(venta.getIdVenta()));

        lblFecha.setText(venta.getFechaVenta() != null ? venta.getFechaVenta().format(FORMATO_FECHA): ""
        );

        lblCui.setText(String.valueOf(clientes.getCui()));
        lblCliente.setText(clientes.getNombreCliente() + " " + clientes.getApellidoCliente());
        lblCorreo.setText(clientes.getCorreoElectronico());
        lblDescuento.setText("Q " + String.format("%.2f", venta.getDescuento()));
        lblTotal.setText("Q " + String.format("%.2f", venta.getTotal()));

        tblDetalleFactura.setItems(FXCollections.observableArrayList(detalles));
    }

//    @FXML
//    public void eventoCerrar(ActionEvent evento) {
//        Stage escenario = (Stage) ((Node) evento.getSource()).getScene().getWindow();
//        escenario.close();
//    }
     @FXML
    public void eventoVolver(ActionEvent evento) {
        try {
            main.volverAlDashboard();
        } catch (IOException e) {
            System.err.println("Error al volver al dashboard: " + e.getMessage());
        }
    }
    
}