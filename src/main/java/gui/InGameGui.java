package gui;

import java.util.ArrayList;
import java.util.List;

import gui.graphics.GraphicsUpdater;
import gui.menu.Custom;
import gui.menu.MainMenu;
import gui.menu.Menu;
import gui.menu.Multiplayer;
import gui.menu.Solo;
import gui.menu.Selector;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Ghost;
import model.MazeState;
import model.PacMan;
import model.Ghost.GhostState;
import model.MazeState.GameState;
import utils.Music;
import utils.Resources;

public final class InGameGui {
    /** La{@code Scene}actuelle. */
    private Scene gameScene;
    /** Le{@code StackPane}de fin de jeu. */
    private StackPane endGamePane = new StackPane();
    /** Le{@code StackPane}lorsque le jeu est en pause. */
    private StackPane pausePane = new StackPane();
    /** Arrière-plan assombri. */
    private Rectangle dimBackground = makeDimBackground();

    /** Les ">" du menu de fin de jeu. */
    private ArrayList<Text> endGameSelects = new ArrayList<>();
    /** Les choix du menu de fin de jeu. */
    private ArrayList<Text> endgameOptions = new ArrayList<>();
    /** Menu de fin de jeu. */
    private VBox endGameMenu;

    /** Les ">" du menu pause. */
    private ArrayList<Text> pauseSelects = new ArrayList<>();
    /** Les choix du menu pause. */
    private ArrayList<Text> pauseOptions = new ArrayList<>();
    /** Menu pause. */
    private VBox pauseMenu = pauseMenu();

    /** {@code VBox}contenant le score et les vies. */
    private VBox infos;
    /** Le{@code Text}contenant le score. */
    private Text scoreText;
    /** Un tableau contenant les images des vies de Pac-Man. */
    private ImageView[] lives;
    // Progress bar
    /** {@code Label}indiquant l'état des fantômes. */
    private Label barLabel;
    /** Barre de progression. */
    private ProgressBar bar;

    /** Liste de{@code GraphicsUpdater}pour le score lorsqu'on mange un fantôme. */
    private static ArrayList<GraphicsUpdater> eatScoreUpdater = new ArrayList<>();

    /** Indice du niveau choisi. */
    private static int currentLevel;
    /** Boolean indiquant si la partie est mise en pause. */
    private static boolean paused = false;
    /** Boolean indiquant si la partie est finie. */
    private static boolean end = false;

    /** Solo ou Multi. */
    private String mode;
    private boolean isCustom;

    private Selector selector;

    /**
     * Constructeur{@code InGameGui}.
     * @param scene
     * @param mode
     */
    public InGameGui(final Scene scene, final String mode) {
        gameScene = scene;
        this.mode = mode;
        setEnd(false);
        addPauseHandlers();
        setPaused(false);
        Music.setBgMusicClip(null);
        Music.playSound("start");
    }

    /**
     * Constructeur{@code InGameGui}en Endless.
     * @param scene
     * @param mode
     * @param isCustom
     */
    public InGameGui(final Scene scene, final String mode, final boolean isCustom) {
        this(scene, mode);
        this.isCustom = isCustom;
    }

    /**
     * Retourne un fond assombri par rapport aux dimensions du {@link MainMenu#mainStage}.
     * @return {@code Rectangle}fond assombri
     */
    public Rectangle makeDimBackground() {
        final int heightDiff = 40;
        final double opacity = 0.6;
        Rectangle dimBg = new Rectangle(
            0, 0, Menu.getMainStage().getWidth(),
            Menu.getMainStage().getHeight() - heightDiff
        );
        dimBg.widthProperty().bind(Menu.getMainStage().widthProperty());
        dimBg.heightProperty().bind(Menu.getMainStage().heightProperty().add(-heightDiff));
        dimBg.setFill(Color.GRAY);
        dimBg.setOpacity(opacity);

        return dimBg;
    }

