package xdi2.connect.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.util.XDIClientUtil;
import xdi2.core.features.signatures.RSASignature;
import xdi2.core.security.signature.create.RSAStaticPrivateKeySignatureCreator;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.constants.XDIMessagingConstants;

public class ConnectionRequest {

	private static final XDIAddress XDI_ADD_SHORT = XDIAddress.create("<#short>");

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

		String returnUri = message.getParameterString(XDIMessagingConstants.XDI_ADD_MESSAGE_PARAMETER_RETURN_URI);
		if (returnUri == null) return null;

		return URI.create(returnUri);
	}

	public void setReturnUri(URI returnUri) throws URISyntaxException {

		for (Message message : this.getMessageEnvelope().getMessages()) {

			message.setParameter(XDIMessagingConstants.XDI_ADD_MESSAGE_PARAMETER_RETURN_URI, returnUri.toString());
		}
	}

	public Boolean getShort() throws URISyntaxException {

		if (! this.getMessageEnvelope().getMessages().hasNext()) return null;

		Message message = this.getMessageEnvelope().getMessages().next();
		if (message == null) return null;

		Boolean zhort = message.getParameterBoolean(XDI_ADD_SHORT);
		if (zhort == null) return null;

		return zhort;
	}

	public void setShort(Boolean zhort) throws URISyntaxException {

		for (Message message : this.getMessageEnvelope().getMessages()) {

			message.setParameter(XDI_ADD_SHORT, zhort);
		}
	}

	public void sign(CloudName cloudName, String secretToken) throws Xdi2ClientException, GeneralSecurityException {

		// obtain private key

		XDIDiscoveryResult xdiDiscoveryResult = XDIDiscoveryClient.XDI2_DISCOVERY_CLIENT.discoverFromRegistry(cloudName.getXDIAddress());
		if (xdiDiscoveryResult == null || xdiDiscoveryResult.getCloudNumber() == null || xdiDiscoveryResult.getXdiEndpointUri() == null) throw new Xdi2ClientException("Discovery failed on " + cloudName);

		CloudNumber cloudNumber = xdiDiscoveryResult.getCloudNumber();
		URI xdiEndpointUri = xdiDiscoveryResult.getXdiEndpointUri();

		String privateKeyString = XDIClientUtil.retrieveSignaturePrivateKey(cloudNumber, xdiEndpointUri, secretToken);
		if (privateKeyString == null) throw new Xdi2ClientException("No private key for " + cloudNumber);
		RSAPrivateKey privateKey = RSAStaticPrivateKeySignatureCreator.rsaPrivateKeyFromPrivateKeyString(privateKeyString);

		// sign messages

		for (Message message : this.getMessageEnvelope().getMessages()) {

			RSASignature signature = (RSASignature) message.createSignature(RSASignature.DIGEST_ALGORITHM_SHA, 256, RSASignature.KEY_ALGORITHM_RSA, 2048, true);
			new RSAStaticPrivateKeySignatureCreator(privateKey).createSignature(signature);
		}
	}
}
