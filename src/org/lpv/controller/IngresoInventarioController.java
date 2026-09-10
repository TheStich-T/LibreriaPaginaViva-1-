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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.MovimientoInventarioDAO;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.dao.impl.MovimientoInventarioDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.manager.SessionContext;
import org.lpv.model.Libros;
import org.lpv.model.MovimientoInventario;
import org.lpv.model.Usuario;
import org.lpv.system.main;

public class IngresoInventarioController implements Initializable {

    @FXML private ComboBox<Libros> cmbLibro;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtNitProveedor;
    @FXML private TextArea txtObservacion;
    @FXML private Label lblStockActual;
    @FXML private Label lblMensaje;
    @FXML private Button btnRegistrar;

    private LibrosDAO librosDAO;
    private MovimientoInventarioDAO movimientoDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        librosDAO = new LibrosDAOImpl();
        movimientoDAO = new MovimientoInventarioDAOImpl();
        lblMensaje.setText("");
        lblStockActual.setText("");

        cargarLibrosDisponibles();

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
        // solo libros activos pueden recibir ingresos de inventario
        ObservableList<Libros> libros = FXCollections.observableArrayList(
                librosDAO.listar().stream().filter(Libros::isActivo).toList());
        cmbLibro.setItems(libros);
    }

    @FXML
    public void eventoRegistrar(ActionEvent evento) {
        try {
            Libros libroSeleccionado = cmbLibro.getValue();
            ValidarException.validarNulo(libroSeleccionado, "Selecciona un libro");
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

            Usuario usuarioActual = SessionContext.getInstancia().getUsuairoActual();

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setIsbn(libroSeleccionado.getIsbn());
            movimiento.setCantidad(cantidad);
            movimiento.setIdUsuario(usuarioActual != null ? usuarioActual.getId() : 0);
            movimiento.setObservacion(txtObservacion.getText() != null ? txtObservacion.getText().trim() : "");
            movimiento.setNitProveedor(txtNitProveedor.getText() != null ? txtNitProveedor.getText().trim() : null);

            boolean registrado = movimientoDAO.registrarIngreso(movimiento);

            if (registrado) {
                mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Ingreso registrado con éxito. Nuevo stock: "
                        + (libroSeleccionado.getStockActual() + cantidad));
                limpiarCampos();
                cargarLibrosDisponibles();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar el ingreso");
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
        txtCantidad.clear();
        txtNitProveedor.clear();
        txtObservacion.clear();
        lblStockActual.setText("");
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}
