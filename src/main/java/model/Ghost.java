package model;

import geometry.IntCoordinates;
import geometry.RealCoordinates;
import gui.graphics.GraphicsBuffer;
import model.MazeState.GameState;

import static model.Direction.NORTH;
import static model.Direction.EAST;
import static model.Direction.SOUTH;
import static model.Direction.WEST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import config.Cell;
import config.MazeConfig;
import config.Cell.Content;

public final class Ghost extends Critter {

    /** Vitesse du fantôme lorsqu'il est dans un tunnel. */
    private static final int TUNNEL_SPEED = 3;
    /** Vitesse du fantôme lorsqu'il à été mangé par {@link PacMan}. */
    private static final int EATEN_SPEED = 20;

    /** Types de fantômes disponibles. (BLINKY, PINKY, INKY et CLYDE).
     * Chaque fantôme a un comportement différent lors du jeu.
    */
    public enum GhostType {
        /** {@link Ghost} rouge. */
        BLINKY,
        /** {@link Ghost} rose. */
        PINKY,
        /** {@link Ghost} cyan. */
        INKY,
        /** {@link Ghost} jaune. */
        CLYDE
    }

    /** Etat que peut prendre un {@link Ghost} au cours d'une partie. (NORMAL, FRIGTHENED, EATEN) */
    public enum GhostState {
        /** Etat de base/normal. */
        NORMAL,
        /** Etat apeuré, il prend des directions aléatoires et change de couleur.*/
        FRIGHTENED,
        /** Etat mangé, il se rend vers le spawn. */
        EATEN
    }

    // region Variables

    /** Type du fantôme actuel. */
    private final GhostType ghostType;
    /** Etat du fantôme actuel. */
    private GhostState ghostState = GhostState.NORMAL;

    /** Coordonnées cible du {@link Ghost}. */
    private RealCoordinates target;
    /** {@link PacMan} attaché au {@link Ghost}. */
    private PacMan pacman;
    private PacMan pacman2;
    private final RealCoordinates eatenTarget;
    private final RealCoordinates scatterTarget;

    /** Variable nécessaire seulement pour les {@link Ghost} de type {@link GhostType INKY}.
     * Utile notamment pour le calcul de sa cible en {@link GhostState#CHASE}.
    */
    private final Ghost inkyLinkedGhost;

    private boolean activated = false;

    /** Pacman CHASED (Mode Coop). */
    private PacMan chaseTarget;

    /** Indice de {@link Ghost} par rapport à {@code chaseTarget} (Mode Coop). */
    private int chaseTargetIndex;

    /** Buffer utilisé pour le mode versus en multijoueur. */
    private DirectionBuffer directionBuffer = null;
    private GraphicsBuffer graphicsBuffer = new GraphicsBuffer();

    // endregion Variables :

    // region Constructeurs :

    /**
     * Constructeur pour un {@link Ghost}.
     * @param ghostType
     * @param config
     */
    public Ghost(final MazeConfig config, final GhostType ghostType) {
        this(config, ghostType, null);
    }

    /**
     * Constructeur pour un {@link Ghost}.
     * Inky à besoin d'un fantôme lié pour calculer sa cible en mode CHASE.
     * @param config
     * @param ghostType
     * @param inkyLinkedGhost
     */
    public Ghost(final MazeConfig config, final GhostType ghostType, final Ghost inkyLinkedGhost) {
        this.ghostType = ghostType;
        this.inkyLinkedGhost = inkyLinkedGhost;
        final RealCoordinates spawnPos = initStartValue(config, "spawn");
        this.eatenTarget = initStartValue(config, "eaten");
        this.scatterTarget = initStartValue(config, "scatter");
        this.target = scatterTarget;
        this.pacman = config.getPacman();
        this.pacman2 = config.getPacman2();
        setSpawnPos(spawnPos);
        setPos(spawnPos);
        return;
    }

