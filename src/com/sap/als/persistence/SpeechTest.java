package com.sap.als.persistence;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQueries({
		@NamedQuery(name = "AllSpeechTests", query = "select s from SpeechTest s"),
		@NamedQuery(name = "SpeechTestById", query = "select s from SpeechTest s where s.id = :id"),
		@NamedQuery(name = "LastSpeechTestByPatientId", query = "select s from SpeechTest s where s.created = (select max(s1.created) from SpeechTest s1 where s1.patientId = s.patientId and s.patientId = :patientId)")
})

public class SpeechTest implements Serializable {

	private static final long serialVersionUID = 1L;

	public SpeechTest() {
	}

	@Id
	@GeneratedValue
	private long id;
	private long patientId;
	private Timestamp created;	
	
	@OneToMany(cascade = CascadeType.ALL)
	private List<SpeechTestRecording> recordings;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPatientId() {
		return patientId;
	}

	public void setPatientId(long patientId) {
		this.patientId = patientId;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public void setCreated(long created) {
		this.created = new Timestamp( created);
	}

	public List<SpeechTestRecording> getRecordings() {
		return recordings;
	}

	public void setRecordings(List<SpeechTestRecording> recordings) {
		this.recordings = recordings;
	}

}