import java.io.File;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class Hangman extends Application {

	private static int hints;
	private static boolean[] wordPlayed;
	private static String word;
	private static ArrayList<Character> guessedCorrect;
	private static ArrayList<Character> guessedIncorrect;
	private static UI ui;
	private static MediaPlayer yes;
	private static MediaPlayer no;
	private static MediaPlayer victory;
	private static MediaPlayer loss;
	
	@Override 
	public void start(Stage primaryStage) {   
		
		loadUI();
		startScene(primaryStage);

	}
	
	
	protected static void checkInput(String userInput){

		String msg = "";
		yes.stop();
		no.stop();
		victory.stop();
		loss.stop();
		
		if(userInput.replaceAll("\\s","").length() > 0){
			String guess = userInput.toUpperCase();
			guess = guess.substring(guess.length() - 1, guess.length());

			if (!guess.equals(" ")){

				if(ui.inputMode == 1){

					if (guess.contains("Y")){ // ask difficulty
						
						yes.play();
						ui.askQuestion("Select difficulty:", "emh".toCharArray());
						ui.inputMode = 2;	
						
					}else if (guess.contains("N")){
						no.play();
						System.exit(0);
					}else{
						msg = "Type y or n!";
					}

				}else if (ui.inputMode == 2){
					
					
					if (guess.equals("E"))
						hints = 2;
					else if (guess.equals("M"))
						hints = 1;
					else 
						hints = 0;
					
					yes.play();
					initializeGame();
					
				}else{

					if(guess.contains("?")){
						
						ui.hint(word);
						yes.play();
						
					}else if(guess.contains("!")){
							
						guessedCorrect.addAll(toArraylist(word));
						yes.play();
						
					}else{

						if (guessedCorrect.contains(guess.toCharArray()[0]) || guessedIncorrect.contains(guess.toCharArray()[0])){
							msg = "You already guessed '"+guess+"'";
						}else{
							if(word.contains(guess)){
								msg = "'"+guess+"' is in the word!! ";
								yes.play();
								guessedCorrect.add(guess.toCharArray()[0]);

							}else{
								msg = "'"+guess+"' isn't in the word... ";
								no.play();
								guessedIncorrect.add(guess.toCharArray()[0]);
								
							}
						}
					}
				}
			}
		}

		System.out.println(msg);
		ui.refresh(word, guessedCorrect, guessedIncorrect.size());
		updateGame();

	}
	
	
	private static void loadUI(){
		
		ui = new UI();

		ui.initialize("", toArraylist(""), 0);
		ui.refresh("HANGMAN  by Ryan Rodriguez", toArraylist("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"), 6);
		ui.askQuestion("Welcome to Hangman! Select difficulty:", "emh".toCharArray());
		ui.inputMode = 2;
		
		Media yesSound = new Media(new File("./res/yes.wav").toURI().toString());
		yes = new MediaPlayer(yesSound);
		yes.setVolume(0.15);
		Media noSound = new Media(new File("./res/no.wav").toURI().toString());
		no = new MediaPlayer(noSound);
		no.setVolume(0.2);
		Media victorySound = new Media(new File("./res/victory.wav").toURI().toString());
		victory = new MediaPlayer(victorySound);
		victory.setVolume(0.2);
		Media lossSound = new Media(new File("./res/loss.wav").toURI().toString());
		loss = new MediaPlayer(lossSound);
		loss.setVolume(0.2);
		
		wordPlayed = new boolean[100];
		
	}
	
	private static void startScene(Stage primaryStage){

		Scene hangmanScene = new Scene(ui.uiPane(), 10, 10, false, SceneAntialiasing.BALANCED);
		primaryStage.setMinHeight(693);
		primaryStage.setMinWidth(806);
		primaryStage.setTitle("Hangman");
		primaryStage.setScene(hangmanScene);
		primaryStage.show();

	}
	
	private static void initializeGame(){

		word = selectWord(hints);
		guessedIncorrect = new ArrayList<Character>();
		guessedCorrect = new ArrayList<Character>();

		ui.initialize(word, guessedCorrect, hints);
		System.out.println("Game initialized");

	}
	
	private static void updateGame(){

		if(ui.inputMode == 0){
			int lettersGuessed = 0;

			for(int i = 0; i < word.length(); i++){
				if (guessedCorrect.contains(word.charAt(i))){
					lettersGuessed++;
				}
			}
			if (lettersGuessed >= word.replaceAll("\\s","").length()){
				victory.play();
				System.out.println("You won!");
				ui.askQuestion("You won! Play again?", "yn".toCharArray());
				ui.inputMode = 1;
			}


			if (guessedIncorrect.size() >= 6){
				loss.play();
				System.out.println("You lose!");
				ui.askQuestion("You lose... Play again?", "yn".toCharArray());
				ui.inputMode = 1;
			}
		}
	}
	
	private static String selectWord(int difficulty){ // <= 1 is hard; > 2 is easy

		int index;
		boolean allWordsPlayed;
		
		String[] hardWords = { // NO CHEATING!!!!!!!!!!!!!!!!!!!!!
				"auxiliary",
				"buzzing wire",
				"coaxial",
				"cryptic",
				"russian czar",
				"feel the rhythm",
				"fizzy soda",
				"fuchsia",
				"gypsy wagon",
				"phlegm",
				"psychic squid",
				"relaxing jazz",
				"six megahertz",
				"syntax error",
				"valkyrie",
				"water nymph",
				"zephyrus"
		};
		
		String[] easyWords = {
				"comic sans is lame",
				"computers rock",
				"fat cats eat rats",
				"flummoxing filibuster",
				"jump on the bandwagon",
				"microsoft windows",
				"null pointer exception",
				"twenty one gun salute",
		};
		
		allWordsPlayed = true;
		for(int i = 0; i < hardWords.length; i++){
			if(wordPlayed[i]==false){
				allWordsPlayed = false;
				break;
			}
		}
		if(allWordsPlayed){
			System.out.println("All hard words played. Resetting.");
			for(int i = 0; i < hardWords.length; i++){
				wordPlayed[i] = false;
			}
		}
		
		
		allWordsPlayed = true;
		for(int i = hardWords.length; i < (hardWords.length + easyWords.length); i++){
			//
			if(wordPlayed[i]==false){
				allWordsPlayed = false;
				break;
			}
		}
		if(allWordsPlayed){
			System.out.println("All easy words played. Resetting.");
			for(int i = hardWords.length; i < (hardWords.length + easyWords.length); i++){
				wordPlayed[i] = false;
			}
		}
		
		
		if (difficulty <= 1){
			
			do {
				index = (int) (Math.random() * hardWords.length);
			}
			while (wordPlayed[index]);
			
			wordPlayed[index] = true;
			return hardWords[index].toUpperCase();
			
		}else{
			
			do {
				index = (int) (Math.random() * easyWords.length);
			}
			while (wordPlayed[index + hardWords.length]);
			
			wordPlayed[index + hardWords.length] = true;
			return easyWords[index].toUpperCase();
		}
			
	}
	
	private static ArrayList<Character> toArraylist(String input){
		
		ArrayList<Character> chars = new ArrayList<Character>();
		for (char c : input.toCharArray()) {
		  chars.add(c);
		}
		
		return chars;
		
	}
	

}