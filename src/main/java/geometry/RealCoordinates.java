package geometry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Direction;

public record RealCoordinates(double x, double y) {
    /** Position du zero. */
    public static final RealCoordinates ZERO = new RealCoordinates(0, 0);
    /** Une unité au Nord. */
    public static final RealCoordinates NORTH_UNIT = new RealCoordinates(0, -1);
    /** Une unité à l'Est. */
    public static final RealCoordinates EAST_UNIT = new RealCoordinates(1, 0);
    /** Une unité au Sud. */
    public static final RealCoordinates SOUTH_UNIT = new RealCoordinates(0, 1);
    /** Une unité à l'Ouest. */
    public static final RealCoordinates WEST_UNIT = new RealCoordinates(-1, 0);

    /**
     * Renvoie un boolean vérifiant si 2 coordonnées sont identiques.
     * @param other
     * @return {@code boolean}
     */
    public boolean equals(final RealCoordinates other) {
        return x == other.x && y == other.y;
    }

    /**
     * Additionne les deux coordonnées.
     * @param other {@link RealCoordinates}
     * @return {@link RealCoordinates}
     */
    public RealCoordinates plus(final RealCoordinates other) {
        return new RealCoordinates(x + other.x, y + other.y);
    }

    /**
     * Soustrait les deux coordonnées.
     * @param other {@link RealCoordinates}
     * @return {@link RealCoordinates}
     */
    public RealCoordinates minus(final RealCoordinates other) {
        return new RealCoordinates(x - other.x, y - other.y);
    }

    /**
     * Multiplie les coordonnées par{@code multiplier}.
     * @param multiplier Le multiplicateur
     * @return {@link RealCoordinates}
     */
    public RealCoordinates times(final double multiplier) {
        return new RealCoordinates(x * multiplier, y * multiplier);
    }

    /**
     * Méthode qui renvoie un {@code Set<IntCoordinates>} de toutes les
     * {@code Cell}que{@code this}superpose
     * sur un rayon de 1.
     * @return Une {@code List<IntCoordinates>} contenant les cellules.
     */
    public Set<IntCoordinates> intNeighbours() {
        return new HashSet<>(
            List.of(
                new IntCoordinates((int) Math.floor(x), (int) Math.floor(y)),
                new IntCoordinates((int) Math.ceil(x), (int) Math.floor(y)),
                new IntCoordinates((int) Math.floor(x), (int) Math.ceil(y)),
                new IntCoordinates((int) Math.ceil(x), (int) Math.ceil(y))
            )
        );
    }

    /**
     * Méthode qui renvoie vraie si les 2 {@link RealCoordinates} se trouvent sur les mêmes cellules
     * {@code this}.
     * @param nextPos
     * @return {@code boolean}
     */
    public boolean haveSameNeighbours(final RealCoordinates nextPos) {
        Set<IntCoordinates> curNeighbours = intNeighbours();
        Set<IntCoordinates> nextNeighbours = nextPos.intNeighbours();
        return curNeighbours.containsAll(nextNeighbours)
            && curNeighbours.size() == nextNeighbours.size();
    }

    /**
     * Téléportation de gauche à droite et de haut en bas.
     * @param width Largeur de la map
     * @param height Hauteur de la map
     * @return Les {@code RealCoordinates} de la position
     * après la téléportation.
     */
    public RealCoordinates warp(final int width, final int height) {
        double rx = x;
        double ry = y;
        while (Math.round(rx) < 0) {
            rx += width;
        }
        while (Math.round(ry) < 0) {
            ry += height;
        }
        while (Math.round(rx) >= width) {
            rx -= width;
        }
        while (Math.round(ry) >= height) {
            ry -= height;
        }
        return new RealCoordinates(rx, ry);
    }
    /**
     * Retourne une Direction calculée en fonction de 2 RealCoordinates.
     * @param end Le {@code RealCoordinates} destination
     * @return Donne la {@code Direction} de this à {@code end}
     */
    public Direction getDirectionTo(final RealCoordinates end) {
        double deltaY = end.y() - this.y();
        if (deltaY != 0) {
            if (deltaY < 0) {
            return Direction.NORTH;
            }
            return Direction.SOUTH;
        }
        double deltaX = end.x() - this.x();
        if (deltaX < 0) {
            return Direction.WEST;
        }
        return Direction.EAST;
    }

    /**
     * Méthode qui renvoie un {@link RealCoordinates} où ses coordonnées on été ajustée à l'unité ou
     * par 0.5. Cette méthode est utile pour le calcul de la prochaine position pour les
     * {@link Critters} (Le centre d'une case est toujours une valeur entière et
     * la position entre 2 cases est toujours une valeur qui se finit par 0.5).
     * <p> Exemple : Si this.x = 1.9 et end.x = 2.4, alors adjustPosition() = 2
     * <p> Exemple : Si this.x = 2.3 et end.x = 2.7, alors adjustPosition() = 2.5
     * @param end
     * @param precision Vrai n'avoir que les coordonnées ajustées par 1.
     * Faux pour toutes les coordonnées ajustée par 0.5.
     * @return
     */
    public RealCoordinates adjustNextPos(final RealCoordinates end) {
        //CHECKSTYLE:OFF
        final Direction d = this.getDirectionTo(end);
        switch (d) {
            case NORTH: {
                    int curY = Math.abs((int) y);
                    final int nextY = Math.abs((int) (end.y));
                    if (y % 1 == 0) {
                        curY--;
                    }
                    if (curY > nextY) {
                        return new RealCoordinates(x, 1 + end.y - end.y % 1);
                    }
                }
                break;
            case EAST: {
                    if (((int) x) < ((int)end.x)) {
                        return new RealCoordinates(end.x - end.x % 1, y);
                    }
                }
                break;
            case SOUTH: {
                    if (((int) y) < ((int) end.y)) {
                        return new RealCoordinates(x, end.y - end.y % 1);
                    }
                }
                break;
            case WEST: {
                    int curX = Math.abs((int) x);
                    final int nextX = Math.abs((int) end.x);
                    if (x % 1 == 0) {
                        curX--;
                    }
                    if (curX > nextX) {
                        return new RealCoordinates(1 + end.x - end.x % 1, y);
                    }
                }
                break;
            default:
                break;
        }
        return end;
        //CHECKSTYLE:ON
    }

