create table
    photo (
        id char(18) not null comment 'id' primary key,
        title varchar(255) null comment '标题',
        file_name varchar(255) null comment '原图地址',
        author varchar(255) null comment '作者',
        width int null comment '原图宽度',
        height int null comment '原图高度',
        shoot_time varchar(255) null comment '拍摄时间',
        aperture varchar(255) null comment '光圈',
        shutter varchar(255) null comment '快门',
        ISO varchar(255) null comment 'iso',
        camera varchar(255) null comment '相机',
        lens varchar(255) null comment '镜头',
        introduce varchar(255) null comment '照片介绍',
        star_rating int null comment '星标',
        created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
        updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
        focal_length varchar(255) null comment '焦距'
    );

create index photo_time_index on photo (shoot_time desc);

create table
    group_photo (
        id char(18) not null comment 'id' primary key,
        title varchar(255) null comment '标题',
        introduce varchar(255) null comment '介绍',
        coverPhotoId char(18) null comment '封面图id',
        created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
        updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
        constraint group_photo_photo_id_fk foreign key (coverPhotoId) references photo (id)
    );

create table
    group_photo_photo (
        groupPhotoId char(18) null comment '组图id',
        photoId char(18) null comment '照片id',
        created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
        updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
        constraint group_photo_photo_group_photo_id_fk foreign key (groupPhotoId) references group_photo (id),
        constraint group_photo_photo_photo_id_fk foreign key (photoId) references photo (id)
    );

create table
    sys_permission (
        id char(18) not null primary key,
        code varchar(100) not null,
        name varchar(50) not null,
        description varchar(200) null,
        created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
        updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
        constraint code unique (code)
    );

create table
    sys_role (
        id char(18) not null primary key,
        name varchar(50) not null,
        description varchar(200) null,
        created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
        updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
        constraint name unique (name)
    );

create table
    sys_role_permission (
        role_id char(18) not null,
        permission_id char(18) not null,
        created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
        updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
        constraint sys_role_permission_sys_permission_id_fk foreign key (permission_id) references sys_permission (id),
        constraint sys_role_permission_sys_role_id_fk foreign key (role_id) references sys_role (id)
    );

create table
    sys_user (
        id char(18) not null primary key,
        username varchar(50) not null,
        password varchar(100) not null,
        status tinyint default 1 null comment '0-禁用, 1-启用',
        created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
        updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
        constraint username unique (username)
    );

create table
    sys_user_role (
        user_id char(18) not null,
        role_id char(18) not null,
        created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
        updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
        constraint sys_user_role_sys_role_id_fk foreign key (role_id) references sys_role (id),
        constraint sys_user_role_sys_user_id_fk foreign key (user_id) references sys_user (id)
    );

create table
    timeline (
        id char(18) not null comment 'id' primary key,
        record_time varchar(255) null comment '时间',
        title varchar(255) null comment '标题',
        text varchar(255) null comment '正文',
        created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
        updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
    );

create index timeline_time_index on timeline (record_time desc);

-- 初始化基础数据
-- 权限
INSERT INTO
    sys_permission (
        id,
        code,
        name,
        description,
        created_time,
        updated_time
    )
VALUES
    ('1', '1', '照片管理', '照片管理', NOW (), NOW ());

INSERT INTO
    sys_permission (
        id,
        code,
        name,
        description,
        created_time,
        updated_time
    )
VALUES
    ('2', '2', '原图浏览', '原图浏览', NOW (), NOW ());

-- 角色
INSERT INTO
    sys_role (id, name, description, created_time, updated_time)
VALUES
    ('1', 'ADMIN', '管理员', NOW (), NOW ());

INSERT INTO
    sys_role (id, name, description, created_time, updated_time)
VALUES
    ('2', 'VIEWER', '访客', NOW (), NOW ());

-- 角色-权限
INSERT INTO
    sys_role_permission (role_id, permission_id)
VALUES
    ('1', '1');

INSERT INTO
    sys_role_permission (role_id, permission_id)
VALUES
    ('2', '2');

-- 用户
INSERT INTO
    sys_user (
        id,
        username,
        password,
        status,
        created_time,
        updated_time
    )
VALUES
    (
        '1',
        'root',
        '$2a$10$jndgC.sZFv0volqxQeXdk.NGN.K7Smrko/5UtP33pPVUcQdP0604a',
        1,
        NOW (),
        NOW ()
    );

INSERT INTO
    sys_user (
        id,
        username,
        password,
        status,
        created_time,
        updated_time
    )
VALUES
    (
        '2',
        'viewer',
        '$2a$10$jndgC.sZFv0volqxQeXdk.NGN.K7Smrko/5UtP33pPVUcQdP0604a',
        1,
        NOW (),
        NOW ()
    );

-- 用户-角色
INSERT INTO
    sys_user_role (user_id, role_id)
VALUES
    ('1', '1');

INSERT INTO
    sys_user_role (user_id, role_id)
VALUES
    ('2', '2');