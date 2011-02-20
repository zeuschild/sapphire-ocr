package ocr.sapphire.image;

public class CharacterEigen {

	private double[] main;
	private double[][] auxiliaries;

	private CharacterEigen(double[] main, double[][] auxilaries) {
		super();
		this.main = main;
		this.auxiliaries = auxilaries;
	}

	public double[] getMain() {
		return main;
	}

	public double[][] getAuxiliaries() {
		return auxiliaries;
	}

}
