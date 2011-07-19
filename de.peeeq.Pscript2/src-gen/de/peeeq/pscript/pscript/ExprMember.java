/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Expr Member</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.ExprMember#getLeft <em>Left</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.ExprMember#getMessage <em>Message</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getExprMember()
 * @model
 * @generated
 */
public interface ExprMember extends Expr
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
   * @see #setLeft(Expr)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprMember_Left()
   * @model containment="true"
   * @generated
   */
  Expr getLeft();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.ExprMember#getLeft <em>Left</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Left</em>' containment reference.
   * @see #getLeft()
   * @generated
   */
  void setLeft(Expr value);

  /**
   * Returns the value of the '<em><b>Message</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Message</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Message</em>' containment reference.
   * @see #setMessage(ExprMemberRight)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprMember_Message()
   * @model containment="true"
   * @generated
   */
  ExprMemberRight getMessage();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.ExprMember#getMessage <em>Message</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Message</em>' containment reference.
   * @see #getMessage()
   * @generated
   */
  void setMessage(ExprMemberRight value);

} // ExprMember
