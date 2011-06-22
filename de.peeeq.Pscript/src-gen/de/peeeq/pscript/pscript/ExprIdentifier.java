/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Expr Identifier</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.ExprIdentifier#getNameVal <em>Name Val</em>}</li>
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
   * Returns the value of the '<em><b>Name Val</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name Val</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name Val</em>' attribute.
   * @see #setNameVal(String)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprIdentifier_NameVal()
   * @model
   * @generated
   */
  String getNameVal();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.ExprIdentifier#getNameVal <em>Name Val</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name Val</em>' attribute.
   * @see #getNameVal()
   * @generated
   */
  void setNameVal(String value);

} // ExprIdentifier
