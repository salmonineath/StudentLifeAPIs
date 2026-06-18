package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.NoteRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NoteResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;

public interface NoteService {

    ApiResponse<NoteResponse> createNote(NoteRequest request);

    ApiResponse<PaginatedResponse<NoteResponse>> getMyNotes(Long categoryId, String search, int page, int size);

    ApiResponse<NoteResponse> getNoteById(Long id);

    ApiResponse<NoteResponse> updateNote(Long id, NoteRequest request);

    ApiResponse<?> deleteNote(Long id);
}
