/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript.impl;

import de.peeeq.pscript.pscript.NativeType;
import de.peeeq.pscript.pscript.PscriptPackage;
import de.peeeq.pscript.pscript.TypeExpr;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Native Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.impl.NativeTypeImpl#getOrigName <em>Orig Name</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.impl.NativeTypeImpl#getSuperName <em>Super Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class NativeTypeImpl extends TypeDefImpl implements NativeType
{
  /**
   * The default value of the '{@link #getOrigName() <em>Orig Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getOrigName()
   * @generated
   * @ordered
   */
  protected static final String ORIG_NAME_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getOrigName() <em>Orig Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getOrigName()
   * @generated
   * @ordered
   */
  protected String origName = ORIG_NAME_EDEFAULT;

  /**
   * The cached value of the '{@link #getSuperName() <em>Super Name</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSuperName()
   * @generated
   * @ordered
   */
  protected TypeExpr superName;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected NativeTypeImpl()
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
    return PscriptPackage.Literals.NATIVE_TYPE;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getOrigName()
  {
    return origName;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setOrigName(String newOrigName)
  {
    String oldOrigName = origName;
    origName = newOrigName;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.NATIVE_TYPE__ORIG_NAME, oldOrigName, origName));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TypeExpr getSuperName()
  {
    return superName;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetSuperName(TypeExpr newSuperName, NotificationChain msgs)
  {
    TypeExpr oldSuperName = superName;
    superName = newSuperName;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PscriptPackage.NATIVE_TYPE__SUPER_NAME, oldSuperName, newSuperName);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSuperName(TypeExpr newSuperName)
  {
    if (newSuperName != superName)
    {
      NotificationChain msgs = null;
      if (superName != null)
        msgs = ((InternalEObject)superName).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.NATIVE_TYPE__SUPER_NAME, null, msgs);
      if (newSuperName != null)
        msgs = ((InternalEObject)newSuperName).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.NATIVE_TYPE__SUPER_NAME, null, msgs);
      msgs = basicSetSuperName(newSuperName, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.NATIVE_TYPE__SUPER_NAME, newSuperName, newSuperName));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
    switch (featureID)
    {
      case PscriptPackage.NATIVE_TYPE__SUPER_NAME:
        return basicSetSuperName(null, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
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
      case PscriptPackage.NATIVE_TYPE__ORIG_NAME:
        return getOrigName();
      case PscriptPackage.NATIVE_TYPE__SUPER_NAME:
        return getSuperName();
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
      case PscriptPackage.NATIVE_TYPE__ORIG_NAME:
        setOrigName((String)newValue);
        return;
      case PscriptPackage.NATIVE_TYPE__SUPER_NAME:
        setSuperName((TypeExpr)newValue);
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
      case PscriptPackage.NATIVE_TYPE__ORIG_NAME:
        setOrigName(ORIG_NAME_EDEFAULT);
        return;
      case PscriptPackage.NATIVE_TYPE__SUPER_NAME:
        setSuperName((TypeExpr)null);
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
      case PscriptPackage.NATIVE_TYPE__ORIG_NAME:
        return ORIG_NAME_EDEFAULT == null ? origName != null : !ORIG_NAME_EDEFAULT.equals(origName);
      case PscriptPackage.NATIVE_TYPE__SUPER_NAME:
        return superName != null;
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
    result.append(" (origName: ");
    result.append(origName);
    result.append(')');
    return result.toString();
  }

} //NativeTypeImpl
