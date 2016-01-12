package data;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Data structure to save all the server data
 */
public class ServerDataStructure {
    private ConcurrentHashMap<String, Player> _Players;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<Player>> _Rooms;
    //list of supported games
    private ConcurrentHashMap<String, Game> _Games;
    //indicates witch game is running in each room
    private ConcurrentHashMap<String, Game> _GamePerRoom;

    public ServerDataStructure() {
        _Players = new ConcurrentHashMap<>();
        _Rooms = new ConcurrentHashMap<>();
        _Games = new ConcurrentHashMap<>();
        _GamePerRoom = new ConcurrentHashMap<>();

        //lets add some games to server
        _Games.put("BLUFFER", new BlufferDataStructure(this));
    }

    public boolean containsPlayer(String nickName) {
        return _Players.containsKey(nickName);
    }

    public boolean containsRoom(String roomName){
        return _Rooms.containsKey(roomName);
    }

    public String getRoom(String playerName){
        if ( _Players.containsKey(playerName)) {
            String RoomName = _Players.get(playerName).getRoomName();
            if (RoomName != null)
                return RoomName;
        }

        return "";
    }

    public synchronized Iterator<Player> getRoomsPlayers(String roomName){
        return _Rooms.get(roomName).iterator();
    }

    public void addPlayer(Player player) {
        _Players.put(player.getName(), player);
    }

    public boolean joinToRoom(String roomName, String name) {
        if (name.equals("") || _GamePerRoom.containsKey(getRoom(name)))
            return false;

        Player player = _Players.get(name);

        if (_Rooms.containsKey(roomName)) {
            //check if the player already in the requested room
            if (player.getRoomName().equals(roomName))
                return false;
            else {
                _Rooms.get(roomName).add(player);
            }
        }
        else {
            CopyOnWriteArrayList<Player> playersInRoom = new CopyOnWriteArrayList<Player>();
            playersInRoom.add(_Players.get(name));
            _Rooms.put(roomName, playersInRoom);
        }

        //the player is in other room - move it out
        if (!player.getRoomName().equals(""))
            _Rooms.get(player.getRoomName()).remove(player);
        //join to room
        player.setRoomName(roomName);

        return true;
    }


    public String getGamesList() {


        StringBuilder str = null;
        if (_Games.keySet().size() > 0) {
            str = new StringBuilder("SYSMSG LISTGAMES ACCEPTED");
            for (String s : _Games.keySet())
                str.append(" " + s);
        } else
            str = new StringBuilder("SYSMSG LISTGAMES REJECTED");

        return str.toString();
    }


    public boolean startGame(String game, String playerName) {
        if (_Games.containsKey(game) && !playerName.equals("") && !_Players.get(playerName).getRoomName().equals("")
                && !_GamePerRoom.containsKey(_Players.get(playerName).getRoomName())) {
            Game gamesData = _Games.get(game);
            _GamePerRoom.put(_Players.get(playerName).getRoomName(), gamesData);
            String RoomName = _Players.get(playerName).getRoomName();
            gamesData.startNewGame(RoomName, _Rooms.get(RoomName).size());
            return true;
        }
        return false;
    }



    public void quit(String name) {
        if (!_GamePerRoom.containsKey(_Players.get(name).getRoomName())) {
            _Rooms.get(_Players.get(name).getRoomName()).remove(_Players.get(name));
            _Players.get(name).call("SYSMSG QUIT ACCEPTED");
            /*Game game = _GamePerRoom.get(_Players.get(name).getRoomName());

            if (game != null)
                game.quit(_Players.get(name).getRoomName());*/
        } else
            _Players.get(name).call("SYSMSG QUIT REJECTED");
    }

    public void sendDataToAllUsersInRoom(String roomName, String msg) {
        for (Player p : _Rooms.get(roomName)) {
            p.call(msg);
        }
    }

    public void sendDataToUser(String n, String s) {
        _Players.get(n).call(s);
    }

    public void endGame(String roomName) {
        _GamePerRoom.remove(roomName);
    }

    public boolean handleGameCommands(String command, String msg, String playerName) {
        Game game = _GamePerRoom.get(_Players.get(playerName).getRoomName());

        if (game == null)
            return false;

        game.handleCommand(command, msg, playerName, getRoom(playerName));

        return true;
    }
}
