package xyz.binormal;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class Hangman extends Application {

	private static Word word;
	private static UI ui;
	
	private static List<Character> guessedCorrect;
	private static List<Character> guessedIncorrect;
	
	private static List<Word> easyWords;
	private static List<Word> mediumWords;
	private static List<Word> hardWords;
	
	private static MediaPlayer yes;
	private static MediaPlayer no;
	private static MediaPlayer victory;
	private static MediaPlayer loss;
	
	private static String difficulty;
	
	
	@Override 
	public void start(Stage primaryStage) {   
		
		loadUI();
		startScene(primaryStage);

	}
	
	public static void main(String[] args) {
		launch(args);

	}
	
	
	protected static void checkInput(String userInput){

		String msg = "";
		yes.stop();
		no.stop();
		victory.stop();
		loss.stop();
		
		if(userInput.replaceAll("\\s","").length() > 0){
			String guess = userInput.toLowerCase();
			guess = guess.substring(guess.length() - 1, guess.length());

			if (!guess.equals(" ")){

				if(ui.inputMode == 1){

					if (guess.contains("y")){ // ask difficulty
						
						yes.play();
						ui.askQuestion("Select difficulty:", "emh".toCharArray());
						ui.inputMode = 2;	
						
					}else if (guess.contains("n")){
						no.play();
						System.exit(0);
					}else{
						msg = "Type y or n!";
					}

				}else if (ui.inputMode == 2){
					
					
					if (guess.equals("e"))
						difficulty = "EASY";
					else if (guess.equals("m"))
						difficulty = "MEDIUM";
					else 
						difficulty = "HARD";
					
					yes.play();
					initializeGame();
					
				}else{

					if(guess.contains("?")){
						
						ui.hint(word.getText());
						yes.play();
						
					}else if(guess.contains("!")){
							
						guessedCorrect.addAll(word.getCharacter());
						yes.play();
						
					}else{

						if (guessedCorrect.contains(guess.toCharArray()[0]) || guessedIncorrect.contains(guess.toCharArray()[0])){
							msg = "You already guessed '"+guess+"'";
						}else{
							if(word.getText().contains(guess)){
								msg = "'"+guess+"' is in the word!! ";
								yes.play();
								guessedCorrect.add(guess.toCharArray()[0]);

							}else{
								msg = "'"+guess+"' isn't in " + word.getText();
								no.play();
								guessedIncorrect.add(guess.toCharArray()[0]);
								
							}
						}
					}
				}
			}
		}

		System.out.println(msg);
		ui.refresh(word.getText(), guessedCorrect, guessedIncorrect.size());
		updateGame();

	}
	
	
	private static void loadUI(){
		
		ui = new UI();

		ui.initialize("", toArraylist(""), 0);
		ui.refresh("hangman!  by ryan rodriguez", toArraylist("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!"), 6);
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
		
		word = selectWord(numberOfHints(difficulty));
		
		guessedIncorrect = new ArrayList<Character>();
		guessedCorrect = new ArrayList<Character>();

		ui.initialize(word.getText(), guessedCorrect, numberOfHints(difficulty));
		System.out.println("Game initialized");

	}
	
	private static void updateGame(){

		if(ui.inputMode == 0){
			int lettersGuessed = 0;

			for(int i = 0; i < word.getText().length(); i++){
				if (guessedCorrect.contains(word.getText().charAt(i))){
					lettersGuessed++;
				}
			}
			if (lettersGuessed >= word.getText().replaceAll("\\s","").length()){
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
	
	private static List<Word> loadWords(String filePath, String tagName){

		List<String> allWords;

		try {
			allWords = Files.readAllLines(Paths.get(filePath));

		} catch (IOException e) {
			System.err.println("Error loading words!");
			allWords = new ArrayList<String>();
			allWords.addAll(Arrays.asList("<" + tagName + ">", "error", "</" + tagName + ">"));
		}

		int[] listIndex = {allWords.indexOf("<" + tagName + ">"), allWords.indexOf("</" + tagName + ">")};
		List<Word> returnWords = new ArrayList<Word>(); 

		for(int i = listIndex[0] + 1; i < listIndex[1]; i++){
			returnWords.add(new Word(allWords.get(i).trim()));
		}
		
		return returnWords;
		
	}
	
	private static Word selectWord(int hints){ // (Difficulty) <= 1 is hard; > 2 is easy

		if(easyWords==null || easyWords.isEmpty())
			easyWords = new ArrayList<Word>(loadWords("./res/dictionary.txt", "EASY"));
		
		if(mediumWords==null || mediumWords.isEmpty())
			mediumWords = new ArrayList<Word>(loadWords("./res/dictionary.txt", "MEDIUM"));
		
		if(hardWords==null || hardWords.isEmpty())
			hardWords = new ArrayList<Word>(loadWords("./res/dictionary.txt", "HARD"));
		
		List<Word> wordPool = new ArrayList<Word>();
		
		switch(hints){
		
		case 0: wordPool = hardWords; break;
		case 1: wordPool = mediumWords; break;
		case 2: wordPool = easyWords; break;

		}
		
		
		int random = (int) (Math.random() * wordPool.size());
		Word newWord = wordPool.get(random);
		wordPool.remove(random);
			
		return newWord;
			
	}
	
	private static ArrayList<Character> toArraylist(String input){
		
		ArrayList<Character> chars = new ArrayList<Character>();
		for (char c : input.toCharArray()) {
		  chars.add(c);
		}
		
		return chars;
		
	}
	
	private static int numberOfHints(String difficulty){
		
		switch (difficulty){
		
		case "EASY": return 2;
		case "MEDIUM": return 1;
		case "HARD": return 0;
		default: return 0;
		
		
		
		}
	}

}