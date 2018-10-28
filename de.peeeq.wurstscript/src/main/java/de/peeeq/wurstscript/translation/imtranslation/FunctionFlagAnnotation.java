package de.peeeq.wurstscript.translation.imtranslation;

public class FunctionFlagAnnotation implements FunctionFlag {
    private final String annotation;

    public FunctionFlagAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getAnnotation() {
        return annotation;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((annotation == null) ? 0 : annotation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FunctionFlagAnnotation other = (FunctionFlagAnnotation) obj;
        if (annotation == null) {
            if (other.annotation != null)
                return false;
        } else if (!annotation.equals(other.annotation))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return annotation;
    }


}