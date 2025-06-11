package gui.graphics;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import model.Direction;
import model.Ghost;
import model.MazeState;
import model.PacMan;
import model.Ghost.GhostState;
import utils.Resources;

public final class CritterGraphicsFactory {
    /** (Nombre d'update par seconde) / 60. */
    private static final double UPDATE_FREQUENCY = (double) 25 / 60;
    /** (Nombre d'update par seconde) / 60. */
    private static final double ALTERNATE_FREQUENCY = (double) 1 / 15;
    /** Scale pour la taille des critters. */
    public static final double SIZE = 1.4;
    private static final double X_SHIFT = 0.175;
    private static final double Y_SHIFT = 0.2;
    /** Echelle globale. */
    private final int scale;

    /**
     * Constructeur qui initialise le {@link scale}.
     * @param newScale Echelle
     */
    public CritterGraphicsFactory(final int newScale) {
        scale = newScale;
    }

    /**
     * La méthode définie les propriétés de l'image du {@link PacMan} et
     * retourne un {@link GraphicsUpdater} contenant la méthode
     * {@link GraphicsUpdater#update} et l'image du {@link PacMan} via
     * la méthode {@link GraphicsUpdater#getNode}.<br><br>
     * {@link GraphicsUpdater#update} met à jour l'image du {@link PacMan} et
     * la position de l'image.
     * @param pacman
     * @param maze
     * @return {@link GraphicsUpdater}
     */
    public GraphicsUpdater makePacGraphics(final PacMan pacman, final MazeState maze) {
        var image = new ImageView();
        image.setFitWidth(scale * SIZE);
        image.setFitHeight(scale * SIZE);
        image.setPreserveRatio(true);
        image.setSmooth(true);
        image.setImage(Resources.getImage(getPacManImageName(pacman, image, maze)));

        return new GraphicsUpdater() {
            @Override
            public void update() {
                image.setTranslateX(pacman.getPos().x() * scale - scale * X_SHIFT);
                image.setTranslateY(pacman.getPos().y() * scale - scale * Y_SHIFT);

                double graphicsBuffer
                = pacman.getGraphicsBuffer().getUpdateBuffer() + UPDATE_FREQUENCY;
                pacman.getGraphicsBuffer().setUpdateBuffer(graphicsBuffer);
                if (graphicsBuffer >= 1) {
                    pacman.getGraphicsBuffer().setUpdateBuffer(0);

                    String newImage = getPacManImageName(pacman, image, maze);

                    if (!pacman.getGraphicsBuffer().isResetPacImg()
                    && pacman.getImage().equals(newImage)) {
                        return;
                    }

                    if (pacman.getGraphicsBuffer().isResetPacImg()) {
                        if (maze.getConfig().isCoop() && pacman == maze.getConfig().getPacman2()) {
                            newImage = "pacman_rainbow_3";
                        } else {
                            newImage = "pacman_3";
                        }
                        image.setRotate(0);
                        image.setScaleX(1);
                        pacman.getGraphicsBuffer().setResetPacImg(false);
                    }

                    pacman.setImage(newImage);
                    image.setImage(Resources.getImage(newImage));
                }
            }

            @Override
            public Node getNode() {
                return image;
            }
        };
    }

    /**
     * La méthode définie les propriétés de l'image du {@link Ghost} et
     * retourne un {@link GraphicsUpdater} contenant la méthode
     * {@link GraphicsUpdater#update} et l'image du {@link Ghost} via
     * la méthode {@link GraphicsUpdater#getNode}.<br><br>
     * {@link GraphicsUpdater#update} met à jour l'image du {@link Ghost} et
     * la position de l'image.
     * @param ghost
     * @return {@link GraphicsUpdater}
     */
    public GraphicsUpdater makeGhostGraphics(final Ghost ghost) {
        var image = new ImageView();
        image.setFitWidth(scale * SIZE);
        image.setFitHeight(scale * SIZE);
        image.setPreserveRatio(true);
        image.setSmooth(true);
        image.setImage(Resources.getImage(getGhostImageName(ghost, image)));

        return new GraphicsUpdater() {
            @Override
            public void update() {
                image.setTranslateX(ghost.getPos().x() * scale - scale * X_SHIFT);
                image.setTranslateY(ghost.getPos().y() * scale - scale * Y_SHIFT);

                final int ratio = 3;
                double graphicsBuffer
                = ghost.getGraphicsBuffer().getUpdateBuffer() + UPDATE_FREQUENCY / ratio;
                double alternateBuffer
                = ghost.getGraphicsBuffer().getAlternateBuffer() + ALTERNATE_FREQUENCY;

                ghost.getGraphicsBuffer().setUpdateBuffer(graphicsBuffer);
                ghost.getGraphicsBuffer().setAlternateBuffer(alternateBuffer);

                if (!ghost.getPacman().isEnergized() && ghost.getGraphicsBuffer().isAlternate()) {
                    ghost.getGraphicsBuffer().setAlternate(false);
                }

                if (ghost.getGraphicsBuffer().getUpdateBuffer() >= 1) {
                    ghost.getGraphicsBuffer().setUpdateBuffer(0);

                    if (PacMan.getEnergizedDuration() - PacMan.getEnergizedTimer() < 2
                    && alternateBuffer >= 1) {
                        ghost.getGraphicsBuffer().setAlternateBuffer(0);
                        ghost.getGraphicsBuffer().setAlternate(
                            !ghost.getGraphicsBuffer().isAlternate()
                        );
                    } else if (PacMan.getEnergizedTimer() < 1) {
                        ghost.getGraphicsBuffer().setAlternate(false);
                    }

                    String newImage = getGhostImageName(ghost, image);

                    if (ghost.getImage().equals(newImage)) {
                        return;
                    }

                    ghost.setImage(newImage);
                    image.setImage(Resources.getImage(newImage));
                }
            }

            @Override
            public Node getNode() {
                return image;
            }
        };
    }

