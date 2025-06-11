package gui.menu;

import java.util.ArrayList;
import java.util.List;

import gui.GameView;
import gui.controller.Controller;
import gui.controller.PacmanController;
import gui.controller.PlayerTwoController;
import gui.graphics.CritterGraphicsFactory;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Direction;
import utils.Resources;

public abstract class Menu extends Application {
    protected static final int INITIAL_TEXT_SIZE = 45;
    private static DoubleProperty fontSize = new SimpleDoubleProperty(INITIAL_TEXT_SIZE);
    private Text titleText;
    private ArrayList<HBox> options = new ArrayList<>();
    private ArrayList<Text> selects = new ArrayList<>();
    private ArrayList<Text> choices = new ArrayList<>();

    /** Stage. */
    private static Stage mainStage;

    private StackPane mainPane;
    private BorderPane menuPane;
    private Scene menuScene;

    private StackPane helpCircle;
    private StackPane helpWindow;

    protected Menu() {
        menuPane = new BorderPane();
        menuPane.setBackground(getBackground());
        mainPane = new StackPane();
        mainPane.getChildren().add(menuPane);
        mainPane.setStyle("-fx-background-color: black");

        menuScene = new Scene(
            mainPane,
            mainStage.getScene().getWidth(),
            mainStage.getScene().getHeight()
        );
        menuScene.setFill(Color.BLACK);

        addHelper();
        addOptionsToMenu();
        addEventHandlers(getMenuScene());

        mainStage.heightProperty().addListener((obs, oldV, newV) -> {
            removeHelper();
            addHelper();
        });
    }

    /**
     * Initialise la scène en question et permet d'actualiser sa taille
     * lorque la méthode est appelée.
     */
    protected void setScene() {
        mainPane.getChildren().clear();
        mainPane.getChildren().add(menuPane);
        menuScene.setRoot(new HBox());
        menuScene = new Scene(
            mainPane, mainStage.getScene().getWidth(), mainStage.getScene().getHeight()
        );

        addHelper();
        addOptionsToMenu();
        addEventHandlers(menuScene);
    }

    /**
     * Ajoute les options au {@link #menuPane}.
     */
    protected void addOptionsToMenu() { }

    /**
     * Ajoute les différents{@code EventHandler}(souris, clavier).
     * @param scene
     */
    protected void addEventHandlers(final Scene scene) { }

    protected final void addHelper() {
        addHelpWindow();
        addHelpCircle();
    }

    protected final void removeHelper() {
        mainPane.getChildren().remove(helpCircle);
        mainPane.getChildren().remove(helpWindow);
    }

    private void addHelpCircle() {
        final double radius = GameView.getScale() * 1.5;
        helpCircle = new StackPane();

        Circle circle = new Circle();
        circle.setRadius(radius);
        circle.setFill(Color.WHITE);

        Text questionMark = createText("?", GameView.getScale() * 2);
        questionMark.setFill(Color.BLACK);

        StackPane.setAlignment(circle, Pos.CENTER);
        StackPane.setAlignment(questionMark, Pos.CENTER);

        helpCircle.setMaxSize(INITIAL_TEXT_SIZE, INITIAL_TEXT_SIZE);
        helpCircle.setTranslateX(-INITIAL_TEXT_SIZE / 2.0);
        helpCircle.setTranslateY(-INITIAL_TEXT_SIZE / 2.0);
        helpCircle.getChildren().addAll(circle, questionMark);
        StackPane.setAlignment(helpCircle, Pos.BOTTOM_RIGHT);

        mainPane.getChildren().add(helpCircle);
        helpCircle.setOnMouseClicked(event -> helpWindow.setVisible(!helpWindow.isVisible()));
        menuScene.setOnKeyTyped(event -> {
            if (event.getCharacter().equals("?") || event.getCharacter().equals(",")) {
                helpWindow.requestFocus();
                helpWindow.setVisible(!helpWindow.isVisible());
            }
        });
    }

