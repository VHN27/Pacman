package gui;

import javafx.application.Application;

public final class Launcher {
    private Launcher() { }

    /**
     * Démarre le jeu.
     * @param args
     */
    public static void main(final String[] args) {
        Application.launch(Intro.class, args);
    }
}
