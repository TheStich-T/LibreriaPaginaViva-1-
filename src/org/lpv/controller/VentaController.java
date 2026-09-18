package org.lpv.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.lpv.dao.ClienteDAO;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.UsuarioDAO;
import org.lpv.dao.VentaDAO;
import org.lpv.dao.impl.ClientesDAOImpl;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.dao.impl.UsuarioDAOImpl;
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
import org.lpv.util.SecurityUtil;

public class VentaController implements Initializable {

    // Porcentaje de descuento a partir del cual se exige autorización de un administrador.
    private static final double UMBRAL_DESCUENTO_AUTORIZACION = 10.0;

    @FXML private TextField txtFiltroLibro;
    @FXML private ComboBox<Libros> cmbLibro;
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
    @FXML private TextField txtDescuento;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;
    @FXML private Label lblMensaje;

    private final ObservableList<detalleVenta> carrito = FXCollections.observableArrayList();
    private final ObservableList<Clientes> clientesData = FXCollections.observableArrayList();
    private final ObservableList<Libros> librosData = FXCollections.observableArrayList();
    private FilteredList<Clientes> clientesFiltrados;
    private FilteredList<Libros> librosFiltrados;
    private LibrosDAO librosDAO;
    private VentaDAO ventaDAO;
    private ClienteDAO clienteDAO;
    private UsuarioDAO usuarioDAO;

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
        usuarioDAO = new UsuarioDAOImpl();
        txtCantidad.setText("1");
        txtDescuento.setText("0");
        tblCarrito.setItems(carrito);
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        actualizarTotales();
        cargarClientes();
        cargarLibros();
        txtFiltroCliente.textProperty().addListener((obs, textoAnterior, textoNuevo) -> filtrarClientes(textoNuevo));
        txtFiltroLibro.textProperty().addListener((obs, textoAnterior, textoNuevo) -> filtrarLibros(textoNuevo));
        txtDescuento.textProperty().addListener((obs, textoAnterior, textoNuevo) -> actualizarTotales());
        cmbCliente.setOnAction(e -> seleccionarCliente());
        cmbLibro.setOnAction(e -> seleccionarLibro());
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
            double porcentajeDescuento = leerDescuento();
            double descuento = calcularDescuento(subtotal, porcentajeDescuento);
            double total = subtotal - descuento;

            // Si el descuento supera el umbral, se exige autorización de un administrador.
            int idAutorizaDescuento = solicitarAutorizacionDescuento(porcentajeDescuento);

            Venta venta = new Venta();
            venta.setSubtotal(subtotal);
            venta.setDescuento(descuento);
            venta.setUsuarioAutorizaDescuento(idAutorizaDescuento);
            venta.setTotal(total);
            venta.setCuiCliente(cui);
            venta.setIdUsuario(actual.getId());

            List<detalleVenta> detallesFactura = new ArrayList<>(carrito);
            boolean registrada = ventaDAO.registrarVenta(venta, detallesFactura);

