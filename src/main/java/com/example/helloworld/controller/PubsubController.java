package com.example.helloworld.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import com.google.cloud.pubsub.v1.AckReplyConsumer;

@RestController
public class PubsubController {
	private static final String PROJECT_ID = System.getenv("PROJECT_ID");
	private static final String TOPIC_ID = System.getenv("TOPIC_ID");
	private static final String SUB_ID = System.getenv("SUB_ID");

	@GetMapping("/pub")
	String pubMessage(String message) throws SQLException, IOException, ExecutionException, InterruptedException {
		System.out.println("input message : " + message);
		if (message != null) {
			publisher(message);
		}
		return "successed";
	}

	public void publisher(String message) throws IOException, ExecutionException, InterruptedException {
		TopicName topicName = TopicName.of(PROJECT_ID, TOPIC_ID);

		Publisher publisher = null;
		try {
			// Create a publisher instance with default settings bound to the topic
			publisher = Publisher.newBuilder(topicName).build();

			ByteString data = ByteString.copyFromUtf8(message);
			PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

			// Once published, returns a server-assigned message id (unique within the
			// topic)
			ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
			String messageId = messageIdFuture.get();
			System.out.println("Published message ID: " + messageId);
		} finally {
			if (publisher != null) {
				// When finished with the publisher, shutdown to free up resources.
				publisher.shutdown();
				publisher.awaitTermination(1, TimeUnit.MINUTES);
			}
		}
		System.out.println("finished");
	}

	@GetMapping("/sub")
	String subMessage(String message) throws SQLException, IOException, ExecutionException, InterruptedException {
		System.out.println("start recevie from subscription");
		subscribeAsync();
		return "successed";
	}

	public static void subscribeAsync() {
		ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(PROJECT_ID, SUB_ID);

		// Instantiate an asynchronous message receiver.
		MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
			// Handle incoming message, then ack the received message.
			System.out.println("Id: " + message.getMessageId());
			System.out.println("message: " + message.getData().toString());
			System.out.println("Data: " + message.getData().toStringUtf8());
			consumer.ack();
		};

		Subscriber subscriber = null;
		try {
			subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
			// Start the subscriber.
			subscriber.startAsync().awaitRunning();
			System.out.printf("Listening for messages on %s:\n", subscriptionName.toString());
			// Allow the subscriber to run for 30s unless an unrecoverable error occurs.
			subscriber.awaitTerminated(30, TimeUnit.SECONDS);
		} catch (TimeoutException timeoutException) {
			// Shut down the subscriber after 30s. Stop receiving messages.
			subscriber.stopAsync();
		}
	}
}
