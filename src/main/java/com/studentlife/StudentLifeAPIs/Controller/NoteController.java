package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.NoteRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.Service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createNote(@RequestBody @Valid NoteRequest request) {
        return ResponseEntity.status(201).body(noteService.createNote(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyNotes(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(noteService.getMyNotes(categoryId, search, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getNoteById(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateNote(
            @PathVariable Long id,
            @RequestBody @Valid NoteRequest request
    ) {
        return ResponseEntity.ok(noteService.updateNote(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteNote(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.deleteNote(id));
    }
}
