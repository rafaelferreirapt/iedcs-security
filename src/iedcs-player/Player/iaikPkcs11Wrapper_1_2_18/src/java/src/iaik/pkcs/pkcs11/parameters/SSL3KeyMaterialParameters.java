package iaik.pkcs.pkcs11.parameters;

import iaik.pkcs.pkcs11.TokenRuntimeException;
import iaik.pkcs.pkcs11.wrapper.CK_SSL3_KEY_MAT_OUT;
import iaik.pkcs.pkcs11.wrapper.CK_SSL3_KEY_MAT_PARAMS;
import iaik.pkcs.pkcs11.wrapper.CK_SSL3_RANDOM_DATA;
import iaik.pkcs.pkcs11.wrapper.Constants;

/**
 * This class encapsulates parameters for the Mechanism.SSL3_KEY_AND_MAC_DERIVE
 * mechanism.
 *
 * @author Karl Scheibelhofer
 * @version 1.0
 * @invariants (randomInfo_ <> null)
 *             and (returnedKeyMaterial_ <> null)
 */
public class SSL3KeyMaterialParameters implements Parameters {

	/**
	 * The length (in bits) of the MACing keys agreed upon during the protocol
	 * handshake phase.
	 */
	protected long macSizeInBits_;

	/**
	 * The length (in bits) of the secret keys agreed upon during the protocol
	 * handshake phase.
	 */
	protected long keySizeInBits_;

	/**
	 * The length (in bits) of the IV agreed upon during the protocol handshake
	 * phase. If no IV is required, the length should be set to 0.
	 */
	protected long initializationVectorSizeInBits_;

	/**
	 * Indicates whether the keys have to be derived for an export version of the
	 * protocol.
	 */
	protected boolean export_;

	/**
	 * The client's and server's random data information.
	 */
	protected SSL3RandomDataParameters randomInfo_;

	/**
	 * Receives the handles for the keys generated and the IVs.
	 */
	protected SSL3KeyMaterialOutParameters returnedKeyMaterial_;

	/**
	 * Create a new SSL3KeyMaterialParameters object with the given
	 * parameters.
	 *
	 * @param macSizeInBits The length (in bits) of the MACing keys agreed upon
	 *                      during the protocol handshake phase.
	 * @param keySizeInBits The length (in bits) of the secret keys agreed upon
	 *                      during the protocol handshake phase.
	 * @param initializationVectorSizeInBits The length (in bits) of the IV agreed
	 *                                       upon during the protocol handshake
	 *                                       phase. If no IV is required, the
	 *                                       length should be set to 0.
	 * @param export Indicates whether the keys have to be derived for an export
	 *               version of the protocol.
	 * @param randomInfo The client's and server's random data information.
	 * @param returnedKeyMaterial  Receives the handles for the keys generated and the IVs.
	 * @preconditions (randomInfo <> null)
	 *                and (returnedKeyMaterial <> null)
	 * @postconditions
	 */
	public SSL3KeyMaterialParameters(long macSizeInBits,
	                                 long keySizeInBits,
	                                 long initializationVectorSizeInBits,
	                                 boolean export,
	                                 SSL3RandomDataParameters randomInfo,
	                                 SSL3KeyMaterialOutParameters returnedKeyMaterial)
	{
		if (randomInfo == null) {
			throw new NullPointerException("Argument \"randomInfo\" must not be null.");
		}
		if (returnedKeyMaterial == null) {
			throw new NullPointerException("Argument \"returnedKeyMaterial\" must not be null.");
		}
		macSizeInBits_ = macSizeInBits;
		keySizeInBits_ = keySizeInBits;
		initializationVectorSizeInBits_ = initializationVectorSizeInBits;
		export_ = export;
		randomInfo_ = randomInfo;
		returnedKeyMaterial_ = returnedKeyMaterial;
	}

	/**
	 * Create a (deep) clone of this object.
	 *
	 * @return A clone of this object.
	 * @preconditions
	 * @postconditions (result <> null)
	 *                 and (result instanceof SSL3KeyMaterialParameters)
	 *                 and (result.equals(this))
	 */
	public java.lang.Object clone() {
		SSL3KeyMaterialParameters clone;

		try {
			clone = (SSL3KeyMaterialParameters) super.clone();

			clone.randomInfo_ = (SSL3RandomDataParameters) this.randomInfo_.clone();
			clone.returnedKeyMaterial_ = (SSL3KeyMaterialOutParameters) this.returnedKeyMaterial_
			    .clone();
		} catch (CloneNotSupportedException ex) {
			// this must not happen, because this class is cloneable
			throw new TokenRuntimeException("An unexpected clone exception occurred.", ex);
		}

		return clone;
	}

