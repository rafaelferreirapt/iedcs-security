package player;

import java.awt.Insets;
import player.api.Utils;
import player.api.Requests;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import player.api.Result;
import player.security.CitizenCard;
import player.security.ComputerDetails;
import player.security.PBKDF2;

public class BackendFrontPageController implements Initializable {
    @FXML private Label name = new Label();
    @FXML private Label email = new Label();
    @FXML private Label username = new Label();
    @FXML private Label citizenAssociated = new Label();
    @FXML private Button associate = new Button();
    @FXML private Button disassociate = new Button();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            JSONObject user = Requests.getUser();
            name.setText("Name: " + user.getString("first_name") + " " + user.getString("last_name"));
            email.setText("E-mail: " + user.getString("email"));
            username.setText("Username: " + user.getString("username"));
            
            if(user.getBoolean("has_cc")){
                citizenAssociated.setVisible(true);
                associate.setVisible(false);
                disassociate.setVisible(true);
                
            }else{
                citizenAssociated.setVisible(false);
                associate.setVisible(true);
                disassociate.setVisible(false);
            }
        } catch (JSONException ex) {
            Logger.getLogger(BackendFrontPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    @FXML
    private void handleDisassociateCitizenCard(ActionEvent event){
        try {            
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("public_key", "");
            parameters.put("first_name", "");
            parameters.put("last_name", "");
            parameters.put("citizen_card_serial_number", "");
            parameters.put("disassociate", "true");
            
            // Create the custom dialog.
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Verification");
            dialog.setHeaderText("We need your password to disassociate CC!");

            // Set the button types.
            ButtonType loginButtonType = new ButtonType("Verificate", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

            // Create the username and password labels and fields.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            PasswordField password = new PasswordField();
            password.setPromptText("Password");

            grid.add(new Label("Password:"), 0, 1);
            grid.add(password, 1, 1);

            // Enable/Disable login button depending on whether a username was entered.
            Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
            dialog.getDialogPane().setContent(grid);

            // Traditional way to get the response value.
            Optional<ButtonType> result = dialog.showAndWait();
            if (!result.get().getButtonData().toString().equals("CANCEL_CLOSE")){
                if(password.getText().isEmpty()){
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Ups!");
                    alert.setHeaderText(null);
                    alert.setContentText("The password field cannot be empty!");
                    alert.showAndWait();

                }else{
                    PBKDF2 password_pbkdf2 = new PBKDF2(password.getText(), password.getText(), 500);
                    String pass = new String (Base64.getEncoder().encode(password_pbkdf2.read(32)));
                    parameters.put("password", pass);
                    Result r = Requests.postJSON(Requests.CITIZEN_AUTHENTICATION, parameters);

                    if(r.getStatusCode()==200){
                        citizenAssociated.setVisible(false);
                        handleLogout(null);
                    }else if(r.getStatusCode()==400){
                        if(!citizenAssociated.visibleProperty().get()){
                            citizenAssociated.setVisible(false);
                        }

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Ups!");
                        alert.setHeaderText(null);
                        JSONObject r_o = (JSONObject)r.getResult();
                        r_o = r_o.getJSONObject("message");

                        Iterator itr = r_o.keys();

                        String message = "";

                        while(itr.hasNext()){
                            String key = (String)itr.next();
                            JSONArray field = r_o.getJSONArray(key);
                            String line = key.replaceAll("_", " ");

                            message += Character.toUpperCase(line.charAt(0)) + line.substring(1) + ":\n";

                            for(int i=0; i<field.length(); i++){
                                message += field.getString(i) + "\n";
                            }

                        }
                        alert.setContentText(message);
                        alert.showAndWait();
                    }else{
                        citizenAssociated.setVisible(false);
                    }    
                }
            }
        } catch (ProtocolException ex) {
            Logger.getLogger(BackendFrontPageController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | JSONException ex) {
            Logger.getLogger(BackendFrontPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void handleWebStoreBtn(ActionEvent event) {
        try {
            Utils.openBrowser(IEDCSPlayer.getRegisterUrl());
        } catch (IOException ex) {
            Logger.getLogger(BackendFrontPageController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(BackendFrontPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void handleGetCitizenCard(ActionEvent event){
        try {
            String pk_pem = CitizenCard.getPublicKeyPem();
            
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("public_key", pk_pem);
            parameters.put("first_name", CitizenCard.getGivenName());
            parameters.put("last_name", CitizenCard.getSurName());
            parameters.put("citizen_card_serial_number", CitizenCard.getSerialNumber());
            parameters.put("disassociate", "false");

            // Create the custom dialog.
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Verification");
            dialog.setHeaderText("We need your password!\nThe CC that is being associated: " + CitizenCard.getSerialNumber().split("BI")[1]);

            // Set the button types.
            ButtonType loginButtonType = new ButtonType("Verificate", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

            // Create the username and password labels and fields.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            PasswordField password = new PasswordField();
            password.setPromptText("Password");

            grid.add(new Label("Password:"), 0, 1);
            grid.add(password, 1, 1);

            // Enable/Disable login button depending on whether a username was entered.
            Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
            dialog.getDialogPane().setContent(grid);

            // Traditional way to get the response value.
            Optional<ButtonType> result = dialog.showAndWait();
            if (!result.get().getButtonData().toString().equals("CANCEL_CLOSE")){
                if(password.getText().isEmpty()){
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Ups!");
                    alert.setHeaderText(null);
                    alert.setContentText("The password field cannot be empty!");
                    alert.showAndWait();
                }else{
                    PBKDF2 password_pbkdf2 = new PBKDF2(password.getText(), password.getText(), 500);
                    String pass = new String (Base64.getEncoder().encode(password_pbkdf2.read(32)));
                    parameters.put("password", pass);
                    Result r = Requests.postJSON(Requests.CITIZEN_AUTHENTICATION, parameters);

                    if(r.getStatusCode()==200){
                        citizenAssociated.setVisible(true);
                        handleLogout(null);
                    }else if(r.getStatusCode()==400){
                        if(!citizenAssociated.visibleProperty().get()){
                            citizenAssociated.setVisible(false);
                        }

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Ups!");
                        alert.setHeaderText(null);
                        JSONObject r_o = (JSONObject)r.getResult();
                        r_o = r_o.getJSONObject("message");

                        Iterator itr = r_o.keys();

                        String message = "";

                        while(itr.hasNext()){
                            String key = (String)itr.next();
                            JSONArray field = r_o.getJSONArray(key);
                            String line = key.replaceAll("_", " ");

                            message += Character.toUpperCase(line.charAt(0)) + line.substring(1) + ":\n";

                            for(int i=0; i<field.length(); i++){
                                message += field.getString(i) + "\n";
                            }

                        }
                        alert.setContentText(message);
                        alert.showAndWait();
                    }else{
                        citizenAssociated.setVisible(false);
                    }    
                }
            }
        } catch (ProtocolException ex) {
            Logger.getLogger(BackendFrontPageController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | JSONException ex) {
            Logger.getLogger(BackendFrontPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void handleMyBooks(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("MyBooksPage.fxml"));
            Scene scene = new Scene(root);
            Player.thestage.setScene(scene);
        } catch (IOException ex) {
            Logger.getLogger(FrontPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void handleLogout(ActionEvent event){
        Result r = null;
        try {
            r = Requests.logout();
        } catch (IOException | JSONException ex) {
            Logger.getLogger(BackendFrontPageController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(r.getStatusCode()==204){
            try {
                Parent root = FXMLLoader.load(getClass().getResource("FrontPage.fxml"));
                Scene scene = new Scene(root);
                Player.thestage.setScene(scene);
            } catch (IOException ex) {
                Logger.getLogger(FrontPageController.class.getName()).log(Level.SEVERE, null, ex);
            }       
        }
    }
}
