package mud;
public class Area extends BasicMudObject {
      private Area parent;
      private String name;
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public Area getParent() {
		return parent;
	}

}
