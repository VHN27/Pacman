package utils;

public final class Fps {
    /** Somme de tick. */
    private static long fpsCheckTimer = 0;
    /** Frames totales depuis le dernier check. */
    private static int totalFrames = 0;

    /** Une nanoseconde. */
    private static final double NANOSECONDS = Math.pow(10, 9);

    private Fps() {
    }

    /**
     * Incrémente {@link #fpsCheckTimer} chaque tick.
     * Si {@link #fpsCheckTimer} dépasse une seconde, les FPS
     * sont print.
     * @param deltaTns Un tick
     */
    public static void updateFps(final long deltaTns) {
        Fps.fpsCheckTimer += deltaTns;
        Fps.totalFrames += 1;
        if (fpsCheckTimer / NANOSECONDS > 1) {
            System.out.println("FPS : " + totalFrames);
            totalFrames = 0;
            fpsCheckTimer = 0;
        }
    }
}