    /**
     * Méthode qui ajoute les infos dans la scène de jeu.
     * @param state
     */
    public void displayInfos(final MazeState state) {
        StackPane rootPane = (StackPane) gameScene.getRoot();
        HBox mainBox = (HBox) (rootPane.getChildren().get(0));

        infos = new VBox();
        infos.setAlignment(Pos.CENTER);
        infos.setSpacing(rootPane.getHeight() / 2);
        makeScoreDisplay();
        makeLivesDisplay(state);
        makeProgressBar();

        mainBox.getChildren().add(infos);
    }

    /**
     * Méthode qui ajoute le texte avec le score dans {@link #infos}.
     */
    public void makeScoreDisplay() {
        final int titleSize = 50;
        final int scoreSize = 40;
        final int spacing = 10;

        Text title = Menu.createText("Score", titleSize);
        scoreText = Menu.createText("", scoreSize);
        VBox score = Menu.createVBox(spacing, Pos.CENTER, title, scoreText);

        infos.getChildren().add(score);
    }

    /**
     * Méthode mettant à jour le score à chaque tick.
     * @param state Le {@link MazeState} actuel
     */
    public void updateScore(final MazeState state) {
        // Affiche le score
        scoreText.setText(String.valueOf(state.getScore()));
        // Total : 244 pac-gommes en classique
        if (!end && state.getEatenPacgomme() >= state.getConfig().getTotalPacGomme()) {
            setEnd(true);
            state.playerWin();
            endGameMenu = endGameMenu(true, state);
            showEndGame();
            addEndHandlers();
        }
    }

    /**
     * Affiche la quantité de points gagnée en mangeant un fantôme.
     * @param eatScore Quantité de points
     * @param ghost Le fantôme qui a été mangé
     */
    public static void showEatGhostPoints(final int eatScore, final Ghost ghost) {
        final int fontSize = 15 + (int) GameView.getScale() - (int) GameView.getInitialScale();
        Text eatGhostPoints = Menu.createText(String.valueOf(eatScore), fontSize);
        final double posX = ghost.getPos().x();
        final double posY = ghost.getPos().y();

        GraphicsUpdater updater = new GraphicsUpdater() {
            @Override
            public void update() {
                if (ghost.getGhostState().equals(GhostState.NORMAL)) {
                    GameView.INSTANCE.getMapStack().getChildren().remove(eatGhostPoints);
                    eatScoreUpdater.remove(this);
                }
                eatGhostPoints.setTranslateX(posX * (int) GameView.getScale());
                eatGhostPoints.setTranslateY(posY * (int) GameView.getScale());
            }

            @Override
            public Node getNode() {
                return eatGhostPoints;
            }
        };
        eatScoreUpdater.add(updater);
        GameView.INSTANCE.addEatScoreGraphics(updater);
    }

    /**
     * Méthode qui initialise une image du tableau {@link #lives}.
     * @return {@code ImageView}
     */
    private ImageView setLife() {
        ImageView imageView = new ImageView();
        Image image = Resources.getImage("pacman");

        final int ratio = 40;
        imageView.setImage(image);
        imageView.fitWidthProperty().bind(Menu.getMainStage().heightProperty().divide(ratio));
        imageView.setPreserveRatio(true);
        return imageView;
    }

    /**
     * Méthode qui ajoute les vies de Pac-Man dans {@link #infos}.
     * @param state Le {@link MazeState} actuel
     */
    public void makeLivesDisplay(final MazeState state) {
        HBox livesBox = new HBox();
        livesBox.setAlignment(Pos.CENTER);
        lives = new ImageView[state.getLives()];
        for (int i = 0; i < lives.length; i++) {
            ImageView life = setLife();
            lives[i] = life;
            livesBox.getChildren().add(lives[i]);
        }
        infos.getChildren().add(livesBox);
    }

