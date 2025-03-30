package com.kkulmoo.rebirth.user.application.eventDTO;

import com.kkulmoo.rebirth.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MyDataEvent {
	private final User user;
}