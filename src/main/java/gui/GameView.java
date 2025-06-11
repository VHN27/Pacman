package gui;

import geometry.IntCoordinates;
import geometry.RealCoordinates;
import gui.graphics.CellGraphicsFactory;
import gui.graphics.CritterGraphicsFactory;
import gui.graphics.GraphicsUpdater;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.Ghost;
import model.MazeState;
import model.PacMan;
import utils.Fps;
import utils.Music;
import utils.Resources;

import java.util.ArrayList;
import java.util.List;

public final class GameView {
    /** La carte qui sera affichée. */
    private MazeState instMaze;
    /** {@code HBox}contenant {@link #mapBox} (barre de progression + map) et le score + vie. */
    private HBox mainBox;
    /** {@code VBox}contenant la barre de progression et la map. */
    private VBox mapBox;
    /** {@code StackPane}contenant la map et les critters. */
    private StackPane mapStack;
    /** Map node. */
    private GridPane mapPane;
    /** L'AnimationTimer qui appelle les fonctions d'update. */
    private AnimationTimer animationTimer;
    /** Liste d'éléments à update. */
    private List<GraphicsUpdater> graphicsUpdaters;

    private static final double INITIAL_SCALE = 12;
    /** Nombre de pixels représentant une unité de la map. */
    private static double scale = INITIAL_SCALE;
    private InGameGui inGameGui;

    private double timeSinceAnimationStart = 0;

    private GameView() {
    }

    /** Implémentation en singleton. */
    public static final GameView INSTANCE = new GameView();

    /**
     * @param maze le "modèle" de cette vue (le labyrinthe et tout ce qui s'y trouve)
     * @param newInGameGui {@code InGameGui}courant
     * @param box {@code HBox}contenant la map, le score et les vies.
     */
    public void setGameView(
    final MazeState maze, final InGameGui newInGameGui, final HBox box) {
        final int spacing = 100;

        instMaze = maze;
        inGameGui = newInGameGui;
        mainBox = box;
        mapBox = new VBox();
        mapStack = new StackPane();
        mapPane = new GridPane();

        mainBox.setAlignment(Pos.CENTER);
        mainBox.setSpacing(spacing);
        mainBox.getChildren().add(mapBox);
        mainBox.setStyle("-fx-background-color: #000000");

        mapBox.setAlignment(Pos.CENTER);
        mapBox.getChildren().add(0, mapStack);
        mapBox.setFillWidth(true);

        mapStack.getChildren().add(mapPane);

        var critterFactory = new CritterGraphicsFactory((int) scale);
        var cellFactory = new CellGraphicsFactory((int) scale);
        graphicsUpdaters = new ArrayList<>();
        for (var critter : maze.getConfig().getCritters()) {
            if (critter instanceof PacMan) {
                addCritterGraphics(critterFactory.makePacGraphics((PacMan) critter, maze));
            } else {
                addCritterGraphics(critterFactory.makeGhostGraphics((Ghost) critter));
            }
        }

        for (int x = 0; x < maze.getWidth(); x++) {
            for (int y = 0; y < maze.getHeight(); y++) {
                addGraphics(
                    cellFactory.makeGraphics(maze, new IntCoordinates(x, y)),
                    x, y
                );
            }
        }

        inGameGui.displayInfos(maze);
    }

    /**
     * Méthode appelée lorsque la hauteur de la fenêtre change.
     */
    public void resizeGame() {
        mapStack.getChildren().clear();
        mapPane = new GridPane();
        mapStack.getChildren().add(mapPane);

        var critterFactory = new CritterGraphicsFactory((int) scale);
        var cellFactory = new CellGraphicsFactory((int) scale);
        graphicsUpdaters = new ArrayList<>();
        for (var critter : instMaze.getConfig().getCritters()) {
            if (critter instanceof PacMan) {
                addCritterGraphics(critterFactory.makePacGraphics((PacMan) critter, instMaze));
            } else {
                addCritterGraphics(critterFactory.makeGhostGraphics((Ghost) critter));
            }
        }

        for (int x = 0; x < instMaze.getWidth(); x++) {
            for (int y = 0; y < instMaze.getHeight(); y++) {
                addGraphics(
                    cellFactory.makeGraphics(instMaze, new IntCoordinates(x, y)),
                    x, y
                );
            }
        }

        for (GraphicsUpdater graphicsUpdater : InGameGui.getEatScoreUpdater()) {
            final int fontSize = 13 + (int) scale - (int) INITIAL_SCALE;
            ((Text) graphicsUpdater.getNode()).setFont(
                Font.loadFont(Resources.getPathOrContent("small_pixel-7"), fontSize)
            );
            addEatScoreGraphics(graphicsUpdater);
        }

        inGameGui.resizeProgressBar();
    }

