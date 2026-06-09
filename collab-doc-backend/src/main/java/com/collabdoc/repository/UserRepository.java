package com.collabdoc.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.collabdoc.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRepository extends BaseMapper<User> {
}
