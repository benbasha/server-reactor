package protocol;  

import com.sun.corba.se.spi.activation.Server;
import game.*;
import tokenizer.StringMessage;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * a simple implementation of the server protocol interface
 */
public class TBGPImpl implements AsyncServerProtocol<StringMessage> {

    private final ServerDataStructure serverDataStructure;
    private boolean _shouldClose = false;
	private boolean _connectionTerminated = false;

    String name= "";

    public TBGPImpl(ServerDataStructure data) {
        serverDataStructure = data;
    }


    /**
	 * processes a message<BR>
	 * this simple interface prints the message to the screen, then composes a simple
	 * reply and sends it back to the client
	 *
	 * @param msg the message to process
	 * @return the reply that should be sent to the client, or null if no reply needed
	 */
	public void processMessage(StringMessage msg, ProtocolCallback<StringMessage> callback) throws IOException {
        MessageParser parser = new MessageParser(msg.getMessage());
        Message command = parser.getCommand(); //getting the Command fm


        switch (command){

            case NICK:
                String nickName = parser.getMessage();

                if (nickName != null){
                    //out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8"), true);

                    if (serverDataStructure.containsPlayer(nickName)){
                        callback.sendMessage(new StringMessage("SYSMSG " + parser.getCommand().toString() + " REJECTED"));
                    }
                    else{
                        Player player = new Player(nickName, callback);
                        serverDataStructure.addPlayer(player);
                        callback.sendMessage(new StringMessage("SYSMSG " + parser.getCommand().toString() + " ACCEPTED"));
                    }
                    name= nickName;
                }
                break;
            case JOIN:
                String roomName = parser.getMessage();
                if (serverDataStructure.joinToRoom(roomName, name))
                    callback.sendMessage(new StringMessage("SYSMSG " + msg + " ACCEPTED"));
                else
                    callback.sendMessage(new StringMessage("SYSMSG " + msg + " REJECTED"));

                break;
            case MSG:
                String message = parser.getMessage();

                //getting player's room
                String playersroomName = serverDataStructure.getRoom(name);

                if (playersroomName != null){
                    if (serverDataStructure.containsRoom(playersroomName)){
                        Iterator<Player> players = serverDataStructure.getRoomsPlayers(playersroomName);
                        while (players.hasNext()){

                            players.next().call("USRMSG " + name + ":" + message);
                        }
                    }
                }

                break;
            case LISTGAMES:

                callback.sendMessage(new StringMessage(serverDataStructure.getGamesList()));
                break;
            case STARTGAME:
                if (serverDataStructure.startGame(parser.getMessage(), name))
                    callback.sendMessage(new StringMessage("SYSMSG " + parser.getCommand().toString() + " ACCEPTED"));
                else
                    callback.sendMessage(new StringMessage("SYSMSG " + parser.getCommand().toString() + " REJECTED"));
                break;
            case TXTRESP:
                if (serverDataStructure.textResp(parser.getMessage(), name))
                    callback.sendMessage(new StringMessage("SYSMSG " + parser.getCommand().toString() + " ACCEPTED"));
                else
                    callback.sendMessage(new StringMessage("SYSMSG " + parser.getCommand().toString() + " REJECTED"));
                break;
            case SELECTRESP:
                if (serverDataStructure.selectResp(parser.getMessage(), name))
                callback.sendMessage(new StringMessage("SYSMSG " + parser.getCommand().toString() + " ACCEPTED"));
                else
                callback.sendMessage(new StringMessage("SYSMSG " + parser.getCommand().toString() + " REJECTED"));
                break;
            case QUIT:
                serverDataStructure.quit(parser.getMessage());
                break;

            default:
                callback.sendMessage(new StringMessage("SYSMSG " + msg.getMessage() + " UNIDENTIFIED"));
        }

	}

	/**
	 * detetmine whether the given message is the termination message
	 *
	 * @param msg the message to examine
	 * @return false - this simple protocol doesn't allow termination...
	 */
	public boolean isEnd(StringMessage msg) {
		return msg.equals("bye");
	}

	/**
	 * Is the protocol in a closing state?.
	 * When a protocol is in a closing state, it's handler should write out all pending data,
	 * and close the connection.
	 * @return true if the protocol is in closing state.
	 */
	public boolean shouldClose() {
		return this._shouldClose;
	}

	/**
	 * Indicate to the protocol that the client disconnected.
	 */
	public void connectionTerminated() {
		this._connectionTerminated = true;
	}

}
