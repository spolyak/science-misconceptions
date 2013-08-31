package com.stevepolyak.aaas;

import java.util.ArrayList;
import java.util.List;

public class Misconception {

	private String id;
	private String text;
	private List<String> values;
	
	public Misconception() {
		setValues(new ArrayList<String>());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}
}
