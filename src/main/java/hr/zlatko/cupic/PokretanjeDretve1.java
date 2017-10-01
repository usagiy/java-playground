package hr.zlatko.cupic;

public class PokretanjeDretve1 {

	private static class Posao implements Runnable {
		@Override
		public void run() {
			Thread current = Thread.currentThread();
			System.out.println("Počinjem: " + current);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignorable) {
			}
			System.out.println("Pozdrav iz nove dretve!");
			System.out.println("Završavam: " + current);
		}
	}

	public static void main(String[] args) {

		Posao posao = new Posao();
		//new Thread(posao).start();
		Thread thread = new Thread(posao);
		//if set deamon odma ce umrijeti jer ako je samo deamonska thread umre
		thread.setDaemon(true);
		

		System.out.println("Završavam metodu main...");
	}
}