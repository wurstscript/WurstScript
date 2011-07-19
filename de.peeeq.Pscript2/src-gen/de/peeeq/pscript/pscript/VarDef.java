/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Var Def</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.VarDef#isConstant <em>Constant</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.VarDef#getType <em>Type</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.VarDef#getE <em>E</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getVarDef()
 * @model
 * @generated
 */
public interface VarDef extends Entity, ClassMember, Statement, StmtSetOrCallOrVarDef
{
  /**
   * Returns the value of the '<em><b>Constant</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Constant</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Constant</em>' attribute.
   * @see #setConstant(boolean)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getVarDef_Constant()
   * @model
   * @generated
   */
  boolean isConstant();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.VarDef#isConstant <em>Constant</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Constant</em>' attribute.
   * @see #isConstant()
   * @generated
   */
  void setConstant(boolean value);

  /**
   * Returns the value of the '<em><b>Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Type</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Type</em>' containment reference.
   * @see #setType(TypeExpr)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getVarDef_Type()
   * @model containment="true"
   * @generated
   */
  TypeExpr getType();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.VarDef#getType <em>Type</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Type</em>' containment reference.
   * @see #getType()
   * @generated
   */
  void setType(TypeExpr value);

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
   * @see de.peeeq.pscript.pscript.PscriptPackage#getVarDef_E()
   * @model containment="true"
   * @generated
   */
  Expr getE();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.VarDef#getE <em>E</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>E</em>' containment reference.
   * @see #getE()
   * @generated
   */
  void setE(Expr value);

} // VarDef
