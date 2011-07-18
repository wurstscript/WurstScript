/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Stmt Call</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.StmtCall#getE <em>E</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtCall()
 * @model
 * @generated
 */
public interface StmtCall extends StmtSetOrCall
{
  /**
   * Returns the value of the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>E</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>E</em>' containment reference.
   * @see #setE(Expr)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtCall_E()
   * @model containment="true"
   * @generated
   */
  Expr getE();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.StmtCall#getE <em>E</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>E</em>' containment reference.
   * @see #getE()
   * @generated
   */
  void setE(Expr value);

} // StmtCall
