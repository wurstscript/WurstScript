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
 *   <li>{@link de.peeeq.pscript.pscript.impl.NativeTypeImpl#getSuperName <em>Super Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class NativeTypeImpl extends TypeDefImpl implements NativeType
{
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
      case PscriptPackage.NATIVE_TYPE__SUPER_NAME:
        return superName != null;
    }
    return super.eIsSet(featureID);
  }

} //NativeTypeImpl
