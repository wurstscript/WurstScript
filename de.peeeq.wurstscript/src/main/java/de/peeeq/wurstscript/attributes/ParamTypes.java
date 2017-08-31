package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class ParamTypes {
    private final List<ParamInfo> params;

    public static ParamTypes noParams() {
        return new ParamTypes(new ArrayList<>());
    }

    public ParamTypes(List<ParamInfo> params) {
        this.params = params;
    }

    public static ParamTypes fromParams(WParameters parameters) {
        List<ParamInfo> params = new ArrayList<>(parameters.size());
        for (int i = 0; i < parameters.size(); i++) {
            params.add(new ParamInfo(i, parameters.get(i)));
        }
        return new ParamTypes(params);
    }

    public int paramCount() {
        return params.size();
    }

    public ParamInfo get(int i) {
        return params.get(i);
    }




    public static class ParamInfo {
        private final int index;
        private final String name;
        private final WurstType type;
        private final boolean isOptional;

        public ParamInfo(int index, String name, WurstType type, boolean isOptional) {
            this.index = index;
            this.name = name;
            this.type = type;
            this.isOptional = isOptional;
        }

        public ParamInfo(int index, WParameter param) {
            this.index = index;
            this.name = param.getName();
            this.type = param.attrTyp();
            this.isOptional = param.getDefaultValue() instanceof Expr;
        }

        public String getName() {
            return name;
        }

        public WurstType getType() {
            return type;
        }

        public boolean isOptional() {
            return isOptional;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            if (isOptional) {
                return type + "? " + name;
            }
            return type + " " + name;
        }

        public ParamInfo setTypeArgs(Map<TypeParamDef, WurstTypeBoundTypeParam> typeArgBinding) {
            WurstType newType = type.setTypeArgs(typeArgBinding);
            if (newType == type) {
                return this;
            }
            return new ParamInfo(index, name, newType, isOptional);
        }
    }

    public List<WurstType> getTypeList() {
        return params.stream()
                .map(ParamInfo::getType)
                .collect(Collectors.toList());
    }

    public ParamTypes setTypeArgs(Map<TypeParamDef, WurstTypeBoundTypeParam> typeArgBinding) {
        if (typeArgBinding.isEmpty()) {
            return this;
        }
        List<ParamInfo> newParams = params.stream()
                .map(p -> p.setTypeArgs(typeArgBinding))
                .collect(Collectors.toList());
        return new ParamTypes(newParams);
    }

    public long getMinRequiredArgs() {
        return params.stream()
                .filter(p -> !p.isOptional)
                .count();
    }

    public long getMaxArgs() {
        return params.size();
    }

    public Optional<ParamInfo> getParam(String name) {
        return params.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();
    }

    public ParamInfo getParam(int index) {
        return params.get(index);
    }

    public Optional<WurstType> getParamType(String name) {
        return params.stream()
                .filter(p -> p.getName().equals(name))
                .map(ParamInfo::getType)
                .findFirst();
    }

    public Optional<WurstType> getParamType(Identifier name) {
        return getParamType(name.getName());
    }

    public WurstType getParamType(int i) {
        return params.get(i).getType();
    }

    public String getParamName(int i) {
        return params.get(i).getName();
    }

    public List<ParamInfo> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return Utils.join(params, ", ");
    }
}
