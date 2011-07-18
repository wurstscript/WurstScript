/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Type Expr Ref</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.TypeExprRef#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getTypeExprRef()
 * @model
 * @generated
 */
public interface TypeExprRef extends TypeExpr
{
  /**
   * Returns the value of the '<em><b>Name</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name</em>' reference.
   * @see #setName(TypeDef)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getTypeExprRef_Name()
   * @model
   * @generated
   */
  TypeDef getName();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.TypeExprRef#getName <em>Name</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' reference.
   * @see #getName()
   * @generated
   */
  void setName(TypeDef value);

} // TypeExprRef
