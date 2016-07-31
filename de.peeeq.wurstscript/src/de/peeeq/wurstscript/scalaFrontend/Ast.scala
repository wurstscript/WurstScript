package de.peeeq.wurstscript.scalaFrontend

import _root_.de.peeeq.wurstscript.WurstOperator
import de.peeeq.wurstscript.ast.WStatement

object Ast {

  case class Position(left: Int, right: Int)

  sealed trait AstElement { self: AstElement =>
    var pos: Position

    def withPos(pos: Position): self.type = {
      this.pos = pos
      return this
    }

  }

  case class CompilationUnit(
    jassDecls: List[JassTopLevelDeclaration],
    packages: List[WPackage])
      extends AstElement

  sealed trait TopLevelDeclaration {

  }

  case class Identifier(name: String) extends AstElement

  case class WPackage(
    modifiers: List[Modifier],
    nameId: Identifier,
    imports: List[WImport],
    elements: List[WEntity])
      extends AstElement with TopLevelDeclaration

  sealed trait WEntity extends AstElement {

  }

  sealed trait JassTopLevelDeclaration extends AstElement {

  }

  sealed trait Modifier extends AstElement {

  }

  case class Annotation(annotationType: String) extends Modifier
  case class ModStatic() extends Modifier
  case class ModOverride() extends Modifier
  case class ModAbstract() extends Modifier
  case class ModConstant() extends Modifier
  case class WurstDoc(rawComment: String) extends Modifier

  sealed trait VisibilityModifier extends Modifier {

  }

  case class VisibilityPublic() extends VisibilityModifier
  case class VisibilityPrivate() extends VisibilityModifier
  case class VisibilityPublicread() extends VisibilityModifier
  case class VisibilityProtected() extends VisibilityModifier
  case class VisibilityDefault() extends VisibilityModifier

  case class WImport(
    isPublic: Boolean,
    isInitLater: Boolean,
    packageNameId: Identifier)
      extends AstElement

  case class FuncDef(modifiers: List[Modifier],
                     nameId: Identifier,
                     typeParameters: List[TypeParamDef],
                     parameters: List[WParameter],
                     returnTyp: Option[TypeExpr],
                     body: WStatement)
      extends WEntity
      with JassTopLevelDeclaration

  case class GlobalVarDef(modifiers: List[Modifier],
                          optTyp: Option[TypeExpr],
                          nameId: Identifier,
                          initialExpr: Option[Expr]) extends WEntity

  case class LocalVarDef(modifiers: List[Modifier],
                         optTyp: Option[TypeExpr],
                         nameId: Identifier,
                         initialExpr: Option[Expr])
      extends AstElement
      with WStatement

  case class ExtensionFuncDef(modifiers: List[Modifier],
                              extendedType: TypeExpr,
                              nameId: Identifier,
                              typeParameters: List[TypeParamDef],
                              parameters: List[WParameter],
                              returnType: Option[TypeExpr],
                              body: WStatement)
      extends WEntity

  case class InitBlock(body: WStatement)
    extends WEntity

  case class NativeFunc(modifiers: List[Modifier],
                        nameId: Identifier,
                        parameters: List[WParameter],
                        returnTyp: Option[TypeExpr])
      extends JassTopLevelDeclaration

  case class JassGlobalBlock(globals: List[GlobalVarDef])
    extends JassTopLevelDeclaration

  case class ModuleDef(modifiers: List[Modifier],
                       nameId: Identifier,
                       typeParameters: List[TypeParamDef],
                       innerClasses: List[ClassDef],
                       methods: List[FuncDef],
                       vars: List[GlobalVarDef],
                       constructors: List[ConstructorDef],
                       moduleUses: List[ModuleUse],
                       onDestroy: OnDestroyDef)

  sealed trait TypeExpr extends AstElement

  case class TypeExprSimple(scopeType: Option[TypeExpr],
                            typeName: Identifier,
                            typeArgs: List[TypeExpr])
      extends TypeExpr

  case class TypeExprArray(base: TypeExpr,
                           arraySize: Option[Expr])
      extends TypeExpr

  case class TypeExprThis(scope: Option[TypeExpr])
    extends TypeExpr

  case class WParameter(modifiers: List[Modifier],
                        typ: TypeExpr,
                        nameId: Identifier)

  case class TypeParamDef(modifiers: List[Modifier],
                          nameId: Identifier)
      extends TypeDef

  sealed trait TypeDef extends AstElement

  case class NativeType(modifiers: List[Modifier],
                        nameId: Identifier,
                        optTyp: Option[TypeExpr])
      extends TypeDef
      with JassTopLevelDeclaration
      with WEntity

