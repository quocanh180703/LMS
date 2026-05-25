package com.example.nhom3_tt_.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nhom3_tt_.controllers.CartItemController;
import com.example.nhom3_tt_.dtos.requests.CartItemRequest;
import com.example.nhom3_tt_.dtos.response.CartItemResponse;
import com.example.nhom3_tt_.services.CartItemService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class CartItemControllerTest {

  @Mock
  private CartItemService cartItemService;

  @InjectMocks
  private CartItemController cartItemController;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void addCartItem_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(cartItemController).build();
    CartItemResponse cartItemResponse = new CartItemResponse();
    when(cartItemService.addCartItem(any(CartItemRequest.class))).thenReturn(cartItemResponse);

    mockMvc.perform(
            post("/api/v1/carts/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"courseId\":101,\"cartId\":1}"))
        .andExpect(status().isOk());
  }

  @Test
  void createCartItem_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(cartItemController).build();
    CartItemResponse cartItemResponse = new CartItemResponse();
    when(cartItemService.createCartItem(1L, 101L)).thenReturn(cartItemResponse);

    mockMvc.perform(
            post("/api/v1/carts/1/items/101"))
        .andExpect(status().isOk());
  }

  @Test
  void getAllCartItemsByOwn_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(cartItemController)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();

    CartItemResponse cartItemResponse1 = new CartItemResponse();
    cartItemResponse1.setId(1L);
    cartItemResponse1.setCartId(10L);
    cartItemResponse1.setCourseId(101L);

    CartItemResponse cartItemResponse2 = new CartItemResponse();
    cartItemResponse2.setId(2L);
    cartItemResponse2.setCartId(11L);
    cartItemResponse2.setCourseId(102L);

    when(cartItemService.getAllCartItemsByOwn(any(Pageable.class))).thenReturn(
        List.of(cartItemResponse1, cartItemResponse2));

    mockMvc.perform(get("/api/v1/carts/items/me")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].cartId").value(10L))
        .andExpect(jsonPath("$[0].courseId").value(101L))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].cartId").value(11L))
        .andExpect(jsonPath("$[1].courseId").value(102L));

    verify(cartItemService, times(1)).getAllCartItemsByOwn(any(Pageable.class));
  }

  @Test
  void getAllCartItems_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(cartItemController)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();

    CartItemResponse cartItemResponse1 = new CartItemResponse();
    cartItemResponse1.setId(1L);
    cartItemResponse1.setCartId(10L);
    cartItemResponse1.setCourseId(101L);

    CartItemResponse cartItemResponse2 = new CartItemResponse();
    cartItemResponse2.setId(2L);
    cartItemResponse2.setCartId(11L);
    cartItemResponse2.setCourseId(102L);

    when(cartItemService.getAllCartItems(any(Pageable.class))).thenReturn(
        List.of(cartItemResponse1, cartItemResponse2));

    mockMvc.perform(get("/api/v1/carts/items")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].cartId").value(10L))
        .andExpect(jsonPath("$[0].courseId").value(101L))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].cartId").value(11L))
        .andExpect(jsonPath("$[1].courseId").value(102L));

    // Verify service interaction
    verify(cartItemService).getAllCartItems(any(Pageable.class));
  }

  @Test
  void getCartItemsByCartId_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(cartItemController)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();

    CartItemResponse cartItemResponse1 = new CartItemResponse();
    cartItemResponse1.setId(1L);
    cartItemResponse1.setCartId(10L);
    cartItemResponse1.setCourseId(101L);

    CartItemResponse cartItemResponse2 = new CartItemResponse();
    cartItemResponse2.setId(2L);
    cartItemResponse2.setCartId(11L);
    cartItemResponse2.setCourseId(102L);

    Pageable pageable = PageRequest.of(0, 10);

    when(cartItemService.getAllCartItemsByCartId(1L, pageable)).thenReturn(
        List.of(cartItemResponse1, cartItemResponse2));

    mockMvc.perform(get("/api/v1/carts/1/items")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].cartId").value(10L))
        .andExpect(jsonPath("$[0].courseId").value(101L))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].cartId").value(11L))
        .andExpect(jsonPath("$[1].courseId").value(102L));

    verify(cartItemService, times(1)).getAllCartItemsByCartId(1L, pageable);
  }

  @Test
  void deleteCartItem_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(cartItemController).build();
    CartItemResponse cartItemResponse = new CartItemResponse();
    when(cartItemService.deleteCartItem(1L)).thenReturn(cartItemResponse);

    mockMvc.perform(delete("/api/v1/items/1"))
        .andExpect(status().isOk());
  }
}