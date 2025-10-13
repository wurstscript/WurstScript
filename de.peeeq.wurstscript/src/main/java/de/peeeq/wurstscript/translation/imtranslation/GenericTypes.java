package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * wraps an imType and adds a hashmap and equals method
 */
class GenericTypes {
    private final List<ImTypeArgument> typeArguments;


    public GenericTypes(List<ImTypeArgument> typeArguments) {
        for (ImTypeArgument ta : typeArguments) {
            Preconditions.checkArgument(!EliminateGenerics.isGenericType(ta.getType()), "Type arguments must not be generic: " + typeArguments);
        }
        this.typeArguments = ImmutableList.copyOf(typeArguments);
    }

    public List<ImTypeArgument> getTypeArguments() {
        return typeArguments;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GenericTypes) {
            GenericTypes ot = (GenericTypes) o;
            if (typeArguments.size() != ot.typeArguments.size()) {
                return false;
            }
            for (int i = 0; i < typeArguments.size(); i++) {
                ImTypeArgument t1 = typeArguments.get(i);
                ImTypeArgument t2 = ot.typeArguments.get(i);
                if (!t1.getType().equalsType(t2.getType())) {
                    return false;
                }
                if (!t1.getTypeClassBinding().equals(t2.getTypeClassBinding())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int res = 7;
        for (ImTypeArgument it : typeArguments) {
            res = 131 * res + hashType(it.getType());
        }
        return res;
    }

    private static int hashType(ImType t) {
        return t.match(new ImType.Matcher<Integer>() {
            @Override
            public Integer case_ImAnyType(ImAnyType imAnyType) {
                return 953;
            }

            @Override
            public Integer case_ImTupleType(ImTupleType t) {
                int res = 172;
                for (ImType it : t.getTypes()) {
                    res = 73 * res + hashType(it);
                }
                return res;
            }

            @Override
            public Integer case_ImVoid(ImVoid imVoid) {
                return 183;
            }

            @Override
            public Integer case_ImClassType(ImClassType ct) {
                int res = ct.getClassDef().hashCode();
                for (ImTypeArgument it : ct.getTypeArguments()) {
                    res = 73 * res + hashType(it.getType());
                }
                return res;
            }

            @Override
            public Integer case_ImArrayTypeMulti(ImArrayTypeMulti t) {
                return 9931532 + hashType(t.getEntryType());
            }

            @Override
            public Integer case_ImSimpleType(ImSimpleType t) {
                return 234312 + t.getTypename().hashCode();
            }

            @Override
            public Integer case_ImArrayType(ImArrayType t) {
                return 91532 + hashType(t.getEntryType());
            }

            @Override
            public Integer case_ImTypeVarRef(ImTypeVarRef t) {
                return t.getTypeVariable().hashCode();
            }
        });
    }

    public String makeName() {
        StringBuilder sb = new StringBuilder();
        for (ImTypeArgument ta : typeArguments) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            ta.getType().print(sb, 0);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<");
        for (ImTypeArgument ta : typeArguments) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            ta.getType().print(sb, 0);
        }
        sb.append(">");
        return sb.toString();
    }

    public GenericTypes take(int n) {
        List<ImTypeArgument> list = new ArrayList<>();
        long limit = n;
        for (ImTypeArgument typeArgument : typeArguments) {
            if (limit-- == 0) break;
            list.add(typeArgument);
        }
        return new GenericTypes(
            list
        );
    }

    public GenericTypes drop(int n) {
        List<ImTypeArgument> list = new ArrayList<>();
        long toSkip = n;
        for (ImTypeArgument typeArgument : typeArguments) {
            if (toSkip > 0) {
                toSkip--;
                continue;
            }
            list.add(typeArgument);
        }
        return new GenericTypes(
            list
        );
    }

    public boolean containsTypeVariable() {
        for (ImTypeArgument ta : typeArguments) {
            if (EliminateGenerics.containsTypeVariable(ta.getType())) {
                return true;
            }
        }
        return false;
    }
}
