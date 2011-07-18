/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Native Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.NativeType#getSuperName <em>Super Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.pscript.pscript.PscriptPackage#getNativeType()
 * @model
 * @generated
 */
public interface NativeType extends TypeDef
{
  /**
   * Returns the value of the '<em><b>Super Name</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Super Name</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Super Name</em>' containment reference.
   * @see #setSuperName(TypeExpr)
   * @see de.peeeq.pscript.pscript.PscriptPackage#getNativeType_SuperName()
   * @model containment="true"
   * @generated
   */
  TypeExpr getSuperName();

  /**
   * Sets the value of the '{@link de.peeeq.pscript.pscript.NativeType#getSuperName <em>Super Name</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Super Name</em>' containment reference.
   * @see #getSuperName()
   * @generated
   */
  void setSuperName(TypeExpr value);

} // NativeType
