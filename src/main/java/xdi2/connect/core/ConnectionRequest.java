package xdi2.connect.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.util.XDIClientUtil;
import xdi2.core.ContextNode;
import xdi2.core.features.signatures.KeyPairSignature;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;

public class ConnectionRequest {

	private static final XDIAddress XDI_ADD_RETURN_URI = XDIAddress.create("<#return><$uri>");

	private MessageEnvelope messageEnvelope;

	private ConnectionRequest(MessageEnvelope messageEnvelope) {

		if (messageEnvelope == null) throw new NullPointerException();

		this.messageEnvelope = messageEnvelope;
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a message envelope is a valid Connection Request.
	 * @param messageEnvelope The message envelope to check.
	 * @return True if the message envelope is a valid Connection Request.
	 */
	public static boolean isValid(MessageEnvelope messageEnvelope) {

		if (messageEnvelope == null) throw new NullPointerException();

		return true;
	}

	/**
	 * Factory method that creates a Connection Request bound to a given message envelope.
	 * @param messageEnvelope The message envelope that is a Connection Request.
	 * @return The Connection Request.
	 */
	public static ConnectionRequest fromMessageEnvelope(MessageEnvelope messageEnvelope) {

		if (messageEnvelope == null) throw new NullPointerException();

		if (! isValid(messageEnvelope)) return null;

		return new ConnectionRequest(messageEnvelope);
	}

	/*
	 * Instance methods
	 */

	public MessageEnvelope getMessageEnvelope() {

		return this.messageEnvelope;
	}

	public URI getReturnUri() throws URISyntaxException {

		if (! this.getMessageEnvelope().getMessages().hasNext()) return null;

		Message message = this.getMessageEnvelope().getMessages().next();
		if (message == null) return null;

		ContextNode contextNode = message.getContextNode().getDeepContextNode(XDI_ADD_RETURN_URI);
		if (contextNode == null) return null;

		String literalDataString = contextNode.getLiteralDataString();
		if (literalDataString == null) return null;

		return new URI(literalDataString);
	}

	public void setReturnUri(URI returnUri) throws URISyntaxException {

		for (Message message : this.getMessageEnvelope().getMessages()) {

			ContextNode contextNode = message.getContextNode().setDeepContextNode(XDI_ADD_RETURN_URI);
			contextNode.setLiteralString(returnUri.toString());
		}
	}

	public void sign(CloudName cloudName, String secretToken) throws Xdi2ClientException, GeneralSecurityException {

		// obtain private key

		XDIDiscoveryResult xdiDiscoveryResult = XDIDiscoveryClient.DEFAULT_DISCOVERY_CLIENT.discoverFromRegistry(cloudName.getXDIAddress(), null);
		if (xdiDiscoveryResult == null || xdiDiscoveryResult.getCloudNumber() == null || xdiDiscoveryResult.getXdiEndpointUrl() == null) throw new Xdi2ClientException("Discovery failed on " + cloudName);

		CloudNumber cloudNumber = xdiDiscoveryResult.getCloudNumber();
		URL xdiEndpointUrl = xdiDiscoveryResult.getXdiEndpointUrl();

		PrivateKey privateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloudNumber, xdiEndpointUrl, secretToken);

		// sign messages

		for (Message message : this.getMessageEnvelope().getMessages()) {

			KeyPairSignature signature = (KeyPairSignature) message.createSignature(KeyPairSignature.DIGEST_ALGORITHM_SHA, 256, KeyPairSignature.KEY_ALGORITHM_RSA, 2048, true);
			signature.sign(privateKey);
		}
	}
}
