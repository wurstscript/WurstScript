/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.jassToPscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Bool Literal</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.jassToPscript.BoolLiteral#getBoolVal <em>Bool Val</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.jassToPscript.JassToPscriptPackage#getBoolLiteral()
 * @model
 * @generated
 */
public interface BoolLiteral extends Literal
{
  /**
   * Returns the value of the '<em><b>Bool Val</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Bool Val</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Bool Val</em>' attribute.
   * @see #setBoolVal(String)
   * @see de.peeeq.jassToPscript.JassToPscriptPackage#getBoolLiteral_BoolVal()
   * @model
   * @generated
   */
  String getBoolVal();

  /**
   * Sets the value of the '{@link de.peeeq.jassToPscript.BoolLiteral#getBoolVal <em>Bool Val</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Bool Val</em>' attribute.
   * @see #getBoolVal()
   * @generated
   */
  void setBoolVal(String value);

} // BoolLiteral
