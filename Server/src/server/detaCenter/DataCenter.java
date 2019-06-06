package server.detaCenter;

import server.Server;
import server.clientPortal.ClientPortal;
import server.clientPortal.models.JsonConverter;
import server.clientPortal.models.message.Message;
import server.detaCenter.models.account.Account;
import server.detaCenter.models.account.Collection;
import server.detaCenter.models.account.TempAccount;
import server.detaCenter.models.card.Card;
import server.detaCenter.models.card.CardType;
import server.detaCenter.models.sorter.LeaderBoardSorter;
import server.detaCenter.models.sorter.StoriesSorter;
import server.exceptions.ClientException;
import server.exceptions.LogicException;
import server.gameCenter.models.game.Story;
import server.gameCenter.models.game.TempStory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DataCenter extends Thread {
    private static final String ACCOUNTS_PATH = "jsonData/accounts";
    private static final String[] CARDS_PATHS = {
            "jsonData/heroCards",
            "jsonData/minionCards",
            "jsonData/spellCards",
            "jsonData/itemCards/collectible",
            "jsonData/itemCards/usable"};
    private static final String FLAG_PATH = "jsonData/itemCards/flag/Flag.item.card.json";
    private static final String STORIES_PATH = "jsonData/stories";

    private static DataCenter ourInstance = new DataCenter();

    private HashMap<Account, String> accounts = new HashMap<>();//Account -> ClientName
    private HashMap<String, Account> clients = new HashMap<>();//clientName -> Account
    private Collection originalCards = new Collection();
    private ArrayList<Card> collectibleItems = new ArrayList<>();
    private Card originalFlag;
    private ArrayList<Story> stories = new ArrayList<>();

    public static DataCenter getInstance() {
        return ourInstance;
    }

    private DataCenter() {
    }

    @Override
    public void run() {
        Server.getInstance().serverPrint("Starting DataCenter...");
        Server.getInstance().serverPrint("Reading Cards...");
        readAllCards();
        Server.getInstance().serverPrint("Reading Accounts...");
        readAccounts();
        Server.getInstance().serverPrint("Reading Stories...");
        readStories();
    }

    public Account getAccount(String username) {
        if (username == null) {
            Server.getInstance().serverPrint("Null Username In getAccount.");
            return null;
        }
        for (Account account : accounts.keySet()) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return account;
            }
        }
        return null;
    }

    public String getClientName(String username) {
        Account account = getAccount(username);
        if (account == null)
            return null;
        return accounts.get(account);
    }

    public void register(Message message) throws LogicException {
        if (message.getAccountFields().getUsername() == null || message.getAccountFields().getUsername().length() < 2
                || getAccount(message.getAccountFields().getUsername()) != null) {
            throw new ClientException("Invalid Username!");
        } else if (message.getAccountFields().getPassword() == null || message.getAccountFields().getPassword().length() < 4) {
            throw new ClientException("Invalid Password!");
        } else {
            Account account = new Account(message.getAccountFields().getUsername(), message.getAccountFields().getPassword());
            accounts.put(account, null);
            saveAccount(account);
            Server.getInstance().serverPrint(message.getAccountFields().getUsername() + " Is Created!");
            login(message);
        }
    }

    public void login(Message message) throws LogicException {
        if (message.getAccountFields().getUsername() == null || message.getSender() == null) {
            throw new ClientException("invalid message!");
        }
        Account account = getAccount(message.getAccountFields().getUsername());
        if (!ClientPortal.getInstance().hasThisClient(message.getSender())) {
            throw new LogicException("Client Wasn't Added!");
        } else if (account == null) {
            throw new ClientException("Username Not Found!");
        } else if (!account.getPassword().equalsIgnoreCase(message.getAccountFields().getPassword())) {
            throw new ClientException("Incorrect PassWord!");
        } else if (accounts.get(account) != null) {
            throw new ClientException("Selected Username Is Online!");
        } else if (clients.get(message.getSender()) != null) {
            throw new ClientException("Your Client Has Logged In Before!");
        } else {
            accounts.replace(account, message.getSender());
            clients.replace(message.getSender(), account);
            Server.getInstance().addToSendingMessages(Message.makeAccountCopyMessage(
                    Server.getInstance().serverName, message.getSender(), account, message.getMessageId()));
            Server.getInstance().serverPrint(message.getSender() + " Is Logged In");
        }
    }

    public void loginCheck(Message message) throws LogicException {
        if (message.getSender() == null) {
            throw new ClientException("invalid message!");
        }
        if (!clients.containsKey(message.getSender())) {
            throw new LogicException("Client Wasn't Added!");
        }
        if (clients.get(message.getSender()) == null) {
            throw new ClientException("Client Was Not LoggedIn");
        }
    }

    public void logout(Message message) throws LogicException {
        loginCheck(message);
        accounts.replace(clients.get(message.getSender()), null);
        clients.replace(message.getSender(), null);
        Server.getInstance().serverPrint(message.getSender() + " Is Logged Out.");
        //TODO:Check online games
        Server.getInstance().addToSendingMessages(Message.makeDoneMessage(Server.getInstance().serverName, message.getSender(), message.getMessageId()));
    }

    public void createDeck(Message message) throws LogicException {
        loginCheck(message);
        Account account = clients.get(message.getSender());
        account.addDeck(message.getOtherFields().getDeckName());
        Server.getInstance().addToSendingMessages(Message.makeAccountCopyMessage(
                Server.getInstance().serverName, message.getSender(), account, message.getMessageId()));
        saveAccount(account);
    }

    public void removeDeck(Message message) throws LogicException {
        loginCheck(message);
        Account account = clients.get(message.getSender());
        account.deleteDeck(message.getOtherFields().getDeckName());
        Server.getInstance().addToSendingMessages(Message.makeAccountCopyMessage(
                Server.getInstance().serverName, message.getSender(), account, message.getMessageId()));
        saveAccount(account);
    }

    public void addToDeck(Message message) throws LogicException {
        loginCheck(message);
        Account account = clients.get(message.getSender());
        account.addCardToDeck(message.getOtherFields().getMyCardId(), message.getOtherFields().getDeckName());
        Server.getInstance().addToSendingMessages(Message.makeAccountCopyMessage(
                Server.getInstance().serverName, message.getSender(), account, message.getMessageId()));
        saveAccount(account);
    }

    public void removeFromDeck(Message message) throws LogicException {
        loginCheck(message);
        Account account = clients.get(message.getSender());
        account.removeCardFromDeck(message.getOtherFields().getMyCardId(), message.getOtherFields().getDeckName());
        Server.getInstance().addToSendingMessages(Message.makeAccountCopyMessage(
                Server.getInstance().serverName, message.getSender(), account, message.getMessageId()));
        saveAccount(account);
    }

    public void selectDeck(Message message) throws LogicException {
        loginCheck(message);
        Account account = clients.get(message.getSender());
        account.selectDeck(message.getOtherFields().getDeckName());
        Server.getInstance().addToSendingMessages(Message.makeAccountCopyMessage(
                Server.getInstance().serverName, message.getSender(), account, message.getMessageId()));
        saveAccount(account);
    }

    public void buyCard(Message message) throws LogicException {
        loginCheck(message);
        Account account = clients.get(message.getSender());
        account.buyCard(message.getOtherFields().getCardName(), originalCards.getCard(message.getOtherFields().getCardName()).getPrice(), originalCards);
        Server.getInstance().addToSendingMessages(Message.makeAccountCopyMessage(
                Server.getInstance().serverName, message.getSender(), account, message.getMessageId()));
        saveAccount(account);
    }

    public void sellCard(Message message) throws LogicException {
        loginCheck(message);
        Account account = clients.get(message.getSender());
        account.sellCard(message.getOtherFields().getMyCardId());
        Server.getInstance().addToSendingMessages(Message.makeAccountCopyMessage(
                Server.getInstance().serverName, message.getSender(), account, message.getMessageId()));
        saveAccount(account);
    }

    private void readAccounts() {
        File[] files = new File(ACCOUNTS_PATH).listFiles();
        if (files != null) {
            for (File file : files) {
                TempAccount account = loadFile(file, TempAccount.class);
                if (account == null) continue;
                Account newAccount = new Account(account);
                accounts.put(newAccount, null);
            }
        }
        Server.getInstance().serverPrint("Accounts Loaded");
    }

    private void readAllCards() {
        for (String path : CARDS_PATHS) {
            File[] files = new File(path).listFiles();
            if (files != null) {
                for (File file : files) {
                    Card card = loadFile(file, Card.class);
                    if (card == null) continue;
                    if (card.getType() == CardType.COLLECTIBLE_ITEM) {
                        collectibleItems.add(card);
                    } else {
                        originalCards.addCard(card);
                    }
                }
            }
        }
        originalFlag = loadFile(new File(FLAG_PATH), Card.class);
        Server.getInstance().serverPrint("Original Cards Loaded");
    }

    private void readStories() {
        File[] files = new File(STORIES_PATH).listFiles();
        if (files != null) {
            for (File file : files) {
                TempStory story = loadFile(file, TempStory.class);
                if (story == null) continue;

                stories.add(new Story(story, originalCards));
            }
        }
        stories.sort(new StoriesSorter());
        Server.getInstance().serverPrint("Stories Loaded");
    }

    public void saveAccount(Account account) {
        String accountJson = JsonConverter.toJson(new TempAccount(account));
        try {
            FileWriter writer = new FileWriter(ACCOUNTS_PATH + "/" + account.getUsername() + ".account.json");
            writer.write(accountJson);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T> T loadFile(File file, Class<T> classOfT) {
        try {
            return JsonConverter.fromJson(new BufferedReader(new FileReader(file)), classOfT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Story> getStories() {
        return stories;
    }

    public HashMap<Account, String> getAccounts() {
        return accounts;
    }

    public HashMap<String, Account> getClients() {
        return clients;
    }

    public Collection getOriginalCards() {
        return originalCards;
    }

    public ArrayList<Card> getCollectibleItems() {
        return collectibleItems;
    }

    public Card getOriginalFlag() {
        return originalFlag;
    }

    public Account[] getLeaderBoard() throws ClientException {
        if (accounts.size() == 0) {
            throw new ClientException("leader board is empty");
        }
        /*Account[] leaderBoard = new Account[accounts.size()];
        int counter = 0;
        for (Account account : accounts.keySet()) {
            leaderBoard[counter] = account;
            counter++;
        }*/
        Account[] leaderBoard = accounts.keySet().toArray(Account[]::new);
        Arrays.sort(leaderBoard, new LeaderBoardSorter());
        return leaderBoard;
    }
}