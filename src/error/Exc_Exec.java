package error;


public class Exc_Exec extends Exc {
	private static final long serialVersionUID = 1L;
	public Exc_Exec ( ErrorType excType ) {
		super(excType);
	}
	public Exc_Exec ( ErrorType excType,  String errMessage ) {
		super(excType, errMessage);
	}	

}
