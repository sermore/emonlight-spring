package net.reliqs.emonlight.xbeegw.send;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;
import net.reliqs.emonlight.xbeegw.config.Server;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryServiceFactory;
import net.reliqs.emonlight.xbeegw.xbee.data.ProbeData;
import net.reliqs.emonlight.xbeegw.xbee.state.GlobalState;

@Component
public class Dispatcher {
	private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

	private final Map<Server, DeliveryService> clients;
	private final GlobalState globalState;

	@Autowired
	public Dispatcher(final Settings settings, GlobalState globalState, DeliveryServiceFactory rsFactory,
			KafkaUtils ku) {
		this.globalState = globalState;
		clients = new HashMap<>();
		if (settings.getServers().stream().anyMatch(Server::isKafkaEnabled)) {
			List<String> topics = settings.getKafkaTopics();

			ku.initTopics(topics);
		}
		settings.getServers().forEach(s -> clients.put(s, rsFactory.getSendService(s)));
	}

	private void collect() {
		Set<ProbeData> toClear = new HashSet<>();
		clients.entrySet().forEach(e -> {
			Server s = e.getKey();
			DeliveryService ds = e.getValue();
			ds.addInit(s.getName());
			s.getMaps().forEach(sm -> {
				ProbeData pd = globalState.getProbeData(sm.getProbe());
				if (!pd.isEmpty()) {
					toClear.add(pd);
					ds.add(sm.getNodeId(), sm.getApiKey(), pd.getIterator());
				}
			});
			ds.addComplete();
		});
		// clear queues in ProbeData
		toClear.forEach(p -> p.clear());
		toClear.clear();
		log.trace(clients.entrySet().stream().map(e -> e.getKey().getName() + ":" + e.getValue())
				.collect(Collectors.joining(", ")));
	}

	public void process() {
		collect();
		clients.values().stream().filter(DeliveryService::isReady).forEach(DeliveryService::post);
	}
}
