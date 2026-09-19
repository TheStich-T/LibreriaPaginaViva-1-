package org.lpv.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.model.Libros;
import org.lpv.system.main;

public class ActualizarPrecioController implements Initializable {

    @FXML private TextField txtIsbn;
    @FXML private Label lblTituloEncontrado;
    @FXML private Label lblPrecioActual;
    @FXML private TextField txtNuevoPrecio;
    @FXML private Button btnBuscar;
    @FXML private Button btnActualizarPrecio;
    @FXML private Label lblMensaje;

    private LibrosDAO librosDAO;
    private Libros libroActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        librosDAO = new LibrosDAOImpl();
        lblMensaje.setText("");
    }

    @FXML
    public void eventoBuscar(ActionEvent evento) {
        try {
            ValidarException.validarNoVacio(txtIsbn.getText(), "ISBN");

            libroActual = librosDAO.buscar(txtIsbn.getText().trim());

            if (libroActual == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "No se encontró ningún libro con ese ISBN");
                lblTituloEncontrado.setText("");
                lblPrecioActual.setText("");
                return;
            }

            lblTituloEncontrado.setText(libroActual.getTitulo());
            lblPrecioActual.setText("Q " + libroActual.getPrecio());
            txtNuevoPrecio.clear();

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoActualizarPrecio(ActionEvent evento) {
        try {
            ValidarException.validarNulo(libroActual, "Busca primero un libro por ISBN");
            ValidarException.validarNoVacio(txtNuevoPrecio.getText(), "nuevo precio");

            double nuevoPrecio;
            try {
                nuevoPrecio = Double.parseDouble(txtNuevoPrecio.getText().trim());
            } catch (NumberFormatException e) {
                throw new ValidarException("El precio debe ser un número válido");
            }
            if (nuevoPrecio <= 0) {
                throw new ValidarException("El precio debe ser mayor a 0");
            }

            boolean actualizado = librosDAO.actualizarPrecio(libroActual.getIsbn(), nuevoPrecio);

            if (actualizado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Precio actualizado con éxito");
                lblPrecioActual.setText("Q " + nuevoPrecio);
                txtNuevoPrecio.clear();
                lblMensaje.setText("");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar el precio");
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

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}