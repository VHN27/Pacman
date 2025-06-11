package config.mazeGen;

import java.util.ArrayList;
import java.util.Random;

import config.Cell;
import config.Cell.Content;

/**
 * Classe qui permet de générer une map du jeu de manière "pseudo-aléatoire". Pour cela, on va
 * d'abord simplifier la grille en manipulant un {@code int[][]} et en {@code attribuant un score}
 * à chaque blocs de pièces possibles du jeu.
 * <p>{@code (0 = bloc qui ne contient rien mais peut être un mur)}
 * <p>{@code (1 = bloc qui ne contient rien mais ne peut pas être un mur)}
 * <p>Limitations du générateur, certains élements sont prédictibles :
 * <p>- La position du Spawn des {@link Ghost}. {@code (score 19 et 15)}
 * <p>- Le nombre et la position des tunnels {@code (score 20+)}
 * <p>La génération de la map suit plusieurs étapes de fabrication.
 * <p>Tout d'abord, on ne génère qu'une moitié de map car l'autre moitié est une symétrie.
 * De plus, on ne manipule pas les pièces directement mais des placeholders.
 * C'est à dire, des pièces pleines qui font office de pièce normale qu'on va remplacer
 * par la suite par de vraies pièces. (Cela permet d'éviter de recalculer
 * le maze pour plusieurs pièces de même dimensions).
 * <p>Ensuite, dès que le terrain est valide, elles sont remplacées aléatoirement par une des pièces
 * des sacs disponibles.
 * Les pièces qui composent le terrain sont rangées dans {@code 4 sacs différents} :
 * <p>- {@code "tunnel"}, généré au tout début sur le côté.
 * (1 tunnel sur une petite map, 2 tunnels sinon). {@code (score 20)}.
 * <p>- {@code "spawnWall"}, pièce qui se trouve en dessous du Spawn. {@code (score 30)}.
 * <p>- {@code "middleWall"}, les pièces qui collent le mur intérieur et se trouvent en dessous
 * et au-dessus du spawn. (cf: moitié de map) {@code (score 40+)}.
 * <p>- {@code "freeWall"}, les pièces qui se trouvent à droite du Spawn {@code (score 50+)}.
 * @author Henri CHIV
 */
public final class MainGrid {

    // CLASSIQUE : 31x28
    private static Random random = new Random();
    private static final int CHAR_TO_INT = 48;
    private static final int[] MIN_MAX_HAUTEUR = {25, 51};
    private static final int[] MIN_MAX_LONGUEUR = {26, 50};
    private static final int MAZE_GEN_DONT_LOOK_0 = 2;

    public enum BuildState {
        /**
         * Etape 1 : Choisi un module parmi la liste des pièces n°0 de
         * {@link Piece.#getPossibleGridPieces()} et la met en dessous du spawn des {@link Ghost}.
         */
        BELOWSPAWN,
        /**
         * Etape 2 : Choisi un module parmi la liste des pièces n°1 de
         * {@link Piece.#getPossibleGridPieces()} et la met au milieu de la map.
         * (Tout à gauche de la map scindé en 2).
         */
        MIDDLEMAP,
        /**
         * Etape 3 : Choisi un module parmi la liste des pièces n°2 de
         * {@link Piece.#getPossibleGridPieces()} et les met sur le reste de la map.
         */
        FREE
    }

    private static int[][] grid;
    private static BuildState buildState = BuildState.BELOWSPAWN;

    private static final int NOTHING = 0;
    private static final int DOT = 1;
    private static final int RESTRICTED_NO_DOT = 2;
    private static final int RESTRICTED_AND_DOT = 3;
    private static final int POWER_UP = 5;
    private static final int TUNNEL = 8;
    private static final int OUTER = 10;
    private static final int SPAWN_WALL_INT = 15;
    private static final int SPAWN_INT = 19;


    private static final int TUNNEL_PIECE = 20;
    private static final int SPAWN_PIECE = 30;
    private static final int MIDDLE_PIECE = 40;
    private static final int FREE_PIECE = 50;

    private static int currentPiece = TUNNEL_PIECE;
    private static boolean finished = false;

    private MainGrid() { }

    // region generateGrid

