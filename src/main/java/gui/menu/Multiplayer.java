package gui.menu;

import java.util.List;

import config.MazeConfig;
import gui.GameView;
import gui.InGameGui;
import gui.controller.Controller;
import gui.controller.PacmanController;
import gui.controller.PlayerTwoController;
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
import model.DirectionBuffer;
import model.Ghost;
import model.MazeState;
import model.Ghost.GhostType;
import utils.Music;

public final class Multiplayer extends Menu {
    public static final Multiplayer INSTANCE = new Multiplayer();

    private Multiplayer() {
    }

    @Override
    public void start(final Stage primaryStage) {
    }

    /**
     * Affiche le menu Multiplayer.
     */
    public void toMulti() {
        setScene();
        getMainStage().setScene(getMenuScene());
    }

    /**
     * Méthode permettant de lancer un niveau, le niveau lancé dépend de
     * la variable statique {@code selected} de la classe {@code Menu}.
     * @param mode
     */
    public static void startLevel(final int mode) {
        InGameGui.setCurrentLevel(mode);
        Music.stopBackgroundMusic();

        HBox box = new HBox();
        StackPane root = new StackPane(box);

        Scene gameScene = new Scene(
            root,
            getMainStage().getScene().getWidth(),
            getMainStage().getScene().getHeight()
        );

        MazeState maze;
        final int versus = 0;
        final int coop = 1;
        switch (mode) {
            case versus:
                maze = new MazeState(MazeConfig.classicalConfig(false));
                break;
            case coop:
                maze = new MazeState(MazeConfig.classicalConfig(true));
                break;

            default:
                maze = new MazeState(MazeConfig.classicalConfig(false));
                break;
        }

        getMainStage().setScene(gameScene);
        InGameGui inGameGui = new InGameGui(gameScene, "Multi");

        Controller.setInGameGui(inGameGui);
        var pacmanController = new PacmanController(maze.getConfig().getPacman());
        gameScene.addEventHandler(KeyEvent.KEY_PRESSED, pacmanController::keyPressedHandler);

        if (mode == versus) {
            List<Ghost> blinkys = maze.getConfig().getGhostKind(GhostType.BLINKY);
            Ghost playerGhost = blinkys.get(0);
            playerGhost.setDirectionBuffer(new DirectionBuffer());
            var ghostController = new PlayerTwoController(playerGhost);

            gameScene.addEventHandler(KeyEvent.KEY_PRESSED, ghostController::keyPressedHandler);
        } else if (mode == coop) {
            var pacmanController2 = new PlayerTwoController(maze.getConfig().getPacman2());
            gameScene.addEventHandler(KeyEvent.KEY_PRESSED, pacmanController2::keyPressedHandler);
        }

        GameView.INSTANCE.setGameView(maze, inGameGui, box);
        GameView.INSTANCE.stopAnimation();
        GameView.INSTANCE.animate();
    }

    @Override
    protected void addOptionsToMenu() {
        final int titleSize = 100;
        final int spacingSize = 20;

        setTitleText(createText("Game Selection", titleSize));
        HBox title = createHBox(0, Pos.TOP_CENTER, getTitleText());

        Text select0 = createText(">", getFontSize().doubleValue());
        Text versus = createText("Versus", getFontSize().doubleValue());

        Text select1 = createText(">", getFontSize().doubleValue());
        Text coop = createText("Coop", getFontSize().doubleValue());

        Text selectBack = createText(">", getFontSize().doubleValue());
        Text back = createText("Back", getFontSize().doubleValue());

        if (getOptions().isEmpty()) {
            getOptions().add(createHBox(
                spacingSize, Pos.CENTER, select0, versus
            ));
            getOptions().add(createHBox(
                spacingSize, Pos.CENTER, select1, coop
            ));
            getOptions().add(createHBox(
                spacingSize, Pos.BOTTOM_LEFT, selectBack, back
            ));
        }

        VBox optionBox = createVBox(
            spacingSize, Pos.CENTER, getOptions().get(0), getOptions().get(1)
        );

        if (getSelects().isEmpty() && getChoices().isEmpty()) {
            getSelects().add(select0);
            getSelects().add(select1);
            getSelects().add(selectBack);

            getChoices().add(versus);
            getChoices().add(coop);
            getChoices().add(back);
        }

        bindTextSizeToScene();

        getMenuPane().setTop(title);
        getMenuPane().setCenter(optionBox);
        getMenuPane().setBottom(getOptions().get(2));
        //CHECKSTYLE:OFF
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
            startLevel(1);
            selector.getKeySelector().resetSelect();
        });
        getOptions().get(2).setOnMouseClicked(event -> {
            MainMenu.INSTANCE.toMenu();
            selector.getKeySelector().resetSelect();
        });

        gameScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (Selector.getSelectedIndex() == 2) {
                    MainMenu.INSTANCE.toMenu();
                } else {
                    startLevel(Selector.getSelectedIndex());
                }
                selector.getKeySelector().resetSelect();
            }
        });
    }
}
