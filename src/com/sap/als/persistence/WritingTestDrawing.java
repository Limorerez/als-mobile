package com.sap.als.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class WritingTestDrawing implements Serializable {

	private static final long serialVersionUID = 1L;

	public WritingTestDrawing() {
	}

	@Id
	@GeneratedValue
	private long id;
	private String drawingName;
	
	@Column(columnDefinition = "CLOB")
	private String drawingJson;

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public String getDrawingName() {
		return drawingName;
	}

	public void setDrawingName(String drawingName) {
		this.drawingName = drawingName;
	}

	public String getDrawingJson() {
		return drawingJson;
	}

	public void setDrawingJson(String drawingJson) {
		this.drawingJson = drawingJson;
	}

}