package com.sap.als.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.sap.als.excel.ExcelCreator;
import com.sap.als.persistence.Patient;
import com.sap.als.persistence.Questionnaire;
import com.sap.als.persistence.SpeechTest;
import com.sap.als.persistence.SpeechTestRecording;
import com.sap.als.persistence.WritingTest;
import com.sap.als.utils.EmfGenerator;
import com.sap.als.utils.Task;

@RestController
@RequestMapping("/tasks")
@SuppressWarnings("unchecked")
public class TasksController {

	private DataSource ds;
	private EntityManagerFactory emf;
	private Gson gson;

	public TasksController() throws ServletException {
		emf = EmfGenerator.initEntityManagerFactory(ds);
		gson = com.sap.als.utils.GsonBuilder.create();
	}

	@RequestMapping(value = "/lastSubmitted", method = RequestMethod.GET)
	@ResponseBody
	public String getLastSubmittedTasks(HttpSession session) {
		long patientId = (long) session.getAttribute("patientId");
		ArrayList<Task> tasks = new ArrayList<Task>();
		EntityManager em = emf.createEntityManager();
		Task task;

		try {
			Query query = em.createNamedQuery("LastQuestionnaireByPatientId");
			query.setParameter("patientId", patientId);
			List<Questionnaire> questionnairesResults = query.getResultList();
			if (questionnairesResults.size() > 0) {
				task = new Task();
				task.setTaskId("QUESTIONNAIRE");
				task.setLastSubmittedDate(questionnairesResults.get(0)
						.getCreated());
				tasks.add(task);
			}

			query = em.createNamedQuery("LastWritingTestByPatientId");
			query.setParameter("patientId", patientId);
			List<WritingTest> writingTestsResults = query.getResultList();
			if (writingTestsResults.size() > 0) {
				task = new Task();
				task.setTaskId("WRITING_TEST");
				task.setLastSubmittedDate(writingTestsResults.get(0)
						.getCreated());
				tasks.add(task);
			}

			query = em.createNamedQuery("LastSpeechTestByPatientId");
			query.setParameter("patientId", patientId);
			List<SpeechTest> speechTestsResults = query.getResultList();
			if (speechTestsResults.size() > 0) {
				task = new Task();
				task.setTaskId("SPEECH_TEST");
				task.setLastSubmittedDate(speechTestsResults.get(0)
						.getCreated());
				tasks.add(task);
			}

			return gson.toJson(tasks);
		} finally {
			em.close();
		}
	}

	@RequestMapping(value = "/questionnaires", method = RequestMethod.GET)
	@ResponseBody
	public String getQuestionnaires(
			@RequestParam(value = "id", required = false) String id) {
		EntityManager em = emf.createEntityManager();
		List<Questionnaire> resultList;

		try {
			if (id != null) {
				Query query = em.createNamedQuery("QuestionnaireById");
				query.setParameter("id", Long.parseLong(id));
				resultList = query.getResultList();

				if (resultList.size() > 0) {
					return gson.toJson(resultList.get(0));
				} else {
					return "{\"status\": \"NOT_FOUND\"}";
				}
			} else {
				resultList = em.createNamedQuery("AllQuestionnaires")
						.getResultList();
				return gson.toJson(resultList);
			}
		} finally {
			em.close();
		}
	}

	@RequestMapping(value = "/questionnaires", method = RequestMethod.POST)
	@ResponseBody
	public String createQuestionnaires(@RequestBody String body, HttpSession session) {
		EntityManager em = emf.createEntityManager();
		long patientId = (long) session.getAttribute("patientId");

		try {
			Questionnaire questionnaire = gson.fromJson(body,
					Questionnaire.class);
			questionnaire.setPatientId(patientId);
			questionnaire.setCreated(new Timestamp(System.currentTimeMillis()));
			em.getTransaction().begin();
			em.persist(questionnaire);
			em.getTransaction().commit();

			return "{\"status\": \"OK\", \"id\": " + questionnaire.getId()
					+ "}";
		} finally {
			em.close();
		}
	}

