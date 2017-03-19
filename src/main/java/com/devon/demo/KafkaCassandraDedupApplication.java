package com.devon.demo;

import com.devon.demo.cassandra.DedupRepository;
import com.devon.demo.cassandra.DedupTable;
import com.devon.demo.pojo_approch.IKafkaConsumer;
import com.devon.demo.pojo_approch.KafkaPOJOConfig;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.TopicPartitionInitialOffset;

@SpringBootApplication
@EnableCassandraRepositories(basePackages = {"com.devon.demo.cassandra"})

public class KafkaCassandraDedupApplication implements IKafkaConsumer, ApplicationContextAware {


  private static ApplicationContext applicationContext;
  private Logger                         log = LoggerFactory
      .getLogger(KafkaCassandraDedupApplication.class);
  static  KafkaCassandraDedupApplication k   = new KafkaCassandraDedupApplication();

  public static void main(String[] args) throws InterruptedException {
    SpringApplication.run(KafkaCassandraDedupApplication.class, args);

    DedupRepository  dedupRepository = (DedupRepository) KafkaCassandraDedupApplication
        .getApplicationContext().getBean("dedupRepository");

    List<DedupTable> listDedupTable  = dedupRepository.findAll();

    KafkaPOJOConfig kconfig = new KafkaPOJOConfig();

    TopicPartitionInitialOffset[] topic1PartitionS = new TopicPartitionInitialOffset[listDedupTable.size()];
    for(int x = 0 ; x < listDedupTable.size(); x++){
      topic1PartitionS[x] = new TopicPartitionInitialOffset(listDedupTable.get(x).getTopicName(),listDedupTable.get(x).getPartition(),listDedupTable.get(x).getOffset()+1);
    }

    ContainerProperties containerProps = new ContainerProperties("dedup");

    ConcurrentMessageListenerContainer<Integer, String> container = kconfig
        .createContainer(containerProps, k);
    container.start();
   /* Thread.sleep(5000);
    container.stop();*/
  }

  @Override
  public void getEvent(String str) {
    log.info("================ {}", str);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

}
