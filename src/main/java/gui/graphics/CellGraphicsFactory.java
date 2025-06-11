package gui.graphics;

import config.Cell;
import config.Cell.Content;
import geometry.IntCoordinates;
import geometry.RealCoordinates;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import model.MazeState;

public class CellGraphicsFactory {
    /** L'échelle des images. */
    private final double scale;

    /**
     * Constructeur qui initialise l'échelle.
     * @param newScale L'échelle
     */
    public CellGraphicsFactory(final int newScale) {
        scale = newScale;
    }

    /**
     * Retourne un {@link GraphicsUpdater} contenant la méthode
     * {@link GraphicsUpdater#update} qui initialise la visibilité
     * du {@link config.Cell#DOT} et {@link GraphicsUpdater#getNode}
     * renvoyant un{@code Group}contenant les graphismes pour la cellule.
     * @param state Etat de la carte
     * @param pos Position de la cellule
     * @return {@link GraphicsUpdater}
     */
    public GraphicsUpdater makeGraphics(final MazeState state, final IntCoordinates pos) {
        final int dotScale = 10;
        final int energizerScale = 3;

        var group = new StackPane();
        group.setMinSize(scale, scale);
        group.setMaxSize(scale, scale);

        Cell cell = state.getConfig().getCell(pos);

        var dot = new Circle();
        group.getChildren().add(dot);
        StackPane.setAlignment(dot, Pos.CENTER);
        dot.setRadius(
            switch (cell.getContent()) {
            case DOT -> scale / dotScale;
            case ENERGIZER -> scale / energizerScale;
            default -> 0;
        });
        dot.setFill(Color.YELLOW);

        final Cell[] neighbours = {
            state.getConfig().getCell(new IntCoordinates(pos.x() - 1, pos.y())),
            state.getConfig().getCell(new IntCoordinates(pos.x() + 1, pos.y())),
            state.getConfig().getCell(new IntCoordinates(pos.x(), pos.y() - 1)),
            state.getConfig().getCell(new IntCoordinates(pos.x(), pos.y() + 1))
        };

        if (cell.getNorthWall()) {
            var nWall = getNewWall(true);
            if (cell.getContent() == Content.OUTER
            && neighbours[2].getContent() == Content.NOTHING) {
                StackPane.setAlignment(nWall, Pos.TOP_CENTER);
            } else {
                StackPane.setAlignment(nWall, Pos.CENTER);
            }
            removeExcess(true, state, pos, cell, nWall);
            group.getChildren().add(nWall);
        }
        //CHECKSTYLE:OFF
        if (cell.getSouthWall()) {
            var sWall = getNewWall(true);
            if (cell.getContent() == Content.OUTER
            && neighbours[3].getContent() == Content.NOTHING) {
                StackPane.setAlignment(sWall, Pos.BOTTOM_CENTER);
            } else {
                StackPane.setAlignment(sWall, Pos.CENTER);
            }
            removeExcess(true, state, pos, cell, sWall);
            group.getChildren().add(sWall);
        }
        //CHECKSTYLE:ON
        if (cell.getWestWall()) {
            var wWall = getNewWall(false);
            if (cell.getContent() == Content.OUTER
            && neighbours[0].getContent() == Content.NOTHING
            || pos.x() == 0) {
                StackPane.setAlignment(wWall, Pos.CENTER_LEFT);
            } else {
                StackPane.setAlignment(wWall, Pos.CENTER);
            }
            removeExcess(false, state, pos, cell, wWall);
            group.getChildren().add(wWall);
        }
        if (cell.getEastWall()) {
            var eWall = getNewWall(false);
            if (cell.getContent() == Content.OUTER
            && neighbours[1].getContent() == Content.NOTHING
            || pos.x() == state.getWidth() - 1) {
                StackPane.setAlignment(eWall, Pos.CENTER_RIGHT);
            } else {
                StackPane.setAlignment(eWall, Pos.CENTER);
            }
            removeExcess(false, state, pos, cell, eWall);
            group.getChildren().add(eWall);
        }
        if (isInnerCorner(state, pos)) {
            group.getChildren().add(getInnerCorner(getInnerCornerType(state, pos)));
        }

        return new GraphicsUpdater() {
            @Override
            public void update() {
                dot.setVisible(cell.isDrawn());
            }

            @Override
            public Node getNode() {
                return group;
            }
        };
    }

    private Rectangle getNewWall(final boolean isHorizontal) {
        final int wallScale = 10;
        final double width = isHorizontal ? scale : scale / wallScale;
        final double height = isHorizontal ? scale / wallScale : scale;

        var wall = new Rectangle();
        wall.setHeight(scale);
        wall.setWidth(width);
        wall.setHeight(height);
        wall.setFill(Color.BLUE);

        return wall;
    }

    private boolean isCorner(final Cell cell) {
        return (
            cell.getNorthWall() && (cell.getWestWall() || cell.getEastWall())
            || cell.getSouthWall() && (cell.getWestWall() || cell.getEastWall())
        );
    }

