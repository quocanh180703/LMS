package com.example.nhom3_tt_.service;

import com.example.nhom3_tt_.dtos.response.section.SectionResponse;
import com.example.nhom3_tt_.exception.NotFoundException;
import com.example.nhom3_tt_.mappers.SectionMapper;
import com.example.nhom3_tt_.models.Section;
import com.example.nhom3_tt_.repositories.SectionRepository;
import com.example.nhom3_tt_.services.impl.SectionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SectionServiceTest {
  @Mock private SectionRepository sectionRepository;
  @Mock private SectionMapper mapper;
  @InjectMocks private SectionServiceImpl sectionService;

  @Test
  void getAll_success() {
    Section section = new Section();
    SectionResponse sectionResponse = new SectionResponse();
    when(sectionRepository.findAll()).thenReturn(List.of(section));
    when(mapper.toSectionResponse(section)).thenReturn(sectionResponse);

    List<SectionResponse> result = sectionService.getAll();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(sectionResponse, result.get(0));

    verify(sectionRepository).findAll();
    verify(mapper).toSectionResponse(section);
  }

  @Test
  void getById_success() {
    Section section = new Section();
    SectionResponse sectionResponse = new SectionResponse();
    when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
    when(mapper.toSectionResponse(section)).thenReturn(sectionResponse);

    SectionResponse result = sectionService.getById(1L);

    assertNotNull(result);
    assertEquals(sectionResponse, result);

    verify(sectionRepository).findById(1L);
    verify(mapper).toSectionResponse(section);
  }

  @Test
  void getById_notFound_throwsException() {
    when(sectionRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> sectionService.getById(1L));
    assertEquals("Section not found", exception.getMessage());

    verify(sectionRepository).findById(1L);
  }

  @Test
  void getEntityById_success() {
    Section section = new Section();
    when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));

    Section result = sectionService.getEntityById(1L);

    assertNotNull(result);
    assertEquals(section, result);

    verify(sectionRepository).findById(1L);
  }

  @Test
  void getEntityById_notFound_throwsException() {
    when(sectionRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> sectionService.getEntityById(1L));
    assertEquals("Section not found", exception.getMessage());

    verify(sectionRepository).findById(1L);
  }

  //  @Test
  //  void create_success() {
  //    Section section = new Section();
  //    SectionResponse sectionResponse = new SectionResponse();
  //    when(sectionRepository.save(section)).thenReturn(section);
  //    when(mapper.toSectionResponse(section)).thenReturn(sectionResponse);
  //
  //    SectionResponse result = sectionService.create(section);
  //
  //    assertNotNull(result);
  //    assertEquals(sectionResponse, result);
  //
  //    verify(sectionRepository).save(section);
  //    verify(mapper).toSectionResponse(section);
  //  }

  //  @Test
  //  void update_success() {
  //    Section section = new Section();
  //    Section existingSection = new Section();
  //    SectionResponse sectionResponse = new SectionResponse();
  //    when(sectionRepository.findById(1L)).thenReturn(Optional.of(existingSection));
  //    when(sectionRepository.save(existingSection)).thenReturn(existingSection);
  //    when(mapper.toSectionResponse(existingSection)).thenReturn(sectionResponse);
  //
  //    SectionResponse result = sectionService.update(1L, section);
  //
  //    assertNotNull(result);
  //    assertEquals(sectionResponse, result);
  //
  //    verify(sectionRepository).findById(1L);
  //    verify(sectionRepository).save(existingSection);
  //    verify(mapper).toSectionResponse(existingSection);
  //  }

  //  @Test
  //  void update_notFound_throwsException() {
  //    Section section = new Section();
  //    when(sectionRepository.findById(1L)).thenReturn(Optional.empty());
  //
  //    NotFoundException exception =
  //        assertThrows(NotFoundException.class, () -> sectionService.update(1L, section));
  //    assertEquals("Section not found", exception.getMessage());
  //
  //    verify(sectionRepository).findById(1L);
  //  }

  //  @Test
  //  void delete_success() {
  //    doNothing().when(sectionRepository).deleteById(1L);
  //
  //    sectionService.delete(1L);
  //
  //    verify(sectionRepository).deleteById(1L);
  //  }
}
