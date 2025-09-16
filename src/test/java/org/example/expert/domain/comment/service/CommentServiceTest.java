package org.example.expert.domain.comment.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
	@Mock
	private CommentRepository commentRepository;

	@Mock
	private TodoRepository todoRepository;

	@InjectMocks
	private CommentService commentService;

	@Test
	public void saveComment_성공_시_DTO_반환() {
		//given
		AuthUser authUser = new AuthUser(1L, "user@test.com","user", UserRole.USER);
		CommentSaveRequest commentSaveRequest = new CommentSaveRequest("content");

		User userEntity = User.fromAuthUser(authUser);
		Todo todo = new Todo("title", "contents", "Sunny", userEntity);
		ReflectionTestUtils.setField(todo, "id", 1L);

		Comment comment = new Comment("contents", userEntity, todo);
		ReflectionTestUtils.setField(comment, "id", 1L);

		given(todoRepository.findById(eq(todo.getId()))).willReturn(Optional.of(todo));
		given(commentRepository.save(any(Comment.class))).willReturn(comment);

		//when
		CommentSaveResponse commentSaveResponse = commentService.saveComment(authUser, todo.getId(), commentSaveRequest);

		//then
		assertThat(commentSaveResponse).isNotNull();
		assertThat(commentSaveResponse.getId()).isEqualTo(1L);
		assertThat(commentSaveResponse.getContents()).isEqualTo("contents");
		assertThat(commentSaveResponse.getUser().getEmail()).isEqualTo(userEntity.getEmail());
		verify(commentRepository).save(any(Comment.class));
	}

	@Test
	public void saveComment_Todo_존재하지_않을_시_400_반환() {
		//given
		AuthUser authUser = new AuthUser(1L, "user@test.com","user", UserRole.USER);
		CommentSaveRequest commentSaveRequest = new CommentSaveRequest("content");

		User userEntity = User.fromAuthUser(authUser);
		Todo todo = new Todo("title", "contents", "Sunny", userEntity);
		ReflectionTestUtils.setField(todo, "id", 1L);

		given(todoRepository.findById(eq(todo.getId()))).willReturn(Optional.empty());

		//when & then
		assertThatThrownBy(() -> commentService.saveComment(authUser, todo.getId(), commentSaveRequest))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("Todo not found");
	}

	@Test
	public void getComments_성공_시_List_DTO_반환() {
		//given
		Long todoId = 1L;

		User user = new User("user@test.com", "pw", "user", UserRole.USER);
		Todo todo = new Todo("title", "contents", "Sunny", user);
		ReflectionTestUtils.setField(todo, "id", todoId);

		Comment comment = new Comment("contents", user, todo);
		ReflectionTestUtils.setField(comment, "id", 1L);

		given(commentRepository.findByTodoIdWithUser(eq(todoId)))
			.willReturn(List.of(comment));

		// when
		List<CommentResponse> commentResponses = commentService.getComments(todoId);

		// then
		assertThat(commentResponses).isNotNull();
		assertThat(commentResponses).hasSize(1);
		CommentResponse first = commentResponses.get(0);
		assertThat(first.getId()).isEqualTo(1L);
		assertThat(first.getContents()).isEqualTo("contents");
		assertThat(first.getUser().getEmail()).isEqualTo(user.getEmail());
	}
}
