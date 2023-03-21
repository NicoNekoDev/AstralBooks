package ro.niconeko.astralbooks.utils;

public enum Side {
    LEFT("left_side"), RIGHT("right_side");

    private final String side;

    Side(String side) {
        this.side = side;
    }

    @Override
    public String toString() {
        return this.side;
    }

    public static Side fromString(String side) {
        if (side.equalsIgnoreCase("left_side"))
            return LEFT;
        return RIGHT;
    }
}
