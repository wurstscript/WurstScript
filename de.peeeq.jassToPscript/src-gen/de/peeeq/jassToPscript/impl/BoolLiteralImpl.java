/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.jassToPscript.impl;

import de.peeeq.jassToPscript.BoolLiteral;
import de.peeeq.jassToPscript.JassToPscriptPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Bool Literal</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.peeeq.jassToPscript.impl.BoolLiteralImpl#getBoolVal <em>Bool Val</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BoolLiteralImpl extends LiteralImpl implements BoolLiteral
{
  /**
   * The default value of the '{@link #getBoolVal() <em>Bool Val</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getBoolVal()
   * @generated
   * @ordered
   */
  protected static final String BOOL_VAL_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getBoolVal() <em>Bool Val</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getBoolVal()
   * @generated
   * @ordered
   */
  protected String boolVal = BOOL_VAL_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected BoolLiteralImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return JassToPscriptPackage.Literals.BOOL_LITERAL;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getBoolVal()
  {
    return boolVal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setBoolVal(String newBoolVal)
  {
    String oldBoolVal = boolVal;
    boolVal = newBoolVal;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, JassToPscriptPackage.BOOL_LITERAL__BOOL_VAL, oldBoolVal, boolVal));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case JassToPscriptPackage.BOOL_LITERAL__BOOL_VAL:
        return getBoolVal();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case JassToPscriptPackage.BOOL_LITERAL__BOOL_VAL:
        setBoolVal((String)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case JassToPscriptPackage.BOOL_LITERAL__BOOL_VAL:
        setBoolVal(BOOL_VAL_EDEFAULT);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case JassToPscriptPackage.BOOL_LITERAL__BOOL_VAL:
        return BOOL_VAL_EDEFAULT == null ? boolVal != null : !BOOL_VAL_EDEFAULT.equals(boolVal);
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    if (eIsProxy()) return super.toString();

    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (boolVal: ");
    result.append(boolVal);
    result.append(')');
    return result.toString();
  }

} //BoolLiteralImpl
