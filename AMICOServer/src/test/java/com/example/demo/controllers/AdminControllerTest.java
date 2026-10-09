package com.example.demo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.course.Course;
import com.example.demo.course.CourseRepository;
import com.example.demo.course.CourseService;
import com.example.demo.skill.Skill;
import com.example.demo.subject.Subject;
import com.example.demo.user.SessionUserComponent;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

public class AdminControllerTest {

	private static final String COURSE = "qualidade-software";
	private static final String REDIRECT = "redirect:/admin/";

	@Mock
	private UserRepository userRepository;
	@Mock
	private CourseRepository courseRepository;
	@Mock
	private SessionUserComponent sessionUserComponent;
	@Mock
	private CourseService courseService;

	@InjectMocks
	private AdminController controller;

	private User admin;
	private User student;
	private Course course;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		admin = new User("admin", "senha1234", "admin@mail.com", false);
		admin.setUserID(1);
		admin.setRoles(new ArrayList<String>(Arrays.asList("ROLE_USER", "ROLE_ADMIN")));
		student = new User("maria", "senha1234", "maria@mail.com", true);
		student.setUserID(2);

		course = new Course("Qualidade de Software", "Curso de qualidade");
		course.setInternalName(COURSE);
		course.setType("online");
		course.setCourseLanguage("Português");

