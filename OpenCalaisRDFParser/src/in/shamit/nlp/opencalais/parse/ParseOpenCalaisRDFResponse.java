package in.shamit.nlp.opencalais.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.tr.tms.abstractionLayer.CalaisModel;
import com.tr.tms.abstractionLayer.CalaisModelCreator;
import com.tr.tms.abstractionLayer.CalaisObject;
import com.tr.tms.abstractionLayer.engine.Engine.Format;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import in.shamit.nlp.opencalais.parse.vo.CompanyResult;
import in.shamit.nlp.opencalais.parse.vo.CountryResult;
import in.shamit.nlp.opencalais.parse.vo.PersonResult;
import in.shamit.nlp.opencalais.parse.vo.RegionResult;

public class ParseOpenCalaisRDFResponse {
	static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	static XPathFactory pathFactory = XPathFactory.newInstance();
	static DocumentBuilder bldr = null;
	static Set<String> nonNames = new HashSet<>();
	static int textLineCount = 0;
	static int personCount = 0;
	static int companyCount = 0;
	static int refCount = 0;
	static AtomicInteger fileCount=new AtomicInteger(0);

	public static void main(String[] args) throws Exception {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		bldr = factory.newDocumentBuilder();
		
		File rdfRespDir = new File("L:/work/nlp/datasets/news/newTest1");
		root.debug("Handling " + rdfRespDir.getAbsolutePath());
		Files.walk(rdfRespDir.toPath()).parallel().forEach(ParseOpenCalaisRDFResponse::handleFile);
		
		root.info("Total lines of text " + textLineCount);
		root.info("Total Person Entities " + personCount);
		root.info("Total Company Entities " + companyCount);
		root.info("Reference to  Person Entities " + refCount);
	}

	static void handleFile(Path p) {
		try {
			File f = p.toFile();
			// System.out.println(f.getAbsolutePath());
			if (f.isFile()) {
				analyzeFile(f);
			}
		} catch (Exception e) {
			System.err.println(p.toFile().getAbsolutePath());
			e.printStackTrace();
		}
	}

	static void analyzeFile(File f) {
		Document xml = getXMLDoc(f);
		ParseResult r = processFile(f, xml);
		int docLineCount = r.getSourceText().split("\\r?\\n").length;
		textLineCount += docLineCount;
		int countVal=fileCount.incrementAndGet();
		if(countVal%1000==0){
			System.err.println("Processed article #"+countVal);
		}
	}

