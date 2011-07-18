/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript.impl;

import de.peeeq.pscript.pscript.ExprFuncRef;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.PscriptPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Expr Func Ref</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.impl.ExprFuncRefImpl#getNameVal <em>Name Val</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ExprFuncRefImpl extends ExprImpl implements ExprFuncRef
{
  /**
   * The cached value of the '{@link #getNameVal() <em>Name Val</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getNameVal()
   * @generated
   * @ordered
   */
  protected FuncDef nameVal;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ExprFuncRefImpl()
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
    return PscriptPackage.Literals.EXPR_FUNC_REF;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FuncDef getNameVal()
  {
    if (nameVal != null && nameVal.eIsProxy())
    {
      InternalEObject oldNameVal = (InternalEObject)nameVal;
      nameVal = (FuncDef)eResolveProxy(oldNameVal);
      if (nameVal != oldNameVal)
      {
        if (eNotificationRequired())
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, PscriptPackage.EXPR_FUNC_REF__NAME_VAL, oldNameVal, nameVal));
      }
    }
    return nameVal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FuncDef basicGetNameVal()
  {
    return nameVal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setNameVal(FuncDef newNameVal)
  {
    FuncDef oldNameVal = nameVal;
    nameVal = newNameVal;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.EXPR_FUNC_REF__NAME_VAL, oldNameVal, nameVal));
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
      case PscriptPackage.EXPR_FUNC_REF__NAME_VAL:
        if (resolve) return getNameVal();
        return basicGetNameVal();
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
      case PscriptPackage.EXPR_FUNC_REF__NAME_VAL:
        setNameVal((FuncDef)newValue);
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
      case PscriptPackage.EXPR_FUNC_REF__NAME_VAL:
        setNameVal((FuncDef)null);
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
      case PscriptPackage.EXPR_FUNC_REF__NAME_VAL:
        return nameVal != null;
    }
    return super.eIsSet(featureID);
  }

} //ExprFuncRefImpl
