package Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Main extends Application { // Nemores dat consturctorja, ker je Application abstract class, lahko pa dajas seterje

    private ChatClient client;
    private FXMLLoader loader;
    private Parent root;
    private Scene scene;
    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.stage = primaryStage;
        this.loader = new FXMLLoader(getClass().getResource("main.fxml"));
        this.root = this.loader.load();
        this.client = this.loader.getController();
        this.scene = new Scene(this.root);
        this.scene.getStylesheets().add(getClass().getResource("css/main.css").toExternalForm());

        this.scene.setOnKeyPressed(event -> { // ce je pressed ENTER, poslje kar je 

            if (event.getCode().equals(KeyCode.ENTER))  this.client.getMessageFromInputField();
        });

        this.stage.setResizable(false);
        this.stage.setTitle(this.client.clientName);
        this.stage.setScene(this.scene);
        this.stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() { // ko se zapre app

        this.client.cleanUp(); // zapre client
        stage.close(); // zapre stage (GUI)
    }
}
