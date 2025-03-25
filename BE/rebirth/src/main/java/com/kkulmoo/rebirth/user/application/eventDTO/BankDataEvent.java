package com.kkulmoo.rebirth.user.application.eventDTO;

import com.kkulmoo.rebirth.user.domain.UserId;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BankDataEvent {
	private final UserId userId;
}
