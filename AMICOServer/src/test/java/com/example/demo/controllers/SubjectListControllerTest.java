package com.example.demo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.example.demo.course.Course;
import com.example.demo.course.CourseRepository;
import com.example.demo.subject.Subject;
import com.example.demo.subject.SubjectRepository;
import com.example.demo.user.SessionUserComponent;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

/**
 * Testes unitários do SubjectListController (gestão de disciplinas de um curso).
 * Repositórios mockados; a lógica testada é a do controller real.
 */
public class SubjectListControllerTest {

	private static final String COURSE_NAME = "qualidade-software";
	private static final String SUBJECT_NAME = "teste-de-unidade";
	private static final String OVERVIEW = "HTML/StudentCourses/student-course-overview";
	private static final String ERROR_VIEW = "/error/";

	@Mock
	private CourseRepository courseRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private SubjectRepository subjectRepository;
	@Mock
	private SessionUserComponent sessionUserComponent;

	@InjectMocks
	private SubjectListController controller;

	private Model model;
	private User admin;
	private User student;
	private User teacher;
	private Course course;
	private Subject subject;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		model = new ExtendedModelMap();

		admin = new User("admin", "senha1234", "admin@mail.com", false);
		admin.setUserID(1);
		admin.setRoles(new ArrayList<String>(Arrays.asList("ROLE_USER", "ROLE_ADMIN")));

		student = new User("maria", "senha1234", "maria@mail.com", true);
		student.setUserID(2);

		teacher = new User("carlos", "senha1234", "carlos@mail.com", false);
		teacher.setUserID(3);

		course = new Course("Qualidade de Software", "Curso de qualidade");
		course.setInternalName(COURSE_NAME);

		subject = new Subject("Teste de Unidade");
		subject.setCourse(course);
		course.getSubjects().add(subject);

