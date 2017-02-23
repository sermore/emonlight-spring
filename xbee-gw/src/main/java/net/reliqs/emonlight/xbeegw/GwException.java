package net.reliqs.emonlight.xbeegw;

public class GwException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GwException(Exception e) {
		super(e);
	}

	public GwException(String msg) {
		super(msg);
	}

}
