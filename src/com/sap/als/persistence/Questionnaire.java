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
		@NamedQuery(name = "AllQuestionnaires", query = "select q from Questionnaire q order by q.patientId"),
		@NamedQuery(name = "QuestionnaireById", query = "select q from Questionnaire q where q.id = :id"),
		@NamedQuery(name = "LastQuestionnaireByPatientId", query = "select q from Questionnaire q where q.created = (select max(q1.created) from Questionnaire q1 where q1.patientId = q.patientId and q.patientId = :patientId)") })
public class Questionnaire implements Serializable {

	private static final long serialVersionUID = 1L;

	public Questionnaire() {
	}

	@Id
	@GeneratedValue
	private long id;
	private long patientId;
	private Timestamp created;

	@OneToMany(cascade = CascadeType.ALL)
	private List<QuestionnaireAnswer> answers;

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
		this.created = new Timestamp(created);
	}

	public List<QuestionnaireAnswer> getAnswers() {
		return answers;
	}

	public void setAnswers(List<QuestionnaireAnswer> answers) {
		this.answers = answers;
	}

}