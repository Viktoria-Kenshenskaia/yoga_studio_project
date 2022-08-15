package com.example.yogastudioproject.service;

import com.example.yogastudioproject.domain.model.AppUser;
import com.example.yogastudioproject.domain.model.OneClass;
import com.example.yogastudioproject.dto.OneClassDto;
import com.example.yogastudioproject.repository.AppUserRepo;
import com.example.yogastudioproject.repository.OneClassRepo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OneClassService {
    private final OneClassRepo oneClassRepo;
    private final AppUserRepo appUserRepo;
//    private final CompanyRepo companyRepo;

    public OneClass createClass(OneClassDto classDto) {
        return oneClassRepo.save(oneClassDtoToOneClass(classDto));
    }

    public OneClass updateClass(OneClassDto classDto, Long classId) {
        OneClass oneClass = oneClassDtoToOneClass(classDto);
        oneClass.setClassId(classId);

        return oneClassRepo.save(oneClass);
    }

    public void deleteClassById(Long classId) {
        OneClass oneClass = oneClassRepo.findById(classId).orElseThrow(() -> new RuntimeException("Class not found"));
        oneClassRepo.delete(oneClass);
    }
    private OneClass oneClassDtoToOneClass(OneClassDto oneClassDto) {
        OneClass oneClass = new OneClass();
        AppUser appUser = appUserRepo.findById(oneClassDto.getTeacherId()).orElseThrow(() -> new UsernameNotFoundException("Teacher not found"));
        oneClass.setDateOfClass(LocalDateTime.parse(oneClassDto.getDateOfClass()));
        oneClass.setTeacher(appUser);
        return oneClass;
    }

    public List<OneClass> getAllClasses(Principal principal) {
        AppUser appUser
                = appUserRepo.findAppUserByEmail(principal.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return  oneClassRepo.findAllByCompany(appUser.getCompany());
    }

    public List<OneClass> getAllClassesForTeacherById(Long teacherId) {
        return oneClassRepo.findAllByTeacher(appUserRepo.getReferenceById(teacherId));
    }

    public List<OneClass> getAllClassesFromTo(Principal principal, LocalDateTime from, LocalDateTime to) {
        AppUser appUser
                = appUserRepo.findAppUserByEmail(principal.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return oneClassRepo.findAllByCompanyAndDateOfClassBeforeAndDateOfClassAfter(appUser.getCompany(), from, to);

    }
}