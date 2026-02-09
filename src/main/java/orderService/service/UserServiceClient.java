package orderService.service;

import orderService.dto.UserDto;

public interface UserServiceClient {

    public UserDto findUserById(Long userId);

}
