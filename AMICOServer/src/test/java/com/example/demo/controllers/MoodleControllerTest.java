package com.example.demo.controllers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.course.Course;
import com.example.demo.course.CourseRepository;
import com.example.demo.practices.Practices;
import com.example.demo.practices.PracticesRepository;
import com.example.demo.practices.PracticesService;
import com.example.demo.studyItem.StudyItem;
import com.example.demo.studyItem.StudyItemRepository;
import com.example.demo.studyItem.StudyItemService;
import com.example.demo.subject.Subject;
import com.example.demo.subject.SubjectRepository;
import com.example.demo.subject.SubjectService;
import com.example.demo.user.SessionUserComponent;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

/**
 * Testes unitários do MoodleController (página da disciplina dentro do curso:
 * itens de estudo, módulos, práticas, notas e downloads).
 * Repositórios e services mockados; a lógica testada é a do controller real.
 *
 * Defeitos encontrados (issues no GitHub):
 *  - allCourses com curso inexistente -> NullPointerException (reproduzido:
 *    HTTP 500 em execução)
 *  - allCourses com disciplina inexistente -> NullPointerException (reproduzido:
 *    HTTP 500 em execução)
 *  - uploadStudyItem sem verificação de usuário nulo -> fragilidade latente
 *    (a segurança do Spring intercepta anônimos em execução; o NPE só ocorre
 *    se a configuração mudar)
 */
public class MoodleControllerTest {

	private static final String COURSE_NAME = "qualidade-software";
	private static final String SUBJECT_NAME = "teste-de-unidade";
	private static final String SUBJECT_PAGE = "HTML/Moodle/student-subject";
	private static final String ERROR_VIEW = "/error/";

	@Mock
	private CourseRepository courseRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private SubjectRepository subjectRepository;
	@Mock
	private StudyItemRepository studyItemRepository;
	@Mock
	private PracticesRepository practicesRepository;
	@Mock
	private SubjectService subjectService;
	@Mock
	private StudyItemService studyItemService;
	@Mock
	private PracticesService practicesService;
	@Mock
	private SessionUserComponent sessionUserComponent;

	@InjectMocks
	private MoodleController controller;

	private Model model;
	private User student;
	private User teacher;
	private User otherTeacher;
	private Course course;
	private Subject subject;
	private StudyItem itemDeEstudo;
	private StudyItem pratica;
	private Practices entrega;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		model = new ExtendedModelMap();

		student = new User("maria", "senha1234", "maria@mail.com", true);
		student.setUserID(2);

		teacher = new User("carlos", "senha1234", "carlos@mail.com", false);
		teacher.setUserID(3);

		otherTeacher = new User("ana", "senha1234", "ana@mail.com", false);
		otherTeacher.setUserID(4);

		course = new Course("Qualidade de Software", "Curso de qualidade");
		course.setInternalName(COURSE_NAME);

		subject = new Subject("Teste de Unidade");
		subject.setCourse(course);
		subject.setNumberModules(2);
		course.getSubjects().add(subject);

		itemDeEstudo = new StudyItem("pdf", "Aula 1", 1, "aula1.pdf", "aula1.pdf");
		itemDeEstudo.setStudyItemID(10);

		pratica = new StudyItem("pdf", "Exercício 1", 1, "ex1.pdf", "ex1.pdf");
		pratica.setStudyItemID(11);
		pratica.setPractice(true);

		entrega = new Practices("Entrega 1");
		entrega.setPracticeID(20);
		entrega.setStudyItem(pratica);
		entrega.setOwner(student);
		pratica.getPractices().add(entrega);

		subject.getStudyItemsList().addAll(Arrays.asList(itemDeEstudo, pratica));

