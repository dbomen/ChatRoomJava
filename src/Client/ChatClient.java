package Client;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import com.google.gson.Gson;

import GsonTypes.BluePrint;
import GsonTypes.Message;
import GsonTypes.Request;
import GsonTypes.Response;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatClient extends Thread implements Initializable {
	protected int serverPort = 1234;
	protected Socket socketToServer;
	protected DataInputStream in;
	protected DataOutputStream out;
	protected BufferedReader std_in;
	protected boolean logedIn;
	protected String messageFromServer;
    protected String clientName;
    protected List<String> currentOnlineUsers;

    @FXML
    protected AnchorPane mainPane;

    @FXML
    protected TextField userInputField;

    @FXML
    protected TextFlow mainTextFlow;

    @FXML
    protected ScrollPane scrollPaneForMainTextFlow;

    @FXML
    protected Label chatLabel;

    @FXML
    protected ListView<String> mainListView;

    @FXML
    protected ContextMenu colorsMenu;

	public ChatClient() throws Exception {
		this.messageFromServer = null;
		this.socketToServer = null;
		this.in = null;
		this.out = null;
        this.logedIn = false;

		// connect to the chat server
		try {
			String connectingMessage = "[system] connecting to chat server ...";
			System.out.println(connectingMessage);

			socketToServer = new Socket("localhost", serverPort); // create socket connection
			in = new DataInputStream(socketToServer.getInputStream()); // create input stream for listening for incoming messages
			out = new DataOutputStream(socketToServer.getOutputStream()); // create output stream for sending messages
			System.out.println("[system] connected");

			ChatClientMessageReceiver message_receiver = new ChatClientMessageReceiver(in, this); // create a separate thread for listening to messages from the chat server
			message_receiver.start(); // run the new thread
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}

		// login in / sing up
		LoginGUI loginGui = new LoginGUI(this);
		while (true) {
			String currentText = loginGui.errorMsg.getText();
			if (this.messageFromServer != null && !this.messageFromServer.equals(currentText)) {
                if (this.messageFromServer.contains("SUCCESS")) {this.logedIn = true; break;}
				loginGui.putErrorMsg(this.messageFromServer);
				this.messageFromServer = null;
			}
		}
		loginGui.setVisible(false);
		loginGui.dispose();

		this.clientName = this.messageFromServer.substring(this.messageFromServer.indexOf(' ') + 1);

        // THREAD, ki vsako sekundo updates online users tab
        Thread onlineUsersUpdater = new Thread() {

            public void run() {

                while (true) { // vsako sekundo updates, online users list

                    Platform.runLater(new Runnable() {

                        public void run() {

                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");
                            LocalDateTime now = LocalDateTime.now();

                            try {
                                out.writeUTF(new Request().createRequest(0, clientName, null, dtf.format(now).toString()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("PROBLEM WITH ONLINE USERS LIST UPDATER\n");
                        e.printStackTrace();
                    }
                }
            }
        };
        onlineUsersUpdater.start();
	}

    public void getMessageFromInputField() {

        String message = userInputField.getText();
        if (message.length() > 0)  this.sendMessage(message, out, false);
        userInputField.setText("");
    }

	public void cleanUp() {
		// cleanup
		try {
			this.out.close();
			in.close();
			std_in.close();
			this.socketToServer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String message, DataOutputStream out, boolean login) {
		try {

            if (login) { // ce smo v login phase (saj v tem phase ne uporabljamo JSON-ov)

                out.writeUTF(message);
                out.flush();
                return;
            }

            // preuredimo v pravilen JSON (2 moznosti, pises public ali private [online or offline]), 
            // uporabnika ne zanima ali je offline ali online on samo poslje privatno sporocilo

            // ostale funkcionalnosti delas z gumbi (PS: privatno sporocilo lahko tudi z gumbi, ali pa ne <oziroma checkbox thing>)
            String msg_send = "";

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();

            if (message.charAt(0) == '@' || !this.chatLabel.getText().equals("PUBLIC")) { // private

                String ciljniClient;
                if (message.charAt(0) == '@') { // ce uporablja command "@" za private messages

                    int endIndex = 0;
                    for (; message.charAt(endIndex) != ' '; endIndex++);
					ciljniClient = message.substring(1, endIndex);

                    if (message.charAt(endIndex + 1) == '/') { // ce uporabi kaksen command (za zdaj je samo history)

                        if (message.charAt(endIndex + 2) == 'H')  msg_send = new Request().createRequest(1, clientName, ciljniClient, dtf.format(now).toString());
                    }
                    else { // ce je normal private message

                        this.chatLabel.setText(ciljniClient);
                        msg_send = new Message().createJson(1, this.clientName, ciljniClient, dtf.format(now).toString(), message.substring(endIndex + 1, message.length()));
                    }
                }
                else { // ce ima selectano nekega other clienta, ki mu hoce poslat private message

                    ciljniClient = this.chatLabel.getText();
                    msg_send = new Message().createJson(1, this.clientName, ciljniClient, dtf.format(now).toString(), message);
                }

            }
            else { // public

                msg_send = new Message().createJson(0, this.clientName, null, dtf.format(now).toString(), message);
            }

            // izpise se v UI, da je poslal
            try {
                this.addMessage(msg_send);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

			out.writeUTF(msg_send); // send the message to the chat server
			out.flush(); // ensure the message has been sent
		} catch (IOException e) {
			System.err.println("[system] could not send message");
			e.printStackTrace(System.err);
		}
	}

    public void addMessage(String message) throws InterruptedException { // TUKAJ SE INTERPRETIRA JSON, KI SMO GA DOBILI (BOLJSE IME BI BILO MOGOCE "interpretMessage", ampak ok)

        String client = this.clientName;

        Platform.runLater(new Runnable() {
            
            String nameOfClient = client;
            
            public void run() {

                Gson gson = new Gson();
                BluePrint json = new BluePrint();
                try {
                    json = gson.fromJson(message, BluePrint.class);
                } catch (Exception e) {

                    System.out.printf("PROBLEMATIC [%s]", message);
                }

                if (json.tipGsona == 1) { // ce dobimo GsonTypes.Message

                    Message msg = gson.fromJson(message, Message.class);
                    Text text = new Text();

                    if (msg.getSender().equals(nameOfClient)) { // ce samo prikazujemo nase posiljanje
    
                        if (msg.getTip() == 0) {
    
                            text.setText(String.format("YOU> %s\n", msg.getBody()));
                        }
                        else if (msg.getTip() == 1) {
    
                            text.setText(String.format("YOU>%s> %s\n", msg.getReciever(), msg.getBody()));
                            text.setFill(Color.rgb(0, 0, 125));
                        }
                    }
                    else { // ce dobimo message od serverja
    
                        if (msg.getTip() == 0) { // public
    
                            text.setText(String.format("%s> %s\n", msg.getSender(), msg.getBody()));
                        }
                        else if (msg.getTip() == 1) { // private
        
                            text.setText(String.format("%s> %s\n", msg.getSender(), msg.getBody()));
                            text.setFill(Color.rgb(0, 0, 125));
                        }
                        else if (msg.getTip() == 2) { // system
        
                            text.setText(String.format("[%s] %s\n", msg.getSender(), msg.getBody()));
                            text.setFill(Color.rgb(125, 0, 0));
                        }
                    }
    
                    mainTextFlow.getChildren().add(text); // ko zgradimo Text, ga dodamo v UI

                    // damo da scrolla dol
                    scrollPaneForMainTextFlow.setVvalue(1.0);
                }
                else if (json.tipGsona == 3) { // ce dobimo GsonTypes.Response

                    Response response = gson.fromJson(message, Response.class);

                    if (response.getTip() == 0) { // reponse za list of online users

                        // INTERPRETIRA TO PRAVILNO IN TA DATA DA V UI (LIST VIEW)
                        @SuppressWarnings("unchecked")
                        ArrayList<String> onlineUsers = (ArrayList<String>) response.getCollection();
                        if (!onlineUsers.equals(currentOnlineUsers)) {

                            mainListView.getItems().setAll("PUBLIC");
                            mainListView.getItems().addAll(onlineUsers);
                            currentOnlineUsers = onlineUsers;
                        }
                    }
                    else if (response.getTip() == 1) { // response za history

                        @SuppressWarnings("unchecked")
                        ArrayList<String> list = (ArrayList<String>) response.getCollection();
                        
                        Text systemText = new Text(String.format("[SYSTEM] HISTORY WITH %s\n", list.get(0))); // na 0 indexu bo vedno ime userja o katerem hoce history
                        systemText.setFill(Color.rgb(125, 0, 0));
                        mainTextFlow.getChildren().add(systemText);

                        // ---
                        // separator magic
                        Separator mainSeperator1 = new Separator(Orientation.HORIZONTAL);
                        mainSeperator1.prefWidthProperty().bind(mainTextFlow.widthProperty());
                        mainSeperator1.getStylesheets().add(getClass().getResource("css/mainSeparator.css").toExternalForm());
                        mainTextFlow.getChildren().add(mainSeperator1);
                        // ---

                        // ---
                        // gremo cez vsebino, ki smo jo dobili od serverja
                        for (int i = 1; i < list.size(); i++) { // gremo cez vsebino histories (od 1 indexa naprej, saj je 0 index ime userja)

                            Message msg = gson.fromJson(list.get(i), Message.class);
                            Text historyMessage = new Text();

                            historyMessage.setText((msg.getSender().equals(this.nameOfClient)) ? String.format("YOU> %s\n", msg.getBody()) : String.format("%s> %s\n", msg.getSender(), msg.getBody()));
                            historyMessage.setFill(Color.rgb(0, 0, 125));
                            mainTextFlow.getChildren().add(historyMessage);
                        }

                        Separator mainSeperator2 = new Separator(Orientation.HORIZONTAL);
                        mainSeperator2.prefWidthProperty().bind(mainTextFlow.widthProperty());
                        mainSeperator2.getStylesheets().add(getClass().getResource("css/mainSeparator.css").toExternalForm());
                        mainTextFlow.getChildren().add(mainSeperator2);
                    } 
                    else if (response.getTip() == 999) { // response za showing offline messages to user

                        Text systemText = new Text("[SYSTEM] YOU GOT MESSAGES WHILE OFFLINE\n");
                        systemText.setFill(Color.rgb(125, 0, 0));
                        mainTextFlow.getChildren().add(systemText);

                        // ---
                        // separator magic
                        Separator mainSeperator1 = new Separator(Orientation.HORIZONTAL);
                        mainSeperator1.prefWidthProperty().bind(mainTextFlow.widthProperty());
                        mainSeperator1.getStylesheets().add(getClass().getResource("css/mainSeparator.css").toExternalForm());
                        mainTextFlow.getChildren().add(mainSeperator1);
                        // ---

                        // ---
                        // gremo cez vsebino, ki smo jo dobili od serverja, dobimo Map<ime, messages>
                        @SuppressWarnings("unchecked")
                        HashMap<String, ArrayList<String>> map = (HashMap<String, ArrayList<String>>) response.getMap();
                        for (String key : map.keySet()) { // gremo cez vse keys (cez users, ki so poslali message offline)

                            for (String message : map.get(key)) { // gremo cez vse messages od tega userja

                                Message msg = gson.fromJson(message, Message.class);
                                Text offlineMessage = new Text();

                                offlineMessage.setText(String.format("%s> %s\n", key, msg.getBody()));
                                offlineMessage.setFill(Color.rgb(0, 0, 125));
                                mainTextFlow.getChildren().add(offlineMessage);
                            }

                            // na koncu damo normalSeperator (torej seperator, ki ni rdec)
                            Separator normalSeperator = new Separator(Orientation.HORIZONTAL);
                            normalSeperator.prefWidthProperty().bind(mainTextFlow.widthProperty());
                            normalSeperator.getStylesheets().add(getClass().getResource("css/classicSeparator.css").toExternalForm());
                            mainTextFlow.getChildren().add(normalSeperator);
                        }
                        // ---

                        Separator mainSeperator2 = new Separator(Orientation.HORIZONTAL);
                        mainSeperator2.prefWidthProperty().bind(mainTextFlow.widthProperty());
                        mainSeperator2.getStylesheets().add(getClass().getResource("css/mainSeparator.css").toExternalForm());
                        mainTextFlow.getChildren().add(mainSeperator2);
                    }
                }
            }
        });
    }

    public void checkForRightClick(MouseEvent event) {

        if (event.getButton() == MouseButton.SECONDARY) { // ce je right click, potem pokazemo color choices

            colorsMenu.show(mainPane, event.getScreenX(), event.getScreenY());
        }
        else {

            colorsMenu.hide();
        }
    }

    public void changeColors(String primary, String secondary) {

        Platform.runLater(new Runnable() {

            public void run() {
                    
                mainPane.setStyle(String.format("primary-color: #%s; secondary-color: #%s; -fx-background-color: primary-color; /* primary ali secondary, idk yet */", 
                primary, secondary)); // "https://htmlcolorcodes.com/color-picker/" <- tuki pejt napis "EEEDDD" pa scroll po barvah za primary
                                      //                                            <- tuki pejt napis "FFFEEE" pa scroll the same kot primary za secondary 
                                      //                                            (glej HSL, 1st value the same: fixed je 7% in 90% / 97%)
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // naredimo change listener, da ko selecta user, se spremeni label value
        // to ima 2 funckionalnosti
        // 1) user vidi komu poslija, moznosti: [PUBLIC, <name of client>]
        // 2) posledicno, uporabimo ta value iz labela, ko delamo JSON, da poslejmo serverju. Label value nam pove, name of reciever
        this.mainListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {

                String reciever = mainListView.getSelectionModel().getSelectedItem();
                
                if (reciever.equals(clientName))  chatLabel.setText("PUBLIC");
                else chatLabel.setText(reciever);
            }
        });

        this.mainListView.setCellFactory(lv -> {

            ListCell<String> cell = new ListCell<>();

            // naredimo userMenu, ki pokaze actions za user
            ContextMenu userOptions = new ContextMenu();
            MenuItem showHistory = new MenuItem("Show History");
            showHistory.setOnAction(event -> {

                Thread requestSender = new Thread() {

                    public void run() {

                        Platform.runLater(new Runnable() {

                            public void run() {
        
                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");
                                LocalDateTime now = LocalDateTime.now();
        
                                try {
                                    out.writeUTF(new Request().createRequest(1, clientName, cell.getItem(), dtf.format(now).toString()));
                                    // v tem primeru je "otherInfo"= ime od katerega hoce user dobiti history
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                };
                requestSender.run();
            });
            userOptions.getItems().addAll(showHistory);

            // little magic :)
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(userOptions);
                }
            });
            return cell;
        });

        // naredimo se colorsMenu, ki se prikaze ob desnem clicku in omogoca izbiro barve
        this.colorsMenu = new ContextMenu();

        ImageView imageColor1 = new ImageView(getClass().getResource("images/color1.png").toExternalForm());
        imageColor1.setFitHeight(20);
        imageColor1.setFitWidth(20);
        MenuItem color1 = new MenuItem("Buttery Yellow", imageColor1);
        color1.setOnAction(event -> {

            Thread color21Changer = new Thread() {

                public void run() {

                    Platform.runLater(new Runnable() { // yellow / orange

                        public void run() {

                            changeColors("EEEDDD", "FFFEEE");
                        }
                    });
                }
            };
            color21Changer.run();
        });

        ImageView imageColor2 = new ImageView(getClass().getResource("images/color2.png").toExternalForm());
        imageColor2.setFitHeight(20);
        imageColor2.setFitWidth(20);
        MenuItem color2 = new MenuItem("Twilight Blue", imageColor2);
        color2.setOnAction(event -> {

            Thread color22Changer = new Thread() { // cyan

                public void run() {

                    Platform.runLater(new Runnable() {

                        public void run() {

                            changeColors("DDEEEE", "EEFFFF");
                        }
                    });
                }
            };
            color22Changer.run();
        });

        ImageView imageColor3 = new ImageView(getClass().getResource("images/color3.png").toExternalForm());
        imageColor3.setFitHeight(20);
        imageColor3.setFitWidth(20);
        MenuItem color3 = new MenuItem("Pearly Red", imageColor3);
        color3.setOnAction(event -> {

            Thread color23Changer = new Thread() { // red

                public void run() {

                    Platform.runLater(new Runnable() {

                        public void run() {

                            changeColors("EEDDDD", "FFEEEE");
                        }
                    });
                }
            };
            color23Changer.run();
        });

        ImageView imageColor4 = new ImageView(getClass().getResource("images/color4.png").toExternalForm());
        imageColor4.setFitHeight(20);
        imageColor4.setFitWidth(20);
        MenuItem color4 = new MenuItem("Hint Of Green", imageColor4);
        color4.setOnAction(event -> {

            Thread color24Changer = new Thread() { // green

                public void run() {

                    Platform.runLater(new Runnable() {

                        public void run() {

                            changeColors("DDEEDD", "EEFFEE");
                        }
                    });
                }
            };
            color24Changer.run();
        });

        ImageView imageColor5 = new ImageView(getClass().getResource("images/color5.png").toExternalForm());
        imageColor5.setFitHeight(20);
        imageColor5.setFitWidth(20);
        MenuItem color5 = new MenuItem("Mercury Purple", imageColor5);
        color5.setOnAction(event -> {

            Thread color25Changer = new Thread() { // hard blue

                public void run() {

                    Platform.runLater(new Runnable() {

                        public void run() {

                            changeColors("E9DDEE", "FAEEFF");
                        }
                    });
                }
            };
            color25Changer.run();
        });

        colorsMenu.getItems().addAll(color1, color2, color3, color4, color5);
    }
}

// wait for messages from the chat server and print the out
class ChatClientMessageReceiver extends Thread {
	private DataInputStream in;
	private ChatClient client;

	public ChatClientMessageReceiver(DataInputStream in, ChatClient client) {
		this.in = in;
		this.client = client;
	}

	public void run() {
		try {
			String message;
			while ((message = this.in.readUTF()) != null) { // read new message

                // MAIN
				System.out.println(message); // print the message to the console

                if (this.client.logedIn) { // MAIN GUI APP

                    this.client.addMessage(message);
                }
                else  this.client.messageFromServer = message; // SWING LOGIN

			}
		} catch (Exception e) {
			System.err.println("[system] could not read message");
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
