package com.example.demo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.demo.course.Course;
import com.example.demo.practices.Practices;
import com.example.demo.practices.PracticesRepository;
import com.example.demo.practices.PracticesService;
import com.example.demo.studyItem.StudyItem;
import com.example.demo.subject.Subject;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.user.UserService;

public class UserServiceTest {

	private static final String USERNAME = "joao_silva";
	private static final String PASSWORD = "senha123";
	private static final String EMAIL = "joao.silva@exemplo.com.br";

	@Mock
	private UserRepository userRepository;

	@Mock
	private PracticesRepository practicesRepository;

	@Mock
	private PracticesService practicesService;

	@InjectMocks
	private UserService userService;

	private User existingUser;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		existingUser = new User("usuario_antigo", "senhaantiga", "antigo@exemplo.com.br", true);
		existingUser.setUserFirstName("Ana");
		existingUser.setUserLastName("Oliveira");
		existingUser.setUserAddress("Rua das Palmeiras, 45");
		existingUser.setCity("Curitiba");
		existingUser.setCountry("Brasil");
		existingUser.setPhoneNumber("123456789");
		existingUser.setInterests("Leitura");
		existingUser.setUrlProfileImage("foto-perfil-antiga.jpg");
	}

	@Test
	public void passwordMatch_acceptsMatchingPasswordsAtLengthBoundaries() {
		assertTrue(userService.passwordMatch("12345678", "12345678"));
		assertTrue(userService.passwordMatch("12345678901234", "12345678901234"));
	}

	@Test
	public void passwordMatch_rejectsMismatchedOrOutOfRangePasswords() {
		assertFalse(userService.passwordMatch("12345678", "87654321"));
		assertFalse(userService.passwordMatch("1234567", "1234567"));
		assertFalse(userService.passwordMatch("123456789012345", "123456789012345"));
	}

	@Test
	public void correctName_acceptsAllowedCharactersAndLength() {
		assertTrue(userService.correctName("joao_silva"));
		assertTrue(userService.correctName("abcde-123"));
	}

	@Test
	public void correctName_rejectsInvalidLengthOrCharacters() {
		assertFalse(userService.correctName("abcd"));
		assertFalse(userService.correctName("abcdefghijklmnop"));
		assertFalse(userService.correctName("valid.user"));
	}

	@Test
	public void isValidEmailAddress_acceptsAWellFormedAddress() {
		assertTrue(userService.isValidEmailAddress("joao.silva+estudos@exemplo.com.br"));
	}

	@Test
	public void isValidEmailAddress_rejectsMalformedAddress() {
		assertFalse(userService.isValidEmailAddress("not-an-email"));
	}

	@Test
	public void checkUser_createsStudentWhenAllRegistrationRulesPass() {
		when(userRepository.findByUsername(USERNAME)).thenReturn(null);
		when(userRepository.findByUserMail(EMAIL)).thenReturn(null);

		User created = userService.checkUser(USERNAME, PASSWORD, PASSWORD, EMAIL, false);

		assertNotNull(created);
		assertEquals(USERNAME, created.getUsername());
		assertEquals(EMAIL, created.getUserMail());
		assertTrue(created.isStudent());
		assertFalse(PASSWORD.equals(created.getPassword()));
		verify(userRepository).save(any(User.class));
	}

	@Test
	public void checkUser_createsNonStudentWhenRegistrationIsForAdmin() {
		when(userRepository.findByUsername(USERNAME)).thenReturn(null);
		when(userRepository.findByUserMail(EMAIL)).thenReturn(null);

		User created = userService.checkUser(USERNAME, PASSWORD, PASSWORD, EMAIL, true);

		assertNotNull(created);
		assertFalse(created.isStudent());
		verify(userRepository).save(any(User.class));
	}

	@Test
	public void checkUser_rejectsInvalidCredentialsWithoutRepositoryAccess() {
		assertNull(userService.checkUser("bad.name", PASSWORD, PASSWORD, EMAIL, false));
		assertNull(userService.checkUser(USERNAME, "short", "short", EMAIL, false));
		assertNull(userService.checkUser(USERNAME, PASSWORD, "different1", EMAIL, false));
		assertNull(userService.checkUser(USERNAME, PASSWORD, PASSWORD, "invalid-email", false));

		verifyZeroInteractions(userRepository);
	}

	@Test
	public void checkUser_rejectsExistingUsernameAndEmail() {
		when(userRepository.findByUsername(USERNAME)).thenReturn(existingUser);

		assertNull(userService.checkUser(USERNAME, PASSWORD, PASSWORD, EMAIL, false));
		verify(userRepository, never()).findByUserMail(EMAIL);
		verify(userRepository, never()).save(any(User.class));

		when(userRepository.findByUsername(USERNAME)).thenReturn(null);
		when(userRepository.findByUserMail(EMAIL)).thenReturn(existingUser);

		assertNull(userService.checkUser(USERNAME, PASSWORD, PASSWORD, EMAIL, false));
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	public void updateUser_appliesValidProfileChangesAndNormalizesUsername() {
		User changes = new User();
		changes.setUsername("heleno_prof");
		changes.setPassword("novasenha");
		changes.setUserMail("Heleno.professor@exemplo.com.br");
		changes.setUserFirstName("Heleno");
		changes.setUserLastName("Professor");
		changes.setUserAddress("Avenida Brasil, 123");
		changes.setCity("Montes Claros");
		changes.setCountry("Brasil");
		changes.setPhoneNumber("999876543");
		changes.setInterests("Futebol e Tigrinho");
		changes.setUrlProfileImage("foto-perfil-nova.jpg");

		when(userRepository.findByInternalName("usuario_antigo")).thenReturn(existingUser);
		when(userRepository.findByUsername("heleno_prof")).thenReturn(null);
		when(userRepository.save(existingUser)).thenReturn(existingUser);

		User updated = userService.updateUser("usuario_antigo", changes);

		assertEquals(existingUser, updated);
		assertEquals("heleno_prof", updated.getUsername());
		assertEquals("heleno_prof", updated.getInternalName());
		assertEquals("Heleno.professor@exemplo.com.br", updated.getUserMail());
		assertEquals("Heleno", updated.getUserFirstName());
		assertEquals("Professor", updated.getUserLastName());
		assertEquals("Avenida Brasil, 123", updated.getUserAddress());
		assertEquals("Montes Claros", updated.getCity());
		assertEquals("Brasil", updated.getCountry());
		assertEquals("999876543", updated.getPhoneNumber());
		assertEquals("Futebol e Tigrinho", updated.getInterests());
		assertEquals("foto-perfil-nova.jpg", updated.getUrlProfileImage());
		assertFalse("novasenha".equals(updated.getPassword()));
		verify(userRepository).save(existingUser);
	}

	@Test
	public void updateUser_ignoresInvalidFieldsAndPreservesExistingProfileValues() {
		User changes = new User();
		changes.setUsername("ab");
		changes.setPassword("");
		changes.setUserMail("invalid-email");
		changes.setUserFirstName("Al");
		changes.setUserLastName(stringOfLength(20));
		changes.setUserAddress(stringOfLength(35));
		changes.setCity(stringOfLength(20));
		changes.setCountry(stringOfLength(20));
		changes.setPhoneNumber("12345678");
		changes.setInterests(stringOfLength(50));
		changes.setUrlProfileImage(stringOfLength(80));

		when(userRepository.findByInternalName("usuario_antigo")).thenReturn(existingUser);
		when(userRepository.save(existingUser)).thenReturn(existingUser);

		User updated = userService.updateUser("usuario_antigo", changes);

		assertEquals("usuario_antigo", updated.getUsername());
		assertEquals("antigo@exemplo.com.br", updated.getUserMail());
		assertEquals("Ana", updated.getUserFirstName());
		assertEquals("Oliveira", updated.getUserLastName());
		assertEquals("Rua das Palmeiras, 45", updated.getUserAddress());
		assertEquals("Curitiba", updated.getCity());
		assertEquals("Brasil", updated.getCountry());
		assertEquals("123456789", updated.getPhoneNumber());
		assertEquals("Leitura", updated.getInterests());
		assertEquals("foto-perfil-antiga.jpg", updated.getUrlProfileImage());
		verify(userRepository).save(existingUser);
	}

	@Test
	public void updateUser_returnsNullWhenUserDoesNotExist() {
		when(userRepository.findByInternalName("usuario_inexistente")).thenReturn(null);

		assertNull(userService.updateUser("usuario_inexistente", new User()));

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	public void deleteUser_removesCourseLinksAndDeletesOwnedPracticesBeforeUser() {
		Course cursoConcluido = new Course("Java", "Curso de Java");
		cursoConcluido.setCompleted(true);
		Course cursoEmAndamento = new Course("Spring", "Curso de Spring");
		Subject disciplina = new Subject("Programação");
		disciplina.setCourse(cursoConcluido);
		disciplina.getTeachers().add(existingUser);
		cursoConcluido.getSubjects().add(disciplina);

		existingUser.getInscribedCourses().addAll(Arrays.asList(cursoConcluido, cursoEmAndamento));
		cursoConcluido.getInscribedUsers().add(existingUser);
		cursoEmAndamento.getInscribedUsers().add(existingUser);

		StudyItem atividade = new StudyItem("pratica", "Exercício de Java");
		Practices entregaUm = new Practices("Entrega 1");
		entregaUm.setPracticeID(1);
		entregaUm.setStudyItem(atividade);
		Practices entregaDois = new Practices("Entrega 2");
		entregaDois.setPracticeID(2);
		entregaDois.setStudyItem(atividade);
		List<Practices> entregas = new ArrayList<>(Arrays.asList(entregaUm, entregaDois));

		when(userRepository.findByInternalName("usuario_antigo")).thenReturn(existingUser);
		when(practicesRepository.findByOwner(existingUser)).thenReturn(entregas);

		User removido = userService.deleteUser("usuario_antigo");

		assertEquals(existingUser, removido);
		assertTrue(existingUser.getInscribedCourses().isEmpty());
		assertTrue(cursoConcluido.getInscribedUsers().isEmpty());
		assertTrue(cursoEmAndamento.getInscribedUsers().isEmpty());
		assertTrue(disciplina.getTeachers().isEmpty());

		InOrder ordem = inOrder(practicesService, userRepository);
		ordem.verify(practicesService).deletePracticeSubmission(atividade, entregaUm);
		ordem.verify(practicesService).deletePracticeSubmission(atividade, entregaDois);
		ordem.verify(userRepository).delete(existingUser);
		verify(practicesRepository).findByOwner(existingUser);
	}

	private String stringOfLength(int length) {
		return new String(new char[length]).replace('\0', 'x');
	}
}
