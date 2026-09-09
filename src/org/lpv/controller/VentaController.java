package org.lpv.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.lpv.dao.ClienteDAO;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.VentaDAO;
import org.lpv.dao.impl.ClientesDAOImpl;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.dao.impl.VentaDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.manager.RolPermisos;
import org.lpv.manager.SessionContext;
import org.lpv.model.Clientes;
import org.lpv.model.Libros;
import org.lpv.model.Usuario;
import org.lpv.model.Venta;
import org.lpv.model.detalleVenta;
import org.lpv.system.main;

public class VentaController implements Initializable {

    @FXML private TextField txtIsbn;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtCuiCliente;
    @FXML private TextField txtFiltroCliente;
    @FXML private ComboBox<Clientes> cmbCliente;
    @FXML private TableView<detalleVenta> tblCarrito;
    @FXML private TableColumn<detalleVenta, String> colIsbn;
    @FXML private TableColumn<detalleVenta, Integer> colCantidad;
    @FXML private TableColumn<detalleVenta, Double> colPrecio;
    @FXML private TableColumn<detalleVenta, Double> colSubtotal;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;
    @FXML private Label lblMensaje;

    private final ObservableList<detalleVenta> carrito = FXCollections.observableArrayList();
    private final ObservableList<Clientes> clientesData = FXCollections.observableArrayList();
    private FilteredList<Clientes> clientesFiltrados;
    private LibrosDAO librosDAO;
    private VentaDAO ventaDAO;
    private ClienteDAO clienteDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Usuario actual = SessionContext.getInstancia().getUsuairoActual();

        if (actual == null || !RolPermisos.tienePermiso(actual.getRol(), RolPermisos.VENTAS)) {
            mostrarAlerta(Alert.AlertType.ERROR, "No tenés permiso para acceder a esta pantalla");
            volverAlDashboard();
            return;
        }

