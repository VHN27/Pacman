package utils;

import java.util.Arrays;
import config.Cell;
import config.Cell.Content;

public final class MapVerification {
// CHECKSTYLE:OFF
    public static final boolean F = false;

    private MapVerification() {
    }

    /**
     * Méthode qui trouve et modifient l'attribut des cellules qui semblent être un tunnel.
     * @param c
     */
    public static void tunnelFinder(final Cell[][] c) {
        for (int y = 1; y < c.length - 1; y++) {
            boolean isTunnel = c[y - 1][0].hasWall() && c[y + 1][0].hasWall();
            if (c[y][0].getContent() == Content.DOT && isTunnel) {
                c[y][0] = new Cell(false, false, false, false, Content.TUNNEL, false);
                c[y][c[0].length - 1] = new Cell(false, false, false, false, Content.TUNNEL, false);
                for (int x = 1; x < c[y].length; x++) {
                    isTunnel = c[y - 1][x].hasWall() && c[y + 1][x].hasWall();
                    if (c[y][x].getContent() == Content.DOT && isTunnel) {
                        c[y][x] = new Cell(false, false, false, false, Content.TUNNEL, false);
                        c[y][c[0].length - x - 1] = new Cell(F, F, F, F, Content.TUNNEL, F);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    /**
     * .
     * @param c
     * @return {@code boolean}
     */
    public static boolean isAValidWall(final Cell c) {
        if (c.getContent() == Content.DOT
        || c.getContent() == Content.ENERGIZER
        || c.getContent() == Content.TUNNEL
        || c.getContent() == Content.SPAWN) {
            return true;
        }
        return !c.hasWall() && c.getContent() == Content.NOTHING;
    }

    /**
     * .
     * @param grid
     * @return {@code Cell[][]}
     */
    public static Cell[][] copyGrid(final Cell[][] grid) {
        Cell[][] copy = new Cell[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            copy[i] = Arrays.copyOf(grid[i], grid[i].length);
        }
        return copy;
    }

    /**
     * .
     * @param grid
     * @return {@code Cell[][]}
     */
    public static void removeUnnecessaryWall(final Cell[][] grid) {
        Cell[][] copy = copyGrid(grid);
        final int posYMaze = 3;
        final int posXMaze = grid[0].length - 1;
        for (int y = posYMaze; y < grid.length - 2; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x].hasWall()) {
                    final int nbWalls = 4;
                    // 0 : NORTH | 1 : EAST | 2 : SOUTH | 3 : WEST
                    boolean[] wall = new boolean[nbWalls];
                    boolean isNearTunnel = false;
                    if (y > 0 && y < grid.length - 2) {
                        isNearTunnel = grid[y + 1][x].getContent() == Content.TUNNEL
                        || grid[y - 1][x].getContent() == Content.TUNNEL;
                    }
                    wall[0] = (y == posYMaze || isAValidWall(grid[y - 1][x]));
                    wall[1] = (x == posXMaze && !isNearTunnel
                        || x != posXMaze && isAValidWall(grid[y][x + 1]));
                    wall[2] = (y == grid.length - posYMaze || isAValidWall(grid[y + 1][x]));
                    wall[3] = (x == 0 && !isNearTunnel || x != 0 && isAValidWall(grid[y][x - 1]));
                    copy[y][x] = new Cell(
                        wall[0], wall[1], wall[2], wall[3], grid[y][x].getContent(), F
                    );
                } else {
                    copy[y][x] = grid[y][x];
                }
            }
        }

        for (int y = 0; y < copy.length; y++) {
            grid[y] = Arrays.copyOf(copy[y], copy[y].length);
        }
    }
    //CHECKSTYLE:ON
}
