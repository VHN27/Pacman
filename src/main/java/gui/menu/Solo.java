package gui.menu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import config.MazeConfig;
import gui.GameView;
import gui.InGameGui;
import gui.controller.Controller;
import gui.controller.PacmanController;
import model.MazeState;
import utils.Music;

public final class Solo extends Menu {
    public static final Solo INSTANCE = new Solo();

    private Solo() {
    }

    @Override
    public void start(final Stage primaryStage) {
    }

    /**
     * Affiche le menu Solo.
     */
    public void toSolo() {
        setScene();
        getMainStage().setScene(getMenuScene());
    }

    @Override
    protected void addOptionsToMenu() {
        final int titleSize = 100;
        final int spacingSize = 20;

        setTitleText(createText("Level Selector", titleSize));
        HBox title = createHBox(0, Pos.TOP_CENTER, getTitleText());

        Text select0 = createText(">", getFontSize().doubleValue());
        Text classic = createText("Classic", getFontSize().doubleValue());

        Text selectBack = createText(">", getFontSize().doubleValue());
        Text backText = createText("Back", getFontSize().doubleValue());

        //CHECKSTYLE:OFF
        if (getOptions().isEmpty()) {
            getOptions().add(createHBox(spacingSize, Pos.CENTER, select0, classic));
            getOptions().add(createHBox(spacingSize, Pos.BOTTOM_LEFT, selectBack, backText));
        }

        VBox optionBox = createVBox(
            spacingSize, Pos.CENTER,
            getOptions().get(0)
        );

        if (getSelects().isEmpty() && getChoices().isEmpty()) {
            getSelects().add(select0);
            getSelects().add(selectBack);

            getChoices().add(classic);
            getChoices().add(backText);
        }

        bindTextSizeToScene();

        getMenuPane().setTop(title);
        getMenuPane().setCenter(optionBox);
        getMenuPane().setBottom(getOptions().get(1));
        BorderPane.setMargin(title, new Insets(100, 0, 0, 0));
        //CHECKSTYLE:ON
    }

    @Override
    protected void addEventHandlers(final Scene gameScene) {
        Selector selector = new Selector(getSelects(), getChoices(), gameScene, false);

        getOptions().get(0).setOnMouseClicked(event -> {
            startLevel(0);
            selector.getKeySelector().resetSelect();
        });
        getOptions().get(1).setOnMouseClicked(event -> {
            MainMenu.INSTANCE.toMenu();
            selector.getKeySelector().resetSelect();
        });

        gameScene.setOnKeyPressed(keyEvent -> {
            final int back = 1;
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (Selector.getSelectedIndex() == back) {
                    MainMenu.INSTANCE.toMenu();
                } else {
                    startLevel(Selector.getSelectedIndex());
                }
                selector.getKeySelector().resetSelect();
            }
        });
    }

    /**
     * Méthode permettant de lancer un niveau, le niveau lancé dépend de
     * la variable statique {@code selected} de la classe {@code Menu}.
     * @param level
     */
    public static void startLevel(final int level) {
        InGameGui.setCurrentLevel(level);
        Music.stopBackgroundMusic();

        HBox box = new HBox();
        StackPane root = new StackPane(box);

        Scene gameScene = new Scene(
            root,
            getMainStage().getScene().getWidth(),
            getMainStage().getScene().getHeight()
        );

        MazeState maze;
        final int classic = 0;
        switch (level) {
            case classic:
                maze = new MazeState(MazeConfig.classicalConfig(false));
                break;

            default:
                maze = new MazeState(MazeConfig.classicalConfig(false));
                break;
        }

        getMainStage().setScene(gameScene);
        InGameGui inGameGui = new InGameGui(gameScene, "Solo");

        Controller.setInGameGui(inGameGui);
        var pacmanController = new PacmanController(maze.getConfig().getPacman());
        gameScene.addEventHandler(KeyEvent.KEY_PRESSED, pacmanController::keyPressedHandler);

        GameView.INSTANCE.setGameView(maze, inGameGui, box);
        GameView.INSTANCE.stopAnimation();
        GameView.INSTANCE.animate();
    }
}
