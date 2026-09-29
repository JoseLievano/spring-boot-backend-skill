package com.agentForgeBackend.models.hq.admin;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QAdminEntity is a Querydsl query type for AdminEntity
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminEntity extends EntityPathBase<AdminEntity> {

    private static final long serialVersionUID = -106164631L;

    public static final QAdminEntity adminEntity = new QAdminEntity("adminEntity");

    public final com.agentForgeBackend.shared.models.baseUser.QBaseUserEntity _super = new com.agentForgeBackend.shared.models.baseUser.QBaseUserEntity(this);

    //inherited
    public final BooleanPath accountNonExpired = _super.accountNonExpired;

    //inherited
    public final BooleanPath accountNonLocked = _super.accountNonLocked;

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

    public QAdminEntity(String variable) {
        super(AdminEntity.class, forVariable(variable));
    }

    public QAdminEntity(Path<? extends AdminEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAdminEntity(PathMetadata metadata) {
        super(AdminEntity.class, metadata);
    }

}

