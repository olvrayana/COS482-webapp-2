package com.example.demo.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.user.UserService;

public class SignUpControllerTest {

	private static final String SIGNUP_VIEW = "HTML/LogIn/signup";
	private static final String LOGIN_VIEW = "HTML/LogIn/login";

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private SignUpController controller;

	private Model model;
	private User validUser;
	private User existingUser;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		model = new ExtendedModelMap();
		validUser = new User("validUser", "password123", "test@mail.com", false);
		existingUser = new User("user1", "pass123", "mail@mail.com", false);
	}

	//  signup 

	@Test
	public void signup_initializesModelAndReturnsSignupView() {
		String view = controller.signup(model);

		assertEquals(SIGNUP_VIEW, view);
		assertEquals(false, model.asMap().get("wrongData"));
	}

	// registered: fluxo de sucesso 

	@Test
	public void registered_successRedirectsToLogin() {
		when(userService.checkUser("validUser", "password123", "password123", "test@mail.com", false))
				.thenReturn(validUser);

		String view = controller.registered(model, "validUser", "test@mail.com", "password123", "password123");

		assertEquals(LOGIN_VIEW, view);
	}

	// registered: campos vazios 

	@Test
	public void registered_rejectsEmptyUsername() {
		when(userService.checkUser("", "pass123", "pass123", "mail@mail.com", false)).thenReturn(null);

		String view = controller.registered(model, "", "mail@mail.com", "pass123", "pass123");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("The username can't be empty", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}

	@Test
	public void registered_rejectsEmptyPassword() {
		when(userService.checkUser("user1", "", "", "mail@mail.com", false)).thenReturn(null);

		String view = controller.registered(model, "user1", "mail@mail.com", "", "");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("The password can't be empty", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}

	@Test
	public void registered_rejectsEmptyEmail() {
		when(userService.checkUser("user1", "pass123", "pass123", "", false)).thenReturn(null);

		String view = controller.registered(model, "user1", "", "pass123", "pass123");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("The userMail can't be empty", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}

	// registered: validação de password

	@Test
	public void registered_rejectsDifferentPasswords() {
		when(userService.checkUser("user1", "pass123", "pass999", "mail@mail.com", false)).thenReturn(null);
		when(userService.passwordMatch("pass123", "pass999")).thenReturn(false);

		String view = controller.registered(model, "user1", "mail@mail.com", "pass123", "pass999");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("The passwords are different.", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}

	@Test
	public void registered_rejectsShortPassword() {
		when(userService.checkUser("user1", "short", "short", "mail@mail.com", false)).thenReturn(null);
		when(userService.passwordMatch("short", "short")).thenReturn(false);

		String view = controller.registered(model, "user1", "mail@mail.com", "short", "short");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("The password is too short.", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}

	@Test
	public void registered_rejectsLongPassword() {
		when(userService.checkUser("user1", "verylongpassword123", "verylongpassword123", "mail@mail.com", false))
				.thenReturn(null);
		when(userService.passwordMatch("verylongpassword123", "verylongpassword123")).thenReturn(false);

		String view = controller.registered(model, "user1", "mail@mail.com", "verylongpassword123", "verylongpassword123");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("The password is too long.", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}

	// registered: validação de username 

	@Test
	public void registered_rejectsShortUsername() {
		when(userService.checkUser("abc", "pass123", "pass123", "mail@mail.com", false)).thenReturn(null);
		when(userService.passwordMatch("pass123", "pass123")).thenReturn(true);
		when(userService.correctName("abc")).thenReturn(false);

		String view = controller.registered(model, "abc", "mail@mail.com", "pass123", "pass123");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("The username is too short.", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}

	@Test
	public void registered_rejectsLongUsername() {
		when(userService.checkUser("thisusernameiswaytoolong", "pass123", "pass123", "mail@mail.com", false))
				.thenReturn(null);
		when(userService.passwordMatch("pass123", "pass123")).thenReturn(true);
		when(userService.correctName("thisusernameiswaytoolong")).thenReturn(false);

		String view = controller.registered(model, "thisusernameiswaytoolong", "mail@mail.com", "pass123", "pass123");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("The username is too long.", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}

	@Test
	public void registered_rejectsInvalidCharactersInUsername() {
		when(userService.checkUser("user@name", "pass123", "pass123", "mail@mail.com", false)).thenReturn(null);
		when(userService.passwordMatch("pass123", "pass123")).thenReturn(true);
		when(userService.correctName("user@name")).thenReturn(false);

		String view = controller.registered(model, "user@name", "mail@mail.com", "pass123", "pass123");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("The username only can contains letters, numbers, - or _.", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}

	// registered: validação de email 

	@Test
	public void registered_rejectsInvalidEmailFormat() {
		when(userService.checkUser("user1", "pass123", "pass123", "invalid-email", false)).thenReturn(null);
		when(userService.passwordMatch("pass123", "pass123")).thenReturn(true);
		when(userService.correctName("user1")).thenReturn(true);
		when(userService.isValidEmailAddress("invalid-email")).thenReturn(false);

		String view = controller.registered(model, "user1", "invalid-email", "pass123", "pass123");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("The email address is not correct.", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}

	// registered: registo duplicado 

	@Test
	public void registered_rejectsUsernameAlreadyInUse() {
		when(userService.checkUser("user1", "pass123", "pass123", "mail@mail.com", false)).thenReturn(null);
		when(userService.passwordMatch("pass123", "pass123")).thenReturn(true);
		when(userService.correctName("user1")).thenReturn(true);
		when(userService.isValidEmailAddress("mail@mail.com")).thenReturn(true);
		when(userRepository.findByUsername("user1")).thenReturn(existingUser);

		String view = controller.registered(model, "user1", "mail@mail.com", "pass123", "pass123");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("There is another user with that username.", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}

	@Test
	public void registered_rejectsEmailAlreadyInUse() {
		when(userService.checkUser("user1", "pass123", "pass123", "mail@mail.com", false)).thenReturn(null);
		when(userService.passwordMatch("pass123", "pass123")).thenReturn(true);
		when(userService.correctName("user1")).thenReturn(true);
		when(userService.isValidEmailAddress("mail@mail.com")).thenReturn(true);
		when(userRepository.findByUsername("user1")).thenReturn(null);
		when(userRepository.findByUserMail("mail@mail.com")).thenReturn(existingUser);

		String view = controller.registered(model, "user1", "mail@mail.com", "pass123", "pass123");

		assertEquals(SIGNUP_VIEW, view);
		assertEquals("There is another user with that email address.", model.asMap().get("errorMessage"));
		assertEquals(true, model.asMap().get("wrongData"));
	}
}
