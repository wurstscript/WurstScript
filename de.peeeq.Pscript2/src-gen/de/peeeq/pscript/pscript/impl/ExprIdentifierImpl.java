/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript.impl;

import de.peeeq.pscript.pscript.Expr;
import de.peeeq.pscript.pscript.ExprIdentifier;
import de.peeeq.pscript.pscript.PscriptPackage;
import de.peeeq.pscript.pscript.VarDef;

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
 * An implementation of the model object '<em><b>Expr Identifier</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.impl.ExprIdentifierImpl#getNameVal <em>Name Val</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.impl.ExprIdentifierImpl#getArrayIndizes <em>Array Indizes</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ExprIdentifierImpl extends ExprImpl implements ExprIdentifier
{
  /**
   * The cached value of the '{@link #getNameVal() <em>Name Val</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getNameVal()
   * @generated
   * @ordered
   */
  protected VarDef nameVal;

  /**
   * The cached value of the '{@link #getArrayIndizes() <em>Array Indizes</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getArrayIndizes()
   * @generated
   * @ordered
   */
  protected EList<Expr> arrayIndizes;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ExprIdentifierImpl()
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
    return PscriptPackage.Literals.EXPR_IDENTIFIER;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public VarDef getNameVal()
  {
    if (nameVal != null && nameVal.eIsProxy())
    {
      InternalEObject oldNameVal = (InternalEObject)nameVal;
      nameVal = (VarDef)eResolveProxy(oldNameVal);
      if (nameVal != oldNameVal)
      {
        if (eNotificationRequired())
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, PscriptPackage.EXPR_IDENTIFIER__NAME_VAL, oldNameVal, nameVal));
      }
    }
    return nameVal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public VarDef basicGetNameVal()
  {
    return nameVal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setNameVal(VarDef newNameVal)
  {
    VarDef oldNameVal = nameVal;
    nameVal = newNameVal;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.EXPR_IDENTIFIER__NAME_VAL, oldNameVal, nameVal));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<Expr> getArrayIndizes()
  {
    if (arrayIndizes == null)
    {
      arrayIndizes = new EObjectContainmentEList<Expr>(Expr.class, this, PscriptPackage.EXPR_IDENTIFIER__ARRAY_INDIZES);
    }
    return arrayIndizes;
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
      case PscriptPackage.EXPR_IDENTIFIER__ARRAY_INDIZES:
        return ((InternalEList<?>)getArrayIndizes()).basicRemove(otherEnd, msgs);
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
      case PscriptPackage.EXPR_IDENTIFIER__NAME_VAL:
        if (resolve) return getNameVal();
        return basicGetNameVal();
      case PscriptPackage.EXPR_IDENTIFIER__ARRAY_INDIZES:
        return getArrayIndizes();
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
      case PscriptPackage.EXPR_IDENTIFIER__NAME_VAL:
        setNameVal((VarDef)newValue);
        return;
      case PscriptPackage.EXPR_IDENTIFIER__ARRAY_INDIZES:
        getArrayIndizes().clear();
        getArrayIndizes().addAll((Collection<? extends Expr>)newValue);
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
      case PscriptPackage.EXPR_IDENTIFIER__NAME_VAL:
        setNameVal((VarDef)null);
        return;
      case PscriptPackage.EXPR_IDENTIFIER__ARRAY_INDIZES:
        getArrayIndizes().clear();
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
      case PscriptPackage.EXPR_IDENTIFIER__NAME_VAL:
        return nameVal != null;
      case PscriptPackage.EXPR_IDENTIFIER__ARRAY_INDIZES:
        return arrayIndizes != null && !arrayIndizes.isEmpty();
    }
    return super.eIsSet(featureID);
  }

} //ExprIdentifierImpl
