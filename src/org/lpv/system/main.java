package org.lpv.system;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application{


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        VBox raiz = new VBox();
        

        Scene escena = new Scene(raiz);
        
        stage.setTitle("Libreria Pagina Viva");
        stage.setScene(escena);
        stage.show();
        

    }
    
}
