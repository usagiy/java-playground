package hr.zlatko.rabbitmq;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;



import akka.http.javadsl.model.StatusCode;

public class Messages {

	public static class DMMessageBase implements Serializable{
		protected String messageId;
		
		public DMMessageBase() {
			this.messageId = UUID.randomUUID().toString();
		}
		
		public DMMessageBase(String messageId) {
			this.messageId = messageId;
		}

		public String getMessageId() {
			return messageId;
		}
	}
	
	
	public static final class ResponseMessage extends DMMessageBase{
		
		private final StatusCode statusCode;
		private final Optional<String> responseBody;
		
		public ResponseMessage(StatusCode statusCode) {
			super();
			this.statusCode = statusCode;
			this.responseBody = Optional.empty();
		}
		
		public ResponseMessage(StatusCode statusCode, String messageId) {
			super(messageId);
			this.statusCode = statusCode;
			this.responseBody = Optional.empty();
		}
		
		public ResponseMessage(StatusCode statusCode, String messageId, Optional<String> responseBody) {
			super(messageId);
			this.statusCode = statusCode;
			this.responseBody = responseBody;
		}

		public StatusCode getStatusCode() {
			return statusCode;
		}

		public Optional<String> getResponseBody() {
			return responseBody;
		}		
	}
	
	
	public static final class QueueAck{
		
		 private final long deliveryTag;
		 
		 public QueueAck(long deliveryTag) {
			this.deliveryTag = deliveryTag;
		}
	
		public long getDeliveryTag() {
			return deliveryTag;
		}
		 
	}


	public static  class QueueNoAck{
		
		 private final long deliveryTag;
		 
		 public QueueNoAck(long deliveryTag) {
			this.deliveryTag = deliveryTag;
		}
	
		public long getDeliveryTag() {
			return deliveryTag;
		}		 
	}
	
		
	public static final class QueueNoAckDelayed {
		
		private final long deliveryTag;
		
		public QueueNoAckDelayed(QueueNoAck msg) {
			this.deliveryTag = msg.getDeliveryTag();
		}
		
		public long getDeliveryTag() {
			return deliveryTag;
		}
		
	}
}
