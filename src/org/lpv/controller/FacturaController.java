package org.lpv.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.lpv.model.detalleVenta;
import org.lpv.model.Venta;
import org.lpv.model.Clientes;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class FacturaController implements Initializable {

    @FXML private TextField txtNoFactura;
    @FXML private TextField txtFecha;
    @FXML private TextField txtCui;
    @FXML private TextField txtCliente;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTotal;

    @FXML private TableView<detalleVenta> tblDetalleFactura;
    @FXML private TableColumn<detalleVenta, String> colLibro;
    @FXML private TableColumn<detalleVenta, Integer> colCompra;
    @FXML private TableColumn<detalleVenta, Double> colPrecio;
    @FXML private TableColumn<detalleVenta, Double> colSubtotal;

    private ObservableList<detalleVenta> listaDetalles;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLibro.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colCompra.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
    }

    public void cargarDatosFactura(Venta venta, Clientes clientes, ObservableList<detalleVenta> detalles) {
        txtNoFactura.setText(String.valueOf(venta.getIdVenta()));
        txtFecha.setText(venta.getFechaVenta() != null
                ? venta.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "");
        txtCui.setText(String.valueOf(clientes.getCui()));
        txtCliente.setText(clientes.getNombreCliente() + " " + clientes.getApellidoCliente());
        txtCorreo.setText(clientes.getCorreoElectronico());
        txtTotal.setText("Q " + String.format("%.2f", venta.getTotal()));

        listaDetalles = FXCollections.observableArrayList(detalles);
        tblDetalleFactura.setItems(listaDetalles);
    }

    @FXML
    public void eventoCerrar(ActionEvent evento) {
        Stage escenario = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        escenario.close();
    }
}