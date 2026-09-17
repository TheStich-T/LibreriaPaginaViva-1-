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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.lpv.dao.AutorDAO;
import org.lpv.dao.CategoriaDAO;
import org.lpv.dao.EditorialDAO;
import org.lpv.dao.LibrosDAO;
import org.lpv.dao.impl.AutorDAOImpl;
import org.lpv.dao.impl.CategoriaDAOImpl;
import org.lpv.dao.impl.EditorialDAOImpl;
import org.lpv.dao.impl.LibrosDAOImpl;
import org.lpv.exception.ValidarException;
import org.lpv.manager.RolPermisos;
import org.lpv.manager.SessionContext;
import org.lpv.model.Autor;
import org.lpv.model.Categoria;
import org.lpv.model.Editorial;
import org.lpv.model.Libros;
import org.lpv.model.Usuario;
import org.lpv.system.main;

public class LibrosFormController implements Initializable {

    public static String isbnAAbrir = null;

    @FXML private TableView<Libros> tblLibros;
    @FXML private TableColumn<Libros, String> colIsbn;
    @FXML private TableColumn<Libros, String> colTitulo;
    @FXML private TableColumn<Libros, Double> colPrecio;
    @FXML private TableColumn<Libros, Integer> colStockActual;
    @FXML private TableColumn<Libros, Integer> colStockMinimo;
    @FXML private TableColumn<Libros, Boolean> colActivo;
    @FXML private TextField txtIsbn;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtFechaPublicacion;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtNitEditorial;
    @FXML private TextField txtStockMinimo;
    @FXML private Button btnAgregar;
    @FXML private Button btnActualizar;
    @FXML private Button btnDesactivar;
    @FXML private Button btnActivar;
    @FXML private Label lblMensaje;
    @FXML private ComboBox<Autor> cmbAutor;
    @FXML private TextField txtNuevoNitEditorial;
    @FXML private TextField txtNuevoNombreEditorial;
    @FXML private TextField txtNuevoTelefonoEditorial;
    @FXML private TextField txtNuevaDireccionEditorial;
    @FXML private Button btnCrearEditorial;
    @FXML private TextField txtNuevoNombreAutor;
    @FXML private TextField txtNuevoApellidoAutor;
    @FXML private TextField txtNuevaNacionalidadAutor;
    @FXML private Button btnCrearAutor;

    private LibrosDAO librosDAO;
    private CategoriaDAO categoriaDAO;
    private EditorialDAO editorialDAO;
    private AutorDAO autorDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Usuario actual = SessionContext.getInstancia().getUsuairoActual();
        if (actual == null || !RolPermisos.tienePermiso(actual.getRol(), RolPermisos.LIBROS_GESTIONAR)) {
            mostrarAlerta(Alert.AlertType.ERROR, "No tenés permiso para acceder a esta pantalla");
            volverAlLogin();
            return;
        }

        librosDAO = new LibrosDAOImpl();
        categoriaDAO = new CategoriaDAOImpl();
        editorialDAO = new EditorialDAOImpl();
        autorDAO = new AutorDAOImpl();
        lblMensaje.setText("");

        cargarCategorias();
        cargarAutores();
        cargarLibros();

