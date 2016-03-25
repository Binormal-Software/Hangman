import java.util.ArrayList;
import java.util.Arrays;

public class Word {

	private boolean used = false;
	private String word;
	
	public Word(String word){
		this.word = word.toLowerCase();
	}
	
	public void setUsed(boolean used){
		this.used = used;
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
