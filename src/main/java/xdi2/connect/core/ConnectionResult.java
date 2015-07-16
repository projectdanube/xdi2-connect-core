package xdi2.connect.core;

import java.util.Iterator;

import xdi2.core.features.linkcontracts.LinkContracts;
import xdi2.core.features.linkcontracts.instance.LinkContract;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.util.GraphUtil;
import xdi2.messaging.response.MessagingResponse;

public class ConnectionResult {

	private MessagingResponse messagingResponse;

	private ConnectionResult(MessagingResponse messagingResponse) {

		if (messagingResponse == null) throw new NullPointerException();

		this.messagingResponse = messagingResponse;
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a messaging response is a valid Connect result.
	 * @param messagingResponse The messaging response to check.
	 * @return True if the messaging response is a valid Connect result.
	 */
	public static boolean isValid(MessagingResponse messagingResponse) {

		if (messagingResponse == null) throw new NullPointerException();

		return true;
	}

	/**
	 * Factory method that creates a Connect result bound to a given messaging response.
	 * @param messagingResponse The messaging response that is a Connect result.
	 * @return The Connect result.
	 */
	public static ConnectionResult fromContextNode(MessagingResponse messagingResponse) {

		if (messagingResponse == null) throw new NullPointerException();

		if (! isValid(messagingResponse)) return null;

		return new ConnectionResult(messagingResponse);
	}

	/*
	 * Instance methods
	 */

	public MessagingResponse getMessagingResponse() {

		return this.messagingResponse;
	}

	public CloudNumber getCloudNumber() {

		XDIAddress ownerXDIAddress = GraphUtil.getOwnerXDIAddress(this.getMessagingResponse().getResultGraph());
		if (ownerXDIAddress == null) return null;

		return CloudNumber.fromXDIAddress(ownerXDIAddress);
	}

	public Iterator<LinkContract> getLinkContracts() {

		return LinkContracts.getAllLinkContracts(this.getMessagingResponse().getResultGraph());
	}
}
