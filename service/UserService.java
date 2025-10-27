package com.aurionpro.app.service;

import com.aurionpro.app.dto.ChangePasswordDto;
import com.aurionpro.app.entity.user.User;

public interface UserService {
    User getCurrentUser();

	void changePassword(ChangePasswordDto changePasswordDto);
}