package com.kkulmoo.rebirth.card.application;

import java.util.List;

import com.kkulmoo.rebirth.card.infrastrucuture.adapter.dto.CardApiResponse;
import com.kkulmoo.rebirth.user.domain.User;

public interface CardPort {
	List<CardApiResponse> fetchCardData(User user);

}
