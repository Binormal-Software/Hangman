package xyz.binormal;
import java.util.ArrayList;

public class Word {

	private String word;
	
	public Word(String word){
		this.word = word.toLowerCase();
	}
	
	public String getText(){
		return this.word;
	}
	
	public ArrayList<Character> getCharacter(){
		ArrayList<Character> chars = new ArrayList<Character>();
		for (char c : word.toCharArray()) {
		  chars.add(c);
		}
		return chars;
	}
	
}
