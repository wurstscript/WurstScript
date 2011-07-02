package de.peeeq.pscript.ast;

import katja.common.*;
import java.util.*;
import java.lang.ref.*;
import java.io.Reader;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

public class pscriptAST extends KatjaSpecification {

    //----- attributes of pscriptAST -----

    private static final WeakHashMap<de.peeeq.pscript.ast.ACompilationUnit, WeakReference<de.peeeq.pscript.ast.ACompilationUnitPos>> _Poscache = new WeakHashMap<de.peeeq.pscript.ast.ACompilationUnit, WeakReference<de.peeeq.pscript.ast.ACompilationUnitPos>>();
    public static final pscriptAST$Creator _pscriptAST$Creator = new pscriptAST$Creator();

    //----- methods of pscriptAST -----

    static final KatjaTerm unique(KatjaTerm term) {
        return _termcache.put(term);
    }

    public static final de.peeeq.pscript.ast.AAnd AAnd() {
        return _termcache.put(new de.peeeq.pscript.ast.AAnd.Impl());
    }

    static final de.peeeq.pscript.ast.AAndPos AAndPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AAnd term, final int pos) {
        return new de.peeeq.pscript.ast.AAndPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AArguments AArguments(de.peeeq.pscript.ast.AExpr... elements) {
        return _termcache.put(new de.peeeq.pscript.ast.AArguments.Impl(elements));
    }

    static final de.peeeq.pscript.ast.AArgumentsPos AArgumentsPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AArguments term, final int pos) {
        return new de.peeeq.pscript.ast.AArgumentsPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AAssignment AAssignment(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.AExpr left, de.peeeq.pscript.ast.AExpr right) {
        return _termcache.put(new de.peeeq.pscript.ast.AAssignment.Impl(source, left, right));
    }

    static final de.peeeq.pscript.ast.AAssignmentPos AAssignmentPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AAssignment term, final int pos) {
        return new de.peeeq.pscript.ast.AAssignmentPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.ABlock ABlock(de.peeeq.pscript.ast.AStatement... elements) {
        return _termcache.put(new de.peeeq.pscript.ast.ABlock.Impl(elements));
    }

    static final de.peeeq.pscript.ast.ABlockPos ABlockPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ABlock term, final int pos) {
        return new de.peeeq.pscript.ast.ABlockPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.ABooleanLiteral ABooleanLiteral(org.eclipse.emf.ecore.EObject source, java.lang.Boolean value) {
        return _termcache.put(new de.peeeq.pscript.ast.ABooleanLiteral.Impl(source, value));
    }

    static final de.peeeq.pscript.ast.ABooleanLiteralPos ABooleanLiteralPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ABooleanLiteral term, final int pos) {
        return new de.peeeq.pscript.ast.ABooleanLiteralPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.ABuildinCall ABuildinCall(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.AArguments args) {
        return _termcache.put(new de.peeeq.pscript.ast.ABuildinCall.Impl(source, name, args));
    }

    public static final de.peeeq.pscript.ast.ABuildinCall ABuildinCall(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.AExpr... args) {
        de.peeeq.pscript.ast.AArguments _list = de.peeeq.pscript.ast.pscriptAST.AArguments(args);
        return _termcache.put(new de.peeeq.pscript.ast.ABuildinCall.Impl(source, name, _list));
    }

    static final de.peeeq.pscript.ast.ABuildinCallPos ABuildinCallPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ABuildinCall term, final int pos) {
        return new de.peeeq.pscript.ast.ABuildinCallPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AClassDef AClassDef(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.AClassMembers members) {
        return _termcache.put(new de.peeeq.pscript.ast.AClassDef.Impl(source, name, members));
    }

    public static final de.peeeq.pscript.ast.AClassDef AClassDef(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.AClassMember... members) {
        de.peeeq.pscript.ast.AClassMembers _list = de.peeeq.pscript.ast.pscriptAST.AClassMembers(members);
        return _termcache.put(new de.peeeq.pscript.ast.AClassDef.Impl(source, name, _list));
    }

    static final de.peeeq.pscript.ast.AClassDefPos AClassDefPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AClassDef term, final int pos) {
        return new de.peeeq.pscript.ast.AClassDefPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.AClassMemberPos AClassMemberPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AClassMember term, final int pos) {
        return term.Switch(new de.peeeq.pscript.ast.AClassMember.Switch<de.peeeq.pscript.ast.AClassMemberPos, RuntimeException>() { public final de.peeeq.pscript.ast.AClassMemberPos CaseAVarDef(de.peeeq.pscript.ast.AVarDef term) { return new de.peeeq.pscript.ast.AVarDefPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AClassMemberPos CaseAFuncDef(de.peeeq.pscript.ast.AFuncDef term) { return new de.peeeq.pscript.ast.AFuncDefPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.pscript.ast.AClassMembers AClassMembers(de.peeeq.pscript.ast.AClassMember... elements) {
        return _termcache.put(new de.peeeq.pscript.ast.AClassMembers.Impl(elements));
    }

    static final de.peeeq.pscript.ast.AClassMembersPos AClassMembersPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AClassMembers term, final int pos) {
        return new de.peeeq.pscript.ast.AClassMembersPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.ACompilationUnit ACompilationUnit(de.peeeq.pscript.ast.APackage... elements) {
        return _termcache.put(new de.peeeq.pscript.ast.ACompilationUnit.Impl(elements));
    }

    static final de.peeeq.pscript.ast.ACompilationUnitPos ACompilationUnitPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ACompilationUnit term, final int pos) {
        return new de.peeeq.pscript.ast.ACompilationUnitPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.ACompilationUnitPos ACompilationUnitPos(de.peeeq.pscript.ast.ACompilationUnit term) {
        WeakReference<de.peeeq.pscript.ast.ACompilationUnitPos> _cached = _Poscache.get(term);
        de.peeeq.pscript.ast.ACompilationUnitPos _temp = null;
        if(_cached != null) _temp = _cached.get();
        if(_temp != null) return _temp;
        _temp = new de.peeeq.pscript.ast.ACompilationUnitPos.Impl(term);
        _Poscache.put(term, new WeakReference<de.peeeq.pscript.ast.ACompilationUnitPos>(_temp));
        return _temp;
    }

    public static final de.peeeq.pscript.ast.ADiv ADiv() {
        return _termcache.put(new de.peeeq.pscript.ast.ADiv.Impl());
    }

    public static final de.peeeq.pscript.ast.ADivInt ADivInt() {
        return _termcache.put(new de.peeeq.pscript.ast.ADivInt.Impl());
    }

    static final de.peeeq.pscript.ast.ADivIntPos ADivIntPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ADivInt term, final int pos) {
        return new de.peeeq.pscript.ast.ADivIntPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.ADivPos ADivPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ADiv term, final int pos) {
        return new de.peeeq.pscript.ast.ADivPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.ADot ADot() {
        return _termcache.put(new de.peeeq.pscript.ast.ADot.Impl());
    }

    static final de.peeeq.pscript.ast.ADotPos ADotPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ADot term, final int pos) {
        return new de.peeeq.pscript.ast.ADotPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.AElementPos AElementPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AElement term, final int pos) {
        return term.Switch(new de.peeeq.pscript.ast.AElement.Switch<de.peeeq.pscript.ast.AElementPos, RuntimeException>() { public final de.peeeq.pscript.ast.AElementPos CaseAFuncDef(de.peeeq.pscript.ast.AFuncDef term) { return new de.peeeq.pscript.ast.AFuncDefPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AElementPos CaseAVarDef(de.peeeq.pscript.ast.AVarDef term) { return new de.peeeq.pscript.ast.AVarDefPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AElementPos CaseAInitBlock(de.peeeq.pscript.ast.AInitBlock term) { return new de.peeeq.pscript.ast.AInitBlockPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AElementPos CaseANativeType(de.peeeq.pscript.ast.ANativeType term) { return new de.peeeq.pscript.ast.ANativeTypePos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AElementPos CaseAClassDef(de.peeeq.pscript.ast.AClassDef term) { return new de.peeeq.pscript.ast.AClassDefPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.pscript.ast.AElements AElements(de.peeeq.pscript.ast.AElement... elements) {
        return _termcache.put(new de.peeeq.pscript.ast.AElements.Impl(elements));
    }

    static final de.peeeq.pscript.ast.AElementsPos AElementsPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AElements term, final int pos) {
        return new de.peeeq.pscript.ast.AElementsPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AEqEq AEqEq() {
        return _termcache.put(new de.peeeq.pscript.ast.AEqEq.Impl());
    }

    static final de.peeeq.pscript.ast.AEqEqPos AEqEqPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AEqEq term, final int pos) {
        return new de.peeeq.pscript.ast.AEqEqPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.AExprPos AExprPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AExpr term, final int pos) {
        return term.Switch(new de.peeeq.pscript.ast.AExpr.Switch<de.peeeq.pscript.ast.AExprPos, RuntimeException>() { public final de.peeeq.pscript.ast.AExprPos CaseAPrefix(de.peeeq.pscript.ast.APrefix term) { return new de.peeeq.pscript.ast.APrefixPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AExprPos CaseAInfix(de.peeeq.pscript.ast.AInfix term) { return new de.peeeq.pscript.ast.AInfixPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AExprPos CaseAFunctionCall(de.peeeq.pscript.ast.AFunctionCall term) { return new de.peeeq.pscript.ast.AFunctionCallPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AExprPos CaseABuildinCall(de.peeeq.pscript.ast.ABuildinCall term) { return new de.peeeq.pscript.ast.ABuildinCallPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AExprPos CaseAVariableAccess(de.peeeq.pscript.ast.AVariableAccess term) { return new de.peeeq.pscript.ast.AVariableAccessPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AExprPos CaseAFieldAccess(de.peeeq.pscript.ast.AFieldAccess term) { return new de.peeeq.pscript.ast.AFieldAccessPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AExprPos CaseANoExpr(de.peeeq.pscript.ast.ANoExpr term) { return new de.peeeq.pscript.ast.ANoExprPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AExprPos CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) { return new de.peeeq.pscript.ast.AIntegerLiteralPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AExprPos CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) { return new de.peeeq.pscript.ast.ARealLiteralPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AExprPos CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) { return new de.peeeq.pscript.ast.AStringLiteralPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AExprPos CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) { return new de.peeeq.pscript.ast.ABooleanLiteralPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.pscript.ast.AFieldAccess AFieldAccess(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.AExpr owner, de.peeeq.pscript.ast.AIdentifier ident) {
        return _termcache.put(new de.peeeq.pscript.ast.AFieldAccess.Impl(source, owner, ident));
    }

    static final de.peeeq.pscript.ast.AFieldAccessPos AFieldAccessPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AFieldAccess term, final int pos) {
        return new de.peeeq.pscript.ast.AFieldAccessPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AFormalParameter AFormalParameter(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.ATypeExpr type) {
        return _termcache.put(new de.peeeq.pscript.ast.AFormalParameter.Impl(source, name, type));
    }

    static final de.peeeq.pscript.ast.AFormalParameterPos AFormalParameterPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AFormalParameter term, final int pos) {
        return new de.peeeq.pscript.ast.AFormalParameterPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AFormalParameters AFormalParameters(de.peeeq.pscript.ast.AFormalParameter... elements) {
        return _termcache.put(new de.peeeq.pscript.ast.AFormalParameters.Impl(elements));
    }

    static final de.peeeq.pscript.ast.AFormalParametersPos AFormalParametersPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AFormalParameters term, final int pos) {
        return new de.peeeq.pscript.ast.AFormalParametersPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AFuncDef AFuncDef(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.AReturnType retType, de.peeeq.pscript.ast.AFormalParameters params, de.peeeq.pscript.ast.ABlock body) {
        return _termcache.put(new de.peeeq.pscript.ast.AFuncDef.Impl(source, name, retType, params, body));
    }

    public static final de.peeeq.pscript.ast.AFuncDef AFuncDef(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.AReturnType retType, de.peeeq.pscript.ast.AFormalParameters params, de.peeeq.pscript.ast.AStatement... body) {
        de.peeeq.pscript.ast.ABlock _list = de.peeeq.pscript.ast.pscriptAST.ABlock(body);
        return _termcache.put(new de.peeeq.pscript.ast.AFuncDef.Impl(source, name, retType, params, _list));
    }

    static final de.peeeq.pscript.ast.AFuncDefPos AFuncDefPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AFuncDef term, final int pos) {
        return new de.peeeq.pscript.ast.AFuncDefPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AFunctionCall AFunctionCall(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.AArguments args) {
        return _termcache.put(new de.peeeq.pscript.ast.AFunctionCall.Impl(source, name, args));
    }

    public static final de.peeeq.pscript.ast.AFunctionCall AFunctionCall(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.AExpr... args) {
        de.peeeq.pscript.ast.AArguments _list = de.peeeq.pscript.ast.pscriptAST.AArguments(args);
        return _termcache.put(new de.peeeq.pscript.ast.AFunctionCall.Impl(source, name, _list));
    }

    static final de.peeeq.pscript.ast.AFunctionCallPos AFunctionCallPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AFunctionCall term, final int pos) {
        return new de.peeeq.pscript.ast.AFunctionCallPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AGt AGt() {
        return _termcache.put(new de.peeeq.pscript.ast.AGt.Impl());
    }

    public static final de.peeeq.pscript.ast.AGtEq AGtEq() {
        return _termcache.put(new de.peeeq.pscript.ast.AGtEq.Impl());
    }

    static final de.peeeq.pscript.ast.AGtEqPos AGtEqPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AGtEq term, final int pos) {
        return new de.peeeq.pscript.ast.AGtEqPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.AGtPos AGtPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AGt term, final int pos) {
        return new de.peeeq.pscript.ast.AGtPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AIdentifier AIdentifier(org.eclipse.emf.ecore.EObject source, java.lang.String name) {
        return _termcache.put(new de.peeeq.pscript.ast.AIdentifier.Impl(source, name));
    }

    static final de.peeeq.pscript.ast.AIdentifierPos AIdentifierPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AIdentifier term, final int pos) {
        return new de.peeeq.pscript.ast.AIdentifierPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AIf AIf(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.AExpr cond, de.peeeq.pscript.ast.ABlock thenBlock, de.peeeq.pscript.ast.ABlock elseBlock) {
        return _termcache.put(new de.peeeq.pscript.ast.AIf.Impl(source, cond, thenBlock, elseBlock));
    }

    public static final de.peeeq.pscript.ast.AIf AIf(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.AExpr cond, de.peeeq.pscript.ast.ABlock thenBlock, de.peeeq.pscript.ast.AStatement... elseBlock) {
        de.peeeq.pscript.ast.ABlock _list = de.peeeq.pscript.ast.pscriptAST.ABlock(elseBlock);
        return _termcache.put(new de.peeeq.pscript.ast.AIf.Impl(source, cond, thenBlock, _list));
    }

    static final de.peeeq.pscript.ast.AIfPos AIfPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AIf term, final int pos) {
        return new de.peeeq.pscript.ast.AIfPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AImport AImport(org.eclipse.emf.ecore.EObject source, java.lang.String name) {
        return _termcache.put(new de.peeeq.pscript.ast.AImport.Impl(source, name));
    }

    static final de.peeeq.pscript.ast.AImportPos AImportPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AImport term, final int pos) {
        return new de.peeeq.pscript.ast.AImportPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AImports AImports(de.peeeq.pscript.ast.AImport... elements) {
        return _termcache.put(new de.peeeq.pscript.ast.AImports.Impl(elements));
    }

    static final de.peeeq.pscript.ast.AImportsPos AImportsPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AImports term, final int pos) {
        return new de.peeeq.pscript.ast.AImportsPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AInfix AInfix(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.AExpr left, de.peeeq.pscript.ast.AInfixOp op, de.peeeq.pscript.ast.AExpr right) {
        return _termcache.put(new de.peeeq.pscript.ast.AInfix.Impl(source, left, op, right));
    }

    static final de.peeeq.pscript.ast.AInfixOpPos AInfixOpPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AInfixOp term, final int pos) {
        return term.Switch(new de.peeeq.pscript.ast.AInfixOp.Switch<de.peeeq.pscript.ast.AInfixOpPos, RuntimeException>() { public final de.peeeq.pscript.ast.AInfixOpPos CaseAEqEq(de.peeeq.pscript.ast.AEqEq term) { return new de.peeeq.pscript.ast.AEqEqPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseAGt(de.peeeq.pscript.ast.AGt term) { return new de.peeeq.pscript.ast.AGtPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseAGtEq(de.peeeq.pscript.ast.AGtEq term) { return new de.peeeq.pscript.ast.AGtEqPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseALt(de.peeeq.pscript.ast.ALt term) { return new de.peeeq.pscript.ast.ALtPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseALtEq(de.peeeq.pscript.ast.ALtEq term) { return new de.peeeq.pscript.ast.ALtEqPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseAPlus(de.peeeq.pscript.ast.APlus term) { return new de.peeeq.pscript.ast.APlusPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseAMinus(de.peeeq.pscript.ast.AMinus term) { return new de.peeeq.pscript.ast.AMinusPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseAMult(de.peeeq.pscript.ast.AMult term) { return new de.peeeq.pscript.ast.AMultPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseADiv(de.peeeq.pscript.ast.ADiv term) { return new de.peeeq.pscript.ast.ADivPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseADivInt(de.peeeq.pscript.ast.ADivInt term) { return new de.peeeq.pscript.ast.ADivIntPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseAModulo(de.peeeq.pscript.ast.AModulo term) { return new de.peeeq.pscript.ast.AModuloPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseADot(de.peeeq.pscript.ast.ADot term) { return new de.peeeq.pscript.ast.ADotPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseAAnd(de.peeeq.pscript.ast.AAnd term) { return new de.peeeq.pscript.ast.AAndPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AInfixOpPos CaseAOr(de.peeeq.pscript.ast.AOr term) { return new de.peeeq.pscript.ast.AOrPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.pscript.ast.AInfixPos AInfixPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AInfix term, final int pos) {
        return new de.peeeq.pscript.ast.AInfixPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AInitBlock AInitBlock(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.ABlock body) {
        return _termcache.put(new de.peeeq.pscript.ast.AInitBlock.Impl(source, body));
    }

    public static final de.peeeq.pscript.ast.AInitBlock AInitBlock(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.AStatement... body) {
        de.peeeq.pscript.ast.ABlock _list = de.peeeq.pscript.ast.pscriptAST.ABlock(body);
        return _termcache.put(new de.peeeq.pscript.ast.AInitBlock.Impl(source, _list));
    }

    static final de.peeeq.pscript.ast.AInitBlockPos AInitBlockPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AInitBlock term, final int pos) {
        return new de.peeeq.pscript.ast.AInitBlockPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AIntegerLiteral AIntegerLiteral(org.eclipse.emf.ecore.EObject source, java.lang.Integer value) {
        return _termcache.put(new de.peeeq.pscript.ast.AIntegerLiteral.Impl(source, value));
    }

    static final de.peeeq.pscript.ast.AIntegerLiteralPos AIntegerLiteralPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AIntegerLiteral term, final int pos) {
        return new de.peeeq.pscript.ast.AIntegerLiteralPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.ALiteralPos ALiteralPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ALiteral term, final int pos) {
        return term.Switch(new de.peeeq.pscript.ast.ALiteral.Switch<de.peeeq.pscript.ast.ALiteralPos, RuntimeException>() { public final de.peeeq.pscript.ast.ALiteralPos CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) { return new de.peeeq.pscript.ast.AIntegerLiteralPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.ALiteralPos CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) { return new de.peeeq.pscript.ast.ARealLiteralPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.ALiteralPos CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) { return new de.peeeq.pscript.ast.AStringLiteralPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.ALiteralPos CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) { return new de.peeeq.pscript.ast.ABooleanLiteralPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.pscript.ast.ALt ALt() {
        return _termcache.put(new de.peeeq.pscript.ast.ALt.Impl());
    }

    public static final de.peeeq.pscript.ast.ALtEq ALtEq() {
        return _termcache.put(new de.peeeq.pscript.ast.ALtEq.Impl());
    }

    static final de.peeeq.pscript.ast.ALtEqPos ALtEqPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ALtEq term, final int pos) {
        return new de.peeeq.pscript.ast.ALtEqPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.ALtPos ALtPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ALt term, final int pos) {
        return new de.peeeq.pscript.ast.ALtPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AMinus AMinus() {
        return _termcache.put(new de.peeeq.pscript.ast.AMinus.Impl());
    }

    static final de.peeeq.pscript.ast.AMinusPos AMinusPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AMinus term, final int pos) {
        return new de.peeeq.pscript.ast.AMinusPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AModulo AModulo() {
        return _termcache.put(new de.peeeq.pscript.ast.AModulo.Impl());
    }

    static final de.peeeq.pscript.ast.AModuloPos AModuloPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AModulo term, final int pos) {
        return new de.peeeq.pscript.ast.AModuloPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AMult AMult() {
        return _termcache.put(new de.peeeq.pscript.ast.AMult.Impl());
    }

    static final de.peeeq.pscript.ast.AMultPos AMultPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AMult term, final int pos) {
        return new de.peeeq.pscript.ast.AMultPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.ANativeType ANativeType(org.eclipse.emf.ecore.EObject source, java.lang.String name, java.lang.String origName, java.lang.String superName) {
        return _termcache.put(new de.peeeq.pscript.ast.ANativeType.Impl(source, name, origName, superName));
    }

    static final de.peeeq.pscript.ast.ANativeTypePos ANativeTypePos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ANativeType term, final int pos) {
        return new de.peeeq.pscript.ast.ANativeTypePos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.ANoExpr ANoExpr(org.eclipse.emf.ecore.EObject source) {
        return _termcache.put(new de.peeeq.pscript.ast.ANoExpr.Impl(source));
    }

    static final de.peeeq.pscript.ast.ANoExprPos ANoExprPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ANoExpr term, final int pos) {
        return new de.peeeq.pscript.ast.ANoExprPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.ANot ANot() {
        return _termcache.put(new de.peeeq.pscript.ast.ANot.Impl());
    }

    static final de.peeeq.pscript.ast.ANotPos ANotPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ANot term, final int pos) {
        return new de.peeeq.pscript.ast.ANotPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AOr AOr() {
        return _termcache.put(new de.peeeq.pscript.ast.AOr.Impl());
    }

    static final de.peeeq.pscript.ast.AOrPos AOrPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AOr term, final int pos) {
        return new de.peeeq.pscript.ast.AOrPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.APackage APackage(java.lang.String name, de.peeeq.pscript.ast.AImports imports, de.peeeq.pscript.ast.AElements elements) {
        return _termcache.put(new de.peeeq.pscript.ast.APackage.Impl(name, imports, elements));
    }

    public static final de.peeeq.pscript.ast.APackage APackage(java.lang.String name, de.peeeq.pscript.ast.AImports imports, de.peeeq.pscript.ast.AElement... elements) {
        de.peeeq.pscript.ast.AElements _list = de.peeeq.pscript.ast.pscriptAST.AElements(elements);
        return _termcache.put(new de.peeeq.pscript.ast.APackage.Impl(name, imports, _list));
    }

    static final de.peeeq.pscript.ast.APackagePos APackagePos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.APackage term, final int pos) {
        return new de.peeeq.pscript.ast.APackagePos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.APlus APlus() {
        return _termcache.put(new de.peeeq.pscript.ast.APlus.Impl());
    }

    static final de.peeeq.pscript.ast.APlusPos APlusPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.APlus term, final int pos) {
        return new de.peeeq.pscript.ast.APlusPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.APrefix APrefix(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.APrefixOp op, de.peeeq.pscript.ast.AExpr e) {
        return _termcache.put(new de.peeeq.pscript.ast.APrefix.Impl(source, op, e));
    }

    static final de.peeeq.pscript.ast.APrefixOpPos APrefixOpPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.APrefixOp term, final int pos) {
        return term.Switch(new de.peeeq.pscript.ast.APrefixOp.Switch<de.peeeq.pscript.ast.APrefixOpPos, RuntimeException>() { public final de.peeeq.pscript.ast.APrefixOpPos CaseAPlus(de.peeeq.pscript.ast.APlus term) { return new de.peeeq.pscript.ast.APlusPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.APrefixOpPos CaseAMinus(de.peeeq.pscript.ast.AMinus term) { return new de.peeeq.pscript.ast.AMinusPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.APrefixOpPos CaseANot(de.peeeq.pscript.ast.ANot term) { return new de.peeeq.pscript.ast.ANotPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.pscript.ast.APrefixPos APrefixPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.APrefix term, final int pos) {
        return new de.peeeq.pscript.ast.APrefixPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.ARealLiteral ARealLiteral(org.eclipse.emf.ecore.EObject source, java.math.BigDecimal value) {
        return _termcache.put(new de.peeeq.pscript.ast.ARealLiteral.Impl(source, value));
    }

    static final de.peeeq.pscript.ast.ARealLiteralPos ARealLiteralPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ARealLiteral term, final int pos) {
        return new de.peeeq.pscript.ast.ARealLiteralPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AReturn AReturn(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.AExpr retValue) {
        return _termcache.put(new de.peeeq.pscript.ast.AReturn.Impl(source, retValue));
    }

    static final de.peeeq.pscript.ast.AReturnPos AReturnPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AReturn term, final int pos) {
        return new de.peeeq.pscript.ast.AReturnPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.AReturnTypePos AReturnTypePos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AReturnType term, final int pos) {
        return term.Switch(new de.peeeq.pscript.ast.AReturnType.Switch<de.peeeq.pscript.ast.AReturnTypePos, RuntimeException>() { public final de.peeeq.pscript.ast.AReturnTypePos CaseAReturnsNothing(de.peeeq.pscript.ast.AReturnsNothing term) { return new de.peeeq.pscript.ast.AReturnsNothingPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AReturnTypePos CaseATypeExprSimple(de.peeeq.pscript.ast.ATypeExprSimple term) { return new de.peeeq.pscript.ast.ATypeExprSimplePos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.pscript.ast.AReturnsNothing AReturnsNothing() {
        return _termcache.put(new de.peeeq.pscript.ast.AReturnsNothing.Impl());
    }

    static final de.peeeq.pscript.ast.AReturnsNothingPos AReturnsNothingPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AReturnsNothing term, final int pos) {
        return new de.peeeq.pscript.ast.AReturnsNothingPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.AStatementPos AStatementPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AStatement term, final int pos) {
        return term.Switch(new de.peeeq.pscript.ast.AStatement.Switch<de.peeeq.pscript.ast.AStatementPos, RuntimeException>() { public final de.peeeq.pscript.ast.AStatementPos CaseABlock(de.peeeq.pscript.ast.ABlock term) { return new de.peeeq.pscript.ast.ABlockPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAIf(de.peeeq.pscript.ast.AIf term) { return new de.peeeq.pscript.ast.AIfPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAWhile(de.peeeq.pscript.ast.AWhile term) { return new de.peeeq.pscript.ast.AWhilePos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAReturn(de.peeeq.pscript.ast.AReturn term) { return new de.peeeq.pscript.ast.AReturnPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAVoidReturn(de.peeeq.pscript.ast.AVoidReturn term) { return new de.peeeq.pscript.ast.AVoidReturnPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAAssignment(de.peeeq.pscript.ast.AAssignment term) { return new de.peeeq.pscript.ast.AAssignmentPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAPrefix(de.peeeq.pscript.ast.APrefix term) { return new de.peeeq.pscript.ast.APrefixPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAInfix(de.peeeq.pscript.ast.AInfix term) { return new de.peeeq.pscript.ast.AInfixPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAFunctionCall(de.peeeq.pscript.ast.AFunctionCall term) { return new de.peeeq.pscript.ast.AFunctionCallPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseABuildinCall(de.peeeq.pscript.ast.ABuildinCall term) { return new de.peeeq.pscript.ast.ABuildinCallPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAVariableAccess(de.peeeq.pscript.ast.AVariableAccess term) { return new de.peeeq.pscript.ast.AVariableAccessPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAFieldAccess(de.peeeq.pscript.ast.AFieldAccess term) { return new de.peeeq.pscript.ast.AFieldAccessPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseANoExpr(de.peeeq.pscript.ast.ANoExpr term) { return new de.peeeq.pscript.ast.ANoExprPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAIntegerLiteral(de.peeeq.pscript.ast.AIntegerLiteral term) { return new de.peeeq.pscript.ast.AIntegerLiteralPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseARealLiteral(de.peeeq.pscript.ast.ARealLiteral term) { return new de.peeeq.pscript.ast.ARealLiteralPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseAStringLiteral(de.peeeq.pscript.ast.AStringLiteral term) { return new de.peeeq.pscript.ast.AStringLiteralPos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.AStatementPos CaseABooleanLiteral(de.peeeq.pscript.ast.ABooleanLiteral term) { return new de.peeeq.pscript.ast.ABooleanLiteralPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.pscript.ast.AStringLiteral AStringLiteral(org.eclipse.emf.ecore.EObject source, java.lang.String value) {
        return _termcache.put(new de.peeeq.pscript.ast.AStringLiteral.Impl(source, value));
    }

    static final de.peeeq.pscript.ast.AStringLiteralPos AStringLiteralPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AStringLiteral term, final int pos) {
        return new de.peeeq.pscript.ast.AStringLiteralPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.ATypeDefPos ATypeDefPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ATypeDef term, final int pos) {
        return term.Switch(new de.peeeq.pscript.ast.ATypeDef.Switch<de.peeeq.pscript.ast.ATypeDefPos, RuntimeException>() { public final de.peeeq.pscript.ast.ATypeDefPos CaseANativeType(de.peeeq.pscript.ast.ANativeType term) { return new de.peeeq.pscript.ast.ANativeTypePos.Impl(parent, term, pos); } public final de.peeeq.pscript.ast.ATypeDefPos CaseAClassDef(de.peeeq.pscript.ast.AClassDef term) { return new de.peeeq.pscript.ast.AClassDefPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.pscript.ast.ATypeExprPos ATypeExprPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ATypeExpr term, final int pos) {
        return term.Switch(new de.peeeq.pscript.ast.ATypeExpr.Switch<de.peeeq.pscript.ast.ATypeExprPos, RuntimeException>() { public final de.peeeq.pscript.ast.ATypeExprPos CaseATypeExprSimple(de.peeeq.pscript.ast.ATypeExprSimple term) { return new de.peeeq.pscript.ast.ATypeExprSimplePos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.pscript.ast.ATypeExprSimple ATypeExprSimple(java.lang.String name) {
        return _termcache.put(new de.peeeq.pscript.ast.ATypeExprSimple.Impl(name));
    }

    static final de.peeeq.pscript.ast.ATypeExprSimplePos ATypeExprSimplePos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.ATypeExprSimple term, final int pos) {
        return new de.peeeq.pscript.ast.ATypeExprSimplePos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AVarDef AVarDef(org.eclipse.emf.ecore.EObject source, java.lang.String name, de.peeeq.pscript.ast.ATypeExpr typeExpr, java.lang.Boolean constant, de.peeeq.pscript.ast.AExpr initial) {
        return _termcache.put(new de.peeeq.pscript.ast.AVarDef.Impl(source, name, typeExpr, constant, initial));
    }

    static final de.peeeq.pscript.ast.AVarDefPos AVarDefPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AVarDef term, final int pos) {
        return new de.peeeq.pscript.ast.AVarDefPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AVariableAccess AVariableAccess(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.AIdentifier ident) {
        return _termcache.put(new de.peeeq.pscript.ast.AVariableAccess.Impl(source, ident));
    }

    static final de.peeeq.pscript.ast.AVariableAccessPos AVariableAccessPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AVariableAccess term, final int pos) {
        return new de.peeeq.pscript.ast.AVariableAccessPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AVoidReturn AVoidReturn(org.eclipse.emf.ecore.EObject source) {
        return _termcache.put(new de.peeeq.pscript.ast.AVoidReturn.Impl(source));
    }

    static final de.peeeq.pscript.ast.AVoidReturnPos AVoidReturnPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AVoidReturn term, final int pos) {
        return new de.peeeq.pscript.ast.AVoidReturnPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.pscript.ast.AWhile AWhile(org.eclipse.emf.ecore.EObject source, de.peeeq.pscript.ast.AExpr cond, de.peeeq.pscript.ast.AStatement body) {
        return _termcache.put(new de.peeeq.pscript.ast.AWhile.Impl(source, cond, body));
    }

    static final de.peeeq.pscript.ast.AWhilePos AWhilePos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final de.peeeq.pscript.ast.AWhile term, final int pos) {
        return new de.peeeq.pscript.ast.AWhilePos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.BigDecimalPos BigDecimalPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final java.math.BigDecimal term, final int pos) {
        return new de.peeeq.pscript.ast.BigDecimalPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.BooleanPos BooleanPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final java.lang.Boolean term, final int pos) {
        return new de.peeeq.pscript.ast.BooleanPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.EObjectPos EObjectPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final org.eclipse.emf.ecore.EObject term, final int pos) {
        return new de.peeeq.pscript.ast.EObjectPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.IntegerPos IntegerPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final java.lang.Integer term, final int pos) {
        return new de.peeeq.pscript.ast.IntegerPos.Impl(parent, term, pos);
    }

    static final de.peeeq.pscript.ast.StringPos StringPos(final KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, final java.lang.String term, final int pos) {
        return new de.peeeq.pscript.ast.StringPos.Impl(parent, term, pos);
    }

    public static final KatjaElement fromAssembler(Reader reader) throws IOException {
        return parseInternal(reader, _pscriptAST$Creator);
    }

    //----- nested classes of pscriptAST -----

    public static interface SortPos extends KatjaSortPos<de.peeeq.pscript.ast.ACompilationUnitPos> {

        //----- methods of SortPos -----

        public pscriptAST.SortPos parent();
        public pscriptAST.SortPos lsib();
        public pscriptAST.SortPos rsib();
        public pscriptAST.SortPos preOrder();
        public pscriptAST.SortPos preOrderSkip();
        public pscriptAST.SortPos postOrder();
        public pscriptAST.SortPos postOrderStart();
        public pscriptAST.SortPos follow(List<Integer> path);
    }

    public static interface TermPos<T> extends pscriptAST.SortPos, KatjaTermPos<de.peeeq.pscript.ast.ACompilationUnitPos, T> {
    }

    public static interface NodePos<T extends KatjaTerm> extends pscriptAST.TermPos<T>, KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, T> {
    }

    public static interface LeafPos<T> extends pscriptAST.TermPos<T>, KatjaLeafPos<de.peeeq.pscript.ast.ACompilationUnitPos, T> {
    }

    public static interface TuplePos<T extends KatjaTuple> extends pscriptAST.NodePos<T>, KatjaTuplePos<de.peeeq.pscript.ast.ACompilationUnitPos, T> {
    }

    public static interface ListPos<T extends KatjaList<E>, L extends KatjaSortPos<de.peeeq.pscript.ast.ACompilationUnitPos>, E> extends pscriptAST.NodePos<T>, KatjaListPos<de.peeeq.pscript.ast.ACompilationUnitPos, T, L, E> {
    }

    private static final class pscriptAST$Creator implements KatjaSpecification.ElementCreator {

        //----- attributes of pscriptAST$Creator -----

        private TreeMap<String, Integer> _sortnumber = new TreeMap<String, Integer>();

        //----- methods of pscriptAST$Creator -----

        pscriptAST$Creator() {
            _sortnumber.put("AAnd", 0);
            _sortnumber.put("AAssignment", 1);
            _sortnumber.put("ABooleanLiteral", 2);
            _sortnumber.put("ABuildinCall", 3);
            _sortnumber.put("AClassDef", 4);
            _sortnumber.put("ADiv", 5);
            _sortnumber.put("ADivInt", 6);
            _sortnumber.put("ADot", 7);
            _sortnumber.put("AEqEq", 8);
            _sortnumber.put("AFieldAccess", 9);
            _sortnumber.put("AFormalParameter", 10);
            _sortnumber.put("AFuncDef", 11);
            _sortnumber.put("AFunctionCall", 12);
            _sortnumber.put("AGt", 13);
            _sortnumber.put("AGtEq", 14);
            _sortnumber.put("AIdentifier", 15);
            _sortnumber.put("AIf", 16);
            _sortnumber.put("AImport", 17);
            _sortnumber.put("AInfix", 18);
            _sortnumber.put("AInitBlock", 19);
            _sortnumber.put("AIntegerLiteral", 20);
            _sortnumber.put("ALt", 21);
            _sortnumber.put("ALtEq", 22);
            _sortnumber.put("AMinus", 23);
            _sortnumber.put("AModulo", 24);
            _sortnumber.put("AMult", 25);
            _sortnumber.put("ANativeType", 26);
            _sortnumber.put("ANoExpr", 27);
            _sortnumber.put("ANot", 28);
            _sortnumber.put("AOr", 29);
            _sortnumber.put("APackage", 30);
            _sortnumber.put("APlus", 31);
            _sortnumber.put("APrefix", 32);
            _sortnumber.put("ARealLiteral", 33);
            _sortnumber.put("AReturn", 34);
            _sortnumber.put("AReturnsNothing", 35);
            _sortnumber.put("AStringLiteral", 36);
            _sortnumber.put("ATypeExprSimple", 37);
            _sortnumber.put("AVarDef", 38);
            _sortnumber.put("AVariableAccess", 39);
            _sortnumber.put("AVoidReturn", 40);
            _sortnumber.put("AWhile", 41);
            _sortnumber.put("AArguments", 42);
            _sortnumber.put("ABlock", 43);
            _sortnumber.put("AClassMembers", 44);
            _sortnumber.put("ACompilationUnit", 45);
            _sortnumber.put("AElements", 46);
            _sortnumber.put("AFormalParameters", 47);
            _sortnumber.put("AImports", 48);
            _sortnumber.put("ACompilationUnitPos", 49);
        }

        public final KatjaElement create(String name, Object[] parameters, int line) {
            if(!_sortnumber.containsKey(name))
                throw new IllegalArgumentException("unknown sort name "+name+", in line "+line);

            switch(_sortnumber.get(name)) {
                case 0:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AAnd, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.AAnd();
                case 1:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AAssignment, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AAssignment constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.pscript.ast.AExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AAssignment constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.AExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of AAssignment constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AAssignment((org.eclipse.emf.ecore.EObject) parameters[0], (de.peeeq.pscript.ast.AExpr) parameters[1], (de.peeeq.pscript.ast.AExpr) parameters[2]);
                case 2:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ABooleanLiteral, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ABooleanLiteral constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.Boolean))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ABooleanLiteral constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.ABooleanLiteral((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.Boolean) parameters[1]);
                case 3:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ABuildinCall, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ABuildinCall constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ABuildinCall constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.AArguments))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ABuildinCall constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.ABuildinCall((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.String) parameters[1], (de.peeeq.pscript.ast.AArguments) parameters[2]);
                case 4:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AClassDef, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AClassDef constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AClassDef constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.AClassMembers))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of AClassDef constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AClassDef((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.String) parameters[1], (de.peeeq.pscript.ast.AClassMembers) parameters[2]);
                case 5:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ADiv, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.ADiv();
                case 6:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ADivInt, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.ADivInt();
                case 7:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ADot, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.ADot();
                case 8:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AEqEq, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.AEqEq();
                case 9:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AFieldAccess, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AFieldAccess constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.pscript.ast.AExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AFieldAccess constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.AIdentifier))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of AFieldAccess constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AFieldAccess((org.eclipse.emf.ecore.EObject) parameters[0], (de.peeeq.pscript.ast.AExpr) parameters[1], (de.peeeq.pscript.ast.AIdentifier) parameters[2]);
                case 10:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AFormalParameter, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AFormalParameter constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AFormalParameter constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.ATypeExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of AFormalParameter constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AFormalParameter((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.String) parameters[1], (de.peeeq.pscript.ast.ATypeExpr) parameters[2]);
                case 11:
                    if(parameters.length != 5)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AFuncDef, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AFuncDef constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AFuncDef constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.AReturnType))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of AFuncDef constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.pscript.ast.AFormalParameters))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of AFuncDef constructor, in line "+line);
                    if(!(parameters[4] instanceof de.peeeq.pscript.ast.ABlock))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 5 of AFuncDef constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AFuncDef((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.String) parameters[1], (de.peeeq.pscript.ast.AReturnType) parameters[2], (de.peeeq.pscript.ast.AFormalParameters) parameters[3], (de.peeeq.pscript.ast.ABlock) parameters[4]);
                case 12:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AFunctionCall, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AFunctionCall constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AFunctionCall constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.AArguments))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of AFunctionCall constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AFunctionCall((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.String) parameters[1], (de.peeeq.pscript.ast.AArguments) parameters[2]);
                case 13:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AGt, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.AGt();
                case 14:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AGtEq, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.AGtEq();
                case 15:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AIdentifier, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AIdentifier constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AIdentifier constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AIdentifier((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.String) parameters[1]);
                case 16:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AIf, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AIf constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.pscript.ast.AExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AIf constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.ABlock))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of AIf constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.pscript.ast.ABlock))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of AIf constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AIf((org.eclipse.emf.ecore.EObject) parameters[0], (de.peeeq.pscript.ast.AExpr) parameters[1], (de.peeeq.pscript.ast.ABlock) parameters[2], (de.peeeq.pscript.ast.ABlock) parameters[3]);
                case 17:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AImport, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AImport constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AImport constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AImport((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.String) parameters[1]);
                case 18:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AInfix, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AInfix constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.pscript.ast.AExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AInfix constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.AInfixOp))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of AInfix constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.pscript.ast.AExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of AInfix constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AInfix((org.eclipse.emf.ecore.EObject) parameters[0], (de.peeeq.pscript.ast.AExpr) parameters[1], (de.peeeq.pscript.ast.AInfixOp) parameters[2], (de.peeeq.pscript.ast.AExpr) parameters[3]);
                case 19:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AInitBlock, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AInitBlock constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.pscript.ast.ABlock))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AInitBlock constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AInitBlock((org.eclipse.emf.ecore.EObject) parameters[0], (de.peeeq.pscript.ast.ABlock) parameters[1]);
                case 20:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AIntegerLiteral, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AIntegerLiteral constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.Integer))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AIntegerLiteral constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AIntegerLiteral((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.Integer) parameters[1]);
                case 21:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ALt, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.ALt();
                case 22:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ALtEq, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.ALtEq();
                case 23:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AMinus, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.AMinus();
                case 24:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AModulo, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.AModulo();
                case 25:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AMult, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.AMult();
                case 26:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ANativeType, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ANativeType constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ANativeType constructor, in line "+line);
                    if(!(parameters[2] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ANativeType constructor, in line "+line);
                    if(!(parameters[3] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of ANativeType constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.ANativeType((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.String) parameters[1], (java.lang.String) parameters[2], (java.lang.String) parameters[3]);
                case 27:
                    if(parameters.length != 1)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ANoExpr, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ANoExpr constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.ANoExpr((org.eclipse.emf.ecore.EObject) parameters[0]);
                case 28:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ANot, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.ANot();
                case 29:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AOr, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.AOr();
                case 30:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort APackage, in line "+line);

                    if(!(parameters[0] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of APackage constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.pscript.ast.AImports))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of APackage constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.AElements))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of APackage constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.APackage((java.lang.String) parameters[0], (de.peeeq.pscript.ast.AImports) parameters[1], (de.peeeq.pscript.ast.AElements) parameters[2]);
                case 31:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort APlus, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.APlus();
                case 32:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort APrefix, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of APrefix constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.pscript.ast.APrefixOp))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of APrefix constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.AExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of APrefix constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.APrefix((org.eclipse.emf.ecore.EObject) parameters[0], (de.peeeq.pscript.ast.APrefixOp) parameters[1], (de.peeeq.pscript.ast.AExpr) parameters[2]);
                case 33:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ARealLiteral, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ARealLiteral constructor, in line "+line);
                    if(!(parameters[1] instanceof java.math.BigDecimal))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ARealLiteral constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.ARealLiteral((org.eclipse.emf.ecore.EObject) parameters[0], (java.math.BigDecimal) parameters[1]);
                case 34:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AReturn, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AReturn constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.pscript.ast.AExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AReturn constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AReturn((org.eclipse.emf.ecore.EObject) parameters[0], (de.peeeq.pscript.ast.AExpr) parameters[1]);
                case 35:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AReturnsNothing, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.AReturnsNothing();
                case 36:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AStringLiteral, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AStringLiteral constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AStringLiteral constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AStringLiteral((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.String) parameters[1]);
                case 37:
                    if(parameters.length != 1)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ATypeExprSimple, in line "+line);

                    if(!(parameters[0] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ATypeExprSimple constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.ATypeExprSimple((java.lang.String) parameters[0]);
                case 38:
                    if(parameters.length != 5)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AVarDef, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AVarDef constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AVarDef constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.ATypeExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of AVarDef constructor, in line "+line);
                    if(!(parameters[3] instanceof java.lang.Boolean))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of AVarDef constructor, in line "+line);
                    if(!(parameters[4] instanceof de.peeeq.pscript.ast.AExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 5 of AVarDef constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AVarDef((org.eclipse.emf.ecore.EObject) parameters[0], (java.lang.String) parameters[1], (de.peeeq.pscript.ast.ATypeExpr) parameters[2], (java.lang.Boolean) parameters[3], (de.peeeq.pscript.ast.AExpr) parameters[4]);
                case 39:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AVariableAccess, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AVariableAccess constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.pscript.ast.AIdentifier))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AVariableAccess constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AVariableAccess((org.eclipse.emf.ecore.EObject) parameters[0], (de.peeeq.pscript.ast.AIdentifier) parameters[1]);
                case 40:
                    if(parameters.length != 1)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AVoidReturn, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AVoidReturn constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AVoidReturn((org.eclipse.emf.ecore.EObject) parameters[0]);
                case 41:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort AWhile, in line "+line);

                    if(!(parameters[0] instanceof org.eclipse.emf.ecore.EObject))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of AWhile constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.pscript.ast.AExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of AWhile constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.pscript.ast.AStatement))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of AWhile constructor, in line "+line);
                    return de.peeeq.pscript.ast.pscriptAST.AWhile((org.eclipse.emf.ecore.EObject) parameters[0], (de.peeeq.pscript.ast.AExpr) parameters[1], (de.peeeq.pscript.ast.AStatement) parameters[2]);
                case 42:
                    de.peeeq.pscript.ast.AExpr[] _AArguments$array = new de.peeeq.pscript.ast.AExpr[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.pscript.ast.AExpr))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for AArguments constructor, in line "+line);
                        _AArguments$array[i] = (de.peeeq.pscript.ast.AExpr) parameters[i];
                    }

                    return de.peeeq.pscript.ast.pscriptAST.AArguments(_AArguments$array);
                case 43:
                    de.peeeq.pscript.ast.AStatement[] _ABlock$array = new de.peeeq.pscript.ast.AStatement[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.pscript.ast.AStatement))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for ABlock constructor, in line "+line);
                        _ABlock$array[i] = (de.peeeq.pscript.ast.AStatement) parameters[i];
                    }

                    return de.peeeq.pscript.ast.pscriptAST.ABlock(_ABlock$array);
                case 44:
                    de.peeeq.pscript.ast.AClassMember[] _AClassMembers$array = new de.peeeq.pscript.ast.AClassMember[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.pscript.ast.AClassMember))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for AClassMembers constructor, in line "+line);
                        _AClassMembers$array[i] = (de.peeeq.pscript.ast.AClassMember) parameters[i];
                    }

                    return de.peeeq.pscript.ast.pscriptAST.AClassMembers(_AClassMembers$array);
                case 45:
                    de.peeeq.pscript.ast.APackage[] _ACompilationUnit$array = new de.peeeq.pscript.ast.APackage[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.pscript.ast.APackage))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for ACompilationUnit constructor, in line "+line);
                        _ACompilationUnit$array[i] = (de.peeeq.pscript.ast.APackage) parameters[i];
                    }

                    return de.peeeq.pscript.ast.pscriptAST.ACompilationUnit(_ACompilationUnit$array);
                case 46:
                    de.peeeq.pscript.ast.AElement[] _AElements$array = new de.peeeq.pscript.ast.AElement[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.pscript.ast.AElement))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for AElements constructor, in line "+line);
                        _AElements$array[i] = (de.peeeq.pscript.ast.AElement) parameters[i];
                    }

                    return de.peeeq.pscript.ast.pscriptAST.AElements(_AElements$array);
                case 47:
                    de.peeeq.pscript.ast.AFormalParameter[] _AFormalParameters$array = new de.peeeq.pscript.ast.AFormalParameter[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.pscript.ast.AFormalParameter))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for AFormalParameters constructor, in line "+line);
                        _AFormalParameters$array[i] = (de.peeeq.pscript.ast.AFormalParameter) parameters[i];
                    }

                    return de.peeeq.pscript.ast.pscriptAST.AFormalParameters(_AFormalParameters$array);
                case 48:
                    de.peeeq.pscript.ast.AImport[] _AImports$array = new de.peeeq.pscript.ast.AImport[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.pscript.ast.AImport))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for AImports constructor, in line "+line);
                        _AImports$array[i] = (de.peeeq.pscript.ast.AImport) parameters[i];
                    }

                    return de.peeeq.pscript.ast.pscriptAST.AImports(_AImports$array);
                case 49:
                    if(parameters.length != 1)
                        throw new IllegalArgumentException("wrong number of arguments given root position ACompilationUnitPos, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.pscript.ast.ACompilationUnit))
                        throw new IllegalArgumentException("wrong sort on stack as parameter for ACompilationUnitPos constructor, in line "+line);

                    return de.peeeq.pscript.ast.pscriptAST.ACompilationUnitPos((de.peeeq.pscript.ast.ACompilationUnit) parameters[0]);
                default:
                    throw new IllegalStateException("impossible state");
            }
        }
    }
}

