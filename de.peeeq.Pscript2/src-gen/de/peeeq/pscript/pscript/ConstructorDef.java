/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Constructor Def</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.ConstructorDef#getParameters <em>Parameters</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.ConstructorDef#getBody <em>Body</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getConstructorDef()
 * @model
 * @generated
 */
public interface ConstructorDef extends ClassSlots
{
  /**
   * Returns the value of the '<em><b>Parameters</b></em>' containment reference list.
   * The list contents are of type {@link de.peeeq.pscript.pscript.VarDef}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Parameters</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Parameters</em>' containment reference list.
   * @see de.peeeq.pscript.pscript.PscriptPackage#getConstructorDef_Parameters()
   * @model containment="true"
   * @generated
   */
  EList<VarDef> getParameters();

  /**
   * Returns the value of the '<em><b>Body</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Body</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Body</em>' containment reference.
   * @see #setBody(Statements)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getConstructorDef_Body()
   * @model containment="true"
   * @generated
   */
  Statements getBody();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.ConstructorDef#getBody <em>Body</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Body</em>' containment reference.
   * @see #getBody()
   * @generated
   */
  void setBody(Statements value);

} // ConstructorDef