		when(courseRepository.findByInternalName(COURSE)).thenReturn(course);
	}

	// ---- createCourse ----

	@Test
	public void createCourse_callsTheService() throws Exception {
		MultipartFile imagem = new MockMultipartFile("courseImage", "capa.jpg", "image/jpeg", "img".getBytes());
		Date inicio = Date.valueOf("2024-03-01");
		Date fim = Date.valueOf("2024-07-01");

		ModelAndView mav = chamarPrivado("createCourse", "Qualidade de Software", "Português", "online", "teste",
				"qualidade", "processo", inicio, fim, "Curso de qualidade", imagem);

		verify(courseService).createCourse("Qualidade de Software", "Português", "online", "teste", "qualidade",
				"processo", inicio, fim, "Curso de qualidade", imagem);
		assertEquals(REDIRECT, mav.getViewName());
	}

	@Test
	public void createCourse_aStudentCanCreateToo() throws Exception {
		// Hoje o controller não confere se o usuário é admin. Se isso mudar, o teste deve inverter.
		MultipartFile imagem = new MockMultipartFile("courseImage", "capa.jpg", "image/jpeg", "img".getBytes());
		Date inicio = Date.valueOf("2024-03-01");
		Date fim = Date.valueOf("2024-07-01");
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);

		chamarPrivado("createCourse", "Curso X", "Português", "online", "a", "b", "c", inicio, fim, "desc", imagem);

		verify(courseService).createCourse("Curso X", "Português", "online", "a", "b", "c", inicio, fim, "desc",
				imagem);
	}

	// ---- createTeacher ----

	@Test
	public void adminCreatesTeacher() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		ModelAndView mav = criarProfessor("professor1", "carlos@mail.com", "senha1234");

		ArgumentCaptor<User> salvo = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(salvo.capture());
		assertEquals("professor1", salvo.getValue().getUsername());
		assertEquals("carlos@mail.com", salvo.getValue().getUserMail());
		assertFalse(salvo.getValue().isStudent());
		assertFalse("senha1234".equals(salvo.getValue().getPassword()));
		assertEquals(REDIRECT, mav.getViewName());
	}

	@Test
	public void createTeacher_acceptsPasswordsFrom8To14Chars() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		criarProfessor("professor1", "carlos@mail.com", "12345678");
		criarProfessor("professor1", "carlos@mail.com", "12345678901234");

		verify(userRepository, times(2)).save(any(User.class));
	}

	@Test
	public void createTeacher_rejectsShortOrLongPasswords() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		criarProfessor("professor1", "carlos@mail.com", "1234567");
		ModelAndView mav = criarProfessor("professor1", "carlos@mail.com", "123456789012345");

		verify(userRepository, never()).save(any(User.class));
		assertEquals(REDIRECT, mav.getViewName());
	}

	@Test
	public void createTeacher_rejectsInvalidUsernames() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		criarProfessor("ab", "carlos@mail.com", "senha1234");
		criarProfessor("prof esor", "carlos@mail.com", "senha1234");
		criarProfessor("professor1234567x", "carlos@mail.com", "senha1234");

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	public void createTeacher_rejectsInvalidEmail() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		criarProfessor("professor1", "email-sem-arroba", "senha1234");

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	public void createTeacher_rejectsUsernameOrEmailAlreadyInUse() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		when(userRepository.findByUsername("professor1")).thenReturn(student);
		criarProfessor("professor1", "carlos@mail.com", "senha1234");

		when(userRepository.findByUsername("professor1")).thenReturn(null);
		when(userRepository.findByUserMail("carlos@mail.com")).thenReturn(student);
		criarProfessor("professor1", "carlos@mail.com", "senha1234");

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	public void createTeacher_onlyAdminsCanDoIt() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		criarProfessor("professor1", "carlos@mail.com", "senha1234");

		when(sessionUserComponent.getLoggedUser()).thenReturn(null);
		ModelAndView mav = criarProfessor("professor1", "carlos@mail.com", "senha1234");

		verify(userRepository, never()).save(any(User.class));
		assertEquals(REDIRECT, mav.getViewName());
	}

	@Test
	public void createTeacher_firstAndSecondNameAreNotUsed() throws Exception {
		// Comportamento atual: newName e newSecondName não chegam ao usuário criado.
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		criarProfessor("professor1", "carlos@mail.com", "senha1234");

		ArgumentCaptor<User> salvo = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(salvo.capture());
		assertEquals("", salvo.getValue().getUserFirstName());
		assertEquals("", salvo.getValue().getUserLastName());
	}

	// ---- modifyCourse: usuário (método ainda vazio) ----

	@Test
	public void deleteUserEndpointDoesNothingYet() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		ModelAndView mav = chamarPrivado("modifyCourse", "maria");

		verifyZeroInteractions(userRepository);
		assertEquals(REDIRECT, mav.getViewName());
	}

	// ---- modifyCourse: curso ----

	@Test
	public void saveUpdatesTheFilledFields() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		ModelAndView mav = chamarPrivado("modifyCourse", COURSE, "Novo nome", "Inglês", "presencial",
				"Nova descrição", "save", null);

		assertEquals("Novo nome", course.getName());
		assertEquals("Inglês", course.getCourseLanguage());
		assertEquals("presencial", course.getType());
		assertEquals("Nova descrição", course.getCourseDescription());
		verify(courseRepository).save(course);
		assertEquals(REDIRECT, mav.getViewName());
	}

	@Test
	public void saveKeepsValuesWhenFieldsComeEmpty() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		chamarPrivado("modifyCourse", COURSE, "", "", "", "Só a descrição mudou", "save", null);

		assertEquals("Qualidade de Software", course.getName());
		assertEquals("Português", course.getCourseLanguage());
		assertEquals("online", course.getType());
		assertEquals("Só a descrição mudou", course.getCourseDescription());
	}

	@Test
	public void renamingTheCourseAlsoChangesItsInternalName() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		chamarPrivado("modifyCourse", COURSE, "Novo nome", "", "", "", "save", null);

		assertEquals("Novo nome", course.getName());
		assertEquals("novo-nome", course.getInternalName());
	}

	@Test
	public void deleteCutsAllTheLinksAndRemovesTheCourse() throws Exception {
		Subject disciplina1 = new Subject("Teste de Unidade");
		Subject disciplina2 = new Subject("Teste de Integração");
		disciplina1.setCourse(course);
		disciplina2.setCourse(course);
		course.getSubjects().addAll(Arrays.asList(disciplina1, disciplina2));

		User ana = new User("ana", "senha1234", "ana@mail.com", true);
		ana.setUserID(3);
		for (User u : Arrays.asList(student, ana)) {
			u.getInscribedCourses().add(course);
			course.getInscribedUsers().add(u);
		}

		Skill habilidade = new Skill("Testes");
		habilidade.setCourse(course);
		course.getSkills().add(habilidade);
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		chamarPrivado("modifyCourse", COURSE, "x", "x", "x", "x", null, "delete");

		assertNull(disciplina1.getCourse());
		assertNull(disciplina2.getCourse());
		assertTrue(course.getSubjects().isEmpty());
		assertTrue(student.getInscribedCourses().isEmpty());
		assertTrue(ana.getInscribedCourses().isEmpty());
		assertTrue(course.getInscribedUsers().isEmpty());
		assertNull(habilidade.getCourse());
		assertTrue(course.getSkills().isEmpty());
		verify(courseRepository).delete(course);
		verify(courseRepository, never()).save(any(Course.class));
	}

	@Test
	public void noButtonMeansNoChange() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		chamarPrivado("modifyCourse", COURSE, "Novo nome", "x", "x", "x", null, null);

		assertEquals("Qualidade de Software", course.getName());
		verify(courseRepository, never()).save(any(Course.class));
		verify(courseRepository, never()).delete(any(Course.class));
	}

	@Test
	public void saveWinsWhenBothButtonsArrive() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);

		chamarPrivado("modifyCourse", COURSE, "Novo nome", "x", "x", "x", "save", "delete");

		verify(courseRepository).save(course);
		verify(courseRepository, never()).delete(any(Course.class));
	}

	@Test
	public void modifyCourse_unknownCourseIsIgnored() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);
		when(courseRepository.findByInternalName("inexistente")).thenReturn(null);

		ModelAndView mav = chamarPrivado("modifyCourse", "inexistente", "x", "x", "x", "x", "save", null);

		verify(courseRepository, never()).save(any(Course.class));
		verify(courseRepository, never()).delete(any(Course.class));
		assertEquals(REDIRECT, mav.getViewName());
	}

	@Test
	public void modifyCourse_studentsAndAnonymousCannotEditOrDelete() throws Exception {
		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		chamarPrivado("modifyCourse", COURSE, "Novo nome", "x", "x", "x", "save", null);
		chamarPrivado("modifyCourse", COURSE, "x", "x", "x", "x", null, "delete");

		when(sessionUserComponent.getLoggedUser()).thenReturn(null);
		chamarPrivado("modifyCourse", COURSE, "Novo nome", "x", "x", "x", "save", null);

		assertEquals("Qualidade de Software", course.getName());
		verify(courseRepository, never()).save(any(Course.class));
		verify(courseRepository, never()).delete(any(Course.class));
	}

	// ---- viewProfile ----

	@Test
	public void viewProfile_adminSeesCoursesAndTeachers() {
		List<Course> cursos = Arrays.asList(course);
		List<User> professores = new ArrayList<>();
		professores.add(admin);
		when(sessionUserComponent.getLoggedUser()).thenReturn(admin);
		when(courseRepository.findAll()).thenReturn(cursos);
		when(userRepository.findByIsStudent(false)).thenReturn(professores);
		Model model = new ExtendedModelMap();

		String view = controller.viewProfile(model);

		assertEquals("HTML/Admin/admin_page", view);
		assertSame(cursos, model.asMap().get("allCourses"));
		assertSame(professores, model.asMap().get("teachers"));
	}

	@Test
	public void viewProfile_othersGetThePageWithNoData() {
		Model modelAluno = new ExtendedModelMap();
		Model modelAnonimo = new ExtendedModelMap();

		when(sessionUserComponent.getLoggedUser()).thenReturn(student);
		String viewAluno = controller.viewProfile(modelAluno);
		when(sessionUserComponent.getLoggedUser()).thenReturn(null);
		String viewAnonimo = controller.viewProfile(modelAnonimo);

		assertEquals("HTML/Admin/admin_page", viewAluno);
		assertEquals("HTML/Admin/admin_page", viewAnonimo);
		assertTrue(modelAluno.asMap().isEmpty());
		assertTrue(modelAnonimo.asMap().isEmpty());
		verify(courseRepository, never()).findAll();
	}

	// ---- auxiliares ----

	private ModelAndView criarProfessor(String username, String email, String senha) throws Exception {
		return chamarPrivado("createTeacher", "Carlos", "Silva", username, email, senha);
	}

	// Os métodos do controller são private, então vão por reflexão. modifyCourse tem duas
	// versões, por isso o método é escolhido pelo nome e pelo número de argumentos.
	@SuppressWarnings("unchecked")
	private <T> T chamarPrivado(String nome, Object... args) throws Exception {
		for (Method m : AdminController.class.getDeclaredMethods()) {
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
