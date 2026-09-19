package org.lpv.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import org.lpv.dao.VentaDAO;
import org.lpv.dao.impl.VentaDAOImpl;
import org.lpv.model.Usuario;
import org.lpv.model.Venta;
import org.lpv.manager.SessionContext;
import org.lpv.system.main;

public class ReportesVentasController implements Initializable {

    @FXML private ComboBox<String> cmbPeriodo;
    @FXML private DatePicker dpFecha;
    @FXML private TableView<Venta> tblReporteVentas;
    @FXML private TableColumn<Venta, Integer> colId;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, Double> colSubtotal;
    @FXML private TableColumn<Venta, Double> colDescuento;
    @FXML private TableColumn<Venta, Double> colTotal;
    @FXML private TableColumn<Venta, String> colEstado;
    @FXML private Label lblTotalVentas;
    @FXML private Label lblCantidadVentas;

    private VentaDAO ventaDAO;
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ventaDAO = new VentaDAOImpl();

        Usuario actual = SessionContext.getInstancia().getUsuairoActual();

        if (actual == null || !"admin".equalsIgnoreCase(actual.getRol())) {
            mostrarAlerta(Alert.AlertType.ERROR, "No tenés permiso para acceder a los reportes de ventas.");
            volverAlDashboard();
            return;
        }

        configurarComboBox();
        configurarTabla();
        dpFecha.setValue(LocalDate.now());
        limpiarResultados();
    }

    private void configurarComboBox() {
        cmbPeriodo.setItems(FXCollections.observableArrayList("Diario", "Semanal", "Mensual"));
        cmbPeriodo.setValue("Diario");
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colDescuento.setCellValueFactory(new PropertyValueFactory<>("descuento"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colFecha.setCellValueFactory(dato -> {
            if (dato.getValue().getFechaVenta() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(dato.getValue().getFechaVenta().format(formatoFecha));
        });
    }

    @FXML
    public void eventoGenerarReporte(ActionEvent evento) {
        try {
            if (dpFecha.getValue() == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Selecciona una fecha.");
                return;
            }

            String periodo = cmbPeriodo.getValue();

            if (periodo == null || periodo.isBlank()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Selecciona un período.");
                return;
            }

            LocalDate fecha = dpFecha.getValue();
            List<Venta> ventas;

            switch (periodo) {
                case "Diario":
                    ventas = ventaDAO.listarVentaPorDia(fecha);
                    break;

                case "Semanal":
                    ventas = ventaDAO.listarVentasPorSemana(fecha);
                    break;

                case "Mensual":
                    ventas = ventaDAO.listarVentasPorMes(fecha.getYear(), fecha.getMonthValue());
                    break;

                default:
                    mostrarAlerta(Alert.AlertType.WARNING, "Período no válido.");
                    return;
            }

            mostrarResultados(ventas);

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Ocurrió un error al generar el reporte: " + e.getMessage());
        }
    }

    private void mostrarResultados(List<Venta> ventas) {
        tblReporteVentas.setItems(FXCollections.observableArrayList(ventas));

        double total = 0;

        for (Venta venta : ventas) {
            total += venta.getTotal();
        }

        lblCantidadVentas.setText("Cantidad de ventas: " + ventas.size());
        lblTotalVentas.setText(String.format("Total de ventas: Q %.2f", total));
    }

    private void limpiarResultados() {
        tblReporteVentas.setItems(FXCollections.observableArrayList());
        lblCantidadVentas.setText("Cantidad de ventas: 0");
        lblTotalVentas.setText("Total de ventas: Q 0.00");
    }

    @FXML
    public void eventoVolver(ActionEvent evento) {
        volverAlDashboard();
    }

    private void volverAlDashboard() {
        try {
            main.volverAlDashboard();
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo volver al dashboard: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}