    /**
     * Génère le terrain de base en fonction de l'objet {@link Size} donné.
     * @param hauteur
     * @param longueur
     */
    public static void generateGrid(final int hauteur, final int longueur) {
        int gridH = hauteur; // toujours impair
        if (gridH < MIN_MAX_HAUTEUR[0]) {
            gridH = MIN_MAX_HAUTEUR[0];
        } else if (gridH > MIN_MAX_HAUTEUR[1]) {
            gridH = MIN_MAX_HAUTEUR[1];
        } else if (gridH % 2 == 0) {
            gridH++;
        }
        int gridL = longueur; // toujours pair
        if (gridL < MIN_MAX_LONGUEUR[0]) {
            gridL = MIN_MAX_LONGUEUR[0];
        } else if (gridL > MIN_MAX_LONGUEUR[1]) {
            gridL = MIN_MAX_LONGUEUR[1];
        } else if (gridL % 2 == 1) {
            gridL++;
        }

        grid = new int[gridH][gridL / 2];
        setBorder(grid);
        generateSpawn(grid);
        generateTunnel(grid);
        updateImpossibleWall(grid);
    }

    /**
     * Rajoute les bordures au terrain de base.
     * @param maze
     */
    public static void setBorder(final int[][] maze) {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (i == 0 || i == maze.length - 1 || j == maze[0].length - 1) {
                    maze[i][j] = OUTER;
                }
            }
        }
    }

    /**
     * Rajoute le spawn des {@link Ghost} sur le terrain au milieu.
     * @param maze
     */
    public static void generateSpawn(final int[][] maze) {
        int middle = maze.length / 2;
        // CHECKSTYLE:OFF
        for (int i = middle - 3; i <= middle + 1; i++) {
            for (int j = 0; j <= 3; j++) {
                if ((i == middle - 2 || i == middle - 1 || i == middle
                || (i == middle - 3 && j == 0)) && j != 3) {
                    maze[i][j] = SPAWN_INT;
                } else {
                    maze[i][j] = SPAWN_WALL_INT;
                }
            }
        }
        // CHECKSTYLE:ON
    }

    /**
     * Génère les tunnels de la map.
     * @param maze
     */
    public static void generateTunnel(final int[][] maze) {
        // CHECKSTYLE:OFF
        final int[] HxL2Tunnel = new int[] {33, 30 / 2};
        int nbTunnel = maze.length >= HxL2Tunnel[0] && maze[0].length >= HxL2Tunnel[1] ? 2 : 1;
        ArrayList<String[]> pieces = new ArrayList<>();
        for (String[] piece : Piece.getGridPieces().get(3)) {
            pieces.add(piece.clone());
        }
        for (int i = 0; i < nbTunnel; i++) {
            final int randomPiece = random.nextInt(pieces.size() - 1);
            final String[] chosenPiece = pieces.get(randomPiece);
            pieces.remove(chosenPiece);
            final int[] dimension = new int[] {chosenPiece.length, chosenPiece[0].length()};
            int startH;
            if (nbTunnel == 1) {
                startH = maze.length / 2 - 1 - ((dimension[0] - 1)/ 2);
            } else if (nbTunnel == 2 && i == 0) {
                startH = 5;
            } else {
                startH = maze.length - 5 - dimension[0];
            }
            final int startL = maze[0].length - dimension[1];
            for (int j = 0; j < dimension[0]; j++) {
                for (int k = 0; k < dimension[1]; k++) {
                    if (chosenPiece[j].charAt(k) == '#') {
                        maze[j + startH][k + startL] = currentPiece;
                    } else if (chosenPiece[j].charAt(k) == '=') {
                        maze[j + startH][k + startL] = TUNNEL;
                    } else {
                        maze[j + startH][k + startL] = NOTHING;
                    }
                }
            }
            currentPiece++;
        }
        // CHECKSTYLE:ON
    }

    /**
     * Met à jour les cellules où les murs ne peuvent pas spawn à 100%.
     * <p> Utile pour la détermination d'un terrain validé.
     * @param maze
     */
    public static void updateImpossibleWall(final int[][] maze) {
        final int maxH = maze.length;
        final int maxL = maze[0].length;
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (maze[i][j] == 0 || maze[i][j] == 1) {
                    int value = 0;
                    boucle:
                    for (int k = i - 1; k <= i + 1; k++) {
                        for (int m = j - 1; m <= j + 1; m++) {
                            if (k < 0 || k >= maxH || m < 0 || m >= maxL) {
                                continue;
                            }
                            if (grid[k][m] >= OUTER) {
                                value = DOT;
                                break boucle;
                            }
                        }
                    }
                    maze[i][j] = value;
                }
            }
        }
    }

    // endregion generateGrid

    // region util

    /**
     * Affiche dans la console la grille donnée en paramètre.
     * @param intGrid
     */
    public static void printGrid(final int[][] intGrid) {
        System.out.println("===============================");
        System.out.println("Hauteur : " + intGrid.length + " | Longueur : " + intGrid[0].length);
        for (int i = 0; i < intGrid.length; i++) {
            for (int j = 0; j < intGrid[i].length; j++) {
                if (String.valueOf(intGrid[i][j]).length() == 2) {
                    System.out.print(intGrid[i][j] + " ");
                } else {
                    System.out.print(intGrid[i][j] + "  ");
                }
            }
            System.out.println();
        }
        System.out.println("===============================");
    }

    /**
     * Renvoie une copie de la grille.
     * @param intGrid
     * @return {@code int[][]}
     */
    public static int[][] copy(final int[][] intGrid) {
        int[][] copy = new int[intGrid.length][intGrid[0].length];
        for (int i = 0; i < copy.length; i++) {
            for (int j = 0; j < copy[i].length; j++) {
                copy[i][j] = intGrid[i][j];
            }
        }
        return copy;
    }

    // endregion util

    // region function

    /**
     * Méthode qui ne trouve qu'une seule solution.
     * @param maze
     */
    public static void backtrackOneSolution(final int[][] maze) {
        if (isValidMaze(maze)) {
            finished = true;
        } else {
            if (buildState == BuildState.BELOWSPAWN) {
                if (currentPiece < SPAWN_PIECE) {
                    currentPiece = SPAWN_PIECE;
                }
                for (String piece : Piece.possibles(grid, buildState)) {
                    addPiece(maze, piece);
                    buildState = BuildState.MIDDLEMAP;
                    backtrackOneSolution(maze);
                    if (finished) {
                        return;
                    }
                    buildState = BuildState.BELOWSPAWN;
                    removePiece(maze);
                }
            } else if (buildState == BuildState.MIDDLEMAP) {
                if (currentPiece < MIDDLE_PIECE) {
                    currentPiece = MIDDLE_PIECE;
                }
                for (String piece : Piece.possibles(grid, buildState)) {
                    addPiece(maze, piece);
                    buildState = areMiddleWallsSet(maze);
                    backtrackOneSolution(maze);
                    if (finished) {
                        return;
                    }
                    buildState = BuildState.MIDDLEMAP;
                    removePiece(maze);
                }
            } else {
                if (currentPiece < FREE_PIECE) {
                    currentPiece = FREE_PIECE;
                }
                for (String piece : Piece.possibles(grid, buildState)) {
                    addPiece(maze, piece);
                    backtrackOneSolution(maze);
                    if (finished) {
                        return;
                    }
                    removePiece(maze);
                }
            }
        }
    }

    /**
     * Méthode qui renvoie faux si les intersections du terrain(static) ne sont pas conformes.
     * @param map
     * @return {@code boolean}
     */
    public static boolean verifyEmptyCell(final int[][] map) {
        for (int i = 0; i < map.length - 1; i++) {
            for (int j = 0; j < map[i].length - 1; j++) {
                if (map[i][j] == 1 && map[i + 1][j] == 1
                && map[i][j + 1] == 1 && map[i + 1][j + 1] == 1) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Remplace les placeholder par de vraies pièces du jeu.
     * @param map
     */
    public static void putRealPiecesOnGrid(final int[][] map) {
        //CHECKSTYLE:OFF
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] >= SPAWN_PIECE && !list.contains(map[i][j])) {
                    list.add(map[i][j]);
                    ArrayList<String[]> possiblesPieces =
                        getPossibleRealPieces(map, new int[]{i, j});
                    int randomNumber = random.nextInt(possiblesPieces.size());
                    String[] chosenPiece = possiblesPieces.get(randomNumber).clone();
                    if (map[i][j] > SPAWN_PIECE) {
                        modifyPiece(chosenPiece, map[i][j] >= FREE_PIECE);
                    }
                    for (int k = 0; k < chosenPiece.length; k++) {
                        for (int k2 = 0; k2 < chosenPiece[0].length(); k2++) {
                            if (chosenPiece[k].charAt(k2) != '#'){
                                map[i + k][j + k2] = 1;
                            }
                        }
                    }
                }
            }
        }
        //CHECKSTYLE:ON
    }

    /**
     * Méthode qui peut faire modifier la pièce avec une rotation de 180°
     * et/ou en prenant son miroir.
     * @param piece
     * @param buildMiroir
     */
    public static void modifyPiece(final String[] piece, final boolean buildMiroir) {
        final boolean miroir = random.nextInt(2) == 1;
        if (buildMiroir && miroir) {
            for (int i = 0; i < piece.length; i++) {
                piece[i] = new StringBuilder(piece[i]).reverse().toString();
            }
        }
        final boolean rotation = random.nextInt(2) == 1;
        if (rotation) {
            for (int i = 0; i < (piece.length - 1) / 2; i++) {
                String tmp = piece[i];
                piece[i] = piece[piece.length - 1 - i];
                piece[piece.length - 1 - i] = tmp;
            }
        }
    }

    /**
     * Méthode qui donne en fonction de la taille du placeholder,
     * les possibles pièces qui peut le remplacer.
     * @param map
     * @param indice
     * @return {@code ArrayList<String[]>}
     */
    public static ArrayList<String[]> getPossibleRealPieces(final int[][] map, final int[] indice) {
        ArrayList<ArrayList<String[]>> gridPieces = Piece.getGridPieces();
        ArrayList<String[]> possiblesPieces = new ArrayList<>();
        final int[] sizePiece = getSizeOfPiece(map, indice);
        for (String[] piece : gridPieces.get(gridValueToInt(map[indice[0]][indice[1]]))) {
            if (piece.length == sizePiece[0] && piece[0].length() == sizePiece[1]) {
                possiblesPieces.add(piece);
            }
        }
        return possiblesPieces;
    }

    /**
     * Méthode qui renvoie la taille de la pièce.
     * @param map
     * @param indice
     * @return {@code int[]} avec int[0] = hauteur et int[1] = longueur
     */
    public static int[] getSizeOfPiece(final int[][] map, final int[] indice) {
        final int pieceValue = map[indice[0]][indice[1]];
        int hauteur = 0;
        int longueur = 0;
        for (int i = indice[0]; i < map.length; i++) {
            if (map[i][indice[1]] == pieceValue) {
                hauteur++;
            } else {
                break;
            }
        }
        for (int i = indice[1]; i < map[0].length; i++) {
            if (map[indice[0]][i] == pieceValue) {
                longueur++;
            } else {
                break;
            }
        }
        return new int[] {hauteur, longueur};
    }

    /**
     * Vérifie si le terrain(static) est valide.
     * @param map
     * @return {@code boolean} Vraie si valide.
     */
    public static boolean isValidMaze(final int[][] map) {
        int spawnCell = 0;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 0) {
                    return false;
                }
                if (map[i][j] == SPAWN_INT) {
                    spawnCell++;
                }
            }
        }
        final int numberOfSpawnCell = 10;
        if (spawnCell != numberOfSpawnCell) {
            return false;
        }
        if (hasOneColumnsOfVoid(map)) {
            return false;
        }
        return true;
    }

    /**
     * Méthode qui renvoie vraie si le terrain a au moins 3 colonnes de vides.
     * (Les 2 colonnes de vides à côté du spawn sont toujours présents).
     * @param map
     * @return {@code boolean}
     */
    public static boolean hasOneColumnsOfVoid(final int[][] map) {
        // CHECKSTYLE:OFF
        for (int i = 5; i < map[0].length - 2; i++) {
            int voidCount = 0;
            for (int j = 1; j < map.length - 1; j++) {
                if (map[j][i] == 1) {
                    voidCount++;
                }
            }
            if (voidCount == map.length - 2) {
                return true;
            }
        }
        return false;
        // CHECKSTYLE:ON
    }

    /**
     * Ajoute de manière récusive une pièce sur le terrain.
     * @param maze
     * @param piece
     */
    public static void addPiece(final int[][] maze, final String piece) {
        int[] indice;
        if (buildState == BuildState.BELOWSPAWN) {
            final int belowSpawnWall = 3;
            indice = new int[] {(maze.length / 2) + belowSpawnWall, 0};
        } else if (buildState == BuildState.MIDDLEMAP) {
            indice = Piece.getFirstNumberOfGridVertical(grid, 0);
        } else {
            indice = Piece.getFirstNumberOfGridHorizontal(grid, 0);
        }
        for (int i = 0; i < (int) piece.charAt(0) - CHAR_TO_INT; i++) {
            for (int j = 0; j < (int) piece.charAt(2) - CHAR_TO_INT; j++) {
                maze[i + indice[0]][j + indice[1]] = currentPiece;
            }
        }
        updateImpossibleWall(maze);
        currentPiece++;
    }

    /**
     * Retire de manière récusive une pièce sur le terrain.
     * @param maze
     */
    public static void removePiece(final int[][] maze) {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (maze[i][j] == currentPiece - 1) {
                    maze[i][j] = 0;
                }
            }
        }
        updateImpossibleWall(maze);
        currentPiece--;
    }

    /**
     * Méthode qui vérifie si les murs à la première colonne ont tous été mises.
     * @param map
     * @return {@code boolean} Vrai si il n'y a aucune cellule libre à la première colonne.
     */
    public static BuildState areMiddleWallsSet(final int[][] map) {
        for (int i = 0; i < grid.length; i++) {
            if (map[i][0] == 0) {
                return BuildState.MIDDLEMAP;
            }
        }
        return BuildState.FREE;
    }

    /**
     * Renvoie un {@code String} correspondant au type de liste.
     * @param i
     * @return {@code String}
     */
    public static String intToList(final int i) {
        if (i == 0) {
            return "belowSpawn";
        } else if (i == 1) {
            return "middle";
        } else if (i == 2) {
            return "free";
        }
        return "tunnel";
    }

    /**
     * Convertie une valeur de la grille en valeur int pour les listes de pièces.
     * @param value
     * @return {@code int}
     */
    public static int gridValueToInt(final int value) {
        // CHECKSTYLE:OFF
        if (value >= FREE_PIECE) {
            return 2;
        } else if (value >= MIDDLE_PIECE) {
            return 1;
        } else if (value >= SPAWN_PIECE) {
            return 0;
        }
        return 3;
        // CHECKSTYLE:ON
    }

    /**
     * Affiche les différentes dimensions possibles des pièces de chaque tailles du jeu permises.
     */
    public static void printPossibleGridPieces() {
        String[][] test = Piece.getPossibleGridPieces();
        for (int i = 0; i < test.length; i++) {
            System.out.print(intToList(i) + " : ");
            for (int j = 0; j < test[i].length; j++) {
                System.out.print(test[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Affiche toutes les pièces de jeu possibles.
     */
    public static void printGetGridPieces() {
        int listCmpt = 0;
        int cmpt = 1;
        ArrayList<ArrayList<String[]>> list = Piece.getGridPieces();
        for (ArrayList<String[]> arrayList : list) {
            for (String[] string : arrayList) {
                System.out.println("===== " + intToList(listCmpt) + " ===== " + cmpt + " =====");
                cmpt++;
                for (int i = 0; i < string.length; i++) {
                    System.out.println(string[i]);
                }
            }
            listCmpt++;
        }
    }

    /**
     * Place les power-ups sur la grille.
     * @param map
     */
    public static void placePowerUps(final int[][] map) {
        // CHECKSTYLE:OFF
        boolean isTopPowerUpPlaced = false;
        boolean isBottomPowerUpPlaced = false;
        for (int i = 3; i >= 1; i--) {
            if (!isTopPowerUpPlaced && map[i][map[0].length - 2] == DOT
            || map[i][map[0].length - 2] == POWER_UP) {
                map[i][map[0].length - 2] = POWER_UP;
                isTopPowerUpPlaced = true;
            }
            if (!isBottomPowerUpPlaced && map[map.length - 1 - i][map[0].length - 2] == DOT
            || map[map.length - 1 - i][map[0].length - 2] == POWER_UP) {
                map[map.length - 1 - i][map[0].length - 2] = POWER_UP;
                isBottomPowerUpPlaced = true;
            }
        }
        if (!isTopPowerUpPlaced) {
            for (int i = 4; i < map.length; i++) {
                if (map[i][map[0].length - 2] == 1) {
                    map[i][map[0].length - 2] = POWER_UP;
                    break;
                }
            }
        }
        if (!isBottomPowerUpPlaced) {
            for (int i = map.length - 5; i >= 0; i--) {
                if (map[i][map[0].length - 2] == 1) {
                    map[i][map[0].length - 2] = POWER_UP;
                    break;
                }
            }
        }
        // CHECKSTYLE:ON
    }

    /**
     * Construis le terrain en taille réel.
     * @param halfGrid
     * @return {@code int[][]}
     */
    public static int[][] buildSymmetry(final int[][] halfGrid) {
        int[][] maze = new int[halfGrid.length][halfGrid[0].length * 2];
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (j < maze[i].length / 2) {
                    maze[i][j] = halfGrid[i][halfGrid[i].length - 1 - j];
                } else {
                    maze[i][j] = halfGrid[i][j - halfGrid[0].length];
                }
            }
        }
        return maze;
    }


    /**
     * Construis un terrain de type {@link Cell} en fonction d'un terrain de int.
     * @param map
     * @return {@link Cell}
     */
    public static Cell[][] intGridToCellGrid(final int[][] map) {
        Cell[][] maze = new Cell[map.length][map[0].length];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                switch (map[i][j]) {
                    case NOTHING:
                        maze[i][j] = new Cell(false, false, false, false, Content.NOTHING, false);
                        break;
                    case DOT:
                        maze[i][j] = new Cell(false, false, false, false, Content.DOT, false);
                        break;
                    case RESTRICTED_NO_DOT:
                        maze[i][j] = new Cell(false, false, false, false, Content.NOTHING, true);
                        break;
                    case RESTRICTED_AND_DOT:
                        maze[i][j] = new Cell(false, false, false, false, Content.DOT, true);
                        break;
                    case POWER_UP:
                        maze[i][j] = new Cell(false, false, false, false, Content.ENERGIZER, false);
                        break;
                    case OUTER:
                        maze[i][j] = new Cell(true, true, true, true, Content.OUTER, false);
                        break;
                    case SPAWN_INT:
                        maze[i][j] = new Cell(false, false, false, false, Content.SPAWN, false);
                        break;
                    case TUNNEL:
                        maze[i][j] = new Cell(false, false, false, false, Content.TUNNEL, false);
                        break;
                    default:
                        maze[i][j] = new Cell(true, true, true, true, Content.NOTHING, false);
                        break;
                }
            }
        }
        return maze;
    }

    /**
     * Méthode qui retire les dots du centre et rajoute les cellules dites "restricted".
     * (les {@link Ghost} ne peuvent pas s'orienter vers le haut sur ces cellules).
     * @param map
     * @return
     */
    public static void removeSpawnDotAndAddRestrictedCells(final int[][] map) {
        // CHECKSTYLE:OFF
        final int middleHauteur = map.length / 2;
        final int startHauteur = middleHauteur -6;
        final int endHauteur = middleHauteur + 5;
        final int endLongueur = 7;
        for (int i = startHauteur; i < endHauteur; i++) {
            for (int j = 0; j < endLongueur; j++) {
                if (map[i][j] == 1) {
                    if (i == middleHauteur - 4 && j <= 2) {
                        map[i][j] = RESTRICTED_NO_DOT;
                    } else {
                        map[i][j] = NOTHING;
                    }
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            if (i < 2 && map[middleHauteur + 8][i] == DOT) {
                map[middleHauteur + 8][i] = RESTRICTED_NO_DOT;
            } else if (map[middleHauteur + 8][i] == DOT) {
                map[middleHauteur + 8][i] = RESTRICTED_AND_DOT;
            }
        }
        // CHECKSTYLE:ON
    }


    /**
     * Rajoute les cellules vides du dessus et en dessous du niveau.
     * @param map
     * @return {@code int[][]}
     */
    public static int[][] addSpace(final int[][] map) {
        final int space = 3;
        final int totalSpace = 5;
        int[][] newMap = new int[map.length + totalSpace][map[0].length];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                newMap[i + space][j] = map[i][j];
            }
        }
        return newMap;
    }

    /**
     * Remet à zero les valeurs de départ.
     */
    public static void resetBacktrackSettings() {
        finished = false;
        buildState = BuildState.BELOWSPAWN;
        currentPiece = TUNNEL_PIECE;
    }

    /**
     * Méthode ultime qui renvoie une map générée aléatoirement.
     * @param hauteur
     * @param longueur
     * @return {@code Cell[][]}
     */
    public static Cell[][] createRandomMap(final int hauteur, final int longueur) {
        Piece.initialiseGridPieces();
        int cmpt = 0;
        do {
            resetBacktrackSettings();
            generateGrid(hauteur, longueur);
            backtrackOneSolution(grid);
            cmpt++;
        } while (cmpt == MAZE_GEN_DONT_LOOK_0 || !verifyEmptyCell(grid));
        putRealPiecesOnGrid(grid);
        placePowerUps(grid);
        removeSpawnDotAndAddRestrictedCells(grid);
        grid = addSpace(grid);
        grid = buildSymmetry(grid);
        return intGridToCellGrid(grid);
    }

    // endregion function

    public static int[] getMinMaxHauteur() {
        return MIN_MAX_HAUTEUR;
    }

    public static int[] getMinMaxLongueur() {
        return MIN_MAX_LONGUEUR;
    }
}
