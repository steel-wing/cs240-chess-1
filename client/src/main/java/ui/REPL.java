package ui;

import ui.menus.Gameplay;
import ui.menus.Postlogin;
import ui.menus.Prelogin;
import webSocketMessages.serverMessages.ServerMessage;
import websocket.MessageHandler;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class REPL implements MessageHandler {
    private final ChessClient client;
    private State oldstate = null;

    public REPL(int serverPort) {
        client = new ChessClient(serverPort, this);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String result = "";

        // primary loop machine
        while (!result.equals("Exiting...")) {

            // handles the different kinds of ways to join a game
            String joinType = switch (client.team) {
                case "WHITE" -> "the White Team";
                case "BLACK" -> "the Black Team";
                default -> "an observer";
            };

            // handle extending salutations when relevant
            if (oldstate != client.state) {
                String welcomeString = switch (client.state) {
                    case LOGGEDOUT -> "Welcome to the CS 240 Chess Client, by Davis Wing\n" + Prelogin.help();
                    case LOGGEDIN -> "Welcome " + client.username + " \n" + Postlogin.help();
                    case GAMEPLAY -> "Joined Game \"" + client.game.gameName() + "\" as " + joinType + "\n" + Gameplay.redraw(client);
                };
                System.out.println(welcomeString);
                oldstate = client.state;
            }

            // print out the prompt for the next action and listen
            System.out.print(SET_TEXT_COLOR_WHITE + SET_TEXT_BOLD + ">>> " + RESET);
            String line = scanner.nextLine();

            // switch between menus
            try {
                result = switch (client.state) {
                    case LOGGEDOUT -> client.prelogin(line);
                    case LOGGEDIN -> client.postlogin(line);
                    case GAMEPLAY -> client.gameplay(line);
                };

                // print out what came from above
                System.out.print(SET_TEXT_COLOR_WHITE + SET_TEXT_BOLD + result + "\n");

            } catch (Throwable exception) {
                System.out.print(exception.getMessage());
            }
        }
    }

    // this is what handles the different incoming ws messages from the server,
    // since the repl acts as a messagehandler
    @Override
    public void notify(ServerMessage message) {
//        switch (message.getServerMessageType()) {
//            case LOAD_GAME -> client.
//        }
//
//
//        System.out.println(SET_TEXT_COLOR_WHITE + SET_TEXT_BOLD + message.getMessage());
//        System.out.print(SET_TEXT_COLOR_WHITE + SET_TEXT_BOLD + ">>> " + RESET);
    }
}
