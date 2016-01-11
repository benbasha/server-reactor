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
        bluffer.answerForPlayer = new ConcurrentHashMap<>();
        bluffer.blufferFakeAnswers =  new ArrayList<String>();
        bluffer.choiceForPlayer = new ConcurrentHashMap<>();
        bluffer.pointsForPlayer = new ConcurrentHashMap<>();

        blufferGames.put(roomName, bluffer);

        sendQuestion(roomName);
    }

    private void sendQuestion(String roomName) {
        Bluffer bluffer = blufferGames.get(roomName);

        if (++bluffer.blufferGame <= 3) {
            bluffer.blufferQuestion =  getNewQuestion();

            serverDataStructure.sendDataToAllUsersInRoom(roomName, "ASKTXT " + bluffer.blufferQuestion.getQuestion());
            bluffer.blufferAnsweredPlayers = bluffer.blufferAnswers;
        }


    }

    @Override
    public void textResp(String answer, String roomName, String playerName) {
        Bluffer bluffer =blufferGames.get(roomName);

        bluffer.blufferFakeAnswers.add(answer);
        bluffer.answerForPlayer.put(playerName, answer.toLowerCase());

        if (--bluffer.blufferAnsweredPlayers == 0) {
            bluffer.blufferFakeAnswers.add(bluffer.blufferQuestion.getRealAnswer());

            StringBuilder str = new StringBuilder("ASKCHOICES");
            ArrayList<String> arr = new ArrayList<>(bluffer.blufferFakeAnswers);
            bluffer.blufferFakeAnswers = new ArrayList<>();
            int i = 0;
            while (arr.size() > 0) {
                int randNumber = new Random().nextInt(arr.size());
                str.append(" (" + i + ") " + arr.get(randNumber).toLowerCase());
                bluffer.blufferFakeAnswers.add(arr.get(randNumber).toLowerCase());
                arr.remove(randNumber);
                i++;
            }

            serverDataStructure.sendDataToAllUsersInRoom(roomName, str.toString());

            bluffer.blufferAnsweredPlayers = bluffer.blufferAnswers;
        }
    }

    @Override
    public void selectResp(String answer, String roomName, String playerName) {
        Bluffer bluffer =blufferGames.get(roomName);


        bluffer.choiceForPlayer.put(playerName, bluffer.blufferFakeAnswers.get(Integer.valueOf(answer)).toLowerCase());

        if (--bluffer.blufferAnsweredPlayers == 0) {

            serverDataStructure.sendDataToAllUsersInRoom(roomName, "GAMEMSG The correct answer is: " + bluffer.blufferQuestion.getRealAnswer());

            ConcurrentHashMap<String, Integer> pointsForPlayer = new ConcurrentHashMap<>();

            for (String c : bluffer.choiceForPlayer.keySet())
                if (pointsForPlayer.get(c) == null)
                    pointsForPlayer.put(c, 0);

            for (String c : bluffer.choiceForPlayer.keySet()) {
                System.out.println(c + " " + bluffer.choiceForPlayer.get(c) + "==" + bluffer.blufferQuestion.getRealAnswer().toLowerCase());
                if (!(bluffer.choiceForPlayer.get(c)).equals(bluffer.blufferQuestion.getRealAnswer().toLowerCase())) {
                    for (String a : bluffer.answerForPlayer.keySet()) {
                        System.out.println("BLA BLA " + c + " " + bluffer.choiceForPlayer.get(c) + "==" + bluffer.answerForPlayer.get(a));
                        if ((bluffer.choiceForPlayer.get(c)).equals(bluffer.answerForPlayer.get(a)))
                            pointsForPlayer.put(a, pointsForPlayer.get(a) + 5);
                    }
                } else
                    pointsForPlayer.put(c, pointsForPlayer.get(c) + 10);
            }

            for (String p : bluffer.choiceForPlayer.keySet()) {
                if (!(bluffer.choiceForPlayer.get(p)).equals(bluffer.blufferQuestion.getRealAnswer()))
                    serverDataStructure.sendDataToUser(p, "GAMEMSG wrong! +" + pointsForPlayer.get(p) + "pts");
                else
                    serverDataStructure.sendDataToUser(p, "GAMEMSG correct! +" + pointsForPlayer.get(p) + "pts");
            }

            StringBuilder str = new StringBuilder("GAMEMSG Summary:");
            for (String p : pointsForPlayer.keySet()) {
                if (bluffer.pointsForPlayer.get(p) == null)
                    bluffer.pointsForPlayer.put(p, 0);

                bluffer.pointsForPlayer.put(p, bluffer.pointsForPlayer.get(p) + pointsForPlayer.get(p));
                str.append(" " + p + ":" + bluffer.pointsForPlayer.get(p) + "pts");
            }

            serverDataStructure.sendDataToAllUsersInRoom(roomName, str.toString());

            bluffer.blufferFakeAnswers =  new ArrayList<String>();

            sendQuestion(roomName);
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
                    new FileReader("q.json"));

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

    ConcurrentHashMap<String,Integer> pointsForPlayer;

    public Bluffer() {
    }
}