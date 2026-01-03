package com.ansh.user.service;

import com.ansh.common.exception.ResourceNotFoundException;
import com.ansh.user.dto.request.UpdateProfileRequest;
import com.ansh.user.dto.response.UserProfileResponse;
import com.ansh.user.entity.UserProfile;
import com.ansh.user.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUserId(String userId) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElse(null);

        if (profile == null) {
            // Create a new empty profile for this user
            profile = createEmptyProfile(userId);
        }

        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileById(String id) {
        UserProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", id));
        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllProfiles() {
        return profileRepository.findAll().stream()
                .map(UserProfileResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyProfile(userId));

        // Update fields if provided
        if (request.getName() != null) {
            profile.setName(request.getName());
        }
        if (request.getEmail() != null) {
            profile.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity());
        }
        if (request.getState() != null) {
            profile.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            profile.setZipCode(request.getZipCode());
        }
        if (request.getCountry() != null) {
            profile.setCountry(request.getCountry());
        }
        if (request.getAvatar() != null) {
            profile.setAvatar(request.getAvatar());
        }
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getNotificationEnabled() != null) {
            profile.setNotificationEnabled(request.getNotificationEnabled());
        }
        if (request.getEmailNotifications() != null) {
            profile.setEmailNotifications(request.getEmailNotifications());
        }

        profile = profileRepository.save(profile);
        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional
    public void deleteProfile(String userId) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));
        profileRepository.delete(profile);
    }

    private UserProfile createEmptyProfile(String userId) {
        String id = "profile_" + UUID.randomUUID().toString().substring(0, 8);
        UserProfile profile = new UserProfile(id, userId);
        return profileRepository.save(profile);
    }
}
