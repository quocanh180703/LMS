package com.example.nhom3_tt_.service;

import com.example.nhom3_tt_.dtos.requests.subscription.SubscriptionCreateRequest;
import com.example.nhom3_tt_.dtos.requests.subscription.SubscriptionDeleteRequest;
import com.example.nhom3_tt_.dtos.response.PageResponse;
import com.example.nhom3_tt_.dtos.response.instructor.InstructorDetailInfo;
import com.example.nhom3_tt_.dtos.response.instructor.StudentDetailInfo;
import com.example.nhom3_tt_.exception.AppException;
import com.example.nhom3_tt_.mappers.SubscriptionMapper;
import com.example.nhom3_tt_.models.Subscription;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.repositories.SubscriptionRepository;
import com.example.nhom3_tt_.repositories.UserRepository;

import com.example.nhom3_tt_.services.impl.SubscriptionImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SubscriptionImpl subscriptionService;

    private void mockSecurityContext(String username) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);
    }

    // Test createSubscription

    @Test
    void createSubscription_success() {
        // Arrange
        mockSecurityContext("studentUser");

        SubscriptionCreateRequest request = new SubscriptionCreateRequest();
        request.setInstructor_id(1L);

        User instructor = new User();
        instructor.setId(1L);

        User student = new User();
        student.setId(2L);

        Subscription subscription = new Subscription();

        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(userRepository.findByUsername("studentUser")).thenReturn(Optional.of(student));
        when(subscriptionMapper.toSubscription(request)).thenReturn(subscription);

        // Act
        String result = subscriptionService.createSubscription(request);

        // Assert
        assertEquals("Subscribe successful", result);
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void createSubscription_instructorNotFound() {
        // Arrange
        mockSecurityContext("studentUser");

        SubscriptionCreateRequest request = new SubscriptionCreateRequest();
        request.setInstructor_id(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AppException.class, () -> subscriptionService.createSubscription(request));
    }

    @Test
    void createSubscription_userNotFound() {
        // Arrange
        mockSecurityContext("studentUser");

        SubscriptionCreateRequest request = new SubscriptionCreateRequest();
        request.setInstructor_id(1L);

        User instructor = new User();
        instructor.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(userRepository.findByUsername("studentUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AppException.class, () -> subscriptionService.createSubscription(request));
    }

    // Test deleteSubscription

    @Test
    void deleteSubscription_success() {
        // Arrange
        mockSecurityContext("studentUser");

        SubscriptionDeleteRequest request = new SubscriptionDeleteRequest();
        request.setInstructor_id(1L);

        User instructor = new User();
        instructor.setId(1L);

        User student = new User();
        student.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(userRepository.findByUsername("studentUser")).thenReturn(Optional.of(student));

        // Act
        assertDoesNotThrow(() -> subscriptionService.deleteSubscription(request));

        // Assert
        verify(subscriptionRepository).deleteByStudentAndInstructor(student, instructor);
    }

    @Test
    void deleteSubscription_instructorNotFound() {
        // Arrange
        mockSecurityContext("studentUser");

        SubscriptionDeleteRequest request = new SubscriptionDeleteRequest();
        request.setInstructor_id(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AppException.class, () -> subscriptionService.deleteSubscription(request));
    }

    // Test getAllSubscriptions

    @Test
    void getAllSubscriptions_success() {
        // Arrange
        mockSecurityContext("studentUser");

        User user = new User();
        user.setId(2L);

        PageRequest pageable = PageRequest.of(0, 10);
        Page<InstructorDetailInfo> page = new PageImpl<>(List.of(new InstructorDetailInfo()));

        when(userRepository.findByUsername("studentUser")).thenReturn(Optional.of(user));
        when(subscriptionRepository.findAllFollowedUsersByUserId(2L, pageable)).thenReturn(page);

        // Act
        PageResponse<?> response = subscriptionService.getAllSubscriptions(1, 10);

        // Assert
        assertNotNull(response);
        verify(subscriptionRepository).findAllFollowedUsersByUserId(2L, pageable);
    }


    @Test
    void getAllSubscriptions_multipleItems() {
        // Arrange
        mockSecurityContext("studentUser");

        User user = new User();
        user.setId(2L);

        PageRequest pageable = PageRequest.of(0, 10);
        InstructorDetailInfo info1 = new InstructorDetailInfo();
        InstructorDetailInfo info2 = new InstructorDetailInfo();
        Page<InstructorDetailInfo> page = new PageImpl<>(List.of(info1, info2));

        when(userRepository.findByUsername("studentUser")).thenReturn(Optional.of(user));
        when(subscriptionRepository.findAllFollowedUsersByUserId(2L, pageable)).thenReturn(page);

        // Act
        PageResponse<?> response = subscriptionService.getAllSubscriptions(1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(2, ((List<?>) response.getItems()).size());
        verify(subscriptionRepository).findAllFollowedUsersByUserId(2L, pageable);
    }

    @Test
    void getAllSubscriptions_userNotFound() {
        // Arrange
        mockSecurityContext("studentUser");

        when(userRepository.findByUsername("studentUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AppException.class, () -> subscriptionService.getAllSubscriptions(1, 10));
    }

    // Test getAllSubscriber

    @Test
    void getAllSubscriber_success() {
        // Arrange
        mockSecurityContext("instructorUser");

        User user = new User();
        user.setId(1L);

        PageRequest pageable = PageRequest.of(0, 10);
        Page<StudentDetailInfo> page = new PageImpl<>(List.of(new StudentDetailInfo()));

        when(userRepository.findByUsername("instructorUser")).thenReturn(Optional.of(user));
        when(subscriptionRepository.findAllFollowedUsersByInstructorId(1L, pageable)).thenReturn(page);

        // Act
        PageResponse<?> response = subscriptionService.getAllSubscriber(1, 10);

        // Assert
        assertNotNull(response);
        verify(subscriptionRepository).findAllFollowedUsersByInstructorId(1L, pageable);
    }

    @Test
    void getAllSubscriber_multipleItems() {
        // Arrange
        mockSecurityContext("instructorUser");

        User user = new User();
        user.setId(1L);

        PageRequest pageable = PageRequest.of(0, 10);
        StudentDetailInfo student1 = new StudentDetailInfo();
        StudentDetailInfo student2 = new StudentDetailInfo();
        Page<StudentDetailInfo> page = new PageImpl<>(List.of(student1, student2));

        when(userRepository.findByUsername("instructorUser")).thenReturn(Optional.of(user));
        when(subscriptionRepository.findAllFollowedUsersByInstructorId(1L, pageable)).thenReturn(page);

        // Act
        PageResponse<?> response = subscriptionService.getAllSubscriber(1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(2, ((List<?>) response.getItems()).size());
        verify(subscriptionRepository).findAllFollowedUsersByInstructorId(1L, pageable);
    }

    @Test
    void getAllSubscriber_userNotFound() {
        // Arrange
        mockSecurityContext("instructorUser");

        when(userRepository.findByUsername("instructorUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AppException.class, () -> subscriptionService.getAllSubscriber(1, 10));
    }
}
