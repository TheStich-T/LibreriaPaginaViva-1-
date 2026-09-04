package org.lpv.system;

import java.io.IOException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.lpv.manager.RolPermisos;
import org.lpv.manager.SessionContext;
import org.lpv.model.Usuario;

public class main extends Application {

    private static Stage escenarioPrincipal;

    public static void cambiarEscena(String rutaFXML) throws IOException {
        Parent raiz = FXMLLoader.load(main.class.getResource(rutaFXML));
        Scene escena = new Scene(raiz);
        escenarioPrincipal.setScene(escena);
        escenarioPrincipal.sizeToScene();
        escenarioPrincipal.centerOnScreen();
        escenarioPrincipal.show();
    }

    public static void volverAlDashboard() throws IOException {
        Usuario actual = SessionContext.getInstancia().getUsuairoActual();
        String rol = (actual != null) ? actual.getRol() : null;
        String dashboard = RolPermisos.getDashboardPorRol(rol);

        cambiarEscena(dashboard != null ? dashboard : "/org/lpv/view/LoginView.fxml");
    }

    @Override
    public void start(Stage stage) throws Exception {
        main.escenarioPrincipal = stage;
        stage.setTitle("Libreria Pagina Viva");
        cambiarEscena("/org/lpv/view/LoginView.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    
}