    /**
     * Méthode renvoyant le nom de l'image pour le {@link PacMan}.
     * L'image de Pac-Man dépendra de son orientation actuelle.
     * @param pacman Un {@link PacMan}
     * @param image L'{@code ImageView}
     * @param maze {@link MazeState}
     * @return {@code String} du nom de l'image
     */
    private String getPacManImageName(
    final PacMan pacman, final ImageView image, final MazeState maze) {
        if (pacman.getDirection() == Direction.NONE) {
            return pacman.getImage();
        }
        rotateImageByDirection(image, pacman.getDirection());
        return alternatePacImage(pacman, maze);
    }

    /**
     * Méthode qui tourne une{@code ImageView}selon la direction.
     * @param image {@code ImageView}
     * @param dir {@code Direction}
     */
    public static void rotateImageByDirection(final ImageView image, final Direction dir) {
        final double angle = 90.0;
        switch (dir) {
            case NORTH:
                image.setRotate(image.getScaleX() == -1 ? angle : -angle);
                break;
            case EAST:
                if (image.getRotate() != 0 || image.getScaleX() != 1) {
                    image.setRotate(0);
                    image.setScaleX(1);
                }
                break;
            case SOUTH:
                image.setRotate(image.getScaleX() == -1 ? -angle : angle);
                break;
            case WEST:
                if (image.getRotate() != 0 || image.getScaleX() != -1) {
                    image.setRotate(0);
                    image.setScaleX(-1);
                }
                break;

            default:
                image.setRotate(0);
                image.setScaleX(1);
                break;
        }
    }

    private String alternatePacImage(final PacMan pacman, final MazeState maze) {
        final String name;
        GraphicsBuffer buffer = pacman.getGraphicsBuffer();

        if (maze.getConfig().isCoop() && pacman == maze.getConfig().getPacman2()) {
            name = "pacman_rainbow_" + buffer.getPhase();
        } else {
            name = "pacman_" + buffer.getPhase();
        }

        final int firstImg = 1;
        final int lastImg = 4;
        if (buffer.getPhase() == firstImg) {
            buffer.setAdd(true);
        } else if (buffer.getPhase() == lastImg) {
            buffer.setAdd(false);
        }

        if (buffer.isAdd()) {
            buffer.setPhase(buffer.getPhase() + 1);
        } else {
            buffer.setPhase(buffer.getPhase() - 1);
        }

        return name;
    }

    /**
     * Méthode renvoyant le nom de l'image pour le {@link Ghost}.
     * @param ghost Un {@link Ghost} pour alterner les images des fantômes.
     * @param image L'{@code ImageView}
     * @return {@code String} du nom de l'image
     */
    private String getGhostImageName(final Ghost ghost, final ImageView image) {
        String img;
        if (ghost.getGhostState() == GhostState.FRIGHTENED
        && !ghost.getGraphicsBuffer().isAlternate()) {
            img = getFrightenedImage(ghost, image);
        } else {
            if (ghost.getGhostState() == GhostState.EATEN) {
                return "eaten" + getImageDirection(ghost, image);
            } else {
                switch (ghost.getGhostType()) {
                    case BLINKY:
                        img = "blinky";
                        break;
                    case CLYDE:
                        img = "clyde";
                        break;
                    case INKY:
                        img = "inky";
                        break;
                    case PINKY:
                        img = "pinky";
                        break;
                    default:
                        img = ghost.getImage();
                        break;
                }
                img += getImageDirection(ghost, image);
            }
        }
        return img + alternateGhostImage(ghost);
    }

    private String getFrightenedImage(final Ghost ghost, final ImageView image) {
        switch (ghost.getDirection()) {
            case EAST:
                if (image.getScaleX() != -1) {
                    image.setScaleX(-1);
                }
                break;
            case WEST:
                if (image.getScaleX() != 1) {
                    image.setScaleX(1);
                }
                break;

            default:
                break;
        }
        return "frightened";
    }

    private String getImageDirection(final Ghost ghost, final ImageView image) {
        switch (ghost.getDirection()) {
            case NORTH:
                return "_u";
            case EAST:
                if (image.getScaleX() != -1) {
                    image.setScaleX(-1);
                }
                return "_s";
            case SOUTH:
                return "_d";
            case WEST:
                if (image.getScaleX() != 1) {
                    image.setScaleX(1);
                }
                return "_s";

            default:
                return "_s";
        }
    }

    private String alternateGhostImage(final Ghost ghost) {
        GraphicsBuffer buffer = ghost.getGraphicsBuffer();
        buffer.setAlternateMove(!buffer.isAlternateMove());
        if (buffer.isAlternateMove()) {
            return "_2";
        }
        return "_1";
    }
}

