package game;

import com.google.gson.Gson;
import game.gson.GsonReader;
import game.gson.Question;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by benbash on 1/10/16.
 */
public class BlufferDataStructure implements GamesData {

    ConcurrentHashMap<String, Bluffer> blufferGames;

    ServerDataStructure serverDataStructure;

    public BlufferDataStructure (ServerDataStructure serverDataStructure) {

        this.serverDataStructure = serverDataStructure;

        blufferGames = new ConcurrentHashMap<>();

    }

    @Override
    public void startNewGame(String roomName, int numberOfPlayers) {
        Bluffer bluffer = new Bluffer();
        bluffer.blufferGame = 0;
        bluffer.blufferAnswers = numberOfPlayers;
        bluffer.blufferAnsweredPlayers = numberOfPlayers;
        bluffer.blufferQuestion =  getNewQuestion();
        bluffer.blufferFakeAnswers =  new ArrayList<String>();

        blufferGames.put(roomName, bluffer);

        sendQuestion(roomName);
    }

    private void sendQuestion(String roomName) {
        Bluffer bluffer =blufferGames.get(roomName);

        serverDataStructure.sendDataToAllUsersInRoom(roomName, "ASKTXT " + bluffer.blufferQuestion.getQuestion());
        bluffer.blufferAnsweredPlayers=bluffer.blufferAnswers;
    }

    @Override
    public void textResp(String answer, String roomName, String playerName) {
        Bluffer bluffer =blufferGames.get(roomName);

        int answers = bluffer.blufferAnsweredPlayers;
        bluffer.blufferFakeAnswers.add(answer);
        bluffer.answerForPlayer.put(playerName, roomName);

        if (--answers == 0) {
            bluffer.blufferFakeAnswers.add(bluffer.blufferQuestion.getRealAnswer());

            StringBuilder str = new StringBuilder("ASKCHOICES");
            ArrayList<String> arr = new ArrayList<>(bluffer.blufferFakeAnswers);
            bluffer.blufferFakeAnswers = new ArrayList<>();
            while (arr.size() > 0) {
                int randNumber = new Random().nextInt(arr.size());
                str.append(" " + arr.get(randNumber));
                bluffer.blufferFakeAnswers.add(arr.get(randNumber));
                arr.remove(randNumber);
            }

            serverDataStructure.sendDataToAllUsersInRoom(roomName, str.toString());

            bluffer.blufferAnsweredPlayers = bluffer.blufferAnswers;
        }
    }

    @Override
    public void selectResp(String answer, String roomName, String playerName) {
        Bluffer bluffer =blufferGames.get(roomName);

        int answers = bluffer.blufferAnsweredPlayers;

        bluffer.choiceForPlayer.put(playerName, answer);

        if (--answers == 0) {

            serverDataStructure.sendDataToAllUsersInRoom(roomName, "GAMEMSG The correct answer is: " + bluffer.blufferQuestion.getRealAnswer());

            ConcurrentHashMap<String,Integer> pointsForPlayer = new ConcurrentHashMap<>();

            for (String c : bluffer.choiceForPlayer.keySet())
                if (!bluffer.choiceForPlayer.get(c).equals(bluffer.blufferQuestion.getRealAnswer())) {
                    for (String a : bluffer.answerForPlayer.keySet())
                        if (bluffer.blufferFakeAnswers.get(Integer.valueOf(bluffer.choiceForPlayer.get(c))).equals(bluffer.answerForPlayer.get(a)))
                            pointsForPlayer.put(a, pointsForPlayer.get(a) + 5);
                } else
                    pointsForPlayer.put(c, pointsForPlayer.get(c) + 10);

            for (String n : bluffer.choiceForPlayer.keySet())
                serverDataStructure.sendDataToUser(n, "GAMEMSG correct! +" + pointsForPlayer.get(n) + "pts");


        }
    }

    @Override
    public void quit(String roomName) {

    }

    private Question getNewQuestion() {

        Gson gson = new Gson();
        GsonReader data = null;
        try {

            //System.out.println("Reading JSON from a file");
            //System.out.println("----------------------------");


            BufferedReader br = new BufferedReader(
                    new FileReader(System.getProperty("user.dir") + "/src/input.json"));

            //convert the json string back to object
            data = gson.fromJson(br, GsonReader.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Question[] questions = data.getQuestions();

        return questions[new Random().nextInt(questions.length)];
    }
}

class Bluffer {

    //save room name and question number
    Integer blufferGame;
    //save number of current answers
    Integer blufferAnsweredPlayers;
    //save number of answers
    Integer blufferAnswers;
    //current question
    Question blufferQuestion;
    //current question
    ArrayList<String> blufferFakeAnswers;
    //player name - fake answer
    ConcurrentHashMap<String, String> answerForPlayer;
    //player name choice
    ConcurrentHashMap<String, String> choiceForPlayer;

    public Bluffer() {
    }
}