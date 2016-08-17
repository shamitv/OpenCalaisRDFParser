package in.shamit.nlp.opencalais.parse;

import java.util.ArrayList;
import java.util.List;

import in.shamit.nlp.opencalais.parse.vo.CompanyResult;
import in.shamit.nlp.opencalais.parse.vo.CountryResult;
import in.shamit.nlp.opencalais.parse.vo.EntityResult;
import in.shamit.nlp.opencalais.parse.vo.PersonResult;
import in.shamit.nlp.opencalais.parse.vo.Reference;


public class ParseResult {
	String sourceText;
	List<Reference> references;

	void init(){
		references = new ArrayList<>();
	}
	
	public ParseResult(String sourceText) {
		super();
		this.sourceText = sourceText;
		init();
	}

	public String getSourceText() {
		return sourceText;
	}

	public void setSourceText(String sourceText) {
		this.sourceText = sourceText;
	}


	public void addPersonReference(PersonResult person, int offset, int length, String fragment) {
		Reference r = new Reference(.5, offset, length, fragment, person);
		references.add(r);
	}

	public void addCompanyReference(CompanyResult cr, int offset, int length, String fragment) {
		Reference r = new Reference(.5, offset, length, fragment, cr);
		references.add(r);
	}

	public List<Reference> getReferences(){
		return references;
	}

	public void addCountryReference(CountryResult cr, int offset, int length, String fragment) {
		addReference(cr, offset, length, fragment);
	}

	public void addReference(EntityResult er, int offset, int length, String fragment) {
		Reference r = new Reference(.5, offset, length, fragment, er);
		references.add(r);
	}

}
