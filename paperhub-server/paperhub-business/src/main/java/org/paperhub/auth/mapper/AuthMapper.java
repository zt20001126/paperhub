package org.paperhub.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.paperhub.auth.po.SysUser;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthMapper extends BaseMapper<SysUser> {
}
