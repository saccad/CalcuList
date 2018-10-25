package error;

public class Exc_Sem extends Exc {
	private static final long serialVersionUID = 1L;
	public Exc_Sem ( ErrorType excType ) {
		super(excType);
	}
	public Exc_Sem ( ErrorType excType,  String errMessage ) {
		super(excType, errMessage);
	}	


}

