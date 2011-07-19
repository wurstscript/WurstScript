/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Expr Member Right</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.ExprMemberRight#getNameVal <em>Name Val</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.ExprMemberRight#getParams <em>Params</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getExprMemberRight()
 * @model
 * @generated
 */
public interface ExprMemberRight extends EObject
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
   * @see #setNameVal(ClassMember)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprMemberRight_NameVal()
   * @model
   * @generated
   */
  ClassMember getNameVal();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.ExprMemberRight#getNameVal <em>Name Val</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name Val</em>' reference.
   * @see #getNameVal()
   * @generated
   */
  void setNameVal(ClassMember value);

  /**
   * Returns the value of the '<em><b>Params</b></em>' containment reference list.
   * The list contents are of type {@link de.peeeq.pscript.pscript.Expr}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Params</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Params</em>' containment reference list.
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprMemberRight_Params()
   * @model containment="true"
   * @generated
   */
  EList<Expr> getParams();

} // ExprMemberRight
