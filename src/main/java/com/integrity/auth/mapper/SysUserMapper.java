package com.integrity.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.integrity.auth.entity.SysUser;

/**
 * sys_users · SysUser 数据访问层（MyBatis-Plus BaseMapper）。
 *
 * <p>承载登录与四级权限（TEACHER/CHAIR/COLLEGE/ADMIN）。
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
}
