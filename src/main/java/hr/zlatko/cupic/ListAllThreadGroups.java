package hr.zlatko.cupic;

public class ListAllThreadGroups {

	public static void main(String[] args) {

		Thread current = Thread.currentThread();
		ThreadGroup group = current.getThreadGroup();

		while (true) {
			ThreadGroup parent = group.getParent();
			if (parent == null)
				break;
			group = parent;
		}

		group.list();

		System.out.println();
		System.out.println("Ja sam: " + current.getName());

	}

}