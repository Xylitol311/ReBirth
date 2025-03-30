package com.kkulmoo.rebirth.card.application;

import com.kkulmoo.rebirth.user.domain.User;

public interface CardPort {
	List<CardApiResponse> fetchCardData(User user);

}
