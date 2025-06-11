package model;

import geometry.RealCoordinates;

public sealed class Critter permits Ghost, PacMan {

    private static final double NORMAL_SPEED = 5.0;

    private RealCoordinates pos;
    private RealCoordinates spawnPos;
    private double speed = NORMAL_SPEED;
    private Direction direction = Direction.NONE;
    private String image = "";

   /**
     * Méthode qui retourne la prochaine position en fonction de la vitesse
     * et de la direction actuelle du {@link Critter}.
     * @param deltaTNS Tick
     * @return {@code RealCoordinates}
     */
    public final RealCoordinates nextPos(final long deltaTNS) {
        return nextPos(deltaTNS, getDirection());
    }

    /**
     * Méthode qui retourne la prochaine position en fonction de la direction
     * donnée en paramètre et de la vitesse du {@link Critter}.
     * @param deltaTNS Tick
     * @param d {@code Direction}
     * @return {@code RealCoordinates}
     */
    public RealCoordinates nextPos(final long deltaTNS, final Direction d) {
        final double nanoseconds = 1E-9;
        return getPos().plus((
            switch (d) {
            case NONE -> RealCoordinates.ZERO;
            case NORTH -> RealCoordinates.NORTH_UNIT;
            case EAST -> RealCoordinates.EAST_UNIT;
            case SOUTH -> RealCoordinates.SOUTH_UNIT;
            case WEST -> RealCoordinates.WEST_UNIT;
            default -> RealCoordinates.ZERO;
        }).times(getSpeed() * deltaTNS * nanoseconds));
    }

    public static double getNormalSpeed() {
        return NORMAL_SPEED;
    }

    public final RealCoordinates getPos() {
        return pos;
    }

    public final void setPos(final RealCoordinates pos) {
        this.pos = pos;
    }

    public final RealCoordinates getSpawnPos() {
        return spawnPos;
    }

    public final void setSpawnPos(final RealCoordinates spawnPos) {
        this.spawnPos = spawnPos;
    }

    public final Direction getDirection() {
        return direction;
    }

    public final void setDirection(final Direction direction) {
        this.direction = direction;
    }

    public final double getSpeed() {
        return speed;
    }

    public final void setSpeed(final double speed) {
        this.speed = speed;
    }

    /**
     * Retourne l'image du critter.
     * @return {@code String}
     */
    public String getImage() {
        return image;
    }

    /**
     * Set l'image du critter.
     * @param image {@code String}
     */
    public void setImage(final String image) {
        this.image = image;
    }
}
