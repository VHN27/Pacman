package gui.controller;

import java.util.EnumMap;

import gui.InGameGui;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.Critter;
import model.Direction;
import model.Ghost;
import model.PacMan;
import utils.Music;

public final class PlayerTwoController extends Controller implements KeyHandlers {
    private static EnumMap<KeyCode, Direction> keybinds = new EnumMap<>(KeyCode.class);
    static {
        keybinds.put(KeyCode.Z, Direction.NORTH);
        keybinds.put(KeyCode.Q, Direction.WEST);
        keybinds.put(KeyCode.S, Direction.SOUTH);
        keybinds.put(KeyCode.D, Direction.EAST);
    }
    private Critter playerTwo;

    /**
     * Constructeur pour {@link PlayerTwoController}.
     * Associe un objet {@link Critter} à une "manette".
     * @param playerTwo {@link Critter}
     */
    public PlayerTwoController(final Critter playerTwo) {
        this.playerTwo = playerTwo;
    }

    @Override
    public void keyPressedHandler(final KeyEvent event) {
        if (InGameGui.isPaused() || Music.isStartBgmPlaying()) {
            return;
        }

        if (playerTwo instanceof PacMan) {
            if (keybinds.containsKey(event.getCode())) {
                ((PacMan) playerTwo).getDirectionBuffer().setBufferDirection(
                    keybinds.get(event.getCode())
                );
            } else {
                ((PacMan) playerTwo).getDirectionBuffer().getBufferDirection();
            }
            ((PacMan) playerTwo).getDirectionBuffer().setDirectionBufferTimer(0);
        } else {
            if (keybinds.containsKey(event.getCode())) {
                ((Ghost) playerTwo).getDirectionBuffer().setBufferDirection(
                    keybinds.get(event.getCode())
                );
            } else {
                ((Ghost) playerTwo).getDirectionBuffer().getBufferDirection();
            }
            ((Ghost) playerTwo).getDirectionBuffer().setDirectionBufferTimer(0);
        }
    }

    public static EnumMap<KeyCode, Direction> getKeybinds() {
        return keybinds;
    }
}
