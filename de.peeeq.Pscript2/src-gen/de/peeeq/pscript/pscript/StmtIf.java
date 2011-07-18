/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Stmt If</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.StmtIf#getCond <em>Cond</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.StmtIf#getThenBlock <em>Then Block</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.StmtIf#getElseIfConds <em>Else If Conds</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.StmtIf#getElseIfBlocks <em>Else If Blocks</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.StmtIf#getElseBlock <em>Else Block</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtIf()
 * @model
 * @generated
 */
public interface StmtIf extends Statement
{
  /**
   * Returns the value of the '<em><b>Cond</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Cond</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Cond</em>' containment reference.
   * @see #setCond(Expr)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtIf_Cond()
   * @model containment="true"
   * @generated
   */
  Expr getCond();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.StmtIf#getCond <em>Cond</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Cond</em>' containment reference.
   * @see #getCond()
   * @generated
   */
  void setCond(Expr value);

  /**
   * Returns the value of the '<em><b>Then Block</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Then Block</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Then Block</em>' containment reference.
   * @see #setThenBlock(Statements)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtIf_ThenBlock()
   * @model containment="true"
   * @generated
   */
  Statements getThenBlock();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.StmtIf#getThenBlock <em>Then Block</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Then Block</em>' containment reference.
   * @see #getThenBlock()
   * @generated
   */
  void setThenBlock(Statements value);

  /**
   * Returns the value of the '<em><b>Else If Conds</b></em>' containment reference list.
   * The list contents are of type {@link de.peeeq.pscript.pscript.Expr}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Else If Conds</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Else If Conds</em>' containment reference list.
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtIf_ElseIfConds()
   * @model containment="true"
   * @generated
   */
  EList<Expr> getElseIfConds();

  /**
   * Returns the value of the '<em><b>Else If Blocks</b></em>' containment reference list.
   * The list contents are of type {@link de.peeeq.pscript.pscript.Statements}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Else If Blocks</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Else If Blocks</em>' containment reference list.
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtIf_ElseIfBlocks()
   * @model containment="true"
   * @generated
   */
  EList<Statements> getElseIfBlocks();

  /**
   * Returns the value of the '<em><b>Else Block</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Else Block</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Else Block</em>' containment reference.
   * @see #setElseBlock(Statements)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtIf_ElseBlock()
   * @model containment="true"
   * @generated
   */
  Statements getElseBlock();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.StmtIf#getElseBlock <em>Else Block</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Else Block</em>' containment reference.
   * @see #getElseBlock()
   * @generated
   */
  void setElseBlock(Statements value);

} // StmtIf
