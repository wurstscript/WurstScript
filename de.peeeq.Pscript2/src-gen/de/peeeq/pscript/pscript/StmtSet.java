/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Stmt Set</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.StmtSet#getLeft <em>Left</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.StmtSet#getOpAssignment <em>Op Assignment</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.StmtSet#getRight <em>Right</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtSet()
 * @model
 * @generated
 */
public interface StmtSet extends StmtSetOrCallOrVarDef
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
   * @see #setLeft(StmtCall)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtSet_Left()
   * @model containment="true"
   * @generated
   */
  StmtCall getLeft();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.StmtSet#getLeft <em>Left</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Left</em>' containment reference.
   * @see #getLeft()
   * @generated
   */
  void setLeft(StmtCall value);

  /**
   * Returns the value of the '<em><b>Op Assignment</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Op Assignment</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Op Assignment</em>' containment reference.
   * @see #setOpAssignment(OpAssignment)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtSet_OpAssignment()
   * @model containment="true"
   * @generated
   */
  OpAssignment getOpAssignment();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.StmtSet#getOpAssignment <em>Op Assignment</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Op Assignment</em>' containment reference.
   * @see #getOpAssignment()
   * @generated
   */
  void setOpAssignment(OpAssignment value);

  /**
   * Returns the value of the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Right</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Right</em>' containment reference.
   * @see #setRight(Expr)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtSet_Right()
   * @model containment="true"
   * @generated
   */
  Expr getRight();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.StmtSet#getRight <em>Right</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Right</em>' containment reference.
   * @see #getRight()
   * @generated
   */
  void setRight(Expr value);

} // StmtSet
