package view;

import controller.Client;
import controller.MainMenuController;
import controller.SoundEffectPlayer;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.gui.*;
import models.message.ChatMessage;


public class GlobalChatDialog {
    private static GlobalChatDialog ourInstance = new GlobalChatDialog();

    private VBox chatMessages = new VBox();
    private DialogBox dialogBox = new DialogBox();
    private NormalField normalField = new NormalField("Message");

    private GlobalChatDialog(){
        ScrollPane scrollPane = new ScrollPane(chatMessages);
        OrangeButton sendButton = new OrangeButton("send",
                event -> MainMenuController.getInstance().sendChatMessage(normalField.getText()),
                SoundEffectPlayer.SoundName.select);
        dialogBox.getChildren().add(scrollPane,new HBox(normalField, sendButton));
    }
    public void show() {
        DialogContainer dialogContainer = new DialogContainer(Client.getInstance().getCurrentShow().root, dialogBox);

        dialogContainer.show();
        dialogBox.makeClosable(dialogContainer);
    }

    public static GlobalChatDialog getInstance() {
        return ourInstance;
    }

    public void addMessage(ChatMessage chatMessage){
        chatMessages.getChildren().add(new DialogText(chatMessage.getSenderUsername()+"\n"+chatMessage.getText()));
    }
}
