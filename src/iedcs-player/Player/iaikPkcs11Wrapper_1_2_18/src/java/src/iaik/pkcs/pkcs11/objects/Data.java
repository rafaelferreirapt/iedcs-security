package iaik.pkcs.pkcs11.objects;

import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.wrapper.Constants;

/**
 * Objects of this class represent a data object as specified by PKCS#11
 * v2.11.
 *
 * @author Karl Scheibelhofer
 * @version 1.0
 * @invariants (application_ <> null)
 *             and (objectID_ <> null)
 *             and (value_ <> null)
 */
public class Data extends Storage {

	/**
	 * The application description attribute of this data object.
	 */
	protected CharArrayAttribute application_;

	/**
	 * The object ID attribute of this data object (DER-encoded).
	 */
	protected ByteArrayAttribute objectID_;

	/**
	 * The value attribute of this data object.
	 */
	protected ByteArrayAttribute value_;

	/**
	 * Deafult Constructor.
	 *
	 * @preconditions
	 * @postconditions
	 */
	public Data() {
		super();
		objectClass_.setLongValue(ObjectClass.DATA);
	}

	/**
	 * Called by getInstance to create an instance of a PKCS#11 data object.
	 *
	 * @param session The session to use for reading attributes.
	 *                This session must have the appropriate rights; i.e.
	 *                it must be a user-session, if it is a private object.
	 * @param objectHandle The object handle as given from the PKCS#111 module.
	 * @exception TokenException If getting the attributes failed.
	 * @preconditions (session <> null)
	 * @postconditions
	 */
	protected Data(Session session, long objectHandle)
	    throws TokenException
	{
		super(session, objectHandle);
		objectClass_.setLongValue(ObjectClass.DATA);
	}

	/**
	 * Put all attributes of the given object into the attributes table of this
	 * object. This method is only static to be able to access invoke the
	 * implementation of this method for each class separately (see use in
	 * clone()).
	 *
	 * @param object The object to handle.
	 * @preconditions (object <> null)
	 * @postconditions
	 */
	protected static void putAttributesInTable(Data object) {
		if (object == null) {
			throw new NullPointerException("Argument \"object\" must not be null.");
		}

		object.attributeTable_.put(Attribute.APPLICATION, object.application_);
		object.attributeTable_.put(Attribute.OBJECT_ID, object.objectID_);
		object.attributeTable_.put(Attribute.VALUE, object.value_);
	}

	/**
	 * Allocates the attribute objects for this class and adds them to the
	 * attribute table.
	 *
	 * @preconditions
	 * @postconditions
	 */
	protected void allocateAttributes() {
		super.allocateAttributes();

		application_ = new CharArrayAttribute(Attribute.APPLICATION);
		objectID_ = new ByteArrayAttribute(Attribute.OBJECT_ID);
		value_ = new ByteArrayAttribute(Attribute.VALUE);

		putAttributesInTable(this);
	}

	/**
	 * The getInstance method of the Object class uses this method to
	 * create an instance of a PKCS#11 data object.
	 *
	 * @param session The session to use for reading attributes.
	 *                This session must have the appropriate rights; i.e.
	 *                it must be a user-session, if it is a private object.
	 * @param objectHandle The object handle as given from the PKCS#111 module.
	 * @return The object representing the PKCS#11 object.
	 *         The returned object can be casted to the
	 *         according sub-class.
	 * @exception TokenException If getting the attributes failed.
	 * @preconditions (session <> null)
	 * @postconditions (result <> null) 
	 */
	public static Object getInstance(Session session, long objectHandle)
	    throws TokenException
	{
		return new Data(session, objectHandle);
	}

	/**
	 * Create a (deep) clone of this object.
	 *
	 * @return A clone of this object.
	 * @preconditions
	 * @postconditions (result <> null)
	 *                 and (result instanceof Data)
	 *                 and (result.equals(this))
	 */
	public java.lang.Object clone() {
		Data clone = (Data) super.clone();

		clone.application_ = (CharArrayAttribute) this.application_.clone();
		clone.objectID_ = (ByteArrayAttribute) this.objectID_.clone();
		clone.value_ = (ByteArrayAttribute) this.value_.clone();

		putAttributesInTable(clone); // put all cloned attributes into the new table

		return clone;
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

		if (otherObject instanceof Data) {
			Data other = (Data) otherObject;
			equal = (this == other)
			    || (super.equals(other) && this.application_.equals(other.application_)
			        && this.objectID_.equals(other.objectID_) && this.value_
			          .equals(other.value_));
		}

		return equal;
	}

	/**
	 * Gets the application attribute of this data object.
	 *
	 * @return The application attribute.
	 * @preconditions
	 * @postconditions (result <> null)
	 */
	public CharArrayAttribute getApplication() {
		return application_;
	}

	/**
	 * Gets the object ID attribute of this data object.
	 *
	 * @return The object ID attribute.
	 * @preconditions
	 * @postconditions (result <> null)
	 */
	public ByteArrayAttribute getObjectID() {
		return objectID_;
	}

	/**
	 * Gets the value attribute of this data object.
	 *
	 * @return The value attribute.
	 * @preconditions
	 * @postconditions (result <> null)
	 */
	public ByteArrayAttribute getValue() {
		return value_;
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
		return application_.hashCode() ^ objectID_.hashCode() ^ value_.hashCode();
	}

	/**
	 * Read the values of the attributes of this object from the token.
	 *
	 * @param session The session handle to use for reading attributes.
	 *                This session must have the appropriate rights; i.e.
	 *                it must be a user-session, if it is a private object.
	 * @exception TokenException If getting the attributes failed.
	 * @preconditions (session <> null)
	 * @postconditions
	 */
	public void readAttributes(Session session)
	    throws TokenException
	{
		super.readAttributes(session);

		Object.getAttributeValue(session, objectHandle_, application_);
		Object.getAttributeValue(session, objectHandle_, objectID_);
		Object.getAttributeValue(session, objectHandle_, value_);
	}

	/**
	 * This method returns a string representation of the current object. The
	 * output is only for debugging purposes and should not be used for other
	 * purposes.
	 *
	 * @return A string presentation of this object for debugging output.
	 * @preconditions
	 * @postconditions (result <> null)
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(128);

		buffer.append(super.toString());

		buffer.append(Constants.NEWLINE);
		buffer.append(Constants.INDENT);
		buffer.append("Application: ");
		buffer.append(application_.toString());

		buffer.append(Constants.NEWLINE);
		buffer.append(Constants.INDENT);
		buffer.append("Object ID (DER, hex): ");
		buffer.append(objectID_.toString());

		buffer.append(Constants.NEWLINE);
		buffer.append(Constants.INDENT);
		buffer.append("Value (hex): ");
		buffer.append(value_.toString());

		return buffer.toString();
	}

}