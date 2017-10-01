package hr.zlatko.actor;





//thread safe sinleton za konnekcije, kako reconnectati 
class ThreadSafeSingleton {	 
	private static final Object instance = new Object(){
		@Override
		public String toString() {			
			return "ja sam singleton instance";
		}
	};
 
	protected ThreadSafeSingleton() {
	}
 
	// Runtime initialization
	// By defualt ThreadSafe
	public static Object getInstance() {
		return instance;
	}
}



interface Bla {
	
	public void myFunction();
	
	
}

/**
 * inline class definition
 * @author zlatko
 *
 */
public class JavaExperiment {

	//inline kreiranje klase
	final static Bla implBla = new Bla(){
		@Override
		public void myFunction() {
			System.out.println("inline clasa");
			System.out.println("Singleton: " + ThreadSafeSingleton.getInstance());
			System.out.println("Singleton: " + ThreadSafeSingleton.getInstance());			
		}		
	};
	
	public static void main(String[] args) {
		implBla.myFunction();
	}
	
}
