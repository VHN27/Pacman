package gui.graphics;

public final class GraphicsBuffer {
    // Buffer pour les images des critters
    private double updateBuffer = 0;
    private double alternateBuffer = 0;
    /** Boolean indiquant s'il faut clignoter les fantômes. */
    private boolean alternate = false;
    /** Boolean pour changer le bas de l'image du fantôme. */
    private boolean alternateMove = false;
    /** Boolean indiquant s'il faut remettre l'image par défaut du Pac-Man. */
    private boolean resetPacImg = false;
    private boolean add = true;
    private int phase = 0;

    public double getUpdateBuffer() {
        return updateBuffer;
    }

    public double getAlternateBuffer() {
        return alternateBuffer;
    }

    public void setUpdateBuffer(final double graphicsBuffer) {
        this.updateBuffer = graphicsBuffer;
    }

    public void setAlternateBuffer(final double alternateBuffer) {
        this.alternateBuffer = alternateBuffer;
    }

    public boolean isAlternate() {
        return alternate;
    }

    public void setAlternate(final boolean alternate) {
        this.alternate = alternate;
    }

    public void setResetPacImg(final boolean resetPacImg) {
        this.resetPacImg = resetPacImg;
    }

    public boolean isResetPacImg() {
        return resetPacImg;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(final int phase) {
        this.phase = phase;
    }

    public boolean isAdd() {
        return add;
    }

    public void setAdd(final boolean add) {
        this.add = add;
    }

    public boolean isAlternateMove() {
        return alternateMove;
    }

    public void setAlternateMove(final boolean alternateMove) {
        this.alternateMove = alternateMove;
    }
}
