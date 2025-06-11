package gui.controller;

import java.util.Map;
import java.util.Map.Entry;

import model.Direction;
import gui.InGameGui;
import javafx.scene.input.KeyCode;

public abstract class Controller {
    private static InGameGui inGameGui;

    protected Controller() { }

    public static void setInGameGui(final InGameGui inGameGui) {
        Controller.inGameGui = inGameGui;
    }

    protected static InGameGui getInGameGui() {
        return inGameGui;
    }

    /**
     * Retourne le{@code KeyCode}associé à la direction{@code dir}.
     * @param keybinds
     * @param dir
     * @return {@code KeyCode}
     */
    public static KeyCode getKeyCode(
    final Map<KeyCode, Direction> keybinds, final Direction dir) {
        for (Entry<KeyCode, Direction> entry : keybinds.entrySet()) {
            if (entry.getValue() == dir) {
                return entry.getKey();
            }
        }
        return KeyCode.UNDEFINED;
    }
}