	static Document getXMLDoc(File xmlFile) {
		try {
			if (bldr == null) {
				factory.setNamespaceAware(true);
				factory.setValidating(false);
				bldr = factory.newDocumentBuilder();
			}
			DocumentBuilder b = factory.newDocumentBuilder();
			return b.parse(new InputSource(new FileInputStream(xmlFile)));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	static ParseResult parseFile(File f) {
		Document xml = getXMLDoc(f);
		return processFile(f, xml);
	}

	public static ParseResult processFile(File f, Document xml) {
		String content = xml.getDocumentElement().getFirstChild().getTextContent();
		ParseResult result = new ParseResult(content);
		CalaisModel calaisModel = CalaisModelCreator.readFile(f.getAbsolutePath(), Format.RDF);
		findPeople(content, calaisModel, result);
		findCompanies(content, calaisModel, result);
		findCountries(content, calaisModel, result);
		findRegions(content, calaisModel, result);
		return result;
	}

	static void findCompanies(String content, CalaisModel calaisModel, ParseResult result) {
		List<CalaisObject> companies = calaisModel.getCalaisObjectByType("http://s.opencalais.com/1/type/em/e/Company");
		if (companies != null) {
			for (CalaisObject co : companies) {
				Double confidence = getConfidenceLevel(co);
				if (confidence != null && confidence > .5) {
					String name = getCommonName(co);
					if (name == null) {
						name = getName(co);
					}
					if (name != null) {
						CompanyResult cr = new CompanyResult(name);
						List<CalaisObject> instanceList = getReferences(co);
						for (CalaisObject objInstance : instanceList) {
							int offset = getInstanceOffset(objInstance);
							int length = getInstanceLength(objInstance);
							if (offset > -1 && length > -1 && offset < content.length()) {
								String fragment = content.substring(offset, offset + length);
								// System.out.println("\t\t" + fragment);
								// System.out.println("Company instance score "
								// + getInstanceScore(objInstance));
								result.addCompanyReference(cr, offset, length, fragment);
							}
						}
					}
				}
			}
		}
	}

	static void findCountries(String content, CalaisModel calaisModel, ParseResult result) {
		List<CalaisObject> countries = calaisModel.getCalaisObjectByType("http://s.opencalais.com/1/type/em/e/Country");
		//List<CalaisObject> countries = calaisModel.getCalaisObjectByType("http://s.opencalais.com/1/type/er/Geo/Country");
		if (countries != null) {
			for (CalaisObject co : countries) {
				String name = getCommonName(co);
				if (name == null) {
					name = getName(co);
				}
				if (name != null) {
					CountryResult cr = new CountryResult(name);
					List<CalaisObject> instanceList = getReferences(co);
					for (CalaisObject objInstance : instanceList) {
						int offset = getInstanceOffset(objInstance);
						int length = getInstanceLength(objInstance);
						if (offset > -1 && length > -1 && offset < content.length()) {
							String fragment = content.substring(offset, offset + length);
							result.addReference(cr, offset, length, fragment);
						}
					}
				}
			}
		}
	}

	static void findRegions(String content, CalaisModel calaisModel, ParseResult result) {
		List<CalaisObject> regions = calaisModel.getCalaisObjectByType("http://s.opencalais.com/1/type/em/e/Region");
		if (regions != null) {
			for (CalaisObject co : regions) {
				String name = getCommonName(co);
				if (name == null) {
					name = getName(co);
				}
				if (name != null) {
					RegionResult cr = new RegionResult(name);
					List<CalaisObject> instanceList = getReferences(co);
					for (CalaisObject objInstance : instanceList) {
						int offset = getInstanceOffset(objInstance);
						int length = getInstanceLength(objInstance);
						if (offset > -1 && length > -1 && offset < content.length()) {
							String fragment = content.substring(offset, offset + length);
							// System.out.println("\t\t" + fragment);
							// System.out.println("Company instance score "
							// + getInstanceScore(objInstance));
							result.addReference(cr, offset, length, fragment);
						}
					}
				}
			}
		}
	}

	static void findPeople(String content, CalaisModel calaisModel, ParseResult result) {
		List<CalaisObject> people = calaisModel.getCalaisObjectByType("http://s.opencalais.com/1/type/em/e/Person");
		if (people != null) {
			for (CalaisObject co : people) {
				String type = co.getType();
				// System.out.println(co.getObjectId());
				String name = getName(co);
				if (name == null) {
					name = getCommonName(co);
				}
				if (name != null) {
					String fname = null, lname = null;
					fname = getFirstName(co);
					lname = getLastName(co);
					PersonResult person = new PersonResult(fname, lname, name);
					List<CalaisObject> instanceList = getReferences(co);
					for (CalaisObject objInstance : instanceList) {
						int offset = getInstanceOffset(objInstance);
						int length = getInstanceLength(objInstance);
						if (offset > -1 && length > -1 && offset < content.length()) {
							String fragment = content.substring(offset, offset + length);
							// System.out.println("\t\t" + fragment);
							// System.out.println("Person instance score " +
							// getInstanceScore(objInstance));
							result.addPersonReference(person, offset, length, fragment);
						}
					}
				}
			}
		}
	}

	static List<CalaisObject> getReferences(CalaisObject co) {
		String refDetailkey = "http://s.opencalais.com/1/type/sys/InstanceInfo";
		String refKey = "http://s.opencalais.com/1/pred/subject";
		Map<String, Map<String, List<CalaisObject>>> backReferences = co.getBackReferences();
		Map<String, List<CalaisObject>> refDetails = backReferences.get(refKey);
		if(refDetails!=null){
			return refDetails.get(refDetailkey);	
		}else{
			return new ArrayList<CalaisObject>();
		}
		
	}

	static int getLiteralAsInt(String literalName, CalaisObject objInstance) {
		int ret = -1;
		String value = getLiteralAsString(literalName, objInstance);
		if (value != null) {
			ret = Integer.parseInt(value);
		}
		return ret;
	}

	static Double getLiteralAsDouble(String literalName, CalaisObject objInstance) {
		Double ret = null;
		String value = getLiteralAsString(literalName, objInstance);
		if (value != null) {
			ret = Double.parseDouble(value);
		}
		return ret;
	}

	static String getLiteralAsString(String literalName, CalaisObject objInstance) {
		String ret = null;
		Map<String, List<String>> literals = objInstance.getLiterals();
		if (literals.containsKey(literalName)) {
			String value = literals.get(literalName).get(0);
			ret = value;
		}
		return ret;
	}

	static Double getConfidenceLevel(CalaisObject objInstance) {
		return getLiteralAsDouble("http://s.opencalais.com/1/pred/confidencelevel", objInstance);
	}

	static Double getInstanceScore(CalaisObject objInstance) {
		return getLiteralAsDouble("http://s.opencalais.com/1/pred/score", objInstance);
	}

	static int getInstanceOffset(CalaisObject objInstance) {
		return getLiteralAsInt("http://s.opencalais.com/1/pred/offset", objInstance);
	}

	static int getInstanceLength(CalaisObject objInstance) {
		return getLiteralAsInt("http://s.opencalais.com/1/pred/length", objInstance);
	}

	static String getFirstName(CalaisObject objInstance) {
		return getLiteralAsString("http://s.opencalais.com/1/pred/firstname", objInstance);
	}

	static String getLastName(CalaisObject objInstance) {
		return getLiteralAsString("http://s.opencalais.com/1/pred/lastname", objInstance);
	}

	static String getCommonName(CalaisObject objInstance) {
		return getLiteralAsString("http://s.opencalais.com/1/pred/commonname", objInstance);
	}

	static String getName(CalaisObject objInstance) {
		return getLiteralAsString("http://s.opencalais.com/1/pred/name", objInstance);
	}
}
