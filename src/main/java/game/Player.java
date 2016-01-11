package game;

import protocol.ProtocolCallback;
import tokenizer.StringMessage;

import java.io.IOException;

/**
 * Created by benbash on 1/10/16.
 */
public class Player {

    private String _Name = "";
    private String _roomName = "";
    private int _score;
    private ProtocolCallback _callback;

    public Player(String clientName, ProtocolCallback<StringMessage> callback){
        _Name = clientName;
        _callback = callback;
        _score = 0;
    }

    public int getScore() {
        return _score;
    }

    public String get_roomName() {
        return _roomName;
    }

    public String get_Name() {
        return _Name;
    }

    public void set_roomName(String _roomName) {
        this._roomName = _roomName;
    }

    public void setScore(int score) {
        _score = score;
    }

    public void setCallback(ProtocolCallback call){
        _callback = call;
    }

    public void call(String msg){
        try {
            _callback.sendMessage(new StringMessage(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
