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
    private ProtocolCallback _callback;

    public Player(String clientName, ProtocolCallback<StringMessage> callback){
        _Name = clientName;
        _callback = callback;
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

    public void call(String msg){
        try {
            _callback.sendMessage(new StringMessage(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