    /**
     * Méthode qui renvoie un {@link RealCoordinates} où ses coordonnées on été ajustée à l'unité ou
     * par 0.5. Cette méthode est utile pour le calcul de la prochaine position pour les
     * {@link Critters} (Le centre d'une case est toujours une valeur entière et
     * la position entre 2 cases est toujours une valeur qui se finit par 0.5).
     * <p> Exemple : Si this.x = 1.9 et end.x = 2.4, alors adjustPosition() = 2
     * <p> Exemple : Si this.x = 2.3 et end.x = 2.7, alors adjustPosition() = 2.5
     * @param end
     * @param precision Vrai n'avoir que les coordonnées ajustées par 1.
     * Faux pour toutes les coordonnées ajustée par 0.5.
     * @return
     */
    public RealCoordinates adjustNextPos(final RealCoordinates end, final boolean precision) {
        //CHECKSTYLE:OFF
        final Direction d = this.getDirectionTo(end);
        final double value = precision ? 0.5 : 1;
        switch (d) {
            case NORTH: {
                    int curY = Math.abs((int) (y / value));
                    final int nextY = Math.abs((int) (end.y / value));
                    if ((y / value) % value == 0) {
                        curY--;
                    }
                    if (curY > nextY) {
                        return new RealCoordinates(x, value + end.y - end.y % value);
                    }
                }
                break;
            case EAST: {
                    if ((int) (x / value) < (int) (end.x / value)) {
                        return new RealCoordinates(end.x - end.x % value, y);
                    }
                }
                break;
            case SOUTH: {
                    if ((int) (y / value) < (int) (end.y / value)) {
                        return new RealCoordinates(x, end.y - end.y % value);
                    }
                }
                break;
            case WEST: {
                    int curX = Math.abs((int) (x / value));
                    final int nextX = Math.abs((int) (end.x / value));
                    if ((x / value) % value == 0) {
                        curX--;
                    }
                    if (curX > nextX) {
                        return new RealCoordinates(value + end.x - end.x % value, y);
                    }
                }
                break;
            default:
                break;
        }
        return end;
        //CHECKSTYLE:ON
    }

    /**
     * Fonction qui renvoie le modulo de 2 nombres double proprement pour
     * les nombres négatifs.
     * @param x
     * @param y
     */
    public static double doubleMod(final double x, final double y) {
        return (x - Math.floor(x / y) * y);
    }

    /**
     * Arrondie le {@link RealCoordinates} et renvoie un {@link IntCoordinates}.
     * @return {@link IntCoordinates}
     */
    public IntCoordinates toIntCoordinates() {
        return new IntCoordinates((int) Math.round(x), (int) Math.round(y));
    }

    /**
     * Renvoie un {@link RealCoordinates} avec une des coordonnée arrondie en fonction de
     * la direction prise.
     * @return {@link RealCoordinates}
     */
    public RealCoordinates roundFromDirection(final Direction dir) {
        switch (dir) {
            case NORTH:
            case SOUTH:
                return new RealCoordinates(x, Math.round(y));
            case EAST:
            case WEST:
                return new RealCoordinates(Math.round(x), y);
            default:
                break;
        }
        return new RealCoordinates(Math.round(x), Math.round(y));
    }

    /**
     * Renvoie un {@link RealCoordinates} dont on a
     * "floor" la coordonnée {@link #x}.
     * @return {@link RealCoordinates}
     */
    public RealCoordinates floorX() {
        return new RealCoordinates((int) Math.floor(x), y);
    }

    /**
     * Renvoie un {@link RealCoordinates} dont on a
     * "floor" la coordonnée {@link #y}.
     * @return {@link RealCoordinates}
     */
    public RealCoordinates floorY() {
        return new RealCoordinates(x, (int) Math.floor(y));
    }

    /**
     * Renvoie un {@link RealCoordinates} dont on a
     * "ceil" (plafonné) la coordonnée {@link #x}.
     * @return {@link RealCoordinates}
     */
    public RealCoordinates ceilX() {
        return new RealCoordinates((int) Math.ceil(x), y);
    }

    /**
     * Renvoie un {@link RealCoordinates} dont on a
     * "ceil" (plafonné) la coordonnée {@link #y}.
     * @return {@link RealCoordinates}
     */
    public RealCoordinates ceilY() {
        return new RealCoordinates(x, (int) Math.ceil(y));
    }

}
