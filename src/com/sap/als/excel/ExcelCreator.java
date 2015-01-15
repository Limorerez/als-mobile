package com.sap.als.excel;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.sap.als.persistence.Patient;
import com.sap.als.persistence.Questionnaire;
import com.sap.als.persistence.QuestionnaireAnswer;

public class ExcelCreator {

	private static String[] columns = { "Id", "Gender", "Year of birth",
			"Date of diagnosis", "Speech", "Salivation", "Swallowing",
			"Handwriting", "Cutting food with gastrostomy",
			"Dressing and hygiene", "Turning in bed", "Walking",
			"Climbing stairs", "Dyspnea", "Orthopnea",
			"Respiratory insufficiency" };

	public static void generateQuestionnairesExcel(
			HttpServletResponse response, List<Questionnaire> resultList,
			List<Patient> patients) {
		try {
			Workbook wb = new HSSFWorkbook();
			Sheet sheet = wb.createSheet("Questionnaires");
			CreationHelper factory = wb.getCreationHelper();
			Drawing drawing = sheet.createDrawingPatriarch();

			SimpleDateFormat yearSdf = new SimpleDateFormat("yyyy");
			SimpleDateFormat monthYearSdf = new SimpleDateFormat("MM/yyyy");

			Font defaultFont = wb.getFontAt((short) 0);
			defaultFont.setFontHeightInPoints((short) 12);

			CellStyle boldStyle = wb.createCellStyle();
			Font boldFont = wb.createFont();
			boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			boldFont.setFontHeightInPoints((short) 12);
			boldStyle.setFont(boldFont);

			Row row;
			Cell cell;

			int rowNumber = 0;
			Patient thisPatient = null;

			row = sheet.createRow((short) rowNumber);
			for (int i = 0; i < columns.length; i++) {
				cell = row.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(boldStyle);
			}

			for (int questionnaireNumber = 0; questionnaireNumber < resultList
					.size(); questionnaireNumber++) {
				Questionnaire questionnaire = resultList
						.get(questionnaireNumber);
				if (thisPatient == null
						|| !(questionnaire.getPatientId() ==
								thisPatient.getId())) {
					thisPatient = getPatientById(questionnaire.getPatientId(),
							patients);
					rowNumber++;
				}

				rowNumber++;
				List<QuestionnaireAnswer> answers = questionnaire.getAnswers();
				row = sheet.createRow((short) rowNumber);

				cell = row.createCell(0);
				cell.setCellValue(thisPatient.getId());
				cell = row.createCell(1);
				cell.setCellValue(thisPatient.getGender() == 0 ? "Male"
						: "Female");
				cell = row.createCell(2);
				cell.setCellValue(Integer.parseInt(yearSdf.format(thisPatient
						.getBirthday())));
				cell = row.createCell(3);
				cell.setCellValue(monthYearSdf.format(thisPatient
						.getDiagnoseDate()));

				for (int answerNum = 0; answerNum < answers.size(); answerNum++) {
					QuestionnaireAnswer questionnaireAnswer = answers
							.get(answerNum);
					cell = row
							.createCell(questionnaireAnswer.getQuestionId() + 3);

					Integer answer = questionnaireAnswer.getAnswer();
					String remark = questionnaireAnswer.getRemark();
					if (answer != null) {
						cell.setCellValue(answer);
					}
					if (remark != null) {
						ClientAnchor anchor = factory.createClientAnchor();
						anchor.setCol1(cell.getColumnIndex());
						anchor.setCol2(cell.getColumnIndex() + 5);
						anchor.setRow1(row.getRowNum());
						anchor.setRow2(row.getRowNum() + 3);

						Comment comment = drawing.createCellComment(anchor);
						RichTextString str = factory
								.createRichTextString(remark);
						comment.setString(str);
						cell.setCellComment(comment);
					}
				}
			}

			wb.write(response.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Patient getPatientById(long id,
			List<Patient> patients) {
		Patient patient;
		for (int i = 0; i < patients.size(); i++) {
			patient = patients.get(i);
			if (patient.getId() == id) {
				return patient;
			}
		}
		return null;
	}

}
