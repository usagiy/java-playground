package hr.zlatko.actor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
//import java.util.function.Function;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.Done;
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
import akka.japi.Pair;
import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.ClosedShape;
import akka.stream.FanInShape2;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Zip;
import akka.stream.scaladsl.RunnableGraph;
import akka.util.ByteString;

public class ReactiveStreamProcess {
	
	private final static Logger logger = LoggerFactory.getLogger(ReactiveStreamProcess.class);

	static final ActorSystem system = ActorSystem.create("ReactiveStreamProcess");
	static final Materializer materializer = ActorMaterializer.create(system);
	
	
	public static class Post {
		
		public Post() {
			// TODO Auto-generated constructor stub
		}
		
		@JsonCreator
		public Post(@JsonProperty("userId") String userId,
					@JsonProperty("id") String id,
					@JsonProperty("title") String title,
					@JsonProperty("body") String body) 
		{
			this.userId = userId;
			this.id = id;
			this.title=title;
			this.body = body;
		}
		
		private String userId;
		private String id;
		private String title;
		private String body;
		@Override
		public String toString() {
			return "Post [userId=" + userId + ", id=" + id + ", title=" + title + ", body=" + body + "]";
		}

		public String getUserId() {
			return userId;
		}

		public String getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public String getBody() {
			return body;
		}
	}
	
	
	public static class Photo {
	
		private String albumId;
		private String id;
		private String title;
		private String url;
		private String thumbnailUrl;
		
		@JsonCreator
		public Photo(@JsonProperty("albumId") String albumId,
						@JsonProperty("id") String id,
						@JsonProperty("title") String title,
						@JsonProperty("url") String url,
						@JsonProperty("thumbnailUrl") String thumbnailUrl) {
			this.albumId = albumId;
			this.id = id;
			this.title = title;
			this.url = url;
			this.thumbnailUrl = thumbnailUrl;
		}

		public String getAlbumId() {
			return albumId;
		}

		public String getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public String getUrl() {
			return url;
		}

		public String getThumbnailUrl() {
			return thumbnailUrl;
		}

		@Override
		public String toString() {
			return "Photo [albumId=" + albumId + ", id=" + id + ", title=" + title + ", url=" + url + ", thumbnailUrl="
					+ thumbnailUrl + "]";
		}
				
	}
	
	
	
	
	
	
	
	
	
	
	
	
	//Imao sam gresku 
	//akka.http.scaladsl.model.IllegalResponseException: Response reason phrase exceeds the configured limit of 64 characters
	//https://groups.google.com/forum/#!topic/akka-user/KqfZ18SWN78
	
	
	public static <T> Unmarshaller<HttpEntity, T> createUnmarshaller(Class<T> clazz){
		return Unmarshaller.entityToString().
					thenApply(resp -> {
						ObjectMapper o =  new ObjectMapper();
					try {
						return o.readValue(resp, clazz);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error("Error {}", e.getMessage());
						return null;
					}
				});	
	}
	
	
	public static <T> Unmarshaller<HttpEntity, T> createUnmarshaller(TypeReference<T> clazz){
		return Unmarshaller.entityToString().
					thenApply(resp -> {
						ObjectMapper o =  new ObjectMapper();
					try {
						return o.readValue(resp, clazz);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error("Error {}", e.getMessage());
						return null;
					}
				});	
	}
	
	
	
	public static CompletionStage<IOResult> saveFileToDisc(String folderName, String fileName, HttpResponse response) throws IOException{
		//File file = new File(folderName, fileName);
		//file.createNewFile();
		return response.entity().getDataBytes().runWith(FileIO.toPath(Paths.get(folderName + fileName)), materializer);
	}
	
	
	
	
	
	
	
	
	
	public static void streamProcess1(){
		Source<HttpRequest, NotUsed> src = Source.single(HttpRequest.create().withUri("/posts/1").withMethod(HttpMethods.GET));
		Flow<HttpRequest, HttpResponse, CompletionStage<OutgoingConnection>> conn = 
				Http.get(system).outgoingConnection(ConnectHttp.toHost("http://jsonplaceholder.typicode.com", 80));		
		
		//Unmarshaller<HttpEntity, Post> unmarshaller = Unmarshaller.entityToString().thenApply(resp ->  new ObjectMapper().convertValue(resp, Post.class));		
		
		Unmarshaller<HttpEntity, Post> unmarshaller = createUnmarshaller(Post.class);
		Flow<HttpResponse, CompletionStage<Post>, NotUsed> fl = Flow.of(HttpResponse.class).map(resp -> unmarshaller.unmarshal(resp.entity(), materializer));
		Function<HttpResponse, CompletionStage<Post>> toPos = resp ->  unmarshaller.unmarshal(resp.entity(), materializer);
		
		
		Flow<HttpResponse, Post, NotUsed> fl2 = Flow.of(HttpResponse.class).mapAsync(1, resp -> unmarshaller.unmarshal(resp.entity(), materializer));
		
		Sink<Post, ?> writePost = Sink.foreach(s -> logger.debug("Got output {}",s)); // Sink.foreach(System.out::println);
		
		//Sink<CompletionStage<Post>, NotUsed> writePost1 = Sink.foreach(cs -> cs.thenAccept(p -> logger.debug(p)));
		//src.via(conn).mapAsync(1, toPos).runWith(writePost, materializer);
		
		src.via(conn).via(fl2).runWith(writePost, materializer);
		
		logger.debug("blaaaaa");	
	}
	
	
	
	
	
