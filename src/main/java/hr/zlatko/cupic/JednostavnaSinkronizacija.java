package hr.zlatko.cupic;

import java.util.Random;

public class JednostavnaSinkronizacija {

	// Model posla popunjavanja dijela polja
	private static class Posao implements Runnable {

		private double[] polje;
		private int pocetniIndeks;
		private int zavrsniIndeks;

		public Posao(double[] polje, int pocetni, int zavrsni) {
			super();
			this.polje = polje;
			this.pocetniIndeks = pocetni;
			this.zavrsniIndeks = zavrsni;
		}

		@Override
		public void run() {
			Random rand = new Random();
			for (int i = pocetniIndeks; i < zavrsniIndeks; i++) {
				polje[i] = rand.nextDouble();
			}
		}
	}

	public static void main(String[] args) {

		double[] polje = new double[1024 * 64];

		final int brojRadnika = 4;
		Thread[] radnici = new Thread[brojRadnika];

		// Stvori poslove i opisnike dretvi
		for (int i = 0; i < radnici.length; i++) {
			Posao posao = new Posao(polje, polje.length * i / radnici.length, polje.length * (i + 1) / radnici.length);
			radnici[i] = new Thread(posao);
		}

		// Pokreni dretve
		for (int i = 0; i < radnici.length; i++) {
			radnici[i].start();
		}

		// Čekaj jednu po jednu dretvu da završi
		for (int i = 0; i < radnici.length; i++) {
			while (true) {
				try {
					//cekamo dok svi posao thread ne zavrse
					radnici[i].join();
					break;
				} catch (InterruptedException e) {
					// Idemo opet zvati join()
				}
			}
		}

		System.out.println("Imam čitavo polje slučajnih brojeva!");
	}

}