    private void addHelpWindow() {
        final int margin = 200;
        final double boxSpacing = mainStage.getWidth() / 8;
        final double keySpacing = mainStage.getWidth() / 70;

        Text player1 = createText("Player 1", fontSize.doubleValue());
        ImageView pacImgPlayerOne = new ImageView(Resources.getImage("pacman_3"));
        pacImgPlayerOne.setPreserveRatio(true);
        pacImgPlayerOne.setSmooth(true);
        Text player2 = createText("Player 2", fontSize.doubleValue());
        ImageView pacImgPlayerTwo = new ImageView(Resources.getImage("pacman_rainbow_3"));
        pacImgPlayerTwo.setPreserveRatio(true);
        pacImgPlayerTwo.setSmooth(true);

        VBox left = new VBox(player1, pacImgPlayerOne);
        left.setAlignment(Pos.CENTER);
        left.setSpacing(keySpacing);

        VBox right = new VBox(player2, pacImgPlayerTwo);
        right.setAlignment(Pos.CENTER);
        right.setSpacing(keySpacing);

        //CHECKSTYLE:OFF
        Text title1 = createText("Press ESC to pause in game!", fontSize.doubleValue());
        Text title2 = createText(
            "Try your keybinds by pressing the corresponding button!",
            fontSize.divide(1.5).doubleValue()
        );
        //CHECKSTYLE:ON
        HBox hbox = createHBox(boxSpacing, Pos.CENTER, left, right);

        VBox vbox = createVBox(margin / 2, Pos.CENTER, title1, title2, hbox);
        helpWindow = new StackPane(vbox);

        helpWindow.setMaxSize(mainStage.getWidth() - margin, mainStage.getHeight() - margin);
        helpWindow.setStyle("-fx-background-color: #0f0f0f; -fx-background-radius: 50 50 50 50");
        helpWindow.setVisible(false);
        StackPane.setAlignment(helpWindow, Pos.CENTER);

        StackPane upPlayerOne = getNewKey(Controller.getKeyCode(
            PacmanController.getKeybinds(), Direction.NORTH
        ).toString());
        StackPane leftPlayerOne = getNewKey(Controller.getKeyCode(
            PacmanController.getKeybinds(), Direction.WEST
        ).toString());
        StackPane downPlayerOne = getNewKey(Controller.getKeyCode(
            PacmanController.getKeybinds(), Direction.SOUTH
        ).toString());
        StackPane rightPlayerOne = getNewKey(Controller.getKeyCode(
            PacmanController.getKeybinds(), Direction.EAST
        ).toString());
        HBox threeKeysPlayerOne = new HBox(leftPlayerOne, downPlayerOne, rightPlayerOne);
        threeKeysPlayerOne.setSpacing(keySpacing);
        left.getChildren().addAll(upPlayerOne, threeKeysPlayerOne);

        StackPane upPlayerTwo = getNewKey(Controller.getKeyCode(
            PlayerTwoController.getKeybinds(), Direction.NORTH
        ).toString());
        StackPane leftPlayerTwo = getNewKey(Controller.getKeyCode(
            PlayerTwoController.getKeybinds(), Direction.WEST
        ).toString());
        StackPane downPlayerTwo = getNewKey(Controller.getKeyCode(
            PlayerTwoController.getKeybinds(), Direction.SOUTH
        ).toString());
        StackPane rightPlayerTwo = getNewKey(Controller.getKeyCode(
            PlayerTwoController.getKeybinds(), Direction.EAST
        ).toString());
        HBox threeKeysPlayerTwo = new HBox(leftPlayerTwo, downPlayerTwo, rightPlayerTwo);
        threeKeysPlayerTwo.setSpacing(keySpacing);
        right.getChildren().addAll(upPlayerTwo, threeKeysPlayerTwo);

        mainPane.getChildren().add(helpWindow);

        addHelperHandlers(
            pacImgPlayerOne, pacImgPlayerTwo,
            upPlayerOne, leftPlayerOne, downPlayerOne, rightPlayerOne,
            upPlayerTwo, leftPlayerTwo, downPlayerTwo, rightPlayerTwo
        );
    }

    private StackPane getNewKey(final String key) {
        final double arcSize = 20.0;
        final double keySize = 5 * GameView.getScale();
        final Color keyColor = Color.rgb(220, 220, 220);

        //CHECKSTYLE:OFF
        Text label = createText(key, fontSize.doubleValue() / 2.5);
        //CHECKSTYLE:ON
        Rectangle rec = new Rectangle();
        StackPane keyPane = new StackPane(rec, label);

        StackPane.setAlignment(label, Pos.CENTER);
        StackPane.setAlignment(rec, Pos.CENTER);
        label.setFill(Color.BLACK);
        rec.setWidth(keySize);
        rec.setHeight(keySize);
        rec.setFill(keyColor);
        rec.setArcWidth(arcSize);
        rec.setArcHeight(arcSize);
        keyPane.setMaxSize(rec.getWidth(), rec.getHeight());

        return keyPane;
    }

    private void addHelperHandlers(
    final ImageView p1, final ImageView p2, final StackPane... panes) {
        for (int i = 0; i < panes.length; i++) {
            final int index = i;
            //CHECKSTYLE:OFF
            switch (index) {
                case 0:
                    setOneKeyHandler(
                        p1, true, (Rectangle) panes[index].getChildren().get(0), Direction.NORTH
                    );
                    break;
                case 1:
                    setOneKeyHandler(
                        p1, true, (Rectangle) panes[index].getChildren().get(0), Direction.WEST
                    );
                    break;
                case 2:
                    setOneKeyHandler(
                        p1, true, (Rectangle) panes[index].getChildren().get(0), Direction.SOUTH
                    );
                    break;
                case 3:
                    setOneKeyHandler(
                        p1, true, (Rectangle) panes[index].getChildren().get(0), Direction.EAST
                    );
                    break;
                case 4:
                    setOneKeyHandler(
                        p2, false, (Rectangle) panes[index].getChildren().get(0), Direction.NORTH
                    );
                    break;
                case 5:
                    setOneKeyHandler(
                        p2, false, (Rectangle) panes[index].getChildren().get(0), Direction.WEST
                    );
                    break;
                case 6:
                    setOneKeyHandler(
                        p2, false, (Rectangle) panes[index].getChildren().get(0), Direction.SOUTH
                    );
                    break;
                case 7:
                    setOneKeyHandler(
                        p2, false, (Rectangle) panes[index].getChildren().get(0), Direction.EAST
                    );
                    break;

                default:
                    break;
            }
            //CHECKSTYLE:ON
        }
    }