	/**
	 * Get this parameters object as a CK_SSL3_KEY_MAT_PARAMS object.
	 *
	 * @return This object as a CK_SSL3_KEY_MAT_PARAMS object.
	 * @preconditions
	 * @postconditions (result <> null)
	 */
	public Object getPKCS11ParamsObject() {
		CK_SSL3_KEY_MAT_PARAMS params = new CK_SSL3_KEY_MAT_PARAMS();

		params.ulMacSizeInBits = macSizeInBits_;
		params.ulKeySizeInBits = keySizeInBits_;
		params.ulIVSizeInBits = initializationVectorSizeInBits_;
		params.bIsExport = export_;
		params.RandomInfo = (CK_SSL3_RANDOM_DATA) randomInfo_.getPKCS11ParamsObject();
		params.pReturnedKeyMaterial = (CK_SSL3_KEY_MAT_OUT) returnedKeyMaterial_
		    .getPKCS11ParamsObject();

		return params;
	}

	/**
	 * Get the length (in bits) of the MACing keys agreed upon during the protocol
	 * handshake phase.
	 *
	 * @return The length (in bits) of the MACing keys agreed upon during the
	 *         protocol handshake phase.
	 * @preconditions
	 * @postconditions
	 */
	public long getMacSizeInBits() {
		return macSizeInBits_;
	}

	/**
	 * Get the length (in bits) of the secret keys agreed upon during the protocol
	 * handshake phase.
	 *
	 * @return The length (in bits) of the secret keys agreed upon during the
	 *         protocol handshake phase.
	 * @preconditions
	 * @postconditions
	 */
	public long getKeySizeInBits() {
		return keySizeInBits_;
	}

	/**
	 * Get the length (in bits) of the IV agreed upon during the protocol
	 * handshake phase. If no IV is required, the length should be set to
	 * 0.
	 *
	 * @return The length (in bits) of the IV agreed upon during the protocol
	 *         handshake phase. If no IV is required, the length should be set to
	 *         0.
	 * @preconditions
	 * @postconditions
	 */
	public long getInitializationVectorSizeInBits() {
		return initializationVectorSizeInBits_;
	}

	/**
	 * Check whether the keys have to be derived for an export version of the
	 * protocol.
	 *
	 * @return True, if the keys have to be derived for an export version of the
	 *         protocol; false, otherwise.
	 * @preconditions
	 * @postconditions
	 */
	public boolean isExport() {
		return export_;
	}

	/**
	 * Get the client's and server's random data information.
	 *
	 * @return The client's and server's random data information.
	 * @preconditions
	 * @postconditions (result <> null)
	 */
	public SSL3RandomDataParameters getRandomInfo() {
		return randomInfo_;
	}

	/**
	 * Get the object that receives the handles for the keys generated and the
	 * IVs.
	 *
	 * @return The object that receives the handles for the keys generated and
	 *          the IVs.
	 * @preconditions
	 * @postconditions (result <> null)
	 */
	public SSL3KeyMaterialOutParameters getReturnedKeyMaterial() {
		return returnedKeyMaterial_;
	}

	/**
	 * Set the length (in bits) of the MACing keys agreed upon during the protocol
	 * handshake phase.
	 *
	 * @param macSizeInBits The length (in bits) of the MACing keys agreed upon
	 *                      during the protocol handshake phase.
	 * @preconditions
	 * @postconditions
	 */
	public void setMacSizeInBits(long macSizeInBits) {
		macSizeInBits_ = macSizeInBits;
	}

	/**
	 * Set the length (in bits) of the secret keys agreed upon during the protocol
	 * handshake phase.
	 *
	 * @param keySizeInBits The length (in bits) of the secret keys agreed upon
	 *                      during the protocol handshake phase.
	 * @preconditions
	 * @postconditions
	 */
	public void setKeySizeInBits(long keySizeInBits) {
		keySizeInBits_ = keySizeInBits;
	}

	/**
	 * Set the length (in bits) of the IV agreed upon during the protocol
	 * handshake phase. If no IV is required, the length should be set to
	 * 0.
	 *
	 * @param initializationVectorSizeInBits The length (in bits) of the IV agreed
	 *                                       upon during the protocol handshake
	 *                                       phase. If no IV is required, the
	 *                                       length should be set to 0.
	 * @preconditions
	 * @postconditions
	 */
	public void setInitializationVectorSizeInBits(long initializationVectorSizeInBits) {
		initializationVectorSizeInBits_ = initializationVectorSizeInBits;
	}

