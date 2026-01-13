package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.annotation.JsonbProperty;

public class JsonbDeserialization{
	
	public JsonbDeserialization(String subFirstName, Childclass child, String subfavoriteEditor) {
		super();
		this.subFirstName = subFirstName;
		this.child = child;
		this.subfavoriteEditor = subfavoriteEditor;
	}

	private String subFirstName;
    
    private Childclass child;
    
    private SubChild subChild;
    
    public SubChild getSubChild() {
		return subChild;
	}

	public void setSubChild(SubChild subChild) {
		this.subChild = subChild;
	}

	public String getSubFirstName() {
		return subFirstName;
	}

	public void setSubFirstName(String subFirstName) {
		this.subFirstName = subFirstName;
	}

	@JsonbProperty("fav_lang1")
    private String subfavoriteEditor;    // Diagnostic: @JsonbProperty property uniqueness in subclass, multiple properties cannot have same property names.

	public String getSubfavoriteEditor() {
		return subfavoriteEditor;
	}

	public void setSubfavoriteEditor(String subfavoriteEditor) {
		this.subfavoriteEditor = subfavoriteEditor;
	}
	
	public Childclass getChild() {
		return child;
		
	}

	public void setChild(Childclass child) {
		this.child = child;
		
	}

	public static class Childclass{
		
		public Childclass(int age, String name) {
			super();
			this.age = age;
			this.name = name;
		}
		private int age;
		private String name;
		public int getAge() {
			return age;
			
		}
		public void setAge(int age) {
			this.age = age;
			
		}
		public String getName() {
			return name;
			
		}
		public void setName(String name) {
			this.name = name;
			
		}
	}
	
	public class SubChild{
		
		private int token;
		public int getToken() {
			return token;
		}
		public void setToken(int token) {
			this.token = token;
		}
		private String title;
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
	}
}
