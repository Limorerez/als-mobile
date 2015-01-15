package com.sap.als.controller;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.sap.als.persistence.Patient;
import com.sap.als.security.HashCode;
import com.sap.als.utils.EmfGenerator;

@RestController
@RequestMapping("/patients")
@SuppressWarnings("unchecked")
public class PatientsController {

	private DataSource ds;
	private EntityManagerFactory emf;
	private Gson gson;

	public PatientsController() throws ServletException {
		emf = EmfGenerator.initEntityManagerFactory(ds);
		gson = com.sap.als.utils.GsonBuilder.create();
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String getPatients(
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "userType", required = false) String userType,
			HttpServletResponse response) {
		EntityManager em = emf.createEntityManager();
		List<Patient> resultList;

		try {
			if (email != null) {
				Query query = em.createNamedQuery("PatientByEmail");
				query.setParameter("email", email);
				resultList = query.getResultList();

				if (resultList.size() > 0) {
					return gson.toJson(resultList.get(0));
				} else {
					return "{\"status\": \"NOT_FOUND\"}";
				}
			} else if (userType != null) {
				Query query = em.createNamedQuery("PatientsByUserType");
				if (userType.toUpperCase().equals("PATIENT")) {
					query.setParameter("userType", Patient.UserType.PATIENT);
				} else if (userType.toUpperCase().equals("CONTROL")) {
					query.setParameter("userType", Patient.UserType.CONTROL);
				}
				return gson.toJson(query.getResultList());
			} else {
				resultList = em.createNamedQuery("AllPatients").getResultList();
				return gson.toJson(resultList);
			}
		} finally {
			em.close();
		}

	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public String createPatient(@RequestBody String body) {
		EntityManager em = emf.createEntityManager();

		try {
			Patient patient = gson.fromJson(body, Patient.class);

			Query query = em.createNamedQuery("PatientByEmail");
			query.setParameter("email", patient.getEmail());
			List<Patient> resultList = query.getResultList();
			if (resultList.size() > 0) {
				return "{\"status\": \"USER_EXISTS\"}";
			} else {
				patient.setCreated(new Timestamp(System.currentTimeMillis()));
				patient.setPassword(HashCode.getHashPassword(patient
						.getPassword()));
				em.getTransaction().begin();
				em.persist(patient);
				em.getTransaction().commit();
				return "{\"status\": \"OK\"}";
			}
		} finally {
			em.close();
		}
	}

	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public String deletePatient(
			@RequestParam(value = "email", required = true) String email) {
		EntityManager em = emf.createEntityManager();
		List<Patient> resultList;

		try {
			Query query = em.createNamedQuery("PatientByEmail");
			query.setParameter("email", email);
			resultList = query.getResultList();

			if (resultList.size() > 0) {
				em.getTransaction().begin();
				Patient patientToRemove = em.merge(resultList.get(0));
				em.remove(patientToRemove);
				em.getTransaction().commit();
				return "{\"status\": \"OK\"}";
			} else {
				return "{\"status\": \"NOT_FOUND\"}";
			}
		} finally {
			em.close();
		}
	}

}