    /**
     * Ajoute le{@code Node}contenu dans l'{@code updater}dans {@link #mapPane} aux coordonnées
     * {@code x}et{@code y}.
     * @param updater {@code GraphicsUpdater}
     * @param x {@code int}
     * @param y {@code int}
     */
    public void addGraphics(final GraphicsUpdater updater, final int x, final int y) {
        mapPane.add(updater.getNode(), x, y);
        if (!graphicsUpdaters.contains(updater)) {
            graphicsUpdaters.add(updater);
        }
    }

    private void addCritterGraphics(final GraphicsUpdater updater) {
        mapStack.getChildren().add(updater.getNode());
        StackPane.setAlignment(updater.getNode(), Pos.TOP_LEFT);
        if (!graphicsUpdaters.contains(updater)) {
            graphicsUpdaters.add(updater);
        }
    }

    /**
     * Méthode qui ajoute le{@code Text}score dans {@link #mapStack} et l'{@code updater}dans
     * {@link #graphicsUpdaters} lorsqu'on mange un fantôme.
     * @param updater
     */
    public void addEatScoreGraphics(final GraphicsUpdater updater) {
        mapStack.getChildren().add(0, updater.getNode());
        StackPane.setAlignment(updater.getNode(), Pos.TOP_LEFT);
        if (!graphicsUpdaters.contains(updater)) {
            graphicsUpdaters.add(updater);
        }
    }


    /**
     * Méthode permettant d'animer les entités de la carte
     * JavaFX utilise le taux de rafraîchissement de l'écran de l'utilisateur
     * pour effectuer ses updates. (Ex: 60 updates pour un écran 60hz)
     */
    public void animate() {
        timeSinceAnimationStart = 0;
        animationTimer = new AnimationTimer() {
            private long last = 0;

            @Override
            public void handle(final long now) {
                // Ignore le premier tick
                if (last == 0 || InGameGui.isPaused()) {
                    last = now;
                    return;
                }
                var deltaT = now - last;

                instMaze.update(deltaT);
                inGameGui.updateScore(instMaze);
                inGameGui.updateLives(instMaze);

                final int maxLength = 5;
                final double nanoseconds = Math.pow(10, 9);
                timeSinceAnimationStart += deltaT / nanoseconds;
                if (Music.isStartBgmPlaying()) {
                    inGameGui.updateStartProgressBar(timeSinceAnimationStart);
                } else if (instMaze.getCurrentLiveTime() == null && !Music.isStartBgmPlaying()) {
                    timeSinceAnimationStart = maxLength;
                    inGameGui.updateStartProgressBar(timeSinceAnimationStart);
                } else {
                    inGameGui.updateProgressBar(instMaze);
                }

                // Changer les boolean pour afficher les infos respectifs
                tests(deltaT, false, null);

                for (var updater : graphicsUpdaters) {
                    updater.update();
                }
                last = now;
            }
        };
        animationTimer.start();
    }

    /** Arrête l'animation. */
    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    /** Remets à{@code null} les variables. */
    public void resetInstance() {
        instMaze = null;
        mainBox = null;
        mapPane = null;
        animationTimer = null;
        graphicsUpdaters = null;
    }

    public static double getScale() {
        return scale;
    }

    public static void setScale(final double scale) {
        GameView.scale = scale;
    }

    public boolean isInGame() {
        return animationTimer != null;
    }

    public GridPane getMapPane() {
        return mapPane;
    }

    public VBox getMapBox() {
        return mapBox;
    }

    public StackPane getMapStack() {
        return mapStack;
    }

    public static double getInitialScale() {
        return INITIAL_SCALE;
    }

    /**
     * Méthode permettant d'obtenir les FPS, les threads et d'afficher
     * les targets d'un fantôme spécifique.
     * @param deltaT Durée depuis le dernier tick
     * @param showFps {@code true}affiche les FPS
     * @param ghostTarget Nom du{@code Ghost}
     */
    public void tests(final long deltaT, final boolean showFps, final Ghost ghostTarget) {
        if (showFps) {
            Fps.updateFps(deltaT);
        }
        if (ghostTarget != null) {
            var cellFactory = new CellGraphicsFactory((int) scale);
            if (ghostTarget.getTarget() != null) {
                RealCoordinates target = ghostTarget.getTarget();
                addGraphics(cellFactory.showTarget(target), 0, 0);
            }
        }
    }
}
