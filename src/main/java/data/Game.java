package data;

/**
 * each game in the server will implement this interface
 * the server data structure will call specific function
 * when it needs to handle some server commands
 */
public interface Game {
    /**
     * starting a new game for specific number of players
     *
     * @param roomName room name
     * @param numberOfPlayers number of players in game
     * */
    void startNewGame(String roomName, int numberOfPlayers);

    /**
     * handle a command that sends by the client. the command
     * is sending in the format: "COMMAND <MESSAGE>"
     *
     * @param command the command
     * @param msg the message
     * @param playerName sender player name
     * @param room room name
     * */
    void handleCommand(String command, String msg, String playerName, String room);
}
