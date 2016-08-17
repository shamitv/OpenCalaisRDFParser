package in.shamit.nlp.opencalais.parse.vo;

public class PersonResult extends EntityResult{

	@Override
	public String toString() {
		return "PersonResult [firstName=" + firstName + ", lastName=" + lastName + ", name=" + name + "]";
	}
	String firstName;
	String lastName;


	public PersonResult(String firstName, String lastName, String name) {
		super(name);
		this.firstName = firstName;
		this.lastName = lastName;
		this.name = name;
		this.type = EntityResult.PERSON_TYPE;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