    private void setOneKeyHandler(
    final ImageView image, final boolean isPlayerOne, final Rectangle key, final Direction dir) {
        final Color keyColor = Color.rgb(220, 220, 220);
        if (isPlayerOne) {
            menuScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode().equals(Controller.getKeyCode(
                PacmanController.getKeybinds(), dir))) {
                    key.setFill(Color.DODGERBLUE);
                    CritterGraphicsFactory.rotateImageByDirection(image, dir);
                }
            });
            menuScene.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                if (event.getCode().equals(Controller.getKeyCode(
                PacmanController.getKeybinds(), dir))) {
                    key.setFill(keyColor);
                }
            });
        } else {
            menuScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode().equals(Controller.getKeyCode(
                PlayerTwoController.getKeybinds(), dir))) {
                    key.setFill(Color.TOMATO);
                    CritterGraphicsFactory.rotateImageByDirection(image, dir);
                }
            });
            menuScene.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                if (event.getCode().equals(Controller.getKeyCode(
                PlayerTwoController.getKeybinds(), dir))) {
                    key.setFill(keyColor);
                }
            });
        }
    }

    /**
     * Relie la taille du texte à la taille de la fenêtre.
     */
    protected void bindTextSizeToScene() {
        final int scale = 30;
        final int titleScale = 50;
        final String fxFontSize = "-fx-font-size: ";

        fontSize.bind(
            getMenuScene().widthProperty().divide(scale).add(
                getMenuScene().heightProperty().divide(scale)
            )
        );

        titleText.styleProperty().bind(
            Bindings.concat(fxFontSize, fontSize.add(titleScale).asString())
        );
        for (Text text : choices) {
            text.styleProperty().bind(Bindings.concat(fxFontSize, fontSize.asString()));
        }
        for (Text text : selects) {
            text.styleProperty().bind(Bindings.concat(fxFontSize, fontSize.asString()));
        }
    }

    /** Permet d'avoir le background.
     * @return {@code background}
     */
    protected static Background getBackground() {
        Image image = Resources.getImage("background");

        BackgroundImage backgroundImage = new BackgroundImage(
            image,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(
                mainStage.getWidth(),
                mainStage.getHeight(),
                false,
                false,
                true,
                false
            ));
        return new Background(backgroundImage);
    }

    /**
     * Méthode qui retourne un{@code Text}initialisé avec les paramètres
     * {@code str}et{@code size}.
     * @param str  Le texte contenu dans le nouveau{@code Text}
     * @param size La taille du{@code Text}
     * @return {@code Text}
     */
    public static Text createText(final String str, final double size) {
        Text text = new Text(str);
        text.setFill(Color.WHITE);
        text.setFont(Font.loadFont(Resources.getPathOrContent("small_pixel-7"), size));
        return text;
    }

    /**
     * Méthode qui retourne un{@code VBox}initialisé avec les paramètres
     * {@code spacing},{@code position}et{@code nodes}.
     * @param spacing  L'espace entre les différents{@code Node}
     * @param position L'alignement des{@code Node}
     * @param nodes    Les{@code Node}à mettre dans la{@code VBox}
     * @return {@code VBox}
     */
    public static VBox createVBox(
    final double spacing, final Pos position, final Node... nodes) {
        VBox box = new VBox(spacing);
        box.getChildren().addAll(nodes);
        box.setAlignment(position);
        return box;
    }

    /**
     * Méthode qui retourne un{@code HBox}initialisé avec les paramètres
     * {@code spacing},{@code position}et{@code nodes}.
     * @param spacing  L'espace entre les différents{@code Node}
     * @param position L'alignement des{@code Node}
     * @param nodes    Les{@code Node}à mettre dans la{@code HBox}
     * @return {@code HBox}
     */
    public static HBox createHBox(
    final double spacing, final Pos position, final Node... nodes) {
        HBox box = new HBox(spacing);
        box.getChildren().addAll(nodes);
        box.setAlignment(position);
        return box;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setMainStage(final Stage mainStage) {
        Menu.mainStage = mainStage;
    }

    public final BorderPane getMenuPane() {
        return menuPane;
    }

    public final Scene getMenuScene() {
        return menuScene;
    }

    public static DoubleProperty getFontSize() {
        return fontSize;
    }

    public final Text getTitleText() {
        return titleText;
    }

    public final List<HBox> getOptions() {
        return options;
    }

    public final List<Text> getSelects() {
        return selects;
    }

    public final List<Text> getChoices() {
        return choices;
    }

    public final void setTitleText(final Text titleText) {
        this.titleText = titleText;
    }
}