	@RequestMapping(value = "/questionnaires", method = RequestMethod.DELETE)
	@ResponseBody
	public String deleteQuestionnaire(
			@RequestParam(value = "id", required = true) long id) {
		EntityManager em = emf.createEntityManager();
		List<Questionnaire> resultList;

		try {
			Query query = em.createNamedQuery("QuestionnaireById");
			query.setParameter("id", id);
			resultList = query.getResultList();

			if (resultList.size() > 0) {
				em.getTransaction().begin();
				Questionnaire toRemove = em.merge(resultList.get(0));
				em.remove(toRemove);
				em.getTransaction().commit();
				return "{\"status\": \"OK\"}";
			} else {
				return "{\"status\": \"NOT_FOUND\"}";
			}
		} finally {
			em.close();
		}
	}

	@RequestMapping(value = "/questionnaires/excel", method = RequestMethod.GET)
	@ResponseBody
	public void getQuestionnairesExcel(HttpServletResponse response) {
		EntityManager em = emf.createEntityManager();

		try {
			List<Questionnaire> resultList = em.createNamedQuery(
					"AllQuestionnaires").getResultList();
			List<Patient> patientsList = em.createNamedQuery("AllPatients")
					.getResultList();
			response.setHeader("Content-Disposition",
					"attachment;filename=questionnaires.xls");
			ExcelCreator.generateQuestionnairesExcel(response, resultList,
					patientsList);
		} finally {
			em.close();
		}
	}

	@RequestMapping(value = "/writingTests", method = RequestMethod.GET)
	@ResponseBody
	public String getWritingTests(
			@RequestParam(value = "id", required = false) String id) {
		EntityManager em = emf.createEntityManager();
		List<WritingTest> resultList;

		try {
			if (id != null) {
				Query query = em.createNamedQuery("WritingTestById");
				query.setParameter("id", Long.parseLong(id));
				resultList = query.getResultList();

				if (resultList.size() > 0) {
					return gson.toJson(resultList.get(0));
				} else {
					return "{\"status\": \"NOT_FOUND\"}";
				}
			} else {
				resultList = em.createNamedQuery("AllWritingTests")
						.getResultList();
				return gson.toJson(resultList);
			}
		} finally {
			em.close();
		}
	}

	@RequestMapping(value = "/writingTests", method = RequestMethod.POST)
	@ResponseBody
	public String createWritingTest(@RequestBody String body, HttpSession session) {
		EntityManager em = emf.createEntityManager();
		long patientId = (long) session.getAttribute("patientId");

		try {
			WritingTest writingTest = gson.fromJson(body, WritingTest.class);
			writingTest.setPatientId(patientId);
			writingTest.setCreated(new Timestamp(System.currentTimeMillis()));
			em.getTransaction().begin();
			em.persist(writingTest);
			em.getTransaction().commit();

			return "{\"status\": \"OK\", \"id\": " + writingTest.getId() + "}";
		} finally {
			em.close();
		}
	}

	@RequestMapping(value = "/writingTests", method = RequestMethod.DELETE)
	@ResponseBody
	public String deleteWritingTest(
			@RequestParam(value = "id", required = true) long id) {
		EntityManager em = emf.createEntityManager();
		List<WritingTest> resultList;

		try {
			Query query = em.createNamedQuery("WritingTestById");
			query.setParameter("id", id);
			resultList = query.getResultList();

			if (resultList.size() > 0) {
				em.getTransaction().begin();
				WritingTest writingTestToRemove = em.merge(resultList.get(0));
				em.remove(writingTestToRemove);
				em.getTransaction().commit();
				return "{\"status\": \"OK\"}";
			} else {
				return "{\"status\": \"NOT_FOUND\"}";
			}
		} finally {
			em.close();
		}
	}

