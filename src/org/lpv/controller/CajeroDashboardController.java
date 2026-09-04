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
import org.lpv.manager.SessionContext;
import org.lpv.model.Usuario;
import org.lpv.system.main;

public class CajeroDashboardController implements Initializable {

    @FXML private Label lblUsuarioActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Usuario actual = SessionContext.getInstancia().getUsuairoActual();

        if (actual == null || !"cajero".equalsIgnoreCase(actual.getRol())) {
            mostrarAlerta(Alert.AlertType.ERROR, "No tenés permiso para acceder a esta pantalla");
            volverAlLogin();
            return;
        }

        lblUsuarioActual.setText(actual.getUsername() + " (Cajero)");
    }

    @FXML
    public void eventoCerrarSesion(ActionEvent evento) {
        SessionContext.getInstancia().cerrarSesion();
        volverAlLogin();
    }

    @FXML
    public void eventoNuevaVenta(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/VentaView.fxml");
    }

    @FXML
    public void eventoDetalleVentas(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/DetalleVentaView.fxml");
    }

    @FXML
    public void eventoListaVentas(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/ListaVentasView.fxml");
    }

    @FXML
    public void eventoConsultarStock(ActionEvent evento) {
        cambiarEscena("/org/lpv/view/BuscarLibrosView.fxml");
    }

    @FXML
    public void eventoModuloEnDesarrollo(ActionEvent evento) {
        String nombreModulo = ((Button) evento.getSource()).getText();
        mostrarAlerta(Alert.AlertType.INFORMATION, "\"" + nombreModulo + "\" todavía no está implementado. Corresponde a una épica futura del proyecto.");
    }

    private void cambiarEscena(String rutaFXML) {
        try {
            main.cambiarEscena(rutaFXML);
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir el módulo: " + e.getMessage());
        }
    }

    private void volverAlLogin() {
        try {
            main.cambiarEscena("/org/lpv/view/LoginView.fxml");
        } catch (IOException e) {
            System.err.println("Error al redirigir al login: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }
}
