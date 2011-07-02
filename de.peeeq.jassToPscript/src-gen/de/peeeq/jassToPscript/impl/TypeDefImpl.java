/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.jassToPscript.impl;

import de.peeeq.jassToPscript.JassToPscriptPackage;
import de.peeeq.jassToPscript.TypeDef;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Type Def</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.peeeq.jassToPscript.impl.TypeDefImpl#getName <em>Name</em>}</li>
 *   <li>{@link de.peeeq.jassToPscript.impl.TypeDefImpl#getExtendName <em>Extend Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TypeDefImpl extends EntityImpl implements TypeDef
{
  /**
   * The default value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected static final String NAME_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected String name = NAME_EDEFAULT;

  /**
   * The default value of the '{@link #getExtendName() <em>Extend Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getExtendName()
   * @generated
   * @ordered
   */
  protected static final String EXTEND_NAME_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getExtendName() <em>Extend Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getExtendName()
   * @generated
   * @ordered
   */
  protected String extendName = EXTEND_NAME_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected TypeDefImpl()
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
    return JassToPscriptPackage.Literals.TYPE_DEF;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getName()
  {
    return name;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setName(String newName)
  {
    String oldName = name;
    name = newName;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, JassToPscriptPackage.TYPE_DEF__NAME, oldName, name));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getExtendName()
  {
    return extendName;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setExtendName(String newExtendName)
  {
    String oldExtendName = extendName;
    extendName = newExtendName;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, JassToPscriptPackage.TYPE_DEF__EXTEND_NAME, oldExtendName, extendName));
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
      case JassToPscriptPackage.TYPE_DEF__NAME:
        return getName();
      case JassToPscriptPackage.TYPE_DEF__EXTEND_NAME:
        return getExtendName();
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
      case JassToPscriptPackage.TYPE_DEF__NAME:
        setName((String)newValue);
        return;
      case JassToPscriptPackage.TYPE_DEF__EXTEND_NAME:
        setExtendName((String)newValue);
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
      case JassToPscriptPackage.TYPE_DEF__NAME:
        setName(NAME_EDEFAULT);
        return;
      case JassToPscriptPackage.TYPE_DEF__EXTEND_NAME:
        setExtendName(EXTEND_NAME_EDEFAULT);
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
      case JassToPscriptPackage.TYPE_DEF__NAME:
        return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
      case JassToPscriptPackage.TYPE_DEF__EXTEND_NAME:
        return EXTEND_NAME_EDEFAULT == null ? extendName != null : !EXTEND_NAME_EDEFAULT.equals(extendName);
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
    result.append(" (name: ");
    result.append(name);
    result.append(", extendName: ");
    result.append(extendName);
    result.append(')');
    return result.toString();
  }

} //TypeDefImpl
