package loglang.symbol;

import loglang.lang.Operator;
import loglang.lang.OperatorTable;
import nez.peg.tpeg.type.LType;
import nez.peg.tpeg.type.TypeEnv;
import nez.peg.tpeg.type.TypeException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by skgchxngsxyz-osx on 15/08/25.
 */
public class SymbolTable {
    private final ArrayList<ClassScope> cases = new ArrayList<>();
    private final Map<String, Integer> labelMap = new HashMap<>();
    private final Map<String, MemberRef.StaticMethodRef> operatorMap;

    public SymbolTable(TypeEnv env) {
        this.operatorMap = initOperatorMap(env);
    }

    /**
     *
     * @param labelName
     * may be null, if has no label name
     * @return
     * created case scope
     */
    public ClassScope newCaseScope(TypeEnv env, String labelName) throws TypeException {
        int index = this.cases.size();
        ClassScope scope = new ClassScope(env.newCaseContextType(index));
        this.cases.add(scope);
        if(labelName != null) {
            if(this.labelMap.put(labelName, index) != null) {
                return null;    // found duplicated label
            }
        }
        return scope;
    }

    /**
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException
     * if not found
     */
    public ClassScope findCaseScope(int index) throws IndexOutOfBoundsException {
        return this.cases.get(index);
    }

    /**
     *
     * @param labelName
     * not null
     * @return
     * if not found, return null
     */
    public ClassScope findCaseScope(String labelName) {
        Integer boxed = this.labelMap.get(labelName);
        if(boxed != null) {
            return this.cases.get(boxed.intValue());
        }
        return null;
    }

    public int getCasesSize() {
        return this.cases.size();
    }

    /**
     *
     * @param operatorName
     * ex. '*', '=='
     * @param paramSize
     * 1 or 2
     * @return
     * return null, if not found
     */
    public MemberRef.StaticMethodRef findOperator(String operatorName, int paramSize) {
        return this.operatorMap.get(Operator.Kind.toActualName(operatorName, paramSize));
    }



    /**
     * lookup all operator from OperatorTable
     * and register them to operatorMap
     */
    private static Map<String, MemberRef.StaticMethodRef> initOperatorMap(TypeEnv env) {
        Map<String, MemberRef.StaticMethodRef> opMap = new HashMap<>();

        Arrays.stream(OperatorTable.class.getMethods())
                .filter(SymbolTable::isValidOperator)
                .forEach(t -> {
                    String name = t.getDeclaredAnnotation(Operator.class).value().toActualName();
                    MemberRef.StaticMethodRef ref = toMethodRef(env, t);
                    MemberRef.StaticMethodRef found = opMap.get(name);
                    if(found != null) {
                        ref.setNext(found);
                    }
                    opMap.put(name, ref);
                });
        return opMap;
    }

    private static boolean isValidOperator(Method method) {
        Operator anno = method.getDeclaredAnnotation(Operator.class);
        return anno != null
                && anno.value() != Operator.Kind.DUMMY
                && anno.value().getParamSize() == method.getParameterCount()
                && Modifier.isStatic(method.getModifiers())
                && Modifier.isPublic(method.getModifiers());
    }

    private static MemberRef.StaticMethodRef toMethodRef(TypeEnv env, Method method) {
        return new MemberRef.StaticMethodRef(
                Objects.requireNonNull(toType(env, method.getReturnType())),
                method.getName(),
                Arrays.stream(method.getParameterTypes())
                        .map(t -> Objects.requireNonNull(toType(env, t)))
                        .collect(Collectors.toList()),
                MemberRef.operatorHolderType
        );
    }

    /**
     *
     * @param env
     * @param clazz
     * @return
     * return null, if not found corresponding type
     */
    private static LType toType(TypeEnv env, Class<?> clazz) {
        if(clazz.equals(int.class)) {
            return env.getIntType();
        }
        if(clazz.equals(float.class)) {
            return env.getFloatType();
        }
        if(clazz.equals(boolean.class)) {
            return env.getBoolType();
        }
        if(clazz.equals(String.class)) {
            return env.getStringType();
        }
        if(clazz.equals(Object.class)) {
            return env.getAnyType();
        }
        return null;
    }
}
