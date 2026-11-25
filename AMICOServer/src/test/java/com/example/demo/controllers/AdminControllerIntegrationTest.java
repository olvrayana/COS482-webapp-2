package com.example.demo.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.course.Course;
import com.example.demo.course.CourseRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerIntegrationTest {

	private static final String NEW_PROFESSOR = "professor1";
	private static final String NEW_PROFESSOR_MAIL = "professor1@mail.com";

	private static final String COURSE_INTERNAL_NAME = "cocina-moderna";

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CourseRepository courseRepository;

	private String originalCourseName;
	private String originalCourseDescription;

	@BeforeEach
	public void guardaEstadoOriginal() {
		Course course = courseRepository.findByInternalName(COURSE_INTERNAL_NAME);
		originalCourseName = course.getName();
		originalCourseDescription = course.getCourseDescription();
	}

	@AfterEach
	public void restauraEstadoOriginal() {
		// Remove o professor criado pelo teste
		User teacher = userRepository.findByUsername(NEW_PROFESSOR);
		if (teacher != null) {
			userRepository.delete(teacher);
		}
		// Restaura o curso (o internalName é recalculado a partir do nome)
		Course course = courseRepository.findByInternalName(COURSE_INTERNAL_NAME);
		if (course == null) {
			course = courseRepository.findByName("Cocina Moderna Editado");
		}
		if (course != null) {
			course.setName(originalCourseName);
			course.setCourseDescription(originalCourseDescription);
			courseRepository.save(course);
		}
	}

	// Faz login e devolve a sessão que carrega o SessionUserComponent.
	private MockHttpSession loginComo(String username, String password) throws Exception {
		MockHttpSession session = new MockHttpSession();
		mvc.perform(get("/api/logIn").with(httpBasic(username, password)).session(session))
				.andExpect(status().isOk());
		return session;
	}

	private void criaProfessor(MockHttpSession session) throws Exception {
		mvc.perform(post("/admin/create/teacher").session(session)
				.param("newName", "Integration")
				.param("newSecondName", "Teacher")
				.param("newUsername", NEW_PROFESSOR)
				.param("newUserMail", NEW_PROFESSOR_MAIL)
				.param("newPassword", "password123"))
				.andExpect(redirectedUrl("/admin/"));
	}

	@Test
	public void adminCriaProfessorEEleFicaPersistido() throws Exception {
		MockHttpSession admin = loginComo("admin", "pass");

		criaProfessor(admin);

		User created = userRepository.findByUsername(NEW_PROFESSOR);
		assertNotNull(created);
		assertFalse(created.isStudent());
		assertEquals(NEW_PROFESSOR_MAIL, created.getUserMail());
	}

	@Test
	public void professorCriadoPeloAdminConsegueFazerLogin() throws Exception {
		criaProfessor(loginComo("admin", "pass"));

		// A senha foi gravada criptografada pelo fluxo real: o login autentica
		loginComo(NEW_PROFESSOR, "password123");
	}

	@Test
	public void alunoNaoConsegueCriarProfessor() throws Exception {
		MockHttpSession aluno = loginComo("student-0", "pass-0");

		criaProfessor(aluno); // o controller redireciona, mas não deve gravar nada

		assertNull(userRepository.findByUsername(NEW_PROFESSOR));
	}

	@Test
	public void adminEditaCursoEMudancaFicaPersistida() throws Exception {
		MockHttpSession admin = loginComo("admin", "pass");

		mvc.perform(post("/admin/modify/course/" + COURSE_INTERNAL_NAME).session(admin)
				.param("newName", "Cocina Moderna Editado")
				.param("newLanguage", "")
				.param("newType", "")
				.param("newDescription", "Descricao editada pelo teste")
				.param("btnSave", "save"))
				.andExpect(redirectedUrl("/admin/"));

		Course edited = courseRepository.findByName("Cocina Moderna Editado");
		assertNotNull(edited);
		assertEquals("Descricao editada pelo teste", edited.getCourseDescription());
	}

	@Test
	public void alunoNaoConsegueEditarCurso() throws Exception {
		MockHttpSession aluno = loginComo("student-0", "pass-0");

		mvc.perform(post("/admin/modify/course/" + COURSE_INTERNAL_NAME).session(aluno)
				.param("newName", "Cocina Moderna Editado")
				.param("newLanguage", "")
				.param("newType", "")
				.param("newDescription", "Descricao editada pelo teste")
				.param("btnSave", "save"))
				.andExpect(redirectedUrl("/admin/"));

		Course unchanged = courseRepository.findByInternalName(COURSE_INTERNAL_NAME);
		assertEquals(originalCourseName, unchanged.getName());
		assertEquals(originalCourseDescription, unchanged.getCourseDescription());
	}
}
