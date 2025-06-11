package config;

import geometry.IntCoordinates;
import geometry.RealCoordinates;
import gui.menu.Custom;
import model.Critter;
import model.Direction;
import model.Ghost;
import model.PacMan;
import model.Ghost.GhostType;
import utils.MapVerification;
import utils.Resources;
import model.MazeState;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONArray;

import static config.Cell.cell;
import static model.Direction.EAST;
import static model.Direction.NONE;
import static model.Direction.NORTH;
import static model.Direction.WEST;

import config.Cell.Content;
import config.Cell.Type;
import config.mazeGen.MainGrid;

public final class MazeConfig {
    private static final HashMap<String, String> LINE_TABLE = new HashMap<>();

    static {
        LINE_TABLE.put("/", "OPEN");
        LINE_TABLE.put("■", "CLOSED");
        LINE_TABLE.put("s", "SPAWN");
        LINE_TABLE.put("=", "TUNNEL");
        LINE_TABLE.put("◦", "DOT");
        LINE_TABLE.put("●", "ENERGIZER");
        LINE_TABLE.put("x", "NOTHING");
        LINE_TABLE.put("¤", "DOT");
        LINE_TABLE.put("□", "OUTER");
    }

    /** La carte contenant chaque cellule {@link Cell}. */
    private final Cell[][] grid;
    /** Tableau de nombre qui indique le nombre de seconde entre
     * chaque changement d'états des {@link Ghost}. */
    private final int[] ghostStateSwap;

    private final int totalPacGomme;
    /**Les coordonnées du sommet haut-gauche et bas-droite du spawn des {@link model.Ghost}.*/
    private final RealCoordinates[] ghostSpawnPos = new RealCoordinates[2];
    private final RealCoordinates ghostSpawnEntrance;
    private final List<Ghost> ghostList = new ArrayList<>();
    private final PacMan pacman;
    /** Pac-Man 2 en Coop.*/
    private final PacMan pacman2;

    /**
     * Constructeur qui initialise les configs pour la carte courante.
     * @param coop Si on est en mode Coop
     * @param configPath {@code String}Le path du fichier JSON de la map
     * @param endless
     * @param width
     * @param height
     * @param restartedEndless
     */
    public MazeConfig(final boolean coop, final String configPath,
    final boolean endless, final int width, final int height,
    final boolean restartedEndless) {
        if (restartedEndless) {
            grid = Custom.getCurrentMap();
            Cell[][] gridCopy = new Cell[grid.length][grid[0].length];
            for (int y = 0; y < grid.length; y++) {
                for (int x = 0; x < grid[0].length; x++) {
                    gridCopy[y][x] = new Cell(grid[y][x]);
                }
            }
            Custom.setCurrentMap(gridCopy);
        } else if (endless) {
            grid = MainGrid.createRandomMap(height, width);
            Cell[][] gridCopy = new Cell[grid.length][grid[0].length];
            for (int y = 0; y < grid.length; y++) {
                for (int x = 0; x < grid[0].length; x++) {
                    gridCopy[y][x] = new Cell(grid[y][x]);
                }
            }
            Custom.setCurrentMap(gridCopy);
        } else {
            grid = getJSONMaze(configPath);
        }
        ghostStateSwap = getStateSwap(configPath);
        totalPacGomme = countPacGomme();
        MapVerification.tunnelFinder(grid);
        MapVerification.removeUnnecessaryWall(grid);

        //CHECKSTYLE:OFF
        final int ghostSpawnX = getWidth() / 2 - 4;
        final int ghostSpawnY = getHeight() / 2 - 3;
        ghostSpawnPos[0] = new RealCoordinates(ghostSpawnX, ghostSpawnY);
        ghostSpawnPos[1] = new RealCoordinates(ghostSpawnX + 7, ghostSpawnY + 4);

        final double entranceY = -1;
        final double entranceX = 3.5;
        ghostSpawnEntrance = ghostSpawnPos[0].plus(new RealCoordinates(entranceX, entranceY));

        pacman = new PacMan();
        pacman.setImage("pacman_3");
        final double PacSpawnY = 12;
        final RealCoordinates entrance = ghostSpawnEntrance;
        if (!coop) {
            pacman.setSpawnPos(new RealCoordinates(entrance.x(), entrance.y() + PacSpawnY));
            PacMan.setEnergizedDuration(PacMan.getMaxEnergizedTime());
            pacman2 = null;
        } else {
            pacman.setSpawnPos(new RealCoordinates(entrance.x() - 0.5, entrance.y() + PacSpawnY));
            PacMan.setEnergizedDuration(PacMan.getMaxEnergizedTime() / 2);
            pacman2 = new PacMan();
            pacman2.setImage("pacman_rainbow_3");
            pacman2.setSpawnPos(new RealCoordinates(entrance.x() + 0.5, entrance.y() + PacSpawnY));
            pacman2.setPos(pacman2.getSpawnPos());
        }
        pacman.setPos(pacman.getSpawnPos());

        Ghost blinky = new Ghost(this, GhostType.BLINKY);
        Ghost inky = new Ghost(this, GhostType.INKY, blinky);
        Ghost pinky = new Ghost(this, GhostType.PINKY);
        Ghost clyde = new Ghost(this, GhostType.CLYDE);
        ghostList.addAll(Arrays.asList(blinky, inky, pinky, clyde));
        //CHECKSTYLE:ON
    }

