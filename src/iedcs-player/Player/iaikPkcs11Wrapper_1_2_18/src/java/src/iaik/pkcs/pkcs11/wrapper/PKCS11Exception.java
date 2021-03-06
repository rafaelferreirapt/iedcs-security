package iaik.pkcs.pkcs11.wrapper;

import iaik.pkcs.pkcs11.TokenException;

import java.util.Properties;

/**
 * This is the superclass of all checked exceptions used by this package. An
 * exception of this class indicates that a function call to the underlying
 * PKCS#11 module returned a value not equal to CKR_OK. The application can get
 * the returned value by calling getErrorCode(). A return value not equal to
 * CKR_OK is the only reason for such an exception to be thrown.
 * PKCS#11 defines the meaning of an error-code, which may depend on the
 * context in which the error occurs.
 *
 * @author <a href="mailto:Karl.Scheibelhofer@iaik.at"> Karl Scheibelhofer </a>
 * @version 1.0
 * @invariants
 */
public class PKCS11Exception extends TokenException {

	/**
	 * The name of the properties file that holds the names of the PKCS#11 error-
	 * codes.
	 */
	protected static final String ERROR_CODE_PROPERTIES = "iaik/pkcs/pkcs11/wrapper/ExceptionMessages.properties";

	/**
	 * The properties object that holds the mapping from error-code to the name
	 * of the PKCS#11 error.
	 */
	protected static Properties errorCodeNames_;

	/**
	 * True, if the mapping of error codes to PKCS#11 error names is available.
	 */
	protected static boolean errorCodeNamesAvailable_;

	/**
	 * The code of the error which was the reason for this exception.
	 */
	protected long errorCode_;

	/**
	 * Constructor taking the error code as defined for the CKR_* constants
	 * in PKCS#11.
	 * 
	 * @param errorCode The PKCS#11 error code (return value).
	 */
	public PKCS11Exception(long errorCode) {
		errorCode_ = errorCode;
	}

	/**
	 * This method gets the corresponding text error message from
	 * a property file. If this file is not available, it returns the error
	 * code as a hex-string.
	 *
	 * @return The message or the error code; e.g. "CKR_DEVICE_ERROR" or
	 *         "0x00000030".
	 * @preconditions
	 * @postconditions (result <> null)
	 */
	public synchronized String getMessage() {
		// if the names of the defined error codes are not yet loaded, load them
		if (errorCodeNames_ == null) { // ensure that another thread has not loaded the codes meanwhile
			Properties errorCodeNames = new Properties();
			try {
				errorCodeNames.load(getClass().getClassLoader().getResourceAsStream(
				    ERROR_CODE_PROPERTIES));
				errorCodeNames_ = errorCodeNames;
				errorCodeNamesAvailable_ = true;
			} catch (Exception exception) {
				System.err.println("Could not read properties for error code names: "
				    + exception.getMessage());
			}
		}

		// if we can get the name of the error code, take the name, otherwise return the code
		String errorCodeHexString = "0x" + Functions.toFullHexString((int) errorCode_);
		String errorCodeName = errorCodeNamesAvailable_ ? errorCodeNames_
		    .getProperty(errorCodeHexString) : null;
		String message = (errorCodeName != null) ? errorCodeName : errorCodeHexString;

		return message;
	}

	/**
	 * Returns the PKCS#11 error code.
	 *
	 * @return The error code; e.g. 0x00000030.
	 * @preconditions
	 * @postconditions
	 */
	public long getErrorCode() {
		return errorCode_;
	}

}
