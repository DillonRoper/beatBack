import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;


public class Client extends Application {
    private int targetBpm;
    private int userBpm = 0;
    private int time = 0;
    private XYChart.Series target = new XYChart.Series();
    private XYChart.Series user = new XYChart.Series();
    private Timeline metro;
    private Timeline update;

    public static void main(String[] args) {
        launch(args);
    }

    public class Metronome {
        private Clip click;

        public Metronome() throws Exception {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(
                getClass().getResourceAsStream("/click.wav"))) {
                click = AudioSystem.getClip();
                click.open(ais);
            }
        }

        public void tick() {
            if (click.isRunning()) {
                click.stop();           // rewind if still playing
            }
            click.setFramePosition(0);  // rewind to start
            click.start();              // fire
        }

        public void update() {
            target.getData().add(new XYChart.Data(time, targetBpm)); // add target data
            user.getData().add(new XYChart.Data(time, 50)); // add user data
            /*if (target.getData().size() > 60) {
                removeDataItemFromDisplay(target, target.getData().remove(0));
                removeDataItemFromDisplay(user, user.getData().remove(0));
            }*/
            time++;
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // metronome set-up
        Metronome m = new Metronome();
        metro = new Timeline();
        update = new Timeline();

        // stage set-up
        primaryStage.setTitle("primaryStage");
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        primaryStage.setFullScreen(true);

        // layout box (vertical)
        VBox box = new VBox(screenBounds.getHeight()/50);
        box.setId("mainBox");
        box.setAlignment(Pos.CENTER);

        // title text
        Label title = newLabel("TempoHero", Color.BLACK, Font.font("Helvetica", FontWeight.BOLD, FontPosture.ITALIC, screenBounds.getHeight()/10), box);
        VBox.setVgrow(title, Priority.ALWAYS);

        // graph
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number,Number> chart = new LineChart<Number,Number>(xAxis, yAxis);
        xAxis.setLabel("Time (seconds)");
        yAxis.setLabel("Beats Per Minute");
        target = new XYChart.Series();
        target.setName("Target BPM");
        user = new XYChart.Series();
        user.setName("Your BPM");
        box.getChildren().add(chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        chart.getData().add(user);
        chart.getData().add(target);

        // bpm input layout box (horizontal)
        HBox bpmBox = new HBox();
        bpmBox.setAlignment(Pos.CENTER);
        box.getChildren().add(bpmBox);
        VBox.setVgrow(bpmBox, Priority.ALWAYS);

        // bpm label for input area
        Label bpmLabel = newLabel("BPM: ", Color.BLACK, Font.font("Times New Roman", screenBounds.getHeight()/20), bpmBox);
        HBox.setHgrow(bpmLabel, Priority.ALWAYS);

        // bpm input field
        TextField enterBpm = new TextField(Integer.toString(targetBpm));
        bpmBox.getChildren().add(enterBpm);
        HBox.setHgrow(enterBpm, Priority.ALWAYS);
        bpmBox.setMaxWidth(screenBounds.getWidth()*0.8);
        enterBpm.setFont(Font.font("Times New Roman", screenBounds.getHeight()/30));

        // changes bpm when someone hits enter on the field
        enterBpm.setOnAction(e -> {
            targetBpm = Integer.parseInt(enterBpm.getText());
            double millis = 60000.0 / targetBpm;
            if (metro != null) metro.stop();
            metro = new Timeline(new KeyFrame(Duration.millis(millis), ev -> m.tick()));
            metro.setCycleCount(Animation.INDEFINITE);
            metro.play();
            update.stop();
            update = new Timeline(new KeyFrame(Duration.millis(1000), ev -> m.update()));
            update.setCycleCount(Animation.INDEFINITE);
            update.play();

        });

        // starts the program
        Scene scene = new Scene (
        box, 
        screenBounds.getWidth()-50,
        screenBounds.getHeight()-100
        );
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Label newLabel(String text, Color color, Font font, Pane parent) {
        Label label = new Label(text);
        label.setTextFill(color);
        label.setFont(font);
        parent.getChildren().add(label);
        return label;
    }
    
}