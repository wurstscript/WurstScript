/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Expr Identifier</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.ExprIdentifier#getNameVal <em>Name Val</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.ExprIdentifier#getArrayIndizes <em>Array Indizes</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getExprIdentifier()
 * @model
 * @generated
 */
public interface ExprIdentifier extends Expr
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
   * @see #setNameVal(VarDef)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprIdentifier_NameVal()
   * @model
   * @generated
   */
  VarDef getNameVal();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.ExprIdentifier#getNameVal <em>Name Val</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name Val</em>' reference.
   * @see #getNameVal()
   * @generated
   */
  void setNameVal(VarDef value);

  /**
   * Returns the value of the '<em><b>Array Indizes</b></em>' containment reference list.
   * The list contents are of type {@link de.peeeq.pscript.pscript.Expr}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Array Indizes</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Array Indizes</em>' containment reference list.
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprIdentifier_ArrayIndizes()
   * @model containment="true"
   * @generated
   */
  EList<Expr> getArrayIndizes();

} // ExprIdentifier
