package model;


import config.Cell;
import config.MazeConfig;

import geometry.IntCoordinates;
import geometry.RealCoordinates;
import gui.graphics.GraphicsBuffer;
import utils.Music;

public final class PacMan extends Critter {

    private static final double ENERGIZED_SPEED_MULTIPLER = 1.7;
    private static final double MAX_ENERGIZED_TIME = 6;

    private static double energizedDuration = 0;

    /** Dernière {@link Direction} de {@link PacMan} qui n'est pas {@link Direction#NONE}. */
    private Direction lastDirection = Direction.NONE;
    // Etat énergisé de PacMan :
    private static double energizedTimer = 0;
    private boolean energized = false;
    private DirectionBuffer directionBuffer = new DirectionBuffer();
    private GraphicsBuffer graphicsBuffer = new GraphicsBuffer();

    private String image;
    /** Paramètre pour le mode Coop.*/
    private int nbDeGhostChased = 1;
    private final int nbGhostChasedMax = 3;
    /**
     * Fonction générale de {@link PacMan} qui actualise tous ses paramètres.
     * @param state
     * @param deltaTns
     */
    public void updatePacMan(final MazeState state, final long deltaTns) {
        updateLastDirection();
        eat(getPos(), true, state);
        if (directionBuffer.isBufferDirectionValid(this, deltaTns, state.getConfig())) {
            setDirection(directionBuffer.getBufferDirection());
        }
        updatePosition(deltaTns, state);
        updateSpeed();
        directionBuffer.updateDirectionBufferTime(deltaTns);
        if (this != state.getConfig().getPacman2()) {
            updateEnergizedTime(state, deltaTns);
        }
    }

    /**
     * Actualise le paramètre {@code lastDirection}.
     */
    public void updateLastDirection() {
        if (getDirection() != Direction.NONE) {
            lastDirection = getDirection();
        }
    }

    /**
     * Met à jour l'état de PacMan s'il mange un energizer. <br><br>
     * Augmente le score du jeu s'il mange un dot.
     * @param pacPos
     * @param sound
     * @param state
     * @return Renvoei true si PacMan a mangé quelque chose.
     */
    public boolean eat(final RealCoordinates pacPos, final boolean sound, final MazeState state) {
        IntCoordinates pos = pacPos.toIntCoordinates();
        Cell c = state.getConfig().getCell(pos);
        if (c.getContent() == Cell.Content.DOT || c.getContent() == Cell.Content.ENERGIZER) {
            if (c.getContent() == Cell.Content.DOT) {
                final int dotScore = 10;
                state.addScore(dotScore);
            } else {
                final int energizerScore = 50;
                state.addScore(energizerScore);
                Music.playLoopingBgMusic("energized");
                setEnergizedTimer(0);
                if (state.getConfig().isCoop()) {
                    state.getConfig().getPacman().setEnergized(true);
                    state.getConfig().getPacman2().setEnergized(true);
                } else {
                    energized = true;
                }
            }
            c.setContent(Cell.Content.EATEN);
            if (sound) {
                Music.playSound("eat");
            }
            return true;
        }
        return false;
    }

    //region Position
    /**
     * Méthode qui actualise la position de PacMan.
     * @param deltaTns
     * @param state
     */
    public void updatePosition(final long deltaTns, final MazeState state) {
        RealCoordinates pos = getPos();
        RealCoordinates nextPos = nextPos(deltaTns);
        final int width = state.getConfig().getWidth();
        final int height = state.getConfig().getHeight();
        if (pos.equals(nextPos)) {
            return;
        } else if (pos.haveSameNeighbours(nextPos)) {
            nextPos = pos.adjustNextPos(nextPos).warp(width, height);
        } else {
            nextPos = furthestFreeCell(nextPos, state).warp(width, height);
        }
        setPos(nextPos.warp(width, height));
    }

