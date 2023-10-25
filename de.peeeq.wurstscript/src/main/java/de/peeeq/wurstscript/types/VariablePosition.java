package de.peeeq.wurstscript.types;

/**
 *
 */
public enum VariablePosition {
    LEFT, RIGHT, NONE;

    public VariablePosition inverse() {
        switch (this) {
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            default:
                return NONE;
        }
    }
}