	@RequestMapping(value = "/speechTests", method = RequestMethod.GET)
	@ResponseBody
	public String getSpeechTests(
			@RequestParam(value = "id", required = false) String id,
			HttpServletResponse response) throws IOException {
		EntityManager em = emf.createEntityManager();

		try {
			if (id != null) {
				Query query = em.createNamedQuery("SpeechTestById");
				query.setParameter("id", Long.parseLong(id));
				List<SpeechTest> resultList = query.getResultList();

				if (resultList.size() > 0) {
								
					List<SpeechTestRecording> recordings = resultList.get(0)
							.getRecordings();
					for (int j = 0; j < recordings.size(); j++) {
						recordings.get(j).setRecording(null);
					}	
					
					return gson.toJson(resultList.get(0));
				} else {
					return "{\"status\": \"NOT_FOUND\"}";
				}
			} else {
				List<SpeechTest> resultList = em.createNamedQuery(
						"AllSpeechTests").getResultList();
				for (int i = 0; i < resultList.size(); i++) {
					List<SpeechTestRecording> recordings = resultList.get(i)
							.getRecordings();
					for (int j = 0; j < recordings.size(); j++) {
						recordings.get(j).setRecording(null);
					}
				}
				return gson.toJson(resultList);
			}
		} finally {
			em.close();
		}
	}
	
	@RequestMapping(value = "/speechTests/{id}/recording/{testId}", method = RequestMethod.GET)
	@ResponseBody
	public String getSpeechTests(
			@PathVariable(value = "id") Long id, @PathVariable(value = "testId") Integer testId,
			HttpServletResponse response) throws IOException {
		EntityManager em = emf.createEntityManager();

		try {
			
				Query query = em.createNamedQuery("SpeechTestById");
				query.setParameter("id", id);
				List<SpeechTest> resultList = query.getResultList();

				if (resultList.size() > 0) {
								
					List<SpeechTestRecording> recordings = resultList.get(0)
							.getRecordings();
					for (int j = 0; j < recordings.size(); j++) {
						SpeechTestRecording recording = recordings.get(j);
						if ( testId.equals(recording.getTestId())) {
							
							response.setHeader("Content-Disposition",
									"attachment; filename=recording.mp3");
							response.getOutputStream().write(
									recording.getRecording());
							response.flushBuffer(); 
							return "{\"status\": \"OK\"}";
						}
						
					}	
					return "{\"status\": \"NOT_FOUND\"}";
					
				} else {
					return "{\"status\": \"NOT_FOUND\"}";
				}
			
		} finally {
			em.close();
		}
	}

	@RequestMapping(value = "/speechTests", method = RequestMethod.POST)
	@ResponseBody
	public String createSpeechTest(
			HttpSession session,
			@RequestParam(value = "testIds", required = false) Integer[] testIds,
			@RequestParam(value = "files", required = false) MultipartFile[] files) {
		long patientId = (long) session.getAttribute("patientId");
		EntityManager em = emf.createEntityManager();

		try {
			SpeechTest test = new SpeechTest();
			test.setPatientId(patientId);
			test.setCreated(new Timestamp(System.currentTimeMillis()));
			ArrayList<SpeechTestRecording> recordings = new ArrayList<SpeechTestRecording>();

			for (int i = 0; i < testIds.length; i++) {
				this.createRecording(recordings, testIds[i], files[i]);
			}

			test.setRecordings(recordings);

			em.getTransaction().begin();
			em.persist(test);
			em.getTransaction().commit();
			return "{\"status\": \"OK\", \"id\": " + test.getId() +    "}";
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return "{\"status\": \"ERROR\"}";
		} finally {
			em.close();
		}
	}

	private void createRecording(ArrayList<SpeechTestRecording> recordings,
			Integer answerId, MultipartFile file) throws IOException {
		if (answerId != null && file != null) {
			SpeechTestRecording recording = new SpeechTestRecording();
			recording.setTestId(answerId);
			recording.setRecording(file.getBytes());
			recordings.add(recording);
		}
	}
	
	@RequestMapping(value = "/speechTests", method = RequestMethod.DELETE)
	@ResponseBody
	public String deleteSpeechTest(
			@RequestParam(value = "id", required = true) long id) {
		EntityManager em = emf.createEntityManager();
		List<SpeechTest> resultList;

		try {
			Query query = em.createNamedQuery("SpeechTestById");
			query.setParameter("id", id);
			resultList = query.getResultList();

			if (resultList.size() > 0) {
				em.getTransaction().begin();
				SpeechTest speechTestToRemove = em.merge(resultList.get(0));
				em.remove(speechTestToRemove);
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
