package hr.zlatko.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class PlayingWithJava8 {

	private final static Logger logger = LoggerFactory.getLogger(PlayingWithJava8.class);

	private static int operate(int a, int b, MathOperation mathOperation) {
		return mathOperation.operation(a, b);
	}
	
	
	private static void eval(List<Integer> list, Predicate<Integer> predicate) {
	      for(Integer n: list) {			
	         if(predicate.test(n)) {
	            System.out.println(n + " ");
	         }
	      }
	   }
	
	
	public static void run() {

		// LAMBDA
		//--------------------------------------
		/*
		 * Lambda expressions are used primarily to define inline 
		 * implementation of a functional interface, i.e., an interface with a single method only
		 * 
		 * Lambda expression eliminates the need of anonymous class and gives a very simple yet 
		 * powerful functional programming capability to Java
		 * 
		 */
		

		List<String> names1 = new ArrayList<String>();
		names1.add("Mahesh ");
		names1.add("Suresh ");
		names1.add("Ramesh ");
		names1.add("Naresh ");
		names1.add("Kalpesh ");

		// Comparator je functional interface - to je drugi argument
		Collections.sort(names1, (s1, s2) -> s1.compareTo(s2));

		MathOperation addition = (a, b) -> a + b;

		int summ = addition.operation(1, 1);

		logger.info("Suma je: {}", summ);

		// with out type declaration
		MathOperation subtraction = (a, b) -> a - b;

		// with return statement along with curly braces
		MathOperation multiplication = (int a, int b) -> {
			return a * b;
		};
				
		logger.info("razlika je : {}", operate(7, 5, subtraction));		
		logger.info("produkt je : {}", operate(7, 8, multiplication));
		
		
		// Method References
		//-------------------------------------
		/*
		 * A method reference can be used to point the following types of methods −Static methods
		   Instance methods, Constructors using new operator (TreeSet::new)
		 */
		names1.forEach(System.out::println);
		
		// Functional Interfaces
		//----------------------------------------
		/* Functional interfaces have a single functionality to exhibit
		 * Java 8 has defined a lot of functional interfaces to be used extensively in lambda expressions
		 * They are defined in : in java.util.Function package 
		 
		 */
		
		//Predicate<T> - Represents a predicate (Boolean-valued function) of one argument.
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
		//print all
		logger.info("print all");
		eval(list, n -> true);
		
		//print even numbers
		logger.info("print even numbers");
		eval(list, n -> n%2 == 0);
		
		//print numbers greater than 5
		logger.info("print numbers grether than 5");		
		eval(list, n -> n > 5);
		
		//Function<T,R> - Represents a function that accepts one argument and produces a result.
		logger.info("multiply by 2");
		evalFunc(list, n -> n*2);
		
		logger.info("add 5");
		evalFunc(list, n -> n + 5);
		
		
		
		//Default Methods
		//---------------------------------
		/*
		 * ava 8 introduces a new concept of default method implementation in interfaces.
		 * This capability is added for backward compatibility so that old interfaces can be used to leverage the lambda expression capability of Java 8
		 * An interface can also have static helper methods from Java 8 onwards.
		 */
		
		//vehicle v = new testVehicle();
		testVehicle vv = new testVehicle();
		vv.print();
		
		
		//Java 8 - Streams
		//----------------------------------
		/*
		 * tream is a new abstract layer introduced in Java 8. 
		 * Using stream, you can process data in a declarative way similar to SQL statements
		 */
		
		//generating stream
		/*
		 * With Java 8, Collection interface has two methods to generate a Stream
		 * stream() parallelStream()
		 */
		
		List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl");
		System.out.println("List: " +strings);
		 
		List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5);
		
		System.out.println("Using Java 8 streams: ");
	    System.out.println("List: " +strings);
	    
	    long count = strings.stream().filter(string->string.isEmpty()).count();
	    System.out.println("Empty Strings: " + count);
	    
	    count = strings.stream().filter(string -> string.length() == 3).count();
	    System.out.println("Strings of length 3: " + count);
	    
	    List<String> filtered = strings.stream().filter(string ->!string.isEmpty()).collect(Collectors.toList());
	    System.out.println("Filtered List: " + filtered);
	    
	    
	    String mergedString = strings.stream().filter(string ->!string.isEmpty()).collect(Collectors.joining(", "));
	    System.out.println("Merged String: " + mergedString);
	    
	    
	    //Flat Map Test
	    //----------------------------------------
	    logger.info("---------- Testing flat map --------------");
	    List<Developer> team = new ArrayList<>();
        Developer polyglot = new Developer("esoteric");
        polyglot.add("clojure");
        polyglot.add("scala");
        polyglot.add("groovy");
        polyglot.add("go");

        Developer busy = new Developer("pragmatic");
        busy.add("java");
        busy.add("javascript");

        team.add(polyglot);
        team.add(busy);
        
        //ovo je stream
        team.stream();
        //d.getLanguages() je set i zato flatMap
        List<String> languages = team.stream().map(d -> d.getLanguages()).flatMap(l -> l.stream()).collect(Collectors.toList());	    
        List<String> names = team.stream().map(d -> d.getName()).collect(Collectors.toList());
	    
        logger.info("Developer Languages: {}", languages);
        logger.info("Developer Names: {}", names);
        
	    
	    
	    
	    
	    
	    
	    //Optional Class
	    //----------------------------------------
	    /*
	     * Optional is a container object which is used to contain not-null objects
	     */
		
	    Integer value1 = null;
	    Integer value2 = new Integer(10);
	    
	    //Optional.ofNullable - allows passed parameter to be null.
	    Optional<Integer> a = Optional.ofNullable(value1);
		
	  //Optional.of - throws NullPointerException if passed parameter is null
		Optional<Integer> b = Optional.of(value2);
	    
		System.out.println(sum(a,b));
		
		
		
	}


	
	private static void evalFunc(List<Integer> list, Function<Integer, Integer> func) {
	      for(Integer n: list) {
	            System.out.println(func.apply(n) + " ");	         
	      }
	   }
	
	
	
	

	// definiran functionalni interface
	interface MathOperation {
		int operation(int a, int b);
	}
	
	
	
	//default interface
	interface vehicle {
		   default void print(){
		      System.out.println("I am a vehicle!");
		   }
			
		   static void blowHorn(){
		      System.out.println("Blowing horn!!!");
		   }
		}
	
	static class testVehicle implements vehicle {
		
	}
	
	//optional
	public static Integer sum(Optional<Integer> a, Optional<Integer> b){
		
	      //Optional.isPresent - checks the value is present or not
			
	      System.out.println("First parameter is present: " + a.isPresent());
	      System.out.println("Second parameter is present: " + b.isPresent());
			
	      //Optional.orElse - returns the value if present otherwise returns
	      //the default value passed.
	      Integer value1 = a.orElse(new Integer(0));
			
	      //Optional.get - gets the value, value should be present
	      Integer value2 = b.get();
	      return value1 + value2;
	   }
	
	
	
	//Flat Map
	public static class Developer {

	    private String name;
	    private Set<String> languages;

	    public Developer(String name) {
	        this.languages = new HashSet<>();
	        this.name = name;
	    }

	    public void add(String language) {
	        this.languages.add(language);
	    }

	    public Set<String> getLanguages() {
	        return languages;
	    }

		public String getName() {
			return name;
		}
	    
	    
	}


}
