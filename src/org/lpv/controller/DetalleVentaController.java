package org.lpv.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.lpv.dao.ClienteDAO;
import org.lpv.dao.VentaDAO;
import org.lpv.dao.detalleVentaDAO;
import org.lpv.dao.impl.ClientesDAOImpl;
import org.lpv.dao.impl.VentaDAOImpl;
import org.lpv.dao.impl.detalleVentaDAOImpl;
import org.lpv.model.Clientes;
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
    private ClienteDAO clienteDAO;

    // Guardamos la última venta consultada para poder generar su factura con los valores correctos.
    private Venta ventaActual;
    private List<detalleVenta> detallesActuales;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ventaDAO = new VentaDAOImpl();
        detalleDAO = new detalleVentaDAOImpl();
        clienteDAO = new ClientesDAOImpl();
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
                ventaActual = null;
                detallesActuales = null;
                return;
            }

            lblVenta.setText("Venta #" + venta.getIdVenta() + " - " + venta.getEstado());
            lblTotal.setText(String.format("Q %.2f", venta.getTotal()));

            List<detalleVenta> detalles = detalleDAO.listarPorVenta(idVenta);
            tblDetalles.setItems(FXCollections.observableArrayList(detalles));

            // Guardamos la venta y sus detalles ya consultados para usarlos al generar la factura.
            ventaActual = venta;
            detallesActuales = detalles;
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

    @FXML
    public void eventoFactura(ActionEvent evento) {
        if (ventaActual == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Primero busca una venta para poder ver su factura.");
            return;
        }

        Clientes cliente = clienteDAO.buscar(ventaActual.getCuiCliente());
        if (cliente == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se encontró el cliente asociado a esta venta.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(main.class.getResource("/org/lpv/view/FacturaView.fxml"));
            Parent raiz = loader.load();
            FacturaController controller = loader.getController();
            controller.cargarDatosFactura(ventaActual, cliente, FXCollections.observableArrayList(detallesActuales));

            Stage ventanaFactura = new Stage();
            ventanaFactura.setTitle("Factura - Venta #" + ventaActual.getIdVenta());
            ventanaFactura.setScene(new Scene(raiz));

            // La factura se abre como ventana modal ENCIMA de Detalle de Venta, sin reemplazar
            // la escena de la ventana principal. Así, al cerrarla, Detalle de Venta sigue ahí.
            Stage ventanaActual = (Stage) ((Node) evento.getSource()).getScene().getWindow();
            ventanaFactura.initOwner(ventanaActual);
            ventanaFactura.initModality(Modality.WINDOW_MODAL);
            ventanaFactura.showAndWait();

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la factura: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        new Alert(tipo, mensaje, ButtonType.OK).show();
    }
}