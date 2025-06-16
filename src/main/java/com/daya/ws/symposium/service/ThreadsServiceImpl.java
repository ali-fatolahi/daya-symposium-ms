package com.daya.ws.symposium.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.daya.symposium.core.ThreadCreatedEvent;
import com.daya.ws.symposium.rest.CreateNewThreadRestModel;

@Service
public class ThreadsServiceImpl implements ThreadsService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ThreadsServiceImpl.class);
  private KafkaTemplate<String, ThreadCreatedEvent> kafkaTemplate;

  public ThreadsServiceImpl(final KafkaTemplate<String, ThreadCreatedEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public String createNewThread(CreateNewThreadRestModel createNewThreadRestModel) throws Exception {
    final String threadId = UUID.randomUUID().toString();

    // TODO: persistence logic to be added ...
    final ThreadCreatedEvent threadCreatedEvent = new ThreadCreatedEvent(
      threadId,
      createNewThreadRestModel.getTitle(),
      createNewThreadRestModel.getBody(),
      createNewThreadRestModel.getTotalAwards(),
      createNewThreadRestModel.getAwardValue());

    final ProducerRecord<String, ThreadCreatedEvent> record =
      new ProducerRecord<String,ThreadCreatedEvent>(
        "daya-symposium-threads", threadId, threadCreatedEvent);
    record.headers().add("messageId", UUID.randomUUID().toString().getBytes()); 
    
    CompletableFuture<SendResult<String, ThreadCreatedEvent>> future =
     kafkaTemplate.send(record);
    future.whenComplete((result, exception) -> {
      if (exception != null) {
        LOGGER.error("Thread created event failed to publish", exception);
      } else {
        LOGGER.info("Thread created event published successfully: {}", result.getRecordMetadata());
      }
    });

    // make it a blocking call
    future.join();

    // blocking until publish is done
    try {
      LOGGER.info("About to publishing a ThreadCreatedEvent ...");

      SendResult<String, ThreadCreatedEvent> result =
       kafkaTemplate.send("daya-symposium-threads", threadId, threadCreatedEvent).get();

      // these aren't publishing anything
      LOGGER.info("Partition: ", result.getRecordMetadata().partition()); 
      LOGGER.info("Topic: ", result.getRecordMetadata().topic()); 
      LOGGER.info("Offset: ", result.getRecordMetadata().offset());

      LOGGER.info("ThreadCreatedEvent published successfully: {}", result.getRecordMetadata());
    } catch (Exception e) {
      LOGGER.error("ThreadCreatedEvent failed to publish.", e);
      throw e;
    }
    
    LOGGER.info("Done creating new thread {}", threadId);
    return threadId;
  }
}