    /**
     * Méthode qui met à jour la visibilité des vies de Pac-Man.
     * Si Pac-Man n'a plus de vie, {@link #endGameMenu} est appelée.
     * @param state Le {@link MazeState} actuel
     */
    public void updateLives(final MazeState state) {
        if (state.getLives() < state.getTotalLives()) {
            lives[state.getLives()].setVisible(false);
        }

        if (state.getLives() == 0 && !end) {
            setEnd(true);
            endGameMenu = endGameMenu(false, state);
            showEndGame();
            addEndHandlers();
        }
    }

    /** Méthode qui initialise la barre de progression. */
    private void makeProgressBar() {
        VBox mapBox = GameView.INSTANCE.getMapBox();

        final int spacing = 5;
        final int fontSize = 50;

        barLabel = new Label();
        barLabel.setFont(Font.loadFont(Resources.getPathOrContent("small_pixel-7"), fontSize));
        barLabel.setTextFill(Color.WHITE);

        bar = new ProgressBar();
        bar.setProgress(0);
        final double ratio = 1.8;
        bar.setMinHeight(GameView.getScale() * ratio);
        bar.setMaxWidth(gameScene.getWidth());

        VBox progressBar = Menu.createVBox(spacing, Pos.CENTER, barLabel, bar);

        mapBox.getChildren().add(0, progressBar);
    }

    /**
     * Méthode mettant à jour la progression de la barre d'état et de son nom.
     * @param state {@code MazeState}
     */
    public void updateProgressBar(final MazeState state) {
        if (state.getCurrentLiveTime() == null) {
            return;
        }

        GameState currentState = state.getGameState();
        barLabel.setText(state.getGameState().toString());

        if (currentState.equals(GameState.FRIGHTENED)) {
            bar.setProgress(PacMan.getEnergizedTimer() / PacMan.getEnergizedDuration());
        } else {
            int stateDuration = 1;
            double time = state.getTimeSinceStart();
            final int[] stateSwap = state.getConfig().getGhostStateSwap();
            for (int i = 0; i < stateSwap.length; i++) {
                if (time - stateSwap[i] < 0) {
                    stateDuration = stateSwap[i];
                    break;
                } else {
                    time -= stateSwap[i];
                }
            }
            bar.setProgress(time / stateDuration);
        }

        switch (currentState) {
            case SCATTER:
                bar.setStyle("-fx-accent: #0000FF");
                break;
            case CHASE:
                bar.setStyle("-fx-accent: #FF0000");
                break;
            case FRIGHTENED:
                bar.setStyle("-fx-accent: #FFFF00");
                break;

            default:
                break;
        }
    }

    /**
     * Méthode mettant à jour la barre de progression lors du démarrage de la partie.
     * @param time
     */
    public void updateStartProgressBar(final double time) {
        final double startDuration = 3.9;
        final double ready = 1.5;
        final int set = 3;
        if (time < ready) {
            barLabel.setText("Ready");
        } else if (time < set) {
            barLabel.setText("Set");
        } else {
            barLabel.setText("Go!");
        }

        bar.setProgress(time / startDuration);
    }

    /**
     * Redimensionne la barre de progression.
     */
    public void resizeProgressBar() {
        final double ratio = 1.8;
        bar.setMinHeight(GameView.getScale() * ratio);
    }

