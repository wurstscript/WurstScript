/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript.impl;

import de.peeeq.pscript.pscript.PscriptPackage;
import de.peeeq.pscript.pscript.TypeDef;
import de.peeeq.pscript.pscript.TypeExpr;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Type Expr</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.impl.TypeExprImpl#getName <em>Name</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.impl.TypeExprImpl#isArray <em>Array</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.impl.TypeExprImpl#getSizes <em>Sizes</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TypeExprImpl extends MinimalEObjectImpl.Container implements TypeExpr
{
  /**
   * The cached value of the '{@link #getName() <em>Name</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected TypeDef name;

  /**
   * The default value of the '{@link #isArray() <em>Array</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isArray()
   * @generated
   * @ordered
   */
  protected static final boolean ARRAY_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isArray() <em>Array</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isArray()
   * @generated
   * @ordered
   */
  protected boolean array = ARRAY_EDEFAULT;

  /**
   * The cached value of the '{@link #getSizes() <em>Sizes</em>}' attribute list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSizes()
   * @generated
   * @ordered
   */
  protected EList<Integer> sizes;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected TypeExprImpl()
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
    return PscriptPackage.Literals.TYPE_EXPR;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TypeDef getName()
  {
    if (name != null && name.eIsProxy())
    {
      InternalEObject oldName = (InternalEObject)name;
      name = (TypeDef)eResolveProxy(oldName);
      if (name != oldName)
      {
        if (eNotificationRequired())
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, PscriptPackage.TYPE_EXPR__NAME, oldName, name));
      }
    }
    return name;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TypeDef basicGetName()
  {
    return name;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setName(TypeDef newName)
  {
    TypeDef oldName = name;
    name = newName;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.TYPE_EXPR__NAME, oldName, name));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public boolean isArray()
  {
    return array;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setArray(boolean newArray)
  {
    boolean oldArray = array;
    array = newArray;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.TYPE_EXPR__ARRAY, oldArray, array));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<Integer> getSizes()
  {
    if (sizes == null)
    {
      sizes = new EDataTypeEList<Integer>(Integer.class, this, PscriptPackage.TYPE_EXPR__SIZES);
    }
    return sizes;
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
      case PscriptPackage.TYPE_EXPR__NAME:
        if (resolve) return getName();
        return basicGetName();
      case PscriptPackage.TYPE_EXPR__ARRAY:
        return isArray();
      case PscriptPackage.TYPE_EXPR__SIZES:
        return getSizes();
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
      case PscriptPackage.TYPE_EXPR__NAME:
        setName((TypeDef)newValue);
        return;
      case PscriptPackage.TYPE_EXPR__ARRAY:
        setArray((Boolean)newValue);
        return;
      case PscriptPackage.TYPE_EXPR__SIZES:
        getSizes().clear();
        getSizes().addAll((Collection<? extends Integer>)newValue);
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
      case PscriptPackage.TYPE_EXPR__NAME:
        setName((TypeDef)null);
        return;
      case PscriptPackage.TYPE_EXPR__ARRAY:
        setArray(ARRAY_EDEFAULT);
        return;
      case PscriptPackage.TYPE_EXPR__SIZES:
        getSizes().clear();
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
      case PscriptPackage.TYPE_EXPR__NAME:
        return name != null;
      case PscriptPackage.TYPE_EXPR__ARRAY:
        return array != ARRAY_EDEFAULT;
      case PscriptPackage.TYPE_EXPR__SIZES:
        return sizes != null && !sizes.isEmpty();
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
    result.append(" (array: ");
    result.append(array);
    result.append(", sizes: ");
    result.append(sizes);
    result.append(')');
    return result.toString();
  }

} //TypeExprImpl
