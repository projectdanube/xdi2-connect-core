package xdi2.connect.core;

import java.util.Iterator;

import xdi2.core.features.linkcontracts.LinkContracts;
import xdi2.core.features.linkcontracts.instance.LinkContract;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.util.GraphUtil;
import xdi2.messaging.MessageResult;

public class ConnectionResult {

	private MessageResult messageResult;

	private ConnectionResult(MessageResult messageResult) {

		if (messageResult == null) throw new NullPointerException();

		this.messageResult = messageResult;
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a message result is a valid Connect result.
	 * @param messageResult The message result to check.
	 * @return True if the message result is a valid Connect result.
	 */
	public static boolean isValid(MessageResult messageResult) {

		if (messageResult == null) throw new NullPointerException();

		return true;
	}

	/**
	 * Factory method that creates a Connect result bound to a given message result.
	 * @param messageResult The message result that is a Connect result.
	 * @return The Connect result.
	 */
	public static ConnectionResult fromContextNode(MessageResult messageResult) {

		if (messageResult == null) throw new NullPointerException();

		if (! isValid(messageResult)) return null;

		return new ConnectionResult(messageResult);
	}

	/*
	 * Instance methods
	 */

	public MessageResult getMessageResult() {

		return this.messageResult;
	}

	public CloudNumber getCloudNumber() {

		XDIAddress ownerXDIAddress = GraphUtil.getOwnerXDIAddress(this.getMessageResult().getGraph());
		if (ownerXDIAddress == null) return null;

		return CloudNumber.fromXDIAddress(ownerXDIAddress);
	}

	public Iterator<LinkContract> getLinkContracts() {

		return LinkContracts.getAllLinkContracts(this.getMessageResult().getGraph());
	}
}
