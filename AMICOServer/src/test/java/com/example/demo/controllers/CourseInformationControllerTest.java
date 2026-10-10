package com.example.demo.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.RedirectView;

import com.example.demo.course.Course;
import com.example.demo.course.CourseRepository;
import com.example.demo.user.SessionUserComponent;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

/**
 * Testes unitários de CourseInformationController.addCourseToUser
 * (inscrição de um usuário em um curso).
 *
 * CourseRepository, UserRepository, SessionUserComponent, Course e User são
 * mocks: nenhum teste usa banco de dados, sessão web ou contexto do Spring.
 *
 * O método guarda a mensagem de erro em campos do controller (message e error)
 * e quem a mostra é course(). Por isso os testes de mensagem chamam course()
 * logo depois, do mesmo jeito que o navegador faz após o redirecionamento.
 */
class CourseInformationControllerTest {

    private static final String NOME_INTERNO = "java-basico";
    private static final String URL_DO_CURSO = "/course/{internalName}";
    private static final String URL_DO_CURSO_COM_ERRO = "/course/{internalName}.html";

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionUserComponent sessionUserComponent;

    @InjectMocks
    private CourseInformationController controller;

    private Model model;

    /** Curso em que o usuário tenta se inscrever (id 1, com 5 inscritos). */
    private Course curso;
    private User usuario;

    // Listas que os mocks devolvem; os testes as preenchem conforme o cenário
    private List<User> inscritosNoCurso;
    private List<Course> cursosDoUsuario;
    private List<Course> cursosConcluidos;

    @BeforeEach
    void preparar() {
        MockitoAnnotations.initMocks(this);
        model = new ExtendedModelMap();

        curso = cursoComId(1L);
        when(curso.getNumberOfUsers()).thenReturn(5);
        inscritosNoCurso = new ArrayList<User>();
        when(curso.getInscribedUsers()).thenReturn(inscritosNoCurso);
        when(courseRepository.findByInternalName(NOME_INTERNO)).thenReturn(curso);

        usuario = mock(User.class);
        when(usuario.getInternalName()).thenReturn("ana");
        cursosDoUsuario = new ArrayList<Course>();
        cursosConcluidos = new ArrayList<Course>();
        when(usuario.getInscribedCourses()).thenReturn(cursosDoUsuario);
        when(usuario.getCompletedCourses()).thenReturn(cursosConcluidos);
    }

    // ---------- helpers ----------

    private Course cursoComId(long id) {
        Course c = mock(Course.class);
        when(c.getCourseID()).thenReturn(id);
        return c;
    }

    private void usuarioLogado() {
        when(sessionUserComponent.isLoggedUser()).thenReturn(true);
        when(sessionUserComponent.getLoggedUser()).thenReturn(usuario);
    }

    private RedirectView inscrever() {
        return controller.addCourseToUser(model, NOME_INTERNO);
    }

    /** Abre a página do curso, que é onde a mensagem e o erro aparecem. */
    private Model paginaDoCurso() {
        when(curso.getStartDate()).thenReturn(new java.sql.Date(0L));
        when(curso.getEndDate()).thenReturn(new java.sql.Date(0L));
        Model pagina = new ExtendedModelMap();
        controller.course(pagina, NOME_INTERNO);
        return pagina;
    }

    private void assertNadaFoiSalvo() {
        verify(userRepository, never()).save(usuario);
        verify(courseRepository, never()).save(curso);
        verify(curso, never()).setNumberOfUsers(6);
    }

    // ---------- visitante sem login ----------

    @Test
    void visitanteSemLogin_voltaParaAPaginaDoCurso() {
        when(sessionUserComponent.isLoggedUser()).thenReturn(false);

        RedirectView resposta = inscrever();

        assertEquals(URL_DO_CURSO, resposta.getUrl());
    }

    @Test
    void visitanteSemLogin_naoConsultaNemSalvaNada() {
        when(sessionUserComponent.isLoggedUser()).thenReturn(false);

        inscrever();

        verify(courseRepository, never()).findByInternalName(NOME_INTERNO);
        assertNadaFoiSalvo();
    }

    @Test
    void visitanteSemLogin_paginaDoCursoMostraAvisoDeLogin() {
        when(sessionUserComponent.isLoggedUser()).thenReturn(false);
        inscrever();

        Model pagina = paginaDoCurso();

        assertEquals(Boolean.TRUE, pagina.asMap().get("error"));
        String mensagem = (String) pagina.asMap().get("message");
        assertTrue(mensagem.startsWith("To register for a course it is necessary to be logged into the system."));
    }

    // ---------- inscrição com sucesso ----------

    @Test
    void usuarioSemHistorico_inscreveEVaiParaOPerfil() {
        usuarioLogado();

        RedirectView resposta = inscrever();

        assertEquals("/profile/ana", resposta.getUrl());
    }

    @Test
    void usuarioSemHistorico_salvaUsuarioECurso() {
        usuarioLogado();

        inscrever();

        verify(userRepository).save(usuario);
        verify(courseRepository).save(curso);
    }

    @Test
    void usuarioSemHistorico_somaUmAoNumeroDeUsuariosDoCurso() {
        usuarioLogado();

        inscrever();

        verify(curso).setNumberOfUsers(6);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void usuarioSemHistorico_cursoEntraNaListaDeCursosDoUsuario() {
        usuarioLogado();

        inscrever();

        ArgumentCaptor<List> capturado = ArgumentCaptor.forClass(List.class);
        verify(usuario).setInscribedCourses(capturado.capture());
        assertEquals(Arrays.asList(curso), capturado.getValue());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void usuarioSemHistorico_usuarioEntraNaListaDeInscritosDoCurso() {
        usuarioLogado();

        inscrever();

        ArgumentCaptor<List> capturado = ArgumentCaptor.forClass(List.class);
        verify(curso).setInscribedUsers(capturado.capture());
        assertEquals(Arrays.asList(usuario), capturado.getValue());
    }

    @Test
    void usuarioComOutrasInscricoes_mantemAsAnterioresEAdicionaOCursoNoFim() {
        usuarioLogado();
        Course outro = cursoComId(2L);
        cursosDoUsuario.add(outro);
        User outroUsuario = mock(User.class);
        inscritosNoCurso.add(outroUsuario);

        RedirectView resposta = inscrever();

        assertEquals("/profile/ana", resposta.getUrl());
        assertEquals(Arrays.asList(outro, curso), cursosDoUsuario);
        assertEquals(Arrays.asList(outroUsuario, usuario), inscritosNoCurso);
        verify(usuario).setInscribedCourses(cursosDoUsuario);
        verify(curso).setInscribedUsers(inscritosNoCurso);
    }

    @Test
    void usuarioQueConcluiuOutroCurso_aindaPodeSeInscreverNesteCurso() {
        usuarioLogado();
        Course concluido = cursoComId(3L);
        cursosDoUsuario.add(concluido);
        cursosConcluidos.add(concluido);

        RedirectView resposta = inscrever();

        assertEquals("/profile/ana", resposta.getUrl());
        assertEquals(Arrays.asList(concluido, curso), cursosDoUsuario);
        verify(userRepository).save(usuario);
    }
}