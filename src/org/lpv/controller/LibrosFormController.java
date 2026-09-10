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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.model.Libros;
import org.lpv.system.main;

public class LibrosFormController implements Initializable {

    @FXML private TableView<Libros> tblLibros;
    @FXML private TableColumn<Libros, String> colIsbn;
    @FXML private TableColumn<Libros, String> colTitulo;
    @FXML private TableColumn<Libros, Double> colPrecio;
    @FXML private TableColumn<Libros, Integer> colStockActual;
    @FXML private TableColumn<Libros, Integer> colStockMinimo;
    @FXML private TableColumn<Libros, Boolean> colActivo;

    @FXML private TextField txtIsbn;
    @FXML private TextField txtTitulo;
    @FXML private DatePicker dpFechaPublicacion;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtIdCategoria;
    @FXML private TextField txtNitEditorial;
    @FXML private TextField txtStockMinimo;

    @FXML private Button btnAgregar;
    @FXML private Button btnActualizar;
    @FXML private Button btnDesactivar;
    @FXML private Button btnActivar;
    @FXML private Label lblMensaje;

    private LibrosDAO librosDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        librosDAO = new LibrosDAOImpl();
        lblMensaje.setText("");

        cargarLibros();

        tblLibros.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtIsbn.setText(seleccionado.getIsbn());
                txtIsbn.setDisable(true); // el ISBN no se edita una vez creado
                txtTitulo.setText(seleccionado.getTitulo());
                dpFechaPublicacion.setValue(seleccionado.getFechaPublicacion());
                txtPrecio.setText(String.valueOf(seleccionado.getPrecio()));
                txtIdCategoria.setText(String.valueOf(seleccionado.getIdCategoria()));
                txtNitEditorial.setText(seleccionado.getNitEditorial());
                txtStockMinimo.setText(String.valueOf(seleccionado.getStockMinimo()));
            }
        });
    }

    private void cargarLibros() {
        ObservableList<Libros> libros = FXCollections.observableArrayList(librosDAO.listar());
        tblLibros.setItems(libros);
    }

    @FXML
    public void eventoAgregar(ActionEvent evento) {
        try {
            Libros nuevoLibro = leerFormulario(true);

            boolean creado = librosDAO.insertar(nuevoLibro);

            if (creado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Libro creado con éxito");
                limpiarCampos();
                cargarLibros();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear el libro");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        try {
            Libros seleccionado = tblLibros.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un libro de la tabla");

            Libros datosActualizados = leerFormulario(false);
            datosActualizados.setIsbn(seleccionado.getIsbn());

            boolean actualizado = librosDAO.actualizar(datosActualizados);

            if (actualizado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Libro actualizado con éxito");
                limpiarCampos();
                cargarLibros();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar el libro");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoDesactivar(ActionEvent evento) {
        try {
            Libros seleccionado = tblLibros.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un libro de la tabla");

            boolean desactivado = librosDAO.eliminar(seleccionado.getIsbn());

            if (desactivado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Libro desactivado con éxito");
                limpiarCampos();
                cargarLibros();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo desactivar el libro");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoActivar(ActionEvent evento) {
        try {
            Libros seleccionado = tblLibros.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un libro de la tabla");

            boolean activado = librosDAO.activar(seleccionado.getIsbn());

            if (activado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Libro activado con éxito");
                limpiarCampos();
                cargarLibros();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo activar el libro");
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

    private Libros leerFormulario(boolean validaIsbn) throws ValidarException {

        if (validaIsbn) {
            ValidarException.validarNoVacio(txtIsbn.getText(), "ISBN");
        }
        ValidarException.validarNoVacio(txtTitulo.getText(), "título");
        ValidarException.validarNulo(dpFechaPublicacion.getValue(), "Debe seleccionar la fecha de publicación");
        ValidarException.validarNoVacio(txtPrecio.getText(), "precio");
        ValidarException.validarNoVacio(txtIdCategoria.getText(), "categoría");
        ValidarException.validarNoVacio(txtNitEditorial.getText(), "editorial");
        ValidarException.validarNoVacio(txtStockMinimo.getText(), "stock mínimo");

        double precio;
        int idCategoria;
        int stockMinimo;

        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidarException("El precio debe ser un número válido");
        }
        if (precio <= 0) {
            throw new ValidarException("El precio debe ser mayor a 0");
        }

        try {
            idCategoria = Integer.parseInt(txtIdCategoria.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidarException("La categoría debe ser un número válido (id de categoría)");
        }

        try {
            stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidarException("El stock mínimo debe ser un número válido");
        }
        if (stockMinimo < 0) {
            throw new ValidarException("El stock mínimo no puede ser negativo");
        }

        Libros libro = new Libros();
        if (validaIsbn) {
            libro.setIsbn(txtIsbn.getText().trim());
        }
        libro.setTitulo(txtTitulo.getText().trim());
        libro.setFechaPublicacion(dpFechaPublicacion.getValue());
        libro.setPrecio(precio);
        libro.setIdCategoria(idCategoria);
        libro.setNitEditorial(txtNitEditorial.getText().trim());
        libro.setStockActual(0); // un libro nuevo inicia sin stock; se carga con un ingreso (US-3.1)
        libro.setStockMinimo(stockMinimo);

        return libro;
    }

    private void limpiarCampos() {
        txtIsbn.clear();
        txtIsbn.setDisable(false);
        txtTitulo.clear();
        dpFechaPublicacion.setValue(null);
        txtPrecio.clear();
        txtIdCategoria.clear();
        txtNitEditorial.clear();
        txtStockMinimo.clear();
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}
