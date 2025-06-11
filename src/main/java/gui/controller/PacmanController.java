package gui.controller;

import model.Direction;
import model.PacMan;
import utils.Music;

import java.util.EnumMap;
import java.util.Map;

import gui.InGameGui;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public final class PacmanController extends Controller implements KeyHandlers {
    private static EnumMap<KeyCode, Direction> keybinds = new EnumMap<>(KeyCode.class);
    static {
        keybinds.put(KeyCode.UP, Direction.NORTH);
        keybinds.put(KeyCode.LEFT, Direction.WEST);
        keybinds.put(KeyCode.DOWN, Direction.SOUTH);
        keybinds.put(KeyCode.RIGHT, Direction.EAST);
    }
    private PacMan pacman;

    /**
     * Constructeur pour {@link PacmanController}.
     * Associe un objet {@link PacMan} à une "manette".
     * @param pacman {@link PacMan}
     */
    public PacmanController(final PacMan pacman) {
        this.pacman = pacman;
    }

    @Override
    public void keyPressedHandler(final KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && !InGameGui.isPaused() && !InGameGui.isEnd()) {
            getInGameGui().pause(true);
        } else if (event.getCode() == KeyCode.ESCAPE) {
            getInGameGui().pause(false);
        }

        if (InGameGui.isPaused() || Music.isStartBgmPlaying()) {
            return;
        }

        if (keybinds.containsKey(event.getCode())) {
            pacman.getDirectionBuffer().setBufferDirection(
                keybinds.get(event.getCode())
            );
        } else {
            pacman.getDirectionBuffer().getBufferDirection();
        }
        pacman.getDirectionBuffer().setDirectionBufferTimer(0);
    }

    public static Map<KeyCode, Direction> getKeybinds() {
        return keybinds;
    }
}