        tblLibros.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtIsbn.setText(seleccionado.getIsbn());
                txtIsbn.setDisable(true); 
                txtTitulo.setText(seleccionado.getTitulo());
                 txtFechaPublicacion.setText(seleccionado.getFechaPublicacion() != null
                        ? seleccionado.getFechaPublicacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "");
                txtPrecio.setText(String.valueOf(seleccionado.getPrecio()));
                seleccionarCategoria(seleccionado.getIdCategoria());
                txtNitEditorial.setText(seleccionado.getNitEditorial());
                txtStockMinimo.setText(String.valueOf(seleccionado.getStockMinimo()));
            }
        });

        if (isbnAAbrir != null) {
            for (Libros libro : tblLibros.getItems()) {
                if (libro.getIsbn().equals(isbnAAbrir)) {
                    tblLibros.getSelectionModel().select(libro);
                    tblLibros.scrollTo(libro);
                    break;
                }
            }
            isbnAAbrir = null;
        }
    }

    private void cargarCategorias() {
        ObservableList<Categoria> categorias = FXCollections.observableArrayList(categoriaDAO.listar());
        cmbCategoria.setItems(categorias);
    }

    private void cargarAutores() {
        ObservableList<Autor> autores = FXCollections.observableArrayList(autorDAO.listar());
        cmbAutor.setItems(autores);
    }

    private void seleccionarCategoria(int idCategoria) {
        for (Categoria categoria : cmbCategoria.getItems()) {
            if (categoria.getIdCategoria() == idCategoria) {
                cmbCategoria.setValue(categoria);
                return;
            }
        }
        cmbCategoria.setValue(null);
    }

    private void cargarLibros() {
        ObservableList<Libros> libros = FXCollections.observableArrayList(librosDAO.listar());
        tblLibros.setItems(libros);
    }

    @FXML
    public void eventoCrearEditorial(ActionEvent evento) {
        try {
            ValidarException.validarNoVacio(txtNuevoNitEditorial.getText(), "NIT de la nueva editorial");
            ValidarException.validarNoVacio(txtNuevoNombreEditorial.getText(), "nombre de la nueva editorial");
            ValidarException.validarNoVacio(txtNuevoTelefonoEditorial.getText(), "teléfono de la nueva editorial");
            ValidarException.validarNoVacio(txtNuevaDireccionEditorial.getText(), "dirección de la nueva editorial");

            Editorial nueva = new Editorial();
            nueva.setNit(txtNuevoNitEditorial.getText().trim());
            nueva.setNombreEditorial(txtNuevoNombreEditorial.getText().trim());
            nueva.setTelefonoEditorial(txtNuevoTelefonoEditorial.getText().trim());
            nueva.setDireccionEditoria(txtNuevaDireccionEditorial.getText().trim());

            boolean creada = editorialDAO.insertar(nueva);

            if (creada) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Editorial creada con éxito");
                txtNitEditorial.setText(nueva.getNit());
                txtNuevoNitEditorial.clear();
                txtNuevoNombreEditorial.clear();
                txtNuevoTelefonoEditorial.clear();
                txtNuevaDireccionEditorial.clear();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear la editorial");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoCrearAutor(ActionEvent evento) {
        try {
            ValidarException.validarNoVacio(txtNuevoNombreAutor.getText(), "nombre del nuevo autor");
            ValidarException.validarNoVacio(txtNuevoApellidoAutor.getText(), "apellido del nuevo autor");

            Autor nuevo = new Autor();
            nuevo.setNombreAutor(txtNuevoNombreAutor.getText().trim());
            nuevo.setApellidoAutor(txtNuevoApellidoAutor.getText().trim());
            nuevo.setNacionalidad(txtNuevaNacionalidadAutor.getText() == null ? "" : txtNuevaNacionalidadAutor.getText().trim());
            nuevo.setBiografia("");

            boolean creado = autorDAO.insertar(nuevo);

            if (creado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Autor creado con éxito");
                cargarAutores();
                seleccionarAutorPorNombre(nuevo.getNombreAutor(), nuevo.getApellidoAutor());
                txtNuevoNombreAutor.clear();
                txtNuevoApellidoAutor.clear();
                txtNuevaNacionalidadAutor.clear();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear el autor");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    private void seleccionarAutorPorNombre(String nombre, String apellido) {
        for (Autor autor : cmbAutor.getItems()) {
            if (autor.getNombreAutor().equalsIgnoreCase(nombre) && autor.getApellidoAutor().equalsIgnoreCase(apellido)) {
                cmbAutor.setValue(autor);
                return;
            }
        }
    }

    @FXML
    public void eventoAgregar(ActionEvent evento) {
        try {
            Libros nuevoLibro = leerFormulario(true);

            boolean creado = librosDAO.insertar(nuevoLibro);

            if (creado) {
                asociarAutorSiAplica(nuevoLibro.getIsbn());
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
                asociarAutorSiAplica(seleccionado.getIsbn());
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

    private void asociarAutorSiAplica(String isbn) {
        Autor autorSeleccionado = cmbAutor.getValue();
        if (autorSeleccionado != null) {
            autorDAO.asociarLibro(autorSeleccionado.getIdAutor(), isbn);
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
        ValidarException.validarNoVacio(txtFechaPublicacion.getText(), "fecha de publicación");
        ValidarException.validarNoVacio(txtPrecio.getText(), "precio");
        ValidarException.validarNulo(cmbCategoria.getValue(), "Selecciona una categoría");
        ValidarException.validarNoVacio(txtNitEditorial.getText(), "editorial");
        ValidarException.validarNoVacio(txtStockMinimo.getText(), "stock mínimo");

        double precio;
        int idCategoria = cmbCategoria.getValue().getIdCategoria();
        int stockMinimo;
        LocalDate fechaPublicacion;

        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidarException("El precio debe ser un número válido");
        }
        if (precio <= 0) {
            throw new ValidarException("El precio debe ser mayor a 0");
        }

        try {
            fechaPublicacion = LocalDate.parse(txtFechaPublicacion.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            throw new ValidarException("La fecha de publicación debe tener el formato dd/MM/yyyy");
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
        libro.setFechaPublicacion(fechaPublicacion);
        libro.setPrecio(precio);
        libro.setIdCategoria(idCategoria);
        libro.setNitEditorial(txtNitEditorial.getText().trim());
        libro.setStockActual(0);
        libro.setStockMinimo(stockMinimo);

        return libro;
    }

    private void limpiarCampos() {
        txtIsbn.clear();
        txtIsbn.setDisable(false);
        txtTitulo.clear();
        txtFechaPublicacion.clear();
        txtPrecio.clear();
        cmbCategoria.setValue(null);
        txtNitEditorial.clear();
        txtStockMinimo.clear();
        cmbAutor.setValue(null);
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.show();
    }

    private void volverAlLogin() {
        try {
            main.cambiarEscena("/org/lpv/view/LoginView.fxml");
        } catch (IOException e) {
            System.err.println("Error al redirigir al login: " + e.getMessage());
        }
    }
}