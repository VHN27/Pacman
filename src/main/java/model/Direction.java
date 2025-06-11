package model;

import config.Cell;
import geometry.RealCoordinates;

public enum Direction {
    /** Directions possibles. */
    NONE, NORTH, EAST, SOUTH, WEST;

    /**
     * Méthode qui retourne true si la cellule{@code Cell c}
     * contient un mur selon la direction du{@code Critter}.
     * @param c {@code Cell}
     * @return {@code boolean}
     */
    public boolean wall(final Cell c) {
        switch (this) {
            case NORTH:
                return c.getNorthWall();
            case EAST:
                return c.getEastWall();
            case SOUTH:
                return c.getSouthWall();
            case WEST:
                return c.getWestWall();
            default:
                break;
        }
        return true;
    }

    /**
     * Renvoie la {@link Direction} opposée à celle appelée.
     * @return {@link Direction}
     */
    public Direction getOpposite() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case EAST:
                return WEST;
            case SOUTH:
                return NORTH;
            case WEST:
                return EAST;
            default:
                return NONE;
        }
    }

    /**
     * Renvoie une unité de {@link RealCoordinates}
     * en fonction de la {@link Direction}.
     * @return {@code RealCoordinates}
     */
    public RealCoordinates directionToUnit() {
        switch (this) {
            case NORTH:
                return RealCoordinates.NORTH_UNIT;
            case EAST:
                return RealCoordinates.EAST_UNIT;
            case SOUTH:
                return RealCoordinates.SOUTH_UNIT;
            case WEST:
                return RealCoordinates.WEST_UNIT;
            default:
                break;
        }
        return RealCoordinates.ZERO;
    }
}
