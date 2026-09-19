package org.lpv.controller;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import org.lpv.dao.VentaDAO;
import org.lpv.dao.impl.VentaDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.manager.SessionContext;
import org.lpv.model.Usuario;
import org.lpv.model.Venta;
import org.lpv.system.main;

public class ListaVentasController implements Initializable {

    @FXML private TableView<Venta> tblVentas;
    @FXML private TableColumn<Venta, Integer> colId;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, Double> colSubtotal;
    @FXML private TableColumn<Venta, Double> colDescuento;
    @FXML private TableColumn<Venta, Double> colTotal;
    @FXML private TableColumn<Venta, String> colEstado;

    @FXML private TextField txtMotivoAnulacion;

    private VentaDAO ventaDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ventaDAO = new VentaDAOImpl();
        colId.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colDescuento.setCellValueFactory(new PropertyValueFactory<>("descuento"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFecha.setCellValueFactory(dato -> new SimpleStringProperty(
                dato.getValue().getFechaVenta() == null
                        ? ""
                        : dato.getValue().getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        cargarVentas();
    }

    private void cargarVentas() {
        List<Venta> ventas = ventaDAO.listar();
        tblVentas.setItems(FXCollections.observableArrayList(ventas));
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        cargarVentas();
    }

    @FXML
    public void eventoAnularVenta(ActionEvent evento) {
        try {
            Venta seleccionada = tblVentas.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionada, "Selecciona una venta de la tabla");

            if (!"COMPLETADA".equalsIgnoreCase(seleccionada.getEstado())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Esta venta ya fue anulada o devuelta anteriormente");
                return;
            }

            ValidarException.validarNoVacio(txtMotivoAnulacion.getText(), "motivo de la anulación");

            Usuario actual = SessionContext.getInstancia().getUsuairoActual();
            int idUsuario = (actual != null) ? actual.getId() : 0;

            boolean devuelta = ventaDAO.devolverVenta(seleccionada.getIdVenta(), idUsuario, txtMotivoAnulacion.getText().trim());

            if (devuelta) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Venta anulada y stock restaurado con éxito");
                txtMotivoAnulacion.clear();
                cargarVentas();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo anular la venta");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
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
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}