    /**
     * Méthode qui initialise le target de son spawn, du scatter et du eaten
     * par rapport au terrain donné.
     * @param config
     * @param posName
     * @return {@link RealCoordinates} Coordonnées associé au String donné en paramètre.
     */
    public RealCoordinates initStartValue(final MazeConfig config, final String posName) {
        // CHECKSTYLE:OFF
        final RealCoordinates entrance = config.getGhostSpawnEntrance();
        if (posName.equals("eaten")) {
            switch (ghostType) {
                case BLINKY:
                case PINKY:
                    return new RealCoordinates(entrance.x(), entrance.y() + 3);
                case INKY:
                    return new RealCoordinates(entrance.x() - 2, entrance.y() + 3);
                case CLYDE:
                    return new RealCoordinates(entrance.x() + 2, entrance.y() + 3);
                default:
                    break;
            }
        } else if (posName.equals("scatter")) {
            switch (ghostType) {
                case BLINKY:
                    return new RealCoordinates(config.getWidth() - 3, 0);
                case PINKY:
                    return new RealCoordinates(2, 0);
                case INKY:
                    return new RealCoordinates(config.getWidth() - 1, config.getHeight() - 2);
                case CLYDE:
                    return new RealCoordinates(0, config.getHeight() - 2);
                default:
                    break;
            }
        } else if (posName.equals("spawn")) {
            switch (ghostType) {
                case BLINKY:
                    return new RealCoordinates(entrance.x(), entrance.y() + 1);
                case PINKY:
                    return new RealCoordinates(entrance.x(), entrance.y() + 3);
                case INKY:
                    return new RealCoordinates(entrance.x() - 2, entrance.y() + 3);
                case CLYDE:
                    return new RealCoordinates(entrance.x() + 2, entrance.y() + 3);
                default:
                    break;
            }
        }
        // CHECKSTYLE:ON
        return new RealCoordinates(0, 0);
    }

    // endregion Constructeurs

    /**
     * Fonction générale qui actualise tous les paramètres d'un {@link Ghost}.
     * @param state {@link MazeState}
     * @param deltaTns Tick de temps.
     */
    public void updateGhost(final MazeState state, final long deltaTns) {
        updateState(state);
        updateTarget(state);
        updateDirection(state);
        updatePosition(deltaTns, state.getConfig());
        updateSpeed(state);
    }

    /**
     * Méthode qui met à jour le fantôme joueur.
     * @param state {@link MazeState}
     * @param deltaTns Tick de temps.
     */
    public void updatePlayerGhost(final MazeState state, final long deltaTns) {
        if (isInSpawn(state.getConfig())) {
            ghostState = GhostState.NORMAL;
        }
        if (directionBuffer.isBufferDirectionValid(this, deltaTns, state.getConfig())) {
            setDirection(directionBuffer.getBufferDirection());
        }
        updatePosition(deltaTns, state.getConfig());
        updateSpeed(state);
        directionBuffer.updateDirectionBufferTime(deltaTns);
    }

    //region State

