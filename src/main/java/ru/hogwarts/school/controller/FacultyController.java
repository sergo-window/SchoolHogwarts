package ru.hogwarts.school.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.FacultyWithStudents;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.service.FacultyService;

import java.util.Collection;
import java.util.List;


@RestController
@RequestMapping("/faculty")
public class FacultyController {
    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping("{id}")
    public ResponseEntity<Faculty> getFacultyInfo(@PathVariable Long id) {
        Faculty faculty = facultyService.findFaculty(id);
        if (faculty == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(faculty);
    }

    @PostMapping
    public Faculty createFaculty(@RequestBody Faculty faculty) {
        return facultyService.createFaculty(faculty);
    }

    @PutMapping
    public ResponseEntity<Faculty> editFaculty(@RequestBody Faculty faculty) {
        Faculty foundFaculty = facultyService.editFaculty(faculty);
        if (foundFaculty == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(foundFaculty);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Collection<Faculty>> getAllFaculties() {
        return ResponseEntity.ok(facultyService.getAllFaculties());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Collection<Faculty>> getFacultiesByNameIgnoreCase(@PathVariable String name) {
        Collection<Faculty> faculties = facultyService.findByNameIgnoreCase(name);
        if (faculties.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(faculties);
    }

    @GetMapping("/color-ignore-case/{color}")
    public ResponseEntity<Collection<Faculty>> getFacultiesByColorIgnoreCase(@PathVariable String color) {
        Collection<Faculty> faculties = facultyService.findByColorIgnoreCase(color);
        if (faculties.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(faculties);
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<Faculty>> searchFaculties(@RequestParam String nameOrColor) {
        Collection<Faculty> faculties = facultyService.findByNameOrColorIgnoreCase(nameOrColor);
        if (faculties.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(faculties);
    }

    @GetMapping("/faculty-students")
    public List<FacultyWithStudents> facultyWithStudents() {
        return facultyService.facultyWithStudents();
    }
}
