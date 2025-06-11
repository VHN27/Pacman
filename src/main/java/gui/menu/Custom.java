package gui.menu;

import java.util.ArrayList;
import java.util.List;

import config.Cell;
import config.MazeConfig;
import config.mazeGen.MainGrid;
import gui.GameView;
import gui.InGameGui;
import gui.controller.Controller;
import gui.controller.PacmanController;
import gui.controller.PlayerTwoController;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.DirectionBuffer;
import model.Ghost;
import model.Ghost.GhostType;
import model.MazeState;
import utils.Music;
import utils.Resources;

public final class Custom extends Menu {
    private ArrayList<Text> mainTexts;
    private ArrayList<Text> subTexts;
    private ArrayList<Button> buttons;
    private static int mazeWidth;
    private static int mazeHeight;
    private static String selectedMode;
    private static Cell[][] currentMap;

    public static final Custom INSTANCE = new Custom();

    private Custom() {
    }

    @Override
    public void start(final Stage primaryStage) {
    }

    /**
     * Affiche le menu Custom.
     */
    public void toCustom() {
        setScene();
        getMainStage().setScene(getMenuScene());
    }

    @Override
    protected void addOptionsToMenu() {
        final int titleSize = 100;
        final int spacingSize = 20;

        setTitleText(createText("Custom", titleSize));
        HBox title = createHBox(0, Pos.TOP_CENTER, getTitleText());

        Text selectStart = createText(">", getFontSize().doubleValue());
        selectStart.setVisible(false);
        Button startButton = new Button("Start");
        startButton.setFont(Font.loadFont(
            Resources.getPathOrContent("small_pixel-7"),
            getFontSize().doubleValue()
        ));
        startButton.setTextFill(Color.WHITE);
        startButton.setBackground(Background.EMPTY);
        startButton.focusedProperty().addListener(
            e -> selectStart.setVisible(startButton.isFocused())
        );

        Text selectMode = createText(">", getFontSize().doubleValue());
        selectMode.setVisible(false);
        Text mode = createText("Mode", getFontSize().doubleValue());
        String[] modes = {"Solo", "Versus", "Coop"};

        ChoiceBox<String> modeChoice = new ChoiceBox<>();
        modeChoice.getItems().addAll(modes);
        modeChoice.setValue(modes[0]);
        //CHECKSTYLE:OFF
        modeChoice.setStyle("-fx-font: " + getFontSize().doubleValue() / 3 + "px Monaco;");
        //CHECKSTYLE:ON
        setSelectedMode(modes[0]);
        modeChoice.valueProperty().addListener(
            (observable, oldValue, newValue) -> setSelectedMode(newValue)
        );
        modeChoice.focusedProperty().addListener(
            e -> selectMode.setVisible(modeChoice.isFocused())
        );
        HBox subModeBox = createHBox(spacingSize / 2, Pos.CENTER, selectMode, modeChoice);
        VBox modeBox = createVBox(0, Pos.CENTER, mode, subModeBox);

        Text size = createText("Size", getFontSize().doubleValue());
        Slider widthSlider = new Slider(
            MainGrid.getMinMaxLongueur()[0],
            MainGrid.getMinMaxLongueur()[1],
            (MainGrid.getMinMaxLongueur()[0] + MainGrid.getMinMaxLongueur()[1]) / 2.0
        );
        widthSlider.valueProperty().addListener(
            (observable, oldValue, newValue) -> setMazeWidth(newValue.intValue())
        );
        //CHECKSTYLE:OFF
        widthSlider.setShowTickLabels(true);
        widthSlider.setShowTickMarks(true);
        widthSlider.setMajorTickUnit(4);
        widthSlider.setMinorTickCount(1);
        widthSlider.setBlockIncrement(1);
        Text selectWidth = createText(">", getFontSize().doubleValue());
        selectWidth.setVisible(false);
        Text width = createText("Width", getFontSize().doubleValue());
        widthSlider.focusedProperty().addListener(
            e -> selectWidth.setVisible(widthSlider.isFocused())
        );
        HBox widthBox = createHBox(spacingSize, Pos.CENTER, selectWidth, width, widthSlider);

        Slider heightSlider = new Slider(
            MainGrid.getMinMaxHauteur()[0],
            MainGrid.getMinMaxHauteur()[1],
            (MainGrid.getMinMaxHauteur()[0] + MainGrid.getMinMaxHauteur()[1]) / 2.0
        );
        heightSlider.valueProperty().addListener(
            (observable, oldValue, newValue) -> setMazeHeight(newValue.intValue())
        );
        heightSlider.setShowTickLabels(true);
        heightSlider.setShowTickMarks(true);
        heightSlider.setMajorTickUnit(4);
        heightSlider.setMinorTickCount(1);
        heightSlider.setBlockIncrement(1);
        //CHECKSTYLE:ON
        Text selectHeight = createText(">", getFontSize().doubleValue());
        selectHeight.setVisible(false);
        Text height = createText("Height", getFontSize().doubleValue());
        heightSlider.focusedProperty().addListener(
            e -> selectHeight.setVisible(heightSlider.isFocused())
        );
        HBox heightBox = createHBox(spacingSize, Pos.CENTER, selectHeight, height, heightSlider);
        VBox sizeBox = createVBox(0, Pos.CENTER, size, widthBox, heightBox);

        VBox settings = createVBox(0, Pos.CENTER, modeBox, sizeBox);

        Text selectBack = createText(">", getFontSize().doubleValue());
        selectBack.setVisible(false);
        Button backButton = new Button("Back");
        backButton.setFont(
            Font.loadFont(Resources.getPathOrContent("small_pixel-7"), getFontSize().doubleValue())
        );
        backButton.setTextFill(Color.WHITE);
        backButton.setBackground(Background.EMPTY);
        backButton.focusedProperty().addListener(
            e -> selectBack.setVisible(backButton.isFocused())
        );

        VBox optionBox = createVBox(
            0, Pos.CENTER,
            createHBox(-spacingSize, Pos.CENTER, selectStart, startButton),
            createHBox(0, Pos.BOTTOM_CENTER, settings)
        );

        mainTexts = new ArrayList<>();
        subTexts = new ArrayList<>();
        buttons = new ArrayList<>();
        mainTexts.add(mode);
        mainTexts.add(size);
        subTexts.add(width);
        subTexts.add(height);
        buttons.add(startButton);
        buttons.add(backButton);

        getSelects().add(selectStart);
        getSelects().add(selectMode);
        getSelects().add(selectWidth);
        getSelects().add(selectHeight);
        getSelects().add(selectBack);

        bindTextSizeToScene();

        getMenuPane().setTop(title);
        getMenuPane().setCenter(optionBox);
        getMenuPane().setBottom(
            createHBox(-spacingSize, Pos.BASELINE_LEFT, selectBack, backButton)
        );
        //CHECKSTYLE:OFF
        BorderPane.setMargin(title, new Insets(100, 0, 0, 0));
        //CHECKSTYLE:ON
    }

