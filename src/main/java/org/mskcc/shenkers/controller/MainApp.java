package org.mskcc.shenkers.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import java.io.IOException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.property.Property;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.glassfish.grizzly.http.server.HttpServer;
import org.mskcc.shenkers.control.alignment.config.AlignmentConfiguration;
import org.mskcc.shenkers.control.track.config.TrackConfiguration;
import org.mskcc.shenkers.jersey.config.ServerModule;
import org.mskcc.shenkers.model.CoordinateChangeModule;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Injector inj = Guice.createInjector(new TrackConfiguration(), new AlignmentConfiguration(), new CoordinateChangeModule(), new ServerModule());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Scene.fxml"));
        loader.setControllerFactory((Class<?> type) -> {
            return inj.getInstance(type);
        });

////        loader.setRoot(this);
////        loader.setController(this);
//
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/styles/Styles.css");

            stage.setTitle("Comparative Browser");
            stage.setScene(scene);
            stage.setOnCloseRequest(e -> {
                TypeLiteral<Property<HttpServer>> serverType = new TypeLiteral<Property<HttpServer>>() {};
                Property<HttpServer> instance = inj.getInstance(Key.get(serverType));
                if (instance.getValue() != null) {
                    instance.getValue().shutdownNow();
                }
            });
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        Injector inj = Guice.createInjector(new AbstractModule() {
//            
//            class ABC implements Serializable{
//                
//            }
//            
//            @Override
//            protected void configure() {
//                bind(Serializable.class).to(ABC.class);
//            }
//        },
//                new AbstractModule() {
//                    
//                    @Override
//                    protected void configure() {
//                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                    }
//                }
//                
//                
//        );
//        Serializable instance = inj.getInstance(Serializable.class);
//        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));
//        Scene scene = new Scene(root);
//        scene.getStylesheets().add("/styles/Styles.css");
//        
//        stage.setTitle("JavaFX and Maven");
//        stage.setScene(scene);
//        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
