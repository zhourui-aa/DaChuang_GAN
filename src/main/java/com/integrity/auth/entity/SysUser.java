package com.integrity.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户（表 sys_users）。
 *
 * <p>手册未列用户表，但 auth 服务需要承载「登录 + 四级权限」，
 * 依据手册「只加不改」的数据模型规则，P0 新增本表。
 *
 * <p>四级权限 role 取值：TEACHER（任课教师）/ CHAIR（教研室）/ COLLEGE（学院）/ ADMIN（管理员）。
 */
@Data
@TableName("sys_users")
public class SysUser {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /** 登录名 */
    private String username;

    /** 密码摘要（BCrypt，$2a/$2y 均可校验） */
    private String password;

    /** 姓名（教师侧展示用，不脱敏；学生姓名才脱敏） */
    private String name;

    /** 角色：TEACHER / CHAIR / COLLEGE / ADMIN */
    private String role;

    /** 所属学院 */
    private Long collegeId;

    /** 是否启用：1 启用 / 0 停用 */
    private Integer enabled;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
