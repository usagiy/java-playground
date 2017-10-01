package hr.zlatko.cupic;



public class PokretanjeDretve2 {

	private static class PosaoIDretva extends Thread {
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

		new PosaoIDretva().start();

		System.out.println("Završavam metodu main...");
	}
 }