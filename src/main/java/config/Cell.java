package config;

public class Cell {
    private boolean northWall = false;
    private boolean eastWall = false;
    private boolean southWall = false;
    private boolean westWall = false;
    private Content content = Content.NOTHING;
    private boolean restricted = false;

    /** Contenu qu'a une {@link Cell}. */
    public enum Content {
        /** Rien. */
        NOTHING,
        /** Un Pac-Gomme que {@link PacMan} peut manger. */
        DOT,
        /** Un Super Pac-Gomme que {@link PacMan} peut manger pour devenir énergisé. */
        ENERGIZER,
        /** Rien. (Indique les cellules de spawn des {@link Ghost}). */
        SPAWN,
        /** Rien. (Indique les cellules de tunnel où la vitesse des {@link Ghost} est modifié). */
        TUNNEL,
        /** Rien. (Indique les cellules en dehors de la carte). */
        OUTER,
        /** Rien. (Indique qu'un pac-gomme a été mangé). */
        EATEN
    }
    /** Type que qu'a une {@link Cell}. (fermé ou ouverte). */
    public enum Type {
        /** Indique que la {@link Cell} ne contient pas de mur. */
        OPEN,
        /** Indique que la {@link Cell} est totalement fermé (4 murs). */
        CLOSED
    }

    /**
     * Constructeur pour la classe {@code Cell}.
     * @param northWall
     * @param eastWall
     * @param southWall
     * @param westWall
     * @param content
     * @param restricted
     */
    public Cell(final boolean northWall, final boolean eastWall, final boolean southWall,
            final boolean westWall, final Content content, final boolean restricted) {
        this.northWall = northWall;
        this.eastWall = eastWall;
        this.southWall = southWall;
        this.westWall = westWall;
        this.content = content;
        this.restricted = restricted;
    }

    /**
     * Retourne une copie de la cellule{@code c}.
     * @param c {@code Cell}
     */
    public Cell(final Cell c) {
        this.content = c.content;
        this.northWall = c.northWall;
        this.eastWall = c.eastWall;
        this.southWall = c.southWall;
        this.westWall = c.westWall;
        this.restricted = c.restricted;
    }

    /**
     * Retourne une nouvelle cellule{@code Cell}de{@code Type}t.
     * @param t {@code Type}
     * @return Nouvelle cellule {@link #Cell}
     */
    public static Cell cell(final Type t) {
        switch (t) {
            case OPEN:
                return new Cell(false, false, false, false, Content.NOTHING, false);
            case CLOSED:
                return new Cell(true, true, true, true, Content.NOTHING, false);
            default:
                return null;
        }
    }

    /**
     * Retourne une nouvelle cellule{@code Cell}de{@code Type}t.
     * @param t {@code Type}
     * @param c {@code Content}
     * @return Nouvelle cellule {@link #Cell}
     */
    public static Cell cell(final Type t, final Content c) {
        switch (t) {
            case OPEN:
                return new Cell(false, false, false, false, c, false);
            case CLOSED:
                return new Cell(true, true, true, true, c, false);
            default:
                return null;
        }
    }

    /**
     * Retourne une nouvelle cellule{@code Cell}contenant{@code c}.
     * Si {@code restricted} est vraie, les {@link Ghost} ne peuvent
     * pas s'orienter vers le haut à l'intérieur de ces cellules.
     * @param c Contenu de la cellule
     * @param r {@code restricted}
     * @return {@code Cell}
     */
    public static Cell cell(final Content c, final boolean r) {
        return new Cell(false, false, false, false, c, r);
    }

    /**
     * Retourne une nouvelle cellule{@code Cell}contenant{@code c}.
     * @param c Contenu de la cellule
     * @return {@code Cell}
     */
    public static Cell cell(final Content c) {
        return new Cell(false, false, false, false, c, false);
    }

    /**
     * Renvoie vraie si le contenu de la cellule doit être affiché graphiquement.
     * (NOTHING, SPAWN, TUNNEL et OUTER retournent false).
     * @return {@code boolean}
     */
    public final boolean isDrawn() {
        switch (content) {
            case NOTHING:
            case SPAWN:
            case TUNNEL:
            case OUTER:
            case EATEN:
                return false;
            default:
                break;
        }
        return true;
    }

    /**
     * Méthode qui renvoie vraie si la cellule contient au moins un mur.
     * @return {@code boolean}
     */
    public final boolean hasWall() {
        return northWall || eastWall || westWall || southWall;
    }

    public final boolean getNorthWall() {
        return northWall;
    }

    public final boolean getEastWall() {
        return eastWall;
    }

    public final boolean getSouthWall() {
        return southWall;
    }

    public final boolean getWestWall() {
        return westWall;
    }

    public final Content getContent() {
        return content;
    }

    public final void setContent(final Content content) {
        this.content = content;
    }

    public final boolean isRestricted() {
        return restricted;
    }

    public final void setRestricted(final boolean restricted) {
        this.restricted = restricted;
    }
}
