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
import javafx.scene.control.CheckBox;
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
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class DitherComparator extends Application {
    private static final Logger LOGGER = Logger.getLogger( DitherComparator.class.getName() );
    private static FileHandler fh = null; //MARK

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
    private Label controlPixelLabel = new Label();
    private CheckBox controlBWBox = new CheckBox("Black & White");
    private ImageData loadedImage;

    private Map<Button, Ditherable.Dither> functions = new HashMap<Button, Ditherable.Dither>();

    // custom handler for multiple dither buttons
    class ditherButtonHandler implements EventHandler<ActionEvent> {
        private final String buttonString; //these final variables are very important. gotta research why.
        private final Button button;
        ditherButtonHandler(Button b) {
            this.buttonString = b.getText();
            this.button = b;
        }

        // Handler that calls algorithms
        @Override
        public void handle(ActionEvent event) {
            lastButtonPressed = this.button; //MARK
            if (original != null) {
                try {
                long startTime = System.nanoTime();
                // CAREFUL: (1) goes together and (2) is commented out
                //(1)output = (BufferedImage) DitherGrayscale.class.
                //(2)getDeclaredMethod(functions.get(buttonString), BufferedImage.class).invoke(null, original);
                //(1)getDeclaredMethod(functions.get(buttonString)).invoke(null);
                if (controlBWBox.isSelected()) {
                    DitherGrayscale.dispatchDithering(loadedImage, functions.get(button));
                } else {
                    DitherColor.dispatchDithering(loadedImage, functions.get(button));
                }

                // output = DitherGrayscale.dispatchDithering(functions.get(button)); //MARK
                long endTime = System.nanoTime();
                long timed = (endTime - startTime) / 1000000;
                LOGGER.log(Level.CONFIG, "DoingWork");
                System.out.println("algorithm time: " + timed);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long startTime = System.nanoTime();
                SwingFXUtils.toFXImage(loadedImage.output, outputWritableImage);

                long endTime = System.nanoTime();
                long timed = (endTime - startTime) / 1000000;

                System.out.println("convert to javafx time:" + timed);
            }
        }
    }

    private void createDitherButton(Button b, Ditherable.Dither d) {
        b.setId(b.getText());
        b.setOnAction(new ditherButtonHandler(b));
        functions.put(b, d);
        functionToolbar.getItems().add(b);
    }

    @Override
    public void start(Stage primaryStage) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
        // scrollPane.setFitToHeight(true);
        // scrollPane.setFitToWidth(true);
        functionToolbar.setOrientation(Orientation.VERTICAL);
        Button controlOpenButton = new Button("Open Image");
        Button controlSaveButton = new Button("Save Image");
        Button controlBayerButton = new Button("Modify Bayer");
        Button controlZoomInButton = new Button(" + ");
        Button controlZoomOutButton = new Button(" - ");
        Button controlZoomResetButton = new Button(" = ");
        Slider luminositySlider = new Slider(0, 3, 1.0);
        luminositySlider.setShowTickMarks(true);
        TextField luminosityTextField = new TextField (Double.toString(luminositySlider.getValue()));

        createDitherButton(new Button("Random"), Ditherable.Dither.RANDOM);
        createDitherButton(new Button("Bayer 2x2"), Ditherable.Dither.BAYER2X2);
        createDitherButton(new Button("Bayer 4x4"), Ditherable.Dither.BAYER4X4);
        createDitherButton(new Button("Bayer 8x8"), Ditherable.Dither.BAYER8X8);
        createDitherButton(new Button("Simple"), Ditherable.Dither.SIMPLE);
        createDitherButton(new Button("FloydStein"), Ditherable.Dither.FS);

        // controlOpenButton.setText("Open Image");
        // controlSaveButton.setText("Save Image");

        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        outputView.setPreserveRatio(true);
        outputView.setSmooth(false);

        controlOpenButton.setOnAction(new EventHandler<ActionEvent>() {

        /////////////////////////////////
        // Control Handlers
        /////////////////////////////////

        // Open Dialog and set preview
        @Override
        public void handle(ActionEvent event) {
            FileChooser chooser = new FileChooser();
            if (loadedPath != null) {
                chooser.setInitialDirectory(loadedPath.getAbsoluteFile().getParentFile());
            }
            chooser.setTitle("Select image to load");
            File newFile = chooser.showOpenDialog(controlOpenButton.getScene().getWindow());
            if (newFile != null) {
                load(newFile);
                imageView.setImage(imagefx);
                System.out.println("Image loaded");
            }
        }
    });

    //Save button handler
    controlSaveButton.setOnAction(new EventHandler<ActionEvent>() {
    @Override
    public void handle(ActionEvent event) {
        try {
            ImageIO.write(loadedImage.output, "PNG", new File("output.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    });

    //Bayer matrix modify handler
    controlBayerButton.setOnAction(new EventHandler<ActionEvent>() {
    @Override
    public void handle(ActionEvent event) {

    }
    });

    controlZoomInButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            outputView.setScaleX(outputView.getScaleX() * 2.);
            outputView.setScaleY(outputView.getScaleY() * 2.);
            // outputView.setTranslateX(outputView.getTranslateX());
            // outputView.setTranslateY(outputView.getTranslateY());
            // scrollPane.setScaleX(scrollPane.getScaleX() * 2.);
            // scrollPane.setScaleY(scrollPane.getScaleY() * 2.);
            // scrollPane.setTranslateX(200 );
            // scrollPane.setTranslateY(200 );
        }
    });

    controlZoomOutButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            outputView.setScaleX(outputView.getScaleX() / 2.);
            outputView.setScaleY(outputView.getScaleY() / 2.);
            // horizBox.setScaleX(horizBox.getScaleX() / 2.);
            // horizBox.setScaleY(horizBox.getScaleY() / 2.);
            // outputView.setTranslateX(0 + outputView.getScene().getWidth()/4);
            // outputView.setTranslateY(0 + outputView.getScene().getHeight()/4);
        }
    });

    controlZoomResetButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            outputView.setScaleX(1.);
            outputView.setScaleY(1.);
            // horizBox.setScaleX(1.);
            // horizBox.setScaleY(1.);
            outputView.setTranslateX(0);
            outputView.setTranslateY(0);
        }
    });


    ///////////////////////////
    // Function handlers
    ///////////////////////////

        // Slider handler
    luminositySlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    loadedImage.luminosityScale = new_val.doubleValue();
                    luminosityTextField.setText(String.format("%.3f", new_val));
                    lastButtonPressed.fire();
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


        horizBox.getChildren().add(vertBox);
        horizBox.getChildren().add(outputView);

        controlToolbar.getItems().add(controlOpenButton);
        controlToolbar.getItems().add(controlSaveButton);
        controlToolbar.getItems().add(controlBayerButton);
        controlToolbar.getItems().add(controlZoomOutButton);
        controlToolbar.getItems().add(controlZoomInButton);
        controlToolbar.getItems().add(controlZoomResetButton);
        controlToolbar.getItems().add(controlPixelLabel);
        controlToolbar.getItems().add(controlBWBox);

        functionToolbar.getItems().add(luminositySlider);
        functionToolbar.getItems().add(luminosityTextField);
        functionToolbar.getItems().add(imageView);

        GridPane masterLayout = new GridPane();
        masterLayout.add(controlToolbar,1,0);
        masterLayout.add(functionToolbar,0,1);

        scrollPane.setContent(horizBox);
        masterLayout.add(scrollPane,1,1);

        Scene scene = new Scene(masterLayout, 900, 600);

        primaryStage.setTitle("Dither Comparator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void load(File file) {
        loadedPath = file;
        try {
            original = ImageIO.read(loadedPath);
            loadedImage = new ImageData(original);
            // new DitherColor(original);
            outputWritableImage = new WritableImage(original.getWidth(), original.getHeight());
            outputView.setImage(outputWritableImage);
            imagefx = SwingFXUtils.toFXImage(original, null);
            // fh=new FileHandler("loggerExample.log", false); //file.getName(); //MARK
            // Logger l = Logger.getLogger("");
            // fh.setFormatter(new SimpleFormatter());
            // l.addHandler(fh);
		    // l.setLevel(Level.CONFIG);
            controlPixelLabel.setText("W: " + original.getWidth() + " H: " + original.getHeight());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
 public static void main(String[] args) {
        launch(args);
    }
}
