package hr.zlatko.cupic;

import java.util.concurrent.atomic.AtomicLong;

public class DijeljenjePodataka5 {

	public static AtomicLong counter = new AtomicLong(0L);

	private static class Posao implements Runnable {
		private int n;

		public Posao(int n) {
			super();
			this.n = n;
		}

		@Override
		public void run() {
			for (int i = 0; i < n; i++) {
				while (true) {
					long current = counter.get();
					if (counter.compareAndSet(current, current + 1)) {
						break;
					}
				}
			}
		}
	}

	public static void main(String[] args) {

		final int brojRadnika = 2;
		Thread[] radnici = new Thread[brojRadnika];
		Posao posao = new Posao(100000);

		for (int i = 0; i < radnici.length; i++) {
			radnici[i] = new Thread(posao);
		}

		for (int i = 0; i < radnici.length; i++) {
			radnici[i].start();
		}

		for (int i = 0; i < radnici.length; i++) {
			while (true) {
				try {
					radnici[i].join();
					break;
				} catch (InterruptedException e) {
					// Idemo opet zvati join()
				}
			}
		}

		System.out.println("Konačno stanje brojača: " + counter.get());

	}

}