    private void removeExcess(
    final boolean isHorizontal, final MazeState state,
    final IntCoordinates pos, final Cell cell, final Rectangle wall) {
        final boolean isCorner = isCorner(cell);

        if (!(isCorner && pos.x() != 0 && pos.x() != state.getWidth() - 1)) {
            return;
        }

        if (isHorizontal) {
            wall.setWidth(wall.getWidth() / 2);
            if (cell.getWestWall()) {
                wall.setTranslateX(wall.getWidth() / 2);
            } else if (cell.getEastWall()) {
                wall.setTranslateX(-wall.getWidth() / 2);
            }
        } else {
            wall.setHeight(wall.getHeight() / 2);
            if (cell.getNorthWall()) {
                wall.setTranslateY(wall.getHeight() / 2);
            } else if (cell.getSouthWall()) {
                wall.setTranslateY(-wall.getHeight() / 2);
            }
        }
    }

    private boolean isInnerCorner(final MazeState state, final IntCoordinates pos) {
        return getInnerCornerType(state, pos) != 0;
    }

    private int getInnerCornerType(final MazeState state, final IntCoordinates pos) {
        final Cell[] neighbours = {
            state.getConfig().getCell(new IntCoordinates(pos.x() - 1, pos.y())),
            state.getConfig().getCell(new IntCoordinates(pos.x() + 1, pos.y())),
            state.getConfig().getCell(new IntCoordinates(pos.x(), pos.y() - 1)),
            state.getConfig().getCell(new IntCoordinates(pos.x(), pos.y() + 1)),
            state.getConfig().getCell(new IntCoordinates(pos.x() - 1, pos.y() - 1)),
            state.getConfig().getCell(new IntCoordinates(pos.x() - 1, pos.y() + 1)),
            state.getConfig().getCell(new IntCoordinates(pos.x() + 1, pos.y() - 1)),
            state.getConfig().getCell(new IntCoordinates(pos.x() + 1, pos.y() + 1))
        };

        final int nw = 1;
        final int ne = 2;
        final int se = 3;
        final int sw = 4;

        //CHECKSTYLE:OFF
        if (neighbours[1].getSouthWall() && neighbours[3].getEastWall()
        && (neighbours[0].getContent() != Content.DOT
        || neighbours[0].getContent() != Content.EATEN)
        && neighbours[7].getContent() != Content.SPAWN) {
            return nw;
        } else if (neighbours[0].getSouthWall() && neighbours[3].getWestWall()
        && (neighbours[1].getContent() != Content.DOT
        || neighbours[1].getContent() != Content.EATEN)
        && neighbours[5].getContent() != Content.SPAWN) {
            return ne;
        } else if (neighbours[0].getNorthWall() && neighbours[2].getWestWall()
        && (neighbours[1].getContent() != Content.DOT
        || neighbours[1].getContent() != Content.EATEN)
        && neighbours[4].getContent() != Content.SPAWN) {
            return se;
        } else if (neighbours[1].getNorthWall() && neighbours[2].getEastWall()
        && (neighbours[0].getContent() != Content.DOT
        || neighbours[0].getContent() != Content.EATEN)
        && neighbours[6].getContent() != Content.SPAWN) {
            return sw;
        }
        //CHECKSTYLE:ON
        return 0;
    }

    private Arc getInnerCorner(final int type) {
        final int cornerScale = 10;
        final double radius = scale / 2.0 - scale / cornerScale + scale / 20.0;
        final Arc corner;
        //CHECKSTYLE:OFF
        switch (type) {
            case 1:
                corner = new Arc(0, 0, radius, radius, 90, 90);
                corner.setStroke(Color.BLUE);
                corner.setStrokeWidth(scale / cornerScale);
                break;
            case 2:
                corner = new Arc(0, 0, radius, radius, 0, 90);
                corner.setStroke(Color.BLUE);
                corner.setStrokeWidth(scale / cornerScale);
                break;
            case 3:
                corner = new Arc(0, 0, radius, radius, 0, -90);
                corner.setStroke(Color.BLUE);
                corner.setStrokeWidth(scale / cornerScale);
                break;
            case 4:
                corner = new Arc(0, 0, radius, radius, 180, 90);
                corner.setStroke(Color.BLUE);
                corner.setStrokeWidth(scale / cornerScale);
                break;

            default:
                return new Arc();
        }
        //CHECKSTYLE:ON
        alignInnerCorner(corner, type);
        return corner;
    }

    private void alignInnerCorner(final Arc corner, final int type) {
        //CHECKSTYLE:OFF
        switch (type) {
            case 1:
                StackPane.setAlignment(corner, Pos.BOTTOM_RIGHT);
                break;
            case 2:
                StackPane.setAlignment(corner, Pos.BOTTOM_LEFT);
                break;
            case 3:
                StackPane.setAlignment(corner, Pos.TOP_LEFT);
                break;
            case 4:
                StackPane.setAlignment(corner, Pos.TOP_RIGHT);
                break;

            default:
                break;
        }
        //CHECKSTYLE:ON
    }

    /**
     * Méthode pour vérifier la position des targets
     * en les affichant sur la map.
     * @param pos Position du target
     * @return {@code GraphicsUpdater}
     */
    public GraphicsUpdater showTarget(final RealCoordinates pos) {
        final int radius = 5;
        var group = new Group();
        group.setTranslateX(pos.x() * scale);
        group.setTranslateY(pos.y() * scale);
        var dot = new Circle();
        group.getChildren().add(dot);
        dot.setRadius(radius);
        dot.setCenterX(scale / 2);
        dot.setCenterY(scale / 2);
        dot.setFill(Color.color(Math.random(), Math.random(), Math.random()));
        return new GraphicsUpdater() {
            @Override
            public void update() {
                dot.setVisible(true);
            }

            @Override
            public Node getNode() {
                return group;
            }
        };
    }
}
