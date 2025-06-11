package model;

import java.util.concurrent.TimeUnit;

import config.MazeConfig;
import geometry.RealCoordinates;
import gui.GameView;
import gui.InGameGui;
import model.Ghost.GhostState;
import utils.Music;

public final class MazeState {
    /** Liste des modes. */
    public enum GameState {
        /** Chasse Pac-Man. */
        CHASE,
        /** Chacun dans un coin. */
        SCATTER,
        /** Effrayé. */
        FRIGHTENED
    }

    private static final double NANOSECONDS = Math.pow(10, 9);

    /** MazeState actuel pour les tests unitaires. */
    private static MazeState currentMaze;

    /** {@link MazeConfig#config} actuel. */
    private final MazeConfig config;

    private static final double COLLISION_RANGE = 0.8;
    private static final int TOTAL_LIVES = 3;
    private static int ghostEatenCount = 0;
    private int lives;

    /** {@link GameState} actuel. */
    private GameState gameState = GameState.SCATTER;
    /** Chronomètre depuis le début de la partie. */
    private double totalTime = 0.0;
    private Double currentLiveTime;
    private int score = 0;
    private int eatenPacgomme = 0;

    private boolean win = false;
    /**
     * Constructeur qui initialise les infos de la partie.
     * @param newConfig {@link MazeConfig#config} Configuration utilisée
     */
    public MazeState(final MazeConfig newConfig) {
        config = newConfig;
        config.getPacman().resetPacMan();
        lives = TOTAL_LIVES;
    }

    /**
     * Méthode appelée à chaque tick pour mettre à jour l'état de la carte.
     * @param deltaTns Durée d'un tick.
     */
    public void update(final long deltaTns) {
        if (lives > 0 && !win) {
            config.getPacman().updatePacMan(this, deltaTns);
            boolean hasStarted = false;
            if (currentLiveTime == null) {
                hasStarted = config.getPacman().getDirection() != Direction.NONE;
                if  (config.isCoop()) {
                    hasStarted = hasStarted || config.getPacman2().getDirection() != Direction.NONE;
                }
            }
            if (config.isCoop()) {
                config.getPacman2().updatePacMan(this, deltaTns);
            }
            if (currentLiveTime == null && hasStarted) {
                currentLiveTime = 0.0;
                config.activateGhost();
                Music.playLoopingBgMusic("siren_1");
            } else if (currentLiveTime != null) {
                updateGhost(deltaTns);
                currentLiveTime += deltaTns / NANOSECONDS;
                updateCollision();
            }
        }
    }

    /**
     * Met à jour les paramètres des {@link Ghost}.
     * @param deltaTns Tick par temps.
     */
    public void updateGhost(final long deltaTns) {
        for (Ghost ghost : config.getGhostList()) {
            if (ghost.isPlayer()) {
                ghost.updatePlayerGhost(this, deltaTns);
                continue;
            }
            ghost.updateGhost(this, deltaTns);
        }
        // Update gameState
        if (gameState != GameState.FRIGHTENED && gameState != getCurrentState()) {
            setGameState(getCurrentState());
        }
        energizedCheck();
    }

    /**
     * Méthode qui actualise l'état actuel du jeu en fonction de si pacman est énergisé.
     */
    public void energizedCheck() {
        if (config.getPacman().isEnergized() && gameState != GameState.FRIGHTENED) {
            Music.playSound("energizer");
            Music.playLoopingBgMusic("energized");
            for (Ghost ghost : config.getGhostList()) {
                if (ghost.getGhostState() == GhostState.NORMAL) {
                    ghost.setGhostState(GhostState.FRIGHTENED);
                    if (ghost.isPlayer()) {
                        continue;
                    }
                    ghost.setDirection(ghost.getDirection().getOpposite());
                }
            }
            gameState = GameState.FRIGHTENED;
        } else if (!config.getPacman().isEnergized() && gameState == GameState.FRIGHTENED) {
            setGhostEatenCount(0);
            for (Ghost ghost : config.getGhostList()) {
                if (ghost.getGhostState() == GhostState.FRIGHTENED)  {
                    ghost.setGhostState(GhostState.NORMAL);
                }
            }
            gameState = getCurrentState();
            Music.playLoopingBgMusic("siren_1");
        }
    }


