package hr.zlatko.actor;

import java.util.concurrent.CompletionStage;
//import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.OutgoingConnection;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class ReactiveStreamProcess {
	
	private final static Logger logger = LoggerFactory.getLogger(ReactiveStreamProcess.class);

	static final ActorSystem system = ActorSystem.create("ReactiveStreamProcess");
	static final Materializer materializer = ActorMaterializer.create(system);
	
	
	public static class Post {
		
		private String userId;
		private String id;
		private String title;
		private String body;
		@Override
		public String toString() {
			return "Post [userId=" + userId + ", id=" + id + ", title=" + title + ", body=" + body + "]";
		}
	}
	
	
	
	
	
	
	
	
	//Imao sam gresku 
	//akka.http.scaladsl.model.IllegalResponseException: Response reason phrase exceeds the configured limit of 64 characters
	//https://groups.google.com/forum/#!topic/akka-user/KqfZ18SWN78
	
	
	
	public static void streamProcess(){
		Source<HttpRequest, NotUsed> src = Source.single(HttpRequest.create().withUri("/posts/1").withMethod(HttpMethods.GET));
		Flow<HttpRequest, HttpResponse, CompletionStage<OutgoingConnection>> conn = 
				Http.get(system).outgoingConnection(ConnectHttp.toHost("http://jsonplaceholder.typicode.com", 80));		
		Unmarshaller<HttpEntity, Post> unmarshaller = Unmarshaller.entityToString().thenApply(resp ->  new ObjectMapper().convertValue(resp, Post.class));		
		Flow<HttpResponse, CompletionStage<Post>, NotUsed> fl = Flow.of(HttpResponse.class).map(resp -> unmarshaller.unmarshal(resp.entity(), materializer));
		Function<HttpResponse, CompletionStage<Post>> toPos = resp ->  unmarshaller.unmarshal(resp.entity(), materializer);
		
		
		Flow<HttpResponse, Post, NotUsed> fl2 = Flow.of(HttpResponse.class).mapAsync(1, resp -> unmarshaller.unmarshal(resp.entity(), materializer));
		
		Sink<Post, ?> writePost = Sink.foreach(s -> logger.debug("Got output {}",s)); // Sink.foreach(System.out::println);
		
		//Sink<CompletionStage<Post>, NotUsed> writePost1 = Sink.foreach(cs -> cs.thenAccept(p -> logger.debug(p)));
		//src.via(conn).mapAsync(1, toPos).runWith(writePost, materializer);
		
		src.via(conn).via(fl2).runWith(writePost, materializer);
		
		logger.debug("blaaaaa");
	
	}

	
	
	
	public static void main(String[] args) {
		streamProcess();
	}
	
	
}
