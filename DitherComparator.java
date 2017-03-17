import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Orientation;


import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DitherComparator extends Application {

    public static final String RANDOM_THRESHOLD_DITHER = "Random Threshold";
    public static final String BAYER2X2_DITHER = "Ordered 2x2";
    public static final String BAYER4X4_DITHER = "Ordered 4x4";
    public static final String BAYER8X8_DITHER = "Ordered 8x8";
    public static final String SIMPLE_DITHER = "Simple";

    private File loadedPath;
    private BufferedImage original;
    private BufferedImage output;
    private ImageView imageView = new ImageView();
    private ImageView outputView = new ImageView();
    private Image imagefx;
    private Image outputfx;
    private WritableImage outputWritableImage;
    private HBox horizBox = new HBox();
    private VBox vertBox = new VBox();
    private VBox layoutBox = new VBox();
    private Button lastButtonPressed;
    private ToolBar controlToolbar = new ToolBar();
    private ToolBar functionToolbar = new ToolBar();

    private static final Map<String, DitherGrayscale.Dither> functions;
    static
    {
        functions = new HashMap<String, DitherGrayscale.Dither>();
        functions.put(RANDOM_THRESHOLD_DITHER, DitherGrayscale.Dither.RANDOM);
        functions.put(BAYER2X2_DITHER, DitherGrayscale.Dither.BAYER2X2);
        functions.put(BAYER4X4_DITHER, DitherGrayscale.Dither.BAYER4X4);
        functions.put(BAYER8X8_DITHER, DitherGrayscale.Dither.BAYER8X8);
        functions.put(SIMPLE_DITHER, DitherGrayscale.Dither.SIMPLE);
    }

    class ditherButtonHandler implements EventHandler<ActionEvent> {
        private final String buttonString; //these final variables are very important. gotta research why.
        private final Button button;
        ditherButtonHandler(Button b) {
            this.buttonString = b.getText();
            this.button = b;
            // System.out.println(s.getText());
        }

        // Handler that calls algorithms
        @Override
        public void handle(ActionEvent event) {
            lastButtonPressed = this.button;
            if (original != null) {
                try {
                long startTime = System.nanoTime();
                // CAREFUL: (1) goes together and (2) is commented out
                //(1)output = (BufferedImage) DitherGrayscale.class.
                //(2)getDeclaredMethod(functions.get(buttonString), BufferedImage.class).invoke(null, original);
                //(1)getDeclaredMethod(functions.get(buttonString)).invoke(null);
                output = DitherGrayscale.dispatchDithering(functions.get(buttonString));
                long endTime = System.nanoTime();
                long timed = (endTime - startTime) / 1000000;
                System.out.println("algorithm time: " + timed);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long startTime = System.nanoTime();
                /*outputfx = */SwingFXUtils.toFXImage(output, outputWritableImage);

                // outputView.setImage(outputWritableImage);
                long endTime = System.nanoTime();
                long timed = (endTime - startTime) / 1000000;

                System.out.println("convert to javafx time:" + timed);
                // System.gc();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {

        functionToolbar.setOrientation(Orientation.VERTICAL);
        Button openButton = new Button();
        Button dither1Button = new Button();
        Button dither2Button = new Button();
        Button dither3Button = new Button();
        Button dither4Button = new Button();
        Button dither5Button = new Button();
        Button saveButton = new Button();
        ScrollPane scrollPane = new ScrollPane();
        Slider luminositySlider = new Slider(0, 3, 1.0);
        final TextField luminosityTextField = new TextField (Double.toString(luminositySlider.getValue()));
        luminositySlider.setShowTickMarks(true);

        openButton.setText("Open Image");
        dither1Button.setText(RANDOM_THRESHOLD_DITHER);
        dither2Button.setText(BAYER2X2_DITHER);
        dither3Button.setText(SIMPLE_DITHER);
        dither4Button.setText(BAYER4X4_DITHER);
        dither5Button.setText(BAYER8X8_DITHER);
        saveButton.setText("Save Image");

        dither1Button.setOnAction(new ditherButtonHandler(dither1Button));
        dither2Button.setOnAction(new ditherButtonHandler(dither2Button));
        dither3Button.setOnAction(new ditherButtonHandler(dither3Button));
        dither4Button.setOnAction(new ditherButtonHandler(dither4Button));
        dither5Button.setOnAction(new ditherButtonHandler(dither5Button));

        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // outputView.setFitWidth(400);
        outputView.setPreserveRatio(true);
        outputView.setSmooth(false);
        // outputView.setCache(true);

        openButton.setOnAction(new EventHandler<ActionEvent>() {

        // Open Dialog and set preview
        @Override
        public void handle(ActionEvent event) {
            FileChooser chooser = new FileChooser();
            if (loadedPath != null) {
                chooser.setInitialDirectory(loadedPath.getAbsoluteFile().getParentFile());
            }
            chooser.setTitle("Select file to load");
            File newFile = chooser.showOpenDialog(openButton.getScene().getWindow());
            if (newFile != null) {
                load(newFile);
                imageView.setImage(imagefx);
                System.out.println("Image loaded");
            }
        }
    });

    luminositySlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    DitherGrayscale.luminosityScale = new_val.doubleValue();
                    luminosityTextField.setText(String.format("%.3f", new_val));
                    lastButtonPressed.fire();
            }
        });

        saveButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            try {
                ImageIO.write(output, "PNG", new File("output.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    // Lambda function doesn't need to check for bounds, because the slider itself is bounded.
    // Textfield coupled to slider.
        luminosityTextField.setOnKeyPressed((event) -> { if(event.getCode() == KeyCode.ENTER)
            { luminositySlider.setValue(Double.valueOf(luminosityTextField.getText())); } });

        luminosityTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                    if (newPropertyValue) {

                    } else {
                        luminositySlider.setValue(Double.valueOf(luminosityTextField.getText()));
                    }
                }
            });

        // vertBox.getChildren().add(openButton);

        // vertBox.getChildren().add(dither1Button);

        horizBox.getChildren().add(vertBox);
        //horizBox.getChildren().add(imageView);
        horizBox.getChildren().add(outputView);

        controlToolbar.getItems().add(openButton);
        controlToolbar.getItems().add(saveButton);
        functionToolbar.getItems().add(dither1Button);
        functionToolbar.getItems().add(dither2Button);
        functionToolbar.getItems().add(dither3Button);
        functionToolbar.getItems().add(dither4Button);
        functionToolbar.getItems().add(dither5Button);
        functionToolbar.getItems().add(luminositySlider);
        functionToolbar.getItems().add(luminosityTextField);
        functionToolbar.getItems().add(imageView);


        StackPane root = new StackPane();
        // BorderPane border = new BorderPane();
        GridPane masterLayout = new GridPane();
        masterLayout.add(controlToolbar,1,0);
        masterLayout.add(functionToolbar,0,1);


        // root.getChildren().add(openButton);
        scrollPane.setContent(horizBox);
        // border.setTop(controlToolbar);
        // border.setLeft(functionToolbar);
        root.getChildren().add(scrollPane);


        masterLayout.add(scrollPane,1,1);
        // layoutBox.getChildren().add(border);
        // layoutBox.getChildren().add(root);

        // root.getChildren().add(imageView);

    Scene scene = new Scene(masterLayout, 900, 600);

        primaryStage.setTitle("Dither Comparator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void load(File file) {
        loadedPath = file;
        try {
            original = ImageIO.read(loadedPath);
            new DitherGrayscale(original);
            outputWritableImage = new WritableImage(original.getWidth(), original.getHeight());
            outputView.setImage(outputWritableImage);
            imagefx = SwingFXUtils.toFXImage(original, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
 public static void main(String[] args) {
        launch(args);
    }
}