		when(courseRepository.findByInternalName(COURSE_NAME)).thenReturn(course);
		when(userRepository.findByUsername("carlos")).thenReturn(teacher);
	}

	// ---- allCourses: quem pode ver o overview ----

	@Test
	public void overview_inscribedStudentSeesThePage() {
		course.getInscribedUsers().add(student);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		when(userRepository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(admin, teacher)));

		String view = controller.allCourses(model, COURSE_NAME);

		assertEquals(OVERVIEW, view);
		assertEquals(subject, ((java.util.List<?>) model.asMap().get("subjects")).get(0));
		assertEquals("Qualidade de Software", model.asMap().get("courseName"));
		assertEquals(COURSE_NAME, model.asMap().get("courseInternalName"));
		assertEquals("maria", model.asMap().get("userInternalName"));
		assertFalse((boolean) model.asMap().get("admin"));
	}

	@Test
	public void overview_adminSeesThePageEvenIfNotInscribed() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);
		when(userRepository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(admin, teacher)));

		String view = controller.allCourses(model, COURSE_NAME);

		assertEquals(OVERVIEW, view);
		assertTrue((boolean) model.asMap().get("admin"));
	}

	@Test
	public void overview_nonInscribedStudentGetsTheErrorPage() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);

		String view = controller.allCourses(model, COURSE_NAME);

		assertEquals(ERROR_VIEW, view);
	}

	@Test
	public void overview_anonymousUserGetsTheErrorPage() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(null);

		String view = controller.allCourses(model, COURSE_NAME);

		assertEquals(ERROR_VIEW, view);
	}

	@Test
	public void overview_onlyNonAdminTeachersAreListed() {
		course.getInscribedUsers().add(student);
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		when(userRepository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(admin, teacher, student)));

		controller.allCourses(model, COURSE_NAME);

		@SuppressWarnings("unchecked")
		java.util.List<User> teachers = (java.util.List<User>) model.asMap().get("allTeachers");
		assertEquals(1, teachers.size());
		assertEquals(teacher, teachers.get(0));
	}

	// ---- createSubject ----

	@Test
	public void createSubject_adminCreatesAndCourseGetsTheSubject() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		controller.createSubject(model, COURSE_NAME, "Teste de Integração");

		Subject created = course.getSubjects().get(1);
		assertEquals("Teste de Integração", created.getName());
		// o internalName é sempre convertido para minúsculas
		assertEquals("teste-de-integração", created.getInternalName());
		assertEquals(course, created.getCourse());
		verify(subjectRepository).save(created);
		verify(courseRepository).save(course);
	}

	@Test
	public void createSubject_emptyNameIsIgnored() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		controller.createSubject(model, COURSE_NAME, "");

		assertEquals(1, course.getSubjects().size());
		verify(subjectRepository, never()).save(org.mockito.Matchers.any(Subject.class));
	}

	@Test
	public void createSubject_nonAdminCannotCreate() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);

		controller.createSubject(model, COURSE_NAME, "Teste de Integração");

		assertEquals(1, course.getSubjects().size());
		verify(subjectRepository, never()).save(org.mockito.Matchers.any(Subject.class));
	}

	@Test
	public void createSubject_unknownCourseIsIgnored() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);
		when(courseRepository.findByInternalName("inexistente")).thenReturn(null);

		controller.createSubject(model, "inexistente", "Teste de Integração");

		verify(subjectRepository, never()).save(org.mockito.Matchers.any(Subject.class));
	}

	// ---- updateSubject ----

	@Test
	public void updateSubject_adminRenamesAndChangesDescription() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		controller.updateSubject(model, COURSE_NAME, SUBJECT_NAME, "Testes de Unidade", "Nova descrição");

		assertEquals("Testes de Unidade", subject.getName());
		// o internalName é sempre convertido para minúsculas
		assertEquals("testes-de-unidade", subject.getInternalName());
		assertEquals("Nova descrição", subject.getDescription());
		verify(subjectRepository).save(subject);
	}

	@Test
	public void updateSubject_emptyFieldsKeepTheOldValues() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		controller.updateSubject(model, COURSE_NAME, SUBJECT_NAME, "", "Só a descrição");

		assertEquals("Teste de Unidade", subject.getName());
		assertEquals("teste-de-unidade", subject.getInternalName());
		assertEquals("Só a descrição", subject.getDescription());
	}

	@Test
	public void updateSubject_nonAdminCannotUpdate() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);

		controller.updateSubject(model, COURSE_NAME, SUBJECT_NAME, "Outro nome", "Outra descrição");

		assertEquals("Teste de Unidade", subject.getName());
		verify(subjectRepository, never()).save(org.mockito.Matchers.any(Subject.class));
	}

	// ---- deleteSubject ----

	@Test
	public void deleteSubject_adminRemovesTheSubjectFromTheCourse() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		controller.deleteSubject(model, COURSE_NAME, SUBJECT_NAME);

		assertTrue(course.getSubjects().isEmpty());
		assertNull(subject.getCourse());
		verify(courseRepository).save(course);
	}

	@Test
	public void deleteSubject_nonAdminCannotDelete() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);

		controller.deleteSubject(model, COURSE_NAME, SUBJECT_NAME);

		assertEquals(1, course.getSubjects().size());
		assertEquals(course, subject.getCourse());
		verify(courseRepository, never()).save(org.mockito.Matchers.any(Course.class));
	}

	// BUG conhecido: se a disciplina não existe, toDelete fica null e o controller
	// lança NullPointerException em toDelete.setCourse(null). Documentado na issue #X.
	@Test
	public void deleteSubject_unknownSubjectThrowsNullPointerException() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		try {
			controller.deleteSubject(model, COURSE_NAME, "inexistente");
			throw new AssertionError("Era esperado um NullPointerException");
		} catch (NullPointerException expected) {
			// comportamento atual (bug): NPE em vez de mensagem amigável
		}
	}

	// ---- ChangeTeachersFromSubject ----

	@Test
	public void changeTeachers_replacesTheOldTeachers() {
		subject.getTeachers().add(teacher);
		teacher.getTeaching().add(subject);
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		controller.ChangeTeachersFromSubject(model, COURSE_NAME, SUBJECT_NAME, new String[] { "carlos" });

		// o professor antigo é removido e o novo entra
		assertEquals(1, subject.getTeachers().size());
		assertTrue(subject.getTeachers().contains(teacher));
		verify(subjectRepository).save(subject);
	}

	@Test
	public void changeTeachers_emptySelectionChangesNothing() {
		subject.getTeachers().add(teacher);
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		controller.ChangeTeachersFromSubject(model, COURSE_NAME, SUBJECT_NAME, new String[] {});

		assertEquals(1, subject.getTeachers().size());
		verify(subjectRepository, never()).save(org.mockito.Matchers.any(Subject.class));
	}

	@Test
	public void changeTeachers_nonAdminCannotChange() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);

		controller.ChangeTeachersFromSubject(model, COURSE_NAME, SUBJECT_NAME, new String[] { "carlos" });

		assertTrue(subject.getTeachers().isEmpty());
		verify(subjectRepository, never()).save(org.mockito.Matchers.any(Subject.class));
	}

	// BUG conhecido: username inexistente faz userRepository.findByUsername retornar
	// null e o controller lança NullPointerException em newTeachers.get(i).getTeaching().
	// Documentado na issue #Y.
	@Test
	public void changeTeachers_unknownUsernameThrowsNullPointerException() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);
		when(userRepository.findByUsername("fantasma")).thenReturn(null);

		try {
			controller.ChangeTeachersFromSubject(model, COURSE_NAME, SUBJECT_NAME, new String[] { "fantasma" });
			throw new AssertionError("Era esperado um NullPointerException");
		} catch (NullPointerException expected) {
			// comportamento atual (bug): NPE em vez de ignorar o username desconhecido
		}
	}
	// A ordem importa: cada mutação altera o estado, então excluímos antes de recriar
	// e usamos o internalName real (minúsculas) nas chamadas seguintes.
	@Test
	public void mutationsRedirectBackToTheOverview() {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		assertEquals("redirect:/course-overview/" + COURSE_NAME,
				controller.deleteSubject(model, COURSE_NAME, SUBJECT_NAME).getViewName());
		assertEquals("redirect:/course-overview/" + COURSE_NAME,
				controller.createSubject(model, COURSE_NAME, "Nova disciplina").getViewName());
		assertEquals("redirect:/course-overview/" + COURSE_NAME,
				controller.updateSubject(model, COURSE_NAME, "nova-disciplina", "n", "d").getViewName());
		assertEquals("redirect:/course-overview/" + COURSE_NAME,
				controller.ChangeTeachersFromSubject(model, COURSE_NAME, "n", new String[] { "carlos" })
						.getViewName());
		// createSubject, updateSubject e changeTeachers salvam a disciplina; delete não
		verify(subjectRepository, times(3)).save(org.mockito.Matchers.any(Subject.class));
	}
}