package com.agentForgeBackend.models.hq.client;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QClientEntity is a Querydsl query type for ClientEntity
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClientEntity extends EntityPathBase<ClientEntity> {

    private static final long serialVersionUID = 202886663L;

    public static final QClientEntity clientEntity = new QClientEntity("clientEntity");

    public final com.agentForgeBackend.shared.models.baseUser.QBaseUserEntity _super = new com.agentForgeBackend.shared.models.baseUser.QBaseUserEntity(this);

    //inherited
    public final BooleanPath accountNonExpired = _super.accountNonExpired;

    //inherited
    public final BooleanPath accountNonLocked = _super.accountNonLocked;

    public final NumberPath<Long> apikey = createNumber("apikey", Long.class);

    //inherited
    public final BooleanPath credentialsNonExpired = _super.credentialsNonExpired;

    //inherited
    public final DateTimePath<java.util.Date> dateCreated = _super.dateCreated;

    //inherited
    public final StringPath email = _super.email;

    //inherited
    public final BooleanPath enabled = _super.enabled;

    //inherited
    public final StringPath firstName = _super.firstName;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.util.Date> lastLogin = _super.lastLogin;

    //inherited
    public final StringPath lastName = _super.lastName;

    //inherited
    public final StringPath password = _super.password;

    //inherited
    public final SetPath<com.agentForgeBackend.shared.models.baseUser.UserRoles, EnumPath<com.agentForgeBackend.shared.models.baseUser.UserRoles>> roles = _super.roles;

    //inherited
    public final StringPath username = _super.username;

    public QClientEntity(String variable) {
        super(ClientEntity.class, forVariable(variable));
    }

    public QClientEntity(Path<? extends ClientEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QClientEntity(PathMetadata metadata) {
        super(ClientEntity.class, metadata);
    }

}

