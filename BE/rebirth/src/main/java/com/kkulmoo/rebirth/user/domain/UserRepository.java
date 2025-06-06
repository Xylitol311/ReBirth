package com.kkulmoo.rebirth.user.domain;

public interface UserRepository{
	User findByUserId(UserId userid);
	boolean update(User user);
	User save(User user);
	User findByPhoneSerialNumber(String phoneSerialNumber);
	User findByPhoneSerialNumberAndHashedPinNumber(String phoneSerialNumber, String hashedPinNumber);
}