    /**
     * Méthode qui est appelée lorsque PacMan a une très grande vitesse.
     * On doit alors vérifier qu'entre sa position actuelle et sa prochaine position qu'il n'y
     * ait pas de murs entre ces 2 positions.
     * @param end
     * @param state
     * @return {@link RealCoordinates} de la cellule la plus lointaine qui ne contient pas de mur.
     */
    public RealCoordinates furthestFreeCell(final RealCoordinates end, final MazeState state) {
        final RealCoordinates pos = getPos();
        final Direction dir = pos.getDirectionTo(end);
        final MazeConfig config = state.getConfig();
        RealCoordinates tmpPos = pos;
        boolean hasEaten = false;
        while (dir == tmpPos.getDirectionTo(end)) {
            // Cas où on trouve un mur
            if (config.getCell(tmpPos.plus(dir.directionToUnit()).toIntCoordinates()).hasWall()) {
                setDirection(Direction.NONE);
                return tmpPos.roundFromDirection(Direction.NONE);
            } else {
                if (eat(tmpPos, !hasEaten, state) || !hasEaten) {
                    hasEaten = true;
                }
                tmpPos = tmpPos.plus(dir.directionToUnit());
            }
        }
        // Cas où aucun mur n'a été trouvé, renvoie nextPos
        return pos.adjustNextPos(end);
    }

    //endregion Position

    /**
     * Méthode qui met à jour la vitesse de PacMan en fonction de s'il est énergisé ou non.
     */
    public void updateSpeed() {
        if (energized && getSpeed() != getNormalSpeed() * ENERGIZED_SPEED_MULTIPLER) {
            setSpeed(ENERGIZED_SPEED_MULTIPLER * getNormalSpeed());
        } else {
            setSpeed(getNormalSpeed());
        }
    }

    /**
     * Incrémente {@link #energizedTime} à chaque tick.
     * {@link #energizedTimer} est réinitialisé s'il dépasse
     * {@link #MAX_ENERGIZED_TIME}.
     * @param state {@link MazeState}
     * @param deltaTns Durée d'un tick
     */
    public void updateEnergizedTime(final MazeState state, final long deltaTns) {
        if (isEnergized()) {
            final double nanoseconds = Math.pow(10, 9);
            setEnergizedTimer(energizedTimer + deltaTns / nanoseconds);
            if (energizedTimer >= energizedDuration) {
                setEnergizedTimer(0);
                energized = false;
                if (state.getConfig().isCoop()) {
                    state.getConfig().getPacman2().setEnergized(false);
                }
            }
        }
    }

    /**
     * Réinitialise l'état de Pac-Man.
     * {@link #energized}, {@link DirectionBuffer#bufferDirection}. {@link Critter#direction},
     * {@link #lastDirection} et la position de Pac-Man sont remis à zéro.
     */
    public void resetPacMan() {
        energized = false;
        setDirection(Direction.NONE);
        directionBuffer.setBufferDirection(Direction.NONE);
        lastDirection = Direction.NONE;
        nbDeGhostChased = 1;
        setPos(getSpawnPos());
    }

    public static double getEnergizedSpeedMultiplier() {
        return ENERGIZED_SPEED_MULTIPLER;
    }

    public Direction getLastDirection() {
        return lastDirection;
    }

    public void setLastDirection(final Direction lastDirection) {
        this.lastDirection = lastDirection;
    }

    public boolean isEnergized() {
        return energized;
    }

    public void setEnergized(final boolean energized) {
        this.energized = energized;
    }

    public static double getMaxEnergizedTime() {
        return MAX_ENERGIZED_TIME;
    }

    public static void setEnergizedTimer(final double energizedTimer) {
        PacMan.energizedTimer = energizedTimer;
    }

    public static double getEnergizedTimer() {
        return energizedTimer;
    }

    public static double getEnergizedDuration() {
        return energizedDuration;
    }

    public static void setEnergizedDuration(final double energizedDuration) {
        PacMan.energizedDuration = energizedDuration;
    }

    public DirectionBuffer getDirectionBuffer() {
        return directionBuffer;
    }

    public GraphicsBuffer getGraphicsBuffer() {
        return graphicsBuffer;
    }

    /**
     * Retourne l'image du Pac-Man.
     */
    @Override
    public String getImage() {
        return image;
    }

    /**
     * Set l'image du Pac-Man.
     */
    @Override
    public void setImage(final String image) {
        this.image = image;
    }

    public int getNbGhostChased() {
        return nbDeGhostChased;
    }

    public void setNbGhostChased(final int nbDeGhostChased2) {
        this.nbDeGhostChased = nbDeGhostChased2;
    }

    /** @return {@link int} */
    public int getNbGhostChasedMax() {
        return nbGhostChasedMax;
    }
}
