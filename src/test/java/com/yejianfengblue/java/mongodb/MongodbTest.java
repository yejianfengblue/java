package com.yejianfengblue.java.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
class MongodbTest {

    // https://mongodb.github.io/mongo-java-driver/3.12/driver/getting-started/quick-start/
    @Test
    void connect() {

        // Creates a new client with the default connection string "mongodb://localhost"
        MongoClient localMongoClient = MongoClients.create();
        log.info("Local MongoDB database names: {}",
                StreamSupport.stream(localMongoClient.listDatabaseNames().spliterator(), false)
                        .collect(Collectors.toList())
        );

        MongoClient remoteMongoClient = MongoClients.create(
                "mongodb+srv://chris:chris@cluster0-djqya.mongodb.net/test?retryWrites=true&w=majority");
        log.info("Remote MongoDB database names: {}",
                StreamSupport.stream(remoteMongoClient.listDatabaseNames().spliterator(), false)
                        .collect(Collectors.toList())
        );
        MongoDatabase database = remoteMongoClient.getDatabase("learn");

    }
}
