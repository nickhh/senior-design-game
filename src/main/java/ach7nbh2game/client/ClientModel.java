package ach7nbh2game.client;

import ach7nbh2game.client.adapters.IModelToView;
import ach7nbh2game.main.Constants;
import ach7nbh2game.network.adapters.IClientToServer;
import ach7nbh2game.network.packets.ClientAction;
import ach7nbh2game.network.packets.GameState;
import ach7nbh2game.network.packets.PlayerInfo;
import ach7nbh2game.util.Logger;
import ach7nbh2game.util.Utility;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientModel {

    private IClientToServer server;

    private IModelToView view;

    private PlayerInfo playerInfo = new PlayerInfo();

    private boolean inGame = false; // TODO state design pattern

    private Set<Thread> infoRequestThreads = new HashSet<>();

    public ClientModel (IClientToServer serverIn, IModelToView viewIn) {

        System.out.println("making new ClientModel...");

        server = serverIn;
        view = viewIn;

    }

    public void start () {

        System.out.println("starting the ClientModel!");

        String username = "";
        while (username.trim() == "")
        	username = view.askForUsername();
        playerInfo.setUsername(username);
        playerInfo.setIcon(username.toUpperCase().toCharArray()[0]);

        boolean failedOnce = false;
        while (!server.isConnected()) {
            try {
                if (!failedOnce) {
                    server.connectTo(0, view.askForServerIP(), playerInfo);
                } else {
                    server.connectTo(0, view.askForServerIPFailed(), playerInfo);
                }
            } catch (IOException e) {
                System.out.println("failed to connect to server");
                e.printStackTrace();
                failedOnce = true;
            }
        }

        server.requestLobbies(playerInfo.getID());

    }

    public void updateState (GameState state) {

        //Logger.Singleton.log(this, 0, "updateState:");
        //Logger.Singleton.log(this, 1, "state = " + state);

        view.updateState(state);

    }
    
    private boolean waitingForInput = false;
    private int myLobby;
    private int selected = 0;

    public void selectUp() {
        if (selected > 0) {
            selected--;
        }
    }

    public void selectDown() {
        if (selected < Constants.clientMapHeight) {
            selected++;
        }
    }

    public void updateLobbyList (
            final Map<Integer, String> lobbies,
            final Map<Integer, String> players,
            final Map<Integer, Set<Integer>> lobbyToPlayers) {

        Logger.Singleton.log(this, 0, "updateLobbyList:");
        Logger.Singleton.log(this, 1, "lobbies = " + lobbies);
        Logger.Singleton.log(this, 1, "players = " + players);
        Logger.Singleton.log(this, 1, "lobbyToPlayers = " + lobbyToPlayers);

        if (!inGame) {

            // NORMAL BEHAVIOR
            Object[] lobbyIDs = lobbies.keySet().toArray();

            String prompt = "";
            prompt += "Lobbies";
            for (int i = 0; i < (Constants.clientMapWidth/2)-"lobbies".length()-1; i++) {
                prompt += " ";
            }
            prompt += "|";
            prompt += "Players\n";

            //final Set<Integer> myLobbies = new HashSet<Integer>();
            final Map<Integer, Integer> smallIntToLobbyID = new HashMap<Integer, Integer>();

            if (lobbies.isEmpty()) {

                for (int i = 0; i < Constants.clientMapHeight-2; i++) {
                    for (int j = 0; j < Constants.clientMapWidth/2-1; j++) {
                        prompt += " ";
                    }
                    prompt += "|\n";
                }

            } else {

                for (int i = 0; i < Constants.clientMapHeight-2; i++) {
                    if (selected == i) {
                        prompt += "*";
                    } else {
                        prompt += " ";
                    }
                    if (i < lobbyIDs.length) {
                        char[] lname = lobbies.get(lobbyIDs[i]).toCharArray();
                        for (int c = 0; c < Constants.clientMapWidth / 2 - 2; c++) {
                            if (c < lname.length) {
                                prompt += lname[c];
                            } else {
                                prompt += " ";
                            }
                        }
                    } else {
                        for (int c = 0; c < Constants.clientMapWidth / 2 - 2; c++) {
                            prompt += " ";
                        }
                    }
                    prompt += "| ";
                    Object[] pInL = lobbyToPlayers.get(lobbyIDs[selected]).toArray();
                    if (pInL.length > 0 && i < pInL.length) {
                        for (int p = 0; p < Constants.clientMapWidth - prompt.length(); p++) {
                            if (p < players.get(pInL[i]).length()) {
                                prompt += players.get(pInL[i]);
                            } else {
                                prompt += " ";
                            }
                        }
                    }
                }

//                int i = 0;
//                for (int lobbyID : lobbies.keySet()) {
//
//                    smallIntToLobbyID.put(i, lobbyID);
//                    prompt += i + ": " + lobbies.get(lobbyID) + "\n";
//                    i++;
//
//                    for (int playerID : lobbyToPlayers.get(lobbyID)) {
//                        if (playerID == playerInfo.getID()) {
//                            myLobby = lobbyID;
//                            prompt += "me! (" + playerInfo.getUsername() + ")\n";
//                        } else {
//                            String username = players.get(playerID);
//                            prompt += "player: " + username + "\n";
//                        }
//                    }
//
//                }

            }

            prompt += "CREATE LOBBY: ";

            final String promptFinal = prompt;

            if (waitingForInput) {
            	
            	view.updateThing(promptFinal);
            	
            } else {

                Thread newThread = new Thread() {
                    public void run() {

                        waitingForInput = true;

                        String action = view.askForThing(promptFinal, "", ClientView.VerticalAlignment.CENTER,
                                ClientView.HorizontalAlignment.LEFT);

                        if (action.equals("")) {

//                            Logger.Singleton.log(ClientModel.this, 0, "updateLobbyList: requesting lobbies...");
//
//                            server.requestLobbies(playerInfo.getID());
                            if (!lobbies.isEmpty()) {
                                server.joinLobby(playerInfo.getID(), (int)lobbyIDs[selected]);
                            } else {
                                server.requestLobbies((playerInfo.getID()));
                            }

                        } else {

                            if (Utility.isInteger(action)) {

                                int smallInt = Integer.parseInt(action);
                                if (smallIntToLobbyID.containsKey(smallInt)) {
                                    int lobbyID = smallIntToLobbyID.get(smallInt);

                                    if (myLobby == lobbyID) {

                                        Logger.Singleton.log(ClientModel.this, 0, "updateLobbyList: starting game " + lobbyID + "...");

                                        server.startGame(lobbyID);

                                    } else {

                                        Logger.Singleton.log(ClientModel.this, 0, "updateLobbyList: joining lobby " + lobbyID + "...");
                                        Logger.Singleton.log(ClientModel.this, 1, "myLobby = " + myLobby);

                                        server.joinLobby(playerInfo.getID(), lobbyID);

                                    }

                                } else {

                                    server.requestLobbies(playerInfo.getID());

                                }

                            } else if (Utility.isAlphanumeric(action)) {

                                Logger.Singleton.log(ClientModel.this, 0, "updateLobbyList: creating lobby " + action + "...");

                                server.createNewLobby(playerInfo.getID(), action);

                            } else {

                                Logger.Singleton.log(ClientModel.this, 0, "updateLobbyList: invalid input. trying again...");

                                updateLobbyList(lobbies, players, lobbyToPlayers);

                                server.requestLobbies(playerInfo.getID());

                            }

                        }

                        waitingForInput = false;

                    }
                };

                infoRequestThreads.add(newThread);
                newThread.start();

            }

//                }
//            };
//
//            infoRequestThreads.add(newThread);
//            newThread.start();

        }

    }

    public void enterGame () {

        Logger.Singleton.log(this, 0, "enterGame:");

        if (!inGame) {

            for (Thread thread : infoRequestThreads) {
                thread.interrupt();
            }

            inGame = true;
            waitingForInput = false;

        } else {
            // TODO what if in multiple lobbies and a second one starts?
        }

    }

    public void endGame(PlayerInfo client) {
        inGame = false;
        view.endGame(client);
        try {Thread.sleep(5000);} catch (InterruptedException e) {}
        view.clearMenus();
        server.requestLobbies(playerInfo.getID());
        SoundEffect.MENU_BGM.loop();
    }

    //public void move(Direction direction) {
    //
    //    Logger.Singleton.log(this, 0, "move:");
    //    Logger.Singleton.log(this, 1, "direction = " + direction);
    //
    //    if (inGame) {
    //        server.move(playerInfo.getID(), direction);
    //    }
    //
    //}

    public void action(ClientAction actionIn) {
        if (inGame) {
            server.performAction(playerInfo.getID(), actionIn);
        }
    }

}
