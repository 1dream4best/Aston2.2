package com.example.assembler;

import com.example.controller.UserController;
import com.example.dto.UserResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler
        implements RepresentationModelAssembler<UserResponse, EntityModel<UserResponse>> {

    @Override
    public EntityModel<UserResponse> toModel(UserResponse user) {

        var userLink = linkTo(
                methodOn(UserController.class).getUserById(user.getId()));

        return EntityModel.of(
                user,
                userLink.withSelfRel(),
                linkTo(methodOn(UserController.class)
                        .getAllUsers()).withRel("users"),
                userLink.withRel("update"),
                userLink.withRel("delete"));
    }
}
