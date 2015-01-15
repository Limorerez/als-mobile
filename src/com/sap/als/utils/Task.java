package com.sap.als.utils;

import java.sql.Timestamp;

public class Task {
	private String taskId;
	private Timestamp lastSubmittedDate;
	
	public String getTaskId() {
		return taskId;
	}
	
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	public Timestamp getLastSubmittedDate() {
		return lastSubmittedDate;
	}

	public void setLastSubmittedDate(Timestamp lastSubmittedDate) {
		this.lastSubmittedDate = lastSubmittedDate;
	}

}
