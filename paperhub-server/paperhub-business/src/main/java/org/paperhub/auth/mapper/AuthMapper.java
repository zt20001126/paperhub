package org.paperhub.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.paperhub.auth.entity.SysUser;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthMapper extends BaseMapper<SysUser> {
}
