import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

import javafx.animation.FadeTransition;
import javafx.animation.PathTransition;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class UI{

	
	/*           ->             -> letterPane    ->
	 *  rootPane -> hangmanPane -> characterPane -> bodyPane
	 *           -> letterTilePane
	 * 
	 */
	public int inputMode;
	private int previousDamage = 0;
	private VBox rootPane;
	private FlowPane letterTilePane;
	private Pane hangmanPane;
	private Pane characterPane[];
	private Pane letterPane;
	private Button messageLabel;
	private static ArrayList<Character> lettersDrawn;
	private LetterAnimator pAnimator;
	private Button[] b;
	private boolean gameReset = false;
	

	
	public UI(){
		
		lettersDrawn = new ArrayList<Character>();
		
		rootPane = new VBox();
		rootPane.setStyle("-fx-background-color: #146ab5;");
	
		hangmanPane = backgroundPane();	
		rootPane.getChildren().add(hangmanPane);
		
		
	}
	
	public Pane uiPane(){
		return rootPane;
	}
	
	public void initialize(String word, ArrayList<Character> guessedCorrect, int hints){
		
		String tiles = "abcdefghijklmnopqrstuvwxyz";
		for(int i = 0; i < hints; i++)
			tiles += "?";
		
		rootPane.getChildren().remove(letterTilePane);
		letterTilePane = selectionPane(tiles.toCharArray());
		rootPane.getChildren().add(letterTilePane);
		
		if(characterPane!=null){
			for(int i = 0; i < characterPane.length; i++){
				hangmanPane.getChildren().remove(characterPane[i]);
			}
		}
		
		hangmanPane.getChildren().remove(letterPane);
		
		
		
		letterPane = new Pane();
		hangmanPane.getChildren().add(letterPane);
		
		characterPane = new Pane[7];
		for(int i = 0; i < characterPane.length; i++){
			
			characterPane[i] = characterPart(i);
			characterPane[i].setLayoutX(30);
			characterPane[i].setEffect(dropShadow());
			
		}
		
		
		
		this.setLabel(null);
		this.inputMode = 0;
		refresh(word, guessedCorrect, 0);
		
	}

	public void askQuestion(String msg, char[] c){
		
		rootPane.getChildren().remove(letterTilePane);
		letterTilePane = selectionPane(c);
		rootPane.getChildren().add(letterTilePane);
		this.setLabel(msg);
		
	}
	
	public void refresh(String word, ArrayList<Character> guessedCorrect, int howDead){
		
		drawCharacter(howDead);
		drawWordArea(word, guessedCorrect);
		
	}
	
	public void hint(String safeLetters){
		
		int j = 0;
		for(int i = 0; i < 10; i++){
			
			int r = new Random().nextInt(b.length);
			
			if(letterTilePane.getChildren().contains(b[r]) && !safeLetters.contains(b[r].getText()) && !b[r].getText().equals("?")){
				removeButton(r);
			}else{
				
				j++;
				if(j<100)
					i--;
					
			}
		}
	}
	
	
	
	private void setLabel(String s){
		
		if(s!=null){
			this.messageLabel.setText(s);
		}else{
			this.letterTilePane.getChildren().remove(this.messageLabel);
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	private void drawWordArea(String word, ArrayList<Character> guessedCorrect){
		
		String wordArea = "";
		String letterSlide = "";
		Pane slidePane = new Pane();
		slidePane.setStyle("-fx-background-color: #ff000000;");
		
		for (int i = 0; i < word.length(); i++){

			if(word.toCharArray()[i] == ' '){
				wordArea += "\r\n";
				letterSlide += "\r\n";
			}else{
				if(guessedCorrect.contains(word.toCharArray()[i]) && !lettersDrawn.contains(word.toCharArray()[i])){
					//wordArea += ("" + word.toCharArray()[i] + " ");
					wordArea += "_ ";
					letterSlide += ("" + word.toCharArray()[i] + " ");
				}else{
					wordArea += "_ ";
					letterSlide += "  ";
				}
			}
		}
		
		lettersDrawn = (ArrayList<Character>) guessedCorrect.clone();
		
		Text textArea = (Text) hangmanPane.lookup("#wordArea");
		textArea.setText(wordArea);
		
		Text letter = coolText(0,22,letterSlide);//230 100
		slidePane.translateYProperty().set(-1000);
		
		Path path = new Path();
		path.getElements().add (new MoveTo ((300) + (letter.getLayoutBounds().getWidth()/2), -300));
		path.getElements().add (new LineTo((300) + (letter.getLayoutBounds().getWidth()/2), ((60) + (letter.getLayoutBounds().getHeight()/2))));
		
		PathTransition pathTransition = new PathTransition();
		pathTransition.setDuration(Duration.millis(500));
		pathTransition.setNode(slidePane);
		pathTransition.setPath(path);
		pathTransition.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
		
		pathTransition.play();
		
		slidePane.getChildren().add(letter);
		letterPane.getChildren().add(slidePane);
		
		
	}

	private void drawCharacter(int howDead){

		for(int i = (previousDamage+1); i < (howDead+1); i++){

			
			
			hangmanPane.getChildren().add(characterPane[i]);
			characterPane[i].toBack();
			FadeTransition ft = new FadeTransition(Duration.millis(500), characterPane[i]);
			
			ft.setFromValue(0.0);
			ft.setToValue(1.0);
			ft.play();
		}
			
		previousDamage = howDead;
		
	}
	
	private void removeButton(int index){
		
		if(index <= b.length){
			if(letterTilePane!=null && b[index]!=null){
				
				gameReset = false;
				
				FadeTransition ft = new FadeTransition(Duration.millis(300), b[index]);
				ft.setFromValue(1.0);
				ft.setToValue(0.0);
				ft.setOnFinished(e -> {
					if (!gameReset){
						letterTilePane.getChildren().remove(b[index]);
					}
				});
				ft.play();
				System.out.println("Removing " + b[index].getText());
			}
		}
	}
	
	
	final private Pane characterPart(int partType){

		Pane bodyPane = new Pane();
		Shape bodyPart = new Line();
		Color strokeColor = Color.WHITE;
		Color fillColor = Color.web("#303030");
		
		switch(partType){

		case 6: 
			bodyPart = drawnLine(125, 290, 145, 230, 2, strokeColor, 10);
			bodyPane.getChildren().add(bodyPart);
			break;
		case 5:
			bodyPart = drawnLine(175, 290, 165, 230, 2, strokeColor, 10);
			bodyPane.getChildren().add(bodyPart);
			break;
		case 4:
			bodyPart = drawnLine(205, 190, 155, 230, 2, strokeColor, 10);
			bodyPane.getChildren().add(bodyPart);
			break;
		case 3:
			bodyPart = drawnLine(105, 190, 155, 230, 2, strokeColor, 10);
			bodyPane.getChildren().add(bodyPart);
			break;
		case 2:
			bodyPart = new Circle();
			((Circle) bodyPart).setCenterX(155);
			((Circle) bodyPart).setCenterY(225);
			((Circle) bodyPart).setRadius(40);
			((Circle) bodyPart).setStrokeWidth(2);
			bodyPart.setStroke(strokeColor);
			bodyPart.setFill(fillColor);
			//bodyPart.setFill(sketchPattern());
			bodyPane.getChildren().add(bodyPart);
			break;
		case 1:
			Pane head = new Pane();

			Circle circle = new Circle();
			circle.setCenterX(155);
			circle.setCenterY(175);
			circle.setRadius(30);
			circle.setStrokeWidth(2);
			circle.setStroke(strokeColor);
			circle.setFill(fillColor);

			Text text1 = new Text(155, 173, "x  x");
			//Text text1 = new Text(155, 173, "\u25CF  \u25CF");
			text1.setFont(Font.font("Arial",12));
			text1.setFill(strokeColor);
			Text text2 = new Text(155, 182, "__");
			text2.setFont(Font.font("Arial",12));
			text2.setFill(strokeColor);

			head.getChildren().add(circle);
			head.getChildren().add(text1);
			head.getChildren().add(text2);

			bodyPane.getChildren().add(head);
			break;
		
		}
			
		return bodyPane;
	}
	
	final private Shape drawnRectangle(double x, double y, double w, double h){
		
		double strokeWidth = 0.5;
		Color color = Color.WHITE;
		double skillLevel = 50; // 50 is good
		
		Shape top = drawnLine(x, y, x + w, y, strokeWidth, color, skillLevel);
        Shape bottom = drawnLine(x, y + h, x + w, y + h, strokeWidth, color, skillLevel);
        Shape left = drawnLine(x, y, x, y + h, strokeWidth, color, skillLevel);
        Shape right = drawnLine(x + w, y, x + w, y + h, strokeWidth, color, skillLevel);
        return Shape.union(top, Shape.union(bottom, Shape.union(left, right)));
	}
	
	final private Shape drawnLine(double x1, double y1, double x2, double y2, double strokeWidth, Color color, double skillLevel){
		
		Point2D startPoint = new Point2D(x1, y1);
        Point2D endPoint = new Point2D(x2, y2);

        double wobble = Math.sqrt((endPoint.getX() - startPoint.getX()) * (endPoint.getX() - startPoint.getX()) + (endPoint.getY() - startPoint.getY()) * (endPoint.getY() - startPoint.getY())) / skillLevel;

        double r1 = Math.random();
        double r2 = Math.random();

        double xfactor = Math.random() > 0.5 ? wobble : -wobble;
        double yfactor = Math.random() > 0.5 ? wobble : -wobble;

        Point2D control1 = new Point2D((endPoint.getX() - startPoint.getX()) * r1 + startPoint.getX() + xfactor, (endPoint.getY() - startPoint.getY()) * r1 + startPoint.getY() + yfactor);
        Point2D control2 = new Point2D((endPoint.getX() - startPoint.getX()) * r2 + startPoint.getX() - xfactor, (endPoint.getY() - startPoint.getY()) * r2 + startPoint.getY() - yfactor);

        MoveTo startMove = new MoveTo(startPoint.getX(), startPoint.getY());
        CubicCurveTo curve = new CubicCurveTo(control1.getX(), control1.getY(),
                control2.getX(), control2.getY(),
                endPoint.getX(), endPoint.getY());

        Path path = new Path(startMove, curve);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStroke(color);
        path.setStrokeWidth(strokeWidth + (strokeWidth * (Math.random() - 0.5) / 8.0));
        path.setStrokeType(StrokeType.CENTERED);
        return path;
	}
	
	final private Pane backgroundPane(){
		
		Pane pane = new Pane();
		Pane noosePane = new Pane();
		
		pane.setStyle("-fx-background-color: #303030;");
		pane.setPadding(new Insets(5));
		pane.setMinHeight(460);
		
		Shape[] r = new Shape[4];
		r[0] = drawnRectangle(50, 350, 250, 50);
		r[1] = drawnRectangle(50, 50, 10, 350);
		r[2] = drawnRectangle(50, 50, 140, 10);
		r[3] = drawnRectangle(180, 50, 10, 95);
		
		Rectangle[] r2 = new Rectangle[4];
		r2[0] = new Rectangle(50, 350, 250, 50);
		r2[1] = new Rectangle(50, 50, 10, 350);
		r2[2] = new Rectangle(50, 50, 140, 10);
		r2[3] = new Rectangle(180, 50, 10, 95);
		
		
		
		Color nooseColor = Color.web("#303030");
		Color strokeColor = Color.web("#ffffffff");
		
		for(int i = 0; i < 4; i++){
			r[i].setStroke(strokeColor);
			r2[i].setFill(nooseColor);
			r2[i].setFill(sketchPattern(7));
			noosePane.getChildren().add(r2[i]);
			noosePane.getChildren().add(r[i]);
			
		}
		
		pane.getChildren().add(noosePane);
		pane.setEffect(dropShadow());
		
		Text wordArea = coolText(300, 90, "");
		wordArea.setId("wordArea");
		pane.getChildren().add(wordArea);

		FadeTransition ft = new FadeTransition(Duration.millis(1200), noosePane);
		ft.setFromValue(0.0);
		ft.setToValue(1.0);
		ft.play();
		
		FadeTransition ft2 = new FadeTransition(Duration.millis(750), pane);
		ft2.setFromValue(0.0);
		ft2.setToValue(1.0);
		ft2.play();
		
		return pane;

	}

	final private FlowPane selectionPane(char[] cs){
		
		this.gameReset = true;
		
		DropShadow dropShadow = dropShadow();
		FlowPane pane = new FlowPane();
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setAlignment(Pos.TOP_LEFT);
		
		messageLabel = new Button();
		messageLabel.setFont(Font.font(java.awt.Font.SANS_SERIF, FontWeight.BOLD, 20));
		messageLabel.setMinSize(50, 50);
		messageLabel.setEffect(dropShadow);
		messageLabel.setStyle("-fx-text-fill: white; -fx-background-color: #178fff;");
		messageLabel.setTranslateX(-200);
		messageLabel.setTranslateY(10);
		
		pane.getChildren().add(messageLabel);
		
		b = new Button[cs.length];
		
		for(int i = 0; i < cs.length; i++){
			
			String letter = (cs[i] + "").toUpperCase();
			int index = i;
			
			b[i] = new Button();
			b[i].setText(letter);
			//b[i].setTranslateX((i * 100) - 600);
			//b[i].setTranslateY(0);
			b[i].setTranslateX(800);
			b[i].setTranslateY(10);
			
			b[i].setStyle("-fx-background-radius: 0; -fx-text-fill: white; -fx-background-color: #178fff;");
			b[i].setFont(Font.font(java.awt.Font.SANS_SERIF, FontWeight.BOLD, 20));
			//b[i].setPadding(new Insets(10,15,10,15));
			b[i].setMinSize(50, 50);
			b[i].setEffect(dropShadow);
			b[i].setOnMouseEntered(e -> {
				b[index].setStyle("-fx-text-fill: white; -fx-background-color: #30a0ff;");
			});
			b[i].setOnMouseExited(e -> {
				b[index].setStyle("-fx-text-fill: white; -fx-background-color: #178fff;");
			});
			b[i].setOnMousePressed(e -> {
				b[index].setStyle("-fx-text-fill: white; -fx-background-color: #077fdf;");
			});
			b[i].setOnAction(e -> {
				this.removeButton(index);
				Hangman.checkInput(b[index].getText());
			});
			pane.getChildren().add(b[i]);
			pane.setPadding(new Insets(10));
		}
		
		pAnimator = new LetterAnimator();
		pAnimator.addAnimation(pane.getChildren(), 300);
		
		return pane;
	}
	
	final private Text coolText(int x, int y, String s){
		
		Text text;
		
		if(x==-1 && x==-1){
			text = new Text(s);
		}else{
			text = new Text(x, y, s);
		}
		//text.setFill(sketchPattern(4));
		text.setFill(Color.WHITE);
		text.setStrokeWidth(1.5);
		text.setStroke(Color.WHITE);
		
		text.setEffect(dropShadow());
		text.setCache(true);
		
		try
	    { 
	      final Font f = Font.loadFont(new FileInputStream(new File("./res/Targa.Hand.ttf")), 40);
	      text.setFont(f); 
	    }
	    catch (FileNotFoundException e)
	    {
	      System.err.println("Unable to find font, loading plain type.");
	      e.printStackTrace();
	      text.setFont(Font.font(java.awt.Font.MONOSPACED,FontWeight.SEMI_BOLD,40));
	    }
		
		
		return text; 
	}
	
	final private DropShadow dropShadow(){
		
		DropShadow ds = new DropShadow();
		ds.setRadius(2.0);
		ds.setOffsetX(1.0f);
		ds.setOffsetY(2.0f);
		ds.setColor(Color.color(0, 0, 0, 0.4f));
		
		return ds;
	}
	
	final private ImagePattern sketchPattern(double width){
		
		
        Canvas canvas = new Canvas(width, width);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.web("#303030"));
        gc.fillRect(0, 0, width, width);
        
        gc.setStroke(Color.web("#ffffff99"));
        gc.setLineWidth(1);
        gc.strokeLine(width, 0, 0, width);
        //gc.strokeLine(width/2, width/2, 0, 0);
        
        
        Image image = canvas.snapshot(new SnapshotParameters(), null);
        ImagePattern pattern = new ImagePattern(image, 0, 0, width, width, false);

        return pattern;
        
	}
	
}