    /**
     * Actualise les paramètres du {@link Ghost} en fonction de l'état du {@link MazeState}.
     * La fonction renvoie true si elle a déjà changé la direction du {@link Ghost}.
     * @param state
     */
    public void updateState(final MazeState state) {
        if (getPos().equals(eatenTarget) && activated) {
            ghostState = GhostState.NORMAL;
        }
        // Vérifie si les conditions d'activation des fantômes INKY et CLYDE sont remplies.
        if (!activated) {
            switch (ghostType) {
                case INKY:
                    final int inkyThreshold = state.getConfig().getTotalPacGomme() / 10;
                    if (state.getEatenPacgomme() >= inkyThreshold) {
                        activated = true;
                    }
                    break;
                case CLYDE:
                    final int clydeThreshold = state.getConfig().getTotalPacGomme() / 3;
                    if (state.getEatenPacgomme() >= clydeThreshold) {
                        activated = true;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    //endregion State

    //region Target

    /**
     * Actualise la cible du {@link Ghost}.
     * @param state
     */
    public void updateTarget(final MazeState state) {
        final RealCoordinates spawnEntrance = state.getConfig().getGhostSpawnEntrance();
        final int spawnInter = 3;
        final RealCoordinates intersection = spawnEntrance.plus(new RealCoordinates(0, spawnInter));
        if (ghostState == GhostState.EATEN) {
            // Si mangé, alors fixe sa cible à l'entrée du spawn des fantômes.
            if (!target.equals(spawnEntrance) && !isInSpawn(state.getConfig())) {
                target = spawnEntrance;
            // Si il se trouve à l'entrée du spawn, alors fixe sa cible en eatenTarget.
            } else if (!target.equals(eatenTarget) && isInSpawn(state.getConfig())) {
                target = eatenTarget;
            }
        // Si il est arrivé à la position de son eatenTarget, va vers le milieu.
        } else if (getPos().y() == intersection.y() && isInSpawn(state.getConfig())) {
            target = intersection;

        // Si l'état actuel du jeu est "SCATTER", alors fixe sa cible en scatterTarget.
        } else if (state.getCurrentState() == GameState.SCATTER && !target.equals(scatterTarget)) {
            target = scatterTarget;

        // Si l'état actuel du jeu est "CHASE", alors fixe sa cible en chaseTarget.
        } else if (state.getCurrentState() == GameState.CHASE) {
            if (state.getConfig().isCoop()) {
                target = calcChaseTargetCoop();
            } else {
                target = calcChaseTarget();
            }
        }
    }
    /**
     * Méthode qui renvoie la cible du {@link Ghost} lorsqu'il est en {@link GhostType#CHASE}.
     * <br><br> Chaque fantôme calcule sa cible différement.<br><br>
     * La cible de BLINKY and PINKY est toujours la position actuelle de PacMan et Pacman2.
     * La cible de INKY et CKYDE dépend de son {@code chaseTargetIndex}
     * @return cible {@link RealCoordinates}
     */
    public RealCoordinates calcChaseTargetCoop() {
        PacMan chosenPacman = choosePacman();
        if (this.chaseTarget != chosenPacman
            && ghostType != GhostType.BLINKY
            && ghostType != GhostType.PINKY) {
            chosenPacman.setNbGhostChased(chosenPacman.getNbGhostChased() + 1);
            if (chaseTarget != null) {
                chaseTarget.setNbGhostChased(chaseTarget.getNbGhostChased() - 1);
            }
            chaseTarget = chosenPacman;
        }
        if (ghostType != GhostType.BLINKY && ghostType != GhostType.PINKY) {
            chaseTargetIndex = chosenPacman.getNbGhostChased();
        }
        switch (ghostType) {
            case BLINKY:
                return calcBlinkyChaseTarget(pacman);
            //CHECKSTYLE:OFF
            case INKY:
                switch (this.chaseTargetIndex) {
                    case 2:
                        return calcPinkyChaseTarget(chaseTarget);
                    case 3:
                        return calcClydeChaseTarget(chaseTarget);
                    default:
                        break;
                }
            case CLYDE:
                switch (this.chaseTargetIndex) {
                    case 2:
                        return calcPinkyChaseTarget(chaseTarget);
                    case 3:
                        return calcClydeChaseTarget(chaseTarget);
                    default:
                        break;
                }
                //CHECKSTYLE:ON
            case PINKY:
                return calcBlinkyChaseTarget(pacman2);
            default:
                break;
        }
        return chaseTarget.getPos();
    }


    /** Méthode qui renvoie quel pacman à suivre en Coop.
     * @return {@link Pacman}
     */
    public PacMan choosePacman() {
        if (getDistance(getPos(), pacman.getPos()) <= getDistance(getPos(), pacman2.getPos())) {
            if (pacman.getNbGhostChased() < pacman.getNbGhostChasedMax()) {
                return pacman;
            }
        }
        if (pacman2.getNbGhostChased() > pacman2.getNbGhostChasedMax()) {
            return pacman;
        }
        return pacman2;
    }

    /**
     * Méthode qui renvoie la cible du {@link Ghost} lorsqu'il est en {@link GhostType#CHASE}.
     * <br><br> Chaque fantôme calcule sa cible différement.<br><br>
     * @return cible {@link RealCoordinates}
     */
    public RealCoordinates calcChaseTarget() {
        switch (ghostType) {
            case BLINKY:
                return calcBlinkyChaseTarget(pacman);
            case PINKY:
                return calcPinkyChaseTarget(pacman);
            case INKY:
                return calcInkyChaseTarget(pacman);
            case CLYDE:
                return calcClydeChaseTarget(pacman);
            default:
                break;
        }
        return pacman.getPos();
    }

    /**
     * La cible de Blinky est toujours la position actuelle de PacMan.
     * @param pac
     * @return cible {@link RealCoordinates}
     */
    public RealCoordinates calcBlinkyChaseTarget(final PacMan pac) {
        return pac.getPos();
    }

    /**
     * La cible de Pinky est toujours 4 cases devant PacMan. <br><br>
     * Exception pour la direction Nord où la cible est aussi décalé de 4 cases à l'Est.
     * @param pac
     * @return cible {@link RealCoordinates}
     */
    public RealCoordinates calcPinkyChaseTarget(final PacMan pac) {
        final int distance = 4;
        RealCoordinates targetCell = pac.getLastDirection().directionToUnit().times(distance);
        if (pac.getLastDirection() == NORTH) {
            targetCell = targetCell.plus(new RealCoordinates(-distance, 0));
        }
        return pac.getPos().plus(targetCell);
    }

    /**
     * La cible de Inky est toujours la symétrie centrale entre la position de Blinky et
     * la 2eme case devant PacMan. <br> <br>
     * Exception pour le Nord où la case devant PacMan est aussi décalé de 2 cases à l'Est.
     * @param pac
     * @return cible {@link RealCoordinates}
     */
    public RealCoordinates calcInkyChaseTarget(final PacMan pac) {
        final int distance = 2;
        RealCoordinates blinkyPos = inkyLinkedGhost.getPos();
        RealCoordinates pacTPos = pac.getPos().
        plus(pac.getLastDirection().directionToUnit().times(distance));
        if (pac.getLastDirection() == NORTH) {
            pacTPos = pacTPos.plus(new RealCoordinates(-distance, 0));
        }
        RealCoordinates vecteur =
            new RealCoordinates(pacTPos.x() - blinkyPos.x(), pacTPos.y() - blinkyPos.y()).times(2);
        return blinkyPos.plus(vecteur);
    }

    /**
     * La cible de Clyde est la position de PacMan mais lorsqu'il est à 8 cases de PacMan,
     * sa cible devient celle de son mode {@GhostType#SCATTER}.
     * @param pac
     * @return cible {@link RealCoordinates}
     */
    public RealCoordinates calcClydeChaseTarget(final PacMan pac) {
        final int radiusRange = 8;
        if (getDistance(getPos(), pac.getPos()) < radiusRange) {
            return scatterTarget;
        }
        return pac.getPos();
    }

    //endregion Target

    //region Direction
    /**
     * Actualise la direction du {@link Ghost}.
     * @param state
     */
    public void updateDirection(final MazeState state) {
        MazeConfig config = state.getConfig();

        // Si le fantôme est dans l'état normal et se trouve dans le spawn,
        // alors le fantôme doit sortir du spawn en prenant un chemin prédéfini.
        if (ghostState != GhostState.EATEN && isInSpawn(config)) {
            setDirection(getDirectionToExitSpawn(config));
            return;
        }

        // Si le fantôme est activé et apeuré et qu'il n'a pas encore été mangé,
        // alors le fantôme doit prendre des directions aléatoires.
        if (activated && ghostState == GhostState.FRIGHTENED) {
            setDirection(getRandomDirection(config));
            return;
        }

        // Si le fantôme est dans l'état mangé et se trouve à l'entrée du spawn ou est dans le spawn
        // alors le fantôme doit aller vers sa cible en prenant un chemin prédéfini.
        if (ghostState == GhostState.EATEN && getPos().equals(config.getGhostSpawnEntrance())
        || isInSpawn(config)) {
            setDirection(getDirectionToEatenTarget());
            return;
        }

        // Quand l'état actuel du jeu change, tous les fantômes doivent faire un 180°.
        GameState gameState = state.getGameState();
        if (gameState != GameState.FRIGHTENED && gameState != state.getCurrentState()) {
            setDirection(getDirection().getOpposite());
            return;
        }

        // Sinon, prend la direction du chemin le plus court vers sa cible.
        setDirection(getDirectionClosestToTarget(config));
    }

    /**
     * La méthode retourne une liste contenant les directions possibles qu'un
     * fantôme peut emprunter à une intersection.
     * @param config
     * @return Une {@code List<Direction>} contenant les directions possibles.
     */
    public List<Direction> possibleDirections(final MazeConfig config) {
        if (isInTunnel(config)) {
            return Arrays.asList(getDirection());
        }
        final RealCoordinates curPos = getPos();
        List<Direction> directions = new ArrayList<>(Arrays.asList(NORTH, WEST, SOUTH, EAST));
        // Si le fantôme est sur 4 cellules en même temps (sécurité).
        if (curPos.y() != Math.floor(curPos.y()) && curPos.x() != Math.floor(curPos.x())) {
            return directions;
        }
        directions.remove(getDirection().getOpposite());
        // Cas où le fantôme se trouve sur les cellules dites "murs invisibles".
        if (config.getCell(curPos.toIntCoordinates()).isRestricted()) {
            directions.remove(NORTH);
        }
        if (curPos.y() != Math.floor(curPos.y())) {
            directions.remove(EAST);
            directions.remove(WEST);
        } else if (curPos.x() != Math.floor(curPos.x())) {
            directions.remove(NORTH);
            directions.remove(SOUTH);
        } else {
            // Cas où le fantôme est au milieu d'une case, alors retire toutes les cases du spawn
            // et celles qui ont un mur.
            List<Direction> possibleDirections = new ArrayList<>();
            for (Direction dir : directions) {
                IntCoordinates adjCoords = curPos.plus(dir.directionToUnit()).toIntCoordinates();
                Cell cell = config.getCell(adjCoords);
                if (!cell.hasWall() && cell.getContent() != Cell.Content.SPAWN) {
                    possibleDirections.add(dir);
                }
            }
            directions = possibleDirections;
        }
        return directions;
    }

    /**
     * La méthode renvoie la {@code Direction} la plus proche de sa cible en
     * utilisant la méthode {@code possibleDirections()}.
     * @param config
     * @return La {@code Direction} la plus proche du target.
     */
    public Direction getDirectionClosestToTarget(final MazeConfig config) {
        // Directions possibles qu'il peut prendre (la direction derrière lui n'est pas incluse).
        List<Direction> possibleDir = possibleDirections(config);

        // Si le fantôme se trouve dans un cul-de-sac, il se retourne (toujours possible)
        if (possibleDir.isEmpty()) {
            return getDirection().getOpposite();
        }
        // Si une seule direction n'est possible, renvoie cette direction.
        if (possibleDir.size() == 1) {
            return possibleDir.get(0);
        }

        // Prend la direction où la distance est la plus petite.
        double[] deltaDist = new double[possibleDir.size()];
        double min = getDistance(getPos().plus(possibleDir.get(0).directionToUnit()), target);
        int indice = 0;
        for (int i = 1; i < possibleDir.size(); i++) {
            final RealCoordinates possiblePos = possibleDir.get(i).directionToUnit();
            deltaDist[i] = getDistance(getPos().plus(possiblePos), target);
            if (deltaDist[i] < min) {
                min = deltaDist[i];
                indice = i;
            }
        }
        return possibleDir.get(indice);
    }

    /**
     * La méthode renvoie une direction "aléatoire" basée sur la direction du
     * fantôme et des probabilités de chaque direction.
     * Les probabilités sont basées sur le jeu original : 16.3% Nord, 25.2% Est,
     * 28.5% Sud, 30.0% Ouest.
     * @param config {@link MazeConfig}
     * @return {@link Direction}
     */
    public Direction getRandomDirection(final MazeConfig config) {
        List<Direction> possibles = possibleDirections(config);
        if (possibles.size() == 1) {
            return possibles.get(0);
        }
        //CHECKSTYLE:OFF: checkstyle:magicnumber
        List<Double> dirProba = new ArrayList<>(Arrays.asList(16.3, 25.2, 28.5, 30.0));
        List<Direction> dirList = new ArrayList<>(Arrays.asList(NORTH, EAST, SOUTH, WEST));

        int i = 0;
        while (i < dirList.size()) {
            if (possibles.contains(dirList.get(i))) {
                i++;
            } else {
                dirProba.set(
                    (i+1)%dirList.size(),
                    dirProba.get(i) + dirProba.get((i+1) % dirList.size())
                );
                dirList.remove(i);
                dirProba.remove(i);
            }
        }
        double random = Math.random() * 100; // 0 à 99
        int j = 0;
        for (int k = 0; k < dirProba.size(); k++) {
            random -= dirProba.get(k);
            if (random <= 0) {
                break;
            }
            j++;
        }
        //CHECKSTYLE:ON: checkstyle:magicnumber
        return dirList.get(j);
    }

    /**
     * Méthode qui renvoie la {@link Direction} que doit prendre le {@link Ghost}
     * pour retourner sur sa cellule de "reset/eatenTarget".
     * @return {@link Direction}
     */
    public Direction getDirectionToEatenTarget() {
        RealCoordinates pos = getPos();
        if (pos.y() == eatenTarget.y()) {
            if (pos.x() < eatenTarget.x()) {
                return EAST;
            }
            return WEST;
        }
        return SOUTH;
    }

    /**
     * Méthode qui indique la trajectoire à prendre pour un fantôme pour sortir du spawn.
     * @param config
     * @return {@code boolean}
     */
    public Direction getDirectionToExitSpawn(final MazeConfig config) {
        RealCoordinates entrance = config.getGhostSpawnEntrance();
        RealCoordinates pos = getPos();
        if (pos.x() == entrance.x()) {
            return NORTH;
        } else if (pos.x() < entrance.x()) {
            return EAST;
        }
        return WEST;
    }

    //endregion Direction

    //region Position

    /**
     * Méthode qui actualise la position d'un fantôme.
     * @param deltaTns
     * @param config
     */
    public void updatePosition(final long deltaTns, final MazeConfig config) {
        if (activated) {
            RealCoordinates pos = getPos();
            RealCoordinates nextPos = nextPos(deltaTns);
            final int width = config.getWidth();
            final int height = config.getHeight();
            if (pos.equals(nextPos)) {
                return;
            } else if (pos.haveSameNeighbours(nextPos)) {
                nextPos = pos.adjustNextPos(nextPos);
            } else {
                nextPos = furthestFreeCell(nextPos, config);
            }
            if (wentThroughTarget(nextPos)) {
                nextPos = target;
            }
            setPos(nextPos.warp(width, height));
        }
    }


    /**
     * Méthode qui vérifie si le {@link Ghost} à traversé la position de son target.
     * @param nextPos
     * @return {@code boolean} Vrai si le {@link Ghost} à traversé son target, faux sinon.
     */
    public boolean wentThroughTarget(final RealCoordinates nextPos) {
        final RealCoordinates curPos = getPos();
        if (curPos.x() == target.x() && curPos.y() == target.y()) {
            return false;
        }
        boolean isInLine = false;
        // Problème si on traverse un tunnel qui se trouve sur la même ligne que le target.
        // Il va se tp sur le target et nom de l'autre côté du tunnel car la méthode est vraie.
        // Il n'y a pas de vrai moyen pour vérifier cela... La seule combine que j'ai trouvé est de
        // vérifier si les 2 positions ont le même signe
        if (curPos.x() == target.x() && nextPos.x() == target.x() && curPos.x() * nextPos.x() > 0) {
            isInLine = true;
        }
        if (curPos.y() == target.y() && nextPos.y() == target.y() && curPos.y() * nextPos.y() > 0) {
            isInLine = true;
        }
        if (!isInLine) {
            return false;
        }
        return curPos.getDirectionTo(target) != nextPos.getDirectionTo(target);
    }

    /**
     * Méthode qui est appelée lorsqu'un fantôme a une très grande vitesse.
     * On doit alors vérifier qu'entre sa position actuelle et sa prochaine position qu'il n'y
     * ait pas de murs entre ces 2 positions.
     * @param end
     * @param config
     * @return {@link RealCoordinates}
     */
    public RealCoordinates furthestFreeCell(final RealCoordinates end, final MazeConfig config) {
        RealCoordinates pos = getPos();
        final Direction dir = pos.getDirectionTo(end);
        RealCoordinates tmpPos = pos;
        while (dir == tmpPos.getDirectionTo(end)) {
            // Cas où on trouve un mur
            if (config.getCell(tmpPos.plus(dir.directionToUnit()).toIntCoordinates()).hasWall()) {
                return tmpPos.roundFromDirection(getDirection());
            } else {
                tmpPos = tmpPos.plus(dir.directionToUnit());
            }
        }
        // Cas où aucun mur n'a été trouvé, renvoie nextPos
        return pos.adjustNextPos(end);
    }

    //endregion Position

    /**
     * Actualise la vitesse du {@link Ghost}.
     * @param state
     */
    public void updateSpeed(final MazeState state) {
        MazeConfig config = state.getConfig();
        if (getSpeed() != TUNNEL_SPEED && isInTunnel(config)) {
            setSpeed(TUNNEL_SPEED);
        } else if (getSpeed() != EATEN_SPEED && ghostState == GhostState.EATEN) {
            setSpeed(EATEN_SPEED);
        } else if (getSpeed() != getNormalSpeed()) {
            setSpeed(getNormalSpeed());
        }
    }

    /**
     * La méthode renvoie {@code true} si le fantôme est dans un tunnel.
     * @param config {@link MazeConfig}
     * @return {@code true} si dans un tunnel
     */
    public boolean isInTunnel(final MazeConfig config) {
        return config.getCell(getPos().toIntCoordinates()).getContent() == Content.TUNNEL;
    }

    /**
     * Méthode qui renvoie vraie si le {@link RealCoordinates}
     * donné se trouve dans le spawn des {@link Ghost}.
     * @param config {@link MazeConfig}
     * @return true ou false
     */
    public boolean isInSpawn(final MazeConfig config) {
        Set<IntCoordinates> neighbours = getPos().intNeighbours();
        for (var cell : neighbours) {
            if (config.getCell(cell).getContent() == Content.SPAWN) {
                return true;
            }
        }
        return false;
    }

    /**
     * Renvoie la distance entre 2 {@link RealCoordinates}.
     * @param pos1 {@link RealCoordinates}
     * @param pos2 {@link RealCoordinates}
     * @return {@code double}
     */
    public static double getDistance(final RealCoordinates pos1, final RealCoordinates pos2) {
        return Math.abs(
            Math.sqrt(Math.pow(pos2.x() - pos1.x(), 2) + Math.pow(pos2.y() - pos1.y(), 2))
        );
    }

    public GhostType getGhostType() {
        return ghostType;
    }

    public RealCoordinates getTarget() {
        return target;
    }

    public void setTarget(final RealCoordinates target) {
        this.target = target;
    }

    public PacMan getPacman() {
        return pacman;
    }

    public PacMan getPacman2() {
        return pacman2;
    }

    public void setPacman(final PacMan pacman) {
        this.pacman = pacman;
    }

    public RealCoordinates getEatenTarget() {
        return eatenTarget;
    }

    public RealCoordinates getScatterTarget() {
        return scatterTarget;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(final boolean activated) {
        this.activated = activated;
    }

    public DirectionBuffer getDirectionBuffer() {
        return directionBuffer;
    }

    public void setDirectionBuffer(final DirectionBuffer directionBuffer) {
        this.directionBuffer = directionBuffer;
    }

    public boolean isPlayer() {
        return directionBuffer != null;
    }

    public GraphicsBuffer getGraphicsBuffer() {
        return graphicsBuffer;
    }

    public void setGhostState(final GhostState ghostState) {
        this.ghostState = ghostState;
    }

    public GhostState getGhostState() {
        return ghostState;
    }

    /** Méthode qui remet le chaseTarget à null lors de réinitialisation. */
    public void setChaseTargetNull() {
        this.chaseTarget = null;
    }
}
