package clojure.llvm;
import org.bridj.BridJ;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("LLVM-3.5svn") 
public class LLVMOpInfoSymbol1 extends StructObject {
	static {
		BridJ.register();
	}
	/** 1 if this symbol is present */
	@Field(0) 
	public long Present() {
		return this.io.getLongField(this, 0);
	}
	/** 1 if this symbol is present */
	@Field(0) 
	public LLVMOpInfoSymbol1 Present(long Present) {
		this.io.setLongField(this, 0, Present);
		return this;
	}
	/** symbol name if not NULL */
	@Field(1) 
	public Pointer<Byte > Name() {
		return this.io.getPointerField(this, 1);
	}
	/** symbol name if not NULL */
	@Field(1) 
	public LLVMOpInfoSymbol1 Name(Pointer<Byte > Name) {
		this.io.setPointerField(this, 1, Name);
		return this;
	}
	/** symbol value if name is NULL */
	@Field(2) 
	public long Value() {
		return this.io.getLongField(this, 2);
	}
	/** symbol value if name is NULL */
	@Field(2) 
	public LLVMOpInfoSymbol1 Value(long Value) {
		this.io.setLongField(this, 2, Value);
		return this;
	}
	public LLVMOpInfoSymbol1() {
		super();
	}
	public LLVMOpInfoSymbol1(Pointer pointer) {
		super(pointer);
	}
}
