/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Class Def</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.ClassDef#isUnmanaged <em>Unmanaged</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.ClassDef#getMembers <em>Members</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getClassDef()
 * @model
 * @generated
 */
public interface ClassDef extends TypeDef
{
  /**
   * Returns the value of the '<em><b>Unmanaged</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Unmanaged</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Unmanaged</em>' attribute.
   * @see #setUnmanaged(boolean)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getClassDef_Unmanaged()
   * @model
   * @generated
   */
  boolean isUnmanaged();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.ClassDef#isUnmanaged <em>Unmanaged</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Unmanaged</em>' attribute.
   * @see #isUnmanaged()
   * @generated
   */
  void setUnmanaged(boolean value);

  /**
   * Returns the value of the '<em><b>Members</b></em>' containment reference list.
   * The list contents are of type {@link de.peeeq.pscript.pscript.ClassSlots}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Members</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Members</em>' containment reference list.
   * @see de.peeeq.pscript.pscript.PscriptPackage#getClassDef_Members()
   * @model containment="true"
   * @generated
   */
  EList<ClassSlots> getMembers();

} // ClassDef