    @Override
    protected void addEventHandlers(final Scene gameScene) {
        buttons.get(0).setOnMouseClicked(event -> startLevel(selectedMode, mazeWidth, mazeHeight));
        buttons.get(1).setOnMouseClicked(event -> MainMenu.INSTANCE.toMenu());

        buttons.get(0).setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                startLevel(selectedMode, mazeWidth, mazeHeight);
            }
        });
        buttons.get(1).setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                    MainMenu.INSTANCE.toMenu();
            }
        });
    }

    /**
     * Méthode permettant de lancer un niveau généré aléatoirement,
     * le mode (Solo / Versus / Coop) dépend de{@code mode}.
     * @param mode
     * @param width
     * @param height
     */
    public static void startLevel(final String mode, final int width, final int height) {
        Music.stopBackgroundMusic();

        HBox box = new HBox();
        StackPane root = new StackPane(box);

        Scene gameScene = new Scene(
            root,
            getMainStage().getScene().getWidth(),
            getMainStage().getScene().getHeight()
        );

        boolean solo = mode.equals("Solo");
        boolean versus = mode.equals("Versus");
        boolean coop = mode.equals("Coop");

        MazeState maze = new MazeState(MazeConfig.endlessConfig(coop, width, height));

        getMainStage().setScene(gameScene);
        InGameGui inGameGui = new InGameGui(gameScene, solo ? "Solo" : "Multi", true);

        Controller.setInGameGui(inGameGui);
        var pacmanController = new PacmanController(maze.getConfig().getPacman());
        gameScene.addEventHandler(KeyEvent.KEY_PRESSED, pacmanController::keyPressedHandler);

        if (versus) {
            List<Ghost> blinkys = maze.getConfig().getGhostKind(GhostType.BLINKY);
            Ghost playerGhost = blinkys.get(0);
            playerGhost.setDirectionBuffer(new DirectionBuffer());
            var ghostController = new PlayerTwoController(playerGhost);

            gameScene.addEventHandler(KeyEvent.KEY_PRESSED, ghostController::keyPressedHandler);
        } else if (coop) {
            var pacmanController2 = new PlayerTwoController(maze.getConfig().getPacman2());
            gameScene.addEventHandler(KeyEvent.KEY_PRESSED, pacmanController2::keyPressedHandler);
        }

        GameView.INSTANCE.setGameView(maze, inGameGui, box);
        GameView.INSTANCE.stopAnimation();
        GameView.INSTANCE.animate();
    }

    /**
     * Recommence un niveau en Endless.
     * @param mode
     */
    public void restartLevel(final String mode) {
        Music.stopBackgroundMusic();

        HBox box = new HBox();
        StackPane root = new StackPane(box);

        Scene gameScene = new Scene(
            root,
            getMainStage().getScene().getWidth(),
            getMainStage().getScene().getHeight()
        );

        boolean solo = mode.equals("Solo");
        boolean versus = mode.equals("Versus");
        boolean coop = mode.equals("Coop");

        MazeState maze = new MazeState(MazeConfig.restartedEndlessConfig(coop));

        getMainStage().setScene(gameScene);
        InGameGui inGameGui = new InGameGui(gameScene, solo ? "Solo" : "Multi", true);

        Controller.setInGameGui(inGameGui);
        var pacmanController = new PacmanController(maze.getConfig().getPacman());
        gameScene.addEventHandler(KeyEvent.KEY_PRESSED, pacmanController::keyPressedHandler);

        if (versus) {
            List<Ghost> blinkys = maze.getConfig().getGhostKind(GhostType.BLINKY);
            Ghost playerGhost = blinkys.get(0);
            playerGhost.setDirectionBuffer(new DirectionBuffer());
            var ghostController = new PlayerTwoController(playerGhost);

            gameScene.addEventHandler(KeyEvent.KEY_PRESSED, ghostController::keyPressedHandler);
        } else if (coop) {
            var pacmanController2 = new PlayerTwoController(maze.getConfig().getPacman2());
            gameScene.addEventHandler(KeyEvent.KEY_PRESSED, pacmanController2::keyPressedHandler);
        }

        GameView.INSTANCE.setGameView(maze, inGameGui, box);
        GameView.INSTANCE.stopAnimation();
        GameView.INSTANCE.animate();

    }

    @Override
    protected void bindTextSizeToScene() {
        super.bindTextSizeToScene();

        for (Text text : mainTexts) {
            text.styleProperty().bind(Bindings.concat("-fx-font-size: ",
            getFontSize().asString()));
        }
        for (Text text : subTexts) {
            text.styleProperty().bind(Bindings.concat("-fx-font-size: ",
            getFontSize().divide(2).asString()));
        }
    }

    public static Cell[][] getCurrentMap() {
        return currentMap;
    }

    public static void setCurrentMap(final Cell[][] currentMap) {
        Custom.currentMap = currentMap;
    }

    public static void setMazeWidth(final int mazeWidth) {
        Custom.mazeWidth = mazeWidth;
    }

    public static void setMazeHeight(final int mazeHeight) {
        Custom.mazeHeight = mazeHeight;
    }

    public static void setSelectedMode(final String selectedMode) {
        Custom.selectedMode = selectedMode;
    }

    public static String getSelectedMode() {
        return selectedMode;
    }

    public static int getMazeWidth() {
        return mazeWidth;
    }

    public static int getMazeHeight() {
        return mazeHeight;
    }
}
