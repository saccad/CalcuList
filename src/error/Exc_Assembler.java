package error;

public class Exc_Assembler extends Exc {

	private static final long serialVersionUID = 1L;
	public Exc_Assembler ( ErrorType excType ) {
		super(excType);
	}
	public Exc_Assembler ( ErrorType excType,  String errMessage ) {
		super(excType, errMessage);
	}	


}
