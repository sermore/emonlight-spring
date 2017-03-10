package net.reliqs.emonlight.xbeegw.xbee;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.reliqs.emonlight.xbeegw.GwException;
import net.reliqs.emonlight.xbeegw.config.Node;

/**
 * Verify the configuration received by the xbee.
 * 
 * If the received configuration doesn't match, a new configuration data is sent back to the xbee.
 * In case the configuration is matching, an acknowledge message is sent back.
 *  
 * This message is sent by the xbee on every startup, and the xbee waits for the response before enterning the main loop.  
 * 
 * @author sergio
 *
 */
class ConfigurationProcessor extends MessageProcessor {
	private static final Logger log = LoggerFactory.getLogger(ConfigurationProcessor.class);

	ConfigurationProcessor(Processor processor) {
		super(processor);
	}

	@Override
	public void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in) {
		Node node = ns.getNode();
		log.debug("{}: process Configuration", node);
		if (!ns.acceptConfigurationMessage(m.time))
			throw new GwException(String.format("too much device configurations for %s, give up.", this));
		DeviceConfig deviceCfg = new DeviceConfig(in);
		DeviceConfig storedCfg = new DeviceConfig(ns);
		if (!deviceCfg.equals(storedCfg)) {
			sendDeviceConfiguration(ns, storedCfg);
		} else {
			sendOK(ns);
		}
		log.debug("{}: process Configuration complete", node);
	}

}
