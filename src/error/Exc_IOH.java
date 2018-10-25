package error;


public class Exc_IOH extends Exc {
	private static final long serialVersionUID = 1L;
	public Exc_IOH ( ErrorType excType ) {
		super(excType);
	}
	public Exc_IOH ( ErrorType excType,  String errMessage ) {
		super(excType, errMessage);
	}	

}
