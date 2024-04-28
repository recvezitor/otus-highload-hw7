package com.dimas.domain.mapper;

import com.dimas.api.model.ApiDialogMessage;
import com.dimas.api.model.ApiPost;
import com.dimas.api.model.ApiPostCreatePostRequest;
import com.dimas.api.model.ApiPostUpdatePutRequest;
import com.dimas.domain.PostCreate;
import com.dimas.domain.PostUpdate;
import com.dimas.domain.entity.DialogMessage;
import com.dimas.domain.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA;


@Mapper(componentModel = JAKARTA, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DialogMessageMapper {

    @Mapping(source = "fromUser", target = "from")
    @Mapping(source = "toUser", target = "to")
    ApiDialogMessage map(DialogMessage source);

//    Post map(PostCreate source);
//
//    PostCreate map(ApiPostCreatePostRequest source);
//
//    PostUpdate map(ApiPostUpdatePutRequest source);


}
