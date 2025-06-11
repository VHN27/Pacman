package model;

import java.util.Set;

import config.Cell;
import config.Cell.Content;
import config.MazeConfig;
import geometry.IntCoordinates;

public final class DirectionBuffer {
    /* Buffer d'un controller
     * Pour faciliter le mouvement lors des intersections, le jeu garde en mémoire la dernière
     * direction que l'utilisateur a entré même si celle-ci n'est pas accessible à ce moment
     * pendant une très courte durée et l'utilise si possible.
     */
    private static final double MAX_BUFFER_DIRECTION_TIME = 0.7;
    private Direction bufferDirection = Direction.NONE;
    private double directionBufferTimer = 0;

    /**
     * Méthode qui vérifie si la {@link Direction} stockée dans le Buffer est valide.
     * S'il l'est, remplace la {@link Direction} du {@link Critter} par celui-ci et
     * réinitialise le contenu du Buffer. Renvoie faux le cas échant.
     * @param critter
     * @param deltaTns
     * @param config
     * @return {@code boolean}
     */
    public boolean isBufferDirectionValid(
    final Critter critter, final long deltaTns, final MazeConfig config) {
        if (bufferDirection == Direction.NONE) {
            return false;
        }
        final Set<IntCoordinates> neighbours
        = critter.nextPos(deltaTns, bufferDirection).intNeighbours();
        // final int maxNeighbours = 4;
        // if (neighbours.size() >= maxNeighbours) {
        //     return false;
        // }
        for (IntCoordinates c: neighbours) {
            Cell cell = config.getCell(c);
            if (cell.hasWall()
            || critter instanceof PacMan && cell.getContent() == Content.SPAWN) {
                return false;
            }
        }
        return true;
    }

    /**
     * Incrémente {@link #bufferDurationTime} à chaque tick.
     * {@link #bufferDurationTime} est réinitialisé s'il dépasse
     * {@link #BUFFER_MAX_TIME}.
     * @param deltaTns Durée d'un tick
     */
    public void updateDirectionBufferTime(final long deltaTns) {
        final double nanoseconds = Math.pow(10, 9);
        directionBufferTimer += deltaTns / nanoseconds;
        if (directionBufferTimer >= MAX_BUFFER_DIRECTION_TIME) {
            directionBufferTimer = 0;
            bufferDirection = Direction.NONE;
        }
    }

    public Direction getBufferDirection() {
        return bufferDirection;
    }

    public double getDirectionBufferTimer() {
        return directionBufferTimer;
    }

    public void setBufferDirection(final Direction bufferDirection) {
        this.bufferDirection = bufferDirection;
    }

    public void setDirectionBufferTimer(final double directionBufferTimer) {
        this.directionBufferTimer = directionBufferTimer;
    }
}
