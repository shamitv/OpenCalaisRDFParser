package in.shamit.nlp.opencalais.parse.vo;

public class EntityResult {
	public static final String PERSON_TYPE = "person";
	public static final String COMPANY_TYPE = "company";
	public static final String COUNTRY_TYPE = "country";
	public static final String REGION_TYPE = "region";
	public static final String GEO_TYPE = "geo";
	String name;
	String type;

	public EntityResult(String name) {
		this.name=name;
	}
	public EntityResult() {
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "EntityResult [" + type + ":" + name + "]";
	}
}
