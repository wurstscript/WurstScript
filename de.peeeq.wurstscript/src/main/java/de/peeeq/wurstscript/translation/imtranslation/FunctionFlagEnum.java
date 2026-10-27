package de.peeeq.wurstscript.translation.imtranslation;

public enum FunctionFlagEnum implements FunctionFlag {
    IS_BJ,
    IS_NATIVE,
    IS_TEST,
    IS_COMPILETIME_NATIVE,
    IS_EXTERN,
    IS_VARARG,
    PRESERVE_NAME,
    /** A source-owned, independent hot-reload initialization action. */
    HOT_RELOAD_INIT_ACTION,
    /** A Jass function which returns an integer as a real (legacy targets only); inlined, the integer would land in an operand. */
    RETURNS_INT_AS_REAL

}
