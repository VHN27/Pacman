package gui.controller;

import javafx.scene.input.KeyEvent;

public interface KeyHandlers {
    /**
     * Méthode appelée lorsqu'une touche est appuyée.
     * Elle contrôle le buffer de direction et l'activation des fantômes.
     * @param event {@code KeyEvent}
     */
    void keyPressedHandler(KeyEvent event);
}
