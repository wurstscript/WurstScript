/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Expr Func Ref</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.ExprFuncRef#getNameVal <em>Name Val</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getExprFuncRef()
 * @model
 * @generated
 */
public interface ExprFuncRef extends Expr
{
  /**
   * Returns the value of the '<em><b>Name Val</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name Val</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name Val</em>' reference.
   * @see #setNameVal(FuncDef)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprFuncRef_NameVal()
   * @model
   * @generated
   */
  FuncDef getNameVal();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.ExprFuncRef#getNameVal <em>Name Val</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name Val</em>' reference.
   * @see #getNameVal()
   * @generated
   */
  void setNameVal(FuncDef value);

} // ExprFuncRef