	public static void downloadOddPictures(){
		//session creation
		Source<HttpRequest, NotUsed> startSession = Source.single(HttpRequest.create().withUri("/posts/1").withMethod(HttpMethods.GET));
		Flow<HttpRequest, HttpResponse, CompletionStage<OutgoingConnection>> createConnection = 
				Http.get(system).outgoingConnection(ConnectHttp.toHost("http://jsonplaceholder.typicode.com", 80));
		
		Flow<HttpRequest, HttpResponse, CompletionStage<OutgoingConnection>> createConnectionPhoto = 
				Http.get(system).outgoingConnection(ConnectHttp.toHost("http://placehold.it", 80));
		
		
		Unmarshaller<HttpEntity, Post> sessionUnmarshaller = createUnmarshaller(Post.class);
		Flow<HttpResponse, Post, NotUsed> unmarshallSession = Flow.of(HttpResponse.class).mapAsync(1, resp -> sessionUnmarshaller.unmarshal(resp.entity(), materializer));
		
		//create list of certificates request
		//we need session for request - mock it with Post TODO - use session here
		Flow<Post, HttpRequest, NotUsed> getCertificatesList = Flow.of(Post.class).map(session -> 
																	{
																		session.getUserId();
																		return HttpRequest.create().withUri("/photos").withMethod(HttpMethods.GET);
																	});		
		//unmarshall list of certificates
		Unmarshaller<HttpEntity, ArrayList<Photo>> certificateListUnmarshaller = createUnmarshaller(new TypeReference<ArrayList<Photo>>(){});
		
		/*
		Flow<HttpResponse,  ArrayList<Photo>, NotUsed> unmarshallCertificateList = Flow.of(HttpResponse.class)
																					.mapAsync(1, resp -> certificateListUnmarshaller.unmarshal(resp.entity(), materializer));
		*/																			
		Flow<HttpResponse,  Photo, NotUsed> unmarshallCertificateList1 = Flow.of(HttpResponse.class)
											.mapAsync(1, resp -> certificateListUnmarshaller.unmarshal(resp.entity(), materializer)).
											mapConcat(photos -> photos);
		//filter - downloadaj samo neparne
		Flow<Photo,  Photo, NotUsed> filterCertificates =  Flow.of(Photo.class).filter(p -> Integer.valueOf(p.getId()).intValue() % 2 > 0 );
		//Flow<Photo,  Photo, NotUsed> filterCertificates =  Flow.of(Photo.class).filter(p -> Integer.valueOf(p.getId()).intValue() == 7 );
		
		
		//DOWNLOAD ---------------------------
		//download certificates Url		
		Flow<Photo,  HttpRequest, NotUsed> downloadFileUrl =  Flow.of(Photo.class).map(photo -> 
															  {	
																	logger.debug("Downloading file url: {}", photo.url);
																	return HttpRequest.create().withUri(photo.url).withMethod(HttpMethods.GET);	
															  });
		
		Sink<Photo, ?> writePhoto = Sink.foreach(s -> logger.debug("Got output {}",s)); // Sink.foreach(System.out::println);		
		Sink<HttpResponse, ?> downloadCertificate = Sink.foreach(resp -> saveFileToDisc("E:\\TEMP\\download_cert", UUID.randomUUID().toString() + ".png", resp));
				
		Sink<ByteString, CompletionStage<Done>> printlnSink =
			    Sink.<ByteString> foreach(chunk -> System.out.println(chunk.utf8String()));
		
		
		startSession.via(createConnection).
					 via(unmarshallSession).
					 via(getCertificatesList).
					 via(createConnection).
					 via(unmarshallCertificateList1).
					 via(filterCertificates).
					 via(downloadFileUrl).
					 via(createConnectionPhoto).
					 mapAsyncUnordered(5, resp -> saveFileToDisc("E:\\TEMP\\download_cert\\", UUID.randomUUID().toString() + ".png", resp)).
					 runWith(Sink.ignore(), materializer);
	}

	
	public static void downloadPicturesWithNames(){
		//session creation
		Source<HttpRequest, NotUsed> startSession = Source.single(HttpRequest.create().withUri("/posts/1").withMethod(HttpMethods.GET));
		Flow<HttpRequest, HttpResponse, CompletionStage<OutgoingConnection>> createConnection = 
				Http.get(system).outgoingConnection(ConnectHttp.toHost("http://jsonplaceholder.typicode.com", 80));
		
		Flow<HttpRequest, HttpResponse, CompletionStage<OutgoingConnection>> createConnectionPhoto = 
				Http.get(system).outgoingConnection(ConnectHttp.toHost("http://placehold.it", 80));
		
		
		Unmarshaller<HttpEntity, Post> sessionUnmarshaller = createUnmarshaller(Post.class);
		Flow<HttpResponse, Post, NotUsed> unmarshallSession = Flow.of(HttpResponse.class).mapAsync(1, resp -> sessionUnmarshaller.unmarshal(resp.entity(), materializer));
		
		//create list of certificates request
		//we need session for request - mock it with Post TODO - use session here
		Flow<Post, HttpRequest, NotUsed> getCertificatesList = Flow.of(Post.class).map(session -> 
																	{
																		session.getUserId();
																		return HttpRequest.create().withUri("/photos").withMethod(HttpMethods.GET);
																	});		
		//unmarshall list of certificates
		Unmarshaller<HttpEntity, ArrayList<Photo>> certificateListUnmarshaller = createUnmarshaller(new TypeReference<ArrayList<Photo>>(){});
		
		/*
		Flow<HttpResponse,  ArrayList<Photo>, NotUsed> unmarshallCertificateList = Flow.of(HttpResponse.class)
																					.mapAsync(1, resp -> certificateListUnmarshaller.unmarshal(resp.entity(), materializer));
		*/																			
		Flow<HttpResponse,  Photo, NotUsed> unmarshallCertificateList1 = Flow.of(HttpResponse.class)
											.mapAsync(1, resp -> certificateListUnmarshaller.unmarshal(resp.entity(), materializer)).
											mapConcat(photos -> photos);
		//filter - downloadaj samo neparne
		Flow<Photo,  Photo, NotUsed> filterCertificates =  Flow.of(Photo.class).filter(p -> Integer.valueOf(p.getId()).intValue() % 100 == 0 );
		//Flow<Photo,  Photo, NotUsed> filterCertificates =  Flow.of(Photo.class).filter(p -> Integer.valueOf(p.getId()).intValue() == 7 );
		
		
		//DOWNLOAD ---------------------------
		//download certificates Url		
		Flow<Photo,  HttpRequest, NotUsed> downloadFileUrl =  Flow.of(Photo.class).map(photo -> 
															  {	
																	logger.debug("Downloading file url: {}", photo.url);
																	return HttpRequest.create().withUri(photo.url).withMethod(HttpMethods.GET);	
															  });
		
		Sink<Photo, ?> writePhoto = Sink.foreach(s -> logger.debug("Got output {}",s)); // Sink.foreach(System.out::println);		
		Sink<HttpResponse, ?> downloadCertificate = Sink.foreach(resp -> saveFileToDisc("E:\\TEMP\\download_cert", UUID.randomUUID().toString() + ".png", resp));
				
		Sink<ByteString, CompletionStage<Done>> printlnSink =
			    Sink.<ByteString> foreach(chunk -> System.out.println(chunk.utf8String()));
		
	
		
		//LETS BUILD GRAPH
		
		RunnableGraph.fromGraph(GraphDSL.create(b -> {
			//fan out
			final UniformFanOutShape<Photo, Photo> bcast = b.add(Broadcast.create(2));
			
			//merging
			 final FanInShape2<HttpResponse, Photo, Pair<HttpResponse, Photo>> zip =
			          b.add(Zip.create());
			//async save
			 final Flow<Pair<HttpResponse, Photo>, IOResult, NotUsed> saveFile =
					    Flow.<Pair<HttpResponse, Photo>>create().
					    	mapAsyncUnordered(5, p ->  saveFileToDisc("E:\\TEMP\\download_cert\\", p.second().getId() + ".png", p.first()));
				
			b.from(b.add(startSession)).
			via(b.add(createConnection)).
			via(b.add(unmarshallSession)).
			via(b.add(getCertificatesList)).
			via(b.add(createConnection)).
			via(b.add(unmarshallCertificateList1)).
			via(b.add(filterCertificates)).
			//fan out photo 
			viaFanOut(bcast).via(b.add(downloadFileUrl)).via(b.add(createConnectionPhoto)).toInlet(zip.in0());
			b.from(bcast).toInlet(zip.in1());
			//from merge
			b.from(zip.out()).via(b.add(saveFile)).to(b.add(Sink.ignore())); //.to(Sink.ignore());			
			//viaFanOut(bcast);			
			return ClosedShape.getInstance();					
		})).run(materializer);
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void fileExperiments(){
		
		final Path file = Paths.get("E:\\TEMP\\copy_file\\original.json");
		  
		Sink<ByteString, CompletionStage<Done>> printlnSink =
		    Sink.<ByteString> foreach(chunk -> System.out.println(chunk.utf8String()));
				
		final Path fileCopy = Paths.get("E:\\TEMP\\copy_file\\copy.json");
		Sink<ByteString, CompletionStage<Done>> copyFileSink =
			    Sink.<ByteString> foreach(chunk -> FileIO.toPath(fileCopy));
		  
		  CompletionStage<IOResult> ioResult =
		    FileIO.fromPath(file)
		      //.to(printlnSink)
		       .to(FileIO.toPath(fileCopy))
		      .run(materializer);		
	}
	
	
	
	public static void downloadFile(){
		
		Source<HttpRequest, NotUsed> startSession = Source.single(HttpRequest.create().withUri("/600/92c952").withMethod(HttpMethods.GET));
		Flow<HttpRequest, HttpResponse, CompletionStage<OutgoingConnection>> createConnection = 
				Http.get(system).outgoingConnection(ConnectHttp.toHost("http://placehold.it", 80));			
		startSession.
		via(createConnection).
		mapAsyncUnordered(5, resp -> saveFileToDisc("E:\\TEMP\\download_cert\\", UUID.randomUUID().toString() + ".png", resp)).
		runWith(Sink.ignore(), materializer);
	}
	
	
	
	
	public static void classicApproach(){
		
		Unmarshaller<HttpEntity, Post> unmarshaller = Unmarshaller.entityToString().
																	thenApply(resp -> {
																		ObjectMapper o =  new ObjectMapper();
																		try {
																			return o.readValue(resp, Post.class);
																		} catch (Exception e) {
																			// TODO Auto-generated catch block
																			logger.error("Error {}", e.getMessage());
																			return new Post();
																		}
																	});		
		CompletionStage<Post> cs = Source.single(HttpRequest.create().withUri("/posts/1").withMethod(HttpMethods.GET)).
		via(Http.get(system).outgoingConnection(ConnectHttp.toHost("jsonplaceholder.typicode.com", 80))).
		runWith(Sink.<HttpResponse>head(), materializer).
		thenCompose(resp -> { return unmarshaller.unmarshal(resp.entity(), materializer);
		 
		}).
		exceptionally(ex -> {
			logger.error(ex.getMessage());
			return null;
		});
		cs.thenAcceptAsync(resp -> logger.debug("Succes response: {}", resp.toString()));
		
		
		
		/* .whenComplete((p,t) -> {logger.debug(p);
		 			  return p;});
		*/
		//whenComplete(p -> logger.debug(p));
		
		
		/*
		thenCompose(resp -> {
			//entity (response body) must be consumed !!!!
			//http://doc.akka.io/docs/akka-http/current/java/http/client-side/request-level.html#request-level-api-java
			//otherwise connection will not be closed until timeout (1 minute)
			return TelevendHttpUtil.createUnmarshaller(resp, request).unmarshal(resp.entity(), ec, materializer);
		}).
		exceptionally(ex -> {
			logger.error("Error in Http Client, {}", ex.getMessage());
			//return HttpResponse.create().withStatus(StatusCodes.NETWORK_CONNECT_TIMEOUT);
			return new ResponseMessage(StatusCodes.NETWORK_CONNECT_TIMEOUT, request.getMessageId());
		}).
		thenCompose(resp -> CompletableFuture.completedFuture(convertResponseToResult(resp, request)));
		return response;
		*/
	}
	
	
	
	public static Post testUnmarshaller(){
		
		
		String aa = "{\n  \"userId\": 1,\n  \"id\": 1,\n  \"title\": \"sunt aut facere \",\n  \"body\": \"quia \\n\\n\"\n}";
		ObjectMapper o =  new ObjectMapper();
		Post p = null;
		try {
			p = o.readValue(aa, Post.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("Result: {}", p);
		return p;
	}
	
	
	public static void main(String[] args){
		//streamProcess1();
		
		//classicApproach();
		//testUnmarshaller();
		
		//fileExperiments();
		//RADI
		//downloadFile();
		//downloadOddPictures();
		downloadPicturesWithNames();
	}
	
	
}
