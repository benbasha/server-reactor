package game;

public class MessageParser {

    String message;

    public MessageParser(String message){
        this.message = message;
    }


    public Message getCommand() {

        int index = message.indexOf(" ");
        if (index != -1) {
            String start = message.substring(0, index);
            message = message.substring(index,message.length());
            switch (start) {
                case "NICK":
                    return Message.NICK;
                case "JOIN":
                    return Message.JOIN;
                case "MSG":
                    return Message.MSG;
                case "LISTGAMES":
                    return Message.LISTGAMES;
                case "STARTGAME":
                    return Message.STARTGAME;
                case "TXTRESP":
                    return Message.TXTRESP;
                case "SELECTRESP":
                    return Message.SELECTRESP;
                case "QUIT":
                    return Message.QUIT;
                default:
                    return null;
            }
        }
        else
            return null;
    }

    public String getMessage(){

        return message;
    }
}
