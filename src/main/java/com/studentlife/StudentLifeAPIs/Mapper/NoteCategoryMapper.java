package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.DTO.Request.NoteCategoryRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.NoteCategoryResponse;
import com.studentlife.StudentLifeAPIs.Entity.NoteCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface NoteCategoryMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "user",      ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NoteCategory toEntity(NoteCategoryRequest request);

    NoteCategoryResponse toResponse(NoteCategory category);
}