    /**
     * La méthode appelle la méthode {@link #playerLost} lorsque Pac-Man est au
     * contact d'un fantôme. Si Pac-Man est {@link PacMan#energized}, le score
     * augmente de 10 et le fantôme revient à sa position initiale.
     */
    private void updateCollision() {
        final int eatScore = 200;
        final RealCoordinates pacmanPos = config.getPacman().getPos();
        RealCoordinates pacman2Pos = new RealCoordinates(0, 0);
        if (config.isCoop()) {
            pacman2Pos = config.getPacman2().getPos();
        }

        for (Ghost ghost : config.getGhostList()) {
            final RealCoordinates ghostPos = ghost.getPos();
            if (Math.abs(ghostPos.x() - pacmanPos.x()) <= COLLISION_RANGE
            && Math.abs(ghostPos.y() - pacmanPos.y()) <= COLLISION_RANGE
            || Math.abs(ghostPos.x() - pacman2Pos.x()) <= COLLISION_RANGE
            && Math.abs(ghostPos.y() - pacman2Pos.y()) <= COLLISION_RANGE) {
                if (ghost.getGhostState() == GhostState.FRIGHTENED
                && config.getPacman().isEnergized()) {
                    Music.playSound("eatghost");
                    ghost.setGhostState(GhostState.EATEN);
                    ghost.setTarget(config.getGhostSpawnEntrance());
                    addScore(eatScore * (int) Math.pow(2, ghostEatenCount));
                    freezeGame(eatScore * (int) Math.pow(2, ghostEatenCount), ghost);
                    setGhostEatenCount(ghostEatenCount + 1);
                } else if (ghost.getGhostState() == GhostState.NORMAL) {
                    setGhostEatenCount(0);
                    playerLost();
                    return;
                }
            }
        }
    }

    private void freezeGame(final int eatScore, final Ghost ghost) {
        final long duration = 500;
        InGameGui.showEatGhostPoints(eatScore, ghost);
        GameView.INSTANCE.stopAnimation();
        try {
            TimeUnit.MILLISECONDS.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        GameView.INSTANCE.animate();
    }

    /**
     * Renvoie {@link GhostState#SCATTER} ou {@link GhostState#CHASE} en fonction du temps de jeu.
     * @return {@link GhostState#SCATTER} ou {@link GhostState#CHASE}
     */
    public GameState getCurrentState() {
        double time = totalTime + currentLiveTime;
        final int[] stateSwap = config.getGhostStateSwap();
        for (int i = 0; i < stateSwap.length; i++) {
            time -= stateSwap[i];
            if (time < 0) {
                if (i % 2 == 0) {
                    return GameState.SCATTER;
                } else {
                    break;
                }
            }
        }
        return GameState.CHASE;
    }

    /**
     * Incrémente le score du jeu par la valeur donnée en paramètre : <br><br>
     * 10 : Dot <br> <br>
     * 200 * nbre de fantôme mangé durant une même frénésie : Fantôme (à implémenter) <br><br>
     * + fruits à faire.
     * @param increment
     */
    public void addScore(final int increment) {
        final int dotEatenScore = 10;
        final int energizerScore = 50;
        score += increment;
        if (increment == dotEatenScore || increment == energizerScore) {
            eatenPacgomme++;
        }
    }

    private void playerLost() {
        lives--;
        totalTime += currentLiveTime;
        currentLiveTime = null;
        Music.stopBackgroundMusic();
        if (lives == 0) {
            Music.playSound("gameOver");
        } else {
            Music.playSound("fail");
        }
        resetCritters();
    }

    /**
     * Méthode appelée lorsque le joueur a gagné.
     * Réinitialise la position des critters.
     */
    public void playerWin() {
        win = true;
        totalTime += currentLiveTime;
        resetCritters();
    }

    private void resetCritters() {
        for (Critter critter : config.getCritters()) {
            resetCritter(critter);
        }
    }

    private void resetCritter(final Critter critter) {
        if (critter instanceof Ghost) {
            ((Ghost) critter).setPos(((Ghost) critter).getSpawnPos());
            ((Ghost) critter).setGhostState(GhostState.NORMAL);
            if (config.isCoop()) {
                ((Ghost) critter).setChaseTargetNull();
            }
            if (((Ghost) critter).isPlayer()) {
                ((Ghost) critter).getDirectionBuffer().setBufferDirection(Direction.NONE);
                critter.setDirection(Direction.NONE);
            }
        } else {
            config.getPacman().resetPacMan();
            if (config.isCoop()) {
                config.getPacman2().resetPacMan();
                config.getPacman2().getGraphicsBuffer().setResetPacImg(true);
            }
            config.getPacman().getGraphicsBuffer().setResetPacImg(true);
        }
    }

    public static MazeState getCurrentMaze() {
        return currentMaze;
    }

    public MazeConfig getConfig() {
        return config;
    }


    public int getTotalLives() {
        return TOTAL_LIVES;
    }

    public int getLives() {
        return lives;
    }

    public GameState getGameState() {
        return gameState;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public Double getCurrentLiveTime() {
        return currentLiveTime;
    }

    public int getScore() {
        return score;
    }

    public int getEatenPacgomme() {
        return eatenPacgomme;
    }

    public void setGameState(final GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Retourne la largeur de la grille.
     * @return {@code int}largeur
     */
    public int getWidth() {
        return config.getGrid()[0].length;
    }

    /**
     * Retourne la hauteur de la grille.
     * @return {@code int}hauteur
     */
    public int getHeight() {
        return config.getGrid().length;
    }

    public double getTimeSinceStart() {
        return totalTime + currentLiveTime;
    }

    public static void setGhostEatenCount(final int ghostEatenCount) {
        MazeState.ghostEatenCount = ghostEatenCount;
    }
}
