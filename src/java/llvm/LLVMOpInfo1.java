package llvm;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class LLVMOpInfo1 extends Structure {
	public LLVMOpInfoSymbol1 AddSymbol;
	public LLVMOpInfoSymbol1 SubtractSymbol;
	public long Value;
	public long VariantKind;
	public LLVMOpInfo1() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("AddSymbol", "SubtractSymbol", "Value", "VariantKind");
	}
	public LLVMOpInfo1(LLVMOpInfoSymbol1 AddSymbol, LLVMOpInfoSymbol1 SubtractSymbol, long Value, long VariantKind) {
		super();
		this.AddSymbol = AddSymbol;
		this.SubtractSymbol = SubtractSymbol;
		this.Value = Value;
		this.VariantKind = VariantKind;
	}
	public LLVMOpInfo1(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends LLVMOpInfo1 implements Structure.ByReference {
		
	};
	public static class ByValue extends LLVMOpInfo1 implements Structure.ByValue {
		
	};
}
