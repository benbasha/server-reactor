package game;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by benbash on 1/10/16.
 */
public class ServerDataStructure {
    ConcurrentHashMap<String, Player> _Players;
    ConcurrentHashMap<String, CopyOnWriteArrayList<Player>> _Rooms;
    ConcurrentHashMap<String, GamesData> _Games;
    ConcurrentHashMap<String, GamesData> _GamePerRoom;

    public ServerDataStructure() {
        _Players = new ConcurrentHashMap<>();
        _Rooms = new ConcurrentHashMap<>();
        _Games = new ConcurrentHashMap<>();
        _GamePerRoom = new ConcurrentHashMap<>();

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
            String RoomName = _Players.get(playerName).get_roomName();
            if (RoomName != null)
                return RoomName;
        }

        return "";
    }

    public synchronized Iterator<Player> getRoomsPlayers(String roomName){
        return _Rooms.get(roomName).iterator();
    }

    public void addPlayer(Player player) {
        _Players.put(player.get_Name(), player);
    }

    public boolean joinToRoom(String roomName, String name) {
        if (name.equals("") || _Games.contains(roomName))
            return false;

        Player player = _Players.get(name);

        if (_Rooms.containsKey(roomName)) {
            //check if the player already in the requested room
            if (player.get_roomName().equals(roomName))
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
        if (!player.get_roomName().equals(""))
            _Rooms.get(player.get_roomName()).remove(player);
        //join to room
        player.set_roomName(roomName);

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
        if (_Games.containsKey(game) && !playerName.equals("") && !_Players.get(playerName).get_roomName().equals("")) {
            GamesData gamesData = _Games.get(game);
            _GamePerRoom.put(_Players.get(playerName).get_roomName(), gamesData);
            String RoomName = _Players.get(playerName).get_roomName();
            gamesData.startNewGame(RoomName, _Rooms.get(RoomName).size());
            return true;
        }
        return false;
    }

    public boolean textResp(String message, String playerName) {
        GamesData game = _GamePerRoom.get(_Players.get(playerName).get_roomName());

        if (game == null)
            return false;

        game.textResp(message, _Players.get(playerName).get_roomName(), playerName);

        return true;
    }

    public boolean selectResp(String message, String playerName) {
        GamesData game = _GamePerRoom.get(_Players.get(playerName).get_roomName());

        if (game == null)
            return false;

        game.selectResp(message, _Players.get(playerName).get_roomName(), playerName);

        return true;
    }

    public void quit(String name) {
        _Rooms.get(_Players.get(name).get_roomName()).remove(_Players.get(name));

        GamesData game = _GamePerRoom.get(_Players.get(name).get_roomName());

        if (game != null)
            game.quit(_Players.get(name).get_roomName());
    }

    public void sendDataToAllUsersInRoom(String roomName, String msg) {
        for (Player p : _Rooms.get(roomName)) {
            p.call(msg);
        }
    }

    public void sendDataToUser(String n, String s) {
        _Players.get(n).call(s);
    }
}
