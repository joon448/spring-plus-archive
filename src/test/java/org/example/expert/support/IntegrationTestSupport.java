package org.example.expert.support;

import org.example.expert.config.JwtUtil;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {
	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected JwtUtil jwtUtil;

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	protected TodoRepository todoRepository;

	@Autowired
	protected CommentRepository commentRepository;

	protected User saveUser(String email, String password, String nickname, UserRole userRole){
		return  userRepository.save(new User(email, password, nickname, userRole));
	}

	protected Todo saveTodo(String title, String contents, String weather, User user){
		return todoRepository.save(new Todo(title, contents, weather, user));
	}

	protected Comment saveComment(String contents, User user, Todo todo){
		return commentRepository.save(new Comment(contents, user, todo));
	}

	protected RequestPostProcessor addHeatherBearerToken(User user){
		String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole());
		return request -> {request.addHeader("Authorization", bearerToken); return request;};
	}

}
