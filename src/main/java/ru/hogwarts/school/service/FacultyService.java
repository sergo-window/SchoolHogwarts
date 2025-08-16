package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.dto.FacultyWithStudents;
import ru.hogwarts.school.dto.StudentWithoutFaculty;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.Collection;
import java.util.List;


@Service
public class FacultyService {

    @Autowired
    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        return facultyRepository.save(faculty);
    }

    public Faculty findFaculty(long id) {
        return facultyRepository.findById(id).get();
    }

    public Faculty editFaculty(Faculty faculty) {
        return facultyRepository.save(faculty);
    }

    public void deleteFaculty(long id) {
        facultyRepository.deleteById(id);
    }

    public Collection<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }

    public Collection<Faculty> findByNameIgnoreCase(String name) {
        return facultyRepository.findByNameIgnoreCase(name);
    }

    public Collection<Faculty> findByColorIgnoreCase(String color) {
        return facultyRepository.findByColorIgnoreCase(color);
    }

    public Collection<Faculty> findByNameOrColorIgnoreCase(String nameOrColor) {
        return facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(nameOrColor, nameOrColor);
    }

    public List<FacultyWithStudents> facultyWithStudents() {
        return facultyRepository.findAll().stream()
                .map(faculty -> {
                    List<StudentWithoutFaculty> list = faculty.getStudents().stream()
                            .map(it -> new StudentWithoutFaculty(it.getId(), it.getName(), it.getAge()))
                            .toList();
                    return new FacultyWithStudents(faculty.getId(), faculty.getName(), faculty.getColor(), list);
                })
                .toList();
    }
}
