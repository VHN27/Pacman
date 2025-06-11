package gui.graphics;

import javafx.scene.Node;

public interface GraphicsUpdater {
    /** Méthode qui est appelée à chaque tick. */
    void update();
    /**
     * Méthode qui retourne le{@code Node}.
     * @return {@code Node}
     */
    Node getNode();
}