    /**
     * Méthode qui retourne la{@code VBox}de la fenêtre de fin de jeu.
     * @param won {@code boolean}indiquant le titre à afficher, win ou gameover
     * @param state Le {@link MazeState} actuel
     * @return {@code VBox}de la fenêtre de fin de jeu
     */
    public VBox endGameMenu(final boolean won, final MazeState state) {
        final int titleSize = 104;
        final int scoreSize = 64;
        final int textSize = 38;
        final int textSpacing = 20;
        final int optionSpacing = 15;
        final int mainBoxSpacing = 50;

        String title = won ? "You Win !" : "GameOver";
        if (won) {
            Music.stopBackgroundMusic();
            Music.playSound("start");
        }
        Text gameover = Menu.createText(title, titleSize);
        Text score = Menu.createText("Score : " + state.getScore(), scoreSize);

        Text selectContinue = Menu.createText(">", textSize);
        Text continuer = Menu.createText(isCustom ? "New map" : "Continue", textSize);
        HBox optionContinue = Menu.createHBox(textSpacing, Pos.CENTER, selectContinue, continuer);

        Text select0 = Menu.createText(">", textSize);
        Text restart = Menu.createText("Restart", textSize);
        HBox option0 = Menu.createHBox(textSpacing, Pos.CENTER, select0, restart);

        Text select1 = Menu.createText(">", textSize);
        Text menu = Menu.createText("Menu", textSize);
        HBox option1 = Menu.createHBox(
            textSpacing, Pos.CENTER, select1, menu
        );

        VBox optionBox;
        if (!isCustom) {
            optionBox = Menu.createVBox(optionSpacing, Pos.CENTER, option0, option1);
        } else {
            endGameSelects.add(selectContinue);
            endgameOptions.add(continuer);
            optionBox = Menu.createVBox(
                optionSpacing, Pos.CENTER, optionContinue, option0, option1
            );
        }

        endGameSelects.add(select0);
        endGameSelects.add(select1);

        endgameOptions.add(restart);
        endgameOptions.add(menu);

        VBox titleBox = Menu.createVBox(0, Pos.CENTER, gameover, score);

        return Menu.createVBox(mainBoxSpacing, Pos.CENTER, titleBox, optionBox);
    }

    /**
     * Ajoute {@link #endGamePane} au {@link #gameScene} ce qui
     * affiche la fenêtre de fin de partie.
     */
    private void showEndGame() {
        endGamePane.getChildren().addAll(dimBackground, endGameMenu);
        ((StackPane) gameScene.getRoot()).getChildren().add(endGamePane);
    }

