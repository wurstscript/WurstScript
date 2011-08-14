/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Type Expr</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.TypeExpr#getName <em>Name</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.TypeExpr#isArray <em>Array</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.TypeExpr#getSizes <em>Sizes</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getTypeExpr()
 * @model
 * @generated
 */
public interface TypeExpr extends EObject
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
   * @see de.peeeq.pscript.pscript.PscriptPackage#getTypeExpr_Name()
   * @model
   * @generated
   */
  TypeDef getName();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.TypeExpr#getName <em>Name</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' reference.
   * @see #getName()
   * @generated
   */
  void setName(TypeDef value);

  /**
   * Returns the value of the '<em><b>Array</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Array</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Array</em>' attribute.
   * @see #setArray(boolean)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getTypeExpr_Array()
   * @model
   * @generated
   */
  boolean isArray();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.TypeExpr#isArray <em>Array</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Array</em>' attribute.
   * @see #isArray()
   * @generated
   */
  void setArray(boolean value);

  /**
   * Returns the value of the '<em><b>Sizes</b></em>' attribute list.
   * The list contents are of type {@link java.lang.Integer}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Sizes</em>' attribute list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Sizes</em>' attribute list.
   * @see de.peeeq.pscript.pscript.PscriptPackage#getTypeExpr_Sizes()
   * @model unique="false"
   * @generated
   */
  EList<Integer> getSizes();

} // TypeExpr
