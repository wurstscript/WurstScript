/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.jassToPscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Type Def</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.jassToPscript.TypeDef#getName <em>Name</em>}</li>
 *   <li>{@link de.peeeq.jassToPscript.TypeDef#getExtendName <em>Extend Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.jassToPscript.JassToPscriptPackage#getTypeDef()
 * @model
 * @generated
 */
public interface TypeDef extends Entity
{
  /**
   * Returns the value of the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name</em>' attribute.
   * @see #setName(String)
   * @see de.peeeq.jassToPscript.JassToPscriptPackage#getTypeDef_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link de.peeeq.jassToPscript.TypeDef#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Extend Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Extend Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Extend Name</em>' attribute.
   * @see #setExtendName(String)
   * @see de.peeeq.jassToPscript.JassToPscriptPackage#getTypeDef_ExtendName()
   * @model
   * @generated
   */
  String getExtendName();

  /**
   * Sets the value of the '{@link de.peeeq.jassToPscript.TypeDef#getExtendName <em>Extend Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Extend Name</em>' attribute.
   * @see #getExtendName()
   * @generated
   */
  void setExtendName(String value);

} // TypeDef
