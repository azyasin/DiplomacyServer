package bandana.gameServer;

public class ValidationValue {
	private boolean valid;
	private int location;

	public ValidationValue(boolean valid, int location) {
		this.valid = valid;
		this.location = location;
	}

	public int getLocation() {
		return location;
	}

	public boolean isValid() {
		return valid;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
