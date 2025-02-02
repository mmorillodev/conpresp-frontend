package com.conpresp.conprespapi.service;

import com.conpresp.conprespapi.dto.user.UserCreateRequest;
import com.conpresp.conprespapi.dto.user.UserPasswordRequest;
import com.conpresp.conprespapi.dto.user.UserUpdateRequest;
import com.conpresp.conprespapi.entity.user.User;
import com.conpresp.conprespapi.exception.NotEqualsException;
import com.conpresp.conprespapi.exception.PasswordInUseException;
import com.conpresp.conprespapi.exception.ResourceCreationException;
import com.conpresp.conprespapi.repository.GroupRepository;
import com.conpresp.conprespapi.repository.ProfileRepository;
import com.conpresp.conprespapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String createUser(UserCreateRequest userCreateRequest) throws ResourceCreationException, NotEqualsException {
        if (!Objects.equals(userCreateRequest.getPassword(), userCreateRequest.getConfirmPassword())) {
            throw new NotEqualsException();
        }

        var profile = profileRepository.findByName(userCreateRequest.getProfile()).orElseThrow(ResourceCreationException::new);
        var group = groupRepository.findByName(userCreateRequest.getUserGroup()).orElseThrow(ResourceCreationException::new);

        var createdUser = userRepository.save(userCreateRequest.toUser(passwordEncoder, profile, group));

        return createdUser.getId();
    }

    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> getUserById(String uuid) {
        return userRepository.findById(uuid);
    }

    public String getUserProfileByEmail(String email) {
        return userRepository.findByEmail(email).map(userGroup -> userGroup.getUserGroup().getName()).orElse("Não Encontrado");
    }


    public Page<User> search(Specification<User> specification, Pageable pageable) { return userRepository.findAll(specification, pageable); }

    public void deleteById(String uuid) {
        userRepository.deleteById(uuid);
    }

    public User updateUser(String uuid, UserUpdateRequest userUpdateRequest) throws ChangeSetPersister.NotFoundException {
        var profile = profileRepository.findByName(userUpdateRequest.getProfile()).orElseThrow(ChangeSetPersister.NotFoundException::new);
        var userGroup = groupRepository.findByName(userUpdateRequest.getUserGroup()).orElseThrow(ChangeSetPersister.NotFoundException::new);
        var user = userRepository.findById(uuid).orElseThrow(ChangeSetPersister.NotFoundException::new);
        user.setProfile(profile);
        user.setFirstName(userUpdateRequest.getFirstName());
        user.setLastName(userUpdateRequest.getLastName());
        user.setEmail(userUpdateRequest.getEmail());
        user.setUserGroup(userGroup);
        user.setStatus(userUpdateRequest.getStatus());

        userRepository.save(user);

        return user;
    }

    public User updatePassword(String uuid, UserPasswordRequest userPasswordRequest) throws ChangeSetPersister.NotFoundException, NotEqualsException, PasswordInUseException {
        if (!Objects.equals(userPasswordRequest.getPassword(), userPasswordRequest.getConfirmPassword())) {
            throw new NotEqualsException();
        }

        var user = userRepository.findById(uuid).orElseThrow(ChangeSetPersister.NotFoundException::new);

        if (passwordEncoder.matches(userPasswordRequest.getPassword(), user.getPassword())) {
            throw new PasswordInUseException();
        }

        user.setPassword(passwordEncoder.encode(userPasswordRequest.getPassword()));
        userRepository.save(user);

        return user;
    }
}