	/**
	 * Set whether the keys have to be derived for an export version of the
	 * protocol.
	 *
	 * @param export True, if the keys have to be derived for an export version
	 *               of the protocol; false, otherwise.
	 * @preconditions
	 * @postconditions
	 */
	public void isExport(boolean export) {
		export_ = export;
	}

	/**
	 * Set the client's and server's random data information.
	 *
	 * @param randomInfo The client's and server's random data information.
	 * @preconditions (randomInfo <> null)
	 * @postconditions
	 */
	public void setRandomInfo(SSL3RandomDataParameters randomInfo) {
		if (randomInfo == null) {
			throw new NullPointerException("Argument \"randomInfo\" must not be null.");
		}
		randomInfo_ = randomInfo;
	}

	/**
	 * Set the object that receives the handles for the keys generated and the
	 * IVs.
	 *
	 * @param returnedKeyMaterial The object that receives the handles for the 
	 *                            keys generated and the IVs.
	 * @preconditions (returnedKeyMaterial <> null)
	 * @postconditions
	 */
	public void setReturnedKeyMaterial(SSL3KeyMaterialOutParameters returnedKeyMaterial) {
		if (returnedKeyMaterial == null) {
			throw new NullPointerException("Argument \"returnedKeyMaterial\" must not be null.");
		}
		returnedKeyMaterial_ = returnedKeyMaterial;
	}

	/**
	 * Returns the string representation of this object. Do not parse data from
	 * this string, it is for debugging only.
	 *
	 * @return A string representation of this object.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(Constants.INDENT);
		buffer.append("MAC Size in Bits (dec): ");
		buffer.append(macSizeInBits_);
		buffer.append(Constants.NEWLINE);

		buffer.append(Constants.INDENT);
		buffer.append("Key Size in Bits (dec): ");
		buffer.append(keySizeInBits_);
		buffer.append(Constants.NEWLINE);

		buffer.append(Constants.INDENT);
		buffer.append("Initialization Vector Size in Bits (dec): ");
		buffer.append(initializationVectorSizeInBits_);
		buffer.append(Constants.NEWLINE);

		buffer.append(Constants.INDENT);
		buffer.append("For Export Version: ");
		buffer.append(export_);
		buffer.append(Constants.NEWLINE);

		buffer.append(Constants.INDENT);
		buffer.append("Client's and Server'S Random Information (hex): ");
		buffer.append(Constants.NEWLINE);
		buffer.append(randomInfo_);
		buffer.append(Constants.NEWLINE);

		buffer.append(Constants.INDENT);
		buffer.append("Handles of the generated Keys and IVs: ");
		buffer.append(returnedKeyMaterial_);
		// buffer.append(Constants.NEWLINE);

		return buffer.toString();
	}

	/**
	 * Compares all member variables of this object with the other object.
	 * Returns only true, if all are equal in both objects.
	 *
	 * @param otherObject The other object to compare to.
	 * @return True, if other is an instance of this class and all member
	 *         variables of both objects are equal. False, otherwise.
	 * @preconditions
	 * @postconditions
	 */
	public boolean equals(java.lang.Object otherObject) {
		boolean equal = false;

		if (otherObject instanceof SSL3KeyMaterialParameters) {
			SSL3KeyMaterialParameters other = (SSL3KeyMaterialParameters) otherObject;
			equal = (this == other)
			    || ((this.macSizeInBits_ == other.macSizeInBits_)
			        && (this.keySizeInBits_ == other.keySizeInBits_)
			        && (this.initializationVectorSizeInBits_ == other.initializationVectorSizeInBits_)
			        && (this.export_ == other.export_)
			        && this.randomInfo_.equals(other.randomInfo_) && this.returnedKeyMaterial_
			          .equals(other.returnedKeyMaterial_));
		}

		return equal;
	}

	/**
	 * The overriding of this method should ensure that the objects of this class
	 * work correctly in a hashtable.
	 *
	 * @return The hash code of this object.
	 * @preconditions
	 * @postconditions
	 */
	public int hashCode() {
		return ((int) macSizeInBits_) ^ ((int) keySizeInBits_)
		    ^ ((int) initializationVectorSizeInBits_) ^ randomInfo_.hashCode()
		    ^ returnedKeyMaterial_.hashCode();
	}

}
