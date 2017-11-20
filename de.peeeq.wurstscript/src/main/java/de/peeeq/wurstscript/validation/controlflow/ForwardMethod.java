package de.peeeq.wurstscript.validation.controlflow;

import de.peeeq.wurstscript.ast.AstElementWithBody;
import de.peeeq.wurstscript.ast.WStatement;

import java.util.Collection;

public abstract class ForwardMethod<T, Target extends AstElementWithBody> {

    private Target f;


    abstract T calculate(WStatement s, T incoming);

    abstract T merge(Collection<T> values);

    boolean equality(T a, T b) {
        return a.equals(b);
    }

    String print(T t) {
        if (t == null) return "null";
        return t.toString();
    }

    abstract void checkFinal(T fin);

    public abstract T startValue();

    public Target getFuncDef() {
        return f;
    }

    public void setFuncDef(Target f) {
        this.f = f;
    }

    public void execute(Target f) {
        this.f = f;
        ForwardExecution<T, Target> ex = new ForwardExecution<>(f, this);
        ex.execute();
    }

}
