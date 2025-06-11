package misc;

import model.Ghost;
import model.PacMan;

public final class Debug {
    //CHECKSTYLE:OFF
    private Debug() {
    }

    /**
     * Méthode pour tester le code en faisant print un message.
     * @param message Le message à print
     */
    public static void out(final String message) {
        // comment this out if you do not want to see the debug messages
        System.out.println(">> DEBUG >> " + message);
    }

    /**
     * Renvoie un message donnant toutes les informations de pacman.
     * @param pacman
     */
    public static void debugPacMan(final PacMan pacman) {
        System.out.print("Position : " + pacman.getPos() + " | ");
        System.out.print("Direction : " + pacman.getDirection() + " | ");
        System.out.print("Etat énergisé : " + pacman.isEnergized() + " | ");
        System.out.println();
    }
    /**
     * Renvoie un message donnant toutes les informations du fantôme.
     * @param ghost
     */
    public static void debugGhost(final Ghost ghost) {
        System.out.print("Direction : " + ghost.getDirection() + " | ");
        System.out.println();
    }
    //CHECKSTYLE:ON
}