    private void addEndHandlers() {
        selector = new Selector(endGameSelects, endgameOptions, gameScene, false);

        endgameOptions.get(0).setOnMouseClicked(event -> {
            if (isCustom) {
                Custom.startLevel(
                    Custom.getSelectedMode(),
                    Custom.getMazeWidth(), Custom.getMazeHeight()
                );
            } else if (mode.equals("Solo")) {
                Solo.startLevel(currentLevel);
            } else {
                Multiplayer.startLevel(currentLevel);
            }
            selector.getKeySelector().resetSelect();
        });
        endgameOptions.get(1).setOnMouseClicked(mouseEvent -> {
            if (isCustom) {
                Custom.INSTANCE.restartLevel(Custom.getSelectedMode());
            } else {
                MainMenu.INSTANCE.toMenu();
            }
            selector.getKeySelector().resetSelect();
        });
        if (isCustom) {
            endgameOptions.get(2).setOnMouseClicked(mouseEvent -> {
                MainMenu.INSTANCE.toMenu();
                selector.getKeySelector().resetSelect();
            });
        }

        gameScene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                switch (Selector.getSelectedIndex()) {
                    case 0:
                        if (isCustom) {
                            Custom.startLevel(
                                Custom.getSelectedMode(),
                                Custom.getMazeWidth(), Custom.getMazeHeight()
                            );
                        } else if (mode.equals("Solo")) {
                            Solo.startLevel(currentLevel);
                        } else {
                            Multiplayer.startLevel(currentLevel);
                        }
                        break;

                    case 1:
                        if (isCustom) {
                            Custom.INSTANCE.restartLevel(Custom.getSelectedMode());
                        } else {
                            MainMenu.INSTANCE.toMenu();
                        }
                        break;

                    case 2:
                        MainMenu.INSTANCE.toMenu();
                        break;

                    default:
                        break;
                }
                selector.getKeySelector().resetSelect();
            }
        });
    }

    /**
     * Met en pause le jeu et affiche le menu de pause si{@code pause}
     * est{@code true}.
     * @param pause {@code boolean}
     */
    public void pause(final boolean pause) {
        if (pause) {
            setPaused(true);
            showPauseMenu();
            Music.pause();
        } else {
            setPaused(false);
            pausePane.getChildren().clear();
            ((StackPane) gameScene.getRoot()).getChildren().remove(pausePane);
            Music.resume();
        }
    }

    private VBox pauseMenu() {
        final int titleSize = 104;
        final int textSize = 38;
        final int textSpacing = 20;
        final int optionSpacing = 15;
        final int mainBoxSpacing = 50;

        String title = "Pause";
        Text pauseText = Menu.createText(title, titleSize);

        Text select0 = Menu.createText(">", textSize);
        Text continuer = Menu.createText("Continue", textSize);
        HBox option0 = Menu.createHBox(
            textSpacing, Pos.CENTER, select0, continuer
        );

        Text select1 = Menu.createText(">", textSize);
        Text restart = Menu.createText("Restart", textSize);
        HBox option1 = Menu.createHBox(textSpacing, Pos.CENTER, select1, restart);

        Text select2 = Menu.createText(">", textSize);
        Text menu = Menu.createText("Menu", textSize);
        HBox option2 = Menu.createHBox(
            textSpacing, Pos.CENTER, select2, menu
        );

        VBox titleBox = Menu.createVBox(0, Pos.CENTER, pauseText);
        VBox optionBox = Menu.createVBox(
            optionSpacing, Pos.CENTER, option0, option1, option2
        );

        pauseSelects.add(0, select0);
        pauseSelects.add(1, select1);
        pauseSelects.add(2, select2);

        pauseOptions.add(0, continuer);
        pauseOptions.add(1, restart);
        pauseOptions.add(2, menu);

        return Menu.createVBox(
            mainBoxSpacing, Pos.CENTER, titleBox, optionBox
        );
    }

    private void showPauseMenu() {
        pausePane.getChildren().addAll(dimBackground, pauseMenu);
        ((StackPane) gameScene.getRoot()).getChildren().add(pausePane);

        Music.pause();
    }

    private void addPauseHandlers() {
        selector = new Selector(pauseSelects, pauseOptions, gameScene, true);

        pauseOptions.get(0).setOnMouseClicked(mouseEvent -> {
            pause(false);
            Music.resume();
            selector.getKeySelector().resetSelect();
        });
        pauseOptions.get(1).setOnMouseClicked(mouseEvent -> {
            pause(false);
            if (isCustom) {
                Custom.INSTANCE.restartLevel(Custom.getSelectedMode());
            } else if (mode.equals("Solo")) {
                Solo.startLevel(currentLevel);
            } else {
                Multiplayer.startLevel(currentLevel);
            }
            selector.getKeySelector().resetSelect();
        });
        pauseOptions.get(2).setOnMouseClicked(mouseEvent -> {
            MainMenu.INSTANCE.toMenu();
            selector.getKeySelector().resetSelect();
        });

        gameScene.setOnKeyPressed(keyEvent -> {
            if (paused && keyEvent.getCode() == KeyCode.ENTER) {
                pause(false);
                switch (Selector.getSelectedIndex()) {
                    case 0:
                        Music.resume();
                        break;

                    case 1:
                        if (isCustom) {
                            Custom.INSTANCE.restartLevel(Custom.getSelectedMode());
                        } else if (mode.equals("Solo")) {
                            Solo.startLevel(currentLevel);
                        } else {
                            Multiplayer.startLevel(currentLevel);
                        }
                        break;

                    case 2:
                        MainMenu.INSTANCE.toMenu();
                        break;

                    default:
                        break;
                }
                selector.getKeySelector().resetSelect();
            }
        });
    }

    /**
     * Retourne true si la partie est en pause.
     * @return {@code boolean}
     */
    public static boolean isPaused() {
        return paused;
    }

    public static void setPaused(final boolean paused) {
        InGameGui.paused = paused;
    }

    public static void setCurrentLevel(final int currentLevel) {
        InGameGui.currentLevel = currentLevel;
    }

    public static List<GraphicsUpdater> getEatScoreUpdater() {
        return eatScoreUpdater;
    }

    public static void setEnd(final boolean end) {
        InGameGui.end = end;
    }

    public static boolean isEnd() {
        return InGameGui.end;
    }
}
