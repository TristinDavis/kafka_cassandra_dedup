package com.devon.demo.pojo_approch;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.adapter.FilteringAcknowledgingMessageListenerAdapter;
import org.springframework.kafka.listener.adapter.RetryingAcknowledgingMessageListenerAdapter;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Devon on 3/18/2017.
 */
public class KafkaPOJOConfig {

  public ConcurrentMessageListenerContainer<Integer, String> createContainer(
          ContainerProperties containerProps, IKafkaConsumer iKafkaConsumer) {

    Map<String, Object> props = consumerProps();

    DefaultKafkaConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<Integer, String>(props);


    CustomKafkaMessageListener ckml = new CustomKafkaMessageListener(iKafkaConsumer);
    CustomRecordFilter cff = new CustomRecordFilter();
    FilteringAcknowledgingMessageListenerAdapter faml = new FilteringAcknowledgingMessageListenerAdapter(ckml,cff,true);
    RetryTemplate rt = new RetryTemplate();

    FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
    fixedBackOffPolicy.setBackOffPeriod(1000);
    rt.setBackOffPolicy(fixedBackOffPolicy);

    RetryingAcknowledgingMessageListenerAdapter rml = new RetryingAcknowledgingMessageListenerAdapter(faml,rt);

    containerProps.setMessageListener(rml);
    containerProps.setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE);
    containerProps.setErrorHandler(ckml);
    ConcurrentMessageListenerContainer<Integer, String> container = new ConcurrentMessageListenerContainer<>(cf, containerProps);
    container.setConcurrency(10);
    return container;
  }


/*public ConcurrentKafkaListenerContainerFactory<Integer, String> factory (ContainerProperties containerProps, IKafkaConsumer iKafkaConsumer){
    ConcurrentKafkaListenerContainerFactory concurrentKafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory();
    Map<String, Object> props = consumerProps();

    DefaultKafkaConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<Integer, String>(props);
    concurrentKafkaListenerContainerFactory.setConsumerFactory(cf);
    concurrentKafkaListenerContainerFactory.setConcurrency(10);
    CustomKafkaMessageListener ckml = new CustomKafkaMessageListener(iKafkaConsumer);
    concurrentKafkaListenerContainerFactory.getContainerProperties().setMessageListener(ckml);
    concurrentKafkaListenerContainerFactory.setAutoStartup(true);


    return concurrentKafkaListenerContainerFactory;
}*/

  public KafkaTemplate<Integer, String> createTemplate() {
    Map<String, Object> senderProps = senderProps();
    ProducerFactory<Integer, String> pf =
        new DefaultKafkaProducerFactory<Integer, String>(senderProps);
    KafkaTemplate<Integer, String> template = new KafkaTemplate<>(pf);
    return template;
  }

  private Map<String, Object> consumerProps() {
    Map<String, Object> props = new HashMap<>();
//    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.28:9092");
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.143.138:9092");

    props.put(ConsumerConfig.GROUP_ID_CONFIG, "pojokafka");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    //props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return props;
  }

  private Map<String, Object> senderProps() {
    Map<String, Object> props = new HashMap<>();
//    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.28:9092");
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.143.138:9092");

    props.put(ProducerConfig.RETRIES_CONFIG, 0);
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
    props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return props;
  }
}