    /**
     * Convertie les données d'un fichier .json en {@code Cell[][]}
     * utilisé pour la configuration de la carte. <br></br>
     * Le fichier doit contenir des données nécessaires pour
     * une carte rectangulaire.
     * @param name Le nom du fichier à importer.
     * @return {@code Cell[][] maze}
     */
    public static Cell[][] getJSONMaze(final String name) {
        try {
            String file = Resources.getPathOrContent(name);
            JSONObject jObject = new JSONObject(file);
            JSONArray jArray = new JSONArray(jObject.getJSONArray("Line1"));
            final int lineLength = (jArray.get(0).toString().length() + 1) / 2;
            Cell[][] maze = new Cell[jObject.length() - 1][lineLength];
            for (int y = 0; y < jObject.length() - 1; y++) {
                jArray = new JSONArray(jObject.getJSONArray("Line" + (y + 1)));
                for (int x = 0; x < lineLength; x++) {
                    String output = jArray.getString(0).charAt(x * 2) + "";
                    if (output.equals("■") || output.equals("/")) {
                        maze[y][x] = cell(Type.valueOf(LINE_TABLE.get(output)));
                    } else if (output.equals("□")) {
                        maze[y][x] = cell(Type.valueOf("CLOSED"),
                        Content.valueOf(LINE_TABLE.get(output)));
                    } else if (output.equals("¤") || output.equals("x")) {
                        maze[y][x] = cell(Content.valueOf(LINE_TABLE.get(output)), true);
                    } else {
                        maze[y][x] = cell(Content.valueOf(LINE_TABLE.get(output)));
                    }
                }
            }
            return maze;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Cell[0][];
    }

    /**
     * Méthode qui retourne le tableau contenant les différents instants pour
     * le changement de mode.
     * @param name Le nom du fichier à importer.
     * @return {@code int[]}
     */
    public static int[] getStateSwap(final String name) {
        try {
            String file = Resources.getPathOrContent(name);
            JSONObject jObject = new JSONObject(file).getJSONObject("config");
            JSONArray jArray = new JSONArray(jObject.getJSONArray("swapTime"));
            int[] stateSwap = new int[jArray.length()];
            for (int i = 0; i < stateSwap.length; i++) {
                stateSwap[i] = jArray.getInt(i);
            }
            return stateSwap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[0];
    }

    /**
     * Initialise le jeu et active les {@link Ghost} du type
     * {@link GhostType#BLINKY} et {@link GhostType#PINKY}.
     */
    public void activateGhost() {
        for (Ghost ghost : ghostList) {
            switch (ghost.getGhostType()) {
                case BLINKY:
                    ghost.setActivated(true);
                    if (ghost.isPlayer()) {
                        break;
                    }
                    ghost.setDirection(Direction.EAST);
                    break;
                case PINKY:
                    ghost.setActivated(true);
                    ghost.setDirection(Direction.NORTH);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Réinitialise la position des fantômes après un reset.
     * @param state {@code MazeState} Etat actuel de la map
     */
    public void resetGhostPos(final MazeState state) {
        final int inkyThreshold = 30;
        final int clydeThreshold = state.getConfig().getTotalPacGomme() / 3;
        for (Ghost ghost : ghostList) {
            switch (ghost.getGhostType()) {
                case BLINKY:
                    ghost.setDirection(EAST);
                    break;
                case PINKY:
                    ghost.setDirection(NORTH);
                    break;
                case INKY:
                    if (state.getEatenPacgomme() >= inkyThreshold) {
                        ghost.setDirection(EAST);
                        break;
                    }
                    ghost.setDirection(NONE);
                    break;
                case CLYDE:
                    if (state.getEatenPacgomme() >= clydeThreshold) {
                        ghost.setDirection(WEST);
                        break;
                    }
                    ghost.setDirection(NONE);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Retourne un {@link MazeConfig}.
     * @param coop {@code true}si en mode Coop
     * @return {@link MazeConfig}
     */
    public static MazeConfig classicalConfig(final boolean coop) {
        return new MazeConfig(coop, "Level1", false, 0, 0, false);
    }

    /**
     * Retourne un {@link MazeConfig} pour le mode Endless.
     * @param coop {@code true}si en coop
     * @param width {@code int}largeur de la carte
     * @param height {@code int}longueur de la carte
     * @return {@link MazeConfig}
     */
    public static MazeConfig endlessConfig(final boolean coop, final int width, final int height) {
        return new MazeConfig(coop, "Level1", true, width, height, false);
    }

    /**
     * Retourne un {@link MazeConfig} pour le mode Endless.
     * @param coop {@code true}si en coop
     * @return {@link MazeConfig}
     */
    public static MazeConfig restartedEndlessConfig(final boolean coop) {
        return new MazeConfig(coop, "Level1", true, 0, 0, true);
    }

    /**
     * Renvoie le nombre total de PacGomme.
     * @return {@code int}
     */
    public int countPacGomme() {
        int count = 0;
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                final boolean hasDot = grid[i][j].getContent() == Content.DOT;
                final boolean hasEnergizer = grid[i][j].getContent() == Content.ENERGIZER;
                count += hasDot || hasEnergizer ? 1 : 0;
            }
        }
        return count;
    }

    /**
     * Renvoie la liste des {@link Critter} du {@link MazeConfig}.
     * @return {@code List<Critter>}
     */
    public List<Critter> getCritters() {
        List<Critter> critters = new ArrayList<>();
        for (Ghost ghost : ghostList) {
            critters.add(ghost);
        }
        critters.add(pacman);
        if (pacman2 != null) {
            critters.add(pacman2);
        }
        return critters;
    }

    /**
     * Renvoie la liste des {@link Ghost} qui ont pour type le paramètre donné.
     * @param ghostType {@link GhostType}
     * @return {@code Lit<Ghost>}
     */
    public List<Ghost> getGhostKind(final GhostType ghostType) {
        List<Ghost> oneKind = new ArrayList<>();
        for (Ghost ghost : ghostList) {
            if (ghost.getGhostType() == ghostType)  {
                oneKind.add(ghost);
            }
        }
        return oneKind;
    }

    public boolean isCoop() {
        return pacman2 != null;
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public int[] getGhostStateSwap() {
        return ghostStateSwap;
    }

    public int getTotalPacGomme() {
        return totalPacGomme;
    }

    public RealCoordinates getGhostSpawnEntrance() {
        return ghostSpawnEntrance;
    }

    public PacMan getPacman() {
        return pacman;
    }

    public PacMan getPacman2() {
        return pacman2;
    }

    public List<Ghost> getGhostList() {
        return ghostList;
    }

    /**
     * Retourne la largeur de la grille.
     * @return {@code int}largeur
     */
    public int getWidth() {
        return grid[0].length;
    }

    /**
     * Retourne la hauteur de la grille.
     * @return {@code int}hauteur
     */
    public int getHeight() {
        return grid.length;
    }

    /**
     * Retourne la{@code Cell}aux coordonnées{@code pos}.
     * @param pos Position de la cellule
     * @return {@link Cell}
     */
    public Cell getCell(final IntCoordinates pos) {
        int y = Math.floorMod(pos.y(), getHeight());
        int x = Math.floorMod(pos.x(), getWidth());
        return grid[y][x];
    }

}
