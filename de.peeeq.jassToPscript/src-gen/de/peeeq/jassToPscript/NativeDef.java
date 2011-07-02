/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.jassToPscript;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Native Def</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.peeeq.jassToPscript.NativeDef#getName <em>Name</em>}</li>
 *   <li>{@link de.peeeq.jassToPscript.NativeDef#getParams <em>Params</em>}</li>
 *   <li>{@link de.peeeq.jassToPscript.NativeDef#getReturnType <em>Return Type</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.peeeq.jassToPscript.JassToPscriptPackage#getNativeDef()
 * @model
 * @generated
 */
public interface NativeDef extends Entity
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
   * @see de.peeeq.jassToPscript.JassToPscriptPackage#getNativeDef_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link de.peeeq.jassToPscript.NativeDef#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Params</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Params</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Params</em>' containment reference.
   * @see #setParams(FormalParameters)
   * @see de.peeeq.jassToPscript.JassToPscriptPackage#getNativeDef_Params()
   * @model containment="true"
   * @generated
   */
  FormalParameters getParams();

  /**
   * Sets the value of the '{@link de.peeeq.jassToPscript.NativeDef#getParams <em>Params</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Params</em>' containment reference.
   * @see #getParams()
   * @generated
   */
  void setParams(FormalParameters value);

  /**
   * Returns the value of the '<em><b>Return Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Return Type</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Return Type</em>' containment reference.
   * @see #setReturnType(ReturnType)
   * @see de.peeeq.jassToPscript.JassToPscriptPackage#getNativeDef_ReturnType()
   * @model containment="true"
   * @generated
   */
  ReturnType getReturnType();

  /**
   * Sets the value of the '{@link de.peeeq.jassToPscript.NativeDef#getReturnType <em>Return Type</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Return Type</em>' containment reference.
   * @see #getReturnType()
   * @generated
   */
  void setReturnType(ReturnType value);

} // NativeDef
