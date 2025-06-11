package geometry;

public record IntCoordinates(int x, int y) {
    /**
     * Méthode convertissant un {@link IntCoordinates}
     * en {@link RealCoordinates} en fonction du{@code scale}.
     * @param scale Echelle
     * @return {@link RealCoordinates}
     */
    public RealCoordinates toRealCoordinates(final double scale) {
        return new RealCoordinates(x * scale, y * scale);
    }
}
