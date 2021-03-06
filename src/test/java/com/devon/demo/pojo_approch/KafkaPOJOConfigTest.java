package com.devon.demo.pojo_approch;

import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Created by Devon on 3/18/2017.
 */
public class KafkaPOJOConfigTest implements IKafkaConsumer {

  private Logger         log   = LoggerFactory.getLogger(KafkaPOJOConfigTest.class);
  final   CountDownLatch latch = new CountDownLatch(3);

  @Test
  public void producer_1() {
    KafkaPOJOConfig                kconfig  = new KafkaPOJOConfig();
    KafkaTemplate<Integer, String> template = kconfig.createTemplate();
    template.setDefaultTopic("dedup");
//    template.sendDefault(0, "foo1");
    for(int x = 0 ; x <1; x++){
   //   GenericMessage<String> msg = new GenericMessage<String>("test"+x);
      //template.send(msg);
       template.sendDefault(x, "foo77");

    }


    template.flush();
  }


  @Test
  public void consumer_1() throws Exception {
   // URL u = new URL("192.168.0.28:9092");
  //  ConsumerGroupCommand.main(new String[] {"--bootstrap-server","192.168.0.28:9092","--describe","--group","pojokafka"});

  }


  @Override
  public void getEvent(String str) {
    log.info("================ {}", str);
    latch.countDown();
  }
}