  case class ClassDef(modifiers: List[Modifier],
                      nameId: Identifier,
                      typeParameters: List[TypeParamDef],
                      extendedClass: Option[TypeExpr],
                      implementsList: List[TypeExpr],
                      innerClasses: List[ClassDef],
                      methods: List[FuncDef],
                      vars: List[GlobalVarDef],
                      constructors: List[ConstructorDef],
                      moduleUses: List[ModuleUse],
                      onDestroy: OnDestroyDef)
      extends TypeDef
      with WEntity

  case class InterfaceDef(modifiers: List[Modifier],
                          nameId: Identifier,
                          typeParameters: List[TypeParamDef],
                          extendsList: List[TypeExpr],
                          methods: List[FuncDef],
                          vars: List[GlobalVarDef],
                          constructors: List[ConstructorDef],
                          moduleUses: List[ModuleUse],
                          onDestroy: OnDestroyDef)
      extends TypeDef

  case class TupleDef(modifiers: List[Modifier],
                      nameId: Identifier,
                      typeParameters: List[TypeParamDef],
                      parameters: List[WParameter])
      extends TypeDef

  case class EnumDef(modifiers: List[Modifier],
                     nameId: Identifier,
                     members: List[EnumMember])
      extends TypeDef

  case class EnumMember(modifers: List[Modifier], nameId: Identifier)
    extends AstElement

  sealed trait ClassSlot {

  }

  case class ConstructorDef(modifers: List[Modifier],
                            parameters: List[WParameter],
                            isExplicit: Boolean,
                            superArgs: List[Expr],
                            body: WStatement)
      extends ClassSlot

  case class OnDestroyDef(body: WStatement)

  case class ModuleUse(moduleNameId: Identifier,
                       typeArgs: List[TypeExpr])

  sealed trait WStatement extends AstElement

  case class EndFunctionStatement() extends WStatement

  case class StmtSkip() extends WStatement

  case class StmtSet(updatedExpr: NameRef, right: Expr)
    extends WStatement

  sealed trait StmtCall extends WStatement

  case class StmtErr()

  case class StmtReturn(returnedObj: Option[Expr]) extends WStatement

  case class StmtExitwhen(cond: Expr) extends WStatement

  case class StmtIf(cond: Expr, thenBlock: WStatement, elseBlock: WStatement)
    extends WStatement

  case class WBlock(body: List[WStatement])
    extends WStatement

  case class StmtWhile(cond: Expr, body: WStatement)
    extends WStatement

  case class StmtLoop(body: WStatement)
    extends WStatement

  case class StmtFor(loopVar: LocalVarDef, in: Expr, iterationMode: IterationMode, body: WStatement)
    extends WStatement

  sealed trait IterationMode {}

  case class ForIn() extends IterationMode

  case class ForFrom() extends IterationMode

  case class StmtForRange(loopVar: LocalVarDef, to: Expr, step: Expr, isUp: Boolean, body: WStatement)
    extends WStatement

  sealed trait Expr extends AstElement

  case class ExprBinary(left: Expr,
                        op: WurstOperator,
                        right: Expr)
      extends Expr

  case class ExprUnary(op: WurstOperator,
                       expr: Expr)
      extends Expr

  case class ExprFunctionCall(funcNameId: Identifier,
                              typeArgs: List[TypeExpr],
                              args: List[Expr])
      extends Expr
      with WStatement

  case class ExprNewObject(typeNameId: Identifier, typeArgs: List[TypeExpr], args: List[Expr])
    extends Expr

  case class ExprCast(typ: TypeExpr, epxr: Expr)
    extends Expr

  case class ExprInstanceOf(typ: TypeExpr, expr: Expr)
    extends Expr

  case class ExprClosure(parameters: List[WParameter], implementation: Expr)
    extends Expr

  case class ExprIncomplete(errorMessage: String)
    extends Expr

  case class ExprStatementsBlock(body: WStatement)
    extends Expr

  case class ExprDestroy(destroyedObj: Expr)
    extends Expr
    with WStatement

  case class ExprArrayLookup(arrayExpr: Expr,
                             indexes: List[Expr])
      extends Expr

  case class ExprIntVal(valIraw: String) extends Expr

  case class ExprRealVal(valR: String) extends Expr
  case class ExprStringVal(valS: String) extends Expr
  case class ExprBoolVal(valB: Boolean) extends Expr
  case class ExprFuncRef(scopeName: String, funcNameId: Identifier) extends Expr
  case class ExprVarAccess(varNameId: Identifier) extends Expr with NameRef
  case class ExprThis() extends Expr
  case class ExprNull() extends Expr
  case class ExprSuper() extends Expr
  case class ExprEmpty() extends Expr

  case class ExprMemberVar(left: Expr,
                           nameId: Identifier,
                           isDotDot: Boolean)
      extends Expr with NameRef

  case class ExprMemberCall(left: Expr,
                            nameId: Identifier,
                            isDotDot: Boolean,
                            typeArgs: List[TypeExpr],
                            args: List[Expr])
      extends Expr

  sealed trait NameRef extends Expr

  sealed trait FunctionCall extends Expr

}

