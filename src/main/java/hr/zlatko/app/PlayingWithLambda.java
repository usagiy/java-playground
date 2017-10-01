package hr.zlatko.app;

import java.awt.event.ActionListener;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Predicate;


/**
 * 
 * @author zlatko
 * {@link ActionListener} {@link Comparator} {@link Runnable} are examples of interfaces with only one method
 *	With Java SE 8, an interface that follows this pattern is known as a "functional interface."
 *	Therefore, functional interfaces are leveraged for use with lambda expressions
 *
 *	java.util.function packafge ima hrpu predefiniranih iunterfacea za rad sa lambdama
 *
 */


public class PlayingWithLambda {

	private final static Logger logger = LoggerFactory.getLogger(PlayingWithLambda.class);

	
	public static void run(){
		
		// Anonymous Runnable
		Runnable r1 = new Runnable(){
		  
		  @Override
		  public void run(){
		    logger.info("Hello world one!");
		  }
		};
		
		// Lambda Runnable
		Runnable r2 = () -> logger.info("Hello world two!");		
		// Run em!
		r1.run();
		r2.run();
		
		
	
	}
	
	
	
}
