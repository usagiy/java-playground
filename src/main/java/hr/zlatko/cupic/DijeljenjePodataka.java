package hr.zlatko.cupic;

public class DijeljenjePodataka {

	// public static long counter = 0L;
	// volatile - instrukcija kompileru da ne optimizira incrementsamo sa
	// lokalnim varijablama vec da ipak svaki put updata counter u memoriji
	public static volatile long counter = 0L;

	/*
	public void uvecajNaSiguranNacin(int n) {
		// Želimo uvećati n puta
		for (int i = 0; i < n; i++) {
			// Pokušavaj uvećati za jedan sve dok ne uspiješ
			while (true) {
				long trenutnaVrijednost = counter;
				long novaVrijednost = trenutnaVrijednost + 1;
				boolean uspjeh = CAS(counter, trenutnaVrijednost, novaVrijednost);
				if (uspjeh)
					break;
			}
		}

	}
	*/


	private static class Posao implements Runnable {
		private int n;

		public Posao(int n) {
			super();
			this.n = n;
		}

		@Override
		public void run() {
			for (int i = 0; i < n; i++) {
				counter++;
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

		System.out.println("Konačno stanje brojača: " + counter);
	}

}
