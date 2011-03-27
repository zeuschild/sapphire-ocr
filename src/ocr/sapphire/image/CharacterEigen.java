package ocr.sapphire.image;

public class CharacterEigen {

	private double[] main;
	private double[][] auxiliaries;

	public CharacterEigen(double[] main, double[][] auxiliaries) {
		super();
		this.main = main;
		this.auxiliaries = auxiliaries;
	}

	public double[] getMain() {
		return main;
	}

	public double[][] getAuxiliaries() {
		return auxiliaries;
	}

}
