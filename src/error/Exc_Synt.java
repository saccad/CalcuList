package error;


public class Exc_Synt extends Exc {
	private static final long serialVersionUID = 1L;
	public Exc_Synt ( ErrorType excType ) {
		super(excType);
	}
	public Exc_Synt ( ErrorType excType,  String errMessage ) {
		super(excType, errMessage);
	}	


}
