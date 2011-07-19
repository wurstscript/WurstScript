/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript.impl;

import de.peeeq.pscript.pscript.Expr;
import de.peeeq.pscript.pscript.ExprFunctioncall;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.PscriptPackage;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Expr Functioncall</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.impl.ExprFunctioncallImpl#getNameVal <em>Name Val</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.impl.ExprFunctioncallImpl#getParams <em>Params</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ExprFunctioncallImpl extends ExprImpl implements ExprFunctioncall
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
   * The cached value of the '{@link #getParams() <em>Params</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getParams()
   * @generated
   * @ordered
   */
  protected EList<Expr> params;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ExprFunctioncallImpl()
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
    return PscriptPackage.Literals.EXPR_FUNCTIONCALL;
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
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, PscriptPackage.EXPR_FUNCTIONCALL__NAME_VAL, oldNameVal, nameVal));
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
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.EXPR_FUNCTIONCALL__NAME_VAL, oldNameVal, nameVal));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<Expr> getParams()
  {
    if (params == null)
    {
      params = new EObjectContainmentEList<Expr>(Expr.class, this, PscriptPackage.EXPR_FUNCTIONCALL__PARAMS);
    }
    return params;
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
      case PscriptPackage.EXPR_FUNCTIONCALL__PARAMS:
        return ((InternalEList<?>)getParams()).basicRemove(otherEnd, msgs);
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
      case PscriptPackage.EXPR_FUNCTIONCALL__NAME_VAL:
        if (resolve) return getNameVal();
        return basicGetNameVal();
      case PscriptPackage.EXPR_FUNCTIONCALL__PARAMS:
        return getParams();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @SuppressWarnings("unchecked")
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case PscriptPackage.EXPR_FUNCTIONCALL__NAME_VAL:
        setNameVal((FuncDef)newValue);
        return;
      case PscriptPackage.EXPR_FUNCTIONCALL__PARAMS:
        getParams().clear();
        getParams().addAll((Collection<? extends Expr>)newValue);
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
      case PscriptPackage.EXPR_FUNCTIONCALL__NAME_VAL:
        setNameVal((FuncDef)null);
        return;
      case PscriptPackage.EXPR_FUNCTIONCALL__PARAMS:
        getParams().clear();
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
      case PscriptPackage.EXPR_FUNCTIONCALL__NAME_VAL:
        return nameVal != null;
      case PscriptPackage.EXPR_FUNCTIONCALL__PARAMS:
        return params != null && !params.isEmpty();
    }
    return super.eIsSet(featureID);
  }

} //ExprFunctioncallImpl
