/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Stmt Change Ref Count</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.StmtChangeRefCount#isIncrease <em>Increase</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.StmtChangeRefCount#isDecrease <em>Decrease</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.StmtChangeRefCount#getObj <em>Obj</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtChangeRefCount()
 * @model
 * @generated
 */
public interface StmtChangeRefCount extends Statement
{
  /**
   * Returns the value of the '<em><b>Increase</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Increase</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Increase</em>' attribute.
   * @see #setIncrease(boolean)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtChangeRefCount_Increase()
   * @model
   * @generated
   */
  boolean isIncrease();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.StmtChangeRefCount#isIncrease <em>Increase</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Increase</em>' attribute.
   * @see #isIncrease()
   * @generated
   */
  void setIncrease(boolean value);

  /**
   * Returns the value of the '<em><b>Decrease</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Decrease</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Decrease</em>' attribute.
   * @see #setDecrease(boolean)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtChangeRefCount_Decrease()
   * @model
   * @generated
   */
  boolean isDecrease();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.StmtChangeRefCount#isDecrease <em>Decrease</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Decrease</em>' attribute.
   * @see #isDecrease()
   * @generated
   */
  void setDecrease(boolean value);

  /**
   * Returns the value of the '<em><b>Obj</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Obj</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Obj</em>' containment reference.
   * @see #setObj(Expr)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getStmtChangeRefCount_Obj()
   * @model containment="true"
   * @generated
   */
  Expr getObj();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.StmtChangeRefCount#getObj <em>Obj</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Obj</em>' containment reference.
   * @see #getObj()
   * @generated
   */
  void setObj(Expr value);

} // StmtChangeRefCount
