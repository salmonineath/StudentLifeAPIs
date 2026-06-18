package com.studentlife.StudentLifeAPIs.Mapper;

import com.studentlife.StudentLifeAPIs.DTO.Request.NoteRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.NoteResponse;
import com.studentlife.StudentLifeAPIs.Entity.Notes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class, uses = NoteCategoryMapper.class)
public interface NoteMapper {

    // ── Request → Entity ──────────────────────────────────────────────────────

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "user",      ignore = true)
    @Mapping(target = "category",  ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Notes toEntity(NoteRequest request);

    // ── Entity → Response ─────────────────────────────────────────────────────

    NoteResponse toResponse(Notes note);
}