        librosDAO = new LibrosDAOImpl();
        ventaDAO = new VentaDAOImpl();
        clienteDAO = new ClientesDAOImpl();
        txtCantidad.setText("1");
        tblCarrito.setItems(carrito);
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        actualizarTotales();
        cargarClientes();
        txtFiltroCliente.textProperty().addListener((obs, textoAnterior, textoNuevo) -> filtrarClientes(textoNuevo));
        cmbCliente.setOnAction(e -> seleccionarCliente());
        lblMensaje.setText("");
    }

    @FXML
    public void eventoAgregar(ActionEvent evento) {
        try {
            ValidarException.validarNoVacio(txtIsbn.getText(), "ISBN");
            int cantidad = leerCantidad();
            String isbn = txtIsbn.getText().trim();

            Libros libro = librosDAO.buscar(isbn);
            if (libro == null) {
                throw new ValidarException("No se encontró el libro con ISBN " + isbn + ".");
            }
            if (!libro.isActivo()) {
                throw new ValidarException("El libro seleccionado está inactivo.");
            }

            detalleVenta existente = buscarEnCarrito(isbn);
            int cantidadFinal = cantidad + (existente != null ? existente.getCantidad() : 0);
            validarStock(libro, cantidadFinal);

            if (existente == null) {
                detalleVenta detalle = new detalleVenta();
                detalle.setIsbn(libro.getIsbn());
                detalle.setTitulo(libro.getTitulo());
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(libro.getPrecio());
                detalle.setSubtotal(cantidad * libro.getPrecio());
                carrito.add(detalle);
            } else {
                existente.setCantidad(cantidadFinal);
                existente.setSubtotal(cantidadFinal * existente.getPrecioUnitario());
                tblCarrito.refresh();
            }

            limpiarEntrada();
            lblMensaje.setText("");
            actualizarTotales();

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "La cantidad debe ser un número entero mayor a 0.");
        }
    }

    @FXML
    public void eventoCambiarCantidad(ActionEvent evento) {
        try {
            detalleVenta seleccionado = tblCarrito.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Seleccioná un producto del carrito.");
            int cantidad = leerCantidad();
            Libros libro = librosDAO.buscar(seleccionado.getIsbn());
            ValidarException.validarNulo(libro, "El libro ya no está disponible.");
            validarStock(libro, cantidad);

            seleccionado.setCantidad(cantidad);
            seleccionado.setSubtotal(cantidad * seleccionado.getPrecioUnitario());
            tblCarrito.refresh();
            actualizarTotales();
            lblMensaje.setText("");
        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "La cantidad debe ser un número entero mayor a 0.");
        }
    }

    @FXML
    public void eventoEliminar(ActionEvent evento) {
        detalleVenta seleccionado = tblCarrito.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccioná un producto del carrito.");
            return;
        }
        carrito.remove(seleccionado);
        actualizarTotales();
    }

    @FXML
    public void eventoRegistrarVenta(ActionEvent evento) {
        try {
            if (carrito.isEmpty()) {
                throw new ValidarException("El carrito no puede estar vacío.");
            }
            ValidarException.validarNoVacio(txtCuiCliente.getText(), "CUI del cliente");

            long cui = Long.parseLong(txtCuiCliente.getText().trim());
            Usuario actual = SessionContext.getInstancia().getUsuairoActual();
            ValidarException.validarNulo(actual, "No hay una sesión activa.");

            double subtotal = calcularSubtotal();
            double descuento = 0;
            double total = subtotal - descuento;

            Venta venta = new Venta();
            venta.setSubtotal(subtotal);
            venta.setDescuento(descuento);
            venta.setTotal(total);
            venta.setCuiCliente(cui);
            venta.setIdUsuario(actual.getId());

            List<detalleVenta> detallesFactura = new ArrayList<>(carrito);
            boolean registrada = ventaDAO.registrarVenta(venta, detallesFactura);
            if (!registrada) {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar la venta. No se realizaron cambios en la base de datos.");
                return;
            }

            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Venta registrada correctamente. Número de venta: " + venta.getIdVenta());

            Clientes cliente = clienteDAO.buscar(cui);
            Venta ventaRegistrada = ventaDAO.buscar(venta.getIdVenta());
            abrirFactura(evento, ventaRegistrada != null ? ventaRegistrada : venta, cliente, detallesFactura);

            carrito.clear();
            txtCuiCliente.clear();
            txtFiltroCliente.clear();
            cmbCliente.getSelectionModel().clearSelection();
            limpiarEntrada();
            actualizarTotales();
            lblMensaje.setText("");

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "El CUI debe ser un número válido.");
        }
    }

    @FXML
    public void eventoLimpiar(ActionEvent evento) {
        carrito.clear();
        txtCuiCliente.clear();
        txtFiltroCliente.clear();
        cmbCliente.getSelectionModel().clearSelection();
        limpiarEntrada();
        actualizarTotales();
        lblMensaje.setText("");
    }

    @FXML
    public void eventoVolver(ActionEvent evento) {
        volverAlDashboard();
    }

    private void abrirFactura(ActionEvent evento, Venta venta, Clientes cliente, List<detalleVenta> detalles) {
        try {
            FXMLLoader loader = new FXMLLoader(main.class.getResource("/org/lpv/view/FacturaView.fxml"));
            Parent raiz = loader.load();
            FacturaController controller = loader.getController();
            controller.cargarDatosFactura(venta, cliente, FXCollections.observableArrayList(detalles));

            Stage ventanaFactura = new Stage();
            ventanaFactura.setTitle("Factura - Venta #" + venta.getIdVenta());
            ventanaFactura.setScene(new Scene(raiz));

            Stage ventanaVenta = (Stage) ((Node) evento.getSource()).getScene().getWindow();
            ventanaFactura.initOwner(ventanaVenta);
            ventanaFactura.initModality(Modality.WINDOW_MODAL);
            ventanaFactura.showAndWait();
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la factura: " + e.getMessage());
        }
    }

    private int leerCantidad() throws ValidarException {
        ValidarException.validarNoVacio(txtCantidad.getText(), "cantidad");
        int cantidad = Integer.parseInt(txtCantidad.getText().trim());
        if (cantidad <= 0) {
            throw new ValidarException("La cantidad debe ser mayor a 0.");
        }
        return cantidad;
    }

    private void validarStock(Libros libro, int cantidad) throws ValidarException {
        if (cantidad > libro.getStockActual()) {
            throw new ValidarException("Stock insuficiente. Disponible: " + libro.getStockActual() + ".");
        }
    }

    private detalleVenta buscarEnCarrito(String isbn) {
        for (detalleVenta detalle : carrito) {
            if (detalle.getIsbn().equalsIgnoreCase(isbn)) {
                return detalle;
            }
        }
        return null;
    }

    private double calcularSubtotal() {
        double subtotal = 0;
        for (detalleVenta detalle : carrito) {
            subtotal += detalle.getSubtotal();
        }
        return subtotal;
    }

    private void actualizarTotales() {
        double subtotal = calcularSubtotal();
        lblSubtotal.setText(String.format("Q %.2f", subtotal));
        lblTotal.setText(String.format("Q %.2f", subtotal));
    }

    private void limpiarEntrada() {
        txtIsbn.clear();
        txtCantidad.setText("1");
        txtIsbn.requestFocus();
    }

    private void cargarClientes() {
        clientesData.setAll(clienteDAO.listar());
        if (clientesData.isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudieron cargar los clientes.");
        }
        clientesFiltrados = new FilteredList<>(clientesData, cliente -> true);
        cmbCliente.setItems(clientesFiltrados);
    }

    private void filtrarClientes(String texto) {
        if (clientesFiltrados == null) {
            return;
        }
        if (texto == null || texto.isBlank()) {
            clientesFiltrados.setPredicate(cliente -> true);
            return;
        }
        String textoBusqueda = texto.trim().toLowerCase();
        clientesFiltrados.setPredicate(cliente ->
                String.valueOf(cliente.getCui()).contains(textoBusqueda)
                || cliente.getNombreCliente().toLowerCase().contains(textoBusqueda)
                || cliente.getApellidoCliente().toLowerCase().contains(textoBusqueda));
    }

    private void seleccionarCliente() {
        Clientes seleccionado = cmbCliente.getValue();
        if (seleccionado == null) {
            return;
        }
        txtCuiCliente.setText(String.valueOf(seleccionado.getCui()));
    }

    private void volverAlDashboard() {
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