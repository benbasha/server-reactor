package data;

public class MessageParser {

    String message;

    public MessageParser(String message){
        this.message = message;
    }


    public Message getCommand() {

        int index = message.indexOf(" ");
        if (index != -1) {
            String start = message.substring(0, index);
            message = message.substring(index + 1,message.length());
            switch (start) {
                case "NICK":
                    return Message.NICK;
                case "JOIN":
                    return Message.JOIN;
                case "MSG":
                    return Message.MSG;
                case "STARTGAME":
                    return Message.STARTGAME;
            }
        }
        else {
            String start = message;
            message = "";
            switch (start) {
                case "LISTGAMES":
                    return Message.LISTGAMES;
                case "QUIT":
                    return Message.QUIT;
            }
        }
        return Message.UNIDENTIFIED;
    }

    public String getMessage(){

        return message;
    }
}
