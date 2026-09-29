package com.agentForgeBackend.shared.models.baseUser;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBaseUserEntity is a Querydsl query type for BaseUserEntity
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBaseUserEntity extends EntityPathBase<BaseUserEntity> {

    private static final long serialVersionUID = -1872940819L;

    public static final QBaseUserEntity baseUserEntity = new QBaseUserEntity("baseUserEntity");

    public final BooleanPath accountNonExpired = createBoolean("accountNonExpired");

    public final BooleanPath accountNonLocked = createBoolean("accountNonLocked");

    public final BooleanPath credentialsNonExpired = createBoolean("credentialsNonExpired");

    public final DateTimePath<java.util.Date> dateCreated = createDateTime("dateCreated", java.util.Date.class);

    public final StringPath email = createString("email");

    public final BooleanPath enabled = createBoolean("enabled");

    public final StringPath firstName = createString("firstName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.util.Date> lastLogin = createDateTime("lastLogin", java.util.Date.class);

    public final StringPath lastName = createString("lastName");

    public final StringPath password = createString("password");

    public final SetPath<UserRoles, EnumPath<UserRoles>> roles = this.<UserRoles, EnumPath<UserRoles>>createSet("roles", UserRoles.class, EnumPath.class, PathInits.DIRECT2);

    public final StringPath username = createString("username");

    public QBaseUserEntity(String variable) {
        super(BaseUserEntity.class, forVariable(variable));
    }

    public QBaseUserEntity(Path<? extends BaseUserEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBaseUserEntity(PathMetadata metadata) {
        super(BaseUserEntity.class, metadata);
    }

}

