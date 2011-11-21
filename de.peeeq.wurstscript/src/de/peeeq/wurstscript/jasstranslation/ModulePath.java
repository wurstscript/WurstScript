//package de.peeeq.wurstscript.jasstranslation;
//
//import com.google.common.base.Objects;
//import com.google.common.base.Preconditions;
//
//import de.peeeq.wurstscript.ast.ClassDef;
//import de.peeeq.wurstscript.ast.ModuleDef;
//
//public abstract class ModulePath {
//	
//	protected int hashCode;
//	
//	static public ModulePath create() {
//		return ModulePathRoot.instance;
//	}
//	
//	static public ModulePath create(ClassDef c) {
//		return new ModulePathConsClassDef(c, ModulePathRoot.instance);
//	}
//	
//	static public ModulePath create(ModuleDef m) {
//		return new ModulePathConsModule(m, ModulePathRoot.instance);
//	}
//	
//	public ModulePath addFront(ModuleDef m) {
//		return new ModulePathConsModule(m, this);
//	}
//	
//	public ModulePath addFront(ClassDef c) {
//		return new ModulePathConsClassDef(c, this);
//	}
//	
//	abstract public ModulePath addBack(ModuleDef m);
//	
//	abstract public ModulePath addBack(ClassDef c);
//	
//	@Override
//	public int hashCode() {
//		return hashCode;
//	}
//
//}
//
//class ModulePathRoot extends ModulePath {
//	public static final ModulePath instance = new ModulePathRoot();
//
//	private ModulePathRoot() {
//		hashCode = 0xea6c23;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		return obj == this;
//	}
//	
//	@Override
//	public String toString() {
//		return "root";
//	}
//
//	@Override
//	public ModulePath addBack(ModuleDef m) {
//		return ModulePath.create(m);
//	}
//
//	@Override
//	public ModulePath addBack(ClassDef c) {
//		return ModulePath.create(c);
//	}
//}
//
//class ModulePathConsModule extends ModulePath {
//
//	private ModuleDef head;
//	private ModulePath tail;
//
//	public ModulePathConsModule(ModuleDef m, ModulePath tail) {
//		Preconditions.checkNotNull(m, tail);
//		this.head = m;
//		this.tail = tail;
//		this.hashCode = Objects.hashCode(head, tail);
//	}
//	
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof ModulePathConsModule) {
//			ModulePathConsModule other = (ModulePathConsModule) obj;
//			return other.hashCode == this.hashCode
//					&& other.head.equals(this.head)
//					&& other.tail.equals(this.tail);
//		}
//		return false;
//	}
//	
//	@Override
//	public String toString() {
//		return head.getName() + "_" + tail;
//	}
//
//	@Override
//	public ModulePath addBack(ModuleDef m) {
//		return tail.addBack(m).addFront(m);
//	}
//
//	@Override
//	public ModulePath addBack(ClassDef c) {
//		return tail.addBack(c).addFront(c);
//	}
//}
//
//class ModulePathConsClassDef extends ModulePath {
//
//	private ClassDef head;
//	private ModulePath tail;
//
//	public ModulePathConsClassDef(ClassDef m, ModulePath tail) {
//		Preconditions.checkNotNull(m, tail);
//		this.head = m;
//		this.tail = tail;
//		this.hashCode = Objects.hashCode(head, tail);
//	}
//	
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof ModulePathConsClassDef) {
//			ModulePathConsClassDef other = (ModulePathConsClassDef) obj;
//			return other.hashCode == this.hashCode
//					&& other.head.equals(this.head)
//					&& other.tail.equals(this.tail);
//		}
//		return false;
//	}
//	
//	@Override
//	public String toString() {
//		return head.getName() + "_" + tail;
//	}
//
//	@Override
//	public ModulePath addBack(ModuleDef m) {
//		return tail.addBack(m).addFront(m);
//	}
//
//	@Override
//	public ModulePath addBack(ClassDef c) {
//		return tail.addBack(c).addFront(c);
//	}
//}
//
