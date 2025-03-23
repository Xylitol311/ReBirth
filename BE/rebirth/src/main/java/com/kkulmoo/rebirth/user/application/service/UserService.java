package com.kkulmoo.rebirth.user.application.service;

import org.springframework.stereotype.Service;

import com.kkulmoo.rebirth.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

}