            if (!registrada) {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar la venta. No se realizaron cambios en la base de datos.");
                return;
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Venta registrada correctamente. Número de venta: " + venta.getIdVenta());

            Clientes cliente = clienteDAO.buscar(cui);
            Venta ventaRegistrada = ventaDAO.buscar(venta.getIdVenta());

            abrirFactura(evento, ventaRegistrada != null ? ventaRegistrada : venta, cliente, detallesFactura);

            carrito.clear();
            txtCuiCliente.clear();
            txtFiltroCliente.clear();
            txtDescuento.setText("0");
            cmbCliente.getSelectionModel().clearSelection();
            limpiarEntrada();
            cargarLibros();
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
        txtDescuento.setText("0");
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

    private double leerDescuento() throws ValidarException {
        ValidarException.validarNoVacio(txtDescuento.getText(), "descuento");

        try {
            double porcentaje = Double.parseDouble(txtDescuento.getText().trim());

            if (porcentaje < 0 || porcentaje > 100) {
                throw new ValidarException("El descuento debe estar entre 0% y 100%.");
            }

            return porcentaje;

        } catch (NumberFormatException e) {
            throw new ValidarException("El descuento debe ser un porcentaje válido.");
        }
    }

    private double calcularDescuento(double subtotal, double porcentajeDescuento) {
        return subtotal * porcentajeDescuento / 100;
    }

 
      //Si el porcentaje de descuento supera el umbral permitido, pide usuario y
      //contraseña de un administrador antes de continuar con la venta.
    private int solicitarAutorizacionDescuento(double porcentajeDescuento) throws ValidarException {
        if (porcentajeDescuento <= UMBRAL_DESCUENTO_AUTORIZACION) {
            return 0;
        }

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Autorización requerida");
        dialogo.setHeaderText(
                "El descuento de " + porcentajeDescuento + "% supera el " + UMBRAL_DESCUENTO_AUTORIZACION
                + "% permitido.\nIngresá el usuario y la contraseña de un administrador para autorizarlo."
        );

        ButtonType btnAutorizar = new ButtonType("Autorizar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnAutorizar, ButtonType.CANCEL);

        TextField txtUsuarioAutoriza = new TextField();
        txtUsuarioAutoriza.setPromptText("Usuario administrador");

        PasswordField txtPasswordAutoriza = new PasswordField();
        txtPasswordAutoriza.setPromptText("Contraseña");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Usuario:"), txtUsuarioAutoriza);
        grid.addRow(1, new Label("Contraseña:"), txtPasswordAutoriza);
        dialogo.getDialogPane().setContent(grid);

        Optional<ButtonType> resultado = dialogo.showAndWait();

        if (resultado.isEmpty() || resultado.get() != btnAutorizar) {
            throw new ValidarException("Se canceló la autorización del descuento.");
        }

        String usuarioAutoriza = txtUsuarioAutoriza.getText() == null ? "" : txtUsuarioAutoriza.getText().trim();
        String passwordAutoriza = txtPasswordAutoriza.getText() == null ? "" : txtPasswordAutoriza.getText();

        if (usuarioAutoriza.isEmpty() || passwordAutoriza.isEmpty()) {
            throw new ValidarException("Debés ingresar usuario y contraseña del administrador.");
        }

        Usuario autorizador = usuarioDAO.buscarPorUsername(usuarioAutoriza);
        String hashIngresado = SecurityUtil.hashSHA256(passwordAutoriza);

        if (autorizador == null || !autorizador.getPasswordHash().equals(hashIngresado)) {
            throw new ValidarException("Usuario o contraseña de autorización incorrectos.");
        }

        if (!autorizador.isActivo()) {
            throw new ValidarException("El usuario autorizador está inactivo.");
        }

        if (!"admin".equalsIgnoreCase(autorizador.getRol())) {
            throw new ValidarException("Solo un administrador puede autorizar descuentos mayores al " + UMBRAL_DESCUENTO_AUTORIZACION + "%.");
        }

        return autorizador.getId();
    }

    private void actualizarTotales() {
        double subtotal = calcularSubtotal();
        double porcentajeDescuento = 0;

        try {
            String texto = txtDescuento == null || txtDescuento.getText() == null ? "" : txtDescuento.getText().trim();

            if (!texto.isEmpty()) {
                porcentajeDescuento = Double.parseDouble(texto);
            }

            if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
                porcentajeDescuento = 0;
            }

        } catch (NumberFormatException e) {
            porcentajeDescuento = 0;
        }

        double descuento = calcularDescuento(subtotal, porcentajeDescuento);
        double total = subtotal - descuento;

        lblSubtotal.setText(String.format("Q %.2f", subtotal));
        lblTotal.setText(String.format("Q %.2f", total));
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
                || cliente.getApellidoCliente().toLowerCase().contains(textoBusqueda)
        );
    }

    private void cargarLibros() {
        // solo libros activos se pueden vender
        librosData.setAll(librosDAO.listar().stream().filter(Libros::isActivo).toList());

        if (librosFiltrados == null) {
            librosFiltrados = new FilteredList<>(librosData, libro -> true);
            cmbLibro.setItems(librosFiltrados);
        }
    }

    private void filtrarLibros(String texto) {
        if (librosFiltrados == null) {
            return;
        }

        if (texto == null || texto.isBlank()) {
            librosFiltrados.setPredicate(libro -> true);
            return;
        }

        String textoBusqueda = texto.trim().toLowerCase();

        librosFiltrados.setPredicate(libro ->
                libro.getTitulo().toLowerCase().contains(textoBusqueda)
                || libro.getIsbn().toLowerCase().contains(textoBusqueda)
        );
    }

    private void seleccionarLibro() {
        Libros seleccionado = cmbLibro.getValue();

        if (seleccionado == null) {
            return;
        }

        txtIsbn.setText(seleccionado.getIsbn());
        txtCantidad.requestFocus();
        txtCantidad.selectAll();
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