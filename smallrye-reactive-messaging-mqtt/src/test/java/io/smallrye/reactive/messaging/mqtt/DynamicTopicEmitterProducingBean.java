package io.smallrye.reactive.messaging.mqtt;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Publisher;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;

@ApplicationScoped
public class DynamicTopicEmitterProducingBean {

    private final List<String> topics = new ArrayList<>(10);

    @Inject
    @Channel("sink")
    MutinyEmitter<String> emitter;

    @Incoming("dyn-data")
    public Uni<Void> process(Message<Integer> input) {
        String topic = "T" + input.getPayload();
        topics.add(topic);
        return emitter.sendMessage(MqttMessage.of(topic, input.getPayload().toString(), MqttQoS.AT_LEAST_ONCE, false))
                .chain(() -> Uni.createFrom().completionStage(input::ack));
    }

    @Outgoing("dyn-data")
    public Publisher<Integer> source() {
        return Multi.createFrom().range(0, 10);
    }

    public List<String> getTopics() {
        return topics;
    }
}
