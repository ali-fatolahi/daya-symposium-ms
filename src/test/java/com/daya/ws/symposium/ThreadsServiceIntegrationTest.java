package com.daya.ws.symposium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.daya.symposium.core.ThreadCreatedEvent;
import com.daya.ws.symposium.rest.CreateNewThreadRestModel;
import com.daya.ws.symposium.service.ThreadsService;

import kafka.utils.Json;

@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 3, count=3, controlledShutdown=true)
@SpringBootTest(properties="spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ThreadsServiceIntegrationTest {
  @Autowired
  private ThreadsService threadsService;

  @Autowired
  private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired
  private Environment environment;

  private KafkaMessageListenerContainer<String, ThreadCreatedEvent> container;
  private BlockingQueue<ConsumerRecord<String, ThreadCreatedEvent>> records;

  @BeforeAll
  void setUp() {
    DefaultKafkaConsumerFactory<String, Object> consumerFactory =
      new DefaultKafkaConsumerFactory<>(getConsumerProperties());

    ContainerProperties containerProperties =
      new ContainerProperties(environment.getProperty("daya-symposium-threads-topic-name"));
    container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
    records = new LinkedBlockingQueue<>();

    container.setupMessageListener((MessageListener<String, ThreadCreatedEvent>) records::add);
    container.start();
    ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    assertEquals(3, container.getAssignedPartitions().size());
  }

  @Test
  void testCreateNewThread_whenValidRequest_thenKafakMessageSent() throws Exception {
    String title = "iPhone 15 Pro Max";
    String body = "I just bought the new iPhone 15 Pro Max and it's amazing";
    Integer totalAwards = 100;
    BigDecimal awardValue = BigDecimal.valueOf(10.00);

    CreateNewThreadRestModel createNewThreadRestModel = new CreateNewThreadRestModel();
    createNewThreadRestModel.setTitle(title);
    createNewThreadRestModel.setBody(body);
    createNewThreadRestModel.setTotalAwards(totalAwards);
    createNewThreadRestModel.setAwardValue(awardValue);

    threadsService.createNewThread(createNewThreadRestModel);

    ConsumerRecord<String, ThreadCreatedEvent> message = records.poll(3000, TimeUnit.MILLISECONDS);
    assertNotNull(message);
    assertNotNull(message.key());
    ThreadCreatedEvent event = message.value();
    assertEquals(createNewThreadRestModel.getAwardValue(), event.getAwardValue());
    assertEquals(createNewThreadRestModel.getBody(), event.getBody());
    assertEquals(createNewThreadRestModel.getTitle(), event.getTitle());
    assertEquals(createNewThreadRestModel.getTotalAwards(), event.getTotalAwards());
  }

  private Map<String, Object> getConsumerProperties() {
    return Map.of(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
      ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
      ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"),
      JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.trusted.packages"),
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset")
    );
  }

  @AfterAll
  void tearDown() {
    container.stop();
  }
}
