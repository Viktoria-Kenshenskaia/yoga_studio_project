package com.example.yogastudioproject.service;

import com.example.yogastudioproject.domain.model.*;
import com.example.yogastudioproject.domain.payload.request.ClassToSubscription;
import com.example.yogastudioproject.repository.OneClassRepo;
import com.example.yogastudioproject.repository.RoleRepo;
import lombok.RequiredArgsConstructor;
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
    private final RoleRepo roleRepo;
    private final AppUserService appUserService;
    private final CompanyService companyService;
    private final SubscriptionService subscriptionService;

    public OneClass createClass(OneClass oneClass, Principal principal) {
        AppUser appUser = appUserService.getAppUserById(oneClass.getTeacher().getUserId(), principal);
        Role role = roleRepo.findRoleByName("ROLE_TEACHER");
        if (!appUser.getRoles().contains(role))
            throw new RuntimeException("This user cannot be a teacher!");

        oneClass.setCompany(companyService.getCompanyByPrincipal(principal));
        return oneClassRepo.save(oneClass);
    }

    public OneClass updateClass(OneClass oneClassUpdate, Long classId, Principal principal) {
        OneClass oneCLassOld = oneClassRepo.findById(classId).orElseThrow(() -> new RuntimeException("Class not found"));
        if (!isBelongCompany(oneCLassOld, principal))
            throw new RuntimeException("This class cannot be updated!");

        oneClassUpdate.setClassId(classId);

        return oneClassRepo.save(oneClassUpdate);
    }

    public void deleteClass(Long classId, Principal principal) {
        OneClass oneClass = oneClassRepo.findById(classId).orElseThrow(() -> new RuntimeException("Class not found"));
        if (!isBelongCompany(oneClass, principal))
            throw new RuntimeException("This class cannot be deleted!");
        oneClassRepo.delete(oneClass);
    }

    public List<OneClass> getAllClasses(Principal principal) {
        Company company = companyService.getCompanyByPrincipal(principal);
        return  oneClassRepo.findAllByCompany(company);
    }

    public List<OneClass> getAllClassesForTeacher(Long teacherId, Principal principal) {
        AppUser appUser = appUserService.getAppUserById(teacherId, principal);
        if (!appUserService.isBelongCompany(appUser, principal))
            throw new RuntimeException("You can't get this user's details");

        return oneClassRepo.findAllByTeacher(appUser);
    }

    public List<OneClass> getAllClassesFromTo(Principal principal, LocalDateTime classesAfter, LocalDateTime classesBefore) {
        Company company = companyService.getCompanyByPrincipal(principal);
        return oneClassRepo.findAllByCompanyAndDateOfClassAfterAndDateOfClassBefore(company, classesAfter, classesBefore);
    }
    public OneClass getOneClassById(Long classId, Principal principal) {
        OneClass oneClass = oneClassRepo.findById(classId).orElseThrow(() -> new RuntimeException("Class not found"));
        if (!isBelongCompany(oneClass, principal))
            throw new RuntimeException("This class cannot be deleted!");

        return oneClass;
    }

    public void addSubscriptionToClass(ClassToSubscription classToSubscription, Principal principal) {
        Subscription subscription = subscriptionService.getSubscriptionById(classToSubscription.getSubscriptionId(), principal);
        OneClass oneClass = getOneClassById(classToSubscription.getClassId(), principal);

        if (!subscriptionService.isActive(subscription)) {
            throw  new RuntimeException("Subscription is not active");
        };
        oneClass.getSubscription().add(subscription);
        subscription.getVisitedClasses().add(oneClass);

        oneClassRepo.save(oneClass);
    }

    public void removeSubscriptionFromClass(ClassToSubscription classToSubscription, Principal principal) {
        Subscription subscription = subscriptionService.getSubscriptionById(classToSubscription.getSubscriptionId(), principal);
        OneClass oneClass = getOneClassById(classToSubscription.getClassId(), principal);
        oneClass.getSubscription().remove(subscription);

        oneClassRepo.save(oneClass);
    }

    private boolean isBelongCompany(OneClass oneClass, Principal principal) {
        return oneClass.getCompany().equals(companyService.getCompanyByPrincipal(principal));
    }
}
