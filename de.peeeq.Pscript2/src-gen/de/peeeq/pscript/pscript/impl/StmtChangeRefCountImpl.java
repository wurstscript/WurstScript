/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript.impl;

import de.peeeq.pscript.pscript.Expr;
import de.peeeq.pscript.pscript.PscriptPackage;
import de.peeeq.pscript.pscript.StmtChangeRefCount;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Stmt Change Ref Count</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.impl.StmtChangeRefCountImpl#isIncrease <em>Increase</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.impl.StmtChangeRefCountImpl#isDecrease <em>Decrease</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.impl.StmtChangeRefCountImpl#getObj <em>Obj</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class StmtChangeRefCountImpl extends StatementImpl implements StmtChangeRefCount
{
  /**
   * The default value of the '{@link #isIncrease() <em>Increase</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isIncrease()
   * @generated
   * @ordered
   */
  protected static final boolean INCREASE_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isIncrease() <em>Increase</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isIncrease()
   * @generated
   * @ordered
   */
  protected boolean increase = INCREASE_EDEFAULT;

  /**
   * The default value of the '{@link #isDecrease() <em>Decrease</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isDecrease()
   * @generated
   * @ordered
   */
  protected static final boolean DECREASE_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isDecrease() <em>Decrease</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isDecrease()
   * @generated
   * @ordered
   */
  protected boolean decrease = DECREASE_EDEFAULT;

  /**
   * The cached value of the '{@link #getObj() <em>Obj</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getObj()
   * @generated
   * @ordered
   */
  protected Expr obj;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected StmtChangeRefCountImpl()
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
    return PscriptPackage.Literals.STMT_CHANGE_REF_COUNT;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public boolean isIncrease()
  {
    return increase;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setIncrease(boolean newIncrease)
  {
    boolean oldIncrease = increase;
    increase = newIncrease;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_CHANGE_REF_COUNT__INCREASE, oldIncrease, increase));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public boolean isDecrease()
  {
    return decrease;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setDecrease(boolean newDecrease)
  {
    boolean oldDecrease = decrease;
    decrease = newDecrease;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_CHANGE_REF_COUNT__DECREASE, oldDecrease, decrease));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Expr getObj()
  {
    return obj;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetObj(Expr newObj, NotificationChain msgs)
  {
    Expr oldObj = obj;
    obj = newObj;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_CHANGE_REF_COUNT__OBJ, oldObj, newObj);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setObj(Expr newObj)
  {
    if (newObj != obj)
    {
      NotificationChain msgs = null;
      if (obj != null)
        msgs = ((InternalEObject)obj).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.STMT_CHANGE_REF_COUNT__OBJ, null, msgs);
      if (newObj != null)
        msgs = ((InternalEObject)newObj).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.STMT_CHANGE_REF_COUNT__OBJ, null, msgs);
      msgs = basicSetObj(newObj, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_CHANGE_REF_COUNT__OBJ, newObj, newObj));
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
      case PscriptPackage.STMT_CHANGE_REF_COUNT__OBJ:
        return basicSetObj(null, msgs);
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
      case PscriptPackage.STMT_CHANGE_REF_COUNT__INCREASE:
        return isIncrease();
      case PscriptPackage.STMT_CHANGE_REF_COUNT__DECREASE:
        return isDecrease();
      case PscriptPackage.STMT_CHANGE_REF_COUNT__OBJ:
        return getObj();
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
      case PscriptPackage.STMT_CHANGE_REF_COUNT__INCREASE:
        setIncrease((Boolean)newValue);
        return;
      case PscriptPackage.STMT_CHANGE_REF_COUNT__DECREASE:
        setDecrease((Boolean)newValue);
        return;
      case PscriptPackage.STMT_CHANGE_REF_COUNT__OBJ:
        setObj((Expr)newValue);
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
      case PscriptPackage.STMT_CHANGE_REF_COUNT__INCREASE:
        setIncrease(INCREASE_EDEFAULT);
        return;
      case PscriptPackage.STMT_CHANGE_REF_COUNT__DECREASE:
        setDecrease(DECREASE_EDEFAULT);
        return;
      case PscriptPackage.STMT_CHANGE_REF_COUNT__OBJ:
        setObj((Expr)null);
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
      case PscriptPackage.STMT_CHANGE_REF_COUNT__INCREASE:
        return increase != INCREASE_EDEFAULT;
      case PscriptPackage.STMT_CHANGE_REF_COUNT__DECREASE:
        return decrease != DECREASE_EDEFAULT;
      case PscriptPackage.STMT_CHANGE_REF_COUNT__OBJ:
        return obj != null;
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
    result.append(" (increase: ");
    result.append(increase);
    result.append(", decrease: ");
    result.append(decrease);
    result.append(')');
    return result.toString();
  }

} //StmtChangeRefCountImpl
