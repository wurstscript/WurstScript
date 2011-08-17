/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Expr New Object</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.ExprNewObject#getClassDef <em>Class Def</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.ExprNewObject#getParams <em>Params</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getExprNewObject()
 * @model
 * @generated
 */
public interface ExprNewObject extends Expr
{
  /**
   * Returns the value of the '<em><b>Class Def</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Class Def</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Class Def</em>' reference.
   * @see #setClassDef(ClassDef)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprNewObject_ClassDef()
   * @model
   * @generated
   */
  ClassDef getClassDef();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.ExprNewObject#getClassDef <em>Class Def</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Class Def</em>' reference.
   * @see #getClassDef()
   * @generated
   */
  void setClassDef(ClassDef value);

  /**
   * Returns the value of the '<em><b>Params</b></em>' containment reference list.
   * The list contents are of type {@link de.peeeq.pscript.pscript.Expr}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Params</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Params</em>' containment reference list.
   * @see de.peeeq.pscript.pscript.PscriptPackage#getExprNewObject_Params()
   * @model containment="true"
   * @generated
   */
  EList<Expr> getParams();

} // ExprNewObject
