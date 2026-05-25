package com.example.nhom3_tt_.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nhom3_tt_.controllers.ProfileController;
import com.example.nhom3_tt_.dtos.requests.profile.EditProfileRequest;
import com.example.nhom3_tt_.dtos.response.profile.EditProfileResponse;
import com.example.nhom3_tt_.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
public class ProfileControllerTest {

  @Mock private UserService userService;

  @InjectMocks private ProfileController profileController;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void getProfile_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(profileController).build();

    EditProfileResponse response = new EditProfileResponse();
    response.setFullname("ABC");
    response.setEmail("acb@example.com");
    response.setPhone("1234567890");
    response.setAvatar("avatar_url");
    response.setPosition("Developer");
    response.setDescription("A passionate developer.");

    when(userService.getProfile(1L)).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/profile/get-profile/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullname").value("ABC"))
        .andExpect(jsonPath("$.email").value("acb@example.com"))
        .andExpect(jsonPath("$.phone").value("1234567890"))
        .andExpect(jsonPath("$.avatar").value("avatar_url"))
        .andExpect(jsonPath("$.position").value("Developer"))
        .andExpect(jsonPath("$.description").value("A passionate developer."));

    verify(userService, times(1)).getProfile(1L);
  }

//  @Test
//  void editProfile_success() throws Exception {
//    mockMvc = MockMvcBuilders.standaloneSetup(profileController).build();
//
//    EditProfileRequest request = new EditProfileRequest();
//    request.setFullname("ABC");
//    request.setEmail("acb@example.com");
//    request.setPhone("1234567890");
//    //        request.setAvatar("avatar_url");
//    request.setPosition("Developer");
//    request.setDescription("A passionate developer.");
//
//    EditProfileResponse response = new EditProfileResponse();
//    response.setFullname("ABC");
//    response.setEmail("acb@example.com");
//    response.setPhone("1234567890");
//    response.setAvatar("avatar_url");
//    response.setPosition("Developer");
//    response.setDescription("A passionate developer.");
//
//    when(userService.editProfile(any(EditProfileRequest.class), any(MultipartFile.class)))
//        .thenReturn(response);
//
//    mockMvc
//        .perform(
//            put("/api/v1/profile/edit-profile")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.fullname").value("ABC"))
//        .andExpect(jsonPath("$.email").value("acb@example.com"))
//        .andExpect(jsonPath("$.phone").value("1234567890"))
//        .andExpect(jsonPath("$.avatar").value("avatar_url"))
//        .andExpect(jsonPath("$.position").value("Developer"))
//        .andExpect(jsonPath("$.description").value("A passionate developer."));
//
//    verify(userService, times(1))
//        .editProfile(any(EditProfileRequest.class), any(MultipartFile.class));
//  }
}
