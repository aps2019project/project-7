package client.models.menus;

import client.Client;
import client.models.message.Message;
import client.view.View;
import client.view.request.InputException;

public class AccountMenu extends Menu {
    private static AccountMenu ACCOUNT_MENU;

    private AccountMenu() {
    }

    public static AccountMenu getInstance() {
        if (ACCOUNT_MENU == null) {
            ACCOUNT_MENU = new AccountMenu();
        }
        return ACCOUNT_MENU;
    }

    public String getAccountMenuHelp() {
        return  "\"create account [userName]\"\n" +
                "\"login [userName]\"\n" +
                "\"show leaderboard\"\n" +
                "\"save\"\n" +
                "\"logout\"";

    }

    public void register(Client client, String serverName, String userName, String password) throws InputException {
        client.addToSendingMessages(
                Message.makeRegisterMessage(
                        client.getClientName(), serverName, userName, password, 0)
        );
        client.sendMessages();
        if (!client.isUserNameValid()) {
            throw new InputException("invalid UserName");
        }
        client.setCurrentMenu(MainMenu.getInstance());
    }

    public void login(Client client, String serverName, String userName, String password) throws InputException {
        client.addToSendingMessages(
                Message.makeLogInMessage(client.getClientName(), serverName, userName, password, 0)
        );
        client.sendMessages();
        if (!client.isUserNameValid()) {
            throw new InputException("invalid UserName");
        }
        if (!client.isPasswordValid()) {
            throw new InputException("password is invalid");
        }
        client.setCurrentMenu(MainMenu.getInstance());
    }


    public void showLeaderBoard(Client client, String serverName) {
        client.updateLeaderBoard( serverName);
        View.getInstance().printLeaderBoard(client);
    }


    public void help() {
        View.getInstance().printAccountHelp();
    }

}