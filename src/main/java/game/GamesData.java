package game;

/**
 * Created by benbash on 1/10/16.
 */
public interface GamesData {
    public void startNewGame(String roomName, int numberOfPlayers);
    public void textResp(String answer, String roomName, String playerName);
    public void selectResp(String answer, String roomName, String playerName);
    public void quit(String roomName);
}
