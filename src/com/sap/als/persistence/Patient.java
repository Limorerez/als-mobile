package com.sap.als.persistence;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
		@NamedQuery(name = "AllPatients", query = "select p from Patient p"),
		@NamedQuery(name = "PatientsByUserType", query = "select p from Patient p where p.userType = :userType"),
		@NamedQuery(name = "PatientByEmail", query = "select p from Patient p where p.email = :email") })
public class Patient implements Serializable {
	public enum UserType {
		PATIENT,
		CONTROL
	}
	private static final long serialVersionUID = 1L;

	public Patient() {
	}

	@Id
	@GeneratedValue
	private long id;
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private int gender;
	private Timestamp birthday;
	private Timestamp diagnoseDate;
	private Timestamp created;
	private UserType userType;

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public Timestamp getBirthday() {
		return birthday;
	}

	public void setBirthday(Timestamp birthday) {
		this.birthday = birthday;
	}

	public void setBirthday(long birthday) {
		this.birthday = new Timestamp(birthday);
	}

	public Timestamp getDiagnoseDate() {
		return diagnoseDate;
	}

	public void setDiagnoseDate(Timestamp diagnoseDate) {
		this.diagnoseDate = diagnoseDate;
	}

	public void setDiagnoseDate(long diagnoseDate) {
		this.diagnoseDate = new Timestamp(diagnoseDate);
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}