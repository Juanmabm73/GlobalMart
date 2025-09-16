package es.codeurjc.global_mart.dto.User;

import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;

import es.codeurjc.global_mart.model.User;

@Mapper (componentModel = "spring")
public interface UserMapper {

    UserDTO toUserDTO(User user);

    List<UserDTO> toUsersDTO(Collection<User> users); // conver a users entity list in to a DTOS user list

    User toUser(UserDTO userDTO);

    UserCartPriceDTO toCartPriceDTO(User user);
}