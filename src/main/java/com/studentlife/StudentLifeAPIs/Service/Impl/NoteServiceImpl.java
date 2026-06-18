package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.NoteRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NoteResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.Entity.NoteCategory;
import com.studentlife.StudentLifeAPIs.Entity.Notes;
import com.studentlife.StudentLifeAPIs.Mapper.NoteMapper;
import com.studentlife.StudentLifeAPIs.Repository.NoteCategoryRepository;
import com.studentlife.StudentLifeAPIs.Repository.NotesRepository;
import com.studentlife.StudentLifeAPIs.Service.NoteService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NotesRepository notesRepository;
    private final NoteCategoryRepository noteCategoryRepository;
    private final NoteMapper noteMapper;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<NoteResponse> createNote(NoteRequest request) {
        var currentUser = authUtil.getAuthenticatedUser();

        Notes note = noteMapper.toEntity(request);
        note.setUser(currentUser);

        if (request.getCategoryId() != null) {
            NoteCategory category = noteCategoryRepository
                    .findByIdAndUserId(request.getCategoryId(), currentUser.getId())
                    .orElseThrow(() -> notFound("Category not found."));
            note.setCategory(category);
        }

        notesRepository.save(note);

        return new ApiResponse<>(
            201, 
            true, 
            "Note created successfully.", 
            noteMapper.toResponse(note)
        );
    }

    @Override
    public ApiResponse<PaginatedResponse<NoteResponse>> getMyNotes(
            Long categoryId, String search, int page, int size
    ) {
        var currentUser = authUtil.getAuthenticatedUser();

        Page<NoteResponse> resultPage = notesRepository
                .findByFilter(currentUser.getId(), categoryId, search, PageRequest.of(page, size))
                .map(noteMapper::toResponse);

        return new ApiResponse<>(
            200, 
            true, 
            "Notes retrieved successfully.", 
            PaginatedResponse.from(resultPage)
        );
    }

    @Override
    public ApiResponse<NoteResponse> getNoteById(Long id) {
        var currentUser = authUtil.getAuthenticatedUser();

        Notes note = notesRepository
                .findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> notFound("Note not found."));

        return new ApiResponse<>(
            200, 
            true,
            "Note retrieved successfully.", 
            noteMapper.toResponse(note)
        );
    }

    @Override
    public ApiResponse<NoteResponse> updateNote(Long id, NoteRequest request) {
        var currentUser = authUtil.getAuthenticatedUser();

        Notes note = notesRepository
                .findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> notFound("Note not found."));

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());

        if (request.getCategoryId() != null) {
            NoteCategory category = noteCategoryRepository
                    .findByIdAndUserId(request.getCategoryId(), currentUser.getId())
                    .orElseThrow(() -> notFound("Category not found."));
            note.setCategory(category);
        } else {
            note.setCategory(null);
        }

        notesRepository.save(note);

        return new ApiResponse<>(
            200, 
            true, 
            "Note updated successfully.", 
            noteMapper.toResponse(note)
        );
    }

    @Override
    public ApiResponse<?> deleteNote(Long id) {
        var currentUser = authUtil.getAuthenticatedUser();

        Notes note = notesRepository
                .findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> notFound("Note not found."));

        notesRepository.delete(note);

        return new ApiResponse<>(
            200, 
            true, 
            "Note deleted successfully."
        );
    }
}
