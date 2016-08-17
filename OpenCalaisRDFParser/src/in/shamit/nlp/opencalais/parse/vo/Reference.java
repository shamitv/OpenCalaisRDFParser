package in.shamit.nlp.opencalais.parse.vo;

import java.util.List;

public class Reference {
	int offset;
	int length;
	String fragment;
	String replacement;
	EntityResult entity;
	static int count=0;
	int id=count;
	List <String> suffix=null;
	List <String> prefix=null;
	public Reference(double confidence, int offset, int length, String fragment, EntityResult entity) {
		super();
		this.offset = offset;
		this.length = length;
		this.fragment = fragment;
		this.entity = entity;
		count++;
	}


	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getFragment() {
		return fragment;
	}

	public void setFragment(String fragment) {
		this.fragment = fragment;
	}

	public EntityResult getEntity() {
		return entity;
	}

	public void setEntity(EntityResult entity) {
		this.entity = entity;
	}
	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	public List<String> getSuffix() {
		return suffix;
	}


	public void setSuffix(List<String> suffix) {
		this.suffix = suffix;
	}


	public List<String> getPrefix() {
		return prefix;
	}


	public void setPrefix(List<String> prefix) {
		this.prefix = prefix;
	}

	String getPrefixString(){
		if(prefix==null){
			return "";
		}else{
			StringBuffer buf = new StringBuffer();
			for(String s : prefix){
				buf.append(s);
				buf.append(" ");
			}
			return buf.toString();
		}
	}

	String getSuffixString(){
		if(suffix==null){
			return "";
		}else{
			StringBuffer buf = new StringBuffer();
			for(String s : suffix){
				buf.append(s);
				buf.append(" ");
			}
			return buf.toString();
		}
	}

	public String getRefContext(){
		return getPrefixString() + "#%#" + getFragment() + "#%#" + getSuffixString(); 
	}
	
	@Override
	public String toString() {
		return "Reference #"+id+" [offset=" + offset + ", length=" + length + ", fragment="
				+ fragment + ", entity=" + entity + " context = "+getRefContext()+"]";
	}
}
