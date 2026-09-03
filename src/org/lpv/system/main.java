package org.lpv.system;

import java.io.IOException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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