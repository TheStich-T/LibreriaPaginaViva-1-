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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.MovimientoInventarioDAO;
import org.lpv.dao.ProveedorDAO;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.dao.impl.MovimientoInventarioDAOImpl;
import org.lpv.dao.impl.ProveedorDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.manager.SessionContext;
import org.lpv.model.Libros;
import org.lpv.model.MovimientoInventario;
import org.lpv.model.Proveedor;
import org.lpv.model.Usuario;
import org.lpv.system.main;

public class SalidaInventarioController implements Initializable {

    @FXML private ComboBox<Libros> cmbLibro;
    @FXML private ComboBox<String> cmbTipoSalida;
    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private TextField txtCantidad;
    @FXML private TextArea txtObservacion;
    @FXML private Label lblStockActual;
    @FXML private Label lblMensaje;
    @FXML private Button btnRegistrar;
    @FXML private TableView<MovimientoInventario> tblSalidas;
    @FXML private TableColumn<MovimientoInventario, String> colLibro;
    @FXML private TableColumn<MovimientoInventario, String> colTipoSalida;
    @FXML private TableColumn<MovimientoInventario, Integer> colCantidad;
    @FXML private TableColumn<MovimientoInventario, String> colNit;

    private LibrosDAO librosDAO;
    private MovimientoInventarioDAO movimientoDAO;
    private ProveedorDAO proveedorDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        librosDAO = new LibrosDAOImpl();
        movimientoDAO = new MovimientoInventarioDAOImpl();
        proveedorDAO = new ProveedorDAOImpl();
        lblMensaje.setText("");
        lblStockActual.setText("");

        cargarLibrosDisponibles();
        cargarTiposSalida();
        cargarProveedores();
        configurarTabla();
        cargarTablaSalidas();

        cmbLibro.setConverter(new StringConverter<Libros>() {
            @Override
            public String toString(Libros libro) {
                return libro == null ? "" : libro.getIsbn() + " - " + libro.getTitulo();
            }

            @Override
            public Libros fromString(String string) {
                return cmbLibro.getValue();
            }
        });

        cmbLibro.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                lblStockActual.setText("Stock actual: " + seleccionado.getStockActual()
                        + " (mínimo: " + seleccionado.getStockMinimo() + ")");
            } else {
                lblStockActual.setText("");
            }
        });
    }

    private void cargarLibrosDisponibles() {
        // solo libros activos pueden tener salidas de inventario
        ObservableList<Libros> libros = FXCollections.observableArrayList(
                librosDAO.listar().stream().filter(Libros::isActivo).toList());
        cmbLibro.setItems(libros);
    }

    private void cargarTiposSalida() {
        cmbTipoSalida.setItems(FXCollections.observableArrayList("MERMA", "TRASLADO", "DEVOLUCION"));
    }

    private void cargarProveedores() {
        ObservableList<Proveedor> proveedores = FXCollections.observableArrayList(proveedorDAO.listar());
        cmbProveedor.setItems(proveedores);
    }

    @FXML
    public void eventoRegistrar(ActionEvent evento) {
        try {
            Libros libroSeleccionado = cmbLibro.getValue();
            ValidarException.validarNulo(libroSeleccionado, "Selecciona un libro");

            String tipoSeleccionado = cmbTipoSalida.getValue();
            ValidarException.validarNulo(tipoSeleccionado, "Selecciona el tipo de salida");

            Proveedor proveedorSeleccionado = cmbProveedor.getValue();
            ValidarException.validarNulo(proveedorSeleccionado, "Selecciona el proveedor");

            ValidarException.validarNoVacio(txtCantidad.getText(), "cantidad");

            int cantidad;
            try {
                cantidad = Integer.parseInt(txtCantidad.getText().trim());
            } catch (NumberFormatException e) {
                throw new ValidarException("La cantidad debe ser un número entero válido");
            }
            if (cantidad <= 0) {
                throw new ValidarException("La cantidad debe ser mayor a 0");
            }

            if (cantidad > libroSeleccionado.getStockActual()) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "La cantidad de salida (" + cantidad + ") supera el stock actual ("
                        + libroSeleccionado.getStockActual() + ") del libro seleccionado.");
                return;
            }

            Usuario usuarioActual = SessionContext.getInstancia().getUsuairoActual();

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setIsbn(libroSeleccionado.getIsbn());
            movimiento.setTipoMovimiento(tipoSeleccionado);
            movimiento.setCantidad(cantidad);
            movimiento.setIdUsuario(usuarioActual != null ? usuarioActual.getId() : 0);
            movimiento.setObservacion(txtObservacion.getText() != null ? txtObservacion.getText().trim() : "");
            movimiento.setNitProveedor(proveedorSeleccionado.getNitProveedor());

            boolean registrado = movimientoDAO.registrarSalida(movimiento);

            if (registrado) {
                mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Salida registrada con éxito. Nuevo stock: "
                        + (libroSeleccionado.getStockActual() - cantidad));
                limpiarCampos();
                cargarLibrosDisponibles();
                cargarTablaSalidas();        
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar la salida");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
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

    private void limpiarCampos() {
        cmbLibro.setValue(null);
        cmbTipoSalida.setValue(null);
        cmbProveedor.setValue(null);
        txtCantidad.clear();
        txtObservacion.clear();
        lblStockActual.setText("");
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
    
    private void configurarTabla() {
    colLibro.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIsbn()));
    colTipoSalida.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTipoMovimiento()));
    colCantidad.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCantidad()));
    colNit.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNitProveedor()));
}

    private void cargarTablaSalidas() {
    ObservableList<MovimientoInventario> listaSalidas = FXCollections.observableArrayList(movimientoDAO.listar());
    tblSalidas.setItems(listaSalidas);
}
}