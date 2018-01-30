package hello.world.esper;

public class ItemNotFoundException extends Exception {

	public ItemNotFoundException(String string, NoSuchFieldException e) {
		super(string,e);
	}

}
