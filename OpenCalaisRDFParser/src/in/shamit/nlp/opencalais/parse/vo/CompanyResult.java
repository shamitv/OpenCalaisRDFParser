package in.shamit.nlp.opencalais.parse.vo;

public class CompanyResult extends EntityResult{
	public CompanyResult(String name) {
		super(name);
		this.type = EntityResult.COMPANY_TYPE;
	}
}