		when(courseRepository.findByInternalName(COURSE_NAME)).thenReturn(course);
		when(subjectService.checkForSubject(teacher, COURSE_NAME, SUBJECT_NAME)).thenReturn(subject);
		when(subjectService.checkForSubject(student, COURSE_NAME, SUBJECT_NAME)).thenReturn(subject);
	}

	// ---- allCourses: página da disciplina ----

	@Test
	public void allCourses_studentSeesModulesPracticesAndTeachers() {
		course.getInscribedUsers().add(student);
		subject.getTeachers().add(teacher);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		when(subjectService.getStudyItems(eq(subject), eq(1), any(org.springframework.data.domain.PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<>(Arrays.asList(itemDeEstudo))));
		when(subjectService.getStudyItems(eq(subject), eq(2), any(org.springframework.data.domain.PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<StudyItem>()));
		when(subjectService.getPractices(subject)).thenReturn(new ArrayList<>(Arrays.asList(pratica)));

		String view = controller.allCourses(model, null, COURSE_NAME, SUBJECT_NAME, 0);

		assertEquals(SUBJECT_PAGE, view);
		assertEquals("Qualidade de Software", model.asMap().get("courseName"));
		assertEquals("Teste de Unidade", model.asMap().get("subjectName"));
		assertEquals(false, model.asMap().get("isTeacher"));

		// 2 módulos -> 2 listas de itens
		List<?> allStudyItems = (List<?>) model.asMap().get("allStudyItems");
		assertEquals(2, allStudyItems.size());

		// para cada prática, o par [itemDeEstudo, entrega do aluno]
		List<?> studentPractices = (List<?>) model.asMap().get("studentPractices");
		assertEquals(1, studentPractices.size());
		assertEquals(Arrays.asList(pratica, entrega), studentPractices.get(0));

		// aluno vê os professores da disciplina
		List<?> usersToShow = (List<?>) model.asMap().get("usersToShowList");
		assertEquals(Arrays.asList(teacher), usersToShow);
	}

	@Test
	public void allCourses_studentWithoutSubmissionGetsANotPresentedPractice() {
		course.getInscribedUsers().add(student);
		pratica.getPractices().clear(); // aluno ainda não entregou nada
		Practices nova = new Practices("Not Presented", "Not Presented");
		nova.setPracticeID(30);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		when(subjectService.getStudyItems(eq(subject), org.mockito.Matchers.anyInt(),
				any(org.springframework.data.domain.PageRequest.class)))
					.thenReturn(new PageImpl<>(new ArrayList<StudyItem>()));
		when(subjectService.getPractices(subject)).thenReturn(new ArrayList<>(Arrays.asList(pratica)));
		when(practicesRepository.count()).thenReturn(1L);
		when(practicesRepository.getOne(1L)).thenReturn(nova);

		controller.allCourses(model, null, COURSE_NAME, SUBJECT_NAME, 0);

		// o sistema cria e persiste uma prática "Not Presented" ao visitar a página
		verify(practicesRepository).save(any(Practices.class));
		verify(studyItemRepository).save(pratica);
		assertTrue(pratica.getPractices().contains(nova));

		List<?> studentPractices = (List<?>) model.asMap().get("studentPractices");
		assertEquals(Arrays.asList(pratica, (Object) nova), studentPractices.get(0));
	}

	@Test
	public void allCourses_teacherSeesTheStudentList() {
		course.getInscribedUsers().addAll(Arrays.asList(teacher, student));
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);
		when(subjectService.getStudyItems(eq(subject), org.mockito.Matchers.anyInt(),
				any(org.springframework.data.domain.PageRequest.class)))
						.thenReturn(new PageImpl<>(new ArrayList<StudyItem>()));
		when(subjectService.getPractices(subject)).thenReturn(new ArrayList<>(Arrays.asList(pratica)));

		String view = controller.allCourses(model, null, COURSE_NAME, SUBJECT_NAME, 0);

		assertEquals(SUBJECT_PAGE, view);
		assertEquals(true, model.asMap().get("isTeacher"));

		// professor vê os alunos inscritos (ele próprio é removido da lista)
		List<?> usersToShow = (List<?>) model.asMap().get("usersToShowList");
		assertEquals(Arrays.asList(student), usersToShow);

		// professor não recebe a lista de práticas próprias
		assertNull(model.asMap().get("studentPractices"));
	}

	@Test
	public void allCourses_nonInscribedUserGetsTheErrorPage() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);

		String view = controller.allCourses(model, null, COURSE_NAME, SUBJECT_NAME, 0);

		assertEquals(ERROR_VIEW, view);
	}

	// BUG conhecido: curso inexistente -> NPE em course.getInscribedUsers().
	// Reproduzido em execução (HTTP 500). Issue no GitHub (MoodleController).
	@Test
	public void allCourses_unknownCourseThrowsNullPointerException() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		when(courseRepository.findByInternalName("inexistente")).thenReturn(null);

		assertThrows(NullPointerException.class,
				() -> controller.allCourses(model, null, "inexistente", SUBJECT_NAME, 0));
	}

	// BUG conhecido: disciplina inexistente -> NPE em subject.getInternalName()
	// depois do bloco if (subject != null). Reproduzido em execução (HTTP 500).
	// Issue no GitHub (MoodleController).
	@Test
	public void allCourses_unknownSubjectThrowsNullPointerException() {
		course.getInscribedUsers().add(student);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);

		assertThrows(NullPointerException.class,
				() -> controller.allCourses(model, null, COURSE_NAME, "disciplina-inexistente", 0));
	}

	// ---- moduleAction: adicionar/remover módulo ----

	@Test
	public void moduleAction_teacherOfTheSubjectAddsAModule() throws Exception {
		subject.getTeachers().add(teacher);
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);

		ModelAndView mav = chamarPrivado("moduleAction", "add", COURSE_NAME, SUBJECT_NAME, null);

		verify(subjectService).addModule(subject);
		assertEquals("redirect:/moodle/" + COURSE_NAME + "/" + SUBJECT_NAME, mav.getViewName());
	}

	@Test
	public void moduleAction_teacherDeletesAModule() throws Exception {
		subject.getTeachers().add(teacher);
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);

		chamarPrivado("moduleAction", "delete", COURSE_NAME, SUBJECT_NAME, 1);

		verify(subjectService).deleteModule(subject, 1);
	}

	@Test
	public void moduleAction_deleteWithoutModuleNumberDoesNothing() throws Exception {
		subject.getTeachers().add(teacher);
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);

		chamarPrivado("moduleAction", "delete", COURSE_NAME, SUBJECT_NAME, null);

		verify(subjectService, never()).deleteModule(eq(subject), org.mockito.Matchers.anyInt());
	}

	@Test
	public void moduleAction_studentCannotAddOrDeleteModules() throws Exception {
		course.getInscribedUsers().add(student);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);

		chamarPrivado("moduleAction", "add", COURSE_NAME, SUBJECT_NAME, null);
		chamarPrivado("moduleAction", "delete", COURSE_NAME, SUBJECT_NAME, 1);

		verify(subjectService, never()).addModule(subject);
		verify(subjectService, never()).deleteModule(eq(subject), org.mockito.Matchers.anyInt());
	}

	@Test
	public void moduleAction_invalidOptionDoesNothing() throws Exception {
		subject.getTeachers().add(teacher);
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);

		chamarPrivado("moduleAction", "rename", COURSE_NAME, SUBJECT_NAME, null);

		verify(subjectService, never()).addModule(subject);
		verify(subjectService, never()).deleteModule(eq(subject), org.mockito.Matchers.anyInt());
	}

	@Test
	public void moduleAction_anonymousUserDoesNothing() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(null);

		chamarPrivado("moduleAction", "add", COURSE_NAME, SUBJECT_NAME, null);

		verify(subjectService, never()).addModule(subject);
	}

	// ---- uploadStudyItem: upload de item de estudo ----

	@Test
	public void uploadStudyItem_teacherOfTheSubjectUploadsAFile() throws Exception {
		subject.getTeachers().add(teacher);
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);
		MockMultipartFile arquivo = new MockMultipartFile("itemFile", "aula2.pdf", "application/pdf",
				"conteudo".getBytes());

		chamarPrivado("uploadStudyItem", COURSE_NAME, SUBJECT_NAME, 2, "Aula 2", "pdf", arquivo);

		verify(studyItemService).createStudyItem(arquivo, subject, 2, "pdf", "Aula 2");
	}

	@Test
	public void uploadStudyItem_emptyNameDoesNothing() throws Exception {
		subject.getTeachers().add(teacher);
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);
		MockMultipartFile arquivo = new MockMultipartFile("itemFile", "aula2.pdf", "application/pdf",
				"conteudo".getBytes());

		chamarPrivado("uploadStudyItem", COURSE_NAME, SUBJECT_NAME, 2, "", "pdf", arquivo);

		verify(studyItemService, never()).createStudyItem(any(), eq(subject), org.mockito.Matchers.anyInt(),
				org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString());
	}

	@Test
	public void uploadStudyItem_studentCannotUpload() throws Exception {
		course.getInscribedUsers().add(student);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		MockMultipartFile arquivo = new MockMultipartFile("itemFile", "aula2.pdf", "application/pdf",
				"conteudo".getBytes());

		chamarPrivado("uploadStudyItem", COURSE_NAME, SUBJECT_NAME, 2, "Aula 2", "pdf", arquivo);

		verify(studyItemService, never()).createStudyItem(any(), eq(subject), org.mockito.Matchers.anyInt(),
				org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString());
	}

	// Fragilidade latente: usuário anônimo -> NPE em user.isStudent() (os outros
	// métodos verificam user != null, este não). Em execução a segurança do Spring
	// intercepta anônimos (302 -> /login); o NPE só ocorre se a configuração mudar.
	// Issue no GitHub (MoodleController).
	@Test
	public void uploadStudyItem_anonymousUserThrowsNullPointerException() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(null);
		MockMultipartFile arquivo = new MockMultipartFile("itemFile", "aula2.pdf", "application/pdf",
				"conteudo".getBytes());

		assertThrows(NullPointerException.class,
				() -> chamarPrivado("uploadStudyItem", COURSE_NAME, SUBJECT_NAME, 2, "Aula 2", "pdf", arquivo));
	}

	// ---- getStudyItemFile: download de item ou prática ----

	@Test
	public void downloadStudyItem_userInscribedDownloadsTheFile() throws Exception {
		course.getInscribedUsers().add(student);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		byte[] conteudo = "conteudo-do-item".getBytes();
		Path arquivo = criarArquivoTemporario(conteudo);
		when(studyItemService.getStudyItemFile(anyLong(), anyLong(), eq(10L))).thenReturn(arquivo);

		MockHttpServletResponse res = new MockHttpServletResponse();
		chamarPrivado("getStudyItemFile", COURSE_NAME, SUBJECT_NAME, 10L, Optional.empty(), res);

		assertArrayEquals(conteudo, res.getContentAsByteArray());
		assertTrue(res.getHeader("Content-Disposition").contains("aula1.pdf"));
	}

	@Test
	public void downloadPractice_studentDownloadsOwnPractice() throws Exception {
		course.getInscribedUsers().add(student);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		byte[] conteudo = "conteudo-da-pratica".getBytes();
		Path arquivo = criarArquivoTemporario(conteudo);
		when(practicesService.getPracticeFile(pratica, entrega)).thenReturn(arquivo);

		MockHttpServletResponse res = new MockHttpServletResponse();
		chamarPrivado("getStudyItemFile", COURSE_NAME, SUBJECT_NAME, 11L, Optional.of(20L), res);

		assertArrayEquals(conteudo, res.getContentAsByteArray());
	}

	@Test
	public void downloadPractice_studentCannotDownloadAnotherStudentsPractice() throws Exception {
		course.getInscribedUsers().add(student);
		User outroAluno = new User("joao", "senha1234", "joao@mail.com", true);
		outroAluno.setUserID(9);
		entrega.setOwner(outroAluno);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);

		MockHttpServletResponse res = new MockHttpServletResponse();
		chamarPrivado("getStudyItemFile", COURSE_NAME, SUBJECT_NAME, 11L, Optional.of(20L), res);

		// nada é escrito na resposta e o arquivo do serviço não é consultado
		assertEquals(0, res.getContentAsByteArray().length);
		verify(practicesService, never()).getPracticeFile(pratica, entrega);
	}

	@Test
	public void downloadStudyItem_unknownCourseDoesNothing() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		when(courseRepository.findByInternalName("inexistente")).thenReturn(null);

		MockHttpServletResponse res = new MockHttpServletResponse();
		chamarPrivado("getStudyItemFile", "inexistente", SUBJECT_NAME, 10L, Optional.empty(), res);

		assertEquals(0, res.getContentAsByteArray().length);
	}

	// ---- modifyStudyItem: salvar/excluir item ----

	@Test
	public void modifyStudyItem_saveButtonUpdatesTheItem() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);
		when(subjectService.getStudyItem(subject, 10L)).thenReturn(itemDeEstudo);

		chamarPrivado("modifyStudyItem", COURSE_NAME, SUBJECT_NAME, 10L, "Aula 1 renomeada", "video", "save", null);

		ArgumentCaptor<StudyItem> salvo = ArgumentCaptor.forClass(StudyItem.class);
		verify(studyItemService).modifyStudyItem(eq(itemDeEstudo), salvo.capture());
		assertEquals("Aula 1 renomeada", salvo.getValue().getName());
	}

	@Test
	public void modifyStudyItem_deleteButtonRemovesANonPracticeItem() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);
		when(subjectService.getStudyItem(subject, 10L)).thenReturn(itemDeEstudo);

		chamarPrivado("modifyStudyItem", COURSE_NAME, SUBJECT_NAME, 10L, "x", "x", null, "delete");

		verify(studyItemService).deleteStudyItem(itemDeEstudo);
	}

	@Test
	public void modifyStudyItem_deletePracticeDetachesSubmissionsAndDeletesEverything() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);
		when(subjectService.getStudyItem(subject, 11L)).thenReturn(pratica);

		chamarPrivado("modifyStudyItem", COURSE_NAME, SUBJECT_NAME, 11L, "x", "x", null, "delete");

		// as entregas são desvinculadas antes da remoção
		assertNull(entrega.getStudyItem());
		assertNull(entrega.getOwner());
		assertTrue(pratica.getPractices().isEmpty());
		verify(studyItemRepository).delete(pratica);

		ArgumentCaptor<List> removidas = ArgumentCaptor.forClass(List.class);
		verify(practicesRepository).delete(removidas.capture());
		assertEquals(Arrays.asList(entrega), removidas.getValue());
	}

	@Test
	public void modifyStudyItem_noButtonDoesNothing() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);
		when(subjectService.getStudyItem(subject, 10L)).thenReturn(itemDeEstudo);

		chamarPrivado("modifyStudyItem", COURSE_NAME, SUBJECT_NAME, 10L, "novo", "video", null, null);

		verify(studyItemService, never()).modifyStudyItem(eq(itemDeEstudo), any(StudyItem.class));
		verify(studyItemService, never()).deleteStudyItem(itemDeEstudo);
	}

	// ---- updateCalificationPractice: professor dá nota ----

	@Test
	public void updateCalification_teacherSetsTheGrade() throws Exception {
		course.getInscribedUsers().addAll(Arrays.asList(teacher, student));
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);
		when(subjectService.getStudyItem(subject, 11L)).thenReturn(pratica);

		chamarPrivado("updateCalificationPractice", COURSE_NAME, SUBJECT_NAME, 11L, 20L, 8.5);

		assertEquals(8.5, entrega.getCalification(), 0.001);
		assertTrue(entrega.isCorrected());
		verify(practicesRepository).save(entrega);
	}

	@Test
	public void updateCalification_studentCannotSetGrades() throws Exception {
		course.getInscribedUsers().add(student);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		when(subjectService.getStudyItem(subject, 11L)).thenReturn(pratica);

		chamarPrivado("updateCalificationPractice", COURSE_NAME, SUBJECT_NAME, 11L, 20L, 8.5);

		assertEquals(0.0, entrega.getCalification(), 0.001);
		verify(practicesRepository, never()).save(entrega);
	}

	// ---- modifyPractice: aluno renomeia/reenvia a prática ----

	@Test
	public void modifyPractice_studentRenamesAndUploadsTheFile() throws Exception {
		course.getInscribedUsers().add(student);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		MockMultipartFile arquivo = new MockMultipartFile("itemFile", "entrega-final.pdf", "application/pdf",
				"minha entrega".getBytes());

		chamarPrivado("modifyPractice", COURSE_NAME, SUBJECT_NAME, 11L, 20L, "Entrega final", arquivo);

		assertEquals("Entrega final", entrega.getPracticeName());
		assertTrue(entrega.isPresented());
		verify(practicesRepository).save(entrega);
		verify(practicesService).createSubmissionFile(student, pratica, entrega, arquivo);
	}

	@Test
	public void modifyPractice_emptyNameDoesNothing() throws Exception {
		course.getInscribedUsers().add(student);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		MockMultipartFile arquivo = new MockMultipartFile("itemFile", "entrega-final.pdf", "application/pdf",
				"minha entrega".getBytes());

		chamarPrivado("modifyPractice", COURSE_NAME, SUBJECT_NAME, 11L, 20L, "", arquivo);

		assertEquals("Entrega 1", entrega.getPracticeName());
		verify(practicesRepository, never()).save(entrega);
	}

	@Test
	public void modifyPractice_teacherCannotModifyAPractice() throws Exception {
		course.getInscribedUsers().add(teacher);
		when(sessionUserComponent.getLoggedUser()).thenReturn(teacher);

		chamarPrivado("modifyPractice", COURSE_NAME, SUBJECT_NAME, 11L, 20L, "Tentativa", null);

		assertEquals("Entrega 1", entrega.getPracticeName());
		verify(practicesRepository, never()).save(entrega);
	}

	// ---- auxiliares ----

	private Path criarArquivoTemporario(byte[] conteudo) throws IOException {
		Path arquivo = Files.createTempFile("teste-download", ".bin");
		Files.write(arquivo, conteudo);
		arquivo.toFile().deleteOnExit();
		return arquivo;
	}

	// Os métodos de mutação do controller são private, então vão por reflexão.
	// O método é escolhido pelo nome e pelo número de argumentos.
	@SuppressWarnings("unchecked")
	private <T> T chamarPrivado(String nome, Object... args) throws Exception {
		for (Method m : MoodleController.class.getDeclaredMethods()) {
			if (m.getName().equals(nome) && m.getParameterTypes().length == args.length) {
				m.setAccessible(true);
				try {
					return (T) m.invoke(controller, args);
				} catch (InvocationTargetException e) {
					if (e.getCause() instanceof Exception) {
						throw (Exception) e.getCause();
					}
					throw e;
				}
			}
		}
		throw new IllegalArgumentException("Método não encontrado: " + nome);
	}
}