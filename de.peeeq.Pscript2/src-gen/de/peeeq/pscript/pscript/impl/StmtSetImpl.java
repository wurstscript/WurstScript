/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript.impl;

import de.peeeq.pscript.pscript.Expr;
import de.peeeq.pscript.pscript.OpAssignment;
import de.peeeq.pscript.pscript.PscriptPackage;
import de.peeeq.pscript.pscript.StmtCall;
import de.peeeq.pscript.pscript.StmtSet;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Stmt Set</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.peeeq.pscript.pscript.impl.StmtSetImpl#getLeftE <em>Left E</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.impl.StmtSetImpl#getRight <em>Right</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.impl.StmtSetImpl#getLeft <em>Left</em>}</li>
 *   <li>{@link de.peeeq.pscript.pscript.impl.StmtSetImpl#getOpAssignment <em>Op Assignment</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class StmtSetImpl extends StatementImpl implements StmtSet
{
  /**
   * The cached value of the '{@link #getLeftE() <em>Left E</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getLeftE()
   * @generated
   * @ordered
   */
  protected Expr leftE;

  /**
   * The cached value of the '{@link #getRight() <em>Right</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getRight()
   * @generated
   * @ordered
   */
  protected Expr right;

  /**
   * The cached value of the '{@link #getLeft() <em>Left</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getLeft()
   * @generated
   * @ordered
   */
  protected StmtCall left;

  /**
   * The cached value of the '{@link #getOpAssignment() <em>Op Assignment</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getOpAssignment()
   * @generated
   * @ordered
   */
  protected OpAssignment opAssignment;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected StmtSetImpl()
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
    return PscriptPackage.Literals.STMT_SET;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Expr getLeftE()
  {
    return leftE;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetLeftE(Expr newLeftE, NotificationChain msgs)
  {
    Expr oldLeftE = leftE;
    leftE = newLeftE;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_SET__LEFT_E, oldLeftE, newLeftE);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setLeftE(Expr newLeftE)
  {
    if (newLeftE != leftE)
    {
      NotificationChain msgs = null;
      if (leftE != null)
        msgs = ((InternalEObject)leftE).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.STMT_SET__LEFT_E, null, msgs);
      if (newLeftE != null)
        msgs = ((InternalEObject)newLeftE).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.STMT_SET__LEFT_E, null, msgs);
      msgs = basicSetLeftE(newLeftE, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_SET__LEFT_E, newLeftE, newLeftE));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Expr getRight()
  {
    return right;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetRight(Expr newRight, NotificationChain msgs)
  {
    Expr oldRight = right;
    right = newRight;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_SET__RIGHT, oldRight, newRight);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setRight(Expr newRight)
  {
    if (newRight != right)
    {
      NotificationChain msgs = null;
      if (right != null)
        msgs = ((InternalEObject)right).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.STMT_SET__RIGHT, null, msgs);
      if (newRight != null)
        msgs = ((InternalEObject)newRight).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.STMT_SET__RIGHT, null, msgs);
      msgs = basicSetRight(newRight, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_SET__RIGHT, newRight, newRight));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public StmtCall getLeft()
  {
    return left;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetLeft(StmtCall newLeft, NotificationChain msgs)
  {
    StmtCall oldLeft = left;
    left = newLeft;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_SET__LEFT, oldLeft, newLeft);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setLeft(StmtCall newLeft)
  {
    if (newLeft != left)
    {
      NotificationChain msgs = null;
      if (left != null)
        msgs = ((InternalEObject)left).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.STMT_SET__LEFT, null, msgs);
      if (newLeft != null)
        msgs = ((InternalEObject)newLeft).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.STMT_SET__LEFT, null, msgs);
      msgs = basicSetLeft(newLeft, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_SET__LEFT, newLeft, newLeft));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpAssignment getOpAssignment()
  {
    return opAssignment;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetOpAssignment(OpAssignment newOpAssignment, NotificationChain msgs)
  {
    OpAssignment oldOpAssignment = opAssignment;
    opAssignment = newOpAssignment;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_SET__OP_ASSIGNMENT, oldOpAssignment, newOpAssignment);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setOpAssignment(OpAssignment newOpAssignment)
  {
    if (newOpAssignment != opAssignment)
    {
      NotificationChain msgs = null;
      if (opAssignment != null)
        msgs = ((InternalEObject)opAssignment).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.STMT_SET__OP_ASSIGNMENT, null, msgs);
      if (newOpAssignment != null)
        msgs = ((InternalEObject)newOpAssignment).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PscriptPackage.STMT_SET__OP_ASSIGNMENT, null, msgs);
      msgs = basicSetOpAssignment(newOpAssignment, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PscriptPackage.STMT_SET__OP_ASSIGNMENT, newOpAssignment, newOpAssignment));
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
      case PscriptPackage.STMT_SET__LEFT_E:
        return basicSetLeftE(null, msgs);
      case PscriptPackage.STMT_SET__RIGHT:
        return basicSetRight(null, msgs);
      case PscriptPackage.STMT_SET__LEFT:
        return basicSetLeft(null, msgs);
      case PscriptPackage.STMT_SET__OP_ASSIGNMENT:
        return basicSetOpAssignment(null, msgs);
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
      case PscriptPackage.STMT_SET__LEFT_E:
        return getLeftE();
      case PscriptPackage.STMT_SET__RIGHT:
        return getRight();
      case PscriptPackage.STMT_SET__LEFT:
        return getLeft();
      case PscriptPackage.STMT_SET__OP_ASSIGNMENT:
        return getOpAssignment();
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
      case PscriptPackage.STMT_SET__LEFT_E:
        setLeftE((Expr)newValue);
        return;
      case PscriptPackage.STMT_SET__RIGHT:
        setRight((Expr)newValue);
        return;
      case PscriptPackage.STMT_SET__LEFT:
        setLeft((StmtCall)newValue);
        return;
      case PscriptPackage.STMT_SET__OP_ASSIGNMENT:
        setOpAssignment((OpAssignment)newValue);
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
      case PscriptPackage.STMT_SET__LEFT_E:
        setLeftE((Expr)null);
        return;
      case PscriptPackage.STMT_SET__RIGHT:
        setRight((Expr)null);
        return;
      case PscriptPackage.STMT_SET__LEFT:
        setLeft((StmtCall)null);
        return;
      case PscriptPackage.STMT_SET__OP_ASSIGNMENT:
        setOpAssignment((OpAssignment)null);
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
      case PscriptPackage.STMT_SET__LEFT_E:
        return leftE != null;
      case PscriptPackage.STMT_SET__RIGHT:
        return right != null;
      case PscriptPackage.STMT_SET__LEFT:
        return left != null;
      case PscriptPackage.STMT_SET__OP_ASSIGNMENT:
        return opAssignment != null;
    }
    return super.eIsSet(featureID);
  }

} //StmtSetImpl
