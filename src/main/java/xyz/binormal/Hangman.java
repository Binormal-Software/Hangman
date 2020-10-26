package xyz.binormal;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
	
	// global vars
	
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
	
	
	protected static void checkInput(String userInput){ // global function called to check if letter is valid

		String msg = "";
		yes.stop();
		no.stop();
		victory.stop();
		loss.stop();
		
		if(userInput.replaceAll("\\s","").length() > 0){
			String guess = userInput.toLowerCase();
			guess = guess.substring(guess.length() - 1, guess.length());

			if (!guess.equals(" ")){

				if(ui.inputMode == 1){ // play again dialogue

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

				}else if (ui.inputMode == 2){ // ask difficulty dialogue 
					
					
					if (guess.equals("e"))
						difficulty = "EASY";
					else if (guess.equals("m"))
						difficulty = "MEDIUM";
					else 
						difficulty = "HARD";
					
					yes.play();
					initializeGame();
					
				}else{

					if(guess.contains("?")){ // remove some letters
						
						ui.hint(word.getText());
						yes.play();
						
					}else if(guess.contains("!")){ // instant solve, for debugging
							
						guessedCorrect.addAll(word.getCharacters());
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
		ui.refresh(word, guessedCorrect, guessedIncorrect.size());
		updateGame();

	}
	
	
	private static void loadUI () { // load ux elements, graphics and sounds
		
		ui = new UI();

		ui.initialize(new Word(""), toArraylist(""), 0);
		ui.refresh(new Word("hangman!  by ryan rodriguez"), toArraylist("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!"), 6);
		ui.askQuestion("Welcome to Hangman! Select difficulty:", "emh".toCharArray());
		ui.inputMode = 2;
		
		try {
			
			Media yesSound = new Media(Hangman.class.getClassLoader().getResource("yes.wav").toURI().toString());
			yes = new MediaPlayer(yesSound);
			yes.setVolume(0.15);
			Media noSound = new Media(Hangman.class.getClassLoader().getResource("no.wav").toURI().toString());
			no = new MediaPlayer(noSound);
			no.setVolume(0.2);
			Media victorySound = new Media(Hangman.class.getClassLoader().getResource("victory.wav").toURI().toString());
			victory = new MediaPlayer(victorySound);
			victory.setVolume(0.2);
			Media lossSound = new Media(Hangman.class.getClassLoader().getResource("loss.wav").toURI().toString());
			loss = new MediaPlayer(lossSound);
			loss.setVolume(0.2);

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
				
	}
	
	private static void startScene(Stage primaryStage){ // start javafx window

		Scene hangmanScene = new Scene(ui.uiPane(), 10, 10, false, SceneAntialiasing.BALANCED);
		primaryStage.setMinHeight(693);
		primaryStage.setMinWidth(806);
		primaryStage.setTitle("Hangman");
		primaryStage.setScene(hangmanScene);
		primaryStage.show();

	}
	
	private static void initializeGame(){ // select new word and start game
		
		word = selectWord(difficulty);
		
		guessedIncorrect = new ArrayList<Character>();
		guessedCorrect = new ArrayList<Character>();

		ui.initialize(word, guessedCorrect, numberOfHints(difficulty));
		System.out.println("Game initialized");

	}
	
	private static void updateGame(){ // check if game is over

		if(ui.inputMode == 0){ // main game loop
			int lettersGuessed = 0;

			for(int i = 0; i < word.getText().length(); i++){
				if (guessedCorrect.contains(word.getText().charAt(i))){ // update correct
					lettersGuessed++;
				}
			}
			if (lettersGuessed >= word.getText().replaceAll("\\s","").length()){ // you win
				victory.play();
				System.out.println("You won!");
				ui.askQuestion("You won! Play again?", "yn".toCharArray());
				ui.inputMode = 1;
			}


			if (guessedIncorrect.size() >= 6){ // you lose
				loss.play();
				System.out.println("You lose!");
				ui.askQuestion("You lose... Play again?", "yn".toCharArray());
				ui.inputMode = 1;
			}
		}
	}
	
	private static List<Word> loadWords(String tagName){ // load list of words from dictionary file

		List<String> allWords;

		try {
			allWords = Files.readAllLines(Paths.get(Hangman.class.getClassLoader().getResource("dictionary.txt").getPath()));
		} catch (Exception e) {
			System.err.println("Error loading words!");
			e.printStackTrace();
			allWords = new ArrayList<String>();
			allWords.addAll(Arrays.asList("<" + tagName + ">", "error", "</" + tagName + ">")); // hehe
		}

		int[] listIndex = {allWords.indexOf("<" + tagName + ">"), allWords.indexOf("</" + tagName + ">")}; // get index of beginning and closing tags
		List<Word> returnWords = new ArrayList<Word>(); 

		for(int i = listIndex[0] + 1; i < listIndex[1]; i++){ // read in each string between tags
			returnWords.add(new Word(allWords.get(i).trim()));
		}
		
		return returnWords;
		
	}
	
	private static Word selectWord(String difficulty){ // select word from loaded list

		if(easyWords==null || easyWords.isEmpty())
			easyWords = new ArrayList<Word>(loadWords("EASY")); // load all words, ...
		
		if(mediumWords==null || mediumWords.isEmpty())
			mediumWords = new ArrayList<Word>(loadWords("MEDIUM"));
		
		if(hardWords==null || hardWords.isEmpty())
			hardWords = new ArrayList<Word>(loadWords("HARD"));
		
		List<Word> wordPool = new ArrayList<Word>();
		
		switch(difficulty){
		
		case "HARD": wordPool = hardWords; break;
		case "MEDIUM": wordPool = mediumWords; break;
		case "EASY": wordPool = easyWords; break;

		}
		
		
		int random = (int) (Math.random() * wordPool.size()); // randomly select word
		Word newWord = wordPool.get(random);
		wordPool.remove(random);
			
		return newWord;
			
	}
	
	private static ArrayList<Character> toArraylist(String input){ // shortcut to convert to arraylist
		
		ArrayList<Character> chars = new ArrayList<Character>();
		for (char c : input.toCharArray()) {
		  chars.add(c);
		}
		
		return chars;
		
	}
	
	private static int numberOfHints(String difficulty){ // quick conversion
		
		switch (difficulty){
		
		case "EASY": return 2;
		case "MEDIUM": return 1;
		case "HARD": return 0;
		default: return 0;
		
		
		
		}
	}

}