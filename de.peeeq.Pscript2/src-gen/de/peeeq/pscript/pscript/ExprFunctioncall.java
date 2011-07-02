/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Expr Functioncall</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.ExprFunctioncall#getNameVal <em>Name Val</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.ExprFunctioncall#getParameters <em>Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getExprFunctioncall()
 * @model
 * @generated
 */
public interface ExprFunctioncall extends Expr
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
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprFunctioncall_NameVal()
   * @model
   * @generated
   */
  FuncDef getNameVal();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.ExprFunctioncall#getNameVal <em>Name Val</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name Val</em>' reference.
   * @see #getNameVal()
   * @generated
   */
  void setNameVal(FuncDef value);

  /**
   * Returns the value of the '<em><b>Parameters</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Parameters</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Parameters</em>' containment reference.
   * @see #setParameters(ExprList)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprFunctioncall_Parameters()
   * @model containment="true"
   * @generated
   */
  ExprList getParameters();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.ExprFunctioncall#getParameters <em>Parameters</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Parameters</em>' containment reference.
   * @see #getParameters()
   * @generated
   */
  void setParameters(ExprList value);

} // ExprFunctioncall
