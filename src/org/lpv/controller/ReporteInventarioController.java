package org.lpv.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.lpv.dao.ReporteInventarioDAO;
import org.lpv.dao.impl.ReporteInventarioDAOImpl;
import org.lpv.manager.SessionContext;
import org.lpv.model.LibroMasVendido;
import org.lpv.model.StockValorizado;
import org.lpv.model.Usuario;
import org.lpv.system.main;

public class ReporteInventarioController implements Initializable {

    @FXML private ComboBox<String> cmbTopN;
    @FXML private TableView<LibroMasVendido> tblRanking;
    @FXML private TableColumn<LibroMasVendido, String> colRankIsbn;
    @FXML private TableColumn<LibroMasVendido, String> colRankTitulo;
    @FXML private TableColumn<LibroMasVendido, Integer> colRankUnidades;
    @FXML private Label lblCantidadRanking;

    @FXML private TableView<StockValorizado> tblStockValorizado;
    @FXML private TableColumn<StockValorizado, String> colValIsbn;
    @FXML private TableColumn<StockValorizado, String> colValTitulo;
    @FXML private TableColumn<StockValorizado, Integer> colValStockActual;
    @FXML private TableColumn<StockValorizado, Double> colValPrecio;
    @FXML private TableColumn<StockValorizado, Double> colValValorInventario;
    @FXML private Label lblValorTotal;

    private ReporteInventarioDAO reporteInventarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Usuario actual = SessionContext.getInstancia().getUsuairoActual();

        if (actual == null || !"admin".equalsIgnoreCase(actual.getRol())) {
            mostrarAlerta(Alert.AlertType.ERROR, "No tenés permiso para acceder a los reportes de inventario.");
            volverAlDashboard();
            return;
        }

        reporteInventarioDAO = new ReporteInventarioDAOImpl();

        configurarComboBox();
        configurarTablas();
        limpiarResultados();
    }

    private void configurarComboBox() {
        cmbTopN.setItems(FXCollections.observableArrayList("5", "10", "20"));
        cmbTopN.setValue("10");
    }

    private void configurarTablas() {
        colRankIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colRankTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colRankUnidades.setCellValueFactory(new PropertyValueFactory<>("unidadesVendidas"));

        colValIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colValTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colValStockActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colValPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colValValorInventario.setCellValueFactory(new PropertyValueFactory<>("valorInventario"));
    }

    @FXML
    public void eventoGenerarRanking(ActionEvent evento) {
        try {
            String seleccion = cmbTopN.getValue();

            if (seleccion == null || seleccion.isBlank()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Selecciona cuántos libros quieres ver en el ranking.");
                return;
            }

            int limite = Integer.parseInt(seleccion);
            List<LibroMasVendido> ranking = reporteInventarioDAO.listarLibrosMasVendidos(limite);

            tblRanking.setItems(FXCollections.observableArrayList(ranking));
            lblCantidadRanking.setText("Libros en el ranking: " + ranking.size());

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Ocurrió un error al generar el ranking: " + e.getMessage());
        }
    }

    @FXML
    public void eventoCargarStockValorizado(ActionEvent evento) {
        try {
            List<StockValorizado> stockValorizado = reporteInventarioDAO.listarStockValorizado();
            tblStockValorizado.setItems(FXCollections.observableArrayList(stockValorizado));

            double valorTotal = 0;
            for (StockValorizado item : stockValorizado) {
                valorTotal += item.getValorInventario();
            }

            lblValorTotal.setText(String.format("Valor total del inventario: Q %,.2f", valorTotal));

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Ocurrió un error al cargar el stock valorizado: " + e.getMessage());
        }
    }

    private void limpiarResultados() {
        tblRanking.setItems(FXCollections.observableArrayList());
        lblCantidadRanking.setText("Libros en el ranking: 0");

        tblStockValorizado.setItems(FXCollections.observableArrayList());
        lblValorTotal.setText("Valor total del inventario: Q 0.00");
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