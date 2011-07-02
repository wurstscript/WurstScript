/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.jassToPscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Mult</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.jassToPscript.Mult#getLeft <em>Left</em>}</li>
 *   <li>{@link de.peeeq.jassToPscript.Mult#getRight <em>Right</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.jassToPscript.JassToPscriptPackage#getMult()
 * @model
 * @generated
 */
public interface Mult extends Expr
{
  /**
   * Returns the value of the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Left</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Left</em>' containment reference.
   * @see #setLeft(Literal)
   * @see de.peeeq.jassToPscript.JassToPscriptPackage#getMult_Left()
   * @model containment="true"
   * @generated
   */
  Literal getLeft();

  /**
   * Sets the value of the '{@link de.peeeq.jassToPscript.Mult#getLeft <em>Left</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Left</em>' containment reference.
   * @see #getLeft()
   * @generated
   */
  void setLeft(Literal value);

  /**
   * Returns the value of the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Right</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Right</em>' containment reference.
   * @see #setRight(Literal)
   * @see de.peeeq.jassToPscript.JassToPscriptPackage#getMult_Right()
   * @model containment="true"
   * @generated
   */
  Literal getRight();

  /**
   * Sets the value of the '{@link de.peeeq.jassToPscript.Mult#getRight <em>Right</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Right</em>' containment reference.
   * @see #getRight()
   * @generated
   */
  void setRight(Literal value);

} // Mult
