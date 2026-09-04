package org.lpv.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.lpv.model.detalleVenta; 
import org.lpv.model.Venta;
import org.lpv.model.Clientes;

import java.net.URL;
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
    @FXML private TableColumn<detalleVenta, String> colCui;
    @FXML private TableColumn<detalleVenta, Integer> colCompra;
    @FXML private TableColumn<detalleVenta, Double> colPrecio;
    @FXML private TableColumn<detalleVenta, Double> colSubtotal;

    private ObservableList<detalleVenta> listaDetalles;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLibro.setCellValueFactory(new PropertyValueFactory<>("nombreLibro"));
        colCui.setCellValueFactory(new PropertyValueFactory<>("cuiCliente"));
        colCompra.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
    }


    public void cargarDatosFactura(Venta venta, Clientes clientes, ObservableList<detalleVenta> detalles) {
        txtNoFactura.setText(String.valueOf(venta.getIdVenta()));
        txtFecha.setText(venta.getFechaVenta().toString());
        txtCui.setText(String.valueOf(clientes.getCui()));
        txtCliente.setText(clientes.getNombreCliente() + " " + clientes.getApellidoCliente());
        txtCorreo.setText(clientes.getCorreoElectronico());
        txtTotal.setText("Q " + String.format("%.2f", venta.getTotal()));

        listaDetalles = FXCollections.observableArrayList(detalles);
        tblDetalleFactura.setItems(listaDetalles);